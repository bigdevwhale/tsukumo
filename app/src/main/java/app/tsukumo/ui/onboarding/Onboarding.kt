package app.tsukumo.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.tsukumo.container
import app.tsukumo.domain.Cat
import app.tsukumo.domain.Catalog
import app.tsukumo.ui.Words
import app.tsukumo.ui.art.Sprites
import app.tsukumo.ui.components.Btn
import app.tsukumo.ui.components.Chip
import app.tsukumo.ui.components.Eyebrow
import app.tsukumo.ui.components.Flow
import app.tsukumo.ui.components.Gap
import app.tsukumo.ui.components.SpriteImage
import app.tsukumo.ui.components.box
import app.tsukumo.ui.theme.C
import app.tsukumo.ui.theme.T
import kotlinx.coroutines.launch

@Composable
fun Onboarding(w: Words, startPage: Int = 0) {
    val c = LocalContext.current.container
    var page by remember { mutableIntStateOf(startPage) }
    var picked by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(C.bg).statusBarsPadding().navigationBarsPadding()) {
      Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
        when (page) {
            0 -> {
                Eyebrow(w.t("Цукумо · つくも", "Tsukumo · つくも"))
                Gap(6)
                Text(w.t("Вещи, которыми долго пользуются, обретают душу", "Things used long and well grow a soul"), style = T.Display)
                Gap(14)
                Text(w.t("Так в Японии говорят о цукумогами: старый зонтик, фонарь или чайник однажды открывает глаза. Здесь это происходит с вещами ваших хобби — гитарой, клубком, кистью, закваской.",
                    "That is what Japan says of tsukumogami: one day an old umbrella, lantern or teapot opens its eyes. Here it happens to the things of your hobbies — a guitar, a ball of yarn, a brush, a sourdough starter."), style = T.Body)
                Gap(18)
                Row(Modifier.fillMaxWidth().box().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    for (st in 0..5) SpriteImage(Sprites.Look("yarn", "sakura", st, legs = if (st >= 4) 1 else 0), 1.4f)
                }
                Text(w.t("вещь → вздрагивает → глаз → глаза → ножки → дух дома", "thing → shiver → an eye → both eyes → legs → house spirit"), style = T.Small, modifier = Modifier.padding(top = 4.dp))
            }
            1 -> {
                Text(w.t("Занятие — свеча", "A session is a candle"), style = T.Display)
                Gap(14)
                Text(w.t("После занятия отметьте, сколько минут и что вы делали: повторяли, пробовали новое, исправляли, завершали, учились, делились. Каждая вещь любит свой глагол и от него просыпается быстрее.",
                    "After a session, note the minutes and what you did: repeated, tried something new, fixed, finished, learned, shared. Each thing loves its own verb and wakes faster from it."), style = T.Body)
                Gap(12)
                Text(w.t("Проснувшийся дух растёт годами — Хранитель, Старейшина, Мудрец, Ками. А дом с каждой сотней свечей получает чудо.",
                    "An awake spirit keeps growing for years — Keeper, Elder, Sage, Kami. And every hundred candles bring the house a wonder."), style = T.Body)
                Gap(18)
                Row(Modifier.fillMaxWidth().box().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    for (rk in 0..4) SpriteImage(Sprites.Look("guitar", "wood", 5, rk, legs = 1), 1.6f)
                }
            }
            2 -> {
                Text(w.t("Без чувства вины", "No guilt"), style = T.Display)
                Gap(14)
                Text(w.t("Пропуски ничего не отнимают. Если вас долго нет, духи просто спят под котацу. Хобби можно убрать в кладовую — и достать через год: духи помнят.",
                    "Missed days take nothing away. If you are gone a while, the spirits just sleep under the kotatsu. A hobby can go into the storeroom and come back a year later: the spirits remember."), style = T.Body)
                Gap(12)
                Text(w.t("Без рекламы, без аккаунтов, без интернета. Всё хранится только на телефоне.", "No ads, no accounts, no internet. Everything stays on the phone."), style = T.Sub)
            }
            else -> {
                Text(w.t("Чем вы увлекаетесь?", "What are your hobbies?"), style = T.Display)
                Gap(8)
                Text(w.t("Выберите одно-два — их вещи встанут на полки. Своё хобби можно завести потом.", "Pick one or two — their things will go on the shelves. Your own hobby can be added later."), style = T.Sub)
                Gap(12)
                for (ct in Cat.entries) {
                    Eyebrow("${w.catIcon(ct)} ${w.cat(ct)}")
                    Gap(4)
                    Flow { for (h in Catalog.hobbies.filter { it.cat == ct }) Chip(h.name(w.ru), h.key in picked) { picked = if (h.key in picked) picked - h.key else picked + h.key } }
                    Gap(10)
                }
            }
        }
      }
        Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (page > 0) Btn(w.t("Назад", "Back")) { page-- }
            if (page < 3) Btn(w.t("Дальше", "Next"), Modifier.weight(1f), primary = true) { page++ }
            else Btn(w.t("В дом", "Into the house"), Modifier.weight(1f), primary = true, enabled = picked.isNotEmpty()) {
                scope.launch {
                    for (k in Catalog.hobbies.map { it.key }.filter { it in picked }) c.repo.adopt(k)
                    c.settings.update { it.copy(onboarded = true) }
                }
            }
        }
    }
}
