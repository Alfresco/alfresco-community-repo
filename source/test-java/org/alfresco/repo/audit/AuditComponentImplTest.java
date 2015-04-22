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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.service.cmr.repository.MLText;

import junit.framework.TestCase;

public class AuditComponentImplTest extends TestCase
{
    public void testTrimStringsIfNecessary()
    {
        final int OVERLIMIT_SIZE = 1500;
        AuditComponentImpl auditComponent = new AuditComponentImpl();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < OVERLIMIT_SIZE; i++)
        {
            sb.append("a");
        }

        // Test map input
        HashMap<String, Serializable> map = new HashMap<String, Serializable>();
        String nullValue = null;
        String oversizeString = sb.toString();
        MLText mlTextValue = new MLText();
        mlTextValue.put(Locale.ENGLISH, oversizeString);

        HashMap<String, Serializable> mapEntry = new HashMap<String, Serializable>();
        MLText mlTextMap = new MLText();
        mlTextMap.put(Locale.ENGLISH, oversizeString);
        mapEntry.put("StringMapEntry", oversizeString);
        mapEntry.put("MLText", mlTextMap);

        ArrayList<Serializable> list = new ArrayList<Serializable>();
        MLText mlTextList = new MLText();
        mlTextList.put(Locale.ENGLISH, oversizeString);
        list.add(oversizeString);
        list.add(mlTextList);

        ArrayList<Serializable> listEntry = new ArrayList<Serializable>();
        MLText mlTextListEntry = new MLText();
        mlTextListEntry.put(Locale.ENGLISH, oversizeString);
        HashMap<String, Serializable> mapListEntry = new HashMap<String, Serializable>();
        mapListEntry.put("StringMapListEntry", oversizeString);
        listEntry.add(nullValue);
        listEntry.add(oversizeString);
        listEntry.add(mlTextListEntry);
        listEntry.add(mapListEntry);
        listEntry.add(list);

        ArrayList<Serializable> listForUnmd = new ArrayList<Serializable>();
        listForUnmd.add(oversizeString);
        Collection<Serializable> unmdCollection = Collections.unmodifiableCollection(listForUnmd);

        map.put("nullValue", nullValue);
        map.put("StringMap", oversizeString);
        map.put("MLText", mlTextValue);
        map.put("mapEntry", mapEntry);
        map.put("listEntry", listEntry);
        map.put("unmodifiableCollection", (Serializable) unmdCollection);

        // Test method
        Map<String, Serializable> processed = auditComponent.trimStringsIfNecessary(map);

        // Check that nothing changed with null
        assertNull(processed.get("nullValue"));

        // Check StringMap
        String stringMap = (String) processed.get("StringMap");
        assertNotSame(stringMap, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringMap.length());

        // Check MLText
        MLText mlTextProc = (MLText) processed.get("MLText");
        assertNotSame(mlTextProc, mlTextValue);
        String stringMLText = mlTextProc.get(Locale.ENGLISH);
        assertNotSame(stringMLText, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringMLText.length());

        // Check mapEntry
        HashMap<String, Serializable> mapEntryProc = (HashMap<String, Serializable>) processed.get("mapEntry");
        assertNotSame(mapEntryProc, mapEntry);

        String stringMapEntry = (String) mapEntryProc.get("StringMapEntry");
        assertNotSame(stringMapEntry, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringMapEntry.length());

        MLText mlTextMapProc = (MLText) mapEntryProc.get("MLText");
        assertNotSame(mlTextMapProc, mlTextMap);
        String stringMLTextMap = mlTextMapProc.get(Locale.ENGLISH);
        assertNotSame(stringMLTextMap, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringMLTextMap.length());

        // Check listEntry
        ArrayList<Serializable> listEntryProc = (ArrayList<Serializable>) processed.get("listEntry");
        assertNotSame(listEntryProc, listEntry);

        assertNull(listEntryProc.get(0));

        String stringListEntry = (String) listEntryProc.get(1);
        assertNotSame(stringListEntry, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringListEntry.length());

        MLText mlTextListEntryProc = (MLText) listEntryProc.get(2);
        assertNotSame(mlTextListEntryProc, mlTextListEntry);
        String stringMLTextListEntry = mlTextListEntryProc.get(Locale.ENGLISH);
        assertNotSame(stringMLTextListEntry, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringMLTextListEntry.length());

        HashMap<String, Serializable> mapListEntryProc = (HashMap<String, Serializable>) listEntryProc.get(3);
        assertNotSame(mapListEntryProc, mapListEntry);

        String stringMapListEntry = (String) mapListEntryProc.get("StringMapListEntry");
        assertNotSame(stringMapListEntry, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringMapListEntry.length());

        ArrayList<Serializable> listProc = (ArrayList<Serializable>) listEntryProc.get(4);
        assertNotSame(listProc, list);

        String stringList = (String) listProc.get(0);
        assertNotSame(stringList, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringList.length());

        MLText mlTextListProc = (MLText) listProc.get(1);
        assertNotSame(mlTextListProc, mlTextList);
        String stringMLTextList = mlTextListProc.get(Locale.ENGLISH);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringMLTextList.length());

        // Check unmodifiableCollection.
        Collection<Serializable> unmdCollectionProc = (Collection<Serializable>) processed.get("unmodifiableCollection");
        assertNotSame(unmdCollectionProc, unmdCollection);
        Object[] array = unmdCollectionProc.toArray();
        String stringUNMDCollection = (String) array[0];
        assertNotSame(stringUNMDCollection, oversizeString);
        assertEquals(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH, stringUNMDCollection.length());

        // Check that initial string have not been changed
        assertEquals(OVERLIMIT_SIZE, oversizeString.length());
    }
}