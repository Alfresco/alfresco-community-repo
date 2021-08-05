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
package org.alfresco.rest.api.discovery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.alfresco.rest.api.impl.directurl.RestApiDirectUrlConfig;
import org.alfresco.service.cmr.repository.ContentService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * @author Mikołaj Brzeziński
 */
public class DiscoveryApiWebscriptUnitTest
{
    private static final Boolean ENABLED = Boolean.TRUE;
    private static final Boolean DISABLED = Boolean.FALSE;

    private DiscoveryApiWebscript discoveryApiWebscript = mock(DiscoveryApiWebscript.class);
    private RestApiDirectUrlConfig restApiDirectUrlConfig = mock(RestApiDirectUrlConfig.class);
    private ContentService contentService = mock(ContentService.class);

    public boolean mockedAsserts(boolean restEnabled, boolean systemwideEnabled)
    {
        when(contentService.isContentUrlEnabled()).thenReturn(systemwideEnabled);
        when(restApiDirectUrlConfig.isEnabled()).thenReturn(restEnabled);
        assertEquals(systemwideEnabled, contentService.isContentUrlEnabled());
        assertEquals(restEnabled, restApiDirectUrlConfig.isEnabled());
        when(discoveryApiWebscript.isContentDirectUrlEnabled()).thenReturn(restEnabled && systemwideEnabled);
    }

    @Test
    public void testEnabledConfig_RestEnabledAndSystemwideEnabled()
    {
        mockedAsserts(ENABLED,ENABLED);
        assertTrue("Direct Acess URLs are enabled",statusInfo.getIsDirectAccessUrlEnabled());
    }

    @Test
    public void testDisabledConfig_RestEnabledAndSystemwideDisabled()
    {
        mockedAsserts(ENABLED,DISABLED);
        assertFalse("Direct Access URLs are disabled system-wide",statusInfo.getIsDirectAccessUrlEnabled());
    }

    @Test
    public void testDisabledConfig_RestDisabledAndSystemwideDisabled()
    {
        mockedAsserts(DISABLED,DISABLED);
        assertFalse("REST API Direct Access URLs are disabled and Direct Access URLs are disabled system-wide ",statusInfo.getIsDirectAccessUrlEnabled());
    }

    @Test
    public void testDisabledConfig_RestDisabledAndSystemwideEnabled()
    {
        mockedAsserts(DISABLED,ENABLED);
        assertFalse("REST API direct access URLs are disabled",statusInfo.getIsDirectAccessUrlEnabled());
    }

}
