package com.example.signifybasic.games

import java.io.Serializable

// helper class for importing data
data class MatchingItem(
    val letter: String,
    val drawableRes: Int
) : Serializable
