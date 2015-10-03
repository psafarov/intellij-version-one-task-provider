package com.psafarov.intellij.versionone

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.parsing.json.{JSONArray, JSONObject}

class Query {

  val asset = "PrimaryWorkitem"

  val fields = List(
      "AssetType",
      "ID.Number",
      "Name",
      "CreateDate",
      "ChangeDate",
      "Description",
      "IsClosed"
  )

  var where = mutable.Map[String, Any]()
  var find = ""
  var findIn = new ListBuffer[String]
  var sort = new ListBuffer[String]

  var limit = 1
  var offset = 0

  override def toString = {
    val map = mutable.Map[String, Any]()
    map("from") = asset
    map("select") = new JSONArray(fields)
    if (where.nonEmpty) map("where") = new JSONObject(where.toMap)
    if (sort.nonEmpty) map("sort") = new JSONArray(sort.toList)
    if (find.nonEmpty) map("find") = find
    if (findIn.nonEmpty) map("findIn") = new JSONArray(findIn.toList)
    map("page") = new JSONObject(Map("start" -> offset, "size" -> limit))

    JSONObject(map.toMap).toString()
  }

}
