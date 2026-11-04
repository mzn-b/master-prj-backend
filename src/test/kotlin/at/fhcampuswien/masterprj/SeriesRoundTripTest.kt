package at.fhcampuswien.masterprj

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The per-frame series (apps since 1.2.0) go in as JSON arrays and must come
 * back unchanged and in order from the per-id endpoint the analysis reads.
 * Runs the real HTTP stack, JSON mapping and array column, not a mock.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["study.write-token=w", "study.read-token=r"],
)
@ActiveProfiles("test")
class SeriesRoundTripTest {

    @LocalServerPort
    private var port: Int = 0

    private val http = HttpClient.newHttpClient()

    private fun send(req: HttpRequest.Builder): HttpResponse<String> =
        http.send(req.build(), HttpResponse.BodyHandlers.ofString())

    private fun session(metricsExtra: String) = """
        {"platform":"IOS_NATIVE","sessionId":"t","mode":"COMBINED",
         "deviceInfo":{"deviceModel":"m","osVersion":"o","appVersion":"1.2.0+test"},
         "metrics":{"fps":30,"avgFps":30,"inferenceTimeMs":30,"avgInferenceTimeMs":30,
           "frameProcessingTimeMs":30,"avgFrameProcessingTimeMs":30,"frameCount":33,
           "droppedFrames":0,"sessionDurationMs":1000,"warmupComplete":true,
           "trackingLostCount":0$metricsExtra}}
    """.trimIndent()

    private fun post(body: String): HttpResponse<String> = send(
        HttpRequest.newBuilder(URI("http://localhost:$port/api/tracking/sessions"))
            .header("Content-Type", "application/json")
            .header("X-Study-Token", "w")
            .POST(HttpRequest.BodyPublishers.ofString(body)),
    )

    private fun read(id: String): String = send(
        HttpRequest.newBuilder(URI("http://localhost:$port/api/tracking/sessions/$id"))
            .header("Authorization", "Bearer r"),
    ).body()

    private fun idOf(response: HttpResponse<String>): String =
        Regex("\"id\"\\s*:\\s*(\\d+)").find(response.body())!!.groupValues[1]

    @Test
    fun `the series are stored and returned in order`() {
        val created = post(session(
            ""","inferenceTimeSeriesMs":[31.25,30.5,29.75],"frameIntervalSeriesMs":[33,34,33]"""))
        assertEquals(201, created.statusCode(), created.body())
        val body = read(idOf(created)).replace(" ", "")
        assertTrue(body.contains("\"inferenceTimeSeriesMs\":[31.25,30.5,29.75]"), body)
        assertTrue(body.contains("\"frameIntervalSeriesMs\":[33.0,34.0,33.0]"), body)
    }

    @Test
    fun `a session from an earlier build without series is still accepted`() {
        val created = post(session(""))
        assertEquals(201, created.statusCode(), created.body())
        assertTrue(read(idOf(created)).replace(" ", "").contains("\"inferenceTimeSeriesMs\":null"))
    }
}
