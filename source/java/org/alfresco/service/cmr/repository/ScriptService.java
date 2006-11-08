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
