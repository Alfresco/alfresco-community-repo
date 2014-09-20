
package org.alfresco.repo.module;

/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

/**
 * I took the existing @see VersionNumberTest class and added some
 * additional tests for ModuleVersionNumber.
 * 
 * @author Gethin James
 */
public class ModuleVersionNumberTest extends TestCase
{
    public void testCreate()
    {
        ModuleVersionNumber version0 = ModuleVersionNumber.VERSION_ZERO;
        assertNotNull(version0);

        ModuleVersionNumber versionBig = ModuleVersionNumber.VERSION_BIG;
        assertNotNull(versionBig);

        ModuleVersionNumber version1 = new ModuleVersionNumber("1");
        assertNotNull(version1);

        ModuleVersionNumber version2 = new ModuleVersionNumber("1.2");
        assertNotNull(version2);

        ModuleVersionNumber version3 = new ModuleVersionNumber("1.2.3");
        assertNotNull(version3);

        ModuleVersionNumber versiona = new ModuleVersionNumber("1.2.3a");
        assertNotNull(versiona);

        ModuleVersionNumber versionSnap = new ModuleVersionNumber("1.2.3-SNAPSHOT");
        assertNotNull(versionSnap);

        ModuleVersionNumber versionFinal = new ModuleVersionNumber("1.2.3-final");
        assertNotNull(versionFinal);

        ModuleVersionNumber versionMixed = new ModuleVersionNumber("0.1-incubating-unreleased");
        assertNotNull(versionMixed);

        versionMixed = new ModuleVersionNumber("3.2.6-alfresco-patched");
        assertNotNull(versionMixed);

        versionMixed = new ModuleVersionNumber("0.2-20120518");
        assertNotNull(versionMixed);

        ModuleVersionNumber version1s = new ModuleVersionNumber("4.2.0-SNAPSHOT");
        assertNotNull(version1s);
        ModuleVersionNumber version4c = new ModuleVersionNumber("4.2.0-C");
        assertNotNull(version4c);
        ModuleVersionNumber version16 = new ModuleVersionNumber("4.0.1.16");
        assertNotNull(version16);
        ModuleVersionNumber versionn1 = new ModuleVersionNumber("4.0.16.1.7");
        assertNotNull(versionn1);
        ModuleVersionNumber versionn2 = new ModuleVersionNumber("4.0.1.2.8.9");
        assertNotNull(versionn2);
        ModuleVersionNumber versionc = new ModuleVersionNumber("4.2.c");
        assertNotNull(versionc);
        ModuleVersionNumber versionb = new ModuleVersionNumber("1.0.b");
        assertNotNull(versionb);
        ModuleVersionNumber versionsnap = new ModuleVersionNumber("1.0-SNAPSHOT");
        assertNotNull(versionsnap);
    }

    public void testEquals()
    {
        ModuleVersionNumber version0 = new ModuleVersionNumber("1");
        ModuleVersionNumber version1 = new ModuleVersionNumber("1.2");
        ModuleVersionNumber version2 = new ModuleVersionNumber("1.2");
        ModuleVersionNumber version3 = new ModuleVersionNumber("1.2.3");
        ModuleVersionNumber version4 = new ModuleVersionNumber("1.2.3");
        ModuleVersionNumber version5 = new ModuleVersionNumber("1.3.3");
        ModuleVersionNumber version6 = new ModuleVersionNumber("1.0");
        ModuleVersionNumber versiona = new ModuleVersionNumber("1.0.a");
        ModuleVersionNumber versionb = new ModuleVersionNumber("1.0.b");
        ModuleVersionNumber versionsnap = new ModuleVersionNumber("1.0-SNAPSHOT");

        assertFalse(version0.equals(version1));
        assertTrue(version1.equals(version2));
        assertFalse(version2.equals(version3));
        assertTrue(version3.equals(version4));
        assertFalse(version4.equals(version5));
        assertTrue(version0.equals(version6));
        assertFalse(versiona.equals(version0));
        assertFalse(versiona.equals(versionb));
        assertFalse(versionsnap.equals(version6));
        assertFalse(versionsnap.equals(versiona));
        assertFalse(versionsnap.equals(versionb));
    }

