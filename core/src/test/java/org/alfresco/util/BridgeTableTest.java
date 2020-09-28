/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.util;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Andy
 *
 */
public class BridgeTableTest extends TestCase
{

    @Test
    public void testBasic()
    {
        BridgeTable<String> bridgeTable = new BridgeTable<String>();
        bridgeTable.addLink("A", "B");
        bridgeTable.addLink("C", "D");
        bridgeTable.addLink("E", "F");
        
        assertEquals(0, bridgeTable.getAncestors("A").size());
        assertEquals(1, bridgeTable.getAncestors("B").size());
        assertEquals(0, bridgeTable.getAncestors("C").size());
        assertEquals(1, bridgeTable.getAncestors("D").size());
        assertEquals(0, bridgeTable.getAncestors("E").size());
        assertEquals(1, bridgeTable.getAncestors("F").size());
        
        assertEquals(1, bridgeTable.getDescendants("A").size());
        assertEquals(0, bridgeTable.getDescendants("B").size());
        assertEquals(1, bridgeTable.getDescendants("C").size());
        assertEquals(0, bridgeTable.getDescendants("D").size());
        assertEquals(1, bridgeTable.getDescendants("E").size());
        assertEquals(0, bridgeTable.getDescendants("F").size());
        
        bridgeTable.addLink("B", "C");
        
        
        assertEquals(0, bridgeTable.getAncestors("A").size());
        assertEquals(1, bridgeTable.getAncestors("B").size());
        assertEquals(2, bridgeTable.getAncestors("C").size());
        assertEquals(3, bridgeTable.getAncestors("D").size());
        assertEquals(0, bridgeTable.getAncestors("E").size());
        assertEquals(1, bridgeTable.getAncestors("F").size());
        
        assertEquals(3, bridgeTable.getDescendants("A").size());
        assertEquals(2, bridgeTable.getDescendants("B").size());
        assertEquals(1, bridgeTable.getDescendants("C").size());
        assertEquals(0, bridgeTable.getDescendants("D").size());
        assertEquals(1, bridgeTable.getDescendants("E").size());
        assertEquals(0, bridgeTable.getDescendants("F").size());
        
        bridgeTable.addLink("D", "E");
        
        assertEquals(0, bridgeTable.getAncestors("A").size());
        assertEquals(1, bridgeTable.getAncestors("B").size());
        assertTrue(bridgeTable.getAncestors("B", 1).contains("A"));
        assertEquals(2, bridgeTable.getAncestors("C").size());
        assertTrue(bridgeTable.getAncestors("C", 1).contains("B"));
        assertTrue(bridgeTable.getAncestors("C", 2).contains("A"));
        assertEquals(3, bridgeTable.getAncestors("D").size());
        assertTrue(bridgeTable.getAncestors("D", 1).contains("C"));
        assertTrue(bridgeTable.getAncestors("D", 2).contains("B"));
        assertTrue(bridgeTable.getAncestors("D", 3).contains("A"));
        assertEquals(4, bridgeTable.getAncestors("E").size());
        assertTrue(bridgeTable.getAncestors("E", 1).contains("D"));
        assertTrue(bridgeTable.getAncestors("E", 2).contains("C"));
        assertTrue(bridgeTable.getAncestors("E", 3).contains("B"));
        assertTrue(bridgeTable.getAncestors("E", 4).contains("A"));
        assertEquals(5, bridgeTable.getAncestors("F").size());
        assertTrue(bridgeTable.getAncestors("F", 1).contains("E"));
        assertTrue(bridgeTable.getAncestors("F", 2).contains("D"));
        assertTrue(bridgeTable.getAncestors("F", 3).contains("C"));
        assertTrue(bridgeTable.getAncestors("F", 4).contains("B"));
        assertTrue(bridgeTable.getAncestors("F", 5).contains("A"));
        
        assertEquals(5, bridgeTable.getDescendants("A").size());
        assertTrue(bridgeTable.getDescendants("A", 1).contains("B"));
        assertTrue(bridgeTable.getDescendants("A", 2).contains("C"));
        assertTrue(bridgeTable.getDescendants("A", 3).contains("D"));
        assertTrue(bridgeTable.getDescendants("A", 4).contains("E"));
        assertTrue(bridgeTable.getDescendants("A", 5).contains("F"));
        assertEquals(4, bridgeTable.getDescendants("B").size());
        assertTrue(bridgeTable.getDescendants("B", 1).contains("C"));
        assertTrue(bridgeTable.getDescendants("B", 2).contains("D"));
        assertTrue(bridgeTable.getDescendants("B", 3).contains("E"));
        assertTrue(bridgeTable.getDescendants("B", 4).contains("F"));
        assertEquals(3, bridgeTable.getDescendants("C").size());
        assertTrue(bridgeTable.getDescendants("C", 1).contains("D"));
        assertTrue(bridgeTable.getDescendants("C", 2).contains("E"));
        assertTrue(bridgeTable.getDescendants("C", 3).contains("F"));
        assertEquals(2, bridgeTable.getDescendants("D").size());
        assertTrue(bridgeTable.getDescendants("D", 1).contains("E"));
        assertTrue(bridgeTable.getDescendants("D", 2).contains("F"));
        assertEquals(1, bridgeTable.getDescendants("E").size());
        assertTrue(bridgeTable.getDescendants("E", 1).contains("F"));
        assertEquals(0, bridgeTable.getDescendants("F").size());
        
        bridgeTable.removeLink("D", "E");
        
        assertEquals(0, bridgeTable.getAncestors("A").size());
        assertEquals(1, bridgeTable.getAncestors("B").size());
        assertEquals(2, bridgeTable.getAncestors("C").size());
        assertEquals(3, bridgeTable.getAncestors("D").size());
        assertEquals(0, bridgeTable.getAncestors("E").size());
        assertEquals(1, bridgeTable.getAncestors("F").size());
        
        assertEquals(3, bridgeTable.getDescendants("A").size());
        assertEquals(2, bridgeTable.getDescendants("B").size());
        assertEquals(1, bridgeTable.getDescendants("C").size());
        assertEquals(0, bridgeTable.getDescendants("D").size());
        assertEquals(1, bridgeTable.getDescendants("E").size());
        assertEquals(0, bridgeTable.getDescendants("F").size());
        
        bridgeTable.removeLink("B", "C");
        
        assertEquals(0, bridgeTable.getAncestors("A").size());
        assertEquals(1, bridgeTable.getAncestors("B").size());
        assertEquals(0, bridgeTable.getAncestors("C").size());
        assertEquals(1, bridgeTable.getAncestors("D").size());
        assertEquals(0, bridgeTable.getAncestors("E").size());
        assertEquals(1, bridgeTable.getAncestors("F").size());
        
        assertEquals(1, bridgeTable.getDescendants("A").size());
        assertEquals(0, bridgeTable.getDescendants("B").size());
        assertEquals(1, bridgeTable.getDescendants("C").size());
        assertEquals(0, bridgeTable.getDescendants("D").size());
        assertEquals(1, bridgeTable.getDescendants("E").size());
        assertEquals(0, bridgeTable.getDescendants("F").size());
    }

//    @Test
//    public void test_1M()
//    {
//     // 1M = 21 
//        for (int i = 0; i < 20; i++) 
//        { 
//            BridgeTable<String> bridgeTable = new BridgeTable<String>();
//            long start = System.nanoTime(); 
//            bridgeTable.addLinks(getTreeLinks(i)); 
//            long end = System.nanoTime(); 
//            double elapsed = ((end - start) / 1e9);
//            System.out.println("" + bridgeTable.size() + " in " + elapsed);
//            assertTrue(elapsed < 60);
//        } 
//    }
    
