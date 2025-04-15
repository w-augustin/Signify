package com.example.signifybasic.features.games

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.signifybasic.features.games.FillBlankGameActivity
import com.example.signifybasic.features.games.SelectingGameActivity
import com.example.signifybasic.features.games.IdentifyGameActivity
import com.example.signifybasic.features.games.MatchingGameActivity
import com.example.signifybasic.features.games.SigningGameActivity

object GameRouter {
    fun routeToGame(context: Context, stepIndex: Int) {
        GameSequenceManager.load(context)

        val step = ModuleManager.getCurrentStep()

        if (step == null) {
            Toast.makeText(context, "No more games to play.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = when (step.type) {
            "selecting" -> Intent(context, SelectingGameActivity::class.java)
            "identify" -> Intent(context, IdentifyGameActivity::class.java)
            "fill_blank" -> Intent(context, FillBlankGameActivity::class.java)
            "matching" -> Intent(context, MatchingGameActivity::class.java)
            "signing" -> Intent(context, SigningGameActivity::class.java)
            "spell_word" -> Intent(context, SpellWordGameActivity::class.java)
            else -> {
                Toast.makeText(context, "Unknown game type: ${step.type}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        intent.putExtra("STEP_INDEX", stepIndex)
        context.startActivity(intent)
    }
}

