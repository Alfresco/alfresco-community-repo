package org.alfresco.web.api;

import java.util.Collection;

import javax.servlet.ServletContext;

public interface APIRegistry
{

    public ServletContext getContext();

    /**
     * Gets an API Service given an HTTP Method and URI
     * 
     * @param method
     * @param uri
     * @return
     */
    public APIServiceMatch findService(String method, String uri);

    public APIService getService(String id);
    
    public Collection<APIService> getServices();
    
    public FormatRegistry getFormatRegistry();
    
    public APITemplateProcessor getTemplateProcessor();
    
    //public APIScriptProcessor getScriptProcessor();
}
