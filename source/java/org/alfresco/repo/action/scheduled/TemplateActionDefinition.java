package org.alfresco.repo.action.scheduled;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A template action definition is used to generate an action from a template style 
 * definition. 
 * 
 * @author Andy Hind
 */
public interface TemplateActionDefinition
{   
    /**
     * Generate an action definition for the action defined by this template.
     * 
     * @param nodeRef NodeRef
     * @return - the action.
     */
    public Action getAction(NodeRef nodeRef);
}
