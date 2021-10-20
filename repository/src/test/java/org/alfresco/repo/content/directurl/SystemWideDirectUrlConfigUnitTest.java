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
package org.alfresco.repo.content.directurl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for system-wide direct access URL configuration settings.
 *
 * @author Sara Aspery
 */
public class SystemWideDirectUrlConfigUnitTest {

  private static final Boolean ENABLED = Boolean.TRUE;
  private static final Boolean DISABLED = Boolean.FALSE;

  private static final Long DEFAULT_EXPIRY_TIME_IN_SECS = 30L;
  private static final Long MAX_EXPIRY_TIME_IN_SECS = 300L;

  private SystemWideDirectUrlConfig systemWideDirectUrlConfig;

  @Before
  public void setup() {
    this.systemWideDirectUrlConfig = new SystemWideDirectUrlConfig();
  }

  @Test
  public void testValidConfig_RemainsEnabled() {
    setupDirectAccessConfig(
      ENABLED,
      DEFAULT_EXPIRY_TIME_IN_SECS,
      MAX_EXPIRY_TIME_IN_SECS
    );

    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testValidConfig_RemainsDisabled() {
    setupDirectAccessConfig(
      DISABLED,
      DEFAULT_EXPIRY_TIME_IN_SECS,
      MAX_EXPIRY_TIME_IN_SECS
    );

    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_DefaultExpiryTimeMissing() {
    setupDirectAccessConfig(ENABLED, null, MAX_EXPIRY_TIME_IN_SECS);

    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_DefaultExpiryTimeZero() {
    setupDirectAccessConfig(ENABLED, 0L, MAX_EXPIRY_TIME_IN_SECS);

    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_DefaultExpiryTimeNegative() {
    setupDirectAccessConfig(ENABLED, -1L, MAX_EXPIRY_TIME_IN_SECS);

    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_MaxExpiryTimeMissing() {
    setupDirectAccessConfig(ENABLED, DEFAULT_EXPIRY_TIME_IN_SECS, null);

    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_MaxExpiryTimeZero() {
    setupDirectAccessConfig(ENABLED, DEFAULT_EXPIRY_TIME_IN_SECS, 0L);

    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_MaxExpiryTimeNegative() {
    setupDirectAccessConfig(ENABLED, DEFAULT_EXPIRY_TIME_IN_SECS, -1L);

    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  @Test
  public void testInvalidConfig_DefaultExpiryTimeExceedsMax() {
    setupDirectAccessConfig(
      ENABLED,
      MAX_EXPIRY_TIME_IN_SECS + 1,
      MAX_EXPIRY_TIME_IN_SECS
    );

    assertTrue(
      "Expected system-wide direct URLs to be enabled",
      systemWideDirectUrlConfig.isEnabled()
    );
    systemWideDirectUrlConfig.validate();
    assertFalse(
      "Expected system-wide direct URLs to be disabled",
      systemWideDirectUrlConfig.isEnabled()
    );
  }

  /* Helper method to set system-wide direct access url configuration settings */
  private void setupDirectAccessConfig(
    Boolean isEnabled,
    Long defaultExpiryTime,
    Long maxExpiryTime
  ) {
    systemWideDirectUrlConfig.setEnabled(isEnabled);
    systemWideDirectUrlConfig.setDefaultExpiryTimeInSec(defaultExpiryTime);
    systemWideDirectUrlConfig.setMaxExpiryTimeInSec(maxExpiryTime);
  }
}
