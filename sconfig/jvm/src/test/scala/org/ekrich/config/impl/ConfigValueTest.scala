/**
 * Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.junit.Assert._
import org.junit._
import java.net.URL
import scala.jdk.CollectionConverters._
import org.ekrich.config.ConfigList

import FileUtils._

/**
 * Lack of URL on native and js preclude this test from working.
 *
 * The following is the test/code in question:
 * {{{
 * def configOriginFileAndLine()
 *
 * SimpleConfigOrigin line 34
 * url = new PlatformUri(uri).toURL().toExternalForm()
 * }}}
 * The test could conceiveable work on native with `File` and even deeper in the
 * API with a URL implementation.
 *
 * The serialization tests won't work on JS due to the lack of
 * `ObjectOutputStream` and others but maybe could work on Native.
 */
class ConfigValueTest extends TestUtils {

  @Test
  def configOriginNotSerializable(): Unit = {
    val a = SimpleConfigOrigin.newSimple("foo")
    checkNotSerializable(a)
  }

  @Test
  def configIntSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_902000000_-050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n090000000100010400000009020000002A0002_4_20103" +
      "000000010001_x"
    val a = intValue(42)
    val b = checkSerializable(expectedSerialization, a)
    assertEquals(42, b.unwrapped)
  }

  @Test
  def configLongSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_E02000000_9050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n090000000100010400000015030000000080000029000A" +
      "_2_1_4_7_4_8_3_6_8_90103000000010001_x"

    val a = longValue(Integer.MAX_VALUE + 42L)
    val b = checkSerializable(expectedSerialization, a)
    assertEquals(Integer.MAX_VALUE + 42L, b.unwrapped)
  }

  @Test
  def configDoubleSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i_z_e" +
      "_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w3F02000000_30500000019060000" +
      "000D000B_f_a_k_e_ _o_r_i_g_i_n09000000010001040000000F0440091EB8_QEB851F0004_3_." +
      "_1_40103000000010001_x"

    val a = doubleValue(3.14)
    val b = checkSerializable(expectedSerialization, a)
    assertEquals(3.14, b.unwrapped)
  }

  @Test
  def configNullSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_10200000025050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n090000000100010400000001000103000000010001_x"

    val a = nullValue()
    val b = checkSerializable(expectedSerialization, a)
    assertNull("b is null", b.unwrapped)
  }

  @Test
  def configBooleanSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_20200000026050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n09000000010001040000000201010103000000010001_x"

    val a = boolValue(true)
    val b = checkSerializable(expectedSerialization, a)
    assertEquals(true, b.unwrapped)
  }

  @Test
  def configStringSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_F02000000_:050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n090000000100010400000016050013_T_h_e_ _q_u_i_c" +
      "_k_ _b_r_o_w_n_ _f_o_x0103000000010001_x"

    val a = stringValue("The quick brown fox")
    val b = checkSerializable(expectedSerialization, a)
    assertEquals("The quick brown fox", b.unwrapped)
  }

  // in both ConfigValueTest and ConfigValueSharedTest
  private def configMap(
      pairs: (String, Int)*
  ): java.util.Map[String, AbstractConfigValue] = {
    val m = new java.util.HashMap[String, AbstractConfigValue]()
    for (p <- pairs) {
      m.put(p._1, intValue(p._2))
    }
    m
  }

  @Test
  def java6ConfigObjectSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_z02000000_n050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n0900000001000104000000_J07000000030001_a050000" +
      "000101040000000802000000010001_1010001_c050000000101040000000802000000030001_301" +
      "0001_b050000000101040000000802000000020001_2010103000000010001_x"

    val aMap = configMap("a" -> 1, "b" -> 2, "c" -> 3)
    val a = new SimpleConfigObject(fakeOrigin(), aMap)
    val b = checkSerializableOldFormat(expectedSerialization, a)
    assertEquals(1, b.toConfig.getInt("a"))
    // check that deserialized Config and ConfigObject refer to each other
    assertTrue(b.toConfig.root eq b)
  }

  @Test
  def java6ConfigConfigSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_z02000000_n050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n0900000001000104000000_J07000000030001_a050000" +
      "000101040000000802000000010001_1010001_c050000000101040000000802000000030001_301" +
      "0001_b050000000101040000000802000000020001_2010103000000010101_x"

    val aMap = configMap("a" -> 1, "b" -> 2, "c" -> 3)
    val a = new SimpleConfigObject(fakeOrigin(), aMap)
    val b = checkSerializableOldFormat(expectedSerialization, a.toConfig)
    assertEquals(1, b.getInt("a"))
    // check that deserialized Config and ConfigObject refer to each other
    assertTrue(b.root.toConfig eq b)
  }

  @Test
  def configObjectSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_z02000000_n050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n0900000001000104000000_J07000000030001_a050000" +
      "000101040000000802000000010001_1010001_b050000000101040000000802000000020001_201" +
      "0001_c050000000101040000000802000000030001_3010103000000010001_x"

    val aMap = configMap("a" -> 1, "b" -> 2, "c" -> 3)
    val a = new SimpleConfigObject(fakeOrigin(), aMap)
    val b = checkSerializable(expectedSerialization, a)
    assertEquals(1, b.toConfig.getInt("a"))
    // check that deserialized Config and ConfigObject refer to each other
    assertTrue(b.toConfig.root eq b)
  }

  @Test
  def configConfigSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_z02000000_n050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n0900000001000104000000_J07000000030001_a050000" +
      "000101040000000802000000010001_1010001_b050000000101040000000802000000020001_201" +
      "0001_c050000000101040000000802000000030001_3010103000000010101_x"

    val aMap = configMap("a" -> 1, "b" -> 2, "c" -> 3)
    val a = new SimpleConfigObject(fakeOrigin(), aMap)
    val b = checkSerializable(expectedSerialization, a.toConfig)
    assertEquals(1, b.getInt("a"))
    // check that deserialized Config and ConfigObject refer to each other
    assertTrue(b.root.toConfig eq b)
  }

  /**
   * Reproduces the issue:
   *
   * <a ref=https://github.com/lightbend/config/issues/461>#461</a>.
   *
   * <p> We use a custom de-/serializer that encodes String objects in a
   * JDK-incompatible way. Encoding used here is rather simplistic: a long
   * indicating the length in bytes (JDK uses a variable length integer)
   * followed by the string's bytes. Running this test with the original
   * SerializedConfigValue.readExternal() implementation results in an
   * EOFException thrown during deserialization.
   */
  @Test
  def configConfigCustomSerializable(): Unit = {
    val aMap = configMap("a" -> 1, "b" -> 2, "c" -> 3)
    val expected = new SimpleConfigObject(fakeOrigin(), aMap).toConfig
    val actual = checkSerializableWithCustomSerializer(expected)

    assertEquals(expected, actual)
  }

  @Test
  def configListSerializable(): Unit = {
    val expectedSerialization = "" +
      "ACED0005_s_r002C_o_r_g_._e_k_r_i_c_h_._c_o_n_f_i_g_._i_m_p_l_._S_e_r_i_a_l_i" +
      "_z_e_d_C_o_n_f_i_g_V_a_l_u_e00000000000000010C0000_x_p_w_q02000000_e050000001906" +
      "0000000D000B_f_a_k_e_ _o_r_i_g_i_n0900000001000104000000_A0600000003050000000101" +
      "040000000802000000010001_101050000000101040000000802000000020001_201050000000101" +
      "040000000802000000030001_3010103000000010001_x"
    val aScalaSeq = Seq(1, 2, 3) map { intValue(_): AbstractConfigValue }
    val aList = new SimpleConfigList(fakeOrigin(), aScalaSeq.asJava)
    val bList = checkSerializable(expectedSerialization, aList)
    assertEquals(1, bList.get(0).unwrapped)
  }

  @Test
  def configReferenceNotSerializable(): Unit = {
    val a = subst("foo")
    assertTrue("wrong type " + a, a.isInstanceOf[ConfigReference])
    checkNotSerializable(a)
  }

  @Test
  def configConcatenationNotSerializable(): Unit = {
    val a = substInString("foo")
    assertTrue("wrong type " + a, a.isInstanceOf[ConfigConcatenation])
    checkNotSerializable(a)
  }

  @Test
  def configDelayedMergeNotSerializable(): Unit = {
    val s1 = subst("foo")
    val s2 = subst("bar")
    val a = new ConfigDelayedMerge(
      fakeOrigin(),
      List[AbstractConfigValue](s1, s2).asJava
    )
    checkNotSerializable(a)
  }

  @Test
  def configDelayedMergeObjectNotSerializable(): Unit = {
    val empty = SimpleConfigObject.empty
    val s1 = subst("foo")
    val s2 = subst("bar")
    val a = new ConfigDelayedMergeObject(
      fakeOrigin(),
      List[AbstractConfigValue](empty, s1, s2).asJava
    )
    checkNotSerializable(a)
  }

  @Test
  def configOriginFileAndLine(): Unit = {
    val hasFilename = SimpleConfigOrigin.newFile("foo")
    val noFilename = SimpleConfigOrigin.newSimple("bar")
    val filenameWithLine = hasFilename.withLineNumber(3)
    val noFilenameWithLine = noFilename.withLineNumber(4)

    assertEquals("foo", hasFilename.filename)
    assertEquals("foo", filenameWithLine.filename)
    assertNull(noFilename.filename)
    assertNull(noFilenameWithLine.filename)

    assertEquals("foo", hasFilename.description)
    assertEquals("bar", noFilename.description)

    assertEquals(-1, hasFilename.lineNumber)
    assertEquals(-1, noFilename.lineNumber)

    assertEquals("foo: 3", filenameWithLine.description)
    assertEquals("bar: 4", noFilenameWithLine.description)

    assertEquals(3, filenameWithLine.lineNumber)
    assertEquals(4, noFilenameWithLine.lineNumber)

    // the filename is made absolute when converting to url
    assertTrue(hasFilename.url.toExternalForm.contains("foo"))
    assertNull(noFilename.url)
    val rootFile = SimpleConfigOrigin.newFile("/baz")
    val rootFileURL = if (isWindows) s"file:/$userDrive/baz" else "file:/baz"
    assertEquals(rootFileURL, rootFile.url.toExternalForm)

    val urlOrigin = SimpleConfigOrigin.newURL(new URL("file:/foo"))
    assertEquals("/foo", urlOrigin.filename)
    assertEquals("file:/foo", urlOrigin.url.toExternalForm)
  }

  @Test
  def configOriginsInSerialization(): Unit = {
    val bases = Seq(
      SimpleConfigOrigin.newSimple("foo"),
      SimpleConfigOrigin.newFile("/tmp/blahblah"),
      SimpleConfigOrigin.newURL(new URL("http://example.com")),
      SimpleConfigOrigin.newResource("myresource"),
      SimpleConfigOrigin.newResource("myresource", new URL("file://foo/bar"))
    )
    val combos = bases.flatMap({ base =>
      Seq(
        (
          base,
          base.withComments(Seq("this is a comment", "another one").asJava)
        ),
        (base, base.withComments(null)),
        (base, base.withLineNumber(41)),
        (
          base,
          SimpleConfigOrigin
            .mergeOrigins(base.withLineNumber(10), base.withLineNumber(20))
        )
      )
    }) ++
      bases
        .sliding(2)
        .map({ seq => (seq.head, seq.tail.head) }) ++
      bases
        .sliding(3)
        .map({ seq => (seq.head, seq.tail.tail.head) }) ++
      bases
        .sliding(4)
        .map({ seq => (seq.head, seq.tail.tail.tail.head) })
    val withFlipped = combos ++ combos.map(_.swap)
    val withDuplicate = withFlipped ++ withFlipped.map(p => (p._1, p._1))
    val values = withDuplicate.flatMap({ combo =>
      Seq(
        // second inside first
        new SimpleConfigList(
          combo._1,
          Seq[AbstractConfigValue](new ConfigInt(combo._2, 42, "42")).asJava
        ),
        // triple-nested means we have to null then un-null then null, which is a tricky case
        // in the origin-serialization code.
        new SimpleConfigList(
          combo._1,
          Seq[AbstractConfigValue](
            new SimpleConfigList(
              combo._2,
              Seq[AbstractConfigValue](new ConfigInt(combo._1, 42, "42")).asJava
            )
          ).asJava
        )
      )
    })
    def top(v: SimpleConfigList) = v.origin
    def middle(v: SimpleConfigList) = v.get(0).origin
    def bottom(v: SimpleConfigList) =
      if (v.get(0).isInstanceOf[ConfigList])
        Some(v.get(0).asInstanceOf[ConfigList].get(0).origin)
      else
        None

    // System.err.println("values=\n  " + values.map(v => top(v).description + ", " + middle(v).description + ", " + bottom(v).map(_.description)).mkString("\n  "))
    for (v <- values) {
      val deserialized = checkSerializable(v)
      // double-check that checkSerializable verified the origins
      assertEquals(top(v), top(deserialized))
      assertEquals(middle(v), middle(deserialized))
      assertEquals(bottom(v), bottom(deserialized))
    }
  }
}
