package com.mert.salik.cleaner.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.mert.salik.cleaner.models.CleaningItem
import com.mert.salik.cleaner.models.CleaningResult
import com.mert.salik.cleaner.services.CleaningService
import com.mert.salik.cleaner.services.LanguageService
import com.mert.salik.cleaner.services.LogService
import com.mert.salik.cleaner.services.LogLevel
import com.mert.salik.cleaner.services.SettingsService
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.geom.Ellipse2D
import java.util.*
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.table.DefaultTableModel
import kotlin.math.cos
import kotlin.math.sin
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.notification.NotificationGroupManager

class MainPanel(private val project: Project?) : JPanel() {
    private val languageService = LanguageService.getInstance()
    private val cleaningService = CleaningService.getInstance()
    private val logService = LogService.getInstance()
    private val settingsService = SettingsService.getInstance()

    // Modern UI Components
    private val scanAndCleanButton = JButton()
    private val progressBar = JProgressBar()
    private val statusLabel = JLabel()
    private val resultPanel = JPanel()
    private val confettiPanel = ConfettiPanel()
    
    // Results display
    private val resultsTextArea = JTextArea()
    private val statsPanel = JPanel()
    private val itemsCleanedLabel = JLabel("0")
    private val spaceFreedLabel = JLabel("0 MB")
    private val timeTakenLabel = JLabel("0 saniye")
    private val pieChartPanel = PieChartPanel()

    // Progress tracking
    private var progressTimer: javax.swing.Timer? = null
    private var currentProgress = 0
    private val totalProgressSteps = 30 // 30 seconds total

    private var cleaningItems = mutableListOf<CleaningItem>()
    private var lastCleaningResult: CleaningResult? = null

    init {
        setupUI()
        setupListeners()
        setupStyling()
        updateLanguage()
    }

    private fun setupUI() {
        layout = BorderLayout(10, 10)
        border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        // Main button panel
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        buttonPanel.add(scanAndCleanButton)
        buttonPanel.add(progressBar)
        add(buttonPanel, BorderLayout.NORTH)

        // Status panel
        val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        statusPanel.add(statusLabel)
        add(statusPanel, BorderLayout.CENTER)

        // Results panel
        setupResultsPanel()
        add(resultPanel, BorderLayout.SOUTH)

        // Confetti panel (invisible initially)
        confettiPanel.isVisible = false
        add(confettiPanel, BorderLayout.CENTER)

        // Initial state
        progressBar.isVisible = false
        resultPanel.isVisible = false
    }

    private fun setupResultsPanel() {
        resultPanel.layout = BorderLayout(10, 10)
        resultPanel.border = BorderFactory.createTitledBorder(languageService.getString("results.title"))

        // Stats panel
        statsPanel.layout = GridLayout(1, 3, 10, 0)
        statsPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        val itemsPanel = createStatPanel(languageService.getString("results.items.cleaned"), itemsCleanedLabel)
        val spacePanel = createStatPanel(languageService.getString("results.space.freed"), spaceFreedLabel)
        val timePanel = createStatPanel(languageService.getString("results.time.taken"), timeTakenLabel)
        
        statsPanel.add(itemsPanel)
        statsPanel.add(spacePanel)
        statsPanel.add(timePanel)

        // Pie chart panel (initially empty)
        pieChartPanel.preferredSize = Dimension(300, 180)
        pieChartPanel.isVisible = false

        // Results text area
        resultsTextArea.isEditable = false
        resultsTextArea.font = Font("Monospaced", Font.PLAIN, 12)
        val scrollPane = JBScrollPane(resultsTextArea)
        scrollPane.preferredSize = Dimension(600, 200)

        resultPanel.add(statsPanel, BorderLayout.NORTH)
        resultPanel.add(pieChartPanel, BorderLayout.CENTER)
        resultPanel.add(scrollPane, BorderLayout.SOUTH)
    }

    private fun createStatPanel(title: String, valueLabel: JLabel): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        
        val titleLabel = JLabel(title)
        titleLabel.font = Font("Arial", Font.BOLD, 12)
        titleLabel.alignmentX = Component.CENTER_ALIGNMENT
        
        valueLabel.font = Font("Arial", Font.BOLD, 18)
        valueLabel.foreground = Color(0, 150, 0)
        valueLabel.alignmentX = Component.CENTER_ALIGNMENT
        
        panel.add(titleLabel)
        panel.add(Box.createVerticalStrut(5))
        panel.add(valueLabel)
        
