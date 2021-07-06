package com.xxz.utils

import com.intellij.openapi.ui.Messages

object DialogUtil {
    fun showInfo(info: String?) {
        Messages.showErrorDialog(info, "AutoAssistJson")
    }
}