    @Test
    public void test_16k()
    {
     // 1M = 21 
        for (int i = 0; i < 15; i++) 
        { 
            BridgeTable<String> bridgeTable = new BridgeTable<String>();
            long start = System.nanoTime(); 
            bridgeTable.addLinks(getTreeLinks(i)); 
            long end = System.nanoTime(); 
            double elapsed = ((end - start) / 1e9);
            System.out.println("" + bridgeTable.size() + " in " + elapsed);
            assertTrue(elapsed < 60);
        } 
    }
    
//    @Test
//    public void test_1000x1000()
//    {
//        BridgeTable<String> bridgeTable = new BridgeTable<String>();
//        HashSet<Pair<String, String>> links = new HashSet<Pair<String, String>>(); 
//        for (int i = 0; i < 10; i++) 
//        { 
//            for (int j = 0; j < 100; j++) 
//            { 
//                links.addAll(getTreeLinks(10)); 
//            } 
//
//            
//            long start = System.nanoTime(); 
//            bridgeTable.addLinks(links); 
//            long end = System.nanoTime(); 
//            System.out.println("Trees " + bridgeTable.size() + " in " + ((end - start) / 1e9)); 
//
//            start = System.nanoTime(); 
//            for (String key : bridgeTable.keySet()) 
//            { 
//                bridgeTable.getAncestors(key); 
//            } 
//            end = System.nanoTime(); 
//            System.out.println("By key " + bridgeTable.size() + " in " + ((end - start) / 1e9)); 
//        } 
//    }
    
    
    @Test
    public void test_100x100()
    {
        BridgeTable<String> bridgeTable = new BridgeTable<String>();
        HashSet<Pair<String, String>> links = new HashSet<Pair<String, String>>(); 
        for (int i = 0; i < 10; i++) 
        { 
            for (int j = 0; j < 10; j++) 
            { 
                links.addAll(getTreeLinks(7)); 
            } 

            
            long start = System.nanoTime(); 
            bridgeTable.addLinks(links); 
            long end = System.nanoTime(); 
            System.out.println("Trees " + bridgeTable.size() + " in " + ((end - start) / 1e9)); 

            start = System.nanoTime(); 
            for (String key : bridgeTable.keySet()) 
            { 
                bridgeTable.getAncestors(key); 
            } 
            end = System.nanoTime(); 
            System.out.println("By key " + bridgeTable.size() + " in " + ((end - start) / 1e9)); 
        } 
    }
    
