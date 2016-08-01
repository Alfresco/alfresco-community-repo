/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.security;

import static org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityServiceImpl.READER_GROUP_PREFIX;
import static org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityServiceImpl.ROOT_IPR_GROUP;
import static org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityServiceImpl.WRITER_GROUP_PREFIX;
import static org.alfresco.service.cmr.security.PermissionService.GROUP_PREFIX;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.repo.security.authority.RMAuthority;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Extended security service implementation unit test.
 * 
 * @author Roy Wetherall
 * @since 2.5
 */
public class ExtendedSecurityServiceImplUnitTest
{
    /** service mocks*/
    @Mock private FilePlanService mockedFilePlanService;
    @Mock private FilePlanRoleService mockedFilePlanRoleService;
    @Mock private AuthorityService mockedAuthorityService;
    @Mock private PermissionService mockedPermissionService;
    @Mock private TransactionService mockedTransactionService;
    @Mock private RetryingTransactionHelper mockedRetryingTransactionHelper;
    @Mock private NodeService mockedNodeService;
    
    /** test component */
    @InjectMocks private ExtendedSecurityServiceImpl extendedSecurityService;
    
    /** read/write group full names */
    private static final String READER_GROUP_FULL_NAME = GROUP_PREFIX + READER_GROUP_PREFIX;
    private static final String WRITER_GROUP_FULL_NAME = GROUP_PREFIX + WRITER_GROUP_PREFIX;
    
    /** test authorities */
    private static final String USER = AlfMock.generateText();
    private static final String GROUP = GROUP_PREFIX + AlfMock.generateText();
    
