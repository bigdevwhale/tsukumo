package app.tsukumo.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** A running session timer: which hobby, and since when (0 = none). */
    val timerHobby: Long = 0,
    val timerSince: Long = 0,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        timerHobby = this[TIMER_HOBBY] ?: 0,
        timerSince = this[TIMER_SINCE] ?: 0,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[TIMER_HOBBY] = s.timerHobby
            it[TIMER_SINCE] = s.timerSince
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val TIMER_HOBBY = longPreferencesKey("timer_hobby")
        val TIMER_SINCE = longPreferencesKey("timer_since")
    }
}
