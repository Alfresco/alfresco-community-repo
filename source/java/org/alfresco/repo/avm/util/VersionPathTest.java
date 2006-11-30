/**
 * 
 */
package org.alfresco.repo.avm.util;

import java.util.List;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.util.Pair;

import junit.framework.TestCase;

/**
 * Test out stuffing and unstuffing Version/Paths
 * @author britt
 */
public class VersionPathTest extends TestCase 
{
    public void testVersionPath()
    {
        VersionPathStuffer stuffer = new VersionPathStuffer();
        stuffer.add(-1, "figs:/bottom/top");
        stuffer.add(1, "piggy:/back/ride");
        stuffer.add(2, "main:/boring/path/to/nowhere");
        String stuffed = stuffer.toString();
        VersionPathUnstuffer unstuffer = new VersionPathUnstuffer(stuffed);
        List<Pair<Integer, String>> items = unstuffer.getVersionPaths();
        assertEquals(3, items.size());
        assertEquals(-1, (int)items.get(0).getFirst());
        assertEquals("figs:/bottom/top", items.get(0).getSecond());
        assertEquals(1, (int)items.get(1).getFirst());
        assertEquals("piggy:/back/ride", items.get(1).getSecond());
        assertEquals(2, (int)items.get(2).getFirst());
        assertEquals("main:/boring/path/to/nowhere", items.get(2).getSecond());
        stuffer = new VersionPathStuffer();
        for (Pair<Integer, String> item : items)
        {
            stuffer.add(AVMNodeConverter.ToNodeRef(item.getFirst(), item.getSecond()));
        }
        String stuffed2 = stuffer.toString();
        assertEquals(stuffed, stuffed2);
        System.out.println(stuffed2);
    }
}
