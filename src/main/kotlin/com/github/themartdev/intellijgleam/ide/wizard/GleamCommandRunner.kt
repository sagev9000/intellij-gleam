package com.github.themartdev.intellijgleam.ide.wizard

import com.github.themartdev.intellijgleam.ide.common.GleamProjectUtils
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.util.progress.sleepCancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class GleamCommandRunner(private val scope: CoroutineScope) {
    companion object {
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