        return panel
    }

    private fun setupListeners() {
        scanAndCleanButton.addActionListener {
            performScanAndClean()
        }
    }

    private fun setupStyling() {
        // Modern button styling
        scanAndCleanButton.font = Font("Arial", Font.BOLD, 16)
        scanAndCleanButton.preferredSize = Dimension(200, 50)
        scanAndCleanButton.background = Color(52, 152, 219)
        scanAndCleanButton.foreground = Color.WHITE
        scanAndCleanButton.isFocusPainted = false
        scanAndCleanButton.border = BorderFactory.createEmptyBorder(10, 20, 10, 20)

        // Progress bar styling
        progressBar.preferredSize = Dimension(300, 30)
        progressBar.font = Font("Arial", Font.BOLD, 12)

        // Status label styling
        statusLabel.font = Font("Arial", Font.PLAIN, 14)
        statusLabel.foreground = Color(52, 73, 94)
    }

    private fun updateLanguage() {
        scanAndCleanButton.text = languageService.getString("main.scan.and.clean.button")
        statusLabel.text = languageService.getString("progress.ready")
        
        // Update results panel title
        (resultPanel.border as? TitledBorder)?.title = languageService.getString("results.title")
        
        // Update stat panel titles
        updateStatPanelTitles()
    }

    private fun updateStatPanelTitles() {
        val itemsPanel = statsPanel.getComponent(0) as JPanel
        val spacePanel = statsPanel.getComponent(1) as JPanel
        val timePanel = statsPanel.getComponent(2) as JPanel
        
        (itemsPanel.getComponent(0) as JLabel).text = languageService.getString("results.items.cleaned")
        (spacePanel.getComponent(0) as JLabel).text = languageService.getString("results.space.freed")
        (timePanel.getComponent(0) as JLabel).text = languageService.getString("results.time.taken")
    }

    private fun performScanAndClean() {
        scanAndCleanButton.isEnabled = false
        progressBar.isVisible = true
        progressBar.isIndeterminate = false
        progressBar.minimum = 0
        progressBar.maximum = totalProgressSteps
        progressBar.value = 0
        resultPanel.isVisible = false
        confettiPanel.isVisible = false
        
        currentProgress = 0
        statusLabel.text = languageService.getString("progress.scanning")
        statusLabel.foreground = Color(52, 152, 219)

        // Start progress timer (30 seconds total)
        progressTimer = javax.swing.Timer(1000) { // Update every second
            currentProgress++
            progressBar.value = currentProgress
            
            // Update status messages during progress
            when (currentProgress) {
                in 1..5 -> statusLabel.text = languageService.getString("progress.scanning")
                in 6..15 -> statusLabel.text = languageService.getString("progress.analyzing")
                in 16..25 -> statusLabel.text = languageService.getString("progress.cleaning")
                else -> statusLabel.text = languageService.getString("progress.completed")
            }
            
            if (currentProgress >= totalProgressSteps) {
                progressTimer?.stop()
                performActualScanAndClean()
            }
        }
        progressTimer?.start()
    }

    private fun performActualScanAndClean() {
        SwingUtilities.invokeLater {
            try {
                // Step 1: Scan
                statusLabel.text = languageService.getString("progress.scanning")
                cleaningItems = cleaningService.scanForCleaningItems().toMutableList()
                
                // Filter based on selected categories
                val selectedCategories = settingsService.getSelectedCategories()
                if (selectedCategories.isNotEmpty()) {
                    cleaningItems = cleaningItems.filter { item ->
                        selectedCategories.contains(item.category.name)
                    }.toMutableList()
                }
                
                if (cleaningItems.isEmpty()) {
                    statusLabel.text = languageService.getString("progress.no.files")
                    statusLabel.foreground = Color(46, 204, 113)
                    return@invokeLater
                }

                // Step 2: Clean automatically (no confirmation)
                statusLabel.text = languageService.getString("progress.cleaning")
                statusLabel.foreground = Color(230, 126, 34)
                
                lastCleaningResult = cleaningService.cleanItems(cleaningItems)
                
                // Update statistics
                settingsService.setLastScanDate(Date().time)
                showCleaningResultDialog(lastCleaningResult!!)

                // Show completion message
                statusLabel.text = languageService.getString("progress.completed")
                statusLabel.foreground = Color(46, 204, 113)
                
                // Show confetti
                confettiPanel.startConfetti()
                
                // Bildirim (notification) gösterimi
                try {
                    val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Android Studio Cleaner Notifications")
                    if (notificationGroup != null) {
                        val cleaned = lastCleaningResult!!.cleanedItems
                        val freed = formatSize(lastCleaningResult!!.freedSpace)
                        val message = "🎉 Cleaning completed! $cleaned items cleaned, $freed freed."
                        val notification = notificationGroup.createNotification(
                            languageService.getString("main.title"),
                            message,
                            NotificationType.INFORMATION
                        )
                        Notifications.Bus.notify(notification, project)
                    } else {
                        println("[Android Studio Cleaner] Notification group not found!")
                    }
                } catch (ex: Exception) {
                    println("[Android Studio Cleaner] Notification error: ${ex.message}")
                }

            } catch (e: Exception) {
                logService.addLog(LogLevel.ERROR, "Error during scan and clean: ${e.message}", "MainPanel")
                statusLabel.text = languageService.getString("progress.error")
                statusLabel.foreground = Color(231, 76, 60)
                
                // Show error notification
                val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Android Studio Cleaner")
                val notification = notificationGroup.createNotification(
                    languageService.getString("notification.error.title"),
                    e.message ?: languageService.getString("notification.error.content"),
                    NotificationType.ERROR
                )
                Notifications.Bus.notify(notification, project)
                
            } finally {
                scanAndCleanButton.isEnabled = true
                progressBar.isVisible = false
            }
        }
    }

    private fun showCleaningResultDialog(result: CleaningResult) {
        val dialog = JDialog()
        dialog.title = languageService.getString("results.title")
        dialog.isModal = true
        dialog.layout = BorderLayout(10, 10)
        dialog.size = Dimension(800, 600)

        // Main panel with GridBagLayout for better control
        val mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.insets = Insets(5, 5, 5, 5)
        gbc.fill = GridBagConstraints.BOTH
        gbc.weightx = 1.0
        gbc.weighty = 1.0

        // Stats Panel
        val statsPanel = JPanel(GridLayout(1, 3, 10, 0))
        statsPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        val itemsCleanedLabel = JLabel("${result.cleanedItems}")
        val spaceFreedLabel = JLabel(formatSize(result.freedSpace))
        val timeTakenLabel = JLabel("${result.duration / 1000} seconds")
        
        val itemsPanel = createStatPanel(languageService.getString("results.items.cleaned"), itemsCleanedLabel)
        val spacePanel = createStatPanel(languageService.getString("results.space.freed"), spaceFreedLabel)
        val timePanel = createStatPanel(languageService.getString("results.time.taken"), timeTakenLabel)
        
        statsPanel.add(itemsPanel)
        statsPanel.add(spacePanel)
        statsPanel.add(timePanel)
        
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 1
        gbc.weighty = 0.0
        mainPanel.add(statsPanel, gbc)

        // Pie Chart + Legend Panel
        val pieAndLegendPanel = JPanel()
        pieAndLegendPanel.layout = BoxLayout(pieAndLegendPanel, BoxLayout.Y_AXIS)
        pieAndLegendPanel.alignmentX = Component.CENTER_ALIGNMENT
        pieAndLegendPanel.preferredSize = Dimension(400, 320)

        // Pie chart
        pieChartPanel.preferredSize = Dimension(300, 200)
        pieChartPanel.maximumSize = Dimension(300, 200)
        pieChartPanel.minimumSize = Dimension(300, 200)
        val categorySizes = cleaningItems.groupBy { it.category.displayName }
            .mapValues { entry -> entry.value.sumOf { it.size } }
        pieChartPanel.setData(categorySizes)
        pieAndLegendPanel.add(Box.createVerticalStrut(10))
        pieAndLegendPanel.add(pieChartPanel)
        pieAndLegendPanel.add(Box.createVerticalStrut(10))

        // Custom Legend Panel
        val legendPanel = JPanel()
        legendPanel.layout = BoxLayout(legendPanel, BoxLayout.Y_AXIS)
        legendPanel.alignmentX = Component.CENTER_ALIGNMENT
        legendPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        var colorIdx = 0
        categorySizes.forEach { (category, size) ->
            val color = pieChartPanel.getColor(colorIdx)
            val count = cleaningItems.count { it.category.displayName == category }
            val legendRow = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
            val colorBox = JPanel()
            colorBox.background = color
            colorBox.preferredSize = Dimension(18, 18)
            colorBox.border = BorderFactory.createLineBorder(Color.DARK_GRAY, 1)
            legendRow.add(colorBox)
            val label = JLabel("$category: $count files / ${formatSize(size)}")
            label.font = Font("Arial", Font.PLAIN, 13)
            legendRow.add(label)
            legendPanel.add(legendRow)
            colorIdx++
        }
        pieAndLegendPanel.add(legendPanel)
        pieAndLegendPanel.add(Box.createVerticalStrut(10))

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weighty = 0.4
        mainPanel.add(pieAndLegendPanel, gbc)

        // Results Text Area
        val resultsTextArea = JTextArea()
        resultsTextArea.isEditable = false
        resultsTextArea.font = Font("Monospaced", Font.PLAIN, 12)

        val sb = StringBuilder()
        sb.append("=== ${languageService.getString("results.summary.title")} ===\n")
        categorySizes.forEach { (category, size) ->
            sb.append("- ${category}: ${cleaningItems.count { it.category.displayName == category }} files / ${formatSize(size)}\n")
        }
        sb.append("\n=== ${languageService.getString("results.deleted.files.title")} ===\n")
        cleaningItems.forEach { file ->
            sb.append("- ${file.path} (${formatSize(file.size)})\n")
        }
        resultsTextArea.text = sb.toString()
        resultsTextArea.caretPosition = 0 // Scroll to top

        val scrollPane = JBScrollPane(resultsTextArea)
        scrollPane.preferredSize = Dimension(780, 250)
        
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.weighty = 0.6
        mainPanel.add(scrollPane, gbc)

        // OK Button
        val okButton = JButton("OK")
        okButton.addActionListener { dialog.dispose() }
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        buttonPanel.add(okButton)

        dialog.add(mainPanel, BorderLayout.CENTER)
        dialog.add(buttonPanel, BorderLayout.SOUTH)

        dialog.setLocationRelativeTo(null)
        dialog.isVisible = true
    }

    private fun formatSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", sizeInBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    fun updateTheme() {
        SwingUtilities.invokeLater {
            // Update UI components based on the new theme
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(800, 600)
    }
}

