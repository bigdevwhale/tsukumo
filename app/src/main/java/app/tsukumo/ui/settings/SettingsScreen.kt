package app.tsukumo.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import app.tsukumo.BuildConfig
import app.tsukumo.ui.Home
import app.tsukumo.ui.components.Btn
import app.tsukumo.ui.components.Chip
import app.tsukumo.ui.components.CommunityLinks
import app.tsukumo.ui.components.Eyebrow
import app.tsukumo.ui.components.Flow
import app.tsukumo.ui.components.Gap
import app.tsukumo.ui.components.Sheet
import app.tsukumo.ui.theme.T

@Composable
fun SettingsScreen(h: Home, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val w = h.w
    val cur = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    Sheet(w.t("Настройки", "Settings"), onClose, w.t("Закрыть", "Close")) {
        Eyebrow(w.t("Язык", "Language"))
        Gap(6)
        Flow {
            for ((tag, label) in listOf("" to w.t("Как в системе", "System"), "ru" to "Русский", "en" to "English"))
                Chip(label, cur == tag) { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) }
        }
        Gap(20)
        Eyebrow(w.t("Сообщество", "Community"))
        Gap(6)
        CommunityLinks(w)
        Gap(20)
        Eyebrow(w.t("О приложении", "About"))
        Gap(6)
        Text(w.t("Цукумо ${BuildConfig.VERSION_NAME}. Без рекламы, аккаунтов и интернета: дом живёт только на этом телефоне.",
            "Tsukumo ${BuildConfig.VERSION_NAME}. No ads, accounts or internet: the house lives only on this phone."), style = T.Body)
        Gap(8)
        Text(w.t("Шрифт m6x11 — Daniel Linssen, кириллица — Natto Games.", "The m6x11 font is by Daniel Linssen, Cyrillic by Natto Games."), style = T.Small)
        Gap(12)
        Btn(w.t("Другие приложения Stillcraft", "More Stillcraft apps"), Modifier.fillMaxWidth()) {
            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bigdevwhale.github.io/stillcraft/"))) }
        }
        Gap(8)
        Btn(w.t("Исходный код", "Source code"), Modifier.fillMaxWidth()) {
            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bigdevwhale/tsukumo"))) }
        }
    }
}
