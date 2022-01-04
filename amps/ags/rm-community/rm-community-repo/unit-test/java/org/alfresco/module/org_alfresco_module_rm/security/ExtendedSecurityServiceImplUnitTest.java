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
package org.alfresco.module.org_alfresco_module_rm.security;

import static org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityServiceImpl.READER_GROUP_PREFIX;
import static org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityServiceImpl.ROOT_IPR_GROUP;
import static org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityServiceImpl.WRITER_GROUP_PREFIX;
import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateText;
import static org.alfresco.service.cmr.security.PermissionService.GROUP_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authority.RMAuthority;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Extended security service implementation unit test.
 * 
 * @author Roy Wetherall
 * @since 2.5
 */
@RunWith(MockitoJUnitRunner.class)
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
    @Mock private PagingResults<String> mockedReadPagingResults;
    @Mock private PagingResults<String> mockedWritePagingResults;
    @Mock private ApplicationContext mockedApplicationContext;
    @Mock private ChildAssociationRef mockedChildAssociationRef;
    
    /** test component */
    @InjectMocks private ExtendedSecurityServiceImpl extendedSecurityService;
    
    /** read/write group full names */
    private static final String READER_GROUP_FULL_PREFIX = GROUP_PREFIX + READER_GROUP_PREFIX;
    private static final String WRITER_GROUP_FULL_PREFIX = GROUP_PREFIX + WRITER_GROUP_PREFIX;
    
    /** test authorities */
    private static final String USER = "USER";
    private static final String GROUP = GROUP_PREFIX + "GROUP";
    private static final String USER_W = "USER_W";
    private static final String GROUP_W = GROUP_PREFIX + "GROUP_W";
    private static final Set<String> READERS = Stream.of(USER, GROUP).collect(Collectors.toSet());
    private static final Set<String> WRITERS = Stream.of(USER_W, GROUP_W).collect(Collectors.toSet());
    
    /** has extended security permission set */
    private static final Set<AccessPermission> HAS_EXTENDED_SECURITY = Stream
                .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, READER_GROUP_FULL_PREFIX, 0),
                    new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
                    new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, WRITER_GROUP_FULL_PREFIX, 2))
                .collect(Collectors.toSet());
    
    /** has no extended security permission set */
    private static final Set<AccessPermission> HAS_NO_EXTENDED_SECURITY = Stream
                .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 0),
                    new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
                    new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 2))
                .collect(Collectors.toSet());
    
    /** test data */
    private NodeRef nodeRef;
    private NodeRef filePlan;
    private String readGroupPrefix;
    private String writeGroupPrefix;
    
    /**
     * Before tests
     */
    @SuppressWarnings("unchecked")
    @Before public void before()
    {
        // setup node
        nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        
        // setup file plan
        filePlan = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getFilePlan(any(NodeRef.class)))
            .thenReturn(filePlan);
        
        // set-up application context
        lenient().when(mockedApplicationContext.getBean("dbNodeService"))
            .thenReturn(mockedNodeService);

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
        
        // setup create authority
        Answer<String> createAuthorityAnswer = new Answer<String>()
        {
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                return PermissionService.GROUP_PREFIX + (String)invocation.getArguments()[1];
            }

        };
        when(mockedAuthorityService.createAuthority(any(AuthorityType.class), anyString(), anyString(), anySet()))
            .thenAnswer(createAuthorityAnswer);
        
        // setup group prefixes
        readGroupPrefix = extendedSecurityService.getIPRGroupPrefixShortName(READER_GROUP_PREFIX, READERS);
        writeGroupPrefix = extendedSecurityService.getIPRGroupPrefixShortName(WRITER_GROUP_PREFIX, WRITERS);
        
        // make sure the users and groups exist
        Stream
            .of(USER, USER_W, GROUP, GROUP_W)
            .forEach((a) -> 
               when(mockedAuthorityService.authorityExists(a))
                   .thenReturn(true));
        extendedSecurityService.setNodeService(mockedNodeService);
    }
    
    /**
     * Given that the root authority does not exist
     * When the application context is refreshed
     * Then the root authority is created
     */
    @Test public void rootAuthorityDoesNotExist()
    {
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
    @Test public void getExtendedReadersNoIPRGroupsAssigned()
    {
        // setup permissions
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(HAS_NO_EXTENDED_SECURITY);
        
        // get extended readers
        assertTrue(extendedSecurityService.getReaders(nodeRef).isEmpty());
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
        
        when(mockedAuthorityService.getContainedAuthorities(null, READER_GROUP_FULL_PREFIX, true))
            .thenReturn(Stream
               .of(USER, GROUP)
               .collect(Collectors.toSet()));
        
        // get extended readers
        Set<String> extendedReaders = extendedSecurityService.getReaders(nodeRef);        
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
    @Test public void getExtendedWritersNoIPRGroupsAssigned()
    {
        // setup permissions
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(HAS_NO_EXTENDED_SECURITY);
        
        // get extended writers
        assertTrue(extendedSecurityService.getWriters(nodeRef).isEmpty());
    }
    
    /**
     * Given that there are IPR groups assigned
     * When I try and get the extended writers
     * The I will get the set of writers
     */
    @Test public void getExtendedWriters()
    {
        // setup permissions
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(HAS_EXTENDED_SECURITY);
        
        when(mockedAuthorityService.getContainedAuthorities(null, WRITER_GROUP_FULL_PREFIX, true))
            .thenReturn(Stream
               .of(USER, GROUP)
               .collect(Collectors.toSet()));
        
        // get extended writers
        Set<String> extendedWriters = extendedSecurityService.getWriters(nodeRef);        
        assertEquals(Stream
                       .of(USER, GROUP)
                       .collect(Collectors.toSet()),
                       extendedWriters);
    }
    
    /**
     * Given a node with no previous IPR groups assigned
     * And no IPR group matching authorities
     * When I add some read and write authorities
     * Then new IPR groups are created
     * And they are assigned to the node
     * And they are added to the RM roles
     */
    @Test public void addExtendedSecurityForTheFirstTimeAndCreateGroups()
    {
        // group names
        String readGroup = extendedSecurityService.getIPRGroupShortName(READER_GROUP_PREFIX, READERS, 0);
        String writeGroup = extendedSecurityService.getIPRGroupShortName(WRITER_GROUP_PREFIX, WRITERS, 0);
        
        // setup query results
        when(mockedReadPagingResults.getPage())
            .thenReturn(Collections.emptyList());
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    any(String.class),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
            .thenReturn(mockedReadPagingResults);
        
        // add extended security
        extendedSecurityService.set(nodeRef, READERS, WRITERS);
        
        // verify no old permissions needing to be cleared
        verify(mockedPermissionService, never()).clearPermission(eq(nodeRef), anyString());
        
        // verify read group created correctly
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, readGroup, readGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        readGroup = GROUP_PREFIX + readGroup;
        verify(mockedAuthorityService).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, readGroup);
        verify(mockedAuthorityService).addAuthority(readGroup, USER);
        verify(mockedAuthorityService).addAuthority(readGroup, GROUP);
        
        // verify write group created correctly
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, writeGroup, writeGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        writeGroup = GROUP_PREFIX + writeGroup;
        verify(mockedAuthorityService).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, writeGroup);
        verify(mockedAuthorityService).addAuthority(writeGroup, USER_W);
        verify(mockedAuthorityService).addAuthority(writeGroup, GROUP_W);

        // verify groups assigned to RM roles
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS, readGroup);
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS, writeGroup);
        
        // verify permissions are assigned to node
        verify(mockedPermissionService).setPermission(nodeRef, readGroup, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService).setPermission(nodeRef, writeGroup, RMPermissionModel.FILING, true);
    }
    
    /**
     * Given a node with no previous IPR groups assigned
     * And existing IPR groups matching
     * When I add some read and write authorities
     * Then the existing IPR groups are used
     * And they are assigned to the node
     * And do not not need to be re-added to the RM roles
     */
    @Test public void addExtendedSecurityForTheFirstTimeAndReuseGroups()
    {
        // group names        
        String readGroup = readGroupPrefix + "0";
        String writeGroup = writeGroupPrefix + "0";
        
        // setup query results
        when(mockedReadPagingResults.getPage())
            .thenReturn(Stream.of(GROUP_PREFIX + readGroup).collect(Collectors.toList()));
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    eq(readGroupPrefix),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
            .thenReturn(mockedReadPagingResults);
        
        when(mockedWritePagingResults.getPage())
            .thenReturn(Stream.of(GROUP_PREFIX + writeGroup).collect(Collectors.toList()));
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    eq(writeGroupPrefix),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
        .thenReturn(mockedWritePagingResults);
        
        // setup exact match
        lenient().when(mockedAuthorityService.authorityExists(GROUP_PREFIX + writeGroup))
            .thenReturn(true);
        when(mockedAuthorityService.getContainedAuthorities(null, GROUP_PREFIX + readGroup, true))
            .thenReturn(Stream
                .of(USER, GROUP)
                .collect(Collectors.toSet()));
        when(mockedAuthorityService.getContainedAuthorities(null, GROUP_PREFIX + writeGroup, true))
            .thenReturn(Stream
                .of(USER_W, GROUP_W)
                .collect(Collectors.toSet()));
        
        // add extended security
        extendedSecurityService.set(nodeRef, READERS, WRITERS);
        
        // verify no old permissions needing to be cleared
        verify(mockedPermissionService, never()).clearPermission(eq(nodeRef), anyString());
        
        // verify read group is not recreated
        verify(mockedAuthorityService, never()).createAuthority(AuthorityType.GROUP, readGroup, readGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        readGroup = GROUP_PREFIX + readGroup;
        verify(mockedAuthorityService, never()).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, readGroup);
        verify(mockedAuthorityService, never()).addAuthority(readGroup, USER);
        verify(mockedAuthorityService, never()).addAuthority(readGroup, GROUP);
        
        // verify write group is not recreated
        verify(mockedAuthorityService, never()).createAuthority(AuthorityType.GROUP, writeGroup, writeGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        writeGroup = GROUP_PREFIX + writeGroup;
        verify(mockedAuthorityService, never()).addAuthority(readGroup, writeGroup);
        verify(mockedAuthorityService, never()).addAuthority(writeGroup, USER_W);
        verify(mockedAuthorityService, never()).addAuthority(writeGroup, GROUP_W);

        // verify groups assigned to RM roles
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS, readGroup);
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS, writeGroup);
        
        // verify permissions are assigned to node
        verify(mockedPermissionService).setPermission(nodeRef, readGroup, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService).setPermission(nodeRef, writeGroup, RMPermissionModel.FILING, true);
        
    }
    
    /**
     * Given a node with no previous IPR groups assigned
     * And existing IPR groups matches existing has, but not exact match
     * When I add some read and write authorities
     * Then new groups are created
     * And they are assigned to the node
     * And added to the RM roles
     */
    @Test public void addExtendedSecurityForTheFirstTimeAndCreateGroupsAfterClash()
    {
        // group names        
        String readGroup = readGroupPrefix + "0";
        String writeGroup = writeGroupPrefix + "0";
        
        // setup query results
        when(mockedReadPagingResults.getPage())
            .thenReturn(Stream.of(GROUP_PREFIX + readGroup).collect(Collectors.toList()));
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    eq(readGroupPrefix),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
            .thenReturn(mockedReadPagingResults);
        
        when(mockedWritePagingResults.getPage())
            .thenReturn(Stream.of(GROUP_PREFIX + writeGroup).collect(Collectors.toList()));
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    eq(writeGroupPrefix),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
        .thenReturn(mockedWritePagingResults);
        
        // setup exact match
        lenient().when(mockedAuthorityService.authorityExists(GROUP_PREFIX + writeGroup))
            .thenReturn(true);
        when(mockedAuthorityService.getContainedAuthorities(null, GROUP_PREFIX + readGroup, true))
            .thenReturn(Stream
                .of(USER, GROUP, AlfMock.generateText())
                .collect(Collectors.toSet()));
        when(mockedAuthorityService.getContainedAuthorities(null, GROUP_PREFIX + writeGroup, true))
            .thenReturn(Stream
                .of(USER_W, AlfMock.generateText())
                .collect(Collectors.toSet()));
        
        // add extended security
        extendedSecurityService.set(nodeRef, READERS, WRITERS);
        
        // verify no old permissions needing to be cleared
        verify(mockedPermissionService, never()).clearPermission(eq(nodeRef), anyString());
        
        // new group names
        readGroup = extendedSecurityService.getIPRGroupShortName(READER_GROUP_PREFIX, READERS, 1);
        writeGroup = extendedSecurityService.getIPRGroupShortName(WRITER_GROUP_PREFIX, WRITERS, 1);
        
        // verify read group created correctly
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, readGroup, readGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        readGroup = GROUP_PREFIX + readGroup;
        verify(mockedAuthorityService).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, readGroup);
        verify(mockedAuthorityService).addAuthority(readGroup, USER);
        verify(mockedAuthorityService).addAuthority(readGroup, GROUP);
        
        // verify write group created correctly
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, writeGroup, writeGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        writeGroup = GROUP_PREFIX + writeGroup;
        verify(mockedAuthorityService).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, writeGroup);
        verify(mockedAuthorityService).addAuthority(writeGroup, USER_W);
        verify(mockedAuthorityService).addAuthority(writeGroup, GROUP_W);

        // verify groups assigned to RM roles
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS, readGroup);
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS, writeGroup);
        
        // verify permissions are assigned to node
        verify(mockedPermissionService).setPermission(nodeRef, readGroup, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService).setPermission(nodeRef, writeGroup, RMPermissionModel.FILING, true);
    }
    
    /**
     * Given a node with no previous IPR groups assigned
     * And existing IPR groups matching, but found on second page of find results
     * When I add some read and write authorities
     * Then the existing IPR groups are used
     * And they are assigned to the node
     * And do not not need to be re-added to the RM roles
     */
    @Test public void addExtendedSecurityWithResultPaging()
    {
        // group names        
        String readGroup = readGroupPrefix + "0";
        String writeGroup = writeGroupPrefix + "0";
        
        // create fity results
        List<String> fiftyResults = new ArrayList<>(50);
        for (int i = 0; i < 50; i++)
        {
            fiftyResults.add(AlfMock.generateText());
        }        
        
        // setup query results
        when(mockedReadPagingResults.getPage())
            .thenReturn(fiftyResults)
            .thenReturn(Stream.of(GROUP_PREFIX + readGroup).collect(Collectors.toList()));
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    eq(readGroupPrefix),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
            .thenReturn(mockedReadPagingResults);
        
        when(mockedWritePagingResults.getPage())
            .thenReturn(fiftyResults)
            .thenReturn(Stream.of(GROUP_PREFIX + writeGroup).collect(Collectors.toList()));
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    eq(writeGroupPrefix),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
        .thenReturn(mockedWritePagingResults);

        // setup exact match
        lenient().when(mockedAuthorityService.authorityExists(GROUP_PREFIX + writeGroup))
            .thenReturn(true);
        when(mockedAuthorityService.getContainedAuthorities(null, GROUP_PREFIX + readGroup, true))
            .thenReturn(Stream
                .of(USER, GROUP)
                .collect(Collectors.toSet()));
        when(mockedAuthorityService.getContainedAuthorities(null, GROUP_PREFIX + writeGroup, true))
            .thenReturn(Stream
                .of(USER_W, GROUP_W)
                .collect(Collectors.toSet()));
        
        // add extended security
        extendedSecurityService.set(nodeRef, READERS, WRITERS);
        
        // verify no old permissions needing to be cleared
        verify(mockedPermissionService, never()).clearPermission(eq(nodeRef), anyString());
        
        // verify read group is not recreated
        verify(mockedAuthorityService, never()).createAuthority(AuthorityType.GROUP, readGroup, readGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        readGroup = GROUP_PREFIX + readGroup;
        verify(mockedAuthorityService, never()).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, readGroup);
        verify(mockedAuthorityService, never()).addAuthority(readGroup, USER);
        verify(mockedAuthorityService, never()).addAuthority(readGroup, GROUP);
        
        // verify write group is not recreated
        verify(mockedAuthorityService, never()).createAuthority(AuthorityType.GROUP, writeGroup, writeGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        writeGroup = GROUP_PREFIX + writeGroup;
        verify(mockedAuthorityService, never()).addAuthority(readGroup, writeGroup);
        verify(mockedAuthorityService, never()).addAuthority(writeGroup, USER_W);
        verify(mockedAuthorityService, never()).addAuthority(writeGroup, GROUP_W);

        // verify groups assigned to RM roles
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS, readGroup);
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS, writeGroup);
        
        // verify permissions are assigned to node
        verify(mockedPermissionService).setPermission(nodeRef, readGroup, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService).setPermission(nodeRef, writeGroup, RMPermissionModel.FILING, true);
        
    }
    
    /**
     * Given that a node already has extended security
     * When I add extended security
     * Then the existing extended security is replaced with the new extended security
     */
    @Test public void addExtendedSecurityToNodeWithExtendedSecurity()
    {
        // group names
        String readGroup = extendedSecurityService.getIPRGroupShortName(READER_GROUP_FULL_PREFIX, READERS, 0);
        String writeGroup = extendedSecurityService.getIPRGroupShortName(WRITER_GROUP_FULL_PREFIX, WRITERS, 0);
        
        // setup permissions
        Set<AccessPermission> permissions = Stream
            .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, readGroup, 0),
                new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
                new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, writeGroup, 2))
            .collect(Collectors.toSet());        
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(permissions);
        
        // set revised reader and writers
        String user = generateText();
        String group = generateText();
        String userW = generateText();
        String groupW = generateText();
        Set<String> newReaders = Stream.of(user, group).collect(Collectors.toSet());
        Set<String> newWriters = Stream.of(userW, groupW).collect(Collectors.toSet());        
        
        // new group names
        String newReadGroup = extendedSecurityService.getIPRGroupShortName(READER_GROUP_PREFIX, newReaders, 0);
        String newWriteGroup = extendedSecurityService.getIPRGroupShortName(WRITER_GROUP_PREFIX, newWriters, 0);
        
        // make sure users and groups exist
        Stream
            .of(user, group, userW, groupW)
            .forEach((a) -> 
               when(mockedAuthorityService.authorityExists(a))
                   .thenReturn(true));
        
        // setup query results for no group matches
        when(mockedReadPagingResults.getPage())
            .thenReturn(Collections.emptyList());
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    any(String.class),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
            .thenReturn(mockedReadPagingResults);
        
        // set extended security
        extendedSecurityService.set(nodeRef, newReaders, newWriters);
        
        // verify that the old permissions are cleared
        verify(mockedPermissionService).clearPermission(nodeRef, readGroup);
        verify(mockedPermissionService).clearPermission(nodeRef, writeGroup);
        
        // verify read group created correctly
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, newReadGroup, newReadGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        newReadGroup = GROUP_PREFIX + newReadGroup;
        verify(mockedAuthorityService).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, newReadGroup);
        verify(mockedAuthorityService).addAuthority(newReadGroup, user);
        verify(mockedAuthorityService).addAuthority(newReadGroup, group);
        
        // verify write group created correctly
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, newWriteGroup, newWriteGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        newWriteGroup = GROUP_PREFIX + newWriteGroup;
        verify(mockedAuthorityService).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, newWriteGroup);
        verify(mockedAuthorityService).addAuthority(newWriteGroup, userW);
        verify(mockedAuthorityService).addAuthority(newWriteGroup, groupW);

        // verify groups assigned to RM roles
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS, newReadGroup);
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS, newWriteGroup);
        
        // verify permissions are assigned to node
        verify(mockedPermissionService).setPermission(nodeRef, newReadGroup, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService).setPermission(nodeRef, newWriteGroup, RMPermissionModel.FILING, true);
        
    }
    
    /**
     * Given that a node has renditions
     * When I add extended security
     * Then they are applied to the renditions
     */
    @Test public void extendedSecurityAddedToRenditions()
    {
        // group names
        String readGroup = extendedSecurityService.getIPRGroupShortName(READER_GROUP_PREFIX, READERS, 0);
        String writeGroup = extendedSecurityService.getIPRGroupShortName(WRITER_GROUP_PREFIX, WRITERS, 0);
        
        // setup query results
        when(mockedReadPagingResults.getPage())
            .thenReturn(Collections.emptyList());
        when(mockedAuthorityService.getAuthorities(
                    eq(AuthorityType.GROUP), 
                    eq(RMAuthority.ZONE_APP_RM), 
                    any(String.class),
                    eq(false), 
                    eq(false), 
                    any(PagingRequest.class)))
            .thenReturn(mockedReadPagingResults);
        
        // setup renditions
        NodeRef renditionNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_RECORD))
            .thenReturn(true);
        when(mockedChildAssociationRef.getChildRef())
            .thenReturn(renditionNodeRef);
        when(mockedNodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL))
            .thenReturn(Collections.singletonList(mockedChildAssociationRef));
        
        // add extended security
        extendedSecurityService.set(nodeRef, READERS, WRITERS);
        
        // verify no old permissions needing to be cleared
        verify(mockedPermissionService, never()).clearPermission(eq(nodeRef), anyString());
        
        // verify read group created correctly
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, readGroup, readGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        readGroup = GROUP_PREFIX + readGroup;
        verify(mockedAuthorityService).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, readGroup);
        verify(mockedAuthorityService).addAuthority(readGroup, USER);
        verify(mockedAuthorityService).addAuthority(readGroup, GROUP);
        
        // verify write group created correctly
        verify(mockedAuthorityService).createAuthority(AuthorityType.GROUP, writeGroup, writeGroup, Collections.singleton(RMAuthority.ZONE_APP_RM));
        writeGroup = GROUP_PREFIX + writeGroup;
        verify(mockedAuthorityService).addAuthority(GROUP_PREFIX + ROOT_IPR_GROUP, writeGroup);
        verify(mockedAuthorityService).addAuthority(writeGroup, USER_W);
        verify(mockedAuthorityService).addAuthority(writeGroup, GROUP_W);

        // verify groups assigned to RM roles
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS, readGroup);
        verify(mockedFilePlanRoleService).assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS, writeGroup);
        
        // verify permissions are assigned to node
        verify(mockedPermissionService).setPermission(nodeRef, readGroup, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService).setPermission(nodeRef, writeGroup, RMPermissionModel.FILING, true);
        
        // verify permissions are assigned to the rendition
        verify(mockedPermissionService).setPermission(renditionNodeRef, readGroup, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService).setPermission(renditionNodeRef, writeGroup, RMPermissionModel.FILING, true);
    }
    
    /**
     * Given that a node has extended security
     * When I remove the extended security
     * Then the inplace groups permissions are removed
     */
    @Test public void removeAllExtendedSecurity()
    {
        // group names
        String readGroup = extendedSecurityService.getIPRGroupShortName(READER_GROUP_FULL_PREFIX, READERS, 0);
        String writeGroup = extendedSecurityService.getIPRGroupShortName(WRITER_GROUP_FULL_PREFIX, WRITERS, 0);
        
        // setup permissions
        Set<AccessPermission> permissions = Stream
            .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, readGroup, 0),
                new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
                new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, writeGroup, 2))
            .collect(Collectors.toSet());        
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(permissions);
        
        // remove extended security
        extendedSecurityService.remove(nodeRef);
        
        // verify that the groups permissions have been removed
        verify(mockedPermissionService).clearPermission(nodeRef, readGroup);
        verify(mockedPermissionService).clearPermission(nodeRef, writeGroup);
    }
    
    /**
     * Given that a node has no extended security
     * When I remove the extended security
     * Then nothing happens
     */
    @Test public void noExtendedSecurityToRemove()
    {
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(HAS_NO_EXTENDED_SECURITY);
        
        // remove extended security
        extendedSecurityService.remove(nodeRef);
        
        // verify that the groups permissions have been removed
        verify(mockedPermissionService, never()).clearPermission(eq(nodeRef), anyString());
    }
    
    /**
     * Given that node has renditions
     * When I remove the extended security for a node
     * Then the extended security is also removed from the renditions
     */
    @Test public void removeExtendedSecurityFromRenditions()
    {
        // group names
        String readGroup = extendedSecurityService.getIPRGroupShortName(READER_GROUP_FULL_PREFIX, READERS, 0);
        String writeGroup = extendedSecurityService.getIPRGroupShortName(WRITER_GROUP_FULL_PREFIX, WRITERS, 0);
        
        // setup renditions
        NodeRef renditionNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_RECORD))
            .thenReturn(true);
        when(mockedChildAssociationRef.getChildRef())
            .thenReturn(renditionNodeRef);
        when(mockedNodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL))
            .thenReturn(Collections.singletonList(mockedChildAssociationRef));        
        
        // setup permissions
        Set<AccessPermission> permissions = Stream
            .of(new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, readGroup, 0),
                new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, AlfMock.generateText(), 1),
                new AccessPermissionImpl(AlfMock.generateText(), AccessStatus.ALLOWED, writeGroup, 2))
            .collect(Collectors.toSet());        
        when(mockedPermissionService.getAllSetPermissions(nodeRef))
            .thenReturn(permissions);      
        
        // remove extended security
        extendedSecurityService.remove(nodeRef);
        
        // verify that the groups permissions have been removed
        verify(mockedPermissionService).clearPermission(nodeRef, readGroup);
        verify(mockedPermissionService).clearPermission(nodeRef, writeGroup);
        
        // verify that the groups permissions have been removed from the rendition
        verify(mockedPermissionService).clearPermission(renditionNodeRef, readGroup);
        verify(mockedPermissionService).clearPermission(renditionNodeRef, writeGroup);
        
    }
}
