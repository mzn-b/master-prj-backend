package at.fhcampuswien.masterprj.entity

import at.fhcampuswien.masterprj.dto.Platform
import at.fhcampuswien.masterprj.dto.TrackingMode
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "tracking_sessions")
class TrackingSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val sessionId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val platform: Platform,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val mode: TrackingMode,

    @Column(nullable = false)
    val recordedAt: Instant = Instant.now(),

    // Device Info
    @Column(nullable = false)
    val deviceModel: String,

    @Column(nullable = false)
    val osVersion: String,

    @Column(nullable = false)
    val appVersion: String,

    val screenWidth: Int? = null,
    val screenHeight: Int? = null,
    val browserInfo: String? = null,

    // Performance Metrics
    @Column(nullable = false)
    val fps: Double,

    @Column(nullable = false)
    val avgFps: Double,

    val minFps: Double? = null,

    val maxFps: Double? = null,

    @Column(nullable = false)
    val inferenceTimeMs: Double,

    @Column(nullable = false)
    val avgInferenceTimeMs: Double,

    @Column(nullable = false)
    val frameProcessingTimeMs: Double,

    @Column(nullable = false)
    val avgFrameProcessingTimeMs: Double,

    // Memory metrics
    val memoryUsageMB: Double? = null,
    val totalMemoryMB: Double? = null,
    val availableMemoryMB: Double? = null,
    val heapLimitMB: Double? = null,
    val peakMemoryUsageMB: Double? = null,

    // CPU metrics
    val cpuUsagePercent: Double? = null,
    val cpuCores: Int? = null,
    val threadCount: Int? = null,

    // GPU metrics
    val gpuUsagePercent: Double? = null,
    val gpuVendor: String? = null,
    val gpuRenderer: String? = null,
    val gpuDelegateActive: Boolean? = null,

    // Thermal metrics
    val thermalState: String? = null,

    // Power metrics
    val batteryLevel: Double? = null,
    val batteryCharging: Boolean? = null,

    // Network metrics
    val networkType: String? = null,
    val networkDownlinkMbps: Double? = null,
    val networkRttMs: Double? = null,

    // Detection metrics
    val facesDetectedAvg: Double? = null,
    val handsDetectedAvg: Double? = null,
    val detectionRate: Double? = null,

    // Model metrics (Native)
    val modelLoadTimeMs: Double? = null,

    @Column(nullable = false)
    val frameCount: Int,

    @Column(nullable = false)
    val droppedFrames: Int,

    @Column(nullable = false)
    val sessionDurationMs: Long,

    @Column(nullable = false)
    val warmupComplete: Boolean,

    val trackingConfidence: Double? = null,

    @Column(nullable = false)
    val trackingLostCount: Int,

    // Stability metrics
    val trackingRecoveryTimeMs: Double? = null,
    val consecutiveTrackingLossMax: Int? = null,
    val errorCount: Int? = null
)
