package com.mert.salik.cleaner.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTabbedPane
import com.mert.salik.cleaner.services.LanguageService
import java.awt.Dimension
import javax.swing.JComponent

class MainDialog(private val project: Project?) : DialogWrapper(true) {
    private val languageService = LanguageService.getInstance()
    private val tabbedPane = JBTabbedPane()
    private val mainPanel = MainPanel(project)
    private val settingsPanel = SettingsPanel()

    init {
        title = languageService.getString("main.title")
        init()
        setupTabs()
    }

    private fun setupTabs() {
        tabbedPane.addTab(languageService.getString("main.scan.button"), mainPanel)
        tabbedPane.addTab(languageService.getString("main.settings.button"), settingsPanel)
    }

    override fun createCenterPanel(): JComponent {
        return tabbedPane
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mainPanel
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(800, 600)
    }
} 