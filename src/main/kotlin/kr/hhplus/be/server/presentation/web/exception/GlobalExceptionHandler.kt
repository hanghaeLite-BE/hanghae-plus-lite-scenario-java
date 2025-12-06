package kr.hhplus.be.server.presentation.web.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.exception.ForbiddenException
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice(
    annotations = [RestController::class, Controller::class],
    basePackages = ["kr.hhplus.be.server.presentation.web"],
)
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(value = [IllegalArgumentException::class])
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity(
            ErrorResponse(e.message ?: "잘못된 요청입니다."),
            HttpStatus.BAD_REQUEST,
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> =
        ResponseEntity(
            ErrorResponse(e.message ?: "요청을 처리하는데 실패하였습니다."),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )

    @ExceptionHandler(ConflictResourceException::class)
    fun handleConflictResourceException(e: ConflictResourceException): ResponseEntity<ErrorResponse> {
        logger.warn(e) { "clue=${e.clue}" }

        return ResponseEntity(
            ErrorResponse(e.message),
            HttpStatus.CONFLICT,
        )
    }

    @ExceptionHandler(NotFoundResourceException::class)
    fun handleNotFoundResourceException(e: NotFoundResourceException): ResponseEntity<ErrorResponse> {
        logger.warn(e) { "clue=${e.clue}" }

        return ResponseEntity(
            ErrorResponse(e.message),
            HttpStatus.NOT_FOUND,
        )
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<ErrorResponse> {

        return ResponseEntity(
            ErrorResponse(e.message),
            HttpStatus.FORBIDDEN,
        )
    }
}

data class ErrorResponse(
    val message: String,
)
