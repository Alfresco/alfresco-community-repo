/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.patch;

/**
 * Module Patch Interface
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public interface ModulePatch extends Comparable<ModulePatch>
{
    /**
     * @return  module patch id
     */
    String getId();
    
    /**
     * @return  module patch description
     */
    String getDescription();
    
    /**
     * @return  module id this patch applies to
     */
    String getModuleId();
    
    /**
     * @return smallest module schema number that this patch may be applied to
     */
    int getFixesFromSchema();

    /**
     * @return largest module schema number that this patch may be applied to
     */
    int getFixesToSchema();
    
    /**
     * @return module schema number that this patch attempts to bring the repo up to
     */
    int getTargetSchema();
    
    /**
     * Apply the module patch
     */
    void apply();

}
