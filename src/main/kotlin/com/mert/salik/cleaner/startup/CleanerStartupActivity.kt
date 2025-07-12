package com.mert.salik.cleaner.startup

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import kotlin.random.Random

class CleanerStartupActivity : StartupActivity.DumbAware {

    private val welcomeMessages = listOf(
        "👋 Merhaba! Kodlarla dans etmeye hazır mısın? Hadi başlayalım!",
        "🎯 Bugün harika bir şeyler geliştirme günü! Hoş geldin.",
        "🚀 Kod seni bekliyor, yaratıcılığını gösterme zamanı!",
        "👩‍💻 Hoş geldin geliştirici! Bugün neleri keşfedeceğiz?",
        "🌟 Büyük fikirler küçük adımlarla başlar. Hazırsan başlayalım!",
        "☕ Kahveni aldıysan kod zamanı! Android seni bekliyor.",
        "💡 Hadi bugün birkaç satır kodla dünyayı değiştir!",
        "🔧 Geliştirme moduna geçildi! Hadi üretmeye başlayalım.",
        "🌈 Hoş geldin! Kodla yarattığın dünyayı renklendir.",
        "🧠 Fikrin var, gücün var, IDE'n de hazır!",
        "📱 Haydi bugün yeni bir uygulamaya hayat ver!",
        "👣 Kod yolculuğunda bir adım daha... Hoş geldin!",
        "🧩 Hatalar dostundur! Bugün yeni şeyler öğrenmeye açık ol.",
        "🖥️ Android dünyasına tekrar hoş geldin! Seni özlemiştik.",
        "🧙‍♂️ Kod sihirbazı geri döndü! Bugün neler öğreneceğiz?",
        "🔍 Detaylarda başarı gizli... Hadi kodlara bakalım.",
        "🎉 Hatalar seni yıldırmasın, her satır seni ileri taşır.",
        "🧭 Kodla yön bulmaya hazır mısın? Yolculuk şimdi başlıyor.",
        "🛠️ Uygulaman seni bekliyor, geliştirme zamanı!",
        "💬 \"Kod yaz, test et, öğren, tekrar et.\" Bugün ne ekleyeceksin?"
    )

    override fun runActivity(project: Project) {
        showWelcomeNotification(project)
    }

    private fun showWelcomeNotification(project: Project?) {
        try {
            val notificationGroup = com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Android Studio Cleaner Notifications")
            val message = welcomeMessages.random()
            if (notificationGroup != null) {
                val notification = notificationGroup.createNotification(
                    "Android Studio Cleaner",
                    message,
                    com.intellij.notification.NotificationType.INFORMATION
                )
                com.intellij.notification.Notifications.Bus.notify(notification, project)
            } else {
                // Fallback: create a default notification
                val notification = com.intellij.notification.Notification(
                    "Android Studio Cleaner",
                    "Android Studio Cleaner",
                    message,
                    com.intellij.notification.NotificationType.INFORMATION
                )
                com.intellij.notification.Notifications.Bus.notify(notification)
            }
        } catch (ex: Exception) {
            println("[Android Studio Cleaner] Startup notification error: ${ex.message}")
        }
    }
} 