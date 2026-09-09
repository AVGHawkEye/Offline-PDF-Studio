package com.example.service

object RangeParser {
    /**
     * Parses a page range string (e.g., "1-3, 5, 8-10", "all", "odd", "even")
     * Returns a sorted, distinct list of 0-based page indices valid for the given totalPages.
     */
    fun parse(input: String, totalPages: Int): List<Int> {
        if (totalPages <= 0) return emptyList()
        val trimmed = input.trim().lowercase()
        if (trimmed.isEmpty() || trimmed == "all") {
            return (0 until totalPages).toList()
        }
        if (trimmed == "odd") {
            return (0 until totalPages).filter { it % 2 == 0 } // Page 1, 3, 5 is index 0, 2, 4
        }
        if (trimmed == "even") {
            return (0 until totalPages).filter { it % 2 == 1 } // Page 2, 4 is index 1, 3
        }

        val result = mutableSetOf<Int>()
        val parts = input.split(",", ";", " ")
        for (rawPart in parts) {
            val part = rawPart.trim()
            if (part.isEmpty()) continue
            if (part.contains("-")) {
                val rangeTokens = part.split("-")
                if (rangeTokens.size == 2) {
                    val start = rangeTokens[0].trim().toIntOrNull()
                    val end = rangeTokens[1].trim().toIntOrNull()
                    if (start != null && end != null) {
                        val low = minOf(start, end).coerceAtLeast(1)
                        val high = maxOf(start, end).coerceAtMost(totalPages)
                        for (p in low..high) {
                            result.add(p - 1)
                        }
                    }
                }
            } else {
                val pageNum = part.toIntOrNull()
                if (pageNum != null && pageNum in 1..totalPages) {
                    result.add(pageNum - 1)
                }
            }
        }
        return result.sorted()
    }

    fun isValid(input: String, totalPages: Int): Boolean {
        return parse(input, totalPages).isNotEmpty()
    }
}
