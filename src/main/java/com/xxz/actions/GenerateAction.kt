package com.xxz.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.xxz.utils.*

class GenerateAction : GenerateJson() {
    override fun actionPerformed(e: AnActionEvent) {
        isRunGenerate = true
        super.actionPerformed(e)
    }
}