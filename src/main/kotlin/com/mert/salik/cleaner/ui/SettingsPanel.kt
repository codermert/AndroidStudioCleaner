package com.mert.salik.cleaner.ui

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.mert.salik.cleaner.services.LanguageService
import com.mert.salik.cleaner.services.ThemeService
import com.mert.salik.cleaner.services.SettingsService
import com.mert.salik.cleaner.services.LogService
import com.mert.salik.cleaner.services.LogLevel
import java.awt.*
import javax.swing.*
import javax.swing.border.TitledBorder
import com.mert.salik.cleaner.actions.AndroidStudioCleanerAction

class SettingsPanel : JPanel() {
    private val languageService = LanguageService.getInstance()
    private val themeService = ThemeService.getInstance()
    private val settingsService = SettingsService.getInstance()
    private val logService = LogService.getInstance()

    // UI Components
    private val languageComboBox = JComboBox<String>()
    private val themeComboBox = JComboBox<String>()
    private val autoScanCheckBox = JCheckBox()
    private val scanDepthComboBox = JComboBox<String>()
    private val minFileSizeComboBox = JComboBox<String>()
    private val showConfettiCheckBox = JCheckBox()
    private val detailedLoggingCheckBox = JCheckBox()
    private val backupBeforeCleanCheckBox = JCheckBox()
    private val confirmBeforeCleanCheckBox = JCheckBox()
    private val greetingField = JTextField(20)
    private val shortcutField = JTextField(20)
    
    private val saveButton = JButton()
    private val resetButton = JButton()
    private val cancelButton = JButton()
    private val aboutButton = JButton()

    init {
        setupUI()
        setupListeners()
        loadCurrentSettings()
    }

    private fun setupUI() {
        layout = BorderLayout(10, 10)
        border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        // Title
        val titleLabel = JBLabel(languageService.getString("settings.title"))
        titleLabel.font = Font("Arial", Font.BOLD, 18)
        titleLabel.foreground = Color(52, 73, 94)
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        add(titleLabel, BorderLayout.NORTH)

        // Main content panel
        val contentPanel = JPanel(BorderLayout(10, 10))
        
        // Settings panel
        val settingsPanel = createSettingsPanel()
        val scrollPane = JBScrollPane(settingsPanel)
        scrollPane.preferredSize = Dimension(500, 400)
        contentPanel.add(scrollPane, BorderLayout.CENTER)

        // Buttons panel
        val buttonsPanel = createButtonsPanel()
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH)

