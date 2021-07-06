package com.xxz.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.xxz.utils.*

class GenerateAndBuildAction : GenerateJson() {
    override fun actionPerformed(e: AnActionEvent) {
        isRunGenerate = true
        isRunCommand = true
        super.actionPerformed(e)
    }
}