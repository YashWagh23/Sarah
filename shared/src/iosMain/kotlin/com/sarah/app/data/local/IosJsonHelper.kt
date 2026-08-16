package com.sarah.app.data.local

import com.sarah.app.domain.model.*

object IosJsonHelper {
    fun escapeJson(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun unescapeJson(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (s[i + 1]) {
                    '\\' -> { sb.append('\\'); i += 2; continue }
                    '"' -> { sb.append('"'); i += 2; continue }
                    'n' -> { sb.append('\n'); i += 2; continue }
                    'r' -> { sb.append('\r'); i += 2; continue }
                    't' -> { sb.append('\t'); i += 2; continue }
                    else -> { sb.append(s[i + 1]); i += 2; continue }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    /**
     * Parses a flat JSON object string like `{"id":1,"name":"foo"}` into a Map<String, String?>
     */
    fun parseObject(json: String): Map<String, String?> {
        val result = mutableMapOf<String, String?>()
        val trimmed = json.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return result
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return result

        var i = 0
        while (i < content.length) {
            // Find key start
            while (i < content.length && (content[i].isWhitespace() || content[i] == ',')) i++
            if (i >= content.length) break
            if (content[i] != '"') break
            i++ // skip opening quote
            val keyStart = i
            while (i < content.length && content[i] != '"') {
                if (content[i] == '\\' && i + 1 < content.length) i++
                i++
            }
            val key = unescapeJson(content.substring(keyStart, i))
            if (i < content.length && content[i] == '"') i++

            // Find colon
            while (i < content.length && content[i] != ':') i++
            if (i < content.length && content[i] == ':') i++
            while (i < content.length && content[i].isWhitespace()) i++
            if (i >= content.length) break

            // Read value
            if (content[i] == '"') {
                i++ // skip opening quote
                val valStart = i
                while (i < content.length && content[i] != '"') {
                    if (content[i] == '\\' && i + 1 < content.length) i++
                    i++
                }
                val value = unescapeJson(content.substring(valStart, i))
                if (i < content.length && content[i] == '"') i++
                result[key] = value
            } else if (content[i] == '[' || content[i] == '{') {
                // Nested structure
                val openChar = content[i]
                val closeChar = if (openChar == '[') ']' else '}'
                var depth = 0
                val structStart = i
                var inString = false
                while (i < content.length) {
                    val c = content[i]
                    if (c == '"' && (i == 0 || content[i - 1] != '\\')) {
                        inString = !inString
                    } else if (!inString) {
                        if (c == openChar) depth++
                        else if (c == closeChar) {
                            depth--
                            if (depth == 0) {
                                i++
                                break
                            }
                        }
                    }
                    i++
                }
                result[key] = content.substring(structStart, minOf(i, content.length))
            } else {
                // Number, boolean, or null
                val valStart = i
                while (i < content.length && content[i] != ',' && content[i] != '}') {
                    i++
                }
                val rawVal = content.substring(valStart, i).trim()
                result[key] = if (rawVal == "null") null else rawVal
            }
        }
        return result
    }

    /**
     * Splits a JSON array string `[ {...}, {...} ]` into individual element strings
     */
    fun splitArray(json: String): List<String> {
        val list = mutableListOf<String>()
        val trimmed = json.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return list
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return list

        var depth = 0
        var inString = false
        var elemStart = -1

        for (i in content.indices) {
            val c = content[i]
            if (c == '"' && (i == 0 || content[i - 1] != '\\')) {
                inString = !inString
            } else if (!inString) {
                if (c == '{' || c == '[') {
                    if (depth == 0) elemStart = i
                    depth++
                } else if (c == '}' || c == ']') {
                    depth--
                    if (depth == 0 && elemStart != -1) {
                        list.add(content.substring(elemStart, i + 1))
                        elemStart = -1
                    }
                }
            }
        }
        return list
    }
}
