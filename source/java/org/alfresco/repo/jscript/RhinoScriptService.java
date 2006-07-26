/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.jscript;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptException;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Implementation of the ScriptService using the Rhino JavaScript engine.
 * 
 * @author Kevin Roast
 */
public class RhinoScriptService implements ScriptService
{
    private static final Logger logger = Logger.getLogger(RhinoScriptService.class);
    
    /** The permission-safe node service */
    private NodeService nodeService;
    
    /** The Content Service to use */
    private ContentService contentService;
    
    /**
     * Set the node service
     * 
     * @param nodeService       The permission-safe node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    The ContentService to use
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(java.lang.String, java.util.Map)
     */
    public Object executeScript(String scriptClasspath, Map<String, Object> model)
        throws ScriptException
    {
        if (scriptClasspath == null)
        {
            throw new IllegalArgumentException("Script ClassPath is mandatory.");
        }
        
        Reader reader = null;
        try
        {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(scriptClasspath);
            if (stream == null)
            {
                throw new AlfrescoRuntimeException("Unable to load classpath resource: " + scriptClasspath);
            }
            reader = new InputStreamReader(stream);
            
            return executeScriptImpl(reader, model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + scriptClasspath + "': " + err.getMessage(), err);
        }
        finally
        {
            if (reader != null)
            {
                try {reader.close();} catch (IOException ioErr) {}
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public Object executeScript(NodeRef scriptRef, QName contentProp, Map<String, Object> model)
        throws ScriptException
    {
        if (scriptRef == null)
        {
            throw new IllegalArgumentException("Script NodeRef is mandatory.");
        }
        
        Reader reader = null;
        try
        {
            if (this.nodeService.exists(scriptRef) == false)
            {
                throw new AlfrescoRuntimeException("Script Node does not exist: " + scriptRef);
            }
            
            if (contentProp == null)
            {
                contentProp = ContentModel.PROP_CONTENT;
            }
            ContentReader cr = this.contentService.getReader(scriptRef, contentProp);
            if (cr == null || cr.exists() == false)
            {
                throw new AlfrescoRuntimeException("Script Node content not found: " + scriptRef);
            }
            reader = new InputStreamReader(cr.getContentInputStream());
            
            return executeScriptImpl(reader, model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + scriptRef.toString() + "': " + err.getMessage(), err);
        }
        finally
        {
            if (reader != null)
            {
                try {reader.close();} catch (IOException ioErr) {}
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScriptString(java.lang.String, java.util.Map)
     */
    public Object executeScriptString(String script, Map<String, Object> model)
        throws ScriptException
    {
        if (script == null || script.length() == 0)
        {
            throw new IllegalArgumentException("Script argument is mandatory.");
        }
        
        Reader reader = null;
        try
        {
            reader = new StringReader(script);
            
            return executeScriptImpl(reader, model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute supplied script: " + err.getMessage(), err);
        }
    }
    
    /**
     * Execute the script content from the supplied Reader. Adds the data model into the default
     * root scope for access by the script.
     * 
     * @param reader        Reader referencing the script to execute.
     * @param model         Data model containing objects to be added to the root scope.
     * 
     * @return result of the script execution, can be null.
     * 
     * @throws AlfrescoRuntimeException
     */
    private Object executeScriptImpl(Reader reader, Map<String, Object> model)
        throws AlfrescoRuntimeException
    {
        long startTime = 0;
        if (logger.isDebugEnabled())
        {
            startTime = System.currentTimeMillis();
        }
        
        // check that rhino script engine is available
        Context cx = Context.enter();
        try
        {
            // The easiest way to embed Rhino is just to create a new scope this way whenever
            // you need one. However, initStandardObjects is an expensive method to call and it
            // allocates a fair amount of memory.  ImporterTopLevel provides a scope allowing 
            // the import of java classes and packages.
            Scriptable topLevelScope = new ImporterTopLevel(cx);
            Scriptable scope = cx.initStandardObjects();
            scope.setParentScope(topLevelScope);
            
            // insert supplied object model into root of the default scope
            if (model != null)
            {
                for (String key : model.keySet())
                {
                    // set the root scope on appropriate objects
                    // this is used to allow native JS object creation etc.
                    Object obj = model.get(key);
                    if (obj instanceof Scopeable)
                    {
                        ((Scopeable)obj).setScope(scope);
                    }
                    
                    // convert/wrap each object to JavaScript compatible
                    Object jsObject = Context.javaToJS(obj, scope);
                    
                    // insert into the root scope ready for access by the script
                    ScriptableObject.putProperty(scope, key, jsObject);
                }
            }
            
            // execute the script
            Object result = cx.evaluateReader(scope, reader, "AlfrescoScript", 1, null);
            
            return result;
        }
        catch (Throwable err)
        {
            throw new AlfrescoRuntimeException(err.getMessage(), err);
        }
        finally
        {
            cx.exit();
            
            if (logger.isDebugEnabled())
            {
                long endTime = System.currentTimeMillis();
                logger.debug("Time to execute script: " + (endTime - startTime) + "ms");
            }
        }
    }
    
    /**
     * Create the default data-model available to scripts as global scope level objects:
     * <p>
     * 'companyhome' - the Company Home node<br>
     * 'userhome' - the current user home space node<br>
     * 'person' - the node representing the current user Person<br>
     * 'document' - document context node (may not be available)<br>
     * 'space' - space context node (may not be available)
     * 
     * @param services      ServiceRegistry
     * @param person        The current user Person Node
     * @param companyHome   The CompanyHome ref
     * @param userHome      The User home space ref
     * @param script        Optional ref to the script itself
     * @param document      Optional ref to a document Node
     * @param space         Optional ref to a space Node
     * 
     * @return A Map of global scope scriptable Node objects
     */
    public static Map<String, Object> buildDefaultModel(
            ServiceRegistry services,
            NodeRef person, NodeRef companyHome, NodeRef userHome,
            NodeRef script, NodeRef document, NodeRef space)
    {
        return buildDefaultModel(services, person, companyHome, userHome, script, document, space, null);
    }
    
    /**
     * Create the default data-model available to scripts as global scope level objects:
     * <p>
     * 'companyhome' - the Company Home node<br>
     * 'userhome' - the current user home space node<br>
     * 'person' - the node representing the current user Person<br>
     * 'script' - the node representing the script itself (may not be available)<br>
     * 'document' - document context node (may not be available)<br>
     * 'space' - space context node (may not be available)
     * 
     * @param services      ServiceRegistry
     * @param person        The current user Person Node
     * @param companyHome   The CompanyHome ref
     * @param userHome      The User home space ref
     * @param script        Optional ref to the script itself
     * @param document      Optional ref to a document Node
     * @param space         Optional ref to a space Node
     * @param resolver      Image resolver to resolve icon images etc.
     * 
     * @return A Map of global scope scriptable Node objects
     */
    public static Map<String, Object> buildDefaultModel(
            ServiceRegistry services,
            NodeRef person, NodeRef companyHome, NodeRef userHome,
            NodeRef script, NodeRef document, NodeRef space,
            TemplateImageResolver resolver)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        // add the well known node wrapper objects
        model.put("companyhome", new Node(companyHome, services, resolver));
        model.put("userhome", new Node(userHome, services, resolver));
        model.put("person", new Node(person, services, resolver));
        if (script != null)
        {
            model.put("script", new Node(script, services, resolver));
        }
        if (document != null)
        {
            model.put("document", new Node(document, services, resolver));
        }
        if (space != null)
        {
            model.put("space", new Node(space, services, resolver));
        }
        
        // add other useful util objects
        model.put("search", new Search(services, companyHome.getStoreRef(), resolver));
        
        return model;
    }
}
