package com.xxz.utils

import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.plugins.terminal.TerminalView
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import java.io.IOException
import java.lang.Exception

object CommandUtil {
    fun runFlutterPubRun(e: AnActionEvent, filePath: String?) {
        val terminalName = "AutoAssistJson"
        val workingDirectory = e.project!!.basePath
        val command = "flutter pub run build_runner build --delete-conflicting-outputs"
        val terminalView = TerminalView.getInstance(e.project!!)
        val window = ToolWindowManager.getInstance(e.project!!)
                .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)
        if (window == null) {
            DialogUtil.showInfo("Please check that the following two plugins are installed: Terminal and Shell Script")
            return
        }
        try {
            val content = window.contentManager.findContent(terminalName)
            if (content != null) {
                terminalView.closeTab(content)
            }
            val localTerminal = window.contentManager.findContent("Local")
            if (localTerminal != null) {
                terminalView.closeTab(content!!)
            }
        } catch (exception: Exception) {
        }
        try {
            var workPath = filePath
            if (workPath == null || workPath.isEmpty()) {
                workPath = workingDirectory
            }
            val terminalWidget = terminalView.createLocalShellWidget(workPath, terminalName)
            terminalWidget.executeCommand(command)
        } catch (exception: IOException) {
            DialogUtil.showInfo("Cannot run command:" + command + "  " + exception.message)
        }
    }
}