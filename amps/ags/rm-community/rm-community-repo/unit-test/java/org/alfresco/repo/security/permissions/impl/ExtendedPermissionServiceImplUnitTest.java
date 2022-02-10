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

package org.alfresco.repo.security.permissions.impl;

import static java.util.Arrays.asList;
import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateText;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.security.permissions.processor.PermissionPostProcessor;
import org.alfresco.repo.security.permissions.processor.PermissionPreProcessor;
import org.alfresco.repo.security.permissions.processor.PermissionProcessorRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Extended permission service implementation unit test
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class ExtendedPermissionServiceImplUnitTest extends BaseUnitTest 
{
    /** test data */
    private static final Long NODE_ACL_ID = 100l;
    private static final Set<String> READERS = Stream.of(generateText(), generateText()).collect(Collectors.toSet());
    private static final Set<String> WRITERS = Stream.of(generateText(), generateText()).collect(Collectors.toSet());
    private static final String OWNER = generateText();
    
    /** permission service impl, default */
	private @InjectMocks @Spy ExtendedPermissionServiceImpl extendedPermissionServiceImpl = new ExtendedPermissionServiceImpl()
	{
		protected AccessStatus hasPermissionImpl(NodeRef nodeRef, String perm) { return AccessStatus.UNDETERMINED; }
    };
	
	/** permission service impl instance extended for reader/writer tests */
    private @InjectMocks ExtendedPermissionServiceImpl extendedPermissionServiceImplWithReaderWritersSet = new ExtendedPermissionServiceImpl()
    {
        protected AccessStatus hasPermissionImpl(NodeRef nodeRef, String perm) { return AccessStatus.UNDETERMINED; }

        public java.util.Set<String> getReaders(Long aclId) { return READERS; }

        public java.util.Set<String> getWriters(Long aclId) { return WRITERS; }
    };
	
    /** mocks */
	private @Mock PermissionProcessorRegistry mockedPermissionProcessorRegistry;
	private @Mock PermissionPreProcessor mockedPermissionPreProcessor;
	private @Mock PermissionPostProcessor mockedPermissionPostProcessor;
	
	/**
	 * Given a permission pre-processor has been registered
	 * And does not DENY
	 * When hasPermission is called
	 * Then the pre-processor is executed
	 * And the ACL's are evaluated as normal
	 */
	@Test
	public void preProcessorDoesNotDeny()
	{
		NodeRef nodeRef = generateCmContent("anyname");
		String perm = AlfMock.generateText();
		when(mockedPermissionProcessorRegistry.getPermissionPreProcessors())
			.thenReturn(asList(mockedPermissionPreProcessor));
		when(mockedPermissionPreProcessor.process(nodeRef, perm))
		    .thenReturn(AccessStatus.UNDETERMINED);
		
		AccessStatus result = extendedPermissionServiceImpl.hasPermission(nodeRef, perm);

		assertEquals(AccessStatus.UNDETERMINED, result);
		verify(mockedPermissionPreProcessor).process(nodeRef, perm);
		verify(extendedPermissionServiceImpl).hasPermissionImpl(nodeRef, perm);
	}
	
	/**
	 * Given a permission pre-processor has been registered
	 * And DENY's
	 * When hasPermission is called
	 * Then the pre-processor is executed
	 * And the remaining permission evaluations do not take place
	 */
	@Test
	public void preProcessorDenys()
	{
		NodeRef nodeRef = generateCmContent("anyname");
		String perm = AlfMock.generateText();
		when(mockedPermissionProcessorRegistry.getPermissionPreProcessors())
			.thenReturn(asList(mockedPermissionPreProcessor));
		when(mockedPermissionPreProcessor.process(nodeRef, perm))
		    .thenReturn(AccessStatus.DENIED);
		
		AccessStatus result = extendedPermissionServiceImpl.hasPermission(nodeRef, perm);

		assertEquals(AccessStatus.DENIED, result);
		verify(mockedPermissionPreProcessor).process(nodeRef, perm);
		verify(extendedPermissionServiceImpl, never()).hasPermissionImpl(nodeRef, perm);
	}

	/**
	 * Given a permission post-processor has been registered
	 * When hasPermission is called
	 * Then the permission post-processor is called
	 */
	@Test
	public void postProcessorRegistered()
	{
		NodeRef nodeRef = generateCmContent("anyname");
		String perm = AlfMock.generateText();
		List<String> configuredReadPermissions = asList("ReadProperties", "ReadChildren");
		List<String> configuredFilePermissions = asList("WriteProperties", "AddChildren");

		extendedPermissionServiceImpl.setConfiguredReadPermissions("ReadProperties,ReadChildren");
		extendedPermissionServiceImpl.setConfiguredFilePermissions("WriteProperties,AddChildren");

		when(mockedPermissionProcessorRegistry.getPermissionPostProcessors())
			.thenReturn(asList(mockedPermissionPostProcessor));
		when(mockedPermissionPostProcessor.process(AccessStatus.UNDETERMINED, nodeRef, perm, configuredReadPermissions, configuredFilePermissions))
	    	.thenReturn(AccessStatus.ALLOWED);
	
		AccessStatus result = extendedPermissionServiceImpl.hasPermission(nodeRef, perm);		

		assertEquals(AccessStatus.ALLOWED, result);
		verify(mockedPermissionPostProcessor).process(AccessStatus.UNDETERMINED, nodeRef, perm, configuredReadPermissions, configuredFilePermissions);
		verify(extendedPermissionServiceImpl).hasPermissionImpl(nodeRef, perm);
	}
	
	/**
	 * Given a node with no owner aspect
	 * When we ask for the readers and writers
	 * Then the owner isn't included in the result
	 */
	@Test
	public void getReadersAndWritersForNodeWithNoOwnerAspect()
	{
	    // setup node acl
	    NodeRef nodeRef = generateNodeRef();
	    when(mockedNodeService.getNodeAclId(nodeRef))
	        .thenReturn(NODE_ACL_ID);
	    
	    // setup owner
	    when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE))
	        .thenReturn(false);
	    
	    // when
	    Pair<Set<String>, Set<String>> result = extendedPermissionServiceImplWithReaderWritersSet.getReadersAndWriters(nodeRef);
	    
	    // then
	    assertEquals(READERS, result.getFirst());
	    assertEquals(WRITERS, result.getSecond());	    
	}
	
	/**
     * Given a node with no owner set
     * When we ask for the readers and writers
     * Then the owner isn't included in the result
     */
    @Test
    public void getReadersAndWritersForNodeWithNoOwnerSet()
    {
        // setup node acl
        NodeRef nodeRef = generateNodeRef();
        when(mockedNodeService.getNodeAclId(nodeRef))
            .thenReturn(NODE_ACL_ID);
        
        // setup owner
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE))
            .thenReturn(true);
        when(mockedOwnableService.getOwner(nodeRef))
            .thenReturn(StringUtils.EMPTY);
        
        // when
        Pair<Set<String>, Set<String>> result = extendedPermissionServiceImplWithReaderWritersSet.getReadersAndWriters(nodeRef);
        
        // then
        assertEquals(READERS, result.getFirst());
        assertEquals(WRITERS, result.getSecond());      
    }
    
    /**
     * Given a node with NO_OWNER value set
     * When we ask for the readers and writers
     * Then the owner isn't included in the result
     */
    @Test
    public void getReadersAndWritersForNodeWithNoOwnerValueSet()
    {
        // setup node acl
        NodeRef nodeRef = generateNodeRef();
        when(mockedNodeService.getNodeAclId(nodeRef))
            .thenReturn(NODE_ACL_ID);
        
        // setup owner
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE))
            .thenReturn(false);
        when(mockedOwnableService.getOwner(nodeRef))
        .thenReturn(OwnableService.NO_OWNER);
        
        // when
        Pair<Set<String>, Set<String>> result = extendedPermissionServiceImplWithReaderWritersSet.getReadersAndWriters(nodeRef);
        
        // then
        assertEquals(READERS, result.getFirst());
        assertEquals(WRITERS, result.getSecond());      
    }
	
	/**
	 * Given a node with an owner
	 * When we ask for the readers and writers
	 * Then the owner is included in the writers set
	 */
	@Test
    public void getReadersAndWritersForNodeWithOwner()
    {
        // setup node acl
        NodeRef nodeRef = generateNodeRef();
        when(mockedNodeService.getNodeAclId(nodeRef))
            .thenReturn(NODE_ACL_ID);
        
        // setup owner
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE))
            .thenReturn(true);
        when(mockedOwnableService.getOwner(nodeRef))
            .thenReturn(OWNER);
        when(mockedAuthorityService.authorityExists(OWNER))
            .thenReturn(true);
        
        // when
        Pair<Set<String>, Set<String>> result = extendedPermissionServiceImplWithReaderWritersSet.getReadersAndWriters(nodeRef);
        
        // then
        assertEquals(READERS, result.getFirst());
        Set<String> writersWithOwner = new HashSet<>(WRITERS);
        writersWithOwner.add(OWNER);
        assertEquals(writersWithOwner, result.getSecond());      
    }
}
