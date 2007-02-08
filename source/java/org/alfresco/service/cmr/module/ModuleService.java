/*
 * Copyright (C) 2007 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
