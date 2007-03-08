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
package org.alfresco.web.api.framework;

import java.util.HashMap;
import java.util.Map;


/**
 * API Description Implementation
 * 
 * @author davidc
 */
public class APIDescriptionImpl implements APIDescription
{
    private String sourceLocation;
    private String id;
    private String shortName;
    private String description;
    private RequiredAuthentication requiredAuthentication;
    private RequiredTransaction requiredTransaction;
    private String httpMethod;
    private URI[] uris;
    private String defaultFormat;
    private Map<String, URI> uriByFormat;
    

    /**
     * Sets the source location
     * 
     * @param sourceLocation
     */
    public void setSourceLocation(String sourceLocation)
    {
        this.sourceLocation = sourceLocation;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getSourceLocation()
     */
    public String getSourceLocation()
    {
        return this.sourceLocation;
    }

    /**
     * Sets the service id
     * 
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getId()
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Sets the service short name
     * 
     * @param shortName
     */
    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getShortName()
     */
    public String getShortName()
    {
        return this.shortName;
    }

    /**
     * Sets the service description
     * 
     * @param description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getDescription()
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Sets the required level of authentication
     * 
     * @param requiredAuthentication
     */
    public void setRequiredAuthentication(RequiredAuthentication requiredAuthentication)
    {
        this.requiredAuthentication = requiredAuthentication;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getRequiredAuthentication()
     */
    public RequiredAuthentication getRequiredAuthentication()
    {
        return this.requiredAuthentication;
    }

    /**
     * Sets the required level of transaction
     * 
     * @param requiredTransaction
     */
    public void setRequiredTransaction(RequiredTransaction requiredTransaction)
    {
        this.requiredTransaction = requiredTransaction;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getRequiredTransaction()
     */
    public RequiredTransaction getRequiredTransaction()
    {
        return this.requiredTransaction;
    }

    /**
     * Sets the service http method
     * 
     * @param httpMethod
     */
    public void setMethod(String httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getMethod()
     */
    public String getMethod()
    {
        return this.httpMethod;
    }

    /**
     * Sets the service URIs
     * 
     * @param uris
     */
    public void setUris(URI[] uris)
    {
        this.uriByFormat = new HashMap<String, URI>(uris.length);
        for (URI uri : uris)
        {
            this.uriByFormat.put(uri.getFormat(), uri);
        }
        this.uris = uris;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getURIs()
     */
    public URI[] getURIs()
    {
        return this.uris;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getURI(java.lang.String)
     */
    public URI getURI(String format)
    {
        return this.uriByFormat.get(format);
    }

    /**
     * Sets the default response format
     * 
     * @param defaultFormat
     */
    public void setDefaultFormat(String defaultFormat)
    {
        this.defaultFormat = defaultFormat;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIDescription#getDefaultFormat()
     */
    public String getDefaultFormat()
    {
        return this.defaultFormat;
    }

    
    /**
     * API Description URL Implementation
     * 
     * @author davidc
     */
    public static class URIImpl implements APIDescription.URI
    {
        private String format;
        private String uri;

        /**
         * Sets the URI response format
         *  
         * @param format
         */
        public void setFormat(String format)
        {
            this.format = format;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.api.APIDescription.URI#getFormat()
         */
        public String getFormat()
        {
            return this.format;
        }

        /**
         * Sets the URI
         * 
         * @param uri
         */
        public void setUri(String uri)
        {
            this.uri = uri;
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.api.APIDescription.URI#getURI()
         */
        public String getURI()
        {
            return this.uri;
        }
    }

}
