package com.example.signifybasic.games

data class GameModule(
    val moduleId: String,
    val title: String,
    val description: String,
    val games: List<GameStep>
)
