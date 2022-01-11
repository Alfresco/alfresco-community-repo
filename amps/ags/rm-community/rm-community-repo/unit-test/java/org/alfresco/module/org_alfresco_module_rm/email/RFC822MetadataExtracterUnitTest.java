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
package org.alfresco.module.org_alfresco_module_rm.email;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for RFC822MetadataExtracter
 *
 * @author Ana Manolache
 * @since 2.7
 */
public class RFC822MetadataExtracterUnitTest extends BaseUnitTest
{
    @InjectMocks
    private RFC822MetadataExtracter metadataExtracter;

    private static final Map<QName, Serializable> COMMON_PROPERTIES = ImmutableMap.of(
            ContentModel.PROP_NAME, "Name",
            ContentModel.PROP_TITLE, "Title");
    private static final Map<QName, Serializable> RECORD_PROPERTIES = ImmutableMap.of(
            RecordsManagementModel.PROP_DECLARED_BY, "DeclaredBy",
            RecordsManagementModel.PROP_DECLARED_AT, new Date());
    private static final Map<QName, Serializable> DOD_PROPERTIES = ImmutableMap.of(
            DOD5015Model.PROP_ORIGINATOR, "DODOriginator",
            DOD5015Model.PROP_ADDRESS, "Title");

    /**
     * Given a node that is not a record nor a dod record
     *       and has record and dod record properties
     * When the method is called
     * Then the record properties and dod properties are filtered out
     */
    @Test
    public void testRemoveSensitivePropertiesFromCommonNodes()
    {
        // Given
        NodeRef node = generateNodeRef();
        when(mockedNodeService.hasAspect(node, RecordsManagementModel.ASPECT_RECORD)).thenReturn(false);
        when(mockedNodeService.hasAspect(node, DOD5015Model.ASPECT_DOD_5015_RECORD)).thenReturn(false);

        // When
        Map<QName, Serializable> systemProperties = new HashMap<>(COMMON_PROPERTIES);
        systemProperties.putAll(RECORD_PROPERTIES);
        systemProperties.putAll(DOD_PROPERTIES);
        metadataExtracter.filterSystemProperties(systemProperties, generateTargetProperties(node));

        // Then
        assertTrue("Sensitive properties were not properly filtered out.",
                systemProperties.keySet().equals(COMMON_PROPERTIES.keySet()));
    }

    /**
     * Given a node that is a record
     *       and has record properties and dod properties
     * When the method is called
     * Then the DOD properties are filtered out
     *      and common and record properties are preserved
     */
    @Test
    public void testRemoveDodPropertiesFromRecordNodes()
    {
        // Given
        NodeRef node = generateNodeRef();
        when(mockedNodeService.hasAspect(node, RecordsManagementModel.ASPECT_RECORD)).thenReturn(true);
        when(mockedNodeService.hasAspect(node, DOD5015Model.ASPECT_DOD_5015_RECORD)).thenReturn(false);

        // When
        Map<QName, Serializable> systemProperties = new HashMap<>(COMMON_PROPERTIES);
        systemProperties.putAll(RECORD_PROPERTIES);
        systemProperties.putAll(DOD_PROPERTIES);
        metadataExtracter.filterSystemProperties(systemProperties, generateTargetProperties(node));

        // Then
        assertTrue("Common properties should not be filtered out from record nodes.",
                systemProperties.keySet().containsAll(COMMON_PROPERTIES.keySet()));
        assertTrue("Record properties should not be filtered out from record nodes.",
                systemProperties.keySet().containsAll(RECORD_PROPERTIES.keySet()));
        assertFalse("Sensitive DOD properties were not properly filtered out from record nodes.",
                systemProperties.keySet().removeAll(DOD_PROPERTIES.keySet()));
    }

    /**
     * Given a node that is a dod record
     * and has record properties and dod properties
     * When the method is called
     * Then the record properties are filtered out
     * and common and DOD properties are preserved
     */
    @Test
    public void testRemoveRecordPropertiesFromDodNodes()
    {
        // Given
        NodeRef node = generateNodeRef();
        when(mockedNodeService.hasAspect(node, RecordsManagementModel.ASPECT_RECORD)).thenReturn(false);
        when(mockedNodeService.hasAspect(node, DOD5015Model.ASPECT_DOD_5015_RECORD)).thenReturn(true);

        // When
        Map<QName, Serializable> systemProperties = new HashMap<>(COMMON_PROPERTIES);
        systemProperties.putAll(RECORD_PROPERTIES);
        systemProperties.putAll(DOD_PROPERTIES);
        metadataExtracter.filterSystemProperties(systemProperties, generateTargetProperties(node));

        // Then
        assertTrue("Common properties should not be filtered out from DOD nodes.",
                systemProperties.keySet().containsAll(COMMON_PROPERTIES.keySet()));
        assertTrue("DOD properties should not be filtered out from DOD nodes.",
                systemProperties.keySet().containsAll(DOD_PROPERTIES.keySet()));
        assertFalse("Sensitive record properties were not properly filtered out from DOD nodes.",
                systemProperties.keySet().removeAll(RECORD_PROPERTIES.keySet()));
    }

    /**
     * Helper method that generates target properties such as the given node is retrieved from them
     *
     * @param node the node to represent in the properties
     * @return the list of properties containing the node's information
     */
    private Map<QName, Serializable> generateTargetProperties(NodeRef node)
    {
        Map<QName, Serializable> targetProperties = new HashMap<>();
        targetProperties.put(ContentModel.PROP_STORE_PROTOCOL, node.getStoreRef().getProtocol());
        targetProperties.put(ContentModel.PROP_STORE_IDENTIFIER, node.getStoreRef().getIdentifier());
        targetProperties.put(ContentModel.PROP_NODE_UUID, node.getId());
        return targetProperties;
    }
}
