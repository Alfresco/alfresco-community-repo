/*
 * Copyright (C) 2007 Alfresco, Inc.
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
package org.alfresco.service.cmr.module;

import java.util.List;

import org.alfresco.repo.module.ModuleComponent;

/**
 * A service to control and provide information about the currently-installed modules.
 * 
 * @author Roy Wetherall
 * @author Derek Hulley
 * @since 2.0
 */
public interface ModuleService
{
    /**
     * Gets the module details for a given module id.  If the module does not exist or is not installed
     * then null is returned.
     * 
     * @param moduleId  a module id
     * @return          the module details
     */
    ModuleDetails getModule(String moduleId);
    
    /**
     * Gets a list of all the modules currently installed.
     * 
     * @return  module details of the currently installed modules.
     */
    List<ModuleDetails> getAllModules();
    
    /**
     * Register a component of a module for execution.
     * 
     * @param component the module component.
     */
    void registerComponent(ModuleComponent component);
    
    /**
     * Start all the modules.  For transaction purposes, each module should be
     * regarded as a self-contained unit and started in its own transaction.
     * Where inter-module dependencies exist, these will be pulled into the
     * transaction.
     */
    void startModules();
}
