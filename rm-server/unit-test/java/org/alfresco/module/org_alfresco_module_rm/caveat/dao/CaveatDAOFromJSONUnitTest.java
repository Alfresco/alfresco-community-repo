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

package org.alfresco.module.org_alfresco_module_rm.caveat.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.caveat.CaveatException.MalformedConfiguration;
import org.alfresco.module.org_alfresco_module_rm.caveat.scheme.CaveatGroup;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link CaveatDAOFromJSON}.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatDAOFromJSONUnitTest
{
    /** The class under test. */
    CaveatDAOFromJSON caveatDAOFromJSON = new CaveatDAOFromJSON();

    @Before public void initClassUnderTest()
    {
        NamespaceService namespaceService = mock(NamespaceService.class);
        when(namespaceService.getNamespaceURI(anyString())).thenReturn("{mockedNamespace}");

        DictionaryService dictionaryService = mock(DictionaryService.class);
        PropertyDefinition mockProperty = mock(PropertyDefinition.class);
        when(dictionaryService.getProperty(any(QName.class))).thenReturn(mockProperty);

        caveatDAOFromJSON.setDictionaryService(dictionaryService);
        caveatDAOFromJSON.setNamespaceService(namespaceService);
    }

    /** Test that loading the default caveat configuration file doesn't throw any exceptions. */
    @Test
    public void testGetCaveatGroups_defaultConfiguration()
    {
        caveatDAOFromJSON.setConfigLocation("/alfresco/module/org_alfresco_module_rm/caveat/rm-caveats.json");
        Map<String, CaveatGroup> caveatGroups = caveatDAOFromJSON.getCaveatGroups();
        assertNotNull(caveatGroups);
    }

    /** Test that if the caveat configuration file is missing then an empty set of caveat groups is created. */
    @Test
    public void testGetCaveatGroups_missingConfiguration()
    {
        caveatDAOFromJSON.setConfigLocation("/does/not/exist.json");
        Map<String, CaveatGroup> caveatGroups = caveatDAOFromJSON.getCaveatGroups();
        assertTrue("A missing configuration file should result in no caveat groups", caveatGroups.keySet().isEmpty());
    }

    /** Test that malformed JSON causes an exception. */
    @Test(expected = MalformedConfiguration.class)
    public void testGetCaveatGroups_malformedJSON()
    {
        caveatDAOFromJSON.setConfigLocation("/alfresco/caveat/rm-caveats-malformedJSON.json");
        caveatDAOFromJSON.getCaveatGroups();
    }

    /** Test that a missing id causes an exception. */
    @Test(expected = MalformedConfiguration.class)
    public void testGetCaveatGroups_missingId()
    {
        caveatDAOFromJSON.setConfigLocation("/alfresco/caveat/rm-caveats-missingMarkId.json");
        caveatDAOFromJSON.getCaveatGroups();
    }

    /** Test that a duplicate group id causes an exception. */
    @Test(expected = MalformedConfiguration.class)
    public void testGetCaveatGroups_duplicateGroupId()
    {
        caveatDAOFromJSON.setConfigLocation("/alfresco/caveat/rm-caveats-duplicateGroupId.json");
        caveatDAOFromJSON.getCaveatGroups();
    }

    /** Test that a duplicate mark id (within a group) causes an exception. */
    @Test(expected = MalformedConfiguration.class)
    public void testGetCaveatGroups_duplicateMarkId()
    {
        caveatDAOFromJSON.setConfigLocation("/alfresco/caveat/rm-caveats-duplicateMarkId.json");
        caveatDAOFromJSON.getCaveatGroups();
    }

    /** Test that a duplicate mark id (in different groups) doesn't cause an exception. */
    @Test
    public void testGetCaveatGroups_duplicateMarkIdInDifferentGroups()
    {
        caveatDAOFromJSON.setConfigLocation("/alfresco/caveat/rm-caveats-duplicateMarkIdInDifferentGroups.json");
        Map<String, CaveatGroup> caveatGroups = caveatDAOFromJSON.getCaveatGroups();
        assertNotNull(caveatGroups);
    }
}
