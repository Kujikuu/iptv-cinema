package com.afifistudio.iptvcinema.data.m3u

data class ParsedM3uEntry(
    val name: String,
    val streamUrl: String,
    val tvgId: String?,
    val logoUrl: String?,
    val groupTitle: String?,
    val channelNumber: Int? = null,
)

data class ParsedM3uPlaylist(
    val entries: List<ParsedM3uEntry>,
    val categories: Map<String, String>,
)

class M3uParser {
    private val extInfRegex = Regex(
        """#EXTINF:-?\d+\s*(.*?),\s*(.*)$""",
        RegexOption.IGNORE_CASE,
    )
    private val attrRegex = Regex("""(\w[\w-]*)="([^"]*)"""")
    private val attrSingleRegex = Regex("""(\w[\w-]*)='([^']*)'""")

    fun parse(content: String): ParsedM3uPlaylist {
        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val entries = mutableListOf<ParsedM3uEntry>()
        val seenUrls = mutableSetOf<String>()
        var index = 0

        while (index < lines.size) {
            val line = lines[index]
            if (!line.startsWith("#EXTINF", ignoreCase = true)) {
                index++
                continue
            }

            val match = extInfRegex.find(line)
            if (match == null) {
                index++
                continue
            }
            val attributesPart = match.groupValues[1]
            val name = match.groupValues[2].trim()
            val attrs = parseAttributes(attributesPart)

            var urlIndex = index + 1
            while (urlIndex < lines.size && lines[urlIndex].startsWith("#")) {
                urlIndex++
            }
            if (urlIndex >= lines.size) break

            val streamUrl = lines[urlIndex].trim()
            if (streamUrl.isNotBlank() && seenUrls.add(streamUrl)) {
                entries.add(
                    ParsedM3uEntry(
                        name = name.ifBlank { "Channel ${entries.size + 1}" },
                        streamUrl = streamUrl,
                        tvgId = attrs["tvg-id"],
                        logoUrl = attrs["tvg-logo"],
                        groupTitle = attrs["group-title"]?.takeIf { it.isNotBlank() },
                        channelNumber = attrs["tvg-chno"]?.toIntOrNull(),
                    ),
                )
            }
            index = urlIndex + 1
        }

        val categories = linkedMapOf<String, String>()
        entries.forEach { entry ->
            val group = entry.groupTitle ?: DEFAULT_CATEGORY
            if (!categories.containsKey(group)) {
                categories[group] = group
            }
        }

        return ParsedM3uPlaylist(entries = entries, categories = categories)
    }

    private fun parseAttributes(part: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        attrRegex.findAll(part).forEach { match ->
            result[match.groupValues[1].lowercase()] = match.groupValues[2]
        }
        attrSingleRegex.findAll(part).forEach { match ->
            result.putIfAbsent(match.groupValues[1].lowercase(), match.groupValues[2])
        }
        return result
    }

    companion object {
        const val DEFAULT_CATEGORY = "Uncategorized"
    }
}
