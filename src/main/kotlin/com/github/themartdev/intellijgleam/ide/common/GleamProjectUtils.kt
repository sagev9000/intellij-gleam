package com.github.themartdev.intellijgleam.ide.common

import com.github.themartdev.intellijgleam.ide.lsp.GleamServiceSettings
import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

object GleamProjectUtils {
    fun getSrcDir(project: Project): String? {
        val projectPath = project.basePath ?: return null
        return Path(projectPath).resolve("src").toString()
    }

    fun gleamCommand(workingDirectory: String, vararg args: String): Boolean {
        val gleamExe = GleamServiceSettings.getInstance().gleamPath
        return ProcessBuilder(gleamExe, *args)
            .directory(File(workingDirectory))
            .start()
            .waitFor(60, TimeUnit.SECONDS)
    }
}