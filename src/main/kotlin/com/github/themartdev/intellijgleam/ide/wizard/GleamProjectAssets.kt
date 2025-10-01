package com.github.themartdev.intellijgleam.ide.wizard

class GleamProjectAssets(val gleamCommands: List<List<String>>, val templates: List<Pair<String, String>>) {

    enum class GleamTemplates(gleamProjectAssets: (template: String) -> GleamProjectAssets) {
        ERLANG({ projectName ->
            GleamProjectAssets(
                gleamCommands = listOf(),
                templates = fileAssets(projectName)
            )
        }),
        JAVASCRIPT({ projectName ->
            GleamProjectAssets(
                gleamCommands = listOf(),
                templates = fileAssets(projectName)
            )
        }),
        LUSTRE_BASIC({ projectName ->
            GleamProjectAssets(
                gleamCommands = listOf(
                    listOf("add", "lustre"),
                    listOf("add", "--dev", "lustre_dev_tools")
                ),
                templates = fileAssets(projectName)
            )
        })
    }

    companion object {
        fun assetDirs(): List<String> = listOf("src")
        fun fileAssets(projectName: String): List<Pair<String, String>> = listOf(
            Pair("gleam.toml", "gleam.toml"),
            Pair(".gitignore", "gleam.gitignore"),
            Pair("src/${projectName}.gleam", "main.gleam")
        )

        fun assetProps(name: String, target: String): Array<Pair<String, String>> =
            arrayOf(
                Pair("gleamProjectName", name),
                Pair("gleamProjectTarget", target),
            )
    }
}