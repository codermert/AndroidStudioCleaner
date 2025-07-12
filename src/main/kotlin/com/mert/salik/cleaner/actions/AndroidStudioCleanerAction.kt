package com.mert.salik.cleaner.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.mert.salik.cleaner.ui.MainDialog
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import javax.swing.KeyStroke
import com.mert.salik.cleaner.services.SettingsService

class AndroidStudioCleanerAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project: Project? = event.project
        val dialog = MainDialog(project)
        dialog.show()
    }

    override fun update(event: AnActionEvent) {
        // Action her zaman aktif olsun
        event.presentation.isEnabledAndVisible = true
    }

    companion object {
        fun updateShortcut() {
            val actionManager = ActionManager.getInstance()
            val actionId = "AndroidStudioCleanerAction"
            val action = actionManager.getAction(actionId)
            val settings = SettingsService.getInstance()
            val shortcutString = settings.getShortcut().ifBlank { "control alt c" }
            val keyStroke = KeyStroke.getKeyStroke(shortcutString.replace(" ", "-").uppercase())
            if (action != null && keyStroke != null) {
                action.shortcutSet = com.intellij.openapi.actionSystem.CustomShortcutSet(KeyboardShortcut(keyStroke, null))
            }
        }
    }
} 