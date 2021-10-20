/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.alfresco.repo.content.directurl.SystemWideDirectUrlConfig;
import org.alfresco.rest.api.impl.directurl.RestApiDirectUrlConfig;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for REST API direct access URL configuration settings.
 *
 * @author Sara Aspery
 */
public class RestApiDirectUrlConfigUnitTest {

  private static final Boolean ENABLED = Boolean.TRUE;
  private static final Boolean DISABLED = Boolean.FALSE;

  private static final Long DEFAULT_EXPIRY_TIME_IN_SECS = 20L;

  private RestApiDirectUrlConfig restApiDirectUrlConfig;

  @Before
  public void setup() {
    this.restApiDirectUrlConfig = new RestApiDirectUrlConfig();
    SystemWideDirectUrlConfig sysConfig = new SystemWideDirectUrlConfig();
    sysConfig.setEnabled(ENABLED);
    sysConfig.setDefaultExpiryTimeInSec(30L);
    sysConfig.setMaxExpiryTimeInSec(300L);
    restApiDirectUrlConfig.setSystemWideDirectUrlConfig(sysConfig);
  }

  @Test
  public void testValidConfig_RemainsEnabled() {
    setupDirectAccessConfig(ENABLED, DEFAULT_EXPIRY_TIME_IN_SECS);

    assertTrue(
      "Expected REST API direct URLs to be enabled",
      restApiDirectUrlConfig.isEnabled()
    );
    restApiDirectUrlConfig.validate();
    assertTrue(
      "Expected REST API direct URLs to be enabled",
      restApiDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testValidConfig_RemainsDisabled() {
    setupDirectAccessConfig(DISABLED, DEFAULT_EXPIRY_TIME_IN_SECS);

    assertFalse(
      "Expected REST API direct URLs to be disabled",
      restApiDirectUrlConfig.isEnabled()
    );
    restApiDirectUrlConfig.validate();
    assertFalse(
      "Expected REST API direct URLs to be disabled",
      restApiDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testValidConfig_DefaultExpiryTimeMissing() {
    setupDirectAccessConfig(ENABLED, null);

    assertNull(
      "Expected REST API default expiry time to be null",
      restApiDirectUrlConfig.getDefaultExpiryTimeInSec()
    );
    restApiDirectUrlConfig.validate();
    Long expectedDefaultExpiryTime = restApiDirectUrlConfig.getSysWideDefaultExpiryTimeInSec();
    assertEquals(
      "Expected REST API default expiry time to be set to the system-wide default",
      expectedDefaultExpiryTime,
      restApiDirectUrlConfig.getDefaultExpiryTimeInSec()
    );
    assertTrue(
      "Expected REST API direct URLs to be enabled",
      restApiDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_DefaultExpiryTimeZero() {
    setupDirectAccessConfig(ENABLED, 0L);

    assertTrue(
      "Expected REST API direct URLs to be enabled",
      restApiDirectUrlConfig.isEnabled()
    );
    restApiDirectUrlConfig.validate();
    assertFalse(
      "Expected REST API direct URLs to be disabled",
      restApiDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_DefaultExpiryTimeNegative() {
    setupDirectAccessConfig(ENABLED, -1L);

    assertTrue(
      "Expected REST API direct URLs to be enabled",
      restApiDirectUrlConfig.isEnabled()
    );
    restApiDirectUrlConfig.validate();
    assertFalse(
      "Expected REST API direct URLs to be disabled",
      restApiDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_DefaultExpiryTimeExceedsSystemMax() {
    Long systemMax = restApiDirectUrlConfig.getSysWideMaxExpiryTimeInSec();
    setupDirectAccessConfig(ENABLED, systemMax + 1);

    assertTrue(
      "Expected REST API direct URLs to be enabled",
      restApiDirectUrlConfig.isEnabled()
    );
    restApiDirectUrlConfig.validate();
    assertFalse(
      "Expected REST API direct URLs to be disabled",
      restApiDirectUrlConfig.isEnabled()
    );
  }

  /* Helper method to set system-wide direct access url configuration settings */
  private void setupDirectAccessConfig(
    Boolean isEnabled,
    Long defaultExpiryTime
  ) {
    restApiDirectUrlConfig.setEnabled(isEnabled);
    restApiDirectUrlConfig.setDefaultExpiryTimeInSec(defaultExpiryTime);
  }
}
