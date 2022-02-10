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

package org.alfresco.module.org_alfresco_module_rm.capability;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.SearchParameters;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Unit tests for {@link RMAfterInvocationProvider}. */
public class RMAfterInvocationProviderUnitTest
{
	private static final NodeRef NODE_A = new NodeRef("test://node/a");

	/** The class under test. */
	@InjectMocks
	private RMAfterInvocationProvider rmAfterInvocationProvider;
	@Mock
	private Authentication authentication;
	@Mock
	Object object;
	@Mock
	ConfigAttributeDefinition config;
	@Mock
	AuthenticationUtil authenticationUtil;
	@Mock
	NodeService nodeService;
	@Mock
	ChildAssociationRef childAssocRefA;

	/** Set up the mocks and common test data. */
	@Before
	public void setUp()
	{
		initMocks(this);

		// Set up the nodes and associations.
		when(nodeService.exists(NODE_A)).thenReturn(true);
		when(childAssocRefA.getParentRef()).thenReturn(NODE_A);

		// Create the config object for use by the tests.
		ConfigAttribute configAttribute = mock(ConfigAttribute.class);
		when(configAttribute.getAttribute()).thenReturn("AFTER_RM.test");
		List<ConfigAttribute> configAttributes = asList(configAttribute);
		when(config.getConfigAttributes()).thenReturn(configAttributes.iterator());
	}

	/** Check that when all the results fit into a page then we get a response of "UNLIMITED". */
	@Test
	public void testDecide_resultSet_unlimited()
	{
		// The returned object is a search result set.
		ResultSet returnedObject = mock(ResultSet.class);
		ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
		when(returnedObject.getResultSetMetaData()).thenReturn(resultSetMetaData);

		// Simulate a single result, and the user has access to it.
		when(returnedObject.length()).thenReturn(1);
		when(returnedObject.getNumberFound()).thenReturn(1L);
		when(returnedObject.getNodeRef(0)).thenReturn(NODE_A);
		when(returnedObject.getChildAssocRef(0)).thenReturn(childAssocRefA);

		// Set the page size to 1 and skip count to 0.
		SearchParameters searchParameters = mock(SearchParameters.class);
		when(searchParameters.getMaxItems()).thenReturn(1);
		when(searchParameters.getSkipCount()).thenReturn(0);
		when(searchParameters.getLanguage()).thenReturn("afts");
		when(resultSetMetaData.getSearchParameters()).thenReturn(searchParameters);

		// Call the method under test.
		FilteringResultSet filteringResultSet = (FilteringResultSet) rmAfterInvocationProvider.decide(authentication, object, config, returnedObject);

		assertEquals("Expected total of one result.", 1, filteringResultSet.getNumberFound());
		assertEquals("Expected one result returned.", 1, filteringResultSet.length());
		assertEquals("Expected that results were not limited by the page size.", LimitBy.UNLIMITED, filteringResultSet.getResultSetMetaData().getLimitedBy());
	}

	/** Check that results can skipped due to the skip count. */
	@Test
	public void testDecide_resultSet_skipped()
	{
		// The returned object is a search result set.
		ResultSet returnedObject = mock(ResultSet.class);
		ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
		when(returnedObject.getResultSetMetaData()).thenReturn(resultSetMetaData);

		// Simulate a single result that was skipped due to the skip count.
		when(returnedObject.length()).thenReturn(0);
		when(returnedObject.getNumberFound()).thenReturn(1L);

		// Set the page size to 1 and skip count to 1 (so the result is skipped).
		SearchParameters searchParameters = mock(SearchParameters.class);
		when(searchParameters.getMaxItems()).thenReturn(1);
		when(searchParameters.getSkipCount()).thenReturn(1);
		when(searchParameters.getLanguage()).thenReturn("afts");
		when(resultSetMetaData.getSearchParameters()).thenReturn(searchParameters);

		// Call the method under test.
		FilteringResultSet filteringResultSet = (FilteringResultSet) rmAfterInvocationProvider.decide(authentication, object, config, returnedObject);

		assertEquals("Expected total of one result.", 1, filteringResultSet.getNumberFound());
		assertEquals("Expected no results returned.", 0, filteringResultSet.length());
		assertEquals("Expected that results were not limited by the page size.", LimitBy.UNLIMITED, filteringResultSet.getResultSetMetaData().getLimitedBy());
	}

	/** Check that results can be limited by the page size. */
	@Test
	public void testDecide_resultSet_pageSize()
	{
		// The returned object is a search result set.
		ResultSet returnedObject = mock(ResultSet.class);
		ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
		when(returnedObject.getResultSetMetaData()).thenReturn(resultSetMetaData);

		// Simulate a single result, and the user has access to it.
		when(returnedObject.length()).thenReturn(1);
		when(returnedObject.getNumberFound()).thenReturn(1L);
		when(returnedObject.getNodeRef(0)).thenReturn(NODE_A);
		when(returnedObject.getChildAssocRef(0)).thenReturn(childAssocRefA);

		// Set the page size to 0 and skip count to 0 (so the result is not in page).
		SearchParameters searchParameters = mock(SearchParameters.class);
		when(searchParameters.getMaxItems()).thenReturn(0);
		when(searchParameters.getSkipCount()).thenReturn(0);
		when(searchParameters.getLanguage()).thenReturn("afts");
		when(resultSetMetaData.getSearchParameters()).thenReturn(searchParameters);

		// Call the method under test.
		FilteringResultSet filteringResultSet = (FilteringResultSet) rmAfterInvocationProvider.decide(authentication, object, config, returnedObject);

		assertEquals("Expected total of one result.", 1, filteringResultSet.getNumberFound());
		assertEquals("Expected no results returned.", 0, filteringResultSet.length());
		assertEquals("Expected that results were limited by page size.", LimitBy.FINAL_SIZE, filteringResultSet.getResultSetMetaData().getLimitedBy());
	}
}
