package org.alfresco.repo.security.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.security.permissions.PermissionCheckedCollection.PermissionCheckedCollectionMixin;

/**
 * Tests {@link PermissionCheckedCollection}
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class PermissionCheckedCollectionTest extends TestCase
{
    @Override
    protected void setUp() throws Exception
    {
    }

    public void testBasicWrapping() throws Exception
    {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        
        final boolean isCutOff = true;
        final int sizeUnchecked = 100;
        final int sizeOriginal = 900;
        
        Collection<Integer> proxiedList = PermissionCheckedCollectionMixin.create(
                list, isCutOff, sizeUnchecked, sizeOriginal);
        
        // Check
        assertTrue("Proxied object must still be a List", proxiedList instanceof List);
        assertEquals("List values incorrect", 3, proxiedList.size());
        assertTrue("Proxied object must also be a PermissionCheckedCollection", proxiedList instanceof PermissionCheckedCollection);
        @SuppressWarnings("unchecked")
        PermissionCheckedCollection<String> proxiedPermissionCheckedCollection = (PermissionCheckedCollection<String>) proxiedList;
        assertEquals("cutOff value incorrect", isCutOff, proxiedPermissionCheckedCollection.isCutOff());
        assertEquals("sizeUnchecked value incorrect", sizeUnchecked, proxiedPermissionCheckedCollection.sizeUnchecked());
        assertEquals("sizeOriginal value incorrect", sizeOriginal, proxiedPermissionCheckedCollection.sizeOriginal());
    }
    
    public void testVolumeWrapping() throws Exception
    {
        int count = 10000;
        long before = System.nanoTime();
        for (int i = 0; i < count; i++)
        {
            testBasicWrapping();
        }
        long after = System.nanoTime();
        double average = ((double) (after - before) / (double) count) / (double) 1.0E6;
        System.out.println("Average is " + average + "ms per wrap.");
    }
}
