/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.content.metadata.xml;

import java.io.FileNotFoundException;
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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * All values are extracted as text values and therefore all XPath statements must evaluate to a node
 * that can be rendered as text.
 * 
 * @see AbstractMappingMetadataExtracter#setMappingProperties(Properties)
 * @see #setXpathMappingProperties(Properties)
 * @since 2.1
 * @author Derek Hulley
 */
public class XPathMetadataExtracter extends AbstractMappingMetadataExtracter implements NamespaceContext
{
    public static String[] SUPPORTED_MIMETYPES = new String[] {MimetypeMap.MIMETYPE_XML};
    
    private static Log logger = LogFactory.getLog(XPathMetadataExtracter.class);
    
    private DocumentBuilder documentBuilder;
    private DocumentBuilder dtdIgnoringDocumentBuilder;
    private XPathFactory xpathFactory;
    private Map<String, String> namespacesByPrefix;
    private Map<String, XPathExpression> xpathExpressionMapping;

    /**
     * Default constructor
     */
    public XPathMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
        try
        {
            DocumentBuilderFactory normalFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = normalFactory.newDocumentBuilder();
            
            DocumentBuilderFactory dtdIgnoringFactory = DocumentBuilderFactory.newInstance();
            dtdIgnoringFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dtdIgnoringFactory.setFeature("http://xml.org/sax/features/validation", false);
            dtdIgnoringDocumentBuilder = dtdIgnoringFactory.newDocumentBuilder();
            
            xpathFactory = XPathFactory.newInstance();
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to initialize XML metadata extractor", e);
        }
    }

    /** {@inheritDoc} */
    public String getNamespaceURI(String prefix)
    {
        ParameterCheck.mandatoryString("prefix", prefix);
        String namespace = namespacesByPrefix.get(prefix);
        if (namespace == null)
        {
            throw new AlfrescoRuntimeException("Prefix '" + prefix + "' is not associated with a namespace.");
        }
        return namespace;
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
     * namespace.prefix.my=http://www....com/alfresco/1.0
     * 
     * # Mapping
     * editor=/my:example-element/@cm:editor
     * title=/my:example-element/text()
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
        // Get the base class to set up its mappings
        super.init();
        // Remove all XPath expressions that aren't going to be used
        Map<String, Set<QName>> mapping = getMapping();
        Set<String> xpathExpressionMappingKeys = new HashSet<String>(xpathExpressionMapping.keySet());
        for (String xpathMappingKey : xpathExpressionMappingKeys)
        {
            if (!mapping.containsKey(xpathMappingKey))
            {
                xpathExpressionMapping.remove(xpathMappingKey);
            }
        }
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
            
            Document doc;
            try 
            {
                // Try with the default settings
                doc = documentBuilder.parse(is);
            }
            catch(FileNotFoundException e)
            {
                // The XML depends on a DTD we don't have available
                // Try to parse it without using DTDs. (This may mean we miss
                //  out on some entities, but it's better than nothing!)
                is = reader.getReader().getContentInputStream();
                doc = dtdIgnoringDocumentBuilder.parse(is);
            }
            
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
            // Get the value, assuming it is a nodeset
            Serializable value = null;
            try
            {
                value = getNodeSetValue(document, xpathExpression);
            }
            catch (XPathExpressionException e)
            {
                // That didn't work, so give it a try as a STRING
                value = getStringValue(document, xpathExpression);
            }
            // Put the value
            super.putRawValue(documentProperty, value, rawProperties);
        }
        // Done
        return rawProperties;
    }
    
    private Serializable getStringValue(Document document, XPathExpression xpathExpression) throws XPathExpressionException
    {
        String value = (String) xpathExpression.evaluate(document, XPathConstants.STRING);
        // Done
        return value;
    }
    
    private Serializable getNodeSetValue(Document document, XPathExpression xpathExpression) throws XPathExpressionException
    {
        // Execute it
        NodeList nodeList = null;
        try
        {
            nodeList = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
        }
        catch (XPathExpressionException e)
        {
            // Expression didn't evaluate to a nodelist
            if (logger.isDebugEnabled())
            {
                logger.debug("Unable to evaluate expression and return a NODESET: " + xpathExpression);
            }
            throw e;
        }
        // Convert the value
        Serializable value = null;
        int nodeCount = nodeList.getLength();
        if (nodeCount == 0)
        {
            // No result
        }
        else if (nodeCount == 1)
        {
            Node node = nodeList.item(0);
            // Get the string value
            value = node.getTextContent();
        }
        else
        {
            // Make a collection of the values
            ArrayList<String> stringValues = new ArrayList<String>(5);
            for (int i = 0; i < nodeCount; i++)
            {
                stringValues.add(nodeList.item(i).getTextContent());
            }
            value = stringValues;
        }
        // Done
        return value;
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
                throw new AlfrescoRuntimeException("\n" +
                        "Failed to create XPath expression: \n" +
                        "   Document property: " + documentProperty + "\n" +
                        "   XPath:             " + xpathStr + "\n" +
                        "   Error: " + e.getMessage(),
                        e);
            }
            // Persist it
            xpathExpressionMapping.put(documentProperty, xpathExpression);
            if (logger.isDebugEnabled())
            {
                logger.debug("Added mapping from " + documentProperty + " to " + xpathStr);
            }
        }
        // Done
    }
}
