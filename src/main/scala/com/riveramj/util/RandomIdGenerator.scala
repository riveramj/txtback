package com.riveramj.util

import org.apache.shiro.crypto.SecureRandomNumberGenerator
import net.liftweb.common.Loggable

object RandomIdGenerator extends Loggable {
  private val rng = new SecureRandomNumberGenerator();

  def generateId() = {
    rng.nextBytes(32).toBase64
  }
}
