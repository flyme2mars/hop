package com.flyme2mars.hop.data

object HopSyncCodec {
    private const val MAGIC = "HOP1"
    private const val MAX_BYTES = 400
    private const val MAX_POSTS = 6
    private const val TITLE_MAX = 48
    private const val BODY_MAX = 96

    fun encode(posts: List<HopPost>, limitBytes: Int = MAX_BYTES): ByteArray {
        val lines = mutableListOf(MAGIC)
        val newest = posts.sortedByDescending { it.createdAtMillis }.take(MAX_POSTS)
        for (post in newest) {
            val line = listOf(
                post.id,
                post.kind.name,
                clip(post.title, TITLE_MAX),
                clip(post.body, BODY_MAX),
                clip(post.authorName, 32),
                clip(post.authorRoom, 16),
                post.authorId,
                post.createdAtMillis.toString(),
                if (post.claimed) "1" else "0",
            ).joinToString("|") { escape(it) }
            val candidate = (lines + line).joinToString("\n")
            if (candidate.toByteArray(Charsets.UTF_8).size > limitBytes) break
            lines += line
        }
        return lines.joinToString("\n").toByteArray(Charsets.UTF_8)
    }

    fun decode(bytes: ByteArray): List<HopPost> {
        if (bytes.isEmpty()) return emptyList()
        val text = bytes.toString(Charsets.UTF_8)
        val lines = text.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty() || lines.first() != MAGIC) return emptyList()
        return lines.drop(1).mapNotNull { line ->
            val parts = splitEscaped(line)
            if (parts.size < 9) return@mapNotNull null
            val kind = runCatching { PostKind.valueOf(parts[1]) }.getOrNull() ?: return@mapNotNull null
            val created = parts[7].toLongOrNull() ?: return@mapNotNull null
            HopPost(
                id = parts[0],
                kind = kind,
                title = parts[2],
                body = parts[3],
                authorName = parts[4],
                authorRoom = parts[5],
                authorId = parts[6],
                createdAtMillis = created,
                claimed = parts[8] == "1",
            )
        }
    }

    private fun clip(value: String, max: Int): String = value.trim().take(max)

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n")

    private fun splitEscaped(line: String): List<String> {
        val out = mutableListOf<String>()
        val current = StringBuilder()
        var escaped = false
        line.forEach { ch ->
            when {
                escaped -> {
                    current.append(if (ch == 'n') '\n' else ch)
                    escaped = false
                }
                ch == '\\' -> escaped = true
                ch == '|' -> {
                    out += current.toString()
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        out += current.toString()
        return out
    }
}
