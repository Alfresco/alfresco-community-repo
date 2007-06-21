/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.content.selector;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.ContentWorker;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * A selector that executes a set of XPath statements against the XML document to determine
 * which content worker to provide.  The XPath rules are simple, i.e. if an XML node is
 * found by the XPath statement, then it is considered to be a hit and the corresponding
 * worker is returned.
 * <p>
 * Currently, the only namespaces supported are those contained in the XML documents being
 * tested.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class XPathContentWorkerSelector<W extends ContentWorker> implements ContentWorkerSelector
{
    private static Log logger = LogFactory.getLog(XPathContentWorkerSelector.class);
    
    private DocumentBuilder documentBuilder;
    private XPathFactory xpathFactory;
    private Set<String> supportedMimetypes;
    private Map<String, W> workersByXPath;
    
    public XPathContentWorkerSelector()
    {
        try
        {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xpathFactory = XPathFactory.newInstance();
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to initialize XPathContentWorkerSelector", e);
        }
        supportedMimetypes = new HashSet<String>();
        supportedMimetypes.add(MimetypeMap.MIMETYPE_XML);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("XPathContentWorkerSelector")
          .append("[ workers=").append(workersByXPath)
          .append("]");
        return sb.toString();
    }

    /**
     * Optionally set the mimetypes supported.  They must be XML formats that the chosen
     * parser will be able to handle.
     * 
     * @param supportedMimetypes        the list of mimetypes.  The default is <b>text/xml</b>.
     */
    public void setSupportedMimetypes(Set<String> supportedMimetypes)
    {
        this.supportedMimetypes = supportedMimetypes;
    }

    /**
     * Set the workers to use.  All the XPath statements provided must be compatible with
     * a return value of type {@linkplain XPathConstants#NODE NODE}.
     * 
     * @param workers            a map of {@linkplain ContentWorker} instances
     *                           keyed by XPath statements
     */
    public void setWorkers(Map<String, W> workers)
    {
        this.workersByXPath = workers;
    }

    /**
     * Checks the configuration.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "workers", workersByXPath);
        PropertyCheck.mandatory(this, "supportedMimetypes", supportedMimetypes);
    }
    
    /**
     * Execute the XPath statements, in order, against the document.  Any statements that fail
     * to run will be ignored.
     */
    public W getWorker(ContentReader reader)
    {
        if (!supportedMimetypes.contains(reader.getMimetype()))
        {
            return null;
        }
        W worker = null;
        InputStream is = null;
        String xpath = null;
        try
        {
            is = reader.getContentInputStream();
            Document doc = documentBuilder.parse(is);
            // Execute the statements
            worker = processDocument(doc);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("\n" +
                    "Failed to XPaths against XML document: \n" +
                    "   Reader:   " + reader + "\n" +
                    "   Selector: " + this,
                    e);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("\n" +
                    "Chosen content worker for reader: \n" +
                    "   Reader:       " + reader + "\n" +
                    "   XPath:        " + xpath + "\n" +
                    "   Worker:    " + worker);
        }
        return worker;
    }
    
    /**
     * Check the given document against the list of XPath statements provided.
     * 
     * @param document          the XML document
     * @return                  Returns a content worker that was matched or <tt>null</tt>
     */
    private W processDocument(Document doc)
    {
        for (Map.Entry<String, W> entry : workersByXPath.entrySet())
        {
            try
            {
                String xpath = entry.getKey();
                W worker = entry.getValue();
                // Execute the statement
                Object ret = xpathFactory.newXPath().evaluate(xpath, doc, XPathConstants.NODE);
                if (ret != null)
                {
                    // We found one
                    return worker;
                }
            }
            catch (XPathExpressionException e)
            {
                // We accept this and move on
            }
        }
        // Nothing found
        return null;
    }
}