// Confetti Animation Panel
class ConfettiPanel : JPanel() {
    private val confetti = mutableListOf<ConfettiPiece>()
    private val animationTimer = javax.swing.Timer(50) { repaint() }
    private val random = Random()

    init {
        isOpaque = false
                }

    fun startConfetti() {
        // Create confetti pieces
        confetti.clear()
        repeat(100) {
            confetti.add(ConfettiPiece(
                x = random.nextDouble() * width,
                y = -20.0,
                vx = (random.nextDouble() - 0.5) * 4,
                vy = random.nextDouble() * 3 + 2,
                color = Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                ),
                size = random.nextDouble() * 8 + 4
            ))
        }
        
        animationTimer.start()
        
        // Stop confetti after 3 seconds
        javax.swing.Timer(3000) {
            animationTimer.stop()
            isVisible = false
        }.start()
        }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Update and draw confetti
        confetti.forEach { piece ->
            piece.update()
            piece.draw(g2d)
        }
    }
}

data class ConfettiPiece(
    var x: Double,
    var y: Double,
    var vx: Double,
    var vy: Double,
    val color: Color,
    val size: Double
) {
    fun update() {
        x += vx
        y += vy
        vy += 0.1 // gravity
    }

    fun draw(g2d: Graphics2D) {
        g2d.color = color
        g2d.fill(Ellipse2D.Double(x, y, size, size))
    }
}

