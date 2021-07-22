package com.xxz.actions

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.lang.dart.DartTokenTypes
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartVarAccessDeclaration
import com.xxz.utils.DialogUtil
import com.xxz.utils.PubRunUtils
import io.flutter.pub.PubRoot
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream


open class GenerateJson : AnAction() {
    private var fileName: String? = ""
    private var mIndex = -1
    protected var isRunCommand = false
    protected var isRunGenerate = false
    override fun actionPerformed(e: AnActionEvent) {
        if (!isExistJsonSerializable(e)) {
            DialogUtil.showInfo("The project needs to depend on json_serializable,build_runner")
            return
        }
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)
        fileName = file?.name?.replace(".dart", "")
        assert(editor != null)
        val templateManager = TemplateManager.getInstance(e.project)
        if (file != null && file.name.contains(".dart")) {
            if (isRunGenerate) {
                val dartFile = file as DartFile
                dartFile.children.forEachIndexed { index, classChild ->
                    if (classChild.elementType == DartTokenTypes.CLASS_DEFINITION) {
                        editor?.caretModel?.moveToOffset(classChild.startOffset)
                        val template = templateManager.createTemplate(this.javaClass.name, "Dart")
                        template.isToReformat = true
                        if (mIndex == -1) {
                            mIndex = index
                            if (!dartFile.text.contains("import 'package:json_annotation/json_annotation.dart';")) {
                                template.addTextSegment("import 'package:json_annotation/json_annotation.dart';\n")
                            }
                            if (!dartFile.text.contains("part '$fileName.g.dart';")) {
                                template.addTextSegment("part '$fileName.g.dart';\n")
                            }
                        }
                        if (!classChild.text.contains("@JsonSerializable()")) {
                            template.addTextSegment("@JsonSerializable()\n")
                        }
                        editor?.let { templateManager.startTemplate(it, template) }
                        val className = (classChild as DartClassDefinition).componentName.name
                        for (bodyChild in classChild.children) {
                            if (bodyChild.elementType == DartTokenTypes.CLASS_BODY) {
                                editor?.caretModel?.moveToOffset(bodyChild.lastChild.startOffset)
                                val methodTemplate = templateManager.createTemplate(this.javaClass.name, "Dart")
                                methodTemplate.isToReformat = true
                                if (isRunCommand &&
                                    !bodyChild.text.contains("$className(this.") &&
                                    !bodyChild.text.contains("$className({")) {
                                    val fields = arrayListOf<String>();
                                    for (member in bodyChild.children) {
                                        if (member.elementType == DartTokenTypes.CLASS_MEMBERS) {
                                            for (varList in member.children) {
                                                if (varList.elementType == DartTokenTypes.VAR_DECLARATION_LIST) {
                                                    for (varAccess in varList.children) {
                                                        if (varAccess.elementType == DartTokenTypes.VAR_ACCESS_DECLARATION) {
                                                            fields.add("this.${(varAccess as DartVarAccessDeclaration).name.toString()}")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    val constructorText = "$className(${fields.joinToString(separator = ", ")});"
                                    methodTemplate.addTextSegment("$constructorText\n")
                                }
                                val fromJsonText =
                                    "factory $className.fromJson(Map<String, dynamic> json) => _${"$"}${className}FromJson(json);"
                                val toJsonText = "Map<String, dynamic> toJson() => _${"$"}${className}ToJson(this);"

                                if (!bodyChild.text.contains("$className.fromJson")) {
                                    methodTemplate.addTextSegment("$fromJsonText\n")
                                }
                                if (!bodyChild.text.contains("toJson")) {
                                    methodTemplate.addTextSegment("$toJsonText\n")
                                }
                                editor?.let { templateManager.startTemplate(it, methodTemplate) }
                            }
                        }
                        editor?.let { templateManager.finishTemplate(it) }
                    }
                }
            }
            if (isRunCommand) {
//                CommandUtil.runFlutterPubRun(
//                    e,
//                    PubRoot.forFile(e.project?.projectFile ?: e.project?.workspaceFile)?.path
//                )
                PubRunUtils.runFlutterPubRun(e)
            }
        } else {
            DialogUtil.showInfo("AutoAssistJson: Can not find any Class.")
        }
    }

    private fun isExistJsonSerializable(e: AnActionEvent): Boolean {
        var isExist = false
        PubRoot.forFile(e.project?.projectFile ?: e.project?.workspaceFile)?.let { pubRoot ->
            FileInputStream(pubRoot.pubspec.path).use { inputStream ->
                (Yaml().load(inputStream) as? Map<String, Any>)?.let { map ->
                    val dependencies = map["dev_dependencies"];
                    if (dependencies is Map<*, *>) {
                        isExist = dependencies["json_serializable"] != null && dependencies["build_runner"] != null
                    }
                }
            }
        }
        return isExist
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = false
        val project = e.project
        if (project != null) {
            val file = e.getData(CommonDataKeys.PSI_FILE)
            if (file != null) {
                val fileName = file.name
                if (fileName.contains(".dart")) {
                    e.presentation.isEnabled = true
                }
            }
        }
    }
}