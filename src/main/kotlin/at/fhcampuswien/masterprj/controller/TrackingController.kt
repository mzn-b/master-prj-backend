package at.fhcampuswien.masterprj.controller

import at.fhcampuswien.masterprj.dto.*
import at.fhcampuswien.masterprj.entity.TrackingSession
import at.fhcampuswien.masterprj.service.PlatformComparisonDTO
import at.fhcampuswien.masterprj.service.TrackingSessionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/tracking")
class TrackingController(
    private val service: TrackingSessionService
) {

    @PostMapping("/sessions")
    fun submitSession(
        @Valid @RequestBody request: TrackingSessionRequest
    ): ResponseEntity<TrackingSessionResponse> {
        val response = service.saveSession(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/sessions")
    fun getAllSessions(
        @RequestParam(required = false) platform: Platform?,
        @RequestParam(required = false) mode: TrackingMode?
    ): ResponseEntity<List<SessionSummaryDTO>> {
        val sessions = when {
            platform != null && mode != null -> service.getSessionsByPlatformAndMode(platform, mode)
            platform != null -> service.getSessionsByPlatform(platform)
            mode != null -> service.getSessionsByMode(mode)
            else -> service.getAllSessions()
        }
        return ResponseEntity.ok(sessions)
    }

    @GetMapping("/sessions/{id}")
    fun getSessionById(@PathVariable id: Long): ResponseEntity<TrackingSession> {
        val session = service.getSessionById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(session)
    }

    @GetMapping("/sessions/range")
    fun getSessionsByDateRange(
        @RequestParam start: Instant,
        @RequestParam end: Instant
    ): ResponseEntity<List<SessionSummaryDTO>> {
        return ResponseEntity.ok(service.getSessionsBetween(start, end))
    }

    @GetMapping("/comparison")
    fun getComparisonStats(): ResponseEntity<PlatformComparisonDTO> {
        return ResponseEntity.ok(service.getComparisonStats())
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "UP"))
    }
}
