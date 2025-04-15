package com.example.signifybasic.games

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object   ModuleManager {
    private lateinit var modules: List<GameModule>

    var currentModuleIndex = 0
    var currentStepIndex = 0

    fun loadModules(context: Context) {
        if (ModuleManager::modules.isInitialized) return

        val jsonString = context.assets.open("modules.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<GameModule>>() {}.type
        modules = Gson().fromJson(jsonString, type)
    }

    fun getModuleIndex() : Int {
        return currentModuleIndex
    }

    fun getCurrentStep(): GameStep? {
        return modules.getOrNull(currentModuleIndex)
            ?.games
            ?.getOrNull(currentStepIndex)
    }

    fun moveToNextStep() {
        val currentModule = modules.getOrNull(currentModuleIndex) ?: return

        if (currentStepIndex < currentModule.games.size - 1) {
            currentStepIndex++
        } else {
            // Reached the end of the module, stay on the last step
            Log.d("DEBUG_FLOW", "Reached end of module $currentModuleIndex, not moving to next module")
        }
    }

    fun resetModule(index: Int) {
        currentModuleIndex = index
        currentStepIndex = 0
    }

    fun isAllModulesComplete(): Boolean {
        return currentModuleIndex >= modules.size
    }

    fun getModules(): List<GameModule> {
        return modules
    }
}

