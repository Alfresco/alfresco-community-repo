
package org.alfresco.repo.search.impl.solr.facet;

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
    
    @Test public void canCreateFromShortFormWithNoPrefix() throws Exception
    {
        // The sensibleness of this from an Alfresco perspective is questionable.
        // But this is what we must do to support 'QName' strings like "Site" or "Tag" in the REST API.
        assertEquals(QName.createQName(null, "localName"),
                FacetQNameUtils.createQName("localName", resolver));
    }
    
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