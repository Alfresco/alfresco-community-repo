/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.serviceaccount;

import static org.alfresco.service.cmr.security.PermissionService.ADMIN_SVC_AUTHORITY;
import static org.alfresco.service.cmr.security.PermissionService.COLLABORATOR_SVC_AUTHORITY;
import static org.alfresco.service.cmr.security.PermissionService.EDITOR_SVC_AUTHORITY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ServiceAccountRegistryImpl} class.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class ServiceAccountRegistryImplTest
{
    private ServiceAccountRegistryImpl serviceAccountService;
    private Properties globalProperties;

    @Before
    public void setUp() throws Exception
    {
        globalProperties = new Properties();
        globalProperties.put("system.test.property", "test-prop");
        globalProperties.put("repo.events.test.someKey", "test-event-value");

        serviceAccountService = new ServiceAccountRegistryImpl();
        serviceAccountService.setGlobalProperties(globalProperties);
        serviceAccountService.afterPropertiesSet();
    }

    @Test
    public void testNoDefinedServiceAccount()
    {
        Optional<String> nonExistentSa = serviceAccountService.getServiceAccountRole("nonExistentServiceAccount");
        assertTrue(nonExistentSa.isEmpty());
        assertTrue(serviceAccountService.getServiceAccountNames().isEmpty());
    }

    @Test
    public void testInvalidServiceAccountName()
    {
        globalProperties.put("serviceaccount.role. ", ADMIN_SVC_AUTHORITY);
        assertTrue("Invalid service account name.", serviceAccountService.getServiceAccountNames().isEmpty());
    }

    @Test
    public void testInvalidServiceAccountRole() throws Exception
    {
        globalProperties.put("serviceaccount.role.testServiceAccount", "");
        serviceAccountService.afterPropertiesSet();

        Optional<String> testServiceAccount = serviceAccountService.getServiceAccountRole("testServiceAccount");
        assertTrue("Invalid service account role.", testServiceAccount.isEmpty());
        assertTrue(serviceAccountService.getServiceAccountNames().isEmpty());
    }

    @Test
    public void testNotSupportedServiceAccountRole() throws Exception
    {
        globalProperties.put("serviceaccount.role.testServiceAccount", "testRole");
        serviceAccountService.afterPropertiesSet();

        Optional<String> testServiceAccount = serviceAccountService.getServiceAccountRole("testServiceAccount");
        assertTrue("Not supported service account role.", testServiceAccount.isEmpty());
        assertTrue(serviceAccountService.getServiceAccountNames().isEmpty());
    }

    @Test
    public void testValidServiceAccount() throws Exception
    {
        globalProperties.put("serviceaccount.role.testServiceAccount", ADMIN_SVC_AUTHORITY);
        serviceAccountService.afterPropertiesSet();

        Optional<String> testServiceAccount = serviceAccountService.getServiceAccountRole("testServiceAccount");
        assertFalse("The service account role is not empty.", testServiceAccount.isEmpty());
        assertEquals(ADMIN_SVC_AUTHORITY, testServiceAccount.get());
        assertEquals(1, serviceAccountService.getServiceAccountNames().size());
    }

    @Test
    public void testManyServiceAccounts() throws Exception
    {
        globalProperties.put("serviceaccount.role.testEditorSA", EDITOR_SVC_AUTHORITY);
        globalProperties.put("serviceaccount.role.testCollaboratorSA", COLLABORATOR_SVC_AUTHORITY);
        globalProperties.put("serviceaccount.role.testAdminSA", ADMIN_SVC_AUTHORITY);
        serviceAccountService.afterPropertiesSet();

        assertEquals(3, serviceAccountService.getServiceAccountNames().size());

        Optional<String> editorSA = serviceAccountService.getServiceAccountRole("testEditorSA");
        assertFalse("The service account role is not empty.", editorSA.isEmpty());
        assertEquals(EDITOR_SVC_AUTHORITY, editorSA.get());

        Optional<String> collaboratorSA = serviceAccountService.getServiceAccountRole("testCollaboratorSA");
        assertFalse("The service account role is not empty.", collaboratorSA.isEmpty());
        assertEquals(COLLABORATOR_SVC_AUTHORITY, collaboratorSA.get());

        Optional<String> adminSA = serviceAccountService.getServiceAccountRole("testAdminSA");
        assertFalse("The service account role is not empty.", adminSA.isEmpty());
        assertEquals(ADMIN_SVC_AUTHORITY, adminSA.get());
    }

    @Test
    public void testValidServiceAccountRoleValues() throws Exception
    {
        globalProperties.put("serviceaccount.role.testEditorSA", "EDITOR_SERVICE_ACCOUNT");
        globalProperties.put("serviceaccount.role.testCollaboratorSA", COLLABORATOR_SVC_AUTHORITY);
        globalProperties.put("serviceaccount.role.testAdminSA", "ADMIN_SERVICE_ACCOUNT");
        serviceAccountService.afterPropertiesSet();

        assertEquals(3, serviceAccountService.getServiceAccountNames().size());

        Optional<String> editorSA = serviceAccountService.getServiceAccountRole("testEditorSA");
        assertFalse("The service account role is not empty.", editorSA.isEmpty());
        assertEquals(EDITOR_SVC_AUTHORITY, editorSA.get());

        Optional<String> collaboratorSA = serviceAccountService.getServiceAccountRole("testCollaboratorSA");
        assertFalse("The service account role is not empty.", collaboratorSA.isEmpty());
        assertEquals(COLLABORATOR_SVC_AUTHORITY, collaboratorSA.get());

        Optional<String> adminSA = serviceAccountService.getServiceAccountRole("testAdminSA");
        assertFalse("The service account role is not empty.", adminSA.isEmpty());
        assertEquals(ADMIN_SVC_AUTHORITY, adminSA.get());
    }
}
