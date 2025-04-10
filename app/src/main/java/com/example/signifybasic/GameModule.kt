package com.example.signifybasic.features.games

import com.example.signifybasic.features.games.GameStep

data class GameModule(
    val moduleId: String,
    val title: String,
    val description: String,
    val games: List<GameStep>
)
