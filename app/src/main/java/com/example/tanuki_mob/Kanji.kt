package com.example.tanuki_mob

data class Kanji (val id: Int = 0,
                 val sign: String = "",
                 val onyomi: List<String> = listOf(),
                 val onyomiRomaji: List<String> = listOf(),
                 val kunyomi: List<String> = listOf(),
                 val kunyomiRomaji: List<String> = listOf(),
                 val meaning: List<String> = listOf(),
                 val level: Int = 0) {
}
