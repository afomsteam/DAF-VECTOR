package com.afomsteam.enlistedplanner.data

import android.content.Context
import com.google.gson.Gson

class PlannerRepository(context: Context) {
    private val prefs = context.getSharedPreferences("enlisted_planner_v2_full", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): PlannerState = try {
        prefs.getString(KEY_STATE, null)?.let { gson.fromJson(it, PlannerState::class.java) } ?: PlannerState()
    } catch (_: Exception) {
        PlannerState()
    }

    fun save(state: PlannerState) {
        prefs.edit().putString(KEY_STATE, gson.toJson(state)).apply()
    }

    fun exportJson(state: PlannerState): String = gson.toJson(state)

    fun importJson(json: String): PlannerState = gson.fromJson(json, PlannerState::class.java)

    companion object { private const val KEY_STATE = "planner_state" }
}
