/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.jscript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.processor.ProcessorExtension;
import org.alfresco.repo.processor.BaseProcessor;
import org.alfresco.scripts.ScriptException;
import org.alfresco.scripts.ScriptResourceHelper;
import org.alfresco.scripts.ScriptResourceLoader;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptProcessor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;

/**
 * Implementation of the ScriptProcessor using the Rhino JavaScript library.
 * 
 * @author Kevin Roast
 */
public class RhinoScriptProcessor extends BaseProcessor implements ScriptProcessor, ScriptResourceLoader, InitializingBean
{
    private static final Log logger = LogFactory.getLog(RhinoScriptProcessor.class);
    
    private static final String PATH_CLASSPATH = "classpath:";
    
    /** Wrap Factory */
    private static final WrapFactory wrapFactory = new RhinoWrapFactory();
    
    /** Base Value Converter */
    private final ValueConverter valueConverter = new ValueConverter();
    
    /** Store into which to resolve cm:name based script paths */
    private StoreRef storeRef;
    
    /** Store root path to resolve cm:name based scripts path from */
    private String storePath;
    
    /** Pre initialized secure scope object. */
    private Scriptable secureScope;
    
    /** Pre initialized non secure scope object. */
    private Scriptable nonSecureScope;
    
    /** Flag to enable or disable runtime script compliation */
    private boolean compile = true;
    
    /** Flag to enable the sharing of sealed root scopes between scripts executions */
    private boolean shareSealedScopes = true;
    
    /** Cache of runtime compiled script instances */
    private final Map<String, Script> scriptCache = new ConcurrentHashMap<String, Script>(256);
    
    
    /**
     * Set the default store reference
     * 
     * @param   storeRef    The default store reference
     */
    public void setStoreUrl(String storeRef)
    {
        this.storeRef = new StoreRef(storeRef);
    }
    
    /**
     * @param storePath     The store path to set.
     */
    public void setStorePath(String storePath)
    {
        this.storePath = storePath;
    }
    
    /**
     * @param compile   the compile flag to set
     */
    public void setCompile(boolean compile)
    {
        this.compile = compile;
    }
    
