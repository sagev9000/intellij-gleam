package com.github.themartdev.intellijgleam.ide.wizard

import com.github.themartdev.intellijgleam.ide.lsp.GleamServiceSettings
import com.intellij.ide.projectWizard.generators.AssetsNewProjectWizardStep
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.StartupManager
import java.io.File
import java.util.concurrent.TimeUnit

class NewGleamProjectAssetsStep(private val parent: NewGleamProjectTargetStep) : AssetsNewProjectWizardStep(parent) {

    override fun setupAssets(project: Project) {
        val outputDirectory = project.guessProjectDir()?.path!!
        setOutputDirectory(outputDirectory = outputDirectory)
        val props = GleamProjectAssets.assetProps(project.name, parent.target)

        if (context.isCreatingNewProject) {
            GleamProjectAssets.assetDirs().forEach { dir -> addEmptyDirectoryAsset(dir) }
            GleamProjectAssets.fileAssets(project.name).forEach { (sourcePath, templateName) -> addTemplateAsset(sourcePath, templateName, *props) }
        }
        StartupManager.getInstance(project).runWhenProjectIsInitialized {
            ApplicationManager.getApplication().invokeLater {
                runWriteAction {
                    val gleamExe = GleamServiceSettings.getInstance().gleamPath;
                    val builder = ProcessBuilder(gleamExe, "add", "lustre")
                        .directory(File(outputDirectory));
                    builder.redirectErrorStream(true)
                    val log = File.createTempFile(gleamExe, "log")
                    builder.redirectOutput(ProcessBuilder.Redirect.appendTo(log))

                    val process = builder.start()
                    if (process.waitFor(60, TimeUnit.SECONDS)) {
                        val process = ProcessBuilder(gleamExe, "add", "--dev", "lustre_dev_tools")
                            .directory(File(outputDirectory))
                            .start()
                        process.waitFor(60, TimeUnit.SECONDS)
                    }
                }
            }
        }
    }
}