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
package org.alfresco.repo.jscript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.processor.BaseProcessor;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ProcessorExtension;
import org.alfresco.service.cmr.repository.ScriptException;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptProcessor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.util.FileCopyUtils;

/**
 * Implementation of the ScriptEninge using the Rhino JavaScript engine.
 * 
 * @author Kevin Roast
 */
public class RhinoScriptProcessor extends BaseProcessor implements ScriptProcessor
{
    private static final Log    logger = LogFactory.getLog(RhinoScriptProcessor.class);
    
    private static final String IMPORT_PREFIX = "<import";
    private static final String IMPORT_RESOURCE = "resource=\"";
    private static final String PATH_CLASSPATH = "classpath:";
    private static final String SCRIPT_ROOT = "_root";

    /** Base Value Converter */
    private ValueConverter valueConverter = new ValueConverter();
    
    /** Store into which to resolve cm:name based script paths */
    private StoreRef storeRef;
    
    /** Store root path to resolve cm:name based scripts path from */
    private String storePath;
    
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
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#execute(org.alfresco.service.cmr.repository.ScriptLocation, java.util.Map)
     */
    public Object execute(ScriptLocation location, Map<String, Object> model)
    {
        try
        {   
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            FileCopyUtils.copy(location.getInputStream(), os);  // both streams are closed
            byte[] bytes = os.toByteArray();
            // create the script string from the byte[]
            return executeScriptImpl(resolveScriptImports(new String(bytes)), model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + location.toString() + "': " + err.getMessage(), err);
        }
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
            
            return executeScriptImpl(resolveScriptImports(cr.getContentString()), model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + nodeRef.toString() + "': " + err.getMessage(), err);
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#execute(java.lang.String, java.util.Map)
     */
    public Object execute(String location, Map<String, Object> model)
    {        
        try
        {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(location);
            if (stream == null)
            {
                throw new AlfrescoRuntimeException("Unable to load classpath resource: " + location);
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            FileCopyUtils.copy(stream, os);  // both streams are closed
            byte[] bytes = os.toByteArray();
            
            return executeScriptImpl(resolveScriptImports(new String(bytes, "UTF-8")), model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + location + "': " + err.getMessage(), err);
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#executeString(java.lang.String, java.util.Map)
     */
    public Object executeString(String script, Map<String, Object> model)
    {
        try
        {
            return executeScriptImpl(resolveScriptImports(script), model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute supplied script: " + err.getMessage(), err);
        }
    }

    
    /**
     * Resolve the imports in the specified script. Include directives are of the following form:
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
        // use a linked hashmap to preserve order of includes - the key in the collection is used
        // to resolve multiple includes of the same scripts and therefore cyclic includes also
        Map<String, String> scriptlets = new LinkedHashMap<String, String>(8, 1.0f);
        
        // perform a recursive resolve of all script imports
        recurseScriptImports(SCRIPT_ROOT, script, scriptlets);
        
        if (scriptlets.size() == 1)
        {
            // quick exit for single script with no includes
            if (logger.isTraceEnabled())
                logger.trace("Script content resolved to:\r\n" + script);
            
            return script;
        }
        else
        {
            // calculate total size of buffer required for the script and all includes
            int length = 0;
            for (String scriptlet : scriptlets.values())
            {
                length += scriptlet.length();
            }
            // append the scripts together to make a single script
            StringBuilder result = new StringBuilder(length);
            for (String scriptlet : scriptlets.values())
            {
                result.append(scriptlet);
            }
            
            if (logger.isTraceEnabled())
                logger.trace("Script content resolved to:\r\n" + result.toString());
            
            return result.toString();
        }
    }
    
    /**
     * Recursively resolve imports in the specified scripts, adding the imports to the
     * specific list of scriplets to combine later.
     * 
     * @param location      Script location - used to ensure duplicates are not added
     * @param script        The script to recursively resolve imports for
     * @param scripts       The collection of scriplets to execute with imports resolved and removed
     */
    private void recurseScriptImports(String location, String script, Map<String, String> scripts)
    {
        int index = 0;
        // skip any initial whitespace
        for (; index<script.length(); index++)
        {
            if (Character.isWhitespace(script.charAt(index)) == false)
            {
                break;
            }
        }
        // look for the "<import" directive marker
        if (script.startsWith(IMPORT_PREFIX, index))
        {
            // skip whitespace between "<import" and "resource"
            boolean afterWhitespace = false;
            index += IMPORT_PREFIX.length() + 1;
            for (; index<script.length(); index++)
            {
                if (Character.isWhitespace(script.charAt(index)) == false)
                {
                    afterWhitespace = true;
                    break;
                }
            }
            if (afterWhitespace == true && script.startsWith(IMPORT_RESOURCE, index))
            {
                // found an import line!
                index += IMPORT_RESOURCE.length();
                int resourceStart = index;
                for (; index<script.length(); index++)
                {
                    if (script.charAt(index) == '"' && script.charAt(index + 1) == '>')
                    {
                        // found end of import line - so we have a resource path
                        String resource = script.substring(resourceStart, index);
                        
                        if (logger.isDebugEnabled())
                            logger.debug("Found script resource import: " + resource);
                        
                        if (scripts.containsKey(resource) == false)
                        {
                            // load the script resource (and parse any recursive includes...)
                            String includedScript = loadScriptResource(resource);
                            if (includedScript != null)
                            {
                                if (logger.isDebugEnabled())
                                    logger.debug("Succesfully located script '" + resource + "'");
                                recurseScriptImports(resource, includedScript, scripts);
                            }
                        }
                        else
                        {
                            if (logger.isDebugEnabled())
                                logger.debug("Note: already imported resource: " + resource);
                        }
                        
                        // continue scanning this script for additional includes
                        // skip the last two characters of the import directive
                        for (index += 2; index<script.length(); index++)
                        {
                            if (Character.isWhitespace(script.charAt(index)) == false)
                            {
                                break;
                            }
                        }
                        recurseScriptImports(location, script.substring(index), scripts);
                        return;
                    }
                }
                // if we get here, we failed to find the end of an import line
                throw new ScriptException(
                        "Malformed 'import' line - must be first in file, no comments and strictly of the form:" +
                        "\r\n<import resource=\"...\">");
            }
            else
            {
                throw new ScriptException(
                        "Malformed 'import' line - must be first in file, no comments and strictly of the form:" +
                        "\r\n<import resource=\"...\">");
            }
        }
        else
        {
            // no (further) includes found - include the original script content
            if (logger.isDebugEnabled())
                logger.debug("Imports resolved, adding resource '" + location);
            if (logger.isTraceEnabled())
                logger.trace(script);
            scripts.put(location, script);
        }
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
    private String loadScriptResource(String resource)
    {
        String result = null;
        
        if (resource.startsWith(PATH_CLASSPATH))
        {
            try
            {
                // load from classpath
                String scriptClasspath = resource.substring(PATH_CLASSPATH.length());
                InputStream stream = getClass().getClassLoader().getResourceAsStream(scriptClasspath);
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
     * 
     * @return result of the script execution, can be null.
     * 
     * @throws AlfrescoRuntimeException
     */
    private Object executeScriptImpl(String script, Map<String, Object> origModel)
        throws AlfrescoRuntimeException
    {
        long startTime = 0;
        if (logger.isDebugEnabled())
        {
            startTime = System.currentTimeMillis();
        }
        
        // Convert the model
        Map<String, Object> model = convertToRhinoModel(origModel);
        
        // check that rhino script engine is available
        Context cx = Context.enter();
        try
        {
            // The easiest way to embed Rhino is just to create a new scope this way whenever
            // you need one. However, initStandardObjects is an expensive method to call and it
            // allocates a fair amount of memory.
            Scriptable scope = cx.initStandardObjects();

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
            
            // execute the script
            Object result = cx.evaluateString(scope, script, "AlfrescoScript", 1, null);
            
            // extract java object result if wrapped by Rhino 
            result = valueConverter.convertValueForRepo((Serializable)result);
            return result;
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
                long endTime = System.currentTimeMillis();
                logger.debug("Time to execute script: " + (endTime - startTime) + "ms");
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
    		newModel = new HashMap<String, Object>(0);
    	}
        return newModel;
    }
        
}
