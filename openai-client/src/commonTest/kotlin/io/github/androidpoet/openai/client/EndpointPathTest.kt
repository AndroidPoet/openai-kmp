package io.github.androidpoet.openai.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EndpointPathTest {
    @Test
    fun test_endpointPath_encodesReservedCharacters() {
        val endpoint = endpointPath("files", "id/with space")

        assertEquals("files/id%2Fwith%20space", endpoint)
    }

    @Test
    fun test_encodePathSegment_blank_throws() {
        assertFailsWith<IllegalArgumentException> {
            encodePathSegment("  ")
        }
    }
}
