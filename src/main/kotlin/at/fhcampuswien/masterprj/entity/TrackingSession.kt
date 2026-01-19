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

    @Column(nullable = false)
    val minFps: Double,

    @Column(nullable = false)
    val maxFps: Double,

    @Column(nullable = false)
    val inferenceTimeMs: Double,

    @Column(nullable = false)
    val avgInferenceTimeMs: Double,

    @Column(nullable = false)
    val frameProcessingTimeMs: Double,

    @Column(nullable = false)
    val avgFrameProcessingTimeMs: Double,

    val memoryUsageMB: Double? = null,
    val cpuUsagePercent: Double? = null,
    val gpuUsagePercent: Double? = null,
    val batteryLevel: Double? = null,
    val batteryCharging: Boolean? = null,

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
    val trackingLostCount: Int
)
