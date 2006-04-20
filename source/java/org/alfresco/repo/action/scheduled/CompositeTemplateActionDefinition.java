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
     * @return
     */
    public List<TemplateActionDefinition> templateActionDefinitions()
    {
        return templateActionDefinitions;
    }

    /**
     * Build the composite action in the context of the given node.
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
