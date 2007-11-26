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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.WebScript;
import org.alfresco.web.scripts.WebScriptDescription;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRegistry;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptServletRequest;
import org.alfresco.web.scripts.WebScriptStatus;
import org.alfresco.web.scripts.WebScriptStorage;
import org.alfresco.web.scripts.WebScriptStore;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;


/**
 * Install a Web Script 
 * 
 * @author davidc
 */
public class ServiceInstall extends DeclarativeWebScript
{
    private WebScriptStorage storage;
    
    
    public void setStorage(WebScriptStorage storage)
    {
        this.storage = storage;
    }
    

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status)
    {
        if (!(req instanceof WebScriptServletRequest))
        {
            throw new WebScriptException("Web Script install only supported via HTTP Servlet");
        }
        HttpServletRequest servletReq = ((WebScriptServletRequest)req).getHttpServletRequest();
        
        // construct model
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        List<InstalledFile> installedFiles = new ArrayList<InstalledFile>();
        model.put("installedFiles", installedFiles);

        try
        {
            // extract uploaded file
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            boolean isMultipart = upload.isMultipartContent(servletReq);
            if (!isMultipart)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Web Script install request is not multi-part");
            }
            FileItem part = null;
            List<FileItem> files = upload.parseRequest(servletReq);
            for (FileItem file : files)
            {
                if (!file.isFormField())
                {
                    if (part != null)
                    {
                        throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Web Script install request expects only one file upload");
                    }
                    part = file;
                }
            }
            
            // find web script definition
            Document document = null;
            InputStream fileIS = part.getInputStream();
            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileIS));
                SAXReader reader = new SAXReader();
                document = reader.read(bufferedReader);
            }
            finally
            {
                fileIS.close();
            }
            Element rootElement = document.getRootElement();
            XPath xpath = rootElement.createXPath("//ws:webscript");
            Map<String,String> uris = new HashMap<String,String>();
            uris.put("ws", "http://www.alfresco.org/webscript/1.0");
            xpath.setNamespaceURIs(uris);
            List nodes = xpath.selectNodes(rootElement);
            if (nodes.size() == 0)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Cannot locate Web Script in uploaded file");
            }
            
            // extract web script definition
            Element webscriptElem = (Element)nodes.get(0);
            String scriptId = webscriptElem.attributeValue("scriptid");
            if (scriptId == null || scriptId.length() == 0)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Expected scriptid value on webscript element");
            }
            Iterator iter = webscriptElem.elementIterator();
            while (iter.hasNext())
            {
                Element fileElem = (Element)iter.next();
                String webscriptStore = fileElem.attributeValue("store");
                if (webscriptStore == null || webscriptStore.length() == 0)
                {
                    throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Expected store value on webscript element");
                }
                String webscriptPath = fileElem.attributeValue("path");
                if (webscriptPath == null || webscriptPath.length() == 0)
                {
                    throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Expected file value on webscript element");
                }
                String webscriptContent = fileElem.getText();
                
                // install web script implementation file
                installFile(webscriptStore, webscriptPath, webscriptContent);
                InstalledFile installedFile = new InstalledFile();
                installedFile.store = webscriptStore;
                installedFile.path = webscriptPath;
                installedFiles.add(installedFile);
            }
            
            // reset the web script registry
            WebScriptRegistry registry = getWebScriptRegistry();
            registry.reset();
            
            // locate installed web script
            WebScript webscript = registry.getWebScript(scriptId);
            if (webscript == null)
            {
                throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to install Web Script " + scriptId);
            }
            model.put("installedScript", webscript.getDescription());
        }
        catch(FileUploadException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        catch(DocumentException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        catch(IOException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return model;
    }

    
    private void installFile(String storePath, String file, String content)
    {
        // retrieve appropriate web script store
        WebScriptStore store = storage.getStore(storePath);
        if (store == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Store path " + storePath + " refers to a store that does not exist");
        }
        
        // determine if file already exists in store
        if (store.hasDocument(file))
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Web Script file " + file + " already exists in store " + storePath);
        }
        
        // create the web script file
        try
        {
            store.createDocument(file, content);
        }
        catch(IOException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to install Web Script file " + file + " into store" + storePath);
        }
    }
    
    
    public static class InstalledFile
    {
        private WebScriptDescription script;
        private String store;
        private String path;
        
        public String getStore()
        {
            return store;
        }
        
        public String getPath()
        {
            return path;
        }
    }

}
    