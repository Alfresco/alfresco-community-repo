/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

import java.util.Map;

import org.alfresco.scripts.ScriptException;
import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.namespace.QName;

/**
 * Script Service.
 * <p>
 * Provides an interface to services for executing a JavaScript engine script file against a
 * Java object based scripting data-model.
 * <p>
 * The script file can either be in the repository (passed as NodeRef string) or on the classpath.
 * Also a script String can be passed directly to the service via the executeScriptString() methods.
 * Java objects are passed into the scripting engine and methods can be accessed directly from the script. 
 * <p>
 * A script is executed within a single transaction, any modifications to nodes or properties that fail
 * and cause a rollback which will rollback all repository modifications made by the script.
 * 
 * @author Kevin Roast
 */
public interface ScriptService
{
    /**
     * Process a script against the supplied data model.  
     * 
     * Uses the most approparite script engine or the default if none found.
     * 
     * @param scriptClasspath   Script location as qualified classpath name
     * @param model             Object model to process script against
     * 
     * @return output of the script (may be null or any valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(parameters = {"scriptClasspath", "model"})
    public Object executeScript(String scriptClasspath, Map<String, Object> model)
        throws ScriptException;
    /**
     * Process a script against the supplied data model.  
     * 
     * Use the 
     * 
     * @param engine            the script engine to use
     * @param scriptClasspath   Script location as qualified classpath name
     * @param model             Object model to process script against
     * 
     * @return output of the script (may be null or any valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(parameters = {"engine", "scriptClasspath", "model"})
    public Object executeScript(String engine, String scriptClasspath, Map<String, Object> model)
        throws ScriptException;
    
    /**
     * Process a script against the supplied data model.
     * 
     * Uses the most approparite script engine or the default if none found.
     * 
     * @param scriptRef    Script NodeRef location
     * @param contentProp  QName of the property on the node that contains the content, null can
     *                     be passed to indicate the default property of 'cm:content'
     * @param model        Object model to process script against
     * 
     * @return output of the script (may be null or any valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(parameters = {"scriptRef", "contentProp", "model"})
    public Object executeScript(NodeRef scriptRef, QName contentProp, Map<String, Object> model)
        throws ScriptException;
    
    /**
     * Process a script against the supplied data model.
     * 
     * @param engine       the script engine to use
     * @param scriptRef    Script NodeRef location
     * @param contentProp  QName of the property on the node that contains the content, null can
     *                     be passed to indicate the default property of 'cm:content'
     * @param model        Object model to process script against
     * 
     * @return output of the script (may be null or any valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(parameters = {"engine", "scriptRef", "contentProp", "model"})
    public Object executeScript(String engine, NodeRef scriptRef, QName contentProp, Map<String, Object> model)
        throws ScriptException;
    
    /**
     * Process a script against the supplied data model
     * 
     * Uses the most approparite script engine or the default if none found.
     * 
     * @param scriptLocation	object representing the script location
     * @param model				Object model to process script against
     * 
     * @return	output of the script (may be null or any other valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(parameters = {"scriptLocation", "model"})
    public Object executeScript(ScriptLocation scriptLocation, Map<String, Object> model)
    	throws ScriptException;
    
    /**
     * Process a script against the supplied data model.
     * 
     * @param engine            the script engine to use
     * @param scriptLocation    object representing the script location
     * @param model             Object model to process script against
     * 
     * @return  output of the script (may be null or any other valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(parameters = {"engine", "scriptLocation", "model"})
    public Object executeScript(String engine, ScriptLocation scriptLocation, Map<String, Object> model)
        throws ScriptException;
    
    /**
     * Process a script against the supplied data model.  Uses the default script engine.
     * 
     * @param script       Script content as a String.
     * @param model        Object model to process script against
     * 
     * @return output of the script (may be null or any valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(parameters = {"script", "model"})
    public Object executeScriptString(String script, Map<String, Object> model)
        throws ScriptException;
    
    /**
     * Process a script against the supplied data model.
     * 
     * @param engine       the script engine to use
     * @param script       Script content as a String.
     * @param model        Object model to process script against
     * 
     * @return output of the script (may be null or any valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(parameters = {"engine", "script", "model"})
    public Object executeScriptString(String engine, String script, Map<String, Object> model)
        throws ScriptException;
    
    /**
     * Registers a script processor with the script service
     * 
     * @param scriptProcessor
     */
    @Auditable(parameters = {"scriptProcessor"})
    public void registerScriptProcessor(ScriptProcessor scriptProcessor);
    
    /**
     * Reset all registered script processors
     */
    @Auditable
    public void resetScriptProcessors();
    
    
    /**
     * Add core data-model to provided Map
     * 
     * @param inputMap initial Map of global scope scriptable Node objects
     * @return A Map of global scope scriptable Node objects
     */
    @Auditable(parameters = {"inputMap"})
    public void buildCoreModel(Map<String, Object> inputMap);
    
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
    @Auditable(parameters = {"person", "companyHome", "userHome", "script", "document", "document"})
    public Map<String, Object> buildDefaultModel(
            NodeRef person, 
            NodeRef companyHome, 
            NodeRef userHome,
            NodeRef script, 
            NodeRef document, 
            NodeRef space);
}
