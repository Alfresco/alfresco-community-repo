/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import static java.util.Collections.singleton;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.service.cmr.favourites.FavouritesService.Type.FILE;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.alfresco.repo.favourites.PersonFavourite;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.DocumentTarget;
import org.alfresco.rest.api.model.Favourite;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Unit tests for {@link FavouritesImpl} class.
 */
public class FavouritesImplUnitTest
{
    static final String NODE_ID = "12345678";
    static final NodeRef NODE_REF = new NodeRef("favourite://node/" + NODE_ID);
    static final String PERSON_ID = "personId";
    static final String ASPECT_NAME = "some:aspect";

    @InjectMocks
    FavouritesImpl favouritesImpl;
    @Mock
    People people;
    @Mock
    Nodes nodes;
    @Mock
    FavouritesService favouritesService;
    @Mock
    NamespaceService namespaceService;
    @Mock
    Favourite favourite;
    @Mock
    Document document;
    @Mock
    PersonFavourite personFavourite;

    @Before
    public void setUp()
    {
        openMocks(this);

        when(nodes.getDocument(NODE_REF)).thenReturn(document);
        when(nodes.nodeMatches(NODE_REF, singleton(TYPE_CONTENT), null)).thenReturn(true);
        when(document.getGuid()).thenReturn(NODE_REF);
        when(people.validatePerson(PERSON_ID, true)).thenReturn(PERSON_ID);
        when(personFavourite.getNodeRef()).thenReturn(NODE_REF);
        when(personFavourite.getType()).thenReturn(FILE);
        when(favouritesService.addFavourite(PERSON_ID, NODE_REF)).thenReturn(personFavourite);
        when(namespaceService.getPrefixes(anyString())).thenReturn(List.of("prefix"));
    }

    @Test
    public void testAddFavourite()
    {
        DocumentTarget documentTarget = new DocumentTarget(document);
        when(favourite.getTarget()).thenReturn(documentTarget);

        Favourite response = favouritesImpl.addFavourite(PERSON_ID, favourite);

        Favourite expected = new Favourite();
        expected.setTarget(documentTarget);
        expected.setTargetGuid(NODE_ID);
        assertEquals(expected, response);
    }

    @Test
    public void testAddFavouriteIncludeAspectNames()
    {
        List<String> includes = List.of("aspectNames");

        DocumentTarget documentTarget = new DocumentTarget(document);
        when(favourite.getTarget()).thenReturn(documentTarget);
        when(nodes.getFolderOrDocument(NODE_REF, null, null, includes, null)).thenReturn(document);
        when(document.getAspectNames()).thenReturn(List.of(ASPECT_NAME));

        Parameters parameters = mock(Parameters.class);
        when(parameters.getInclude()).thenReturn(includes);

        Favourite response = favouritesImpl.addFavourite(PERSON_ID, favourite, parameters);

        Favourite expected = new Favourite();
        expected.setTarget(documentTarget);
        expected.setTargetGuid(NODE_ID);
        expected.setAspectNames(List.of(ASPECT_NAME));
        assertEquals(expected, response);
    }
}
