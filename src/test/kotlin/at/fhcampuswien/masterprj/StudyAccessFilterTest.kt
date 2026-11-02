package at.fhcampuswien.masterprj

import at.fhcampuswien.masterprj.config.StudyAccessFilter
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StudyAccessFilterTest {

    private val pwa = "https://master-prj-pwa.vercel.app"
    private val filter = StudyAccessFilter("write-secret", "read-secret", pwa)

    /** Runs the filter; returns the response and whether the request got through. */
    private fun run(
        f: StudyAccessFilter, method: String, path: String,
        headers: Map<String, String> = emptyMap(),
    ): Pair<MockHttpServletResponse, Boolean> {
        val req = MockHttpServletRequest(method, path)
        headers.forEach { (k, v) -> req.addHeader(k, v) }
        val res = MockHttpServletResponse()
        val chain = MockFilterChain()
        f.doFilter(req, res, chain)
        return res to (chain.request != null)
    }

    @Test
    fun `a session is accepted only with the write token`() {
        assertEquals(401, run(filter, "POST", "/api/tracking/sessions").first.status)
        assertEquals(401, run(filter, "POST", "/api/tracking/sessions",
            mapOf("X-Study-Token" to "wrong")).first.status)
        assertEquals(true, run(filter, "POST", "/api/tracking/sessions",
            mapOf("X-Study-Token" to "write-secret")).second)
    }

    @Test
    fun `the write token does not open the records`() {
        val (res, passed) = run(filter, "GET", "/api/tracking/sessions",
            mapOf("Authorization" to "Bearer write-secret"))
        assertEquals(401, res.status)
        assertEquals(false, passed)
    }

    @Test
    fun `every read endpoint needs the read token`() {
        for (path in listOf("/api/tracking/sessions", "/api/tracking/sessions/1",
            "/api/tracking/sessions/range", "/api/tracking/comparison",
            "/actuator/metrics", "/actuator/info", "/v3/api-docs", "/swagger-ui/index.html")) {
            assertEquals(401, run(filter, "GET", path).first.status, path)
            assertEquals(true, run(filter, "GET", path,
                mapOf("Authorization" to "Bearer read-secret")).second, path)
        }
    }

    @Test
    fun `the liveness checks stay open`() {
        assertEquals(true, run(filter, "GET", "/api/tracking/health").second)
        assertEquals(true, run(filter, "GET", "/actuator/health").second)
    }

    @Test
    fun `CORS is granted to the PWA origin only`() {
        val (ok, _) = run(filter, "OPTIONS", "/api/tracking/sessions", mapOf("Origin" to pwa))
        assertEquals(200, ok.status)
        assertEquals(pwa, ok.getHeader("Access-Control-Allow-Origin"))

        val (other, _) = run(filter, "OPTIONS", "/api/tracking/sessions",
            mapOf("Origin" to "https://example.org"))
        assertNull(other.getHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `unset tokens fail closed`() {
        val open = StudyAccessFilter("", "", pwa)
        assertEquals(401, run(open, "POST", "/api/tracking/sessions",
            mapOf("X-Study-Token" to "")).first.status)
        assertEquals(401, run(open, "GET", "/api/tracking/sessions",
            mapOf("Authorization" to "Bearer ")).first.status)
        assertNotNull(run(open, "GET", "/api/tracking/health").second.takeIf { it })
    }
}
