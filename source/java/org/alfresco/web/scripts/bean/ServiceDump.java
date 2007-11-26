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
package org.alfresco.web.scripts.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.WebScript;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptStatus;
import org.alfresco.web.scripts.WebScriptStorage;
import org.alfresco.web.scripts.WebScriptStore;


/**
 * Dumps everything known about the specified Web Script 
 * 
 * @author davidc
 */
public class ServiceDump extends DeclarativeWebScript
{
    private WebScriptStorage storage;
    
    
    public void setStorage(WebScriptStorage storage)
    {
        this.storage = storage;
    }
    

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status)
    {
        // extract web script id
        String scriptId = req.getExtensionPath();
        if (scriptId == null || scriptId.length() == 0)
        {
            throw new WebScriptException("Web Script Id not provided");
        }
        
        // locate web script
        WebScript script = getWebScriptRegistry().getWebScript(scriptId);
        if (script == null)
        {
            throw new WebScriptException("Web Script Id '" + scriptId + "' not found");
        }

        // construct model
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        Map<String, String> implPaths = new HashMap<String, String>();
        List<Store> modelStores = new ArrayList<Store>();
        model.put("script", script.getDescription());
        model.put("script_class", script.getClass().toString());
        model.put("stores", modelStores);

        // locate web script stores
        Collection<WebScriptStore> stores = storage.getStores();
        for (WebScriptStore store : stores)
        {
            Store modelStore = new Store();
            modelStore.path = store.getBasePath();
            
            // locate script implementation files
            String[] scriptPaths = store.getScriptDocumentPaths(script);
            for (String scriptPath : scriptPaths)
            {
                Implementation impl = new Implementation();
                impl.path = scriptPath;
                impl.overridden = implPaths.containsKey(scriptPath);
                
                // extract implementation content
                InputStream documentIS = null;
                try
                {
                    documentIS = store.getDocument(scriptPath);
                    InputStreamReader isReader = new InputStreamReader(documentIS);
                    StringWriter stringWriter = new StringWriter();
                    char[] buffer = new char[2048];
                    int read = isReader.read(buffer);
                    while (read != -1)
                    {
                        stringWriter.write(buffer, 0, read);
                        read = isReader.read(buffer);
                    }
                    impl.content = stringWriter.toString();
                }
                catch(IOException e)
                {
                    impl.throwable = e;
                }
                finally
                {
                    try
                    {
                        if (documentIS != null) documentIS.close();
                    }
                    catch(IOException e)
                    {
                        // NOTE: ignore close exception
                    }
                }
                
                // record web script implementation file against store
                modelStore.files.add(impl);
            }
            
            // record store in list of stores
            modelStores.add(modelStore);
        }
        
        return model;
    }

    
    public static class Store
    {
        private String path;
        private Collection<Implementation> files = new ArrayList<Implementation>();
        
        public String getPath()
        {
            return path;
        }
        
        public Collection<Implementation> getFiles()
        {
            return files;
        }
     
    }
    
    public static class Implementation
    {
        private String path;
        private boolean overridden;
        private String content;
        private Exception throwable;
        
        public String getPath()
        {
            return path;
        }
        
        public String getContent()
        {
            return content;
        }

        public boolean getOverridden()
        {
            return overridden;
        }
        
        public Throwable getException()
        {
            return throwable;
        }
    }

}
    