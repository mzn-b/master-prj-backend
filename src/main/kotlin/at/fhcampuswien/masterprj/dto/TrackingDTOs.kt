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

data class BlendshapeCategory(
    val categoryName: String,
    val score: Double
)

data class FaceData(
    val landmarks: List<NormalizedPoint>,
    // F12 — MediaPipe FaceLandmarker blendshapes (~52 morph-target weights per face)
    val blendshapes: List<BlendshapeCategory>? = null
)

data class HandLandmarksDTO(
    val hands: List<HandData>
)

data class HandData(
    val handedness: String? = null,
    val landmarks: List<NormalizedPoint>,
    // F13 — top gesture label from MediaPipe GestureRecognizer
    val gesture: String? = null,
    val gestureScore: Double? = null
)

data class TrackingDataDTO(
    val timestampMs: Long,
    val mode: TrackingMode,
    val face: FaceLandmarksDTO? = null,
    val hand: HandLandmarksDTO? = null
)

data class PerformanceMetricsDTO(
    // Frame timing metrics
    val fps: Double,
    val avgFps: Double,
    val minFps: Double? = null,
    val maxFps: Double? = null,
    val inferenceTimeMs: Double,
    val avgInferenceTimeMs: Double,
    val frameProcessingTimeMs: Double,
    val avgFrameProcessingTimeMs: Double,

    // Memory metrics
    val memoryUsageMB: Double? = null,
    val totalMemoryMB: Double? = null,
    val availableMemoryMB: Double? = null,
    val heapLimitMB: Double? = null,  // JS heap limit for PWA
    val peakMemoryUsageMB: Double? = null,  // Peak memory during session

    // CPU metrics
    val cpuUsagePercent: Double? = null,
    val cpuCores: Int? = null,
    val threadCount: Int? = null,

    // GPU metrics
    val gpuUsagePercent: Double? = null,
    val gpuVendor: String? = null,
    val gpuRenderer: String? = null,
    val gpuDelegateActive: Boolean? = null,  // Whether GPU acceleration is active (Native)

    // Thermal metrics
    val thermalState: String? = null,  // nominal, fair, serious, critical (iOS) or similar

    // Power metrics
    val batteryLevel: Double? = null,
    val batteryCharging: Boolean? = null,
    // Battery consumption over the session (start - end, positive = used)
    val batteryLevelStart: Double? = null,
    val batteryLevelEnd: Double? = null,
    val batteryDeltaPercent: Double? = null,

    // Network metrics
    val networkType: String? = null,  // wifi, cellular, ethernet, etc.
    val networkDownlinkMbps: Double? = null,
    val networkRttMs: Double? = null,

    // Detection metrics
    val facesDetectedAvg: Double? = null,  // Average faces detected per frame
    val handsDetectedAvg: Double? = null,  // Average hands detected per frame
    val detectionRate: Double? = null,  // Detections per second (for async processing)

    // Model metrics (Native)
    val modelLoadTimeMs: Double? = null,  // Time to load ML models

    // Session metrics
    val frameCount: Int,
    val droppedFrames: Int,
    val sessionDurationMs: Long,
    val warmupComplete: Boolean,
    val trackingConfidence: Double? = null,
    val trackingLostCount: Int,

    // Stability metrics
    val trackingRecoveryTimeMs: Double? = null,  // Average time to recover after tracking loss
    val consecutiveTrackingLossMax: Int? = null,  // Longest streak of lost tracking
    val errorCount: Int? = null,  // Number of errors during session

    // Memory consumption over the session (end - start, positive = grew)
    val memoryUsageStartMB: Double? = null,
    val memoryUsageEndMB: Double? = null,
    val memoryDeltaMB: Double? = null,

    // ---- Run conditions ----
    // NF4: what a row means depends on how the session was configured. Without
    // these, two rows that differ only in capture resolution or threading model
    // are indistinguishable, and the platform comparison silently mixes them.

    // Capture resolution actually delivered by the camera, not the one requested.
    val frameWidth: Int? = null,
    val frameHeight: Int? = null,

    // Which graphics backend drew the overlay.
    // PWA: "webgpu" | "webgl2" | "webgl". Native: "skia".
    val renderBackend: String? = null,

    // Where inference ran relative to the UI.
    // PWA: "main" | "worker". Native: "async-runner".
    val inferenceThreading: String? = null,

    // NF3 — milliseconds from tracking start to the first frame with a detection.
    val timeToFirstDetectionMs: Double? = null
)

data class TrackingSessionRequest(
    val platform: Platform,
    val deviceInfo: DeviceInfoDTO,
    val sessionId: String,
    val mode: TrackingMode,
    val metrics: PerformanceMetricsDTO,
    val recordedAt: Instant = Instant.now(),
    val activeFilters: List<String>? = null
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
    val recordedAt: Instant,
    val activeFilters: List<String>? = null
)
