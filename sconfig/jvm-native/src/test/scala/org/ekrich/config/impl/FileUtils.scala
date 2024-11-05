/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import java.io.File
import java.util.Locale

/**
 * Extracted from TestUtils as they contain File and Path and are not useful on
 * Scala.js
 *
 * Note: Some functions may only be used in this file so they could be made
 * private.
 */
object FileUtils {
  // Some functions duplicated in TestUtils

  def isWindows: Boolean =
    sys.props
      .get("os.name")
      .exists(_.toLowerCase(Locale.ROOT).contains("windows"))

  def userDrive: String =
    if (isWindows)
      sys.props.get("user.dir").fold("")(_.takeWhile(_ != File.separatorChar))
    else ""

  val resourceDir = {
    val f = TestPath.file()
    if (!f.exists()) {
      val here = new File(".").getAbsolutePath
      throw new Exception(
        s"""Tests must be run from the root project directory containing
           | ${f.getPath()}, however the current directory is
           | ${here}""".stripMargin
      )
    }
    f
  }

  // TODO: can't test if file exists because some tests require the absense
  // of the file. The problem is that later on in the framework, not test
  // is done so silent failures can occur
  def resourceFile(filename: String): File =
    new File(resourceDir, filename)

  def jsonQuotedResourceFile(filename: String): String =
    quoteJsonString(resourceFile(filename).toString)

  def quoteJsonString(s: String): String =
    ConfigImplUtil.renderJsonString(s)

  def writeFile(f: File, content: String): Unit = {
    val writer = new java.io.PrintWriter(f, "UTF-8")
    writer.append(content)
    writer.close()
  }

  def deleteRecursive(f: File): Unit = {
    if (f.exists) {
      if (f.isDirectory) {
        val children = f.listFiles()
        if (children ne null) {
          for (c <- children)
            deleteRecursive(c)
        }
      }
      f.delete()
    }
  }

  def withScratchDirectory[T](
      testcase: String
  )(body: File => T): Unit = {
    val target = new File("target")
    if (!target.isDirectory)
      throw new RuntimeException(s"Expecting $target to exist")
    val suffix = java.lang.Integer
      .toHexString(java.util.concurrent.ThreadLocalRandom.current.nextInt)
    val scratch = new File(target, s"$testcase-$suffix")
    scratch.mkdirs()
    try {
      body(scratch)
    } finally {
      deleteRecursive(scratch)
    }
  }
}
