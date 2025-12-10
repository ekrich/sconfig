package org.ekrich.config.impl
import org.ekrich.config.{
  ConfigFactory,
  ConfigFormatOptions,
  ConfigParseOptions,
  ConfigRenderOptions
}

trait RenderingTestSuite extends TestUtilsShared {
  val parseOptions = ConfigParseOptions.defaults.setAllowMissing(true)
  val myDefaultRenderOptions = ConfigRenderOptions.defaults
    .setJson(false)
    .setOriginComments(false)
    .setComments(true)
    .setFormatted(true)

  def formatHocon(
      str: String
  )(implicit configFormatOptions: ConfigFormatOptions): String =
    ConfigFactory
      .parseString(str, parseOptions)
      .root
      .render(
        myDefaultRenderOptions.setConfigFormatOptions(configFormatOptions)
      )

  def checkEqualsAndStable(expected: String, result: String)(implicit
      configFormatOptions: ConfigFormatOptions
  ) = {
    checkEqualObjects(expected, result)
    checkEqualObjects(result, formatHocon(result)(configFormatOptions))
  }
}
