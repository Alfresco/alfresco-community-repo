/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.version;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.util.AlfrescoTransactionSupport;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.GUID;

/**
 * Extended versionable aspect unit test.
 *
 * @author Roy Wetherall
 * @since 2.3.1
 */
public class ExtendedVersionableAspectUnitTest implements RecordsManagementModel
{
    /** Transaction resource key */
    private static final String KEY_VERSIONED_NODEREFS = "versioned_noderefs";

    /** test data */
    private NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
    private NodeRef anotherNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
    private QName oldType = QName.createQName(RM_URI, GUID.generate());
    private QName newType = QName.createQName(RM_URI, GUID.generate());

    /** service mocks */
    private @Mock NodeService mockedNodeService;
    private @Mock VersionService mockedVersionService;
    private @Mock LockService mockedLockService;
    private @Mock AlfrescoTransactionSupport mockedAlfrescoTransactionSupport;
    private @Mock AuthenticationUtil mockedAuthenticationUtil;

    /** test instance of extended versionable aspect behaviour bean */
    private @InjectMocks ExtendedVersionableAspect extendedVersionableAspect;

    @SuppressWarnings("unchecked")
    @Before
    public void testSetup()
    {
        MockitoAnnotations.initMocks(this);

        // just do the work
        doAnswer(new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                RunAsWork work = (RunAsWork)invocation.getArguments()[0];
                return work.doWork();
            }

        }).when(mockedAuthenticationUtil).runAsSystem((RunAsWork<Object>) any(RunAsWork.class));
    }

    /**
     * given that autoversion on type change is configured off
     * when the type set behvaiour is executed
     * then a new version is not created
     */
    @SuppressWarnings("unchecked")
    @Test
    public void autoVersionOff()
    {
        // auto version off
        extendedVersionableAspect.setAutoVersionOnTypeChange(false);

        // execute behaviour
        extendedVersionableAspect.onSetNodeType(nodeRef, oldType, newType);

        // assert the version was not created
        verify(mockedVersionService, never()).createVersion(eq(nodeRef), any(Map.class));
    }

    /**
     * given the node doesn't exist
     * when the type set behaviour is executed
     * then a new version is not created
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeDoesNotExist()
    {
        // auto version on
        extendedVersionableAspect.setAutoVersionOnTypeChange(true);

        // node does not exist
        when(mockedNodeService.exists(nodeRef))
            .thenReturn(false);

        // execute behaviour
        extendedVersionableAspect.onSetNodeType(nodeRef, oldType, newType);

        // assert the version was not created
        verify(mockedVersionService, never()).createVersion(eq(nodeRef), any(Map.class));
    }

    /**
     * given that the node is locked
     * when the type set behaviour is executed
     * then a new version is not created
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeLocked()
    {
        // auto version on
        extendedVersionableAspect.setAutoVersionOnTypeChange(true);

        // node does exists
        when(mockedNodeService.exists(nodeRef))
            .thenReturn(true);

        // node is locked
        when(mockedLockService.getLockStatus(nodeRef))
            .thenReturn(LockStatus.LOCKED);

        // execute behaviour
        extendedVersionableAspect.onSetNodeType(nodeRef, oldType, newType);

        // verify other
        verify(mockedNodeService).exists(nodeRef);

        // assert the version was not created
        verify(mockedVersionService, never()).createVersion(eq(nodeRef), any(Map.class));
    }

    /**
     * given that the node does not have the versionable aspect
     * when the type set behaviour is executed
     * then a new version is not created
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeIsNotVersionable()
    {
        // auto version on
        extendedVersionableAspect.setAutoVersionOnTypeChange(true);

        // node does exists
        when(mockedNodeService.exists(nodeRef))
            .thenReturn(true);

        // node is not locked
        when(mockedLockService.getLockStatus(nodeRef))
            .thenReturn(LockStatus.NO_LOCK);

        // node does not have the versionable aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            .thenReturn(false);

        // execute behaviour
        extendedVersionableAspect.onSetNodeType(nodeRef, oldType, newType);

        // verify other
        verify(mockedNodeService).exists(nodeRef);
        verify(mockedLockService).isLockedAndReadOnly(nodeRef);

        // assert the version was not created
        verify(mockedVersionService, never()).createVersion(eq(nodeRef), any(Map.class));
    }

    /**
     * given that the node has the temporary aspect
     * when the type set behaviour is executed
     * then a new version is not created
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeIsTemporary()
    {
        // auto version on
        extendedVersionableAspect.setAutoVersionOnTypeChange(true);

        // node does exists
        when(mockedNodeService.exists(nodeRef))
            .thenReturn(true);

        // node is not locked
        when(mockedLockService.getLockStatus(nodeRef))
            .thenReturn(LockStatus.NO_LOCK);

        // node has the versionable aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            .thenReturn(true);

        // node has the temporary aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY))
            .thenReturn(true);

        // execute behaviour
        extendedVersionableAspect.onSetNodeType(nodeRef, oldType, newType);

        // verify other
        verify(mockedNodeService).exists(nodeRef);
        verify(mockedLockService).isLockedAndReadOnly(nodeRef);
        verify(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);

        // assert the version was not created
        verify(mockedVersionService, never()).createVersion(eq(nodeRef), any(Map.class));
    }

    /**
     * given that the node is already being versioned
     * when the type set behvaiour is executed
     * then a new version is not created
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeIsBeingVersioned()
    {
        // auto version on
        extendedVersionableAspect.setAutoVersionOnTypeChange(true);

        // node does exists
        when(mockedNodeService.exists(nodeRef))
            .thenReturn(true);

        // node is not locked
        when(mockedLockService.getLockStatus(nodeRef))
            .thenReturn(LockStatus.NO_LOCK);

        // node has the versionable aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            .thenReturn(true);

        // node does not have the temporary aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY))
            .thenReturn(false);

        // node is currently being processed for versioning
        when(mockedAlfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS))
            .thenReturn(Collections.singletonMap(nodeRef, nodeRef));

        // execute behaviour
        extendedVersionableAspect.onSetNodeType(nodeRef, oldType, newType);

        // verify other
        verify(mockedNodeService).exists(nodeRef);
        verify(mockedLockService).isLockedAndReadOnly(nodeRef);
        verify(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        verify(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY);

        // assert the version was not created
        verify(mockedVersionService, never()).createVersion(eq(nodeRef), any(Map.class));
    }

    /**
     * given that the node has the auto version property set to false
     * when the type set behaviour is executed
     * then a new version is not created
     */
    @SuppressWarnings("unchecked")
    @Test
    public void autoVersionFalse()
    {
        // auto version on
        extendedVersionableAspect.setAutoVersionOnTypeChange(true);

        // node does exists
        when(mockedNodeService.exists(nodeRef))
            .thenReturn(true);

        // node is not locked
        when(mockedLockService.getLockStatus(nodeRef))
            .thenReturn(LockStatus.NO_LOCK);

        // node has the versionable aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            .thenReturn(true);

        // node does not have the temporary aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY))
            .thenReturn(false);

        // node is not being processed for versioning
        when(mockedAlfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS))
            .thenReturn(Collections.singletonMap(anotherNodeRef, anotherNodeRef));

        // auto version false
        when(mockedNodeService.getProperty(nodeRef, ContentModel.PROP_AUTO_VERSION))
            .thenReturn(Boolean.FALSE);

        // execute behaviour
        extendedVersionableAspect.onSetNodeType(nodeRef, oldType, newType);

        // verify other
        verify(mockedNodeService).exists(nodeRef);
        verify(mockedLockService).isLockedAndReadOnly(nodeRef);
        verify(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        verify(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY);
        verify(mockedAlfrescoTransactionSupport).getResource(KEY_VERSIONED_NODEREFS);

        // assert the version was not created
        verify(mockedVersionService, never()).createVersion(eq(nodeRef), any(Map.class));
    }

    /**
     * given that autoversion on type change is configured on
     * and the node exists
     * and the node is not locked
     * and the node has the versionable aspect
     * and the node doesn't have the temporary aspect
     * and the node isn't already being versioned
     * and the auto version property is true
     * when the type set behavour is executed
     * then a new version is created
     */
    @SuppressWarnings("unchecked")
    @Test
    public void createVersion()
    {
        // auto version on
        extendedVersionableAspect.setAutoVersionOnTypeChange(true);

        // node does exists
        when(mockedNodeService.exists(nodeRef))
            .thenReturn(true);

        // node is not locked
        when(mockedLockService.getLockStatus(nodeRef))
            .thenReturn(LockStatus.NO_LOCK);

        // node has the versionable aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            .thenReturn(true);

        // node does not have the temporary aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY))
            .thenReturn(false);

        // node is not being processed for versioning
        when(mockedAlfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS))
            .thenReturn(new HashMap<>(Collections.singletonMap(anotherNodeRef, anotherNodeRef)));

        // auto version false
        when(mockedNodeService.getProperty(nodeRef, ContentModel.PROP_AUTO_VERSION))
            .thenReturn(Boolean.TRUE);

        // execute behaviour
        extendedVersionableAspect.onSetNodeType(nodeRef, oldType, newType);

        // verify other
        verify(mockedNodeService).exists(nodeRef);
        verify(mockedLockService).isLockedAndReadOnly(nodeRef);
        verify(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        verify(mockedNodeService).hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY);
        verify(mockedAlfrescoTransactionSupport, times(2)).getResource(KEY_VERSIONED_NODEREFS);
        verify(mockedNodeService).getProperty(nodeRef, ContentModel.PROP_AUTO_VERSION);

        // assert the version was not created
        verify(mockedVersionService).createVersion(eq(nodeRef), any(Map.class));
    }

}
