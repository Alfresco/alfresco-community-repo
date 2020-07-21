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
package org.alfresco.util;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Some tests for {@link PropertyMap}
 * 
 * @author Neil Mc Erlean
 * @since Odin
 */
public class PropertyMapTest
{
    private static Map<QName, Serializable> beforeTestMap, afterTestMap;
    
    @BeforeClass public static void initTestMaps()
    {
        beforeTestMap = new HashMap<QName, Serializable>();
        afterTestMap = new HashMap<QName, Serializable>();
        
        beforeTestMap.put(ContentModel.PROP_NAME, "willBeChanged");
        beforeTestMap.put(ContentModel.PROP_ADDRESSEE, "willBeNulled");
        beforeTestMap.put(ContentModel.PROP_TITLE, "willBeRemoved");
        beforeTestMap.put(ContentModel.PROP_COUNTER, "unchanging");
        
        afterTestMap.put(ContentModel.PROP_NAME, "hasBeenChanged");
        afterTestMap.put(ContentModel.PROP_ADDRESSEE, null);
        afterTestMap.put(ContentModel.PROP_DESCRIPTION, "hasBeenAdded");
        afterTestMap.put(ContentModel.PROP_COUNTER, "unchanging");
    }
    
    @Test public void propertiesAdded() throws Exception
    {
        Map<QName, Serializable> expected = new HashMap<QName, Serializable>();
        expected.put(ContentModel.PROP_DESCRIPTION, "hasBeenAdded");
        
        assertEquals(expected, PropertyMap.getAddedProperties(beforeTestMap, afterTestMap));
    }
    
    @Test public void propertiesChanged() throws Exception
    {
        Map<QName, Serializable> expected = new HashMap<QName, Serializable>();
        expected.put(ContentModel.PROP_NAME, "hasBeenChanged");
        expected.put(ContentModel.PROP_ADDRESSEE, null);
        
        assertEquals(expected, PropertyMap.getChangedProperties(beforeTestMap, afterTestMap));
    }
    
    @Test public void propertiesRemoved() throws Exception
    {
        Map<QName, Serializable> expected = new HashMap<QName, Serializable>();
        expected.put(ContentModel.PROP_TITLE, "willBeRemoved");
        
        assertEquals(expected, PropertyMap.getRemovedProperties(beforeTestMap, afterTestMap));
    }
    
    @Test public void propertiesUnchanged() throws Exception
    {
        Map<QName, Serializable> expected = new HashMap<QName, Serializable>();
        expected.put(ContentModel.PROP_COUNTER, "unchanging");
        
        assertEquals(expected, PropertyMap.getUnchangedProperties(beforeTestMap, afterTestMap));
    }
    
    @Test public void nullMapsShouldntCauseExceptions() throws Exception
    {
        assertEquals(Collections.emptyMap(), PropertyMap.getAddedProperties(null, null));
        assertEquals(Collections.emptyMap(), PropertyMap.getRemovedProperties(null, null));
        assertEquals(Collections.emptyMap(), PropertyMap.getChangedProperties(null, null));
        assertEquals(Collections.emptyMap(), PropertyMap.getUnchangedProperties(null, null));
    }
}