    @Test
    public void testSecondary()
    {   
        BridgeTable<String> bridgeTable = new BridgeTable<String>();
        
        bridgeTable.addLink("A", "B");
        bridgeTable.addLink("A", "C");
        bridgeTable.addLink("B", "D");
        bridgeTable.addLink("B", "E");
        bridgeTable.addLink("C", "F");
        bridgeTable.addLink("C", "G");
        
        assertEquals(2, bridgeTable.getDescendants("A", 1).size());
        assertEquals(4, bridgeTable.getDescendants("A", 2).size());
        assertEquals(6, bridgeTable.getDescendants("A", 1, 2).size());
        assertEquals(2, bridgeTable.getDescendants("B", 1).size());
        assertEquals(0, bridgeTable.getDescendants("B", 2).size());
        assertEquals(2, bridgeTable.getDescendants("B", 1, 2).size());
        
        bridgeTable.addLink("N", "O");
        bridgeTable.addLink("N", "P");
        bridgeTable.addLink("O", "Q");
        bridgeTable.addLink("O", "R");
        bridgeTable.addLink("P", "S");
        bridgeTable.addLink("P", "T");
        
        assertEquals(2, bridgeTable.getDescendants("N", 1).size());
        assertEquals(4, bridgeTable.getDescendants("N", 2).size());
        assertEquals(6, bridgeTable.getDescendants("N", 1, 2).size());
        assertEquals(2, bridgeTable.getDescendants("O", 1).size());
        assertEquals(0, bridgeTable.getDescendants("O", 2).size());
        assertEquals(2, bridgeTable.getDescendants("O", 1, 2).size());
        
        bridgeTable.addLink("A", "N");
        assertEquals(3, bridgeTable.getDescendants("A", 1).size());
        assertEquals(2, bridgeTable.getDescendants("B", 1).size());
        
        bridgeTable.addLink("A", "N");
        assertEquals(3, bridgeTable.getDescendants("A", 1).size());
        assertEquals(2, bridgeTable.getDescendants("B", 1).size());
        
        bridgeTable.addLink("B", "N");
        assertEquals(3, bridgeTable.getDescendants("A", 1).size());
        assertEquals(3, bridgeTable.getDescendants("B", 1).size());
        
        bridgeTable.addLink("B", "N");
        assertEquals(3, bridgeTable.getDescendants("A", 1).size());
        assertEquals(3, bridgeTable.getDescendants("B", 1).size());
        
        bridgeTable.removeLink("A", "N");
        assertEquals(3, bridgeTable.getDescendants("A", 1).size());
        assertEquals(3, bridgeTable.getDescendants("B", 1).size());
        
        bridgeTable.removeLink("A", "N");
        assertEquals(2, bridgeTable.getDescendants("A", 1).size());
        assertEquals(3, bridgeTable.getDescendants("B", 1).size());
        
        bridgeTable.removeLink("B", "N");
        assertEquals(2, bridgeTable.getDescendants("A", 1).size());
        assertEquals(3, bridgeTable.getDescendants("B", 1).size());
        
        bridgeTable.removeLink("B", "N");
        
        assertEquals(2, bridgeTable.getDescendants("A", 1).size());
        assertEquals(4, bridgeTable.getDescendants("A", 2).size());
        assertEquals(6, bridgeTable.getDescendants("A", 1, 2).size());
        assertEquals(2, bridgeTable.getDescendants("B", 1).size());
        assertEquals(0, bridgeTable.getDescendants("B", 2).size());
        assertEquals(2, bridgeTable.getDescendants("B", 1, 2).size());
        
        
        assertEquals(2, bridgeTable.getDescendants("N", 1).size());
        assertEquals(4, bridgeTable.getDescendants("N", 2).size());
        assertEquals(6, bridgeTable.getDescendants("N", 1, 2).size());
        assertEquals(2, bridgeTable.getDescendants("O", 1).size());
        assertEquals(0, bridgeTable.getDescendants("O", 2).size());
        assertEquals(2, bridgeTable.getDescendants("O", 1, 2).size());
        
    }
    
    private Set<Pair<String, String>> getTreeLinks(int depth) 
    { 
        int count = 0; 
        String base = "" + System.nanoTime(); 

        HashSet<String> currentRow = new HashSet<String>(); 
        HashSet<String> lastRow = new HashSet<String>(); 

        HashSet<Pair<String, String>> links = new HashSet<Pair<String, String>>(); 

        for (int i = 0; i < depth; i++) 
        { 
            if (lastRow.size() == 0) 
            { 
                currentRow.add("GROUP_" + base + "_" + count); 
                count++; 
            } 
            else 
            { 
                for (String group : lastRow) 
                { 
                    String newGroup = "GROUP_" + base + "_" + count; 
                    currentRow.add(newGroup); 
                    count++; 
                    links.add(new Pair<String, String>(group, newGroup)); 

                    newGroup = "GROUP_" + base + "_" + count; 
                    currentRow.add(newGroup); 
                    count++; 
                    links.add(new Pair<String, String>(group, newGroup)); 
                } 
            } 
            lastRow = currentRow; 
            currentRow = new HashSet<String>(); 
        } 

        return links; 

    } 

}
