package org.alfresco.repo.action.executer;

import org.alfresco.repo.action.ActionDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * An action executor that really does nothing.
 *
 * @author Gethin James
 */
public class EmptyActionExecutor extends ActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(EmptyActionExecutor.class);

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        logger.info("I did nothing of interest.");
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        //No Op
    }

    @Override
    public ActionDefinition getActionDefinition()
    {
        if (this.actionDefinition == null)
        {
            this.actionDefinition = createActionDefinition(this.name);
            ((ActionDefinitionImpl)this.actionDefinition).setTitleKey(getTitleKey());
            ((ActionDefinitionImpl)this.actionDefinition).setDescriptionKey(getDescriptionKey());
            ((ActionDefinitionImpl)this.actionDefinition).setTrackStatus(getTrackStatus());
            ((ActionDefinitionImpl)this.actionDefinition).setAdhocPropertiesAllowed(getAdhocPropertiesAllowed());
            ((ActionDefinitionImpl)this.actionDefinition).setRuleActionExecutor(this.name);
            ((ActionDefinitionImpl)this.actionDefinition).setApplicableTypes(this.applicableTypes);
            ((ActionDefinitionImpl) this.actionDefinition).setParameterDefinitions(null);
        }
        return this.actionDefinition;

    }
}
