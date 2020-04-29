/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.namespace;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @see org.alfresco.service.namespace.QName
 * 
 * @author David Caruana
 */
public class QNameTest
{
    private final NamespacePrefixResolver mockResolver = new MockNamespacePrefixResolver();

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameFromInternalStringRepresentationWithEmptyString()
    {
        QName.createQName("");
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameFromInternalStringRepresentationWithInvalidNameCase1()
    {
        QName.createQName("invalid{}name");
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameFromInternalStringRepresentationWithInvalidNameCase2()
    {
        QName.createQName("{name");
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameFromInternalStringRepresentationWithInvalidNameCase3()
    {
        QName.createQName("{}");
    }

    @Test
    public void testCreateQNameFromInternalStringRepresentationWithInvalidNameCase4()
    {
        try
        {
            QName.createQName("{}name");
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
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameWithNoPrefixWithNullString()
    {
        QName.createQName(NamespaceService.ALFRESCO_PREFIX, (String) null);
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameWithNoPrefixWithEmptyString()
    {
        QName.createQName(NamespaceService.ALFRESCO_PREFIX, "");
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameWithPrefixWithNullString()
    {
        QName.createQName(NamespaceService.ALFRESCO_PREFIX, null, mockResolver);
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameWithPrefixWithEmptyString()
    {
        QName.createQName(NamespaceService.ALFRESCO_PREFIX, "", mockResolver);
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameWithPrefixFormatWithEmptyString()
    {
        QName.createQName("", mockResolver);
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateValidLocalNameWithNullString()
    {
        QName.createValidLocalName(null);
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateValidLocalNameWithEmptyString()
    {
        QName.createValidLocalName("");
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameResolveToNameWithNullString()
    {
        QName.resolveToQName(mockResolver, null);
    }

    @Test(expected = InvalidQNameException.class)
    public void testCreateQNameResolveToNameWithEmptyString()
    {
        QName.resolveToQName(mockResolver, "");
    }

    @Test
    public void testCreateQnameWithNoPrefixWithIllegalCharactersThrowsInvalidQNameException()
    {
        char[] illegalCharacters = {'/', '\\', '\n', '\r', '"'};
        for (char illegalCharacter : illegalCharacters)
        {
            try
            {
                String localName = "testLocalNameWith" + illegalCharacter;
                QName.createQName(NamespaceService.ALFRESCO_PREFIX, localName);
                fail("InvalidQNameException not caught for illegalCharacter: " +localName.charAt(localName.indexOf(illegalCharacter)));
            }
            catch (InvalidQNameException ignored)
            {
            }
        }
    }

    @Test
    public void testCreateQnameWithPrefixWithIllegalCharactersThrowsInvalidQNameException()
    {
        char[] illegalCharacters = {'/', '\\', '\n', '\r', '"'};
        for (char illegalCharacter : illegalCharacters)
        {
            try
            {
                String localName = "testLocalNameWith" + illegalCharacter;
                QName.createQName(NamespaceService.ALFRESCO_PREFIX, localName, mockResolver);
                fail("InvalidQNameException not caught for illegalCharacter: " +localName.charAt(localName.indexOf(illegalCharacter)));
            }
            catch (InvalidQNameException ignored)
            {
            }
        }
    }

    @Test
    public void testCreateQnameWithPrefixFormatWithIllegalCharactersThrowsInvalidQNameException()
    {
        char[] illegalCharacters = {'/', '\\', '\n', '\r', '"'};
        for (char illegalCharacter : illegalCharacters)
        {
            try
            {
                String localName = "testPrefix:testLocalNameWith" + illegalCharacter;
                QName.createQName(localName, mockResolver);
                fail("InvalidQNameException not caught for illegalCharacter: " +localName.charAt(localName.indexOf(illegalCharacter)));
            }
            catch (InvalidQNameException ignored)
            {
            }
        }
    }

    @Test
    public void testCreateValidLocalNameWithIllegalCharactersThrowsInvalidQNameException()
    {
        char[] illegalCharacters = {'/', '\\', '\n', '\r', '"'};
        for (char illegalCharacter : illegalCharacters)
        {
            try
            {
                String localName = "testNameWith" + illegalCharacter;
                QName.createValidLocalName(localName);
                fail("InvalidQNameException not caught for illegalCharacter: " +localName.charAt(localName.indexOf(illegalCharacter)));
            }
            catch (InvalidQNameException ignored)
            {
            }
        }
    }

    @Test
    public void testResolveToQNameWithIllegalCharactersThrowsInvalidQNameException() {
        char[] illegalCharacters = {'/', '\\', '\n', '\r', '"'};
        for (char illegalCharacter : illegalCharacters)
        {
            try
            {
                String localName = "testNameWith" + illegalCharacter;
                QName.resolveToQName(mockResolver, localName);
                fail("InvalidQNameException not caught for illegalCharacter: " +localName.charAt(localName.indexOf(illegalCharacter)));
            }
            catch (InvalidQNameException ignored)
            {
            }
        }
    }

    @Test
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

    @Test
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

    @Test
    public void testCommonTypes()
    {
        QName qname3 = QName.createQName("{http://www.alfresco.org/model/content/1.0}created");
        assertEquals("{http://www.alfresco.org/model/content/1.0}created", qname3.toString());

        QName qname4 = QName.createQName("{http://www.alfresco.org/model/content/1.0}creator.__");
        assertEquals("{http://www.alfresco.org/model/content/1.0}creator.__", qname4.toString());

        QName qname5 = QName.createQName("{http://www.alfresco.org/model/content/1.0}content.mimetype");
        assertEquals("{http://www.alfresco.org/model/content/1.0}content.mimetype", qname5.toString());
    }

    @Test
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

    @Test
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

    public static class MockNamespacePrefixResolver
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