// Pie chart panel for category distribution
class PieChartPanel : JPanel() {
    private var data: Map<String, Long> = emptyMap()
    private val colors = listOf(
        Color(52, 152, 219), Color(46, 204, 113), Color(231, 76, 60), Color(241, 196, 15),
        Color(155, 89, 182), Color(230, 126, 34), Color(26, 188, 156), Color(127, 140, 141),
        Color(52, 73, 94), Color(39, 174, 96)
    )

    fun setData(newData: Map<String, Long>) {
        data = newData
    }

    fun getColor(index: Int): Color {
        return colors[index % colors.size]
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
            }
        }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (data.isEmpty()) return
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val total = data.values.sum().toDouble()
        if (total == 0.0) return
        val size = Math.min(width, height) - 40
        val x = (width - size) / 2
        val y = 10
        var startAngle = 0.0
        var colorIdx = 0
        data.entries.forEach { (_, value) ->
            val angle = value / total * 360.0
            g2.color = colors[colorIdx % colors.size]
            g2.fillArc(x, y, size, size, startAngle.toInt(), Math.round(angle).toInt())
            startAngle += angle
            colorIdx++
        }
        // Draw legend
        var legendY = y + size + 20
        colorIdx = 0
        val legendX = 20 // X position for legend
        data.entries.forEach { (label, value) ->
            g2.color = colors[colorIdx % colors.size]
            g2.fillRect(legendX, legendY, 16, 16)
            g2.color = UIManager.getColor("Label.foreground") // Use theme color
            g2.font = Font("Arial", Font.PLAIN, 12)
            g2.drawString("$label: ${formatSize(value)}", legendX + 22, legendY + 13)
            legendY += 20
            colorIdx++
        }
    }
} 