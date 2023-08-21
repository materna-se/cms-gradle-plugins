/*
 * Copyright © 2020 Materna Information & Communications SE
 */

package de.materna.cms.tools.tracelog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingAspectTest {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspectTest.class);

  @Test
  public void testReturn() {
    returnObj(0, "dummy0");
    returnObj(1, "dummy1");
    returnObj(2, "dummy2");
    returnObj(10, "dummy10");
  }

  @Test
  public void testThrow() {
    Assertions.assertThrows(NullPointerException.class, () ->
        throwEx(10, new NullPointerException("foobar"))
    );
  }

  public Object returnObj(int i, Object object) {
    if (i > 0) {
      return returnObj(i - 1, object);
    } else {
      log.debug("returning");
      return object;
    }
  }

  public void throwEx(int i, Exception e) throws Exception {
    if (i > 0) {
      throwEx(i - 1, e);
    } else {
      throw e;
    }
  }
}
