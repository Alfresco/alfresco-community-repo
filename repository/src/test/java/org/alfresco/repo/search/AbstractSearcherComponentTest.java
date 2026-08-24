/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.repo.search.impl.QueryParameterisationException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSearcherComponentTest
{
    private static final String TEST_URI = "http://www.alfresco.org/test/1.0";
    private static final String PREFIX = "test";

    private final AbstractSearcherComponent searcher = new StubSearcherComponent();

    @Mock
    private NamespacePrefixResolver namespacePrefixResolver;

    @Before
    public void setUp()
    {
        lenient().when(namespacePrefixResolver.getNamespaceURI(PREFIX)).thenReturn(TEST_URI);
    }

    private static QName paramQName(String localName)
    {
        return QName.createQName(TEST_URI, localName);
    }

    @Test
    public void parameterise_substitutesSuppliedParameterValue()
    {
        Map<QName, QueryParameterDefinition> map = new HashMap<>();
        map.put(paramQName("param"), mock(QueryParameterDefinition.class));

        QueryParameter parameter = mock(QueryParameter.class);
        when(parameter.getQName()).thenReturn(paramQName("param"));
        when(parameter.getValue()).thenReturn("hello");

        String result = searcher.parameterise("value:${test:param}", map, new QueryParameter[]{parameter}, namespacePrefixResolver);

        assertThat(result).isEqualTo("value:hello");
    }

    @Test
    public void parameterise_substitutesDefaultValueWhenNoParameterSupplied()
    {
        QueryParameterDefinition qpd = mock(QueryParameterDefinition.class);
        when(qpd.hasDefaultValue()).thenReturn(true);
        when(qpd.getDefault()).thenReturn("admin");

        Map<QName, QueryParameterDefinition> map = new HashMap<>();
        map.put(paramQName("param"), qpd);

        String result = searcher.parameterise("user:${test:param}", map, null, namespacePrefixResolver);

        assertThat(result).isEqualTo("user:admin");
    }

    @Test
    public void parameterise_iteratesSuppliedValuesInOrderForRepeatedPlaceholder()
    {
        Map<QName, QueryParameterDefinition> map = new HashMap<>();
        map.put(paramQName("param"), mock(QueryParameterDefinition.class));

        QueryParameter first = mock(QueryParameter.class);
        when(first.getQName()).thenReturn(paramQName("param"));
        when(first.getValue()).thenReturn("one");
        QueryParameter second = mock(QueryParameter.class);
        when(second.getQName()).thenReturn(paramQName("param"));
        when(second.getValue()).thenReturn("two");

        String result = searcher.parameterise("${test:param}-${test:param}", map, new QueryParameter[]{first, second}, namespacePrefixResolver);

        assertThat(result).isEqualTo("one-two");
    }

    @Test
    public void parameterise_returnsQueryUnchangedWhenNoPlaceholders()
    {
        String result = searcher.parameterise("TYPE:\"cm:content\"", new HashMap<>(), null, namespacePrefixResolver);

        assertThat(result).isEqualTo("TYPE:\"cm:content\"");
    }

    @Test
    public void parameterise_throwsWhenPlaceholderReferencesUndefinedParameter()
    {
        assertThatExceptionOfType(QueryParameterisationException.class)
                .isThrownBy(() -> searcher.parameterise("value:${test:param}", new HashMap<>(), null, namespacePrefixResolver))
                .withMessageContaining("not defined");
    }

    @Test
    public void parameterise_throwsWhenPlaceholderIsUnclosed()
    {
        assertThatExceptionOfType(QueryParameterisationException.class)
                .isThrownBy(() -> searcher.parameterise("value:${test:param", new HashMap<>(), null, namespacePrefixResolver))
                .withMessageContaining("Unclosed");
    }

    @Test
    public void parameterise_throwsWhenParameterHasNoValueAndNoDefault()
    {
        QueryParameterDefinition qpd = mock(QueryParameterDefinition.class);
        when(qpd.hasDefaultValue()).thenReturn(false);

        Map<QName, QueryParameterDefinition> map = new HashMap<>();
        map.put(paramQName("param"), qpd);

        assertThatExceptionOfType(QueryParameterisationException.class)
                .isThrownBy(() -> searcher.parameterise("value:${test:param}", map, null, namespacePrefixResolver))
                .withMessageContaining("No value provided");
    }

    /**
     * Minimal concrete subclass so the shared {@code protected} parameterise implementation can be exercised in isolation. The remaining {@link org.alfresco.service.cmr.search.SearchService} operations are not needed here.
     */
    private static final class StubSearcherComponent extends AbstractSearcherComponent
    {
        @Override
        public ResultSet query(StoreRef store, String language, String query, QueryParameterDefinition[] queryParameterDefinitions)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResultSet query(SearchParameters searchParameters)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
                NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language)
                throws InvalidNodeRefException, XPathException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
                NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language)
                throws InvalidNodeRefException, XPathException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern) throws InvalidNodeRefException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern, SearchParameters.Operator defaultOperator)
                throws InvalidNodeRefException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS) throws InvalidNodeRefException
        {
            throw new UnsupportedOperationException();
        }
    }
}
