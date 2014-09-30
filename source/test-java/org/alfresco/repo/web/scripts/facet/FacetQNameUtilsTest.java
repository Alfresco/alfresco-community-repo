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

package org.alfresco.repo.web.scripts.facet;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

/**
 * Unit tests for {@link FacetQNameUtils}.
 * @since 5.0
 */
public class FacetQNameUtilsTest
{
    /** A test-only namespace resolver. */
    private final NamespacePrefixResolver resolver = new NamespacePrefixResolver()
    {
        private final List<String> uris     = Arrays.asList(new String[] { "http://www.alfresco.org/model/foo/1.0" });
        private final List<String> prefixes = Arrays.asList(new String[] { "foo" });
        
        @Override public Collection<String> getURIs()     { return this.uris; }
        @Override public Collection<String> getPrefixes() { return this.prefixes; }
        
        @Override public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
        {
            if (uris.contains(namespaceURI))
            { return prefixes; }
            else
            { throw new NamespaceException("Unrecognised namespace: " + namespaceURI); }
        }
        
        @Override public String getNamespaceURI(String prefix) throws NamespaceException
        {
            if (prefixes.contains(prefix))
            { return "http://www.alfresco.org/model/foo/1.0"; }
            else
            { throw new NamespaceException("Unrecognised prefix: " + prefix); }
        }
    };
    
    @Test public void canCreateFromShortForm() throws Exception
    {
        assertEquals(QName.createQName("http://www.alfresco.org/model/foo/1.0", "localName"),
                     FacetQNameUtils.createQName("foo:localName", resolver));
    }
    
    @Test public void canCreateFromLongForm() throws Exception
    {
        assertEquals(QName.createQName("http://www.alfresco.org/model/foo/1.0", "localName"),
                     FacetQNameUtils.createQName("{http://www.alfresco.org/model/foo/1.0}localName", resolver));
    }
    
    // Note: it doesn't really make sense to have a short-form qname with no prefix.
    @Test public void canCreateFromLongFormWithNoPrefix() throws Exception
    {
        assertEquals(QName.createQName(null, "localName"),
                     FacetQNameUtils.createQName("{}localName", resolver));
    }
    
    @Test public void canCreateLongFormQnameWithUnrecognisedUri() throws Exception
    {
        // Intentionally no validation of URIs against dictionary.
        assertEquals(QName.createQName("http://www.alfresco.org/model/illegal/1.0", "localName"),
                FacetQNameUtils.createQName("{http://www.alfresco.org/model/illegal/1.0}localName", resolver));
    }
    
    @Test (expected=NamespaceException.class)
    public void shortFormQnameWithUnrecognisedPrefixFails() throws Exception
    {
        FacetQNameUtils.createQName("illegal:localName", resolver);
    }
}