package org.alfresco.repo.transfer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.io.OutputFormat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A simple class whose primary purpose is to wrap the fairly unfriendly interface presented by 
 * the dom4j XMLWriter with one that is simpler to use.
 * 
 * @author Brian
 *
 */
public class XMLWriter
{
    private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

    private org.dom4j.io.XMLWriter dom4jWriter;
    private NamespaceMap prefixResolver = new NamespaceMap();
    private OutputStream output;

    public XMLWriter(OutputStream outputStream, boolean prettyPrint, String encoding)
            throws UnsupportedEncodingException
    {
        OutputFormat format = prettyPrint ? OutputFormat.createPrettyPrint() : OutputFormat.createCompactFormat();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(3);
        format.setEncoding(encoding);
        output = outputStream;
        this.dom4jWriter = new org.dom4j.io.XMLWriter(outputStream, format);
    }

    public void startDocument()
    {
        try
        {
            dom4jWriter.startDocument();
        }
        catch (SAXException e)
        {
            //Can't happen
        }

    }

    public void startElement(QName name)
    {
        startElement(name, null);
    }

    public void startElement(QName name, Attributes attrs)
    {
        if (attrs == null)
        {
            attrs = EMPTY_ATTRIBUTES;
        }
        try
        {
            dom4jWriter.startElement(name.getNamespaceURI(), name.getLocalName(), name.toPrefixString(prefixResolver),
                    attrs);
        }
        catch (SAXException e)
        {
            //Can't happen
        }
    }

    public void endElement(QName name)
    {
        try
        {
            dom4jWriter.endElement(name.getNamespaceURI(), name.getLocalName(), name.toPrefixString(prefixResolver));
        }
        catch (SAXException e)
        {
            //Can't happen
        }
    }

    public void startPrefixMapping(String prefix, String uri)
    {
        prefixResolver.map(prefix, uri);
        try
        {
            dom4jWriter.startPrefixMapping(prefix, uri);
        }
        catch (SAXException e)
        {
            //Can't happen
        }
    }

    public void endPrefixMapping(String prefix)
    {
        prefixResolver.unmap(prefix);
        try
        {
            dom4jWriter.endPrefixMapping(prefix);
        }
        catch (SAXException e)
        {
            //Can't happen
        }
    }

    public void endDocument()
    {
        try
        {
            for (String prefix : prefixResolver.getPrefixes())
            {
                endPrefixMapping(prefix);
            }
            dom4jWriter.endDocument();
        }
        catch (SAXException e)
        {
            //Can't actually happen
        }
    }

    public void close() throws IOException
    {
        output.flush();
        output.close();
    }

    public void addAttribute(AttributesImpl attrs, QName name, String value)
    {
        attrs.addAttribute(name.getNamespaceURI(), name.getLocalName(), name.toPrefixString(prefixResolver), "String",
                value);
    }

    public void addAttribute(AttributesImpl attrs, QName name, int value)
    {
        attrs.addAttribute(name.getNamespaceURI(), name.getLocalName(), name.toPrefixString(prefixResolver), "int",
                Integer.toString(value));
    }
    

    /**
     * A local namespace prefix resolver class that allows the XMLWriter interface to be simplified to use proper QNames
     *
     */
    private static class NamespaceMap implements NamespacePrefixResolver
    {
        private Map<String, Set<String>> uriToPrefixesMap = new TreeMap<String, Set<String>>();
        private Map<String, String> prefixToUriMap = new TreeMap<String, String>();

        public void map(String prefix, String uri)
        {
            Set<String> prefixes = uriToPrefixesMap.get(uri);
            if (prefixes == null)
            {
                prefixes = new TreeSet<String>();
                uriToPrefixesMap.put(uri, prefixes);
            }
            prefixes.add(prefix);
            prefixToUriMap.put(prefix, uri);
        }

        public void unmap(String prefix)
        {
            String uri = prefixToUriMap.remove(prefix);
            if (uri != null)
            {
                uriToPrefixesMap.get(uri).remove(prefix);
            }
        }

        public Collection<String> getURIs()
        {
            return uriToPrefixesMap.keySet();
        }

        public Collection<String> getPrefixes()
        {
            return prefixToUriMap.keySet();
        }

        public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
        {
            return Collections.unmodifiableCollection(uriToPrefixesMap.get(namespaceURI));
        }

        public String getNamespaceURI(String prefix) throws NamespaceException
        {
            return prefixToUriMap.get(prefix);
        }
    }
}
