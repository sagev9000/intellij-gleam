package com.github.themartdev.intellijgleam.ide.wizard

import com.github.themartdev.intellijgleam.ide.common.GleamProjectUtils
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.util.progress.sleepCancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class GleamCommandRunner(private val scope: CoroutineScope) {
    companion object {
        fun runCommand(vararg args: String) {
            val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
            val projectDir = project.guessProjectDir() ?: return

            project.getService(GleamCommandRunner::class.java).scope.launch {
                withBackgroundProgress(project, "Gleam project setup") {
                    GleamProjectUtils.gleamCommand(projectDir.path, *args)
                }
            }
        }

        fun runCommands(project: Project, directory: String, assets: GleamProjectAssets) {
            project.getService(GleamCommandRunner::class.java).scope.launch {
                withBackgroundProgress(project, "Gleam project setup") {
                    assets.gleamCommands.forEach { args ->
                        sleepCancellable(3000)
                        GleamProjectUtils.gleamCommand(directory, *args)
                    }
                }
            }
        }
    }
}