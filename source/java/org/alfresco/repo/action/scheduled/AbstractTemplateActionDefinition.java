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
