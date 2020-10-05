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
package org.alfresco.repo.solr;

import static java.util.Collections.emptyMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Unit tests for {@link org.alfresco.repo.solr.SOLRTrackingComponent}. */
public class SOLRTrackingComponentUnitTest
{
    /** A pair of QNames for use in the tests. */
    private static final QName FIRST_PROPERTY = QName.createQName("the://first/property");
    private static final QName SECOND_PROPERTY = QName.createQName("the://second/property");
    /** A node id for use in the tests. */
    private static final long NODE_ID = 123L;

    /** The class under test. */
    @InjectMocks
    private SOLRTrackingComponentImpl solrTrackingComponent;
    @Mock
    private NodeDAO nodeDAO;
    @Mock
    private DictionaryService dictionaryService;

    @Before
    public void setUp()
    {
        initMocks(this);
    }

    /** Check that properties of different types can be returned. */
    @Test
    public void testGetProperties_indexedPropertiesPassedThrough()
    {
        Map<QName, Serializable> propertiesFromDB = Map.of(FIRST_PROPERTY, "value1", SECOND_PROPERTY, 2);
        when(nodeDAO.getNodeProperties(NODE_ID)).thenReturn(propertiesFromDB);
        PropertyDefinition firstDefinition = mock(PropertyDefinition.class);
        when(firstDefinition.isIndexed()).thenReturn(true);
        when(dictionaryService.getProperty(FIRST_PROPERTY)).thenReturn(firstDefinition);
        PropertyDefinition secondDefinition = mock(PropertyDefinition.class);
        when(secondDefinition.isIndexed()).thenReturn(true);
        when(dictionaryService.getProperty(SECOND_PROPERTY)).thenReturn(secondDefinition);

        Map<QName, Serializable> properties = solrTrackingComponent.getProperties(NODE_ID);

        assertEquals("Expected both properties to be returned.", propertiesFromDB, properties);
    }

    /** Check that a property is not indexed if it is not registered in the dictionary service. */
    @Test
    public void testGetProperties_propertyWithoutModelIsNotIndexed()
    {
        Map<QName, Serializable> propertiesFromDB = Map.of(FIRST_PROPERTY, "value1");
        when(nodeDAO.getNodeProperties(NODE_ID)).thenReturn(propertiesFromDB);
        when(dictionaryService.getProperty(FIRST_PROPERTY)).thenReturn(null);

        Map<QName, Serializable> properties = solrTrackingComponent.getProperties(NODE_ID);

        assertEquals("Expected residual property to be skipped.", emptyMap(), properties);
    }

    /** Check that a property is not indexed if the model contains <index enabled="false"/> */
    @Test
    public void testGetProperties_propertySkippedIfIndexFalseSet()
    {
        Map<QName, Serializable> propertiesFromDB = Map.of(FIRST_PROPERTY, "value1");
        when(nodeDAO.getNodeProperties(NODE_ID)).thenReturn(propertiesFromDB);
        PropertyDefinition firstDefinition = mock(PropertyDefinition.class);
        when(firstDefinition.isIndexed()).thenReturn(false);
        when(dictionaryService.getProperty(FIRST_PROPERTY)).thenReturn(firstDefinition);

        Map<QName, Serializable> properties = solrTrackingComponent.getProperties(NODE_ID);

        assertEquals("Unexpected property when index enabled set to false.", emptyMap(), properties);
    }
}
