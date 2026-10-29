package com.example.fullstacklab.api.calendar

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** API → 外部カレンダーサービスの通信（URL・API キー・レスポンス変換・障害時の扱い）を確認する */
class CalendarClientTest {
    private lateinit var server: MockRestServiceServer
    private lateinit var client: CalendarClient

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder()
            .baseUrl("http://calendar.test")
            .defaultHeader("X-Api-Key", "test-key")
        server = MockRestServiceServer.bindTo(builder).build()
        client = CalendarClient(builder.build())
    }

    @Test
    fun `sends api key and maps holiday response`() {
        server.expect(requestTo("http://calendar.test/v1/days/2026-11-03"))
            .andExpect(header("X-Api-Key", "test-key"))
            .andRespond(
                withSuccess(
                    """{"date":"2026-11-03","businessDay":false,"holidayName":"文化の日"}""",
                    MediaType.APPLICATION_JSON,
                ),
            )

        val info = client.getDay(LocalDate.of(2026, 11, 3))

        assertEquals(DueDateInfo(LocalDate.of(2026, 11, 3), false, "文化の日"), info)
        server.verify()
    }

    @Test
    fun `wraps upstream error as CalendarUnavailableException`() {
        server.expect(requestTo("http://calendar.test/v1/days/2099-12-31"))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        assertFailsWith<CalendarUnavailableException> {
            client.getDay(LocalDate.of(2099, 12, 31))
        }
    }
}
