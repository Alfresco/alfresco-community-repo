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
