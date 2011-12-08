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
package org.alfresco.repo.processor;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.jscript.ScriptUrls;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptProcessor;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Script service implementation
 * 
 * @author Kevin Roast
 * @author Roy Wetherall
 */
public class ScriptServiceImpl implements ScriptService
{
    /** Logger */
    private static final Log    logger = LogFactory.getLog(ScriptServiceImpl.class);
    
    /** The name of the default script processor */
    private String defaultScriptProcessor;    
    
    /** Maps containing the script processors */
    private Map<String, ScriptProcessor> scriptProcessors = new HashMap<String, ScriptProcessor>(8);
    private Map<String, String> scriptProcessorNamesByExtension = new HashMap<String, String>(8);
    
    /** The node service */
    private NodeService nodeService;

    private SysAdminParams sysAdminParams;

    /**
     * Sets the name of the default script processor
     * 
     * @param defaultScriptProcessor    the name of the default script processor
     */
    public void setDefaultScriptProcessor(String defaultScriptProcessor)
    {
        this.defaultScriptProcessor = defaultScriptProcessor;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the sysAdminParams
     * 
     * @param sysAdminParams the sysAdminParams 
     */
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    /**
     * Register a script processor
     * 
     * @param   scriptProcessor     the script processor to register with the script service
     */
    public void registerScriptProcessor(ScriptProcessor scriptProcessor)
    {
        this.scriptProcessors.put(scriptProcessor.getName(), scriptProcessor);
        this.scriptProcessorNamesByExtension.put(scriptProcessor.getExtension(), scriptProcessor.getName());
    }
    
    /**
     * Reset all registered script processors
     */
    public void resetScriptProcessors()
    {
        for (ScriptProcessor p : this.scriptProcessors.values())
        {
            p.reset();
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(java.lang.String, java.util.Map)
     */
    public Object executeScript(String scriptClasspath, Map<String, Object> model)
        throws ScriptException
    {
        ParameterCheck.mandatory("scriptClasspath", scriptClasspath);
        ScriptProcessor scriptProcessor = getScriptProcessor(scriptClasspath);
        return execute(scriptProcessor, scriptClasspath, model);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(java.lang.String, java.lang.String, java.util.Map)
     */
    public Object executeScript(String engine, String scriptClasspath, Map<String, Object> model)
        throws ScriptException
    {
        ScriptProcessor scriptProcessor = lookupScriptProcessor(engine);
        return execute(scriptProcessor, scriptClasspath, model);
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public Object executeScript(NodeRef scriptRef, QName contentProp, Map<String, Object> model)
        throws ScriptException
    {
        ParameterCheck.mandatory("scriptRef", scriptRef);
        ScriptProcessor scriptProcessor = getScriptProcessor(scriptRef);
        return execute(scriptProcessor, scriptRef, contentProp, model);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public Object executeScript(String engine, NodeRef scriptRef, QName contentProp, Map<String, Object> model)
        throws ScriptException
    {
        ScriptProcessor scriptProcessor = lookupScriptProcessor(engine);
        return execute(scriptProcessor, scriptRef, contentProp, model);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(org.alfresco.service.cmr.repository.ScriptLocation, java.util.Map)
     */
    public Object executeScript(ScriptLocation location, Map<String, Object> model)
    	throws ScriptException
    {
    	ParameterCheck.mandatory("location", location);
        ScriptProcessor scriptProcessor = getScriptProcessor(location.toString());
        return execute(scriptProcessor, location, model);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(java.lang.String, org.alfresco.service.cmr.repository.ScriptLocation, java.util.Map)
     */
    public Object executeScript(String engine, ScriptLocation location, Map<String, Object> model)
        throws ScriptException
    {
        ScriptProcessor scriptProcessor = lookupScriptProcessor(engine);
        return execute(scriptProcessor, location, model);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScriptString(java.lang.String, java.util.Map)
     */
    public Object executeScriptString(String script, Map<String, Object> model)
        throws ScriptException
    {
        return executeScriptString(this.defaultScriptProcessor, script, model);
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScriptString(java.lang.String, java.util.Map)
     */
    public Object executeScriptString(String engine, String script, Map<String, Object> model)
        throws ScriptException
    {
        ScriptProcessor scriptProcessor = lookupScriptProcessor(engine);
        return executeString(scriptProcessor, script, model);
    }
    
    /**
     * Execute script
     * 
     * @param location  the location of the script 
     * @param model     context model
     * @return Object   the result of the script
     */
    protected Object execute(ScriptProcessor processor, ScriptLocation location, Map<String, Object> model)
    {
        ParameterCheck.mandatory("location", location);
        if (logger.isDebugEnabled())
        {
            logger.debug("Executing script:\n" + location);
        }
        try
        {
            return processor.execute(location, model);
        }
        catch (Throwable err)
        {
            throw translateProcessingException(location.toString(), err);
        }
    }
    
    /**
     * Execute script
     * 
     * @param scriptRef       the script node reference
     * @param contentProp   the content property of the script
     * @param model         the context model
     * @return Object       the result of the script
     */
    protected Object execute(ScriptProcessor processor, NodeRef scriptRef, QName contentProp, Map<String, Object> model)
    {
        ParameterCheck.mandatory("scriptRef", scriptRef);
        if (logger.isDebugEnabled())
        {
            logger.debug("Executing script:\n" + scriptRef);
        }
        try
        {
            return processor.execute(scriptRef, contentProp, model);
        }
        catch (Throwable err)
        {
            throw translateProcessingException(scriptRef.toString(), err);
        }
    }
    
    /** 
     * Execute script
     * 
     * @param location  the classpath string locating the script
     * @param model     the context model
     * @return Object   the result of the script
     */
    protected Object execute(ScriptProcessor processor, String location, Map<String, Object> model)
    {
        ParameterCheck.mandatoryString("location", location);
        if (logger.isDebugEnabled())
        {
            logger.debug("Executing script:\n" + location);
        }
        try
        {
            return processor.execute(location, model);
        }
        catch (Throwable err)
        {
            throw translateProcessingException(location, err);
        }
    }
    
    /**
     * Execute script string
     * 
     * @param script    the script string
     * @param model     the context model
     * @return Object   the result of the script 
     */
    protected Object executeString(ScriptProcessor processor, String script, Map<String, Object> model)
    {
        ParameterCheck.mandatoryString("script", script);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Executing script:\n" + script);
        }
        try
        {
            return processor.executeString(script, model);
        }
        catch (Throwable err)
        {
            throw translateProcessingException("provided by caller", err);
        }
    }

    protected ScriptException translateProcessingException(String scriptInfo, Throwable err)
    {
        ScriptException result = null;
        String msg = "Failed to execute script " + (scriptInfo == null ? "" : scriptInfo);
        if (logger.isWarnEnabled())
        {
            logger.warn(msg, err);
        }
        if (ScriptException.class.isAssignableFrom(err.getClass()))
        {
            result = (ScriptException)err;
        }
        else
        {
            result = new ScriptException(msg, err);
        }
        return result;
    }

    /**
     * Helper method to lookup the script processor based on a name
     * 
     * @param   name  the name of the script processor
     * @return  ScriptProcessor the script processor, default processor if no match found
     */
    protected ScriptProcessor lookupScriptProcessor(String name)
    {
        ScriptProcessor scriptProcessor = (name == null ? null : this.scriptProcessors.get(name));
        if (scriptProcessor == null)
        {
            scriptProcessor = this.scriptProcessors.get(this.defaultScriptProcessor);
        }
        return scriptProcessor;
    }
    
    /**
     * Gets a scipt processor based on the node reference of a script
     * 
     * @param   scriptNode          the node reference of the script
     * @return  ScriptProcessor     the script processor
     */
    protected ScriptProcessor getScriptProcessor(NodeRef scriptNode)
    {
        String scriptName = (String)this.nodeService.getProperty(scriptNode, ContentModel.PROP_NAME);
        return getScriptProcessorImpl(scriptName);
    }
    
    /**
     * Gets a script processor based on the script location string
     * 
     * @param   scriptLocation      the script location
     * @return  ScriptProcessor     the script processor
     */
    protected ScriptProcessor getScriptProcessor(String scriptLocation)
    {
        if (scriptLocation.indexOf(StoreRef.URI_FILLER) != -1)
        {
            // Try and create the nodeRef
            NodeRef nodeRef = new NodeRef(scriptLocation);
            scriptLocation = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);   
        }
        
        return getScriptProcessorImpl(scriptLocation);
    }
    
    /** 
     * Gets a script processor based on the scripts file name
     * 
     * @param   scriptFileName      the scripts file name
     * @return  ScriptProcessor     the matching script processor
     */
    protected ScriptProcessor getScriptProcessorImpl(String scriptFileName)
    {
        String engine = null;
        
        if (scriptFileName != null)
        {
            String extension = getFileExtension(scriptFileName);
            if (extension != null)
            {
                engine = this.scriptProcessorNamesByExtension.get(extension);
            }
        }
        
        return lookupScriptProcessor(engine);
    }
    
    /**
     * Gets the file extension of a file
     * 
     * @param fileName  the file name
     * @return  the file extension
     */
    private String getFileExtension(String fileName)
    {
        String extension = null;
        int index = fileName.lastIndexOf('.');
        if (index > -1 && (index < fileName.length() - 1))
        {
            extension = fileName.substring(index + 1);
        }
        return extension;
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#buildCoreModel(java.util.Map)
     */
    public void buildCoreModel(Map<String, Object> inputMap)
    {
        ParameterCheck.mandatory("InputMap", inputMap);
        inputMap.put("urls", new ScriptUrls(sysAdminParams));
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#buildDefaultModel(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<String, Object> buildDefaultModel(
            NodeRef person, 
            NodeRef companyHome, 
            NodeRef userHome,
            NodeRef script, 
            NodeRef document, 
            NodeRef space)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        buildCoreModel(model);
        
        // add the well known node wrapper objects
        model.put("companyhome", companyHome);
        if (userHome!= null)
        {
            model.put("userhome", userHome);
        }
        if (person != null)
        {
            model.put("person", person);
        }
        if (script != null)
        {
            model.put("script", script);
        }
        if (document != null)
        {
            model.put("document", document);
        }
        if (space != null)
        {
            model.put("space", space);
        }
        
        return model;
    }
}
