package org.ekrich.config.impl

import java.io.{BufferedReader, FileReader}

import org.ekrich.config.parser._
import org.junit.Assert._
import org.junit.Test

import FileUtils._

class ConfigDocumentFactoryTest extends TestUtils {

  @Test
  def configDocumentFileParse: Unit = {
    val configDocument =
      ConfigDocumentFactory.parseFile(resourceFile("/test03.conf"))
    val fileReader = new BufferedReader(
      new FileReader(resourceFile("/test03.conf"))
    )
    var line = fileReader.readLine()
    val sb = new StringBuilder()
    while (line != null) {
      sb.append(line)
      sb.append("\n")
      line = fileReader.readLine()
    }
    fileReader.close()
    val fileText = sb.toString()
    assertEquals(fileText, defaultLineEndingsToUnix(configDocument.render))
  }

  private def defaultLineEndingsToUnix(s: String): String =
    s.replaceAll(System.lineSeparator(), "\n")

  @Test
  def configDocumentReaderParse: Unit = {
    val configDocument = ConfigDocumentFactory.parseReader(
      new FileReader(resourceFile("/test03.conf"))
    )
    val configDocumentFile =
      ConfigDocumentFactory.parseFile(resourceFile("/test03.conf"))
    assertEquals(configDocumentFile.render, configDocument.render)
  }
}
