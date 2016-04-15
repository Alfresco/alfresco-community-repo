
package org.alfresco.repo.rendition;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/*
 * In Alfresco 3.3, this class was a required 'containing action' for all renditions.
 * It added handling for asynchronous renditions as well as some common business logic.
 * It should no longer be used.
 * 
 * @author Neil McErlean
 * @since 3.3
 * @deprecated Starting with Alfresco 3.4 the perform-rendition action is no longer
 *             necessary. All the work previously done by this class has been moved
 *             into the rendition action executers themselves. This class now delegates
 *             straight to those actions and therefore is no longer needed.
 */
@Deprecated
public class PerformRenditionActionExecuter extends ActionExecuterAbstractBase
{
    private static final Log log = LogFactory.getLog(PerformRenditionActionExecuter.class);

    /** Action name and parameters */
    public static final String NAME = "perform-rendition";
    public static final String PARAM_RENDITION_DEFINITION = "renditionDefinition";

    /*
     * Injected beans
     */
    private ActionService actionService;

    /**
     * Injects the actionService bean.
     * 
     * @param actionService
     *            the actionService.
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    @Override
    protected void executeImpl(final Action containingAction, final NodeRef actionedUponNodeRef)
    {
        final RenditionDefinition renditionDefinition = getRenditionDefinition(containingAction);
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Rendering node ").append(actionedUponNodeRef).append(" with rendition definition ").append(
                        renditionDefinition.getRenditionName());
            msg.append("\n").append("  parameters:").append("\n");
            if (renditionDefinition.getParameterValues().isEmpty() == false)
            {
            	for (String paramKey : renditionDefinition.getParameterValues().keySet())
            	{
            		msg.append("    ").append(paramKey).append("=").append(renditionDefinition.getParameterValue(paramKey)).append("\n");
            	}
            }
            else
            {
            	msg.append("    [None]");
            }
            log.debug(msg.toString());
        }

        ChildAssociationRef result = executeRendition(actionedUponNodeRef, renditionDefinition);
        containingAction.setParameterValue(PARAM_RESULT, result);
    }

    /**
     * This method gets the (mandatory) rendition definition parameter from the containing action.
     * @param containingAction the containing action.
     * @return the rendition definition.
     * @throws IllegalArgumentException if the rendition definition is missing.
     */
    private RenditionDefinition getRenditionDefinition(final Action containingAction)
    {
        Serializable rendDefObj = containingAction.getParameterValue(PARAM_RENDITION_DEFINITION);
        ParameterCheck.mandatory(PARAM_RENDITION_DEFINITION, rendDefObj);
        return (RenditionDefinition) rendDefObj;
    }

    /**
     * This method delegates to the action service for the execution of the rendition.
     * @param sourceNode NodeRef
     * @param definition RenditionDefinition
     * @return the ChildAssociationRef result.
     */
    private ChildAssociationRef executeRendition(NodeRef sourceNode, RenditionDefinition definition)
    {
        actionService.executeAction(definition, sourceNode);
        // Extract the result from the action
        Serializable serializableResult = definition.getParameterValue(ActionExecuter.PARAM_RESULT);
        return (ChildAssociationRef) serializableResult;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_RENDITION_DEFINITION, DataTypeDefinition.ANY, true,
                    getParamDisplayLabel(PARAM_RENDITION_DEFINITION)));

        paramList.add(new ParameterDefinitionImpl(PARAM_RESULT, DataTypeDefinition.CHILD_ASSOC_REF, false,
                    getParamDisplayLabel(PARAM_RESULT)));
    }
}
