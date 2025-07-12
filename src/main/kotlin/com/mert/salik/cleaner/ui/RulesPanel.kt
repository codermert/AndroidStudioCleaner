package com.mert.salik.cleaner.ui

import com.intellij.ui.components.JBScrollPane
import com.mert.salik.cleaner.models.CleaningCategory
import com.mert.salik.cleaner.services.LanguageService
import com.mert.salik.cleaner.services.SettingsService
import com.mert.salik.cleaner.services.LogService
import com.mert.salik.cleaner.services.LogLevel
import java.awt.*
import javax.swing.*
import javax.swing.border.TitledBorder

class RulesPanel : JPanel() {
    private val languageService = LanguageService.getInstance()
    private val settingsService = SettingsService.getInstance()
    private val logService = LogService.getInstance()

    private val categoryCheckboxes = mutableMapOf<CleaningCategory, JCheckBox>()
    private val selectAllButton = JButton()
    private val deselectAllButton = JButton()
    private val applyButton = JButton()
    private val resetButton = JButton()
    private val saveButton = JButton()

    init {
        setupUI()
        setupListeners()
        loadCurrentSettings()
    }

    private fun setupUI() {
        layout = BorderLayout(10, 10)
        border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        // Title
        val titleLabel = JLabel(languageService.getString("rules.title"))
        titleLabel.font = Font("Arial", Font.BOLD, 18)
        titleLabel.foreground = Color(52, 73, 94)
        add(titleLabel, BorderLayout.NORTH)

        // Description
        val descriptionLabel = JLabel(languageService.getString("rules.description"))
        descriptionLabel.font = Font("Arial", Font.PLAIN, 12)
        descriptionLabel.foreground = Color(127, 140, 141)
        add(descriptionLabel, BorderLayout.CENTER)

        // Main content panel
        val contentPanel = JPanel(BorderLayout(10, 10))
        
        // Categories panel
        val categoriesPanel = createCategoriesPanel()
        val scrollPane = JBScrollPane(categoriesPanel)
        scrollPane.preferredSize = Dimension(400, 300)
        contentPanel.add(scrollPane, BorderLayout.CENTER)

        // Buttons panel
        val buttonsPanel = createButtonsPanel()
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH)

