package com.github.themartdev.intellijgleam.ide.wizard

import com.github.themartdev.intellijgleam.GleamBundle

enum class GleamTemplates(
    val label: String,
    val gleamProjectAssets: (template: String) -> GleamProjectAssets
) {
    ERLANG(GleamBundle.message("gleam.wizard.template.erlang.name"), { projectName ->
        GleamProjectAssets(
            target = "",
            gleamCommands = listOf(),
            templates = listOf(toml(), gitignore(), helloWorld(projectName)),
        )
    }),
    JAVASCRIPT(GleamBundle.message("gleam.wizard.template.javascript.name"), { projectName ->
        GleamProjectAssets(
            target = JS_TARGET,
            gleamCommands = listOf(),
            templates = listOf(toml(), gitignore(), helloWorld(projectName)),
        )
    }),
    LUSTRE_BASIC(GleamBundle.message("gleam.wizard.template.lustre.simple.name"), { projectName ->
        GleamProjectAssets(
            target = JS_TARGET,
            gleamCommands = listOf(
                arrayOf("add", "lustre"),
                arrayOf("add", "--dev", "lustre_dev_tools")
            ),
            templates = listOf(
                toml(),
                gitignore(),
                lustreReadme(),
                Pair("src/${projectName}.gleam", "lustre.simple.main.gleam"),
            )
        )
    });

    companion object {
        fun fromLabel(label: String): GleamTemplates {
            return entries.first { it.label == label }
        }
        private const val JS_TARGET = "target = \"javascript\"\n"

        fun toml() = Pair("gleam.toml", "gleam.toml")
        fun gitignore() = Pair(".gitignore", "gleam.gitignore")

        fun lustreReadme() = Pair("README.md", "lustre.README.md")

        fun helloWorld(projectName: String) =
            Pair("src/${projectName}.gleam", "main.gleam")

    }
}