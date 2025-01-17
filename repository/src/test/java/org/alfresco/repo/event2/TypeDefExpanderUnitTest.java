/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.model.DataListModel;
import org.alfresco.repo.event2.shared.TypeDefExpander;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class TypeDefExpanderUnitTest
{
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private TypeDefExpander typeDefExpander;

    @Before
    public void setUp()
    {
        dictionaryService = mock(DictionaryService.class);
        namespaceService = mock(NamespaceService.class);
        typeDefExpander = new TypeDefExpander(dictionaryService, namespaceService);
    }

    @Test
    public void testExpandWithValidType()
    {
        String input = "usr:username";
        when(namespaceService.getNamespaceURI("usr")).thenReturn(ContentModel.USER_MODEL_URI);

        Collection<QName> result = typeDefExpander.expand(input);

        QName expected = ContentModel.PROP_USER_USERNAME;
        assertEquals(expected, result.iterator().next());
    }

    @Test
    public void testExpandWithValidTypeIncludingSubtypes()
    {
        String input = "cm:content include_subtypes";
        when(namespaceService.getNamespaceURI("cm")).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        Set<QName> subtypes = Set.of(TransferModel.TYPE_TRANSFER_RECORD, DataListModel.TYPE_EVENT, WorkflowModel.TYPE_TASK);
        when(dictionaryService.getSubTypes(ContentModel.TYPE_CONTENT, true)).thenReturn(subtypes);

        Collection<QName> result = typeDefExpander.expand(input);

        assertEquals(subtypes, result);
    }

    @Test
    public void testExpandWithInvalidTypes()
    {
        Set<String> input = Stream.of(null, " ", "none", "${test.prop}").collect(Collectors.toSet());

        Collection<QName> result = typeDefExpander.expand(input);

        assertTrue(result.isEmpty());
    }
}
