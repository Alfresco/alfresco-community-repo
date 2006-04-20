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
