package com.example.signifybasic.games

// helper class for modules
data class GameModule(
    val moduleId: String,
    val title: String,
    val description: String,
    val games: List<GameStep>
)
