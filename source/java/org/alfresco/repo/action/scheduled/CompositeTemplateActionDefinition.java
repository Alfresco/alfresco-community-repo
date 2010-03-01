/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.action.scheduled;

import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The template to define a composite action.
 * 
 * @author Andy Hind
 */
public class CompositeTemplateActionDefinition extends AbstractTemplateActionDefinition
{

    /*
     * The list of action templates that define this composite
     */
    private List<TemplateActionDefinition> templateActionDefinitions;
    
    /**
     * Default constructor.
     *
     */
    public CompositeTemplateActionDefinition()
    {
        super();
    }

    /**
     * Set the action templates - IOC.
     * 
     * @param templateActionDefinitions
     */
    public void setTemplateActionDefinitions(List<TemplateActionDefinition> templateActionDefinitions)
    {
        this.templateActionDefinitions = templateActionDefinitions;

    }

    /**
     * Get the list of template actions.
     * 
     * @return - a list of templates for action definitions.
     */
    public List<TemplateActionDefinition> templateActionDefinitions()
    {
        return templateActionDefinitions;
    }

    /**
     * Build the composite action in the context of the given node.
     * @param nodeRef 
     * @return - the contextualised action.
     * 
     */
    public Action getAction(NodeRef nodeRef)
    {
        CompositeAction compositeAction = getActionService().createCompositeAction();
        for(TemplateActionDefinition tad : templateActionDefinitions)
        {   
            compositeAction.addAction(tad.getAction(nodeRef));
        }
        
        if (getCompensatingTemplateCompositeActionDefinition() != null)
        {
            compositeAction.setCompensatingAction(getCompensatingTemplateCompositeActionDefinition().getAction(nodeRef));
        }

        return compositeAction;
    }

}
