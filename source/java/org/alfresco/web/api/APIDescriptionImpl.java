package org.alfresco.web.api;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.web.api.APIRequest.RequiredAuthentication;


public class APIDescriptionImpl implements APIDescription
{
    private String sourceLocation;
    private String id;
    private String shortName;
    private String description;
    private RequiredAuthentication requiredAuthentication;
    private String httpMethod;
    private URI[] uris;
    private String defaultFormat;
    private Map<String, URI> uriByFormat;
    
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getId()
    {
        return this.id;
    }

    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }
    
    public String getShortName()
    {
        return this.shortName;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return this.description;
    }

    public void setRequiredAuthentication(RequiredAuthentication requiredAuthentication)
    {
        this.requiredAuthentication = requiredAuthentication;
    }

    public RequiredAuthentication getRequiredAuthentication()
    {
        return this.requiredAuthentication;
    }

    public void setMethod(String httpMethod)
    {
        this.httpMethod = httpMethod;
    }
    
    public String getMethod()
    {
        return this.httpMethod;
    }

    public void setUris(URI[] uris)
    {
        this.uriByFormat = new HashMap<String, URI>(uris.length);
        for (URI uri : uris)
        {
            this.uriByFormat.put(uri.getFormat(), uri);
        }
        this.uris = uris;
    }

    public URI[] getURIs()
    {
        return this.uris;
    }
    
    public URI getURI(String format)
    {
        return this.uriByFormat.get(format);
    }
    
    public void setDefaultFormat(String defaultFormat)
    {
        this.defaultFormat = defaultFormat;
    }

    public String getDefaultFormat()
    {
        return this.defaultFormat;
    }

    
    public static class URIImpl implements APIDescription.URI
    {
        private String format;
        private String uri;

        public void setFormat(String format)
        {
            this.format = format;
        }
        
        public String getFormat()
        {
            return this.format;
        }

        public void setUri(String uri)
        {
            this.uri = uri;
        }

        public String getURI()
        {
            return this.uri;
        }

        
    }

    
    public void setSourceLocation(String sourceLocation)
    {
        this.sourceLocation = sourceLocation;
    }

    public String getSourceLocation()
    {
        return this.sourceLocation;
    }
    
}
