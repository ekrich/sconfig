package org.ekrich.config

import java.io.{File, Reader}
import java.net.URL
import java.{util => ju}
import java.util.Properties
import java.util.concurrent.Callable

import org.ekrich.config.impl.ConfigImpl
import org.ekrich.config.impl.Parseable

abstract class ConfigFactoryCommon {}
