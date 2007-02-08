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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action.scheduled;

import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.TemplateService;

/**
 * Common attributes for template action definitions.
 * 
 * @author Andy Hind
 */
public abstract class AbstractTemplateActionDefinition implements TemplateActionDefinition
{
    /*
     * The action service
     */
    public ActionService actionService;

    /*
     * The template service
     */
    public TemplateService templateService;

    /*
     * The compensating action
     */
    protected TemplateActionDefinition compensatingTemplateActionDefinition;

    /**
     * Simple construction
     */
    public AbstractTemplateActionDefinition()
    {
        super();
    }

    /**
     * Get the action service.
     * 
     * @return
     */
    public ActionService getActionService()
    {
        return actionService;
    }

    /**
     * Set the action service - IOC.
     * 
     * @param actionService
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Get the template service.
     * 
     * @return
     */
    public TemplateService getTemplateService()
    {
        return templateService;
    }

    /**
     * Set the template service - IOC.
     * 
     * @param templateService
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    /**
     * Set the template to define the compensating action.
     * 
     * @param compensatingTemplateActionDefinition
     */
    public void setCompensatingTemplateCompositeActionDefinition(
            TemplateActionDefinition compensatingTemplateActionDefinition)
    {
        this.compensatingTemplateActionDefinition = compensatingTemplateActionDefinition;
    }

    /**
     * Get the template that defines the conpensating action.
     * 
     * @return
     */
    public TemplateActionDefinition getCompensatingTemplateCompositeActionDefinition()
    {
        return compensatingTemplateActionDefinition;
    }

}
