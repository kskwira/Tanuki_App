package com.example.tanuki_mob

data class Kanji(val id: Int,
                 val sign: String,
                 val onyomi: List<String>,
                 val onyomiRomaji: List<String>,
                 val kunyomi: List<String>,
                 val kunyomiRomaji: List<String>,
                 val meaning: List<String>,
                 val level: Int) {
}
