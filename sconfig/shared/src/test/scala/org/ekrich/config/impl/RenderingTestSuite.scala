package org.ekrich.config.impl
import org.ekrich.config.{
  ConfigException,
  ConfigFactory,
  ConfigFormatOptions,
  ConfigParseOptions,
  ConfigRenderOptions
}
import scala.annotation.nowarn

trait RenderingTestSuite extends TestUtilsShared {
  val parseOptions = ConfigParseOptions.defaults.setAllowMissing(true)
  val myDefaultRenderOptions = ConfigRenderOptions.defaults
    .setJson(false)
    .setOriginComments(false)
    .setComments(true)
    .setFormatted(true)

  @nowarn("cat=deprecation")
  def renderHocon(
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
    checkEqualObjects(result, renderHocon(result))
  }

  // rendered text has to be readable back, or a round trip loses the config
  def checkReparses(result: String)(implicit
      configFormatOptions: ConfigFormatOptions
  ) =
    try renderHocon(result)
    catch {
      case e: ConfigException.Parse =>
        throw new AssertionError(
          s"rendered text does not parse back: ${e.getMessage}\n$result",
          e
        )
    }
}
