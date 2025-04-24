package com.example.signifybasic.deprecated

object GameSequenceManager {
//    private var _sequence: List<GameStep>? = null
//    val sequence: List<GameStep>
//        get() = _sequence ?: throw IllegalStateException("Game sequence not loaded")
//
//    var currentIndex = 0
//        private set
//
//    fun load(context: Context) {
//        if (_sequence != null) return
//
//        val jsonString = context.assets.open("modules.json").bufferedReader().use { it.readText() }
//        val moduleType = object : TypeToken<List<GameModule>>() {}.type
//        val modules: List<GameModule> = Gson().fromJson(jsonString, moduleType)
//
//        _sequence = modules.flatMap { it.games }
//        currentIndex = 0
//    }
//
//
//    fun getCurrentGame(): GameStep? {
//        return _sequence?.getOrNull(currentIndex)
//    }
//
//    fun moveToNext() {
//        currentIndex++
//    }
//
//    fun reset() {
//        currentIndex = 0
//    }
//
//    fun isLastStep(): Boolean {
//        return currentIndex >= (sequence.size - 1)
//    }
//
//    fun isLoaded(): Boolean = _sequence != null
}
