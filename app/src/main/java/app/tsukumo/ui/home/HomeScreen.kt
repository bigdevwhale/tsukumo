package app.tsukumo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tsukumo.domain.Rules
import app.tsukumo.ui.Home
import app.tsukumo.ui.art.HouseView
import app.tsukumo.ui.components.Bar
import app.tsukumo.ui.components.Btn
import app.tsukumo.ui.components.Eyebrow
import app.tsukumo.ui.components.Gap
import app.tsukumo.ui.components.box
import app.tsukumo.ui.theme.C
import app.tsukumo.ui.theme.T
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(h: Home, onSession: (Long?) -> Unit, onSpirit: (Long) -> Unit, onChest: () -> Unit, onCandles: () -> Unit, onSettings: () -> Unit) {
    val w = h.w
    val s = h.s
    Column(Modifier.fillMaxSize().background(C.bg).statusBarsPadding().verticalScroll(rememberScrollState())) {
        HouseView(h.world, h.mood, onSpirit = onSpirit, onChest = onChest, onCandles = onCandles)
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(w.t("${s.year(h.nowMs) + 1}-й год дома", "Year ${s.year(h.nowMs) + 1} of the house"), style = T.Title)
                    Text("${w.candles(s.candles)} · ${w.season(h.mood.season)}", style = T.Sub)
                }
                Text("⚙", style = T.Title.copy(color = C.muted), modifier = Modifier.clickable(onClick = onSettings).padding(8.dp))
            }
            Gap(12)
            Timer(h, onSession)
            Btn(w.t("Зажечь свечу", "Light a candle"), Modifier.fillMaxWidth(), primary = true) { onSession(null) }
            Text(w.t("Каждое занятие любым хобби — одна свеча", "Every session of any hobby is one candle"), style = T.Small, modifier = Modifier.padding(top = 6.dp))
            Gap(14)
            s.nextWonder?.let { nx ->
                val prev = Rules.WONDERS.lastOrNull { it.at <= s.candles }?.at ?: 0
                Column(Modifier.fillMaxWidth().box().clickable(onClick = onCandles).padding(12.dp)) {
                    Eyebrow(w.t("Следующее чудо", "Next wonder"))
                    Text(w.wonder(nx.key), style = T.Heading)
                    Text(w.t("ещё ${w.candles(nx.at - s.candles)}", "${w.candles(nx.at - s.candles)} to go"), style = T.Small)
                    Gap(6)
                    Bar((s.candles - prev).toFloat() / (nx.at - prev), color = C.goldDeep)
                }
                Gap(14)
            }
            val asleep = h.mood.asleep
            if (asleep) {
                Text(w.t("Вас давно не было — духи спят под котацу. Никто не обижен: зажгите свечу, и они проснутся.",
                    "It's been a while — the spirits are asleep under the kotatsu. No hard feelings: light a candle and they'll wake."), style = T.Sub)
                Gap(14)
            }
            val recent = s.evs.filter { w.big(it) }.takeLast(5).reversed()
            if (recent.isNotEmpty()) {
                Eyebrow(w.t("Недавно", "Lately"))
                Gap(4)
                for (e in recent) Row(Modifier.padding(vertical = 3.dp)) {
                    Text("·", style = T.Body.copy(color = C.accent)); Spacer(Modifier.width(8.dp))
                    Text(w.event(e, s), style = T.Sub)
                }
            }
            Gap(10)
            Text(w.t("Нажмите на духа, на свечи в нише или на сундук.", "Tap a spirit, the candles in the alcove or the chest."), style = T.Small.copy(color = C.faint))
        }
    }
}

/** A running session timer, if one was started. */
@Composable
private fun Timer(h: Home, onSession: (Long?) -> Unit) {
    val st = h.settings
    if (st.timerSince == 0L) return
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(st.timerSince) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    val sec = ((now - st.timerSince) / 1000).coerceAtLeast(0)
    val hobby = h.s.hobby(st.timerHobby)
    Row(Modifier.fillMaxWidth().box(C.panel, C.accent).clickable { onSession(st.timerHobby) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Eyebrow(h.w.t("Идёт занятие", "Session running"))
            Text(h.w.hobby(hobby), style = T.Heading)
        }
        Text("%d:%02d".format(sec / 60, sec % 60), style = T.Number)
    }
    Gap(10)
}
