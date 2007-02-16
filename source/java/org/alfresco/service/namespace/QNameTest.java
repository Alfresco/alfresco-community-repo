/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.namespace;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;



/**
 * @see org.alfresco.service.namespace.QName
 * 
 * @author David Caruana
 */
public class QNameTest extends TestCase
{

    public QNameTest(String name)
    {
        super(name);
    }


    public void testInvalidQName() throws Exception
    {
        try
        {
            QName qname = QName.createQName("");
            fail("Missing local name was not caught");
        }
        catch (InvalidQNameException e)
        {
        }

        try
        {
            QName qname = QName.createQName("invalid{}name");
            fail("Namespace not at start was not caught");
        }
        catch (InvalidQNameException e)
        {
        }

        try
        {
            QName qname = QName.createQName("{name");
            fail("Missing closing namespace token was not caught");
        }
        catch (InvalidQNameException e)
        {
        }

        try
        {
            QName qname = QName.createQName("{}");
            fail("Missing local name after namespace was not caught");
        }
        catch (InvalidQNameException e)
        {
        }

        try
        {
            QName qname = QName.createQName("{}name");
        }
        catch (InvalidQNameException e)
        {
            fail("Empty namespace is valid");
        }

        try
        {
            QName qname = QName.createQName("{namespace}name");
            assertEquals("namespace", qname.getNamespaceURI());
            assertEquals("name", qname.getLocalName());
        }
        catch (InvalidQNameException e)
        {
            fail("Valid namespace has been thrown out");
        }

        try
        {
            QName qname = QName.createQName((String) null, (String) null);
            fail("Null name was not caught");
        }
        catch (InvalidQNameException e)
        {
        }

        try
        {
            QName qname = QName.createQName((String) null, "");
            fail("Empty name was not caught");
        }
        catch (InvalidQNameException e)
        {
        }
        
    }


    public void testConstruction()
    {
        QName qname1 = QName.createQName("namespace1", "name1");
        assertEquals("namespace1", qname1.getNamespaceURI());
        assertEquals("name1", qname1.getLocalName());

        QName qname2 = QName.createQName("{namespace2}name2");
        assertEquals("namespace2", qname2.getNamespaceURI());
        assertEquals("name2", qname2.getLocalName());

        QName qname3 = QName.createQName(null, "name3");
        assertEquals("", qname3.getNamespaceURI());

        QName qname4 = QName.createQName("", "name4");
        assertEquals("", qname4.getNamespaceURI());

        QName qname5 = QName.createQName("{}name5");
        assertEquals("", qname5.getNamespaceURI());

        QName qname6 = QName.createQName("name6");
        assertEquals("", qname6.getNamespaceURI());
    }


    public void testStringRepresentation()
    {
        QName qname1 = QName.createQName("namespace", "name1");
        assertEquals("{namespace}name1", qname1.toString());

        QName qname2 = QName.createQName("", "name2");
        assertEquals("{}name2", qname2.toString());

        QName qname3 = QName.createQName("{namespace}name3");
        assertEquals("{namespace}name3", qname3.toString());

        QName qname4 = QName.createQName("{}name4");
        assertEquals("{}name4", qname4.toString());

        QName qname5 = QName.createQName("name5");
        assertEquals("{}name5", qname5.toString());
    }


    public void testEquality()
    {
        QName qname1 = QName.createQName("namespace", "name");
        QName qname2 = QName.createQName("namespace", "name");
        QName qname3 = QName.createQName("{namespace}name");
        assertEquals(qname1, qname2);
        assertEquals(qname1, qname3);
        assertEquals(qname1.hashCode(), qname2.hashCode());
        assertEquals(qname1.hashCode(), qname3.hashCode());

        QName qname4 = QName.createQName("", "name");
        QName qname5 = QName.createQName("", "name");
        QName qname6 = QName.createQName(null, "name");
        assertEquals(qname4, qname5);
        assertEquals(qname4, qname6);
        assertEquals(qname4.hashCode(), qname5.hashCode());
        assertEquals(qname4.hashCode(), qname6.hashCode());

        QName qname7 = QName.createQName("namespace", "name");
        QName qname8 = QName.createQName("namespace", "differentname");
        assertFalse(qname7.equals(qname8));
        assertFalse(qname7.hashCode() == qname8.hashCode());

        QName qname9 = QName.createQName("namespace", "name");
        QName qname10 = QName.createQName("differentnamespace", "name");
        assertFalse(qname9.equals(qname10));
        assertFalse(qname9.hashCode() == qname10.hashCode());
    }


    public void testPrefix()
    {
        try
        {
            QName noResolver = QName.createQName(NamespaceService.ALFRESCO_PREFIX, "alfresco prefix", null);
            fail("Null resolver was not caught");
        }
        catch (IllegalArgumentException e)
        {
        }

        NamespacePrefixResolver mockResolver = new MockNamespacePrefixResolver();
        QName qname1 = QName.createQName(NamespaceService.ALFRESCO_PREFIX, "alfresco prefix", mockResolver);
        assertEquals(NamespaceService.ALFRESCO_URI, qname1.getNamespaceURI());
        QName qname2 = QName.createQName("", "default prefix", mockResolver);
        assertEquals(NamespaceService.DEFAULT_URI, qname2.getNamespaceURI());
        QName qname3 = QName.createQName(null, "null default prefix", mockResolver);
        assertEquals(NamespaceService.DEFAULT_URI, qname3.getNamespaceURI());

        try
        {
            QName qname4 = QName.createQName("garbage", "garbage prefix", mockResolver);
            fail("Invalid Prefix was not caught");
        }
        catch (NamespaceException e)
        {
        }
    }

    
    private static class MockNamespacePrefixResolver
        implements NamespacePrefixResolver
    {

        public String getNamespaceURI(String prefix)
        {
            if (prefix.equals(NamespaceService.DEFAULT_PREFIX))
            {
                return NamespaceService.DEFAULT_URI;
            }
            else if (prefix.equals(NamespaceService.ALFRESCO_PREFIX))
            {
                return NamespaceService.ALFRESCO_URI;
            }
            throw new NamespaceException("Prefix " + prefix + " not registered");
        }

        public Collection<String> getPrefixes(String namespaceURI)
        {
            throw new NamespaceException("URI " + namespaceURI + " not registered");
        }

        public Collection<String> getPrefixes()
        {
            HashSet<String> prefixes = new HashSet<String>();
            prefixes.add(NamespaceService.DEFAULT_PREFIX);
            prefixes.add(NamespaceService.ALFRESCO_PREFIX);
            return prefixes;
        }
        
        public Collection<String> getURIs()
        {
            HashSet<String> uris = new HashSet<String>();
            uris.add(NamespaceService.DEFAULT_URI);
            uris.add(NamespaceService.ALFRESCO_URI);
            return uris;
        }
        
    }
    
}
