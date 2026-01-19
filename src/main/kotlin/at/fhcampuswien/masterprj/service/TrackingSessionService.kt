package at.fhcampuswien.masterprj.service

import at.fhcampuswien.masterprj.dto.*
import at.fhcampuswien.masterprj.entity.TrackingSession
import at.fhcampuswien.masterprj.repository.TrackingSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TrackingSessionService(
    private val repository: TrackingSessionRepository
) {

    @Transactional
    fun saveSession(request: TrackingSessionRequest): TrackingSessionResponse {
        val session = TrackingSession(
            sessionId = request.sessionId,
            platform = request.platform,
            mode = request.mode,
            recordedAt = request.recordedAt,
            // Device info
            deviceModel = request.deviceInfo.deviceModel,
            osVersion = request.deviceInfo.osVersion,
            appVersion = request.deviceInfo.appVersion,
            screenWidth = request.deviceInfo.screenWidth,
            screenHeight = request.deviceInfo.screenHeight,
            browserInfo = request.deviceInfo.browserInfo,
            // Metrics
            fps = request.metrics.fps,
            avgFps = request.metrics.avgFps,
            minFps = request.metrics.minFps,
            maxFps = request.metrics.maxFps,
            inferenceTimeMs = request.metrics.inferenceTimeMs,
            avgInferenceTimeMs = request.metrics.avgInferenceTimeMs,
            frameProcessingTimeMs = request.metrics.frameProcessingTimeMs,
            avgFrameProcessingTimeMs = request.metrics.avgFrameProcessingTimeMs,
            memoryUsageMB = request.metrics.memoryUsageMB,
            cpuUsagePercent = request.metrics.cpuUsagePercent,
            gpuUsagePercent = request.metrics.gpuUsagePercent,
            batteryLevel = request.metrics.batteryLevel,
            batteryCharging = request.metrics.batteryCharging,
            frameCount = request.metrics.frameCount,
            droppedFrames = request.metrics.droppedFrames,
            sessionDurationMs = request.metrics.sessionDurationMs,
            warmupComplete = request.metrics.warmupComplete,
            trackingConfidence = request.metrics.trackingConfidence,
            trackingLostCount = request.metrics.trackingLostCount
        )

        val saved = repository.save(session)

        return TrackingSessionResponse(
            id = saved.id,
            sessionId = saved.sessionId,
            platform = saved.platform,
            recordedAt = saved.recordedAt
        )
    }

    fun getAllSessions(): List<SessionSummaryDTO> {
        return repository.findAll().map { it.toSummary() }
    }

    fun getSessionsByPlatform(platform: Platform): List<SessionSummaryDTO> {
        return repository.findByPlatform(platform).map { it.toSummary() }
    }

    fun getSessionsByMode(mode: TrackingMode): List<SessionSummaryDTO> {
        return repository.findByMode(mode).map { it.toSummary() }
    }

    fun getSessionsByPlatformAndMode(platform: Platform, mode: TrackingMode): List<SessionSummaryDTO> {
        return repository.findByPlatformAndMode(platform, mode).map { it.toSummary() }
    }

    fun getSessionById(id: Long): TrackingSession? {
        return repository.findById(id).orElse(null)
    }

    fun getSessionsBetween(start: Instant, end: Instant): List<SessionSummaryDTO> {
        return repository.findByRecordedAtBetween(start, end).map { it.toSummary() }
    }

    fun getComparisonStats(): PlatformComparisonDTO {
        val platforms = Platform.entries
        val modes = TrackingMode.entries

        val stats = platforms.flatMap { platform ->
            modes.map { mode ->
                PlatformModeStats(
                    platform = platform,
                    mode = mode,
                    avgFps = repository.getAverageFpsByPlatformAndMode(platform, mode) ?: 0.0,
                    avgInferenceTimeMs = repository.getAverageInferenceTimeByPlatformAndMode(platform, mode) ?: 0.0,
                    sessionCount = repository.findByPlatformAndMode(platform, mode).size
                )
            }
        }

        return PlatformComparisonDTO(stats)
    }

    private fun TrackingSession.toSummary() = SessionSummaryDTO(
        id = id,
        sessionId = sessionId,
        platform = platform,
        deviceModel = deviceModel,
        mode = mode,
        avgFps = avgFps,
        avgInferenceTimeMs = avgInferenceTimeMs,
        sessionDurationMs = sessionDurationMs,
        recordedAt = recordedAt
    )
}

data class PlatformModeStats(
    val platform: Platform,
    val mode: TrackingMode,
    val avgFps: Double,
    val avgInferenceTimeMs: Double,
    val sessionCount: Int
)

data class PlatformComparisonDTO(
    val stats: List<PlatformModeStats>
)
