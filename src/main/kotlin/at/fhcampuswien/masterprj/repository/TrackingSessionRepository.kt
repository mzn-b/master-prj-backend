package at.fhcampuswien.masterprj.repository

import at.fhcampuswien.masterprj.dto.Platform
import at.fhcampuswien.masterprj.dto.TrackingMode
import at.fhcampuswien.masterprj.entity.TrackingSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface TrackingSessionRepository : JpaRepository<TrackingSession, Long> {

    fun findByPlatform(platform: Platform): List<TrackingSession>

    fun findByMode(mode: TrackingMode): List<TrackingSession>

    fun findByPlatformAndMode(platform: Platform, mode: TrackingMode): List<TrackingSession>

    fun findBySessionId(sessionId: String): TrackingSession?

    fun findByRecordedAtBetween(start: Instant, end: Instant): List<TrackingSession>

    @Query("""
        SELECT ts FROM TrackingSession ts
        WHERE ts.deviceModel = :deviceModel
        ORDER BY ts.recordedAt DESC
    """)
    fun findByDeviceModel(deviceModel: String): List<TrackingSession>

    @Query("""
        SELECT AVG(ts.avgFps) FROM TrackingSession ts
        WHERE ts.platform = :platform AND ts.mode = :mode
    """)
    fun getAverageFpsByPlatformAndMode(platform: Platform, mode: TrackingMode): Double?

    @Query("""
        SELECT AVG(ts.avgInferenceTimeMs) FROM TrackingSession ts
        WHERE ts.platform = :platform AND ts.mode = :mode
    """)
    fun getAverageInferenceTimeByPlatformAndMode(platform: Platform, mode: TrackingMode): Double?
}
