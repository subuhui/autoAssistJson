package com.xxz.actions

import com.intellij.openapi.actionSystem.AnActionEvent

class RunCommand : GenerateJson() {
    override fun actionPerformed(e: AnActionEvent) {
        isRunCommand = true
        super.actionPerformed(e)
    }
}