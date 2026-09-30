package org.ekrich.config.impl
import org.ekrich.config.{
  ConfigException,
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
    checkEqualObjects(result, formatHocon(result))
  }

  // rendered text has to be readable back, or a round trip loses the config
  def checkReparses(result: String)(implicit
      configFormatOptions: ConfigFormatOptions
  ) =
    try formatHocon(result)
    catch {
      case e: ConfigException.Parse =>
        throw new AssertionError(
          s"rendered text does not parse back: ${e.getMessage}\n$result",
          e
        )
    }
}
