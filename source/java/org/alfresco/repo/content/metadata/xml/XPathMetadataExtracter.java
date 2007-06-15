/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * An extracter that pulls values from XML documents using configurable XPath
 * statements.  It is not possible to list a default set of mappings - this is
 * down to the configuration only.
 * <p>
 * When an instance of this extracter is configured, XPath statements should be
 * provided to extract all the available metadata.  The implementation is sensitive
 * to what is actually requested by the
 * {@linkplain AbstractMappingMetadataExtracter#setMapping(Map) configured mapping}
 * and will only perform the queries necessary to fulfill the requirements.
 * <p>
 * To summarize, there are two configurations required for this class:
 * <ul>
 *   <li>
 *     A mapping of all reasonable document properties to XPath statements.
 *     See {@link AbstractMappingMetadataExtracter#setMappingProperties(java.util.Properties)}.
 *   </li>
 *   <li>
 *     A mapping of document property names to Alfresco repository model QNames.
 *     See {@link #setXPathMappingProperties(Properties).}
 *   </li>
 * </ul>
 * <p>
 * The mapping of document properties to XPaths must look as follows:
 * <pre>
 *    # Get the author
 *    author=/root/author@name
 * </pre>
 * 
 * @author Derek Hulley
 */
public class XPathMetadataExtracter extends AbstractMappingMetadataExtracter implements NamespaceContext
{
    public static String[] SUPPORTED_MIMETYPES = new String[] {MimetypeMap.MIMETYPE_XML};
    
    private static Log logger = LogFactory.getLog(XPathMetadataExtracter.class);
    
    private DocumentBuilder documentBuilder;
    private XPathFactory xpathFactory;
    private Map<String, String> namespacesByPrefix;
    private Map<String, XPathExpression> xpathExpressionMapping;

    /**
     * Default constructor
     */
    public XPathMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    /** {@inheritDoc} */
    public String getNamespaceURI(String prefix)
    {
        ParameterCheck.mandatoryString("prefix", prefix);
        return namespacesByPrefix.get(prefix);
    }

    /** {@inheritDoc} */
    public String getPrefix(String namespaceURI)
    {
        ParameterCheck.mandatoryString("namespaceURI", namespaceURI);
        for (Map.Entry<String, String> entry : namespacesByPrefix.entrySet())
        {
            if (namespaceURI.equals(entry.getValue()))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public Iterator getPrefixes(String namespaceURI)
    {
        ParameterCheck.mandatoryString("namespaceURI", namespaceURI);
        List<String> prefixes = new ArrayList<String>(2);
        for (Map.Entry<String, String> entry : namespacesByPrefix.entrySet())
        {
            if (namespaceURI.equals(entry.getValue()))
            {
                prefixes.add(entry.getKey());
            }
        }
        return prefixes.iterator();
    }

    /**
     * Set the properties file that maps document properties to the XPath statements
     * necessary to retrieve them.
     * <p> 
     * The Xpath mapping is of the form:
     * <pre>
     * # Namespaces prefixes
     * namespace.prefix.cm=http://www.alfresco.org/model/content/1.0
     * namespace.prefix.my=http://www....com/alfresco/1.0
     * 
     * # Mapping
     * editor=/cm:some-xpath-1
     * title=/my:some-xpath-2
     * </pre>
     */
    public void setXpathMappingProperties(Properties xpathMappingProperties)
    {
        namespacesByPrefix = new HashMap<String, String>(7);
        xpathExpressionMapping = new HashMap<String, XPathExpression>(17);
        readXPathMappingProperties(xpathMappingProperties);
    }
    
    @Override
    protected void init()
    {
        PropertyCheck.mandatory(this, "xpathMappingProperties", xpathExpressionMapping);
        try
        {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xpathFactory = XPathFactory.newInstance();
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to initialize XML metadata extractor", e);
        }
        super.init();
    }

    /**
     * It is not possible to have any default mappings, but something has to be returned.
     * 
     * @return          Returns an empty map
     */
    @Override
    protected Map<String, Set<QName>> getDefaultMapping()
    {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            Document doc = documentBuilder.parse(is);
            Map<String, Serializable> rawProperties = processDocument(doc);
            if (logger.isDebugEnabled())
            {
                logger.debug("\n" +
                        "Extracted XML metadata: \n" +
                        "   Reader:  " + reader + "\n" +
                        "   Results: " + rawProperties);
            }
            return rawProperties;
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }
    }
    
    /**
     * Executes all the necessary XPath statements to extract values.
     */
    protected Map<String, Serializable> processDocument(Document document) throws Throwable
    {
        Map<String, Serializable> rawProperties = super.newRawMap();
        
        // Execute all the XPaths that we saved
        for (Map.Entry<String, XPathExpression> element : xpathExpressionMapping.entrySet())
        {
            String documentProperty = element.getKey();
            XPathExpression xpathExpression = element.getValue();
            // Execute it
            String value = xpathExpression.evaluate(document);
            // Put the value
            rawProperties.put(documentProperty, value);
        }
        // Done
        return rawProperties;
    }
    
    /**
     * A utility method to convert mapping properties to the Map form.
     * 
     * @see #setMappingProperties(Properties)
     */
    protected void readXPathMappingProperties(Properties xpathMappingProperties)
    {
        // Get the namespaces
        for (Map.Entry entry : xpathMappingProperties.entrySet())
        {
            String propertyName = (String) entry.getKey();
            if (propertyName.startsWith("namespace.prefix."))
            {
                String prefix = propertyName.substring(17);
                String namespace = (String) entry.getValue();
                namespacesByPrefix.put(prefix, namespace);
            }
        }
        // Get the mapping that will be applied by the base class
        Map<String, Set<QName>> finalMapping = getMapping();
        // Create the mapping
        for (Map.Entry entry : xpathMappingProperties.entrySet())
        {
            String documentProperty = (String) entry.getKey();
            String xpathStr = (String) entry.getValue();
            if (documentProperty.startsWith(NAMESPACE_PROPERTY_PREFIX))
            {
                // Ignore these now
                continue;
            }
            // If the property is not going to be mapped, then just ignore it too
            if (!finalMapping.containsKey(documentProperty))
            {
                continue;
            }
            // Construct the XPath
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(this);
            XPathExpression xpathExpression = null;
            try
            {
                xpathExpression = xpath.compile(xpathStr);
            }
            catch (XPathExpressionException e)
            {
                throw new AlfrescoRuntimeException(
                        "Failed to path XPath expression: \n" +
                        "   Document property: " + documentProperty + "\n" +
                        "   XPath:             " + xpathStr);
            }
            // Persist it
            xpathExpressionMapping.put(documentProperty, xpathExpression);
            if (logger.isDebugEnabled())
            {
                logger.debug("Added mapping from " + documentProperty + " to " + xpathExpression);
            }
        }
        // Done
    }
}
