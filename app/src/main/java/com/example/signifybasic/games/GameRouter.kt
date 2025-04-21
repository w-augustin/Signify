package com.example.signifybasic.games

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.signifybasic.features.games.FillBlankGameActivity
import com.example.signifybasic.features.games.IdentifyGameActivity
import com.example.signifybasic.features.games.SelectingGameActivity

object GameRouter {

    fun routeToGame(context: Context, stepIndex: Int) {
        routeToGame(context, stepIndex, null)
    }

    fun routeToGame(context: Context, stepIndex: Int, options: Bundle?) {

        val step = ModuleManager.getModules()[ModuleManager.currentModuleIndex].games[stepIndex]

        if (step == null) {
            Toast.makeText(context, "No more games to play.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = when (step.type) {
            "spell_word" -> Intent(context, SpellWordGameActivity::class.java)
            "selecting" -> Intent(context, SelectingGameActivity::class.java)
            "identify" -> Intent(context, IdentifyGameActivity::class.java)
            "fill_blank" -> Intent(context, FillBlankGameActivity::class.java)
            "matching" -> Intent(context, MatchingGameActivity::class.java)
            "signing" -> Intent(context, SigningGameActivity::class.java)
            else -> {
                Toast.makeText(context, "Unknown game type: ${step.type}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        intent.putExtra("STEP_INDEX", stepIndex)
        if (context is Activity && options != null) {
            context.startActivity(intent, options)
        } else {
            context.startActivity(intent)
        }
    }
}

