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
package org.alfresco.module.org_alfresco_module_rm.classification;

import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.ASPECT_SECURITY_CLEARANCE;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CLEARANCE_LEVEL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.Map;

/**
 * Unit tests for {@link SecurityClearanceServiceImpl}.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class SecurityClearanceServiceImplUnitTest
{
    @InjectMocks private SecurityClearanceServiceImpl securityClearanceServiceImpl;

    @Mock private AuthenticationUtil          mockedAuthenticationUtil;
    @Mock private ClassificationService       mockClassificationService;
    @Mock private DictionaryService           mockDictionaryService;
    @Mock private NodeService                 mockNodeService;
    @Mock private PersonService               mockPersonService;

    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    private PersonInfo createMockPerson(String userName, String firstName, String lastName, String clearanceLevel)
    {
        final NodeRef    userNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, userName);
        final PersonInfo info     = new PersonInfo(userNode, userName, firstName, lastName);

        when(mockPersonService.getPerson(eq(userName), anyBoolean())).thenReturn(userNode);
        when(mockPersonService.getPerson(eq(userNode))).thenReturn(info);

        when(mockNodeService.hasAspect(eq(userNode), eq(ASPECT_SECURITY_CLEARANCE))).thenReturn(clearanceLevel != null);
        when(mockNodeService.getProperty(eq(userNode), eq(PROP_CLEARANCE_LEVEL))).thenReturn(clearanceLevel);

        if (clearanceLevel != null)
        {
            final ClassificationLevel dummyValue = new ClassificationLevel(clearanceLevel, clearanceLevel);
            when(mockClassificationService.getClassificationLevelById(eq(clearanceLevel))).thenReturn(dummyValue);
        }

        return info;
    }

    @Test public void userWithNoClearanceGetsDefaultClearance()
    {
        final PersonInfo user1 = createMockPerson("user1", "User", "One", null);
        MockAuthenticationUtilHelper.setup(mockedAuthenticationUtil, user1.getUserName());

        when(mockClassificationService.getDefaultClassificationLevel())
                .thenReturn(new ClassificationLevel("default", "default"));

        final SecurityClearance clearance = securityClearanceServiceImpl.getUserSecurityClearance();

        assertEquals("default", clearance.getClearanceLevel().getId());
    }

    /** Check that a user can have their clearance set for the first time. */
    @Test public void setUserSecurityClearance_initialClearance()
    {
        // Create the user.
        String userName = "User 1";
        NodeRef personNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, userName);
        when(mockPersonService.getPerson(userName, false)).thenReturn(personNode);
        // The user has no previous clearance.
        when(mockNodeService.hasAspect(personNode, ASPECT_SECURITY_CLEARANCE)).thenReturn(false);
        // Create the clearance.
        String clearanceId = "ClearanceId";
        ClassificationLevel level = new ClassificationLevel(clearanceId, "TopSecretKey");
        when(mockClassificationService.getClassificationLevelById(clearanceId)).thenReturn(level);

        // Call the method under test.
        securityClearanceServiceImpl.setUserSecurityClearance(userName, clearanceId);

        Map<QName, Serializable> expectedProperties = ImmutableMap.of(PROP_CLEARANCE_LEVEL, clearanceId);
        verify(mockNodeService).addAspect(personNode, ASPECT_SECURITY_CLEARANCE, expectedProperties);
    }

    /** Check that a user can have their clearance edited. */
    @Test public void setUserSecurityClearance_updateClearance()
    {
        // Create the user.
        String userName = "User 1";
        NodeRef personNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, userName);
        when(mockPersonService.getPerson(userName, false)).thenReturn(personNode);
        // The user has a previous clearance.
        when(mockNodeService.hasAspect(personNode, ASPECT_SECURITY_CLEARANCE)).thenReturn(true);
        // Create the clearance.
        String clearanceId = "ClearanceId";
        ClassificationLevel level = new ClassificationLevel(clearanceId, "TopSecretKey");
        when(mockClassificationService.getClassificationLevelById(clearanceId)).thenReturn(level);

        // Call the method under test.
        securityClearanceServiceImpl.setUserSecurityClearance(userName, clearanceId);

        verify(mockNodeService).setProperty(personNode, PROP_CLEARANCE_LEVEL, clearanceId);
    }

    /**
     * Check that a user cannot raise someone's clearance above their own. Here we check that an exception thrown by the
     * classification service is passed through.
     */
    @Test(expected = LevelIdNotFound.class)
    public void setUserSecurityClearance_insufficientClearance()
    {
        String userName = "User 1";
        NodeRef personNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, userName);
        when(mockPersonService.getPerson(userName, false)).thenReturn(personNode);
        String clearanceId = "ClearanceId";
        // If the user has insufficient clearance then they cannot access the level.
        when(mockClassificationService.getClassificationLevelById(clearanceId)).thenThrow(new LevelIdNotFound(clearanceId));

        securityClearanceServiceImpl.setUserSecurityClearance(userName, clearanceId);
    }
}
