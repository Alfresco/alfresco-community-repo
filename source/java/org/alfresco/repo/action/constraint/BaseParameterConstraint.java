/*
 * Copyright (C) 2009-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.action.constraint;

import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Base implementation of a parameter constraint
 * 
 * @author Roy Wetherall
 */
public abstract class BaseParameterConstraint implements ParameterConstraint,
                                                               BeanNameAware
{   
    /** Constraint name */
    protected String name;

    /** Runtime action service */
    protected RuntimeActionService actionService;
    
    /**
     * Init method
     */
    public void init()
    {
        actionService.registerParameterConstraint(this);
    }
    
    /**
     * Set the action service
     * 
     * @param actionService     action service
     */
    public void setActionService(RuntimeActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }    
}
