package app.tsukumo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tsukumo.R
import app.tsukumo.container
import app.tsukumo.data.Settings
import app.tsukumo.domain.Outcome
import app.tsukumo.domain.State
import app.tsukumo.ru
import app.tsukumo.ui.art.Mood
import app.tsukumo.ui.art.World
import app.tsukumo.ui.art.mood
import app.tsukumo.ui.chronicle.ChronicleScreen
import app.tsukumo.ui.hobbies.AddHobbySheet
import app.tsukumo.ui.hobbies.AddThingSheet
import app.tsukumo.ui.hobbies.HobbiesScreen
import app.tsukumo.ui.home.CandlesSheet
import app.tsukumo.ui.home.ChestSheet
import app.tsukumo.ui.home.HomeScreen
import app.tsukumo.ui.home.SpiritSheet
import app.tsukumo.ui.onboarding.Onboarding
import app.tsukumo.ui.session.ResultSheet
import app.tsukumo.ui.session.SessionSheet
import app.tsukumo.ui.settings.SettingsScreen
import app.tsukumo.ui.theme.C
import app.tsukumo.ui.theme.T
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/** Everything a screen reads, at one moment. */
class Home(val s: State, val settings: Settings, val w: Words, val mood: Mood, val world: World, val nowMs: Long) {
    val ru get() = w.ru
}

enum class Tab { HOUSE, HOBBIES, CHRONICLE }

sealed interface Overlay {
    data class Session(val hobby: Long?) : Overlay
    data class Result(val out: Outcome, val hobby: Long) : Overlay
    data class Spirit(val id: Long) : Overlay
    data object Chest : Overlay
    data object Candles : Overlay
    data object AddHobby : Overlay
    data class AddThing(val hobby: Long) : Overlay
    data object Settings : Overlay
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TsukumoRoot(settings: Settings) {
    val ctx = LocalContext.current
    val c = ctx.container
    val state by c.repo.state.collectAsStateWithLifecycle()
    val words = remember(ctx.ru) { Words(ctx.ru) }
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        c.repo.refresh()
        while (true) { delay(30_000); nowMs = System.currentTimeMillis() }
    }
    if (!settings.onboarded) {
        Onboarding(words)
        return
    }
    val s = state ?: run { Box(Modifier.fillMaxSize().background(C.bg)); return }
    val now = LocalDateTime.now()
    val m = remember(s, now.hour, now.monthValue, nowMs / 600_000) { mood(s, now, nowMs) }
    val world = remember { World { th, md -> words.line(th, md, c.repo.state.value) } }
    LaunchedEffect(s, m) { world.sync(s.living, m) }
    val home = Home(s, settings, words, m, world, nowMs)

    var tab by remember { mutableStateOf(Tab.HOUSE) }
    val overlays = remember { mutableStateListOf<Overlay>() }
    fun push(o: Overlay) = overlays.add(o)
    fun pop() { if (overlays.isNotEmpty()) overlays.removeAt(overlays.lastIndex) }
    BackHandler(enabled = overlays.isEmpty() && tab != Tab.HOUSE) { tab = Tab.HOUSE }

    Box(Modifier.fillMaxSize().background(C.bg).imePadding()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    Tab.HOUSE -> HomeScreen(home,
                        onSession = { push(Overlay.Session(it)) }, onSpirit = { push(Overlay.Spirit(it)) },
                        onChest = { push(Overlay.Chest) }, onCandles = { push(Overlay.Candles) }, onSettings = { push(Overlay.Settings) })
                    Tab.HOBBIES -> HobbiesScreen(home,
                        onSpirit = { push(Overlay.Spirit(it)) }, onAddHobby = { push(Overlay.AddHobby) },
                        onAddThing = { push(Overlay.AddThing(it)) }, onSession = { push(Overlay.Session(it)) })
                    Tab.CHRONICLE -> ChronicleScreen(home)
                }
            }
            if (!WindowInsets.isImeVisible) TabBar(tab) { tab = it }
        }
        when (val o = overlays.lastOrNull()) {
            is Overlay.Session -> SessionSheet(home, o.hobby, onClose = { pop() }, onDone = { out, h -> pop(); push(Overlay.Result(out, h)) })
            is Overlay.Result -> ResultSheet(home, o.out, o.hobby, onClose = { pop(); tab = Tab.HOUSE })
            is Overlay.Spirit -> SpiritSheet(home, o.id, onClose = { pop() }, onSession = { pop(); push(Overlay.Session(it)) })
            Overlay.Chest -> ChestSheet(home, onClose = { pop() })
            Overlay.Candles -> CandlesSheet(home, onClose = { pop() }, onChronicle = { pop(); tab = Tab.CHRONICLE })
            Overlay.AddHobby -> AddHobbySheet(home, onClose = { pop() }, onCustom = { pop(); push(Overlay.AddThing(it)) })
            is Overlay.AddThing -> AddThingSheet(home, o.hobby, onClose = { pop() })
            Overlay.Settings -> SettingsScreen(home, onClose = { pop() })
            null -> Unit
        }
    }
}

@Composable
private fun TabBar(tab: Tab, onTab: (Tab) -> Unit) {
    val items = listOf(Tab.HOUSE to R.string.tab_house, Tab.HOBBIES to R.string.tab_hobbies, Tab.CHRONICLE to R.string.tab_chronicle)
    Row(Modifier.fillMaxWidth().background(C.line).navigationBarsPadding().padding(top = 2.dp)) {
        for ((t, label) in items) {
            val sel = t == tab
            Column(
                Modifier.weight(1f).clickable { onTab(t) }.background(if (sel) C.panel else C.panel2).padding(top = 6.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TabIcon(t, sel)
                Text(stringResource(label), style = T.Small.copy(color = if (sel) C.gold else C.muted))
            }
        }
    }
}

/** Tiny pixel icons for the tabs: a house, a shelf of things, a scroll. */
@Composable
private fun TabIcon(t: Tab, sel: Boolean) {
    val ink = if (sel) C.gold else C.muted
    val accent = if (sel) C.accent else C.faint
    Canvas(Modifier.size(24.dp).padding(2.dp)) {
        val p = size.width / 10
        fun r(x: Int, y: Int, w: Int, h: Int, col: Color = ink) = drawRect(col, Offset(x * p, y * p), Size(w * p, h * p))
        when (t) {
            Tab.HOUSE -> { r(0, 3, 10, 1); r(1, 2, 8, 1); r(2, 1, 6, 1); r(1, 4, 1, 6); r(8, 4, 1, 6); r(1, 9, 8, 1); r(4, 6, 2, 3, accent); r(2, 5, 1, 1, accent); r(7, 5, 1, 1, accent) }
            Tab.HOBBIES -> { r(0, 4, 10, 1); r(0, 9, 10, 1); r(1, 1, 2, 3, accent); r(4, 2, 2, 2); r(7, 0, 2, 4, accent); r(1, 6, 3, 3); r(6, 5, 2, 4, accent) }
            Tab.CHRONICLE -> { r(1, 0, 8, 1); r(1, 9, 8, 1); r(2, 1, 6, 8, C.paper.copy(alpha = if (sel) 1f else .5f)); r(3, 2, 4, 1); r(3, 4, 4, 1); r(3, 6, 3, 1); r(6, 7, 1, 1, accent) }
        }
    }
}
