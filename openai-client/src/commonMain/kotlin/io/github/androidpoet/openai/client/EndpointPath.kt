package io.github.androidpoet.openai.client

public fun endpointPath(vararg segments: String): String =
    segments.joinToString("/") { encodePathSegment(it) }

public fun encodePathSegment(value: String): String {
    require(value.isNotBlank()) { "Path segment must not be blank" }

    val bytes = value.encodeToByteArray()
    val out = StringBuilder(bytes.size)
    bytes.forEach { b ->
        val c = b.toInt().toChar()
        if (c.isAsciiUnreserved()) {
            out.append(c)
        } else {
            val v = b.toInt() and 0xff
            out.append('%')
            out.append("0123456789ABCDEF"[v ushr 4])
            out.append("0123456789ABCDEF"[v and 0x0f])
        }
    }
    return out.toString()
}

private fun Char.isAsciiUnreserved(): Boolean =
    (this in 'a'..'z') ||
        (this in 'A'..'Z') ||
        (this in '0'..'9') ||
        this == '-' ||
        this == '_' ||
        this == '.' ||
        this == '~'
