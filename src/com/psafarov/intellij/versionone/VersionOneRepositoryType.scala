package com.psafarov.intellij.versionone

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.tasks.impl.BaseRepositoryType
import com.intellij.util.Consumer

class VersionOneRepositoryType extends BaseRepositoryType[VersionOneRepository] {

  override def getName = "VersionOne"

  override def getIcon = IconLoader.getIcon("/versionone.png")

  override def createRepository = new VersionOneRepository(this)

  override def getRepositoryClass = classOf[VersionOneRepository]

  override def createEditor(repository: VersionOneRepository,
                            project: Project,
                            changeListener: Consumer[VersionOneRepository]) =
    new VersionOneRepositoryEditor(repository, project, changeListener)

}
