package org.alfresco.web.api;

import java.io.IOException;
import java.io.InputStream;

import freemarker.cache.TemplateLoader;


public interface APIStore
{
    public String getBasePath();
    
    public String[] getDescriptionDocumentPaths();

    public InputStream getDescriptionDocument(String documentPath)
        throws IOException;

    public TemplateLoader getTemplateLoader();
    
    public ScriptLoader getScriptLoader();
}

