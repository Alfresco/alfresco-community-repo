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
     * @param templateActionDefinitions List<TemplateActionDefinition>
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
     * @param nodeRef  NodeRef
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