    /** has extended security permission set */
    private static final Set<AccessPermission> HAS_EXTENDED_SECURITY = Stream
                .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, READER_GROUP_FULL_NAME, 0),
                    new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
                    new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, WRITER_GROUP_FULL_NAME, 2))
                .collect(Collectors.toSet());
    
    /** has no extended security permission set */
    private static final Set<AccessPermission> HAS_NO_EXTENDED_SECURITY = Stream
                .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 0),
                    new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
                    new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 2))
                .collect(Collectors.toSet());
    
    /** test data */
    private NodeRef nodeRef;
    
    /**
     * Before tests
     */
    @Before public void before()
    {
        // initialise mocks 
        MockitoAnnotations.initMocks(this);
        
        // setup nodeRef
        nodeRef = AlfMock.generateNodeRef(mockedNodeService);
    }
    
    /**
     * Helper to setup retrying transaction helper
     */
    @SuppressWarnings("unchecked")
    private void setupRetryingTransactionHelper()
    {
        // setup retrying transaction helper
        Answer<Object> doInTransactionAnswer = new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                RetryingTransactionCallback callback = (RetryingTransactionCallback)invocation.getArguments()[0];
                return callback.execute();
            }
        };
        doAnswer(doInTransactionAnswer)
            .when(mockedRetryingTransactionHelper)
            .<Object>doInTransaction(any(RetryingTransactionCallback.class));        
        when(mockedTransactionService.getRetryingTransactionHelper())
            .thenReturn(mockedRetryingTransactionHelper);
    }
    
    /**
     * Given that the root authority does not exist
     * When the application context is refreshed
     * Then the root authority is created
     */
    @Test public void rootAuthorityDoesNotExist()
    {
        // setup retrying transaction helper       
        setupRetryingTransactionHelper();
        
        // group doesn't exist
        when(mockedAuthorityService.authorityExists(GROUP_PREFIX + ExtendedSecurityServiceImpl.ROOT_IPR_GROUP))
            .thenReturn(false);
        
        // refresh context
        extendedSecurityService.onApplicationEvent(mock(ContextRefreshedEvent.class));
        
        // verify group is created
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, ROOT_IPR_GROUP, ROOT_IPR_GROUP, Collections.singleton(RMAuthority.ZONE_APP_RM));
    }
    
    /**
     * Given that the root authority exists
     * When the application context is refreshed
     * Then nothing happens
     */
    @Test public void rootAuthorityDoesExist()
    {
        // setup retrying transaction helper       
        setupRetryingTransactionHelper();
        
        // group doesn't exist
        when(mockedAuthorityService.authorityExists(GROUP_PREFIX + ROOT_IPR_GROUP))
            .thenReturn(true);
        
        // refresh context
        extendedSecurityService.onApplicationEvent(mock(ContextRefreshedEvent.class));
        
        // verify group is created
        verify(mockedAuthorityService, never()).createAuthority(AuthorityType.GROUP, ROOT_IPR_GROUP, ROOT_IPR_GROUP, Collections.singleton(RMAuthority.ZONE_APP_RM));
    }
    
    /**
     * Given that an IPR read group has read on a node
     * And that an IPR write group has filling on a node
     * When checking for the existence of extended permissions on that node
     * Then it will be confirmed
     */
    @Test public void hasExtendedSecurityWithReadAndWriteGroups()
    {
        // setup permissions
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(HAS_EXTENDED_SECURITY);
        
        // has extended security
        assertTrue(extendedSecurityService.hasExtendedSecurity(nodeRef));        
    }

    /**
     * Given that there is no IPR read group has read on a node
     * When checking for the existence of extended permissions on that node
     * Then it will be denied
     */
    @Test public void hasExtendedSecurityWithNoReadGroup()
    {
        // setup permissions
        Set<AccessPermission> permissions =  Stream
           .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 0),
               new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
               new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, GROUP_PREFIX + WRITER_GROUP_PREFIX, 2))
           .collect(Collectors.toSet());
        
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(permissions);
        
        // has extended security
        assertFalse(extendedSecurityService.hasExtendedSecurity(nodeRef));        
    }
    
    /**
     * Given that there is no IPR write group has read on a node
     * When checking for the existence of extended permissions on that node
     * Then it will be denied
     */
    @Test public void hasExtendedSecurityWithNoWriteGroup()
    {
        // setup permissions
        Set<AccessPermission> permissions =  Stream
           .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, GROUP_PREFIX + READER_GROUP_PREFIX, 0),
               new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
               new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 2))
           .collect(Collectors.toSet());
        
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(permissions);
        
        // has extended security
        assertFalse(extendedSecurityService.hasExtendedSecurity(nodeRef));        
    }
    
    /**
     * Given that an IPR read group has no groups assigned permission
     * When checking for the existence of extended permissions on that node
     * Then it will be denied
     */
    @Test public void hasExtendedSecurityWithNoAssignedGroups()
    {
        // setup permissions
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(HAS_NO_EXTENDED_SECURITY);
        
        // has extended security
        assertFalse(extendedSecurityService.hasExtendedSecurity(nodeRef));        
    }
    
    /**
     * Given that there are no IPR groups assigned
     * When I try and get the extended readers
     * The I will get an empty set
     */
    @Test public void getExtendedReadersNoIPRGoupsAssigned()
    {
        // setup permissions
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(HAS_NO_EXTENDED_SECURITY);
        
        // get extended readers
        assertTrue(extendedSecurityService.getExtendedReaders(nodeRef).isEmpty());
    }
    
    /**
     * Given that there are IPR groups assigned
     * When I try and get the extended readers
     * The I will get the set of readers
     */
    @Test public void getExtendedReaders()
    {
        // setup permissions
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(HAS_EXTENDED_SECURITY);
        
        when(mockedAuthorityService.getContainedAuthorities(null, READER_GROUP_FULL_NAME, true))
            .thenReturn(Stream
               .of(USER, GROUP, WRITER_GROUP_FULL_NAME)
               .collect(Collectors.toSet()));
        
        // get extended readers
        Set<String> extendedReaders = extendedSecurityService.getExtendedReaders(nodeRef);        
        assertEquals(Stream
                       .of(USER, GROUP)
                       .collect(Collectors.toSet()),
                     extendedReaders);
    }
    
    /**
     * Given that there are no IPR groups assigned
     * When I try and get the extended writers
     * The I will get an empty set
     */
    
    /**
     * Given that there are IPR groups assigned
     * When I try and get the extended writers
     * The I will get the set of writers
     */
    
    // TODO add AC for adding extended security marks
    
    // TODO add AC for removing extended security marks
}
