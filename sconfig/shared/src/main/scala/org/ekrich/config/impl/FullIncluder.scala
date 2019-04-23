/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package org.ekrich.config.impl

import org.ekrich.config.ConfigIncluder
import org.ekrich.config.ConfigIncluderClasspath
import org.ekrich.config.ConfigIncluderFile
import org.ekrich.config.ConfigIncluderURL

trait FullIncluder
    extends ConfigIncluder
    with ConfigIncluderFile
    with ConfigIncluderURL
    with ConfigIncluderClasspath {}
