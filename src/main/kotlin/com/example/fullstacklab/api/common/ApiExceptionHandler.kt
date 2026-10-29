package com.example.fullstacklab.api.common

import com.example.fullstacklab.api.calendar.CalendarUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

class NotFoundException(message: String) : RuntimeException(message)

@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: "not found")

    // 外部サービスの障害は 502 として呼び出し元（BFF）へ伝え、縮退するかどうかは BFF に判断させる
    @ExceptionHandler(CalendarUnavailableException::class)
    fun handleCalendarUnavailable(e: CalendarUnavailableException): ProblemDetail {
        log.warn("calendar service unavailable: {}", e.message)
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "calendar service unavailable")
    }
}