    public void testCompare()
    {
        ModuleVersionNumber version0 = new ModuleVersionNumber("1");
        ModuleVersionNumber version1 = new ModuleVersionNumber("1.2");
        ModuleVersionNumber version2 = new ModuleVersionNumber("1.2");
        ModuleVersionNumber version3 = new ModuleVersionNumber("1.2.3");
        ModuleVersionNumber version4 = new ModuleVersionNumber("1.11");
        ModuleVersionNumber version5 = new ModuleVersionNumber("1.3.3");
        ModuleVersionNumber version6 = new ModuleVersionNumber("2.0");
        ModuleVersionNumber version7 = new ModuleVersionNumber("2.0.1");
        ModuleVersionNumber version8 = new ModuleVersionNumber("10.0");
        ModuleVersionNumber version9 = new ModuleVersionNumber("10.3");
        ModuleVersionNumber version10 = new ModuleVersionNumber("11.1");

        assertEquals(-1, version0.compareTo(version1));
        assertEquals(1, version1.compareTo(version0));
        assertEquals(0, version1.compareTo(version2));
        assertEquals(-1, version2.compareTo(version3));
        assertEquals(-1, version2.compareTo(version4));
        assertEquals(-1, version3.compareTo(version5));
        assertEquals(1, version6.compareTo(version5));
        assertEquals(-1, version6.compareTo(version7));
        assertEquals(-1, version1.compareTo(version8));
        assertEquals(-1, version8.compareTo(version9));
        assertEquals(-1, version9.compareTo(version10));

        ModuleVersionNumber version1point4 = new ModuleVersionNumber("1.4");
        ModuleVersionNumber version1point4a = new ModuleVersionNumber("1.4.a");
        ModuleVersionNumber version1point4b = new ModuleVersionNumber("1.4.b");
        ModuleVersionNumber version1point4c = new ModuleVersionNumber("1.4.c");
        ModuleVersionNumber version1point4d = new ModuleVersionNumber("1.4.d");
        ModuleVersionNumber version1point4snapshot = new ModuleVersionNumber("1.4-SNAPSHOT");
        ModuleVersionNumber version1point40 = new ModuleVersionNumber("1.4.0");

        assertEquals(1, ModuleVersionNumber.VERSION_BIG.compareTo(version1));
        assertEquals(1, ModuleVersionNumber.VERSION_BIG.compareTo(version0));
        assertEquals(1, ModuleVersionNumber.VERSION_BIG.compareTo(version8));
        assertEquals(1, ModuleVersionNumber.VERSION_BIG.compareTo(version1point4b));
        assertEquals(1, ModuleVersionNumber.VERSION_BIG.compareTo(version1point4snapshot));

        assertEquals(0, version1point4.compareTo(new ModuleVersionNumber("1.4")));
        assertTrue(version1point4.compareTo(version1point4a) < 1);
        assertTrue(version1point4.compareTo(version1point4snapshot) > 0);

        assertTrue(version1point4b.compareTo(version1point4a) > 0);
        assertTrue(version1point4b.compareTo(version1point4snapshot) > 0);

        assertTrue(version1point4c.compareTo(version1point4b) > 0);
        assertTrue(version1point4c.compareTo(version1point4d) < 0);

        assertTrue(version1point4d.compareTo(version1point4c) > 0);
        assertTrue(version1point40.compareTo(version1point4) == 0);// the same

        ModuleVersionNumber versionBase = new ModuleVersionNumber("0.1");
        ModuleVersionNumber versionMixed = new ModuleVersionNumber("0.1-incubating-unreleased");
        assertTrue(versionMixed.compareTo(versionBase) > 0);

        versionBase = new ModuleVersionNumber("3.2.6");
        versionMixed = new ModuleVersionNumber("3.2.6-alfresco-patched");
        assertTrue(versionMixed.compareTo(versionBase) > 0);

        versionBase = new ModuleVersionNumber("0.2");
        versionMixed = new ModuleVersionNumber("0.2-20120518");
        assertTrue(versionMixed.compareTo(versionBase) > 0);
    }
    
    public void testSerialize() throws IOException, ClassNotFoundException
    {
        ModuleVersionNumber version0Before = new ModuleVersionNumber("1");
        ModuleVersionNumber version6Before = new ModuleVersionNumber("1.0");
        ModuleVersionNumber versionaBefore = new ModuleVersionNumber("1.0.a");
        ModuleVersionNumber versionbBefore = new ModuleVersionNumber("1.0.b");
        ModuleVersionNumber versionsnapBefore = new ModuleVersionNumber("1.0-SNAPSHOT");
        
        //read and write versions then check they are the same.
        ModuleVersionNumber version0 = writeAndRead(version0Before);
        ModuleVersionNumber version6 = writeAndRead(version6Before);
        ModuleVersionNumber versiona = writeAndRead(versionaBefore);
        ModuleVersionNumber versionb = writeAndRead(versionbBefore);
        ModuleVersionNumber versionsnap = writeAndRead(versionsnapBefore);
        
        assertTrue(version0.equals(version0Before));
        assertTrue(version6.equals(version6Before));
        assertTrue(versiona.equals(versionaBefore));
        assertTrue(versionb.equals(versionbBefore));
        assertTrue(versionsnap.equals(versionsnapBefore));
        
        assertTrue(version0.equals(version6));
        assertFalse(versiona.equals(version0));
        assertFalse(versiona.equals(versionb));
        assertFalse(versionsnap.equals(version6));
        assertFalse(versionsnap.equals(versiona));
        assertFalse(versionsnap.equals(versionb));
    }

    private ModuleVersionNumber writeAndRead(ModuleVersionNumber versionNumber) throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(versionNumber);
        oos.flush();
        oos.close();
        
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (ModuleVersionNumber) objectInputStream.readObject();
    }
}
