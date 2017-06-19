/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.virtual.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class VirtualQueryImplTest extends TestCase
{

    private static final String QUERY_TEST_STRING_QUERY = "QUERY_TEST_STRING_QUERY";

    private static final String TEST_LOCAL_NAME_1 = "testQName1";

    private static final String TEST_LOCAL_NAME_2 = "testQName2";

    private static final String TEST_URI = "http://test/uri";

    private static final String TST_PREFIX = "tst";

    private VirtualQueryImpl query;

    private ActualEnvironment mockitoActualEnvironment;

    private NamespacePrefixResolver mockitoPrefixResolver;

    private QName testQName1;

    private QName testQName2;

    private Reference nodeOneReference;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        query = new VirtualQueryImpl(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString(),
                                     SearchService.LANGUAGE_FTS_ALFRESCO,
                                     QUERY_TEST_STRING_QUERY);

        mockitoActualEnvironment = Mockito.mock(ActualEnvironment.class);
        Mockito.when(mockitoActualEnvironment.query(Mockito.any(SearchParameters.class)))
                        .thenReturn(new EmptyResultSet());

        mockitoPrefixResolver = Mockito.mock(NamespacePrefixResolver.class);
        Mockito.when(mockitoPrefixResolver.getNamespaceURI(TST_PREFIX)).thenReturn(TEST_URI);
        Mockito.when(mockitoPrefixResolver.getPrefixes(TEST_URI)).thenReturn(Arrays.asList(TST_PREFIX));

        Mockito.when(mockitoActualEnvironment.getNamespacePrefixResolver()).thenReturn(mockitoPrefixResolver);

        testQName1 = QName.createQName(TST_PREFIX,
                                       TEST_LOCAL_NAME_1,
                                       mockitoPrefixResolver);

        testQName2 = QName.createQName(TST_PREFIX,
                                       TEST_LOCAL_NAME_2,
                                       mockitoPrefixResolver);

        NodeRef n1 = new NodeRef("workspace://SpacesStore/17c8f11d-0936-4295-88a0-12b85764c76f");
        NodeRef n2 = new NodeRef("workspace://SpacesStore/27c8f11d-0936-4295-88a0-12b85764c76f");
        nodeOneReference = ((VirtualProtocol) Protocols.VIRTUAL.protocol).newReference(n1,
                                                                                       "/1",
                                                                                       n2);
    }

    @Test
    public void testPerform_1() throws Exception
    {
        Pair<QName, Boolean> withSortDefinitions = new Pair<QName, Boolean>(testQName2,
                                                                            true);

        VirtualQueryConstraint constraint = BasicConstraint.INSTANCE;
        constraint = new FilesFoldersConstraint(constraint,
                                                true,
                                                true);
        constraint = new IgnoreConstraint(constraint,
                                          Collections.singleton(testQName2),
                                                 Collections.singleton(testQName1));
        constraint = new SortConstraint(constraint,
                                        Arrays.asList(withSortDefinitions));

        query.perform(mockitoActualEnvironment,
                      constraint,
                      null,
                      nodeOneReference);

        assertPerform1Results(withSortDefinitions);
    }

    public void testPerform_2() throws Exception
    {

        VirtualQueryConstraint constraint = BasicConstraint.INSTANCE;
        constraint = new FilesFoldersConstraint(constraint,
                                                false,
                                                true);
        constraint = new IgnoreConstraint(constraint,
                                          Collections.singleton(testQName2),
                                                 Collections.singleton(testQName1));

        query.perform(mockitoActualEnvironment,
                      constraint,
                      null,
                      nodeOneReference);

        assertPerform2Results();
    }

    private void assertPerform1Results(Pair<QName, Boolean> withSortDefinitions)
    {
        ArgumentCaptor<SearchParameters> queryCaptor = ArgumentCaptor.forClass(SearchParameters.class);
        Mockito.verify(mockitoActualEnvironment).query(queryCaptor.capture());

        assertEquals("(QUERY_TEST_STRING_QUERY) and !ASPECT:'tst:testQName1' and !TYPE:'tst:testQName2'",
                     queryCaptor.getValue().getQuery());

        ArrayList<SortDefinition> sortDefinitions = queryCaptor.getValue().getSortDefinitions();

        assertNotNull(sortDefinitions);
        assertEquals(1,
                     sortDefinitions.size());
        assertEquals(withSortDefinitions.getFirst().getLocalName(),
                     sortDefinitions.get(0).getField());

        assertEquals(withSortDefinitions.getSecond(),
                     Boolean.valueOf(sortDefinitions.get(0).isAscending()));
    }

    @Test
    public void testPerform_deprecated_1() throws Exception
    {
        Pair<QName, Boolean> withSortDefinitions = new Pair<QName, Boolean>(testQName2,
                                                                            true);
        query.perform(mockitoActualEnvironment,
                      true,
                      true,
                      null,
                      Collections.<QName> emptySet(),
                      Collections.singleton(testQName2),
                      Collections.singleton(testQName1),
                      Arrays.asList(withSortDefinitions),
                      null,
                      nodeOneReference);

        assertPerform1Results(withSortDefinitions);
    }

    @Test
    public void testPerform_deprecated_2() throws Exception
    {
        query.perform(mockitoActualEnvironment,
                      false,
                      true,
                      null,
                      Collections.<QName> emptySet(),
                      Collections.singleton(testQName2),
                      Collections.singleton(testQName1),
                      Collections.<Pair<QName, Boolean>> emptyList(),
                      null,
                      nodeOneReference);

        assertPerform2Results();

    }

    private void assertPerform2Results()
    {
        ArgumentCaptor<SearchParameters> queryCaptor = ArgumentCaptor.forClass(SearchParameters.class);
        Mockito.verify(mockitoActualEnvironment).query(queryCaptor.capture());

        assertEquals("((QUERY_TEST_STRING_QUERY) and TYPE:\"cm:folder\") and !ASPECT:'tst:testQName1' and !TYPE:'tst:testQName2'",
                     queryCaptor.getValue().getQuery());

        ArrayList<SortDefinition> sortDefinitions = queryCaptor.getValue().getSortDefinitions();

        assertNotNull(sortDefinitions);
        assertEquals(0,
                     sortDefinitions.size());
    }
}
