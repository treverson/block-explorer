package com.xsn.explorer.helpers

import java.io.File

import com.xsn.explorer.models.rpc.Block
import play.api.libs.json.{JsValue, Json}

object BlockLoader {

  private val BasePath = "blocks"

  def get(blockhash: String): Block = {
    json(blockhash).as[Block]
  }

  def json(blockhash: String): JsValue = {
    try {
      val resource = s"$BasePath/$blockhash"
      val json = scala.io.Source.fromResource(resource).getLines().mkString("\n")
      Json.parse(json)
    } catch {
      case _ => throw new RuntimeException(s"Block $blockhash not found")
    }
  }

  def all(): List[Block] = {
    val uri = getClass.getResource(s"/$BasePath")
    new File(uri.getPath)
        .listFiles()
        .toList
        .map(_.getName)
        .map(get)
  }
}
