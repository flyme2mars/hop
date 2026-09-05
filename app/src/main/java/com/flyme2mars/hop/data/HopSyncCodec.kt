package com.flyme2mars.hop.data

object HopSyncCodec {
    private const val MAGIC_V2 = "HOP2"
    private const val MAGIC_V1 = "HOP1"
    const val MAX_BYTES = 32_768
    private const val TITLE_MAX = 160
    private const val BODY_MAX = 2_000

    fun encode(posts: List<HopPost>, limitBytes: Int = MAX_BYTES): ByteArray {
        val lines = mutableListOf(MAGIC_V2)
        val newest = posts.sortedByDescending { it.updatedAtMillis }
        for (post in newest) {
            val line = listOf(
                post.id,
                post.kind.name,
                clip(post.title, TITLE_MAX),
                clip(post.body, BODY_MAX),
                clip(post.authorName, 48),
                clip(post.authorRoom, 24),
                post.authorId,
                post.createdAtMillis.toString(),
                post.updatedAtMillis.toString(),
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
        if (lines.isEmpty()) return emptyList()
        val magic = lines.first()
        if (magic != MAGIC_V2 && magic != MAGIC_V1) return emptyList()
        val version2 = magic == MAGIC_V2
        return lines.drop(1).mapNotNull { line -> parseLine(line, version2) }
    }

    private fun parseLine(line: String, version2: Boolean): HopPost? {
        val parts = splitEscaped(line)
        if (parts.size < 9) return null
        val kind = runCatching { PostKind.valueOf(parts[1]) }.getOrNull() ?: return null
        val created = parts[7].toLongOrNull() ?: return null
        val updated: Long
        val claimedFlag: String
        if (version2 && parts.size >= 10) {
            updated = parts[8].toLongOrNull() ?: created
            claimedFlag = parts[9]
        } else {
            updated = created
            claimedFlag = parts[8]
        }
        return HopPost(
            id = parts[0],
            kind = kind,
            title = parts[2],
            body = parts[3],
            authorName = parts[4],
            authorRoom = parts[5],
            authorId = parts[6],
            createdAtMillis = created,
            updatedAtMillis = updated,
            claimed = claimedFlag == "1",
        )
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
