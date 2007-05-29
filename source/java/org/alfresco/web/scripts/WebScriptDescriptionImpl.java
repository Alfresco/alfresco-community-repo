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
package org.alfresco.web.scripts;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of a Web Script Description
 * 
 * @author davidc
 */
public class WebScriptDescriptionImpl implements WebScriptDescription
{
    private WebScriptStore store;
    private String scriptPath;
    private String descPath;
    private String id;
    private String shortName;
    private String description;
    private RequiredAuthentication requiredAuthentication;
    private RequiredTransaction requiredTransaction;
    private FormatStyle formatStyle;
    private String httpMethod;
    private URI[] uris;
    private String defaultFormat;
    private Map<String, URI> uriByFormat;

    
    /**
     * Sets the web description store
     * 
     * @param store  store
     */
    public void setStore(WebScriptStore store)
    {
        this.store = store;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription#getStorePath()
     */
    public String getStorePath()
    {
        return store.getBasePath();
    }

    /**
     * Sets the script path
     * 
     * @param scriptPath
     */
    public void setScriptPath(String scriptPath)
    {
        this.scriptPath = scriptPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription#getScriptPath()
     */
    public String getScriptPath()
    {
        return scriptPath;
    }

    /**
     * Sets the desc path
     * 
     * @param descPath
     */
    public void setDescPath(String descPath)
    {
        this.descPath = descPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription#getDescPath()
     */
    public String getDescPath()
    {
        return descPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription#getDescDocument()
     */
    public InputStream getDescDocument()
        throws IOException
    {
        return store.getDescriptionDocument(descPath);
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
     * @see org.alfresco.web.scripts.WebScriptDescription#getId()
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
     * @see org.alfresco.web.scripts.WebScriptDescription#getShortName()
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
     * @see org.alfresco.web.scripts.WebScriptDescription#getDescription()
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
     * @see org.alfresco.web.scripts.WebScriptDescription#getRequiredAuthentication()
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
     * @see org.alfresco.web.scripts.WebScriptDescription#getRequiredTransaction()
     */
    public RequiredTransaction getRequiredTransaction()
    {
        return this.requiredTransaction;
    }

    /**
     * Sets the format style
     * 
     * @param formatStyle
     */
    public void setFormatStyle(FormatStyle formatStyle)
    {
        this.formatStyle = formatStyle;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription#getFormatStyle()
     */
    public FormatStyle getFormatStyle()
    {
        return this.formatStyle;
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
     * @see org.alfresco.web.scripts.WebScriptDescription#getMethod()
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
     * @see org.alfresco.web.scripts.WebScriptDescription#getURIs()
     */
    public URI[] getURIs()
    {
        return this.uris;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptDescription#getURI(java.lang.String)
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
     * @see org.alfresco.web.scripts.WebScriptDescription#getDefaultFormat()
     */
    public String getDefaultFormat()
    {
        return this.defaultFormat;
    }

    
    /**
     * Web Script URL Implementation
     * 
     * @author davidc
     */
    public static class URIImpl implements WebScriptDescription.URI
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
         * @see org.alfresco.web.scripts.WebScriptDescription.URI#getFormat()
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
         * @see org.alfresco.web.scripts.WebScriptDescription.URI#getURI()
         */
        public String getURI()
        {
            return this.uri;
        }
    }

}
