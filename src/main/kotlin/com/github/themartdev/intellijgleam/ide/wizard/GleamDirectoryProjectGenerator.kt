package com.github.themartdev.intellijgleam.ide.wizard

import com.github.themartdev.intellijgleam.GleamBundle
import com.github.themartdev.intellijgleam.GleamIcons
import com.github.themartdev.intellijgleam.ide.common.GleamProjectUtils
import com.github.themartdev.intellijgleam.ide.lsp.GleamServiceSettings
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectGeneratorBase
import com.intellij.platform.GeneratorPeerImpl
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.psi.PsiManager
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JPanel

class GleamDirectoryProjectGenerator : DirectoryProjectGeneratorBase<GleamDirectoryProjectGenerator.GleamGeneratorSettings>() {

    class GleamGeneratorSettings(val gleamPathIsKnown: Boolean, var template: GleamTemplates)

    override fun getName(): @NlsContexts.Label String = GleamBundle.message("gleam.wizard.directory.project.generator.name")

    override fun getLogo(): Icon = GleamIcons.GLEAM

    fun boxWith(layout: Int, vararg children: Component) = Box(layout).apply {
        children.forEach { child -> this.add(child) }
    }

    fun row(vararg children: Component) = boxWith(BoxLayout.X_AXIS, *children)
    fun col(vararg children: Component) = boxWith(BoxLayout.Y_AXIS, *children)

    override fun createPeer(): ProjectGeneratorPeer<GleamGeneratorSettings> {
        val settings = GleamGeneratorSettings(
            template = GleamTemplates.ERLANG,
            gleamPathIsKnown = GleamServiceSettings.getInstance().gleamPath.isNotBlank(),
        )

        val warningLabel = JLabel()
        fun updateWarningLabel() {
            warningLabel.isVisible = !settings.gleamPathIsKnown && settings.template.requiresGleamExe()
        }

        val component = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(row(
                JLabel(GleamBundle.message("gleam.wizard.template.label")),

                col(
                    ComboBox<String>().apply {
                        GleamTemplates.entries.forEach { addItem(it.label) }
                        addItemListener {
                            settings.template = GleamTemplates.fromLabel(this.selectedItem as String)
                            updateWarningLabel()
                        }
                    }
                )
            ))
            add(row(
                add(warningLabel.apply {
                    text = GleamBundle.message("gleam.wizard.template.no.gleam.error")
                    foreground = Color.ORANGE
                    isVisible = true
                })
            ))
        }
        updateWarningLabel()

        return GeneratorPeerImpl(settings, component)
    }

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: GleamGeneratorSettings,
        module: Module
    ) {
        ApplicationManager.getApplication().invokeLater {
            runWriteAction {
                val psiBaseDir = PsiManager.getInstance(project).findDirectory(baseDir) ?: return@runWriteAction
                val templateManager = FileTemplateManager.getInstance(project)

                val projectTemplate = settings.template
                val templateAssets = projectTemplate.gleamProjectAssets(project.name)

                val properties = GleamProjectAssets.assetProps(project.name, templateAssets.target)

                templateAssets.templates.forEach { (sourcePath, templateName) ->
                    val pathParts = sourcePath.split("/").toMutableList()
                    val targetFile = pathParts.removeLast()
                    var dir = psiBaseDir
                    pathParts.forEach { dir = dir.createSubdirectory(it) }
                    val template = templateManager.getInternalTemplate(templateName)
                    FileTemplateUtil.createFromTemplate(template, targetFile, properties, dir, null)
                }

                val workingDirectory = psiBaseDir.virtualFile.path
                templateAssets.gleamCommands.forEach { args ->
                    GleamProjectUtils.gleamCommand(workingDirectory, *args)
                }
            }
        }
    }
}