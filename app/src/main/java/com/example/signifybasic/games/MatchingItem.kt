package com.example.signifybasic.games

import java.io.Serializable

data class MatchingItem(
    val letter: String,
    val drawableRes: Int
) : Serializable
