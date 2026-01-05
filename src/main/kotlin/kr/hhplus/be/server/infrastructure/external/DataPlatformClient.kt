package kr.hhplus.be.server.infrastructure.external

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kr.hhplus.be.server.application.port.out.ExternalServiceOutput
import kr.hhplus.be.server.application.vo.PlaceOrderResultVO
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Component
class DataPlatformClient(
    private val dataPlatformRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
) : ExternalServiceOutput {
    private val logger = KotlinLogging.logger { }

    /**
     * POST https://data-platform.example.com/api/events
     * request body는 dto를 JSON으로 직렬화하여 전송
     * 200 OK 응답이 오면 성공으로 간주, 그 외인 경우 로그를 남김
     */
    override fun call(dto: PlaceOrderResultVO) {
        val url = "https://data-platform.example.com/api/events"

        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val requestBody = objectMapper.writeValueAsString(dto)
            val requestEntity = HttpEntity(requestBody, headers)

            val response: ResponseEntity<String> =
                dataPlatformRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String::class.java,
                )

            if (response.statusCode.is2xxSuccessful) {
                logger.info { "Successfully sent data to Data Platform: $dto" }
            } else {
                logger.warn { "Failed to send data to Data Platform. Response: ${response.statusCode}, Body: ${response.body}" }
            }
        } catch (ex: HttpStatusCodeException) {
            logger.error(ex) { "HTTP error while sending data to Data Platform: ${ex.statusCode}, Body: ${ex.responseBodyAsString}" }
        } catch (ex: Exception) {
            logger.error(ex) { "Unexpected error while sending data to Data Platform" }
        }
    }
}
