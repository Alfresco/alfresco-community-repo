
package org.alfresco.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test model related utility functions.
 * 
 * @since 3.4
 */
public class ModelUtilTest extends TestCase
{
    public void testPage()
    {
        List<String> res;
        
        List<String> entries = new ArrayList<String>();
        entries.add("1");
        entries.add("2");
        entries.add("3");
        entries.add("AB");
        entries.add("CD");

        // Full
        res = ModelUtil.page(entries, 5, 0);
        assertEquals(5, res.size());
        assertEquals("1", res.get(0));
        assertEquals("CD", res.get(4));
        
        // Partial
        res = ModelUtil.page(entries, 3, 0);
        assertEquals(3, res.size());
        assertEquals("1", res.get(0));
        assertEquals("3", res.get(2));
        
        res = ModelUtil.page(entries, 3, 1);
        assertEquals(3, res.size());
        assertEquals("2", res.get(0));
        assertEquals("AB", res.get(2));
        
        res = ModelUtil.page(entries, 3, 2);
        assertEquals(3, res.size());
        assertEquals("3", res.get(0));
        assertEquals("CD", res.get(2));
        
        // Too many
        res = ModelUtil.page(entries, 10, 0);
        assertEquals(5, res.size());
        assertEquals("1", res.get(0));
        assertEquals("CD", res.get(4));
        
        res = ModelUtil.page(entries, 10, 2);
        assertEquals(3, res.size());
        assertEquals("3", res.get(0));
        assertEquals("CD", res.get(2));
        
        
        // Now with an array
        String[] entriesS = entries.toArray(new String[entries.size()]);
        String[] resS; 

        // Full
        resS = ModelUtil.page(entriesS, 5, 0);
        assertEquals(5, resS.length);
        assertEquals("1", resS[0]);
        assertEquals("CD", resS[4]);
        
        // Partial
        resS = ModelUtil.page(entriesS, 3, 0);
        assertEquals(3, resS.length);
        assertEquals("1", resS[0]);
        assertEquals("3", resS[2]);
        
        resS = ModelUtil.page(entriesS, 3, 1);
        assertEquals(3, resS.length);
        assertEquals("2", resS[0]);
        assertEquals("AB", resS[2]);
        
        resS = ModelUtil.page(entriesS, 3, 2);
        assertEquals(3, resS.length);
        assertEquals("3", resS[0]);
        assertEquals("CD", resS[2]);
        
        // Too many
        resS = ModelUtil.page(entriesS, 10, 0);
        assertEquals(5, resS.length);
        assertEquals("1", resS[0]);
        assertEquals("CD", resS[4]);
        
        resS = ModelUtil.page(entriesS, 10, 2);
        assertEquals(3, resS.length);
        assertEquals("3", resS[0]);
        assertEquals("CD", resS[2]);
    }
    
    public void testBuildPaging()
    {
        Map<String, Object> model = ModelUtil.buildPaging(100, 50, 50);
        
        assertEquals(100, model.get(ModelUtil.PAGING_TOTAL_ITEMS));
        assertEquals(50, model.get(ModelUtil.PAGING_MAX_ITEMS));
        assertEquals(50, model.get(ModelUtil.PAGING_SKIP_COUNT));
    }
}
