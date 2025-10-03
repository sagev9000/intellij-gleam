package com.github.themartdev.intellijgleam.ide.ui.components

import com.github.themartdev.intellijgleam.ide.common.captureErlang
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import kotlin.io.path.Path

class ErlangPathComboBox() : AbstractExecutablePathComboBox() {
    override fun computeVersionInline(path: String): String? {
        val executable = captureErlang(Path(path))
        return executable?.version
    }

    override fun showBrowseDialog() {
        val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        fileChooserDescriptor.title = "Select Erlang SDK"
        val selectedFiles = FileChooser.chooseFiles(fileChooserDescriptor, null, null)
        if (selectedFiles.isNotEmpty()) {
            val path = selectedFiles[0].path
            selectedPath = path
        }
    }
}
