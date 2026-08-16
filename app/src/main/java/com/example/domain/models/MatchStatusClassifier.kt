package com.example.domain.models

object MatchStatusClassifier {

    private val LIVE_STATUS_KEYWORDS = setOf(
        "1H", "HT", "2H", "ET", "BT", "P", "LIVE", "IN_PLAY", "INPLAY",
        "HALF_TIME", "FIRST_HALF", "SECOND_HALF", "EXTRA_TIME", "PENALTIES"
    )

    private val FINISHED_STATUS_KEYWORDS = setOf(
        "FT", "AET", "PEN", "FINISHED", "FIN", "ENDED", "AFTER_EXTRA_TIME", "AFTER_PENALTIES", "FINAL"
    )

    private val NOT_STARTED_STATUS_KEYWORDS = setOf(
        "NS", "TBD", "PST", "CANC", "ABD", "SCHEDULED", "NOT_STARTED", "POSTPONED", "CANCELLED", "ABANDONED"
    )

    fun isLive(statusShort: String?, statusText: String?, eventLive: String? = null): Boolean {
        if (eventLive == "1") return true
        if (eventLive == "0") return false

        val short = statusShort?.trim()?.uppercase() ?: ""
        val text = statusText?.trim()?.uppercase() ?: ""

        // 1. Finished statuses MUST NEVER be counted as live
        if (FINISHED_STATUS_KEYWORDS.contains(short) || FINISHED_STATUS_KEYWORDS.contains(text) ||
            short.startsWith("FT") || text.startsWith("FT") ||
            short.startsWith("FIN") || text.contains("FINISH") ||
            text.contains("ENDED") || text == "FINAL"
        ) {
            return false
        }

        // 2. Not started / postponed
        if (NOT_STARTED_STATUS_KEYWORDS.contains(short) || NOT_STARTED_STATUS_KEYWORDS.contains(text) ||
            text.contains("POSTPON") || text.contains("CANCEL") || text.contains("ABANDON")
        ) {
            return false
        }

        // 3. Live indicators
        if (LIVE_STATUS_KEYWORDS.contains(short) || LIVE_STATUS_KEYWORDS.contains(text) ||
            short.contains("1H") || short.contains("2H") || short.contains("HT") ||
            text.contains("1ST") || text.contains("2ND") || text.contains("HALF") ||
            text.contains("LIVE") || text.contains("IN PLAY")
        ) {
            return true
        }

        // 4. Minute pattern e.g. "45'", "72", "90+3'"
        if (short.isNotEmpty() && (short.all { it.isDigit() } || (short.endsWith("'") && short.dropLast(1).all { it.isDigit() || it == '+' }))) {
            return true
        }

        return false
    }

    fun toMatchStatus(statusShort: String?, statusText: String?): MatchStatus {
        val short = statusShort?.trim()?.uppercase() ?: ""
        val text = statusText?.trim()?.uppercase() ?: ""

        return when {
            FINISHED_STATUS_KEYWORDS.contains(short) || FINISHED_STATUS_KEYWORDS.contains(text) ||
                short.startsWith("FT") || text.startsWith("FT") || short.startsWith("FIN") || text.contains("FINISH") -> MatchStatus.FINISHED
            short == "HT" || text.contains("HALF") -> MatchStatus.HALF_TIME
            isLive(statusShort, statusText) -> MatchStatus.LIVE
            short == "PST" || text.contains("POST") -> MatchStatus.POSTPONED
            short == "CANC" || text.contains("CANC") -> MatchStatus.CANCELLED
            else -> MatchStatus.SCHEDULED
        }
    }
}
