package com.github.themartdev.intellijgleam.ide.wizard.java

import com.github.themartdev.intellijgleam.ide.common.GleamProjectUtils
import com.github.themartdev.intellijgleam.ide.wizard.GleamProjectAssets
import com.intellij.ide.projectWizard.generators.AssetsNewProjectWizardStep
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir

@Suppress("UnstableApiUsage")
class NewGleamProjectAssetsStep(private val parent: NewGleamProjectTargetStep) : AssetsNewProjectWizardStep(parent) {

    override fun setupAssets(project: Project) {
        val outputDirectory = project.guessProjectDir()?.path!!
        setOutputDirectory(outputDirectory = outputDirectory)

        val templateAssets = parent.template().gleamProjectAssets(project.name)

        val props = GleamProjectAssets.Companion.assetProps(project.name, templateAssets.target)
        GleamProjectAssets.Companion.assetDirs().forEach { dir -> addEmptyDirectoryAsset(dir) }
        templateAssets.templates.forEach { (sourcePath, templateName) -> addTemplateAsset(sourcePath, templateName, props) }

        ApplicationManager.getApplication().invokeLater {
            runWriteAction {
                templateAssets.gleamCommands.forEach { args ->
                    GleamProjectUtils.gleamCommand(outputDirectory, *args)
                }
            }
        }
    }
}