package kr.hhplus.be.server.common.support

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.UUID

fun postJsonWithIdempotency(
    uri: String,
    body: String,
    memberId: Long? = null,
    idempotencyKey: String = UUID.randomUUID().toString(),
    headers: Map<String, String> = emptyMap(),
): MockHttpServletRequestBuilder {
    val builder =
        MockMvcRequestBuilders
            .post(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)

    memberId?.let { builder.header("X-Member-Id", it) }
    builder.header("Idempotency-Key", idempotencyKey)
    headers.forEach { (name, value) -> builder.header(name, value) }

    return builder
}
