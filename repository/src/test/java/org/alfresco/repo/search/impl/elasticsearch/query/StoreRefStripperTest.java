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
package org.alfresco.repo.search.impl.elasticsearch.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class StoreRefStripperTest
{

    private StoreRefStripper storeRefStripper;
    private AlfrescoFunctionEvaluationContext functionContext;

    @Before
    public void setUp()
    {
        NamespacePrefixResolver namespacePrefixResolver = mock(NamespacePrefixResolver.class);
        DictionaryService dictionaryService = mock(DictionaryService.class);
        when(namespacePrefixResolver.getNamespaceURI("cm")).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        when(dictionaryService.getProperty(any(QName.class))).thenReturn(mock(PropertyDefinition.class));
        when(dictionaryService.getDataType(any(QName.class))).thenReturn(mock(DataTypeDefinition.class));
        functionContext = mock(AlfrescoFunctionEvaluationContext.class);
        storeRefStripper = new StoreRefStripper(namespacePrefixResolver, dictionaryService, "namespace");
    }

    @Test
    public void testStrippingIfNeeded()
    {
        String fieldName = "@{http://www.alfresco.org/model/content/1.0}categories";
        String phrase = "workspace://SpacesStore/2275273b-b628-4d14-b527-3bb6287d1461";
        when(functionContext.getLuceneFieldName(ElasticsearchConstants.CM_CATEGORIES)).thenReturn(fieldName);

        String result = storeRefStripper.stripIfNeeded(fieldName, phrase);
        assertEquals("2275273b-b628-4d14-b527-3bb6287d1461", result);
    }

    @Test
    public void shouldNotStrip()
    {
        String fieldName = "@{http://www.alfresco.org/model/content/1.0}categories";
        String phrase = "2275273b-b628-4d14-b527-3bb6287d1461";
        when(functionContext.getLuceneFieldName(ElasticsearchConstants.CM_CATEGORIES)).thenReturn(fieldName);

        String result = storeRefStripper.stripIfNeeded(fieldName, phrase);
        assertEquals("2275273b-b628-4d14-b527-3bb6287d1461", result);
    }

    @Test
    public void shouldNotStripInvalidNodeRef()
    {
        String fieldName = "@{http://www.alfresco.org/model/content/1.0}categories";
        String phrase = "invalid-node-ref";
        when(functionContext.getLuceneFieldName(ElasticsearchConstants.CM_CATEGORIES)).thenReturn(fieldName);

        String result = storeRefStripper.stripIfNeeded(fieldName, phrase);
        assertEquals(phrase, result);
    }

    @Test
    public void testStrippingIfNotNeeded()
    {
        String fieldName = "@{http://www.alfresco.org/model/content/1.0}name";
        String phrase = "workspace://SpacesStore/2275273b-b628-4d14-b527-3bb6287d1461";

        String result = storeRefStripper.stripIfNeeded(fieldName, phrase);
        assertEquals(phrase, result);
    }

    @Test
    public void testStrippingIfPhraseIsNull()
    {
        String fieldName = "@{http://www.alfresco.org/model/content/1.0}name";

        String result = storeRefStripper.stripIfNeeded(fieldName, null);
        assertNull(result);
    }

}
