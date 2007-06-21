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
package org.alfresco.repo.content.metadata.xml;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.selector.ContentWorkerSelector;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A metadata extractor that selects an appropiate workder for the extraction.
 * <p>
 * The {@linkplain #setSelectors(List) selectors} are used to find an extracter most
 * appropriate of a given XML document.  The chosen extracter is then asked to extract
 * the values, passing through the {@linkplain MetadataExtracter.OverwritePolicy overwrite policy}
 * as {@linkplain #setOverwritePolicy(String)} on this instance.  The overwrite policy of the
 * embedded extracters is not relevant unless they are used separately in another context.
 * 
 * @see ContentWorkerSelector
 * @see MetadataExtracter
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class XmlMetadataExtracter extends AbstractMappingMetadataExtracter
{
    public static String[] SUPPORTED_MIMETYPES = new String[] {MimetypeMap.MIMETYPE_XML};
    
    private static Log logger = LogFactory.getLog(XPathMetadataExtracter.class);
    
    private List<ContentWorkerSelector<MetadataExtracter>> selectors;

    /**
     * Default constructor
     */
    public XmlMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    /**
     * Sets the list of metadata selectors to use to find the extracter to use, given
     * some content.  The evaluations are done in the order that they occur in the
     * list.
     * 
     * @param selectors     A list of selectors
     */
    public void setSelectors(List<ContentWorkerSelector<MetadataExtracter>> selectors)
    {
        this.selectors = selectors;
    }

    @Override
    protected void init()
    {
        PropertyCheck.mandatory(this, "selectors", selectors);
        // Get the base class to set up its mappings
        super.init();
    }

    /**
     * It is not possible to have any default mappings, but something has to be returned.
     * 
     * @return              Returns an empty map
     */
    @Override
    protected Map<String, Set<QName>> getDefaultMapping()
    {
        return Collections.emptyMap();
    }

    /**
     * Selects and extracter to perform the work and redirects to it.
     */
    @Override
    public Map<QName, Serializable> extract(
            ContentReader reader,
            OverwritePolicy overwritePolicy,
            Map<QName, Serializable> destination,
            Map<String, Set<QName>> mapping)
    {
        MetadataExtracter extracter = null;
        // Select a worker
        for (ContentWorkerSelector<MetadataExtracter> selector : selectors)
        {
            ContentReader spawnedReader = reader.getReader();
            try
            {
                extracter = selector.getWorker(spawnedReader);
            }
            finally
            {
                if (reader.isChannelOpen())
                {
                    logger.error("Content reader not closed by MetadataExtractor selector: \n" +
                            "   reader:   " + reader + "\n" +
                            "   selector: " + selector);
                }
            }
            // Just take the first successful one
            if (extracter != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "Found metadata extracter to process XML document: \n" +
                            "   Selector: " + selector + "\n" +
                            "   Document: " + reader);
                }
                break;
            }
        }
        Map<QName, Serializable> modifiedProperties = null;
        // Did we find anything?
        if (extracter == null)
        {
            // There will be no properties extracted
            modifiedProperties = Collections.emptyMap();
        }
        else
        {
            // An extractor was selected
            try
            {
                modifiedProperties = extracter.extract(reader, overwritePolicy, destination, mapping);
            }
            finally
            {
                if (reader.isChannelOpen())
                {
                    logger.error("Content reader not closed by MetadataExtractor: \n" +
                            "   Reader:   " + reader + "\n" +
                            "   extracter: " + extracter);
                }
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("\n" +
                    "XML metadata extractor redirected: \n" +
                    "   Reader:    " + reader + "\n" +
                    "   Extracter: " + extracter + "\n" +
                    "   Extracted: " + modifiedProperties);
        }
        return modifiedProperties;
    }

    /**
     * This is not required as the 
     */
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        throw new UnsupportedOperationException();
    }
}
