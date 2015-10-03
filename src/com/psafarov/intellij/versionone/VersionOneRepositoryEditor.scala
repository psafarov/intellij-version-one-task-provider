package com.psafarov.intellij.versionone

import javax.swing.{JComponent, SwingConstants}

import collection.JavaConversions._
import com.intellij.openapi.ui.ComboBox
import com.intellij.tasks.config.BaseRepositoryEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.components.{JBTextField, JBLabel}
import com.intellij.util.Consumer
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.combobox.ListComboBoxModel

class VersionOneRepositoryEditor(repository: VersionOneRepository, project: Project,
  changeListener: Consumer[VersionOneRepository]) extends BaseRepositoryEditor(project, repository, changeListener) {

  var myScopeLabel: JBLabel = _
  var myScopeInput: ComboBox = _
  installListener(myScopeInput)

  var myTeamLabel: JBLabel = _
  var myTeamInput: JBTextField = _
  installListener(myTeamInput)

  override def createCustomPanel = {
    myScopeLabel = new JBLabel("Scope:", SwingConstants.RIGHT)
    val myScopeModel = new ListComboBoxModel(Scope.values.toList)
    myScopeModel.setSelectedItem(repository.getScope)
    myScopeInput = new ComboBox(myScopeModel)

    myTeamLabel = new JBLabel("Team:", SwingConstants.RIGHT)
    myTeamInput = new JBTextField(repository.getTeam)
    switchTeamInput()

    FormBuilder.createFormBuilder
      .addLabeledComponent(myScopeLabel, myScopeInput)
      .addLabeledComponent(myTeamLabel, myTeamInput)
      .setVertical(false)
      .getPanel
  }

  override def apply() = {
    super.apply()
    repository.setScope(myScopeInput.getSelectedItem.toString)
    repository.setTeam(myTeamInput.getText)
    switchTeamInput()
  }

  protected def switchTeamInput() =
    myTeamInput.setEnabled(myScopeInput.getSelectedItem.toString == Scope.TEAM.toString)

  override def setAnchor(anchor: JComponent) = {
    super.setAnchor(anchor)
    myScopeLabel.setAnchor(anchor)
    myTeamLabel.setAnchor(anchor)
  }

}