    /**
     * @param shareSealedScopes true to allow sharing of sealed scopes between script executions - set to
     * false to disable this feature and ensure that a new scope is created for each executed script.
     */
    public void setShareSealedScopes(boolean shareSealedScopes)
    {
        this.shareSealedScopes = shareSealedScopes;
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#reset()
     */
    public void reset()
    {
        this.scriptCache.clear();
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#execute(org.alfresco.service.cmr.repository.ScriptLocation, java.util.Map)
     */
    public Object execute(ScriptLocation location, Map<String, Object> model)
    {
        try
        {
            // test the cache for a pre-compiled script matching our path
            Script script = null;
            String path = location.getPath();
            if (this.compile && location.isCachable())
            {
                script = this.scriptCache.get(path);
            }
            if (script == null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Resolving and compiling script path: " + path);
                
                // retrieve script content and resolve imports
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                FileCopyUtils.copy(location.getInputStream(), os);  // both streams are closed
                byte[] bytes = os.toByteArray();
                String source = new String(bytes, "UTF-8");
                source = resolveScriptImports(new String(bytes));
                
                // compile the script and cache the result
                Context cx = Context.enter();
                try
                {
                    script = cx.compileString(source, path, 1, null);
                    
                    // We do not worry about more than one user thread compiling the same script.
                    // If more than one request thread compiles the same script and adds it to the
                    // cache that does not matter - the results will be the same. Therefore we
                    // rely on the ConcurrentHashMap impl to deal both with ensuring the safety of the
                    // underlying structure with asynchronous get/put operations and for fast
                    // multi-threaded access to the common cache.
                    if (this.compile && location.isCachable())
                    {
                        this.scriptCache.put(path, script);
                    }
                }
                finally
                {
                    Context.exit();
                }
            }
            
            return executeScriptImpl(script, model, location.isSecure());
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + location.toString() + "': " + err.getMessage(), err);
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#execute(java.lang.String, java.util.Map)
     */
    public Object execute(String location, Map<String, Object> model)
    {        
        return execute(new ClasspathScriptLocation(location), model);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#execute(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public Object execute(NodeRef nodeRef, QName contentProp, Map<String, Object> model)
    {
        try
        {
            if (this.services.getNodeService().exists(nodeRef) == false)
            {
                throw new AlfrescoRuntimeException("Script Node does not exist: " + nodeRef);
            }
            
            if (contentProp == null)
            {
                contentProp = ContentModel.PROP_CONTENT;
            }
            ContentReader cr = this.services.getContentService().getReader(nodeRef, contentProp);
            if (cr == null || cr.exists() == false)
            {
                throw new AlfrescoRuntimeException("Script Node content not found: " + nodeRef);
            }
            
            // compile the script based on the node content
            Script script;
            Context cx = Context.enter();
            try
            {
                script = cx.compileString(resolveScriptImports(cr.getContentString()), nodeRef.toString(), 1, null);
            }
            finally
            {
                Context.exit();
            }
            
            return executeScriptImpl(script, model, false);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + nodeRef.toString() + "': " + err.getMessage(), err);
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#executeString(java.lang.String, java.util.Map)
     */
    public Object executeString(String source, Map<String, Object> model)
    {
        try
        {
            // compile the script based on the node content
            Script script;
            Context cx = Context.enter();
            try
            {
                script = cx.compileString(resolveScriptImports(source), "AlfrescoJS", 1, null);
            }
            finally
            {
                Context.exit();
            }
            return executeScriptImpl(script, model, true);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute supplied script: " + err.getMessage(), err);
        }
    }

    /**
     * Resolve the imports in the specified script. Supported include directives are of the following form:
     * <pre>
     * <import resource="classpath:alfresco/includeme.js">
     * <import resource="workspace://SpacesStore/6f73de1b-d3b4-11db-80cb-112e6c2ea048">
     * <import resource="/Company Home/Data Dictionary/Scripts/includeme.js">
     * </pre>
     * Either a classpath resource, NodeRef or cm:name path based script can be includes. Multiple includes
     * of the same script are dealt with correctly and nested includes of scripts is fully supported.
     * <p>
     * Note that for performance reasons the script import directive syntax and placement in the file
     * is very strict. The import lines <i>must</i> always be first in the file - even before any comments.
     * Immediately that the script service detects a non-import line it will assume the rest of the
     * file is executable script and no longer attempt to search for any further import directives. Therefore
     * all imports should be at the top of the script, one following the other, in the correct syntax and with
     * no comments present - the only separators valid between import directives is white space.
     * 
     * @param script        The script content to resolve imports in
     * 
     * @return a valid script with all nested includes resolved into a single script instance 
     */
    private String resolveScriptImports(String script)
    {
        return ScriptResourceHelper.resolveScriptImports(script, this, logger);
    }
    
    /**
     * Load a script content from the specific resource path.
     *  
     * @param resource      Resources can be of the form:
     * <pre>
     * classpath:alfresco/includeme.js
     * workspace://SpacesStore/6f73de1b-d3b4-11db-80cb-112e6c2ea048
     * /Company Home/Data Dictionary/Scripts/includeme.js
     * </pre>
     * 
     * @return the content from the resource, null if not recognised format
     * 
     * @throws AlfrescoRuntimeException on any IO or ContentIO error
     */
    public String loadScriptResource(String resource)
    {
        String result = null;
        
        if (resource.startsWith(PATH_CLASSPATH))
        {
            try
            {
                // load from classpath
                String scriptClasspath = resource.substring(PATH_CLASSPATH.length());
                URL scriptResource = getClass().getClassLoader().getResource(scriptClasspath);
                if (scriptResource == null)
                {
                    throw new AlfrescoRuntimeException("Unable to locate included script classpath resource: " + resource);
                }
                InputStream stream = scriptResource.openStream();
                if (stream == null)
                {
                    throw new AlfrescoRuntimeException("Unable to load included script classpath resource: " + resource);
                }
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                FileCopyUtils.copy(stream, os);  // both streams are closed
                byte[] bytes = os.toByteArray();
                // create the string from the byte[] using encoding if necessary
                result = new String(bytes, "UTF-8");
            }
            catch (IOException err)
            {
                throw new AlfrescoRuntimeException("Unable to load included script classpath resource: " + resource);
            }
        }
        else
        {
            NodeRef scriptRef;
            if (resource.startsWith("/"))
            {
                // resolve from default SpacesStore as cm:name based path
                // TODO: remove this once FFS correctly allows name path resolving from store root!
                NodeRef rootNodeRef = this.services.getNodeService().getRootNode(this.storeRef);
                List<NodeRef> nodes = this.services.getSearchService().selectNodes(
                        rootNodeRef, this.storePath, null, this.services.getNamespaceService(), false);
                if (nodes.size() == 0)
                {
                    throw new AlfrescoRuntimeException("Unable to find store path: " + this.storePath);
                }
                StringTokenizer tokenizer = new StringTokenizer(resource, "/");
                List<String> elements = new ArrayList<String>(6);
                if (tokenizer.hasMoreTokens())
                {
                    tokenizer.nextToken();
                }
                while (tokenizer.hasMoreTokens())
                {
                    elements.add(tokenizer.nextToken());
                }
                try
                {
                    FileInfo fileInfo = this.services.getFileFolderService().resolveNamePath(nodes.get(0), elements);
                    scriptRef = fileInfo.getNodeRef();
                }
                catch (FileNotFoundException err)
                {
                    throw new AlfrescoRuntimeException("Unable to load included script repository resource: " + resource);
                }
            }
            else
            {
                scriptRef = new NodeRef(resource);
            }
            
            // load from NodeRef default content property
            try
            {
                ContentReader cr = this.services.getContentService().getReader(scriptRef, ContentModel.PROP_CONTENT);
                if (cr == null || cr.exists() == false)
                {
                    throw new AlfrescoRuntimeException("Included Script Node content not found: " + resource);
                }
                result = cr.getContentString();
            }
            catch (ContentIOException err)
            {
                throw new AlfrescoRuntimeException("Unable to load included script repository resource: " + resource);
            }
        }
        
        return result;
    }
    
    /**
     * Execute the supplied script content. Adds the default data model and custom configured root
     * objects into the root scope for access by the script.
     * 
     * @param script        The script to execute.
     * @param model         Data model containing objects to be added to the root scope.
     * @param secure        True if the script is considered secure and may access java.* libs directly
     * 
     * @return result of the script execution, can be null.
     * 
     * @throws AlfrescoRuntimeException
     */
    private Object executeScriptImpl(Script script, Map<String, Object> model, boolean secure)
        throws AlfrescoRuntimeException
    {
        long startTime = 0;
        if (logger.isDebugEnabled())
        {
            startTime = System.nanoTime();
        }
        
        // Convert the model
        model = convertToRhinoModel(model);
        
        Context cx = Context.enter();
        try
        {
            // Create a thread-specific scope from one of the shared scopes.
            // See http://www.mozilla.org/rhino/scopes.html
            cx.setWrapFactory(wrapFactory);
            Scriptable scope;
            if (this.shareSealedScopes)
            {
                Scriptable sharedScope = secure ? this.nonSecureScope : this.secureScope;
                scope = cx.newObject(sharedScope);
                scope.setPrototype(sharedScope);
                scope.setParentScope(null);
            }
            else
            {
                scope = initScope(cx, secure, false);
            }
            
            // there's always a model, if only to hold the util objects
            if (model == null)
            {
                model = new HashMap<String, Object>();
            }
            
            // add the global scripts
            for (ProcessorExtension ex : this.processorExtensions.values()) 
            {
                model.put(ex.getExtensionName(), ex);
            }
            
            // insert supplied object model into root of the default scope
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
            
            // execute the script and return the result
            Object result = script.exec(cx, scope);
            
            // extract java object result if wrapped by Rhino 
            return valueConverter.convertValueForJava(result);
        }
        catch (WrappedException w)
        {
            Throwable err = w.getWrappedException();
            if (err instanceof RuntimeException)
            {
                throw (RuntimeException)err;
            }
            throw new AlfrescoRuntimeException(err.getMessage(), err);
        }
        catch (Throwable err)
        {
            throw new AlfrescoRuntimeException(err.getMessage(), err);
        }
        finally
        {
            Context.exit();
            
            if (logger.isDebugEnabled())
            {
                long endTime = System.nanoTime();
                logger.debug("Time to execute script: " + (endTime - startTime)/1000000f + "ms");
            }
        }
    }
    
    /**
     * Converts the passed model into a Rhino model
     * 
     * @param model     the model
     * 
     * @return Map<String, Object> the converted model
     */
    private Map<String, Object> convertToRhinoModel(Map<String, Object> model)
    {
    	Map<String, Object> newModel = null;
    	if (model != null)
    	{
	        newModel = new HashMap<String, Object>(model.size());
	        for (Map.Entry<String, Object> entry : model.entrySet())
	        {
	            if (entry.getValue() instanceof NodeRef)
	            {
	                newModel.put(entry.getKey(), new ScriptNode((NodeRef)entry.getValue(), this.services));
	            }
	            else
	            {
	                newModel.put(entry.getKey(), entry.getValue());
	            }
	        }
    	}
    	else
    	{
    		newModel = new HashMap<String, Object>(1, 1.0f);
    	}
        return newModel;
    }

    
    /**
     * Rhino script value wraper
     */
    private static class RhinoWrapFactory extends WrapFactory
    {
    	/* (non-Javadoc)
    	 * @see org.mozilla.javascript.WrapFactory#wrapAsJavaObject(org.mozilla.javascript.Context, org.mozilla.javascript.Scriptable, java.lang.Object, java.lang.Class)
    	 */
        public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType)
        {
            if (javaObject instanceof Map && !(javaObject instanceof ScriptableHashMap))
            {
                return new NativeMap(scope, (Map)javaObject);
            }
            return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
        }
    }

    
    /**
     * Pre initializes two scope objects (one secure and one not) with the standard objects preinitialised.
     * This saves on very expensive calls to reinitialize a new scope on every web script execution. See
     * http://www.mozilla.org/rhino/scopes.html
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        // Initialize the secure scope
        Context cx = Context.enter();
        try
        {
            cx.setWrapFactory(wrapFactory);
            this.secureScope = initScope(cx, false, true);
        }
        finally
        {
            Context.exit();
        }
        
        // Initialize the non-secure scope
        cx = Context.enter();
        try
        {
            cx.setWrapFactory(wrapFactory);
            this.nonSecureScope = initScope(cx, true, true);
        }
        finally
        {
            Context.exit();
        }
    }
    
    /**
     * Initializes a scope for script execution. The easiest way to embed Rhino is just to create a new scope this
     * way whenever you need one. However, initStandardObjects() is an expensive method to call and it allocates a
     * fair amount of memory.
     * 
     * @param cx        the thread execution context
     * @param secure    Do we consider the script secure? When <code>false</code> this ensures the script may not
     *                  access insecure java.* libraries or import any other classes for direct access - only the
     *                  configured root host objects will be available to the script writer.
     * @param sealed    Should the scope be sealed, making it immutable? This should be <code>true</code> if a scope
     *                  is to be reused.
     * @return the scope object
     */
    protected Scriptable initScope(Context cx, boolean secure, boolean sealed)
    {
        Scriptable scope;
        if (secure)
        {
            // Initialise the non-secure scope
            // allow access to all libraries and objects, including the importer
            // @see http://www.mozilla.org/rhino/ScriptingJava.html
            scope = new ImporterTopLevel(cx, sealed);
        }
        else
        {
            // Initialise the secure scope
            scope = cx.initStandardObjects(null, sealed);
            // remove security issue related objects - this ensures the script may not access
            // unsecure java.* libraries or import any other classes for direct access - only
            // the configured root host objects will be available to the script writer
            scope.delete("Packages");
            scope.delete("getClass");
            scope.delete("java");
        }
        return scope;
    }
}