        add(contentPanel, BorderLayout.CENTER)
    }

    private fun createCategoriesPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Create checkboxes for each category
        CleaningCategory.values().forEach { category ->
            val checkbox = JCheckBox(languageService.getString("rules.category.${category.name.lowercase()}"))
            checkbox.font = Font("Arial", Font.PLAIN, 14)
            checkbox.isSelected = true // Default to selected
            
            // Add some spacing
            panel.add(checkbox)
            panel.add(Box.createVerticalStrut(8))
            
            categoryCheckboxes[category] = checkbox
        }

        return panel
    }

    private fun createButtonsPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 5))
        
        // Update button texts
        selectAllButton.text = languageService.getString("rules.select.all")
        deselectAllButton.text = languageService.getString("rules.deselect.all")
        applyButton.text = languageService.getString("rules.apply.button")
        resetButton.text = languageService.getString("rules.reset.button")
        saveButton.text = languageService.getString("rules.save.button")

        // Style buttons
        val buttons = listOf(selectAllButton, deselectAllButton, applyButton, resetButton, saveButton)
        buttons.forEach { button ->
            button.font = Font("Arial", Font.BOLD, 12)
            button.preferredSize = Dimension(120, 35)
            button.isFocusPainted = false
        }

        // Set colors
        selectAllButton.background = Color(46, 204, 113)
        selectAllButton.foreground = Color.WHITE
        
        deselectAllButton.background = Color(231, 76, 60)
        deselectAllButton.foreground = Color.WHITE
        
        applyButton.background = Color(52, 152, 219)
        applyButton.foreground = Color.WHITE
        
        resetButton.background = Color(155, 89, 182)
        resetButton.foreground = Color.WHITE
        
        saveButton.background = Color(230, 126, 34)
        saveButton.foreground = Color.WHITE

        panel.add(selectAllButton)
        panel.add(deselectAllButton)
        panel.add(applyButton)
        panel.add(resetButton)
        panel.add(saveButton)

        return panel
    }

    private fun setupListeners() {
        selectAllButton.addActionListener {
            selectAllCategories()
        }

        deselectAllButton.addActionListener {
            deselectAllCategories()
        }

        applyButton.addActionListener {
            applyRules()
        }

        resetButton.addActionListener {
            resetToDefaults()
        }

        saveButton.addActionListener {
            saveRules()
        }
    }

    private fun selectAllCategories() {
        categoryCheckboxes.values.forEach { checkbox ->
            checkbox.isSelected = true
        }
        logService.addLog(LogLevel.INFO, "All categories selected", "RulesPanel")
    }

    private fun deselectAllCategories() {
        categoryCheckboxes.values.forEach { checkbox ->
            checkbox.isSelected = false
        }
        logService.addLog(LogLevel.INFO, "All categories deselected", "RulesPanel")
    }

    private fun applyRules() {
        val selectedCategories = categoryCheckboxes
            .filter { it.value.isSelected }
            .map { it.key.name }
            .toSet()

        settingsService.setSelectedCategories(selectedCategories)
        
        // Update cleaning rules
        val rules = mutableMapOf<String, Boolean>()
        categoryCheckboxes.forEach { (category, checkbox) ->
            rules[category.name] = checkbox.isSelected
        }
        settingsService.setCleaningRules(rules)

        logService.addLog(LogLevel.INFO, "Rules applied: ${selectedCategories.size} categories selected", "RulesPanel")
        
        JOptionPane.showMessageDialog(
            this,
            languageService.getString("message.applying"),
            languageService.getString("message.info"),
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    private fun resetToDefaults() {
        val result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset all rules to defaults?",
            languageService.getString("message.warning"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )

        if (result == JOptionPane.YES_OPTION) {
            settingsService.resetToDefaults()
            loadCurrentSettings()
            logService.addLog(LogLevel.INFO, "Rules reset to defaults", "RulesPanel")
        }
    }

    private fun saveRules() {
        applyRules() // Apply current rules first
        
        // Save to persistent storage
        try {
            // SettingsService automatically saves when state changes
            logService.addLog(LogLevel.INFO, "Rules saved successfully", "RulesPanel")
            
            JOptionPane.showMessageDialog(
                this,
                languageService.getString("message.saving"),
                languageService.getString("message.success"),
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            logService.addLog(LogLevel.ERROR, "Failed to save rules: ${e.message}", "RulesPanel")
            
            JOptionPane.showMessageDialog(
                this,
                languageService.getString("error.settings.save"),
                languageService.getString("message.error"),
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun loadCurrentSettings() {
        val selectedCategories = settingsService.getSelectedCategories()
        val cleaningRules = settingsService.getCleaningRules()

        categoryCheckboxes.forEach { (category, checkbox) ->
            val isSelected = if (selectedCategories.isEmpty()) {
                // If no categories are selected, use rules or default to true
                cleaningRules[category.name] ?: true
            } else {
                selectedCategories.contains(category.name)
            }
            checkbox.isSelected = isSelected
        }
    }

    fun refreshLanguage() {
        // Update all text elements with current language
        selectAllButton.text = languageService.getString("rules.select.all")
        deselectAllButton.text = languageService.getString("rules.deselect.all")
        applyButton.text = languageService.getString("rules.apply.button")
        resetButton.text = languageService.getString("rules.reset.button")
        saveButton.text = languageService.getString("rules.save.button")

        // Update category labels
        categoryCheckboxes.forEach { (category, checkbox) ->
            checkbox.text = languageService.getString("rules.category.${category.name.lowercase()}")
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(500, 400)
    }
} 