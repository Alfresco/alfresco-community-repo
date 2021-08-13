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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for content store direct access URL configuration settings.
 *
 * @author Sara Aspery
 */
public class ContentStoreDirectUrlConfigUnitTest
{
    private static final Boolean ENABLED = Boolean.TRUE;
    private static final Boolean DISABLED = Boolean.FALSE;

    private static final Long DEFAULT_EXPIRY_TIME_IN_SECS = 10L;
    private static final Long MAX_EXPIRY_TIME_IN_SECS = 20L;

    private static final Long SYS_DEFAULT_EXPIRY_TIME_IN_SECS = 30L;
    private static final Long SYS_MAX_EXPIRY_TIME_IN_SECS = 300L;

    private ContentStoreDirectUrlConfig contentStoreDirectUrlConfig;

    @Before
    public void setup()
    {
        this.contentStoreDirectUrlConfig = new ContentStoreDirectUrlConfig();
        setupSystemWideDirectAccessConfig();
    }

    @Test
    public void testValidConfig_RemainsEnabled()
    {
        setupDirectAccessConfig(ENABLED, DEFAULT_EXPIRY_TIME_IN_SECS, MAX_EXPIRY_TIME_IN_SECS);

        assertTrue("Expected content store direct URLs to be enabled", contentStoreDirectUrlConfig.isEnabled());
        contentStoreDirectUrlConfig.validate();
        assertTrue("Expected REST API direct URLs to be enabled", contentStoreDirectUrlConfig.isEnabled());
    }

    @Test
    public void testValidConfig_RemainsDisabled()
    {
        setupDirectAccessConfig(DISABLED, DEFAULT_EXPIRY_TIME_IN_SECS, MAX_EXPIRY_TIME_IN_SECS);

        assertFalse("Expected content store direct URLs to be disabled", contentStoreDirectUrlConfig.isEnabled());
        contentStoreDirectUrlConfig.validate();
        assertFalse("Expected content store direct URLs to be disabled", contentStoreDirectUrlConfig.isEnabled());
    }

    @Test
    public void testInvalidConfig_DefaultExpiryTimeMissing_ValidReplacement()
    {
        Long maxExpiryTimeInSecs = SYS_DEFAULT_EXPIRY_TIME_IN_SECS + 1;
        setupDirectAccessConfig(ENABLED, null, maxExpiryTimeInSecs);

        verifyDirectAccessConfig(ENABLED, null, maxExpiryTimeInSecs);
        contentStoreDirectUrlConfig.validate();
        verifyDirectAccessConfig(ENABLED, SYS_DEFAULT_EXPIRY_TIME_IN_SECS, maxExpiryTimeInSecs);
    }

    @Test
    public void testInvalidConfig_DefaultExpiryTimeMissing_ReplacementExceedsMax()
    {
        setupDirectAccessConfig(ENABLED, null, MAX_EXPIRY_TIME_IN_SECS);

        verifyDirectAccessConfig(ENABLED, null, MAX_EXPIRY_TIME_IN_SECS);
        contentStoreDirectUrlConfig.validate();
        verifyDirectAccessConfig(DISABLED, SYS_DEFAULT_EXPIRY_TIME_IN_SECS, MAX_EXPIRY_TIME_IN_SECS);
    }

    @Test
    public void testInvalidConfig_DefaultExpiryTimeZero()
    {
        setupDirectAccessConfig(ENABLED, 0L, MAX_EXPIRY_TIME_IN_SECS);

        assertTrue("Expected content store direct URLs to be enabled", contentStoreDirectUrlConfig.isEnabled());
        contentStoreDirectUrlConfig.validate();
        assertFalse("Expected content store direct URLs to be disabled", contentStoreDirectUrlConfig.isEnabled());
    }

    @Test
    public void testInvalidConfig_DefaultExpiryTimeNegative()
    {
        setupDirectAccessConfig(ENABLED, -1L, MAX_EXPIRY_TIME_IN_SECS);

        assertTrue("Expected content store direct URLs to be enabled", contentStoreDirectUrlConfig.isEnabled());
        contentStoreDirectUrlConfig.validate();
        assertFalse("Expected content store direct URLs to be disabled", contentStoreDirectUrlConfig.isEnabled());
    }

    @Test
    public void testInvalidConfig_DefaultExpiryTimeExceedsSystemMax()
    {
        Long defaultExpiryTimeInSecs = SYS_MAX_EXPIRY_TIME_IN_SECS + 1;
        setupDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, MAX_EXPIRY_TIME_IN_SECS);

        assertTrue("Expected content store direct URLs to be enabled", contentStoreDirectUrlConfig.isEnabled());
        contentStoreDirectUrlConfig.validate();
        assertFalse("Expected content store direct URLs to be disabled", contentStoreDirectUrlConfig.isEnabled());
    }

    @Test
    public void testInvalidConfig_DefaultExpiryTimeExceedsStoreMax_ValidReplacement()
    {
        Long maxExpiryTimeInSecs = SYS_DEFAULT_EXPIRY_TIME_IN_SECS + 1;
        Long defaultExpiryTimeInSecs = maxExpiryTimeInSecs + 1;
        setupDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, maxExpiryTimeInSecs);

        verifyDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, maxExpiryTimeInSecs);
        contentStoreDirectUrlConfig.validate();
        verifyDirectAccessConfig(ENABLED, SYS_DEFAULT_EXPIRY_TIME_IN_SECS, maxExpiryTimeInSecs);
    }

    @Test
    public void testInvalidConfig_DefaultExpiryTimeExceedsStoreMax_ReplacementExceedsStoreMax()
    {
        Long defaultExpiryTimeInSecs = MAX_EXPIRY_TIME_IN_SECS + 1;
        setupDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, MAX_EXPIRY_TIME_IN_SECS);

        verifyDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, MAX_EXPIRY_TIME_IN_SECS);
        contentStoreDirectUrlConfig.validate();
        verifyDirectAccessConfig(DISABLED, SYS_DEFAULT_EXPIRY_TIME_IN_SECS, MAX_EXPIRY_TIME_IN_SECS);
    }
    @Test
    public void testInvalidConfig_DefaultExpiryTimeExceedsSystemDefault_ValidReplacement()
    {
        Long defaultExpiryTimeInSecs = SYS_DEFAULT_EXPIRY_TIME_IN_SECS + 1;
        Long maxExpiryTimeInSecs = SYS_MAX_EXPIRY_TIME_IN_SECS;
        setupDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, maxExpiryTimeInSecs);

        verifyDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, maxExpiryTimeInSecs);
        contentStoreDirectUrlConfig.validate();
        verifyDirectAccessConfig(ENABLED, SYS_DEFAULT_EXPIRY_TIME_IN_SECS, maxExpiryTimeInSecs);
    }

    @Test
    public void testInvalidConfig_DefaultExpiryTimeExceedsSystemDefault_ReplacementExceedsStoreMax()
    {
        Long defaultExpiryTimeInSecs = SYS_DEFAULT_EXPIRY_TIME_IN_SECS + 1;
        setupDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, MAX_EXPIRY_TIME_IN_SECS);

        verifyDirectAccessConfig(ENABLED, defaultExpiryTimeInSecs, MAX_EXPIRY_TIME_IN_SECS);
        contentStoreDirectUrlConfig.validate();
        verifyDirectAccessConfig(DISABLED, SYS_DEFAULT_EXPIRY_TIME_IN_SECS, MAX_EXPIRY_TIME_IN_SECS);
    }

    @Test
    public void testInvalidConfig_MaxExpiryTimeZero()
    {
        setupDirectAccessConfig(ENABLED, DEFAULT_EXPIRY_TIME_IN_SECS, 0L);

        assertTrue("Expected content store direct URLs to be enabled", contentStoreDirectUrlConfig.isEnabled());
        contentStoreDirectUrlConfig.validate();
        assertFalse("Expected content store direct URLs to be disabled", contentStoreDirectUrlConfig.isEnabled());
    }

    @Test
    public void testInvalidConfig_MaxExpiryTimeNegative()
    {
        setupDirectAccessConfig(ENABLED, DEFAULT_EXPIRY_TIME_IN_SECS, -1L);

        assertTrue("Expected content store direct URLs to be enabled", contentStoreDirectUrlConfig.isEnabled());
        contentStoreDirectUrlConfig.validate();
        assertFalse("Expected content store direct URLs to be disabled", contentStoreDirectUrlConfig.isEnabled());
    }

    @Test
    public void testInvalidConfig_MaxExpiryTimeExceedsSystemMax()
    {
        Long maxExpiryTimeInSec = contentStoreDirectUrlConfig.getSysWideMaxExpiryTimeInSec() + 1;
        setupDirectAccessConfig(ENABLED, DEFAULT_EXPIRY_TIME_IN_SECS, maxExpiryTimeInSec);

        assertTrue("Expected content store direct URLs to be enabled", contentStoreDirectUrlConfig.isEnabled());
        contentStoreDirectUrlConfig.validate();
        assertFalse("Expected content store direct URLs to be disabled", contentStoreDirectUrlConfig.isEnabled());
    }

    /* Helper method to set content store direct access url configuration settings */
    private void setupDirectAccessConfig(Boolean isEnabled, Long defaultExpiryTime, Long maxExpiryTime)
    {
        contentStoreDirectUrlConfig.setEnabled(isEnabled);
        contentStoreDirectUrlConfig.setDefaultExpiryTimeInSec(defaultExpiryTime);
        contentStoreDirectUrlConfig.setMaxExpiryTimeInSec(maxExpiryTime);
    }

    /* Helper method to verify content store direct access url configuration settings */
    private void verifyDirectAccessConfig(Boolean isEnabled, Long defaultExpiryTime, Long maxExpiryTime)
    {
        assertEquals("Expected content store direct URLs to be enabled = " + isEnabled, isEnabled, contentStoreDirectUrlConfig.isEnabled());
        assertEquals("Expected default expiry time to match " + defaultExpiryTime, defaultExpiryTime, contentStoreDirectUrlConfig.getDefaultExpiryTimeInSec());
        assertEquals("Expected maximum expiry time to match " + maxExpiryTime, maxExpiryTime, contentStoreDirectUrlConfig.getMaxExpiryTimeInSec());
    }

    /* Helper method to set system-wide direct access url configuration settings */
    private void setupSystemWideDirectAccessConfig()
    {
        SystemWideDirectUrlConfig sysConfig = new SystemWideDirectUrlConfig();
        sysConfig.setEnabled(ENABLED);
        sysConfig.setDefaultExpiryTimeInSec(SYS_DEFAULT_EXPIRY_TIME_IN_SECS);
        sysConfig.setMaxExpiryTimeInSec(SYS_MAX_EXPIRY_TIME_IN_SECS);
        sysConfig.validate();
        contentStoreDirectUrlConfig.setSystemWideDirectUrlConfig(sysConfig);
    }
}
