package com.github.themartdev.intellijgleam.ide.wizard.java

import com.github.themartdev.intellijgleam.GleamBundle
import com.github.themartdev.intellijgleam.ide.wizard.GleamTemplates
import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.ui.dsl.builder.Panel

class NewGleamProjectTargetStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
    val templateProperty: GraphProperty<String> = propertyGraph.property(GleamTemplates.ERLANG.label)
    private var template: String by templateProperty

    fun template() = GleamTemplates.Companion.fromLabel(template)

    override fun setupUI(builder: Panel) {
        with(builder) {
            group(GleamBundle.message("gleam.wizard.template.label")) {
                row {
                    dropDownLink(
                        template,
                        GleamTemplates.entries.map { it.label }
                    ).apply {
                        onChanged { template = it.selectedItem }
                    }
                }
            }
        }
    }

}