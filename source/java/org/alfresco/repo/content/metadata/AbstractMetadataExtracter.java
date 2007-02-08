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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Jesper Steen Møller
 */
abstract public class AbstractMetadataExtracter implements MetadataExtracter
{
    protected static Log logger = LogFactory.getLog(AbstractMetadataExtracter.class);
    
    private MimetypeService mimetypeService;
    private MetadataExtracterRegistry registry;
    private Set<String> supportedMimetypes;
    private double reliability;
    private long extractionTime;

    protected AbstractMetadataExtracter(String supportedMimetype, double reliability, long extractionTime)
    {
        this.supportedMimetypes = Collections.singleton(supportedMimetype);
        this.reliability = reliability;
        this.extractionTime = extractionTime;
    }

    protected AbstractMetadataExtracter(Set<String> supportedMimetypes, double reliability, long extractionTime)
    {
        this.supportedMimetypes = supportedMimetypes;
        this.reliability = reliability;
        this.extractionTime = extractionTime;
    }

    /**
     * Set the registry to register with
     * 
     * @param registry a metadata extracter registry
     */
    public void setRegistry(MetadataExtracterRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Helper setter of the mimetype service.  This is not always required.
     * 
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @return Returns the mimetype helper
     */
    protected MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }
    
    /**
     * Registers this instance of the extracter with the registry.
     * 
     * @see #setRegistry(MetadataExtracterRegistry)
     */
    public void register()
    {
        if (registry == null)
        {
            logger.warn("Property 'registry' has not been set.  Ignoring auto-registration: \n" +
                    "   extracter: " + this);
            return;
        }
        registry.register(this);
    }
    
    /**
     * Default reliability check that returns the reliability as configured by the contstructor
     * if the mimetype is in the list of supported mimetypes.
     * 
     * @param mimetype the mimetype to check
     */
    public double getReliability(String mimetype)
    {
        if (supportedMimetypes.contains(mimetype))
            return reliability;
        else
            return 0.0;
    }

    public long getExtractionTime()
    {
        return extractionTime;
    }
    
    /**
     * Checks if the mimetype is supported.
     * 
     * @param reader the reader to check
     * @throws AlfrescoRuntimeException if the mimetype is not supported
     */
    protected void checkReliability(ContentReader reader)
    {
        String mimetype = reader.getMimetype();
        if (getReliability(mimetype) <= 0.0)
        {
            throw new AlfrescoRuntimeException(
                    "Metadata extracter does not support mimetype: \n" +
                    "   reader: " + reader + "\n" +
                    "   supported: " + supportedMimetypes + "\n" +
                    "   extracter: " + this);
        }
    }

    public final void extract(ContentReader reader, Map<QName, Serializable> destination) throws ContentIOException
    {
        // check the reliability
        checkReliability(reader);
        
        try
        {
            extractInternal(reader, destination);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Metadata extraction failed: \n" +
                    "   reader: " + reader,
                    e);
        }
        finally
        {
            // check that the reader was closed
            if (!reader.isClosed())
            {
                logger.error("Content reader not closed by metadata extracter: \n" +
                        "   reader: " + reader + "\n" +
                        "   extracter: " + this);
            }
        }
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Completed metadata extraction: \n" +
                    "   reader: " + reader + "\n" +
                    "   extracter: " + this);
        }
    }

    /**
     * Override to provide the necessary extraction logic.  Implementations must ensure that the reader
     * is closed before the method exits.
     * 
     * @param reader the source of the content
     * @param destination the property map to fill
     * @throws Throwable an exception
     */
    protected abstract void extractInternal(ContentReader reader, Map<QName, Serializable> destination) throws Throwable;
    
    /**
     * Examines a value or string for nulls and adds it to the map (if
     * non-empty)
     * 
     * @param prop Alfresco's <code>ContentModel.PROP_</code> to set.
     * @param value Value to set it to
     * @param destination Map into which to set it
     * @return true, if set, false otherwise
     */
    protected boolean trimPut(QName prop, Object value, Map<QName, Serializable> destination)
    {
        if (value == null)
            return false;
        if (value instanceof String)
        {
            String svalue = ((String) value).trim();
            if (svalue.length() > 0)
            {
                destination.put(prop, svalue);
                return true;
            }
            return false;
        }
        else if (value instanceof Serializable)
        {
            destination.put(prop, (Serializable) value);
        }
        else
        {
            destination.put(prop, value.toString());
        }
        return true;
    }
}
