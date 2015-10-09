package com.psafarov.intellij.versionone

import javax.xml.datatype.DatatypeFactory

import scala.util.parsing.json._

import com.intellij.openapi.util.io.StreamUtil
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.tasks.TaskRepository.CancellableConnection
import com.intellij.tasks.impl.BaseRepositoryImpl
import com.intellij.tasks.{Comment, Task, TaskType}
import com.intellij.util.xmlb.annotations.Tag
import org.apache.commons.httpclient.methods.{PostMethod, StringRequestEntity}

@Tag("VersionOne")
class VersionOneRepository(repositoryType: VersionOneRepositoryType) extends BaseRepositoryImpl(repositoryType) {

  type taskMap = Map[String, String]

  myUseHttpAuthentication = true

  var myScope = Scope.ALL
  var myTeam = ""

  def this() = this(null)

  def getScope = myScope.toString
  def setScope(scope: String) = myScope = Scope.withName(scope)

  def getTeam = myTeam
  def setTeam(team: String) = myTeam = team

  override def clone() = {
    val cloned = new VersionOneRepository(getRepositoryType.asInstanceOf[VersionOneRepositoryType])
    cloned.setUrl(getUrl)
    cloned.setPassword(getPassword)
    cloned.setUsername(getUsername)
    cloned.setUseProxy(isUseProxy)
    cloned.myScope = myScope
    cloned.myTeam = myTeam
    cloned
  }

  def findTask(id: String): Task = {
    val query = new Query
    query.where("ID.Number") = id

    val json = sendQuery(query)

    createTask(getTaskMapList(json).head)
  }

  override def getIssues(keyword: String, offset: Int, limit: Int, withClosed: Boolean): Array[Task] = {
    val query = new Query
    query.sort += "-ID"
    query.offset = offset
    query.limit = limit

    if (keyword != null) {
      query.find = keyword
      query.findIn += "Name"
    }

    if (!withClosed) {
      query.where("IsClosed") = false
    }

    myScope match {
      case Scope.ALL =>
      case Scope.TEAM => query.where("Team.Name") = myTeam
      case Scope.USER => query.where("Owners.Nickname") = getUsername
    }

    val json = sendQuery(query)

    getTaskMapList(json).map(createTask).toArray
  }

  protected def getTaskMapList(value: Option[Any]) = value match {
    case Some(List(list: List[this.taskMap])) => list
    case _ => throw new Exception("Server returned invalid response")
  }

  protected def createTask(map: taskMap) = new Task {
    override def getType        = getTaskType(map("AssetType"))
    override def getSummary     = map("Name")
    override def isIssue        = true
    override def getIcon        = getRepositoryType.getIcon
    override def getId          = map("ID.Number")
    override def getDescription = map("Description")
    override def getComments    = Comment.EMPTY_ARRAY
    override def getIssueUrl    = s"$getUrl/${map("AssetType")}.mvc/Summary?oidToken=${map("_oid")}"
    override def getCreated     = parseDate(map("CreateDate"))
    override def getUpdated     = parseDate(map("ChangeDate"))
    override def isClosed       = map("IsClosed").toBoolean
  }

  protected def getTaskType(assetType: String) = assetType match {
    case "Story"  => TaskType.FEATURE
    case "Defect" => TaskType.BUG
    case _        => TaskType.OTHER
  }

  protected def parseDate(date: String) =
    DatatypeFactory.newInstance.newXMLGregorianCalendar(date).toGregorianCalendar.getTime

  protected def createRequest(requestBody: String = "") = {
    val method = new PostMethod(getUrl + "/query.v1")
    val requestEntity = new StringRequestEntity(requestBody, "text/json", "utf8")
    method.setRequestEntity(requestEntity)
    method
  }

  protected def sendRequest(request: PostMethod) = getHttpClient.executeMethod(request) match {
    case 200 =>
    case 400 => throw new Exception("Query is incorrect")
    case 401 => throw new Exception("Your credentials are incorrect")
    case x   => throw new Exception("Unexpected HTTP response code: " + x)
  }

  protected def sendQuery(query: Query) = {
    val request = createRequest(query.toString)
    sendRequest(request)
    val responseBody = StreamUtil.readText(request.getResponseBodyAsStream, CharsetToolkit.UTF8_CHARSET)
    request.releaseConnection()
    JSON.parseFull(responseBody)
  }

  override def createCancellableConnection = new CancellableConnection {

    val method = createRequest((new Query).toString)

    override def doTest() = sendRequest(method)

    override def cancel() = method.abort()

  }

}