        add(contentPanel, BorderLayout.CENTER)
    }

    private fun createSettingsPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Personalization
        val personalizationPanel = createSettingGroup(
            "Personalization",
            createPersonalizationSettings()
        )
        panel.add(personalizationPanel)
        panel.add(Box.createVerticalStrut(20))

        // Language Settings
        val languagePanel = createSettingGroup(
            languageService.getString("settings.language"),
            createLanguageSettings()
        )
        panel.add(languagePanel)
        panel.add(Box.createVerticalStrut(20))

        // Theme Settings
        val themePanel = createSettingGroup(
            languageService.getString("settings.theme"),
            createThemeSettings()
        )
        panel.add(themePanel)
        panel.add(Box.createVerticalStrut(20))

        // Scan Settings
        val scanPanel = createSettingGroup(
            languageService.getString("settings.advanced"),
            createScanSettings()
        )
        panel.add(scanPanel)
        panel.add(Box.createVerticalStrut(20))

        // Advanced Settings
        val advancedPanel = createSettingGroup(
            "Advanced Options",
            createAdvancedSettings()
        )
        panel.add(advancedPanel)

        return panel
    }

    private fun createLanguageSettings(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        
        // Language combo box
        languageService.getAvailableLanguages().forEach { (code, name) ->
            languageComboBox.addItem(name)
        }
        languageComboBox.preferredSize = Dimension(150, 30)
        
        panel.add(JLabel("${languageService.getString("settings.language")}:"))
        panel.add(languageComboBox)
        
        return panel
    }

    private fun createThemeSettings(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        
        // Theme combo box
        themeComboBox.addItem(languageService.getString("settings.theme.light"))
        themeComboBox.addItem(languageService.getString("settings.theme.dark"))
        themeComboBox.preferredSize = Dimension(150, 30)
        
        panel.add(JLabel("${languageService.getString("settings.theme")}:"))
        panel.add(themeComboBox)
        
        return panel
    }

    private fun createScanSettings(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Auto scan checkbox
        autoScanCheckBox.text = languageService.getString("settings.auto.scan")
        autoScanCheckBox.font = Font("Arial", Font.PLAIN, 12)
        panel.add(autoScanCheckBox)
        panel.add(Box.createVerticalStrut(10))
        
        // Scan depth
        val scanDepthPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        scanDepthComboBox.addItem(languageService.getString("settings.scan.depth.shallow"))
        scanDepthComboBox.addItem(languageService.getString("settings.scan.depth.normal"))
        scanDepthComboBox.addItem(languageService.getString("settings.scan.depth.deep"))
        scanDepthComboBox.preferredSize = Dimension(150, 30)
        
        scanDepthPanel.add(JLabel("${languageService.getString("settings.scan.depth")}:"))
        scanDepthPanel.add(scanDepthComboBox)
        panel.add(scanDepthPanel)
        panel.add(Box.createVerticalStrut(10))
        
        // Minimum file size
        val minFileSizePanel = JPanel(FlowLayout(FlowLayout.LEFT))
        minFileSizeComboBox.addItem(languageService.getString("settings.min.file.size.small"))
        minFileSizeComboBox.addItem(languageService.getString("settings.min.file.size.medium"))
        minFileSizeComboBox.addItem(languageService.getString("settings.min.file.size.large"))
        minFileSizeComboBox.preferredSize = Dimension(150, 30)
        
        minFileSizePanel.add(JLabel("${languageService.getString("settings.min.file.size")}:"))
        minFileSizePanel.add(minFileSizeComboBox)
        panel.add(minFileSizePanel)
        
        return panel
    }

    private fun createAdvancedSettings(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Show confetti
        showConfettiCheckBox.text = "Show confetti animation after cleaning"
        showConfettiCheckBox.font = Font("Arial", Font.PLAIN, 12)
        panel.add(showConfettiCheckBox)
        panel.add(Box.createVerticalStrut(8))
        
        // Detailed logging
        detailedLoggingCheckBox.text = "Enable detailed logging"
        detailedLoggingCheckBox.font = Font("Arial", Font.PLAIN, 12)
        panel.add(detailedLoggingCheckBox)
        panel.add(Box.createVerticalStrut(8))
        
        // Backup before clean
        backupBeforeCleanCheckBox.text = "Create backup before cleaning"
        backupBeforeCleanCheckBox.font = Font("Arial", Font.PLAIN, 12)
        panel.add(backupBeforeCleanCheckBox)
        panel.add(Box.createVerticalStrut(8))
        
        // Confirm before clean
        confirmBeforeCleanCheckBox.text = "Ask for confirmation before cleaning"
        confirmBeforeCleanCheckBox.font = Font("Arial", Font.PLAIN, 12)
        panel.add(confirmBeforeCleanCheckBox)
        
        return panel
    }

    private fun createPersonalizationSettings(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        // Greeting
        val greetingPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        greetingPanel.add(JLabel("Greeting / Name:"))
        greetingPanel.add(greetingField)
        panel.add(greetingPanel)
        // Shortcut
        val shortcutPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        shortcutPanel.add(JLabel("Shortcut (e.g. control alt c):"))
        shortcutPanel.add(shortcutField)
        panel.add(shortcutPanel)
        return panel
    }

    private fun createSettingGroup(title: String, content: JPanel): JPanel {
        val panel = JPanel(BorderLayout(5, 5))
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            Font("Arial", Font.BOLD, 12),
            Color(52, 73, 94)
        )
        panel.add(content, BorderLayout.CENTER)
        return panel
    }

    private fun createButtonsPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 5))
        
        // Update button texts
        saveButton.text = languageService.getString("settings.save.button")
        resetButton.text = languageService.getString("settings.reset.button")
        cancelButton.text = languageService.getString("settings.cancel.button")
        aboutButton.text = languageService.getString("main.about.button")

        // Style buttons
        val buttons = listOf(saveButton, resetButton, cancelButton, aboutButton)
        buttons.forEach { button ->
            button.font = Font("Arial", Font.BOLD, 12)
            button.preferredSize = Dimension(100, 35)
            button.isFocusPainted = false
        }

        // Set colors
        saveButton.background = Color(46, 204, 113)
        saveButton.foreground = Color.WHITE
        
        resetButton.background = Color(231, 76, 60)
        resetButton.foreground = Color.WHITE
        
        cancelButton.background = Color(149, 165, 166)
        cancelButton.foreground = Color.WHITE

        aboutButton.background = Color(52, 152, 219)
        aboutButton.foreground = Color.WHITE

        panel.add(saveButton)
        panel.add(resetButton)
        panel.add(cancelButton)
        panel.add(aboutButton)

        return panel
    }

    private fun setupListeners() {
        saveButton.addActionListener {
            saveSettings()
        }

        resetButton.addActionListener {
            resetSettings()
        }

        cancelButton.addActionListener {
            loadCurrentSettings() // Reload current settings
        }

        aboutButton.addActionListener {
            showAboutDialog()
        }

        // Language change listener
        languageComboBox.addActionListener {
            val selectedIndex = languageComboBox.selectedIndex
            val languages = languageService.getAvailableLanguages()
            if (selectedIndex >= 0 && selectedIndex < languages.size) {
                val selectedLanguage = languages[selectedIndex].first
                languageService.setLanguage(selectedLanguage)
                refreshLanguage()
            }
        }
    }

    private fun loadCurrentSettings() {
        // Language
        val currentLanguage = settingsService.getLanguage()
        val languages = languageService.getAvailableLanguages()
        val languageIndex = languages.indexOfFirst { it.first == currentLanguage }
        if (languageIndex >= 0) {
            languageComboBox.selectedIndex = languageIndex
        }

        // Theme
        val currentTheme = settingsService.getTheme()
        themeComboBox.selectedIndex = if (currentTheme == "dark") 1 else 0

        // Auto scan
        autoScanCheckBox.isSelected = settingsService.isAutoScan()

        // Scan depth
        val scanDepth = settingsService.getScanDepth()
        scanDepthComboBox.selectedIndex = when (scanDepth) {
            "shallow" -> 0
            "deep" -> 2
            else -> 1 // normal
        }

        // Min file size
        val minFileSize = settingsService.getMinFileSize()
        minFileSizeComboBox.selectedIndex = when (minFileSize) {
            "small" -> 0
            "large" -> 2
            else -> 1 // medium
        }

        // Advanced settings
        showConfettiCheckBox.isSelected = settingsService.isShowConfetti()
        detailedLoggingCheckBox.isSelected = settingsService.isDetailedLogging()
        backupBeforeCleanCheckBox.isSelected = settingsService.isBackupBeforeClean()
        confirmBeforeCleanCheckBox.isSelected = settingsService.isConfirmBeforeClean()

        // Personalization
        greetingField.text = settingsService.getGreeting()
        shortcutField.text = settingsService.getShortcut()

        // Update button texts
        resetButton.text = languageService.getString("settings.reset.button")
        cancelButton.text = languageService.getString("settings.cancel.button")
        aboutButton.text = languageService.getString("main.about.button")
    }

    private fun saveSettings() {
        try {
            // Language
            val languages = languageService.getAvailableLanguages()
            val selectedLanguageIndex = languageComboBox.selectedIndex
            if (selectedLanguageIndex >= 0 && selectedLanguageIndex < languages.size) {
                settingsService.setLanguage(languages[selectedLanguageIndex].first)
            }

            // Theme
            settingsService.setTheme(if (themeComboBox.selectedIndex == 1) "dark" else "light")

            // Auto scan
            settingsService.setAutoScan(autoScanCheckBox.isSelected)

            // Scan depth
            val scanDepth = when (scanDepthComboBox.selectedIndex) {
                0 -> "shallow"
                2 -> "deep"
                else -> "normal"
            }
            settingsService.setScanDepth(scanDepth)

            // Min file size
            val minFileSize = when (minFileSizeComboBox.selectedIndex) {
                0 -> "small"
                2 -> "large"
                else -> "medium"
            }
            settingsService.setMinFileSize(minFileSize)

            // Advanced settings
            settingsService.setShowConfetti(showConfettiCheckBox.isSelected)
            settingsService.setDetailedLogging(detailedLoggingCheckBox.isSelected)
            settingsService.setBackupBeforeClean(backupBeforeCleanCheckBox.isSelected)
            settingsService.setConfirmBeforeClean(confirmBeforeCleanCheckBox.isSelected)

            // Personalization
            settingsService.setGreeting(greetingField.text)
            settingsService.setShortcut(shortcutField.text)

            logService.addLog(LogLevel.INFO, "Settings saved successfully", "SettingsPanel")
            
            JOptionPane.showMessageDialog(
                this,
                languageService.getString("message.saving"),
                languageService.getString("message.success"),
                JOptionPane.INFORMATION_MESSAGE
            )

            AndroidStudioCleanerAction.updateShortcut()
        } catch (e: Exception) {
            logService.addLog(LogLevel.ERROR, "Failed to save settings: ${e.message}", "SettingsPanel")
            
            JOptionPane.showMessageDialog(
                this,
                languageService.getString("error.settings.save"),
                languageService.getString("message.error"),
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun resetSettings() {
        val result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset all settings to defaults?",
            languageService.getString("message.warning"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )

        if (result == JOptionPane.YES_OPTION) {
            settingsService.resetToDefaults()
            loadCurrentSettings()
            logService.addLog(LogLevel.INFO, "Settings reset to defaults", "SettingsPanel")
        }
    }

    private fun refreshLanguage() {
        // Update all text elements with current language
        saveButton.text = languageService.getString("settings.save.button")
        resetButton.text = languageService.getString("settings.reset.button")
        cancelButton.text = languageService.getString("settings.cancel.button")
        aboutButton.text = languageService.getString("main.about.button")
        
        autoScanCheckBox.text = languageService.getString("settings.auto.scan")
        
        // Update combo box items
        updateComboBoxItems()
        
        revalidate()
        repaint()
    }

    private fun updateComboBoxItems() {
        // Update scan depth combo box
        scanDepthComboBox.removeAllItems()
        scanDepthComboBox.addItem(languageService.getString("settings.scan.depth.shallow"))
        scanDepthComboBox.addItem(languageService.getString("settings.scan.depth.normal"))
        scanDepthComboBox.addItem(languageService.getString("settings.scan.depth.deep"))
        
        // Update min file size combo box
        minFileSizeComboBox.removeAllItems()
        minFileSizeComboBox.addItem(languageService.getString("settings.min.file.size.small"))
        minFileSizeComboBox.addItem(languageService.getString("settings.min.file.size.medium"))
        minFileSizeComboBox.addItem(languageService.getString("settings.min.file.size.large"))
        
        // Update theme combo box
        themeComboBox.removeAllItems()
        themeComboBox.addItem(languageService.getString("settings.theme.light"))
        themeComboBox.addItem(languageService.getString("settings.theme.dark"))
    }

    private fun showAboutDialog() {
        val aboutTitle = languageService.getString("main.about.button")
        val aboutMessage = (
            "Android Studio Cleaner\n" +
            "--------------------------\n" +
            "${languageService.getString("about.developer")}: Mert SALIK\n" +
            "${languageService.getString("about.contact")}: codermert@bk.ru\n" +
            "\n" +
            languageService.getString("about.description") + "\n" +
            "\n" +
            "Magazin:\n" +
            "- Sabahekonomi: Flutter Dünyasına Yapay Zekâ Dokunuşu\n  https://www.sabahekonomi.com/flutter-dunyasina-yapay-zeka-dokunusu-mert-salik-tan-yenilikci-gelistirmeler/18659/\n" +
            "- BestLifeMagazin: Flutter Dünyasına Yapay Zekâ Dokunuşu\n  https://www.bestlifemagazin.com/flutter-dunyasina-yapay-zeka-dokunusu-mert-salik-tan-yenilikci-gelistirmeler/4266/\n" +
            "- HitMagazin: Flutter Dünyasına Yapay Zekâ Dokunuşu\n  https://hitmagazin.com.tr/flutter-dunyasina-yapay-zek-dokunusu-mert-salik-tan-yenilikci-gelistirmeler/1665/\n"
        )
        val textArea = javax.swing.JTextArea(aboutMessage)
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.font = java.awt.Font("monospaced", java.awt.Font.PLAIN, 13)
        val scrollPane = javax.swing.JScrollPane(textArea)
        scrollPane.preferredSize = java.awt.Dimension(480, 350)
        javax.swing.JOptionPane.showMessageDialog(this, scrollPane, aboutTitle, javax.swing.JOptionPane.INFORMATION_MESSAGE)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(600, 500)
    }
} 