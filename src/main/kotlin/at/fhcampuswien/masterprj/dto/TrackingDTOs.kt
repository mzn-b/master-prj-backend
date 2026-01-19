package at.fhcampuswien.masterprj.dto

import java.time.Instant

enum class Platform {
    IOS_PWA,
    ANDROID_PWA,
    IOS_NATIVE,
    ANDROID_NATIVE
}

enum class TrackingMode {
    FACE,
    HAND,
    COMBINED
}

data class NormalizedPoint(
    val x: Double,
    val y: Double,
    val z: Double? = null
)

data class FaceLandmarksDTO(
    val faces: List<FaceData>
)

data class FaceData(
    val landmarks: List<NormalizedPoint>
)

data class HandLandmarksDTO(
    val hands: List<HandData>
)

data class HandData(
    val handedness: String? = null,
    val landmarks: List<NormalizedPoint>
)

data class TrackingDataDTO(
    val timestampMs: Long,
    val mode: TrackingMode,
    val face: FaceLandmarksDTO? = null,
    val hand: HandLandmarksDTO? = null
)

data class PerformanceMetricsDTO(
    val fps: Double,
    val avgFps: Double,
    val minFps: Double,
    val maxFps: Double,
    val inferenceTimeMs: Double,
    val avgInferenceTimeMs: Double,
    val frameProcessingTimeMs: Double,
    val avgFrameProcessingTimeMs: Double,
    val memoryUsageMB: Double? = null,
    val cpuUsagePercent: Double? = null,
    val gpuUsagePercent: Double? = null,
    val batteryLevel: Double? = null,
    val batteryCharging: Boolean? = null,
    val frameCount: Int,
    val droppedFrames: Int,
    val sessionDurationMs: Long,
    val warmupComplete: Boolean,
    val trackingConfidence: Double? = null,
    val trackingLostCount: Int
)

data class TrackingSessionRequest(
    val platform: Platform,
    val deviceInfo: DeviceInfoDTO,
    val sessionId: String,
    val mode: TrackingMode,
    val metrics: PerformanceMetricsDTO,
    val recordedAt: Instant = Instant.now()
)

data class DeviceInfoDTO(
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val screenWidth: Int? = null,
    val screenHeight: Int? = null,
    val browserInfo: String? = null  // For PWA only
)

data class TrackingSessionResponse(
    val id: Long,
    val sessionId: String,
    val platform: Platform,
    val recordedAt: Instant,
    val message: String = "Session recorded successfully"
)

data class SessionSummaryDTO(
    val id: Long,
    val sessionId: String,
    val platform: Platform,
    val deviceModel: String,
    val mode: TrackingMode,
    val avgFps: Double,
    val avgInferenceTimeMs: Double,
    val sessionDurationMs: Long,
    val recordedAt: Instant
)
