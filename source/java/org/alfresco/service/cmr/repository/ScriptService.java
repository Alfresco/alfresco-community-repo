/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.repository;

import java.util.Map;

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
@PublicService
public interface ScriptService
{
    /**
     * Process a script against the supplied data model.
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
     * @param scriptRef    Script NodeRef location
     * @param contentProp  QName of the property on the node that contains the content, null can
     *                     be passed to indicate the default property of 'cm:content'
     * @param model        Object model to process script against
     * 
     * @return output of the script (may be null or any valid wrapped JavaScript object)
     * 
     * @throws ScriptException
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"scriptRef", "contentProp", "model"})
    public Object executeScript(NodeRef scriptRef, QName contentProp, Map<String, Object> model)
        throws ScriptException;
    
    /**
     * Process a script against the supplied data model.
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
     * Registers a script implementation with the script service
     * 
     * @param script	the script implementation
     */
    @Auditable(parameters = {"script"})
    public void registerScript(ScriptImplementation script);
}
