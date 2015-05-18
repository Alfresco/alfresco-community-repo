/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevelManager;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for {@link ClearancesForSpecialUsersBootstrapComponent}.
 *
 * @author tpage
 */
public class ClearancesForSpecialUsersBootstrapComponentUnitTest implements ClassifiedContentModel
{
    @InjectMocks ClearancesForSpecialUsersBootstrapComponent clearancesForSpecialUsersBootstrapComponent;
    @Mock AuthenticationUtil mockAuthenticationUtil;
    @Mock PersonService mockPersonService;
    @Mock NodeService mockNodeService;
    @Mock ClassificationServiceBootstrap mockClassificationServiceBootstrap;

    @Before public void setUp()
    {
        initMocks(this);
    }

    /** Check that the system and admin users get assigned the provided clearance. */
    @Test public void testCreateClearancesForSpecialUsers()
    {
        // Allow the classification level id to be found.
        ClassificationLevel level = new ClassificationLevel("id", "displayLabelKey");
        ClassificationLevelManager mockClassificationLevelManager = mock(ClassificationLevelManager.class);
        when(mockClassificationLevelManager.getMostSecureLevel()).thenReturn(level);
        when(mockClassificationServiceBootstrap.getClassificationLevelManager()).thenReturn(mockClassificationLevelManager);

        // Set up the admin and system users.
        when(mockAuthenticationUtil.getSystemUserName()).thenReturn("system");
        NodeRef system = new NodeRef("system://node/");
        when(mockPersonService.getPerson("system")).thenReturn(system);
        when(mockAuthenticationUtil.getAdminUserName()).thenReturn("admin");
        NodeRef admin = new NodeRef("admin://node/");
        when(mockPersonService.getPerson("admin")).thenReturn(admin);

        // Call the method under test.
        clearancesForSpecialUsersBootstrapComponent.createClearancesForSpecialUsers();

        verify(mockNodeService).setProperty(system, PROP_CLEARANCE_LEVEL, "id");
        verify(mockNodeService).setProperty(admin, PROP_CLEARANCE_LEVEL, "id");
        // Check that the classification levels were loaded.
        verify(mockClassificationServiceBootstrap).onBootstrap(null);
    }
}
