/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
    /**
     * The action service
     */
    public ActionService actionService;

    /**
     * The template service
     */
    public TemplateService templateService;

    /**
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
     * @return - the action service.
     */
    public ActionService getActionService()
    {
        return actionService;
    }

    /**
     * Set the action service - IOC.
     * 
     * @param actionService ActionService
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Get the template service.
     * 
     * @return - the template service
     */
    public TemplateService getTemplateService()
    {
        return templateService;
    }

    /**
     * Set the template service - IOC.
     *
     * @param templateService TemplateService
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    /**
     * Set the template to define the compensating action.
     * 
     * @param compensatingTemplateActionDefinition TemplateActionDefinition
     */
    public void setCompensatingTemplateCompositeActionDefinition(
            TemplateActionDefinition compensatingTemplateActionDefinition)
    {
        this.compensatingTemplateActionDefinition = compensatingTemplateActionDefinition;
    }

    /**
     * Get the template that defines the conpensating action.
     * 
     * @return - the template action definition.
     */
    public TemplateActionDefinition getCompensatingTemplateCompositeActionDefinition()
    {
        return compensatingTemplateActionDefinition;
    }

}
