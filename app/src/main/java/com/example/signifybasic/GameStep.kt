package com.example.signifybasic.features.games

data class GameStep(
    val type: String,
    val prompt: String? = null,
    val questionText: String? = null,  // used for identify
    val question: String? = null,      // used for fill in the blank
    val imageRes: String? = null,
    val options: List<Any>? = null,
    val items: List<Map<String, String>>? = null,
    val correctAnswer: String? = null,
    val resultKey: String,
    val expectedSign: String? = null // used for signing activity
)

