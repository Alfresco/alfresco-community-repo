/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import org.alfresco.repo.module.AbstractModuleComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Module patch component base class.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class ModulePatchComponent extends AbstractModuleComponent
{
    /** logger */
    private static Log logger = LogFactory.getLog(ModulePatchComponent.class);
    
    @Override
    protected void executeInternal() throws Throwable
    {
        try
        {
            if (logger.isInfoEnabled() == true)
            {
                logger.info("Module patch component '" + getName() + "' is executing ...");
            }
            
            executePatch();
        }
        catch (Throwable exception)
        {
            // record the exception otherwise it gets swallowed
            exception.printStackTrace();
            throw exception;
        }
    }
    
    protected abstract void executePatch() throws Throwable;
}
