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
package org.alfresco.module.org_alfresco_module_rm.classification.permission;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.util.Triple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.common.collect.Sets;

/**
 * Classification permission pre-processor unit test
 * 
 * @author Roy Wetherall
 * @since 3.0.a
 */
public class ClassificationPermissionPreProcessorUnitTest extends BaseUnitTest 
{
	/** test artifact */
	private @InjectMocks ClassificationPermissionPreProcessor processor;
	
	/** mocks */
	private @Mock ClassificationServiceBootstrap mockedClassificationServiceBootstrap;
	private @Mock ContentClassificationService mockedContentClassificationService;
	
	/** test data */
	private NodeRef nodeRef;
	private String perm = AlfMock.generateText();
	private String user = AlfMock.generateText();
	
	@Before
	@Override
	public void before() throws Exception
	{
		super.before();
		nodeRef = generateCmContent(AlfMock.generateText());
	}
	
	/**
	 * Given that the classification hierarchy hasn't been bootstraped
	 * When the classification permission pre processor is executed
	 * Then an undetermined result will be returned
	 */
	@Test
	public void classificationServiceNotBootstraped()
	{
		when(mockedClassificationServiceBootstrap.isInitialised())
			.thenReturn(false);
		
		assertEquals(AccessStatus.UNDETERMINED, processor.process(nodeRef, perm));
		
		verify(mockedContentClassificationService, never())
			.hasClearance(nodeRef);
	}	

	/**
	 * Given that the node being evaluated is already being processed
	 * When the classification permission pre processor is executed
	 * Then an undetermined result will be returned
	 */
	@Test
	public void nodeBeingProcessed()
	{
		when(mockedClassificationServiceBootstrap.isInitialised())
			.thenReturn(true);
		when(mockedTransactionalResourceHelper.getSet(ClassificationPermissionPreProcessor.KEY_PROCESSING))
			.thenReturn(Sets.newHashSet(nodeRef));
		
		assertEquals(AccessStatus.UNDETERMINED, processor.process(nodeRef, perm));
		
		verify(mockedContentClassificationService, never())
			.hasClearance(nodeRef);
	}	
	
	/**
	 * Given that the node already exists in the transaction cache
	 * When the classification permission pre processor is executed
	 * Then the result will be returned from the cache
	 */
	@Test
	public void resultAlreadyCached()
	{
		when(mockedClassificationServiceBootstrap.isInitialised())
			.thenReturn(true);
		NodeRef notTheNodeRef = AlfMock.generateNodeRef(mockedNodeService);
		when(mockedTransactionalResourceHelper.getSet(ClassificationPermissionPreProcessor.KEY_PROCESSING))
			.thenReturn(Sets.newHashSet(notTheNodeRef));		
		when(mockedAuthenticationUtil.getRunAsUser())
			.thenReturn(user);
		
		Map<Object, Object> cache = new HashMap<Object, Object>(1); 
		cache.put(new Triple<NodeRef, String, String>(nodeRef, perm, user), AccessStatus.ALLOWED);		
		when(mockedTransactionalResourceHelper.getMap(ClassificationPermissionPreProcessor.KEY_CACHE))
			.thenReturn(cache);
		
		assertEquals(AccessStatus.ALLOWED, processor.process(nodeRef, perm));
		
		verify(mockedContentClassificationService, never())
			.hasClearance(nodeRef);
	}
	
	/**
	 * Given that the user does have clearance
	 * When the classification permission pre processor is executed
	 * Then an undetermined result will be returned
	 */
	@Test
	public void userHasClearance()
	{
		when(mockedClassificationServiceBootstrap.isInitialised())
			.thenReturn(true);
		NodeRef notTheNodeRef = AlfMock.generateNodeRef(mockedNodeService);
		when(mockedTransactionalResourceHelper.getSet(ClassificationPermissionPreProcessor.KEY_PROCESSING))
			.thenReturn(Sets.newHashSet(notTheNodeRef));		
		when(mockedAuthenticationUtil.getRunAsUser())
			.thenReturn(user);
		
		Map<Object, Object> cache = new HashMap<Object, Object>(1); 
		cache.put(new Triple<NodeRef, String, String>(notTheNodeRef, perm, user), AccessStatus.ALLOWED);		
		when(mockedTransactionalResourceHelper.getMap(ClassificationPermissionPreProcessor.KEY_CACHE))
			.thenReturn(cache);
		
		when(mockedContentClassificationService.hasClearance(nodeRef))
			.thenReturn(true);
		
		assertEquals(AccessStatus.UNDETERMINED, processor.process(nodeRef, perm));
		
		verify(mockedContentClassificationService)
			.hasClearance(nodeRef);
	}
	
	/**
	 * Given that the user doesn't have clearance
	 * When the classification permission pre processor is executed
	 * Then an undetermined result will be returned
	 */
	@Test
	public void userDoesNotHaveClearance()
	{
		when(mockedClassificationServiceBootstrap.isInitialised())
			.thenReturn(true);
		NodeRef notTheNodeRef = AlfMock.generateNodeRef(mockedNodeService);
		when(mockedTransactionalResourceHelper.getSet(ClassificationPermissionPreProcessor.KEY_PROCESSING))
			.thenReturn(Sets.newHashSet(notTheNodeRef));		
		when(mockedAuthenticationUtil.getRunAsUser())
			.thenReturn(user);
		
		Map<Object, Object> cache = new HashMap<Object, Object>(1); 
		cache.put(new Triple<NodeRef, String, String>(notTheNodeRef, perm, user), AccessStatus.ALLOWED);		
		when(mockedTransactionalResourceHelper.getMap(ClassificationPermissionPreProcessor.KEY_CACHE))
			.thenReturn(cache);
		
		when(mockedContentClassificationService.hasClearance(nodeRef))
			.thenReturn(false);
		
		assertEquals(AccessStatus.DENIED, processor.process(nodeRef, perm));
		
		verify(mockedContentClassificationService)
			.hasClearance(nodeRef);
	}
}
