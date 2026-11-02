package at.fhcampuswien.masterprj.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.security.MessageDigest

/**
 * Access control for the study backend. It replaces the former CorsFilter, which
 * allowed every origin and let anyone read and write the stored sessions.
 *
 * - POST /api/tracking/sessions needs the write token (header X-Study-Token).
 *   The study builds of both apps carry it. It is no secret from someone who
 *   unpacks the public web app, but it keeps out writes from anything else; the
 *   analysis attributes a session only through the session log in any case.
 * - Every other /api/tracking endpoint, the actuator and the API docs need the
 *   read token (Authorization: Bearer ...), which never ships in an app. It is
 *   what keeps the participants' records from being read by third parties.
 * - GET /api/tracking/health stays open as the liveness check.
 * - CORS is granted only to the configured origins (the deployed PWA).
 *
 * An unset token rejects every request it guards, so a deployment that forgot
 * to configure one fails closed rather than open.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class StudyAccessFilter(
    @Value("\${study.write-token:}") private val writeToken: String,
    @Value("\${study.read-token:}") private val readToken: String,
    @Value("\${study.allowed-origins:}") allowedOrigins: String,
) : Filter {

    private val origins: Set<String> =
        allowedOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse
        val path = req.requestURI.removePrefix(req.contextPath)

        val origin = req.getHeader("Origin")
        if (origin != null && origin in origins) {
            res.setHeader("Access-Control-Allow-Origin", origin)
            res.setHeader("Vary", "Origin")
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            res.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Study-Token")
            res.setHeader("Access-Control-Max-Age", "3600")
        }

        if (req.method.equals("OPTIONS", ignoreCase = true)) {
            res.status = HttpServletResponse.SC_OK
            return
        }

        val allowed = when {
            path == "/api/tracking/health" -> true
            path == "/actuator/health" -> true
            req.method == "POST" && path == "/api/tracking/sessions" ->
                matches(req.getHeader("X-Study-Token"), writeToken)
            path.startsWith("/api/") || path.startsWith("/actuator") ||
                path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") ->
                matches(req.getHeader("Authorization")?.removePrefix("Bearer ")?.trim(), readToken)
            else -> true
        }

        if (!allowed) {
            res.status = HttpServletResponse.SC_UNAUTHORIZED
            return
        }
        chain.doFilter(request, response)
    }

    /** Constant-time comparison; an unconfigured token matches nothing. */
    private fun matches(given: String?, expected: String): Boolean {
        if (expected.isEmpty() || given.isNullOrEmpty()) return false
        return MessageDigest.isEqual(given.toByteArray(), expected.toByteArray())
    }
}
