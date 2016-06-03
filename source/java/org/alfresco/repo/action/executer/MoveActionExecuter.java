package org.alfresco.repo.action.executer;

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Move action executor.
 * <p>
 * Moves the actioned upon node to a specified location.
 * 
 * @author Roy Wetherall
 */
public class MoveActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "move";
    public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
    
    /**
     * FileFolder service
     */
    private FileFolderService fileFolderService;
    
    /**
     * The node service
     */
    private NodeService nodeService;
	
    public void setFileFolderService(FileFolderService fileFolderService) 
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * Sets the node service
     */
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(Action, NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        // ALF-17635: A move action should not fire on a working copy - wait until check in
        if (this.nodeService.exists(actionedUponNodeRef)
                && !this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            NodeRef destinationParent = (NodeRef) ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);

            // Check the destination not to be in a pending delete list
            // MNT-11695
            Set<QName> destinationAspects = nodeService.getAspects(destinationParent);
            if (destinationAspects.contains(ContentModel.ASPECT_PENDING_DELETE))
            {
                return;
            }

            try
            {
                fileFolderService.move(actionedUponNodeRef, destinationParent, null);
            }
            catch (FileNotFoundException e)
            {
                // Do nothing
            }
        }
    }

}
