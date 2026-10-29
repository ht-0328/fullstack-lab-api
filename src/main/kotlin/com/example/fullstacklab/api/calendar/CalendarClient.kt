package com.example.fullstacklab.api.calendar

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body
import java.time.Duration
import java.time.LocalDate

/** 外部カレンダーサービスの接続設定。ローカルでは WireMock、本番では実サービスを指す */
@ConfigurationProperties("external.calendar")
data class CalendarProperties(
    val baseUrl: String,
    val apiKey: String,
    val connectTimeout: Duration = Duration.ofSeconds(2),
    val readTimeout: Duration = Duration.ofSeconds(3),
)

/** 期限日の補足情報（営業日かどうか、祝日名） */
data class DueDateInfo(
    val date: LocalDate,
    val businessDay: Boolean,
    val holidayName: String?,
)

class CalendarUnavailableException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Configuration
class CalendarClientConfig {
    @Bean
    fun calendarRestClient(builder: RestClient.Builder, props: CalendarProperties): RestClient {
        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(props.connectTimeout)
            setReadTimeout(props.readTimeout)
        }
        return builder
            .baseUrl(props.baseUrl)
            .defaultHeader("X-Api-Key", props.apiKey)
            .requestFactory(requestFactory)
            .build()
    }
}

@Component
class CalendarClient(private val calendarRestClient: RestClient) {

    fun getDay(date: LocalDate): DueDateInfo =
        try {
            calendarRestClient.get()
                .uri("/v1/days/{date}", date)
                .retrieve()
                .body<DueDateInfo>()
                ?: throw IllegalStateException("empty response")
        } catch (e: RestClientException) {
            // 4xx/5xx・タイムアウト・接続失敗をまとめて「外部サービス利用不可」として扱う
            throw CalendarUnavailableException("failed to get calendar day $date: ${e.message}", e)
        }
}
