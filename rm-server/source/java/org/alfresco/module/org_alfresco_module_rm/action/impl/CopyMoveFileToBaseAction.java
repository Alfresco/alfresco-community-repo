package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.Arrays;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;

/**
 * File To action implementation.
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public abstract class CopyMoveFileToBaseAction extends RMActionExecuterAbstractBase
{
    /** action parameters */
    public static final String PARAM_DESTINATION_RECORD_FOLDER = "destinationRecordFolder";
    public static final String PARAM_PATH = "path";
    public static final String PARAM_CREATE_RECORD_PATH = "createRecordPath";
    public static final String ACTION_FILETO = "fileTo";

    /** file folder service */
    private FileFolderService fileFolderService;

    /** file plan service */
    private FilePlanService filePlanService;

    /** action modes */
    public enum CopyMoveFileToActionMode
    {
        COPY, MOVE
    };
    protected CopyMoveFileToActionMode mode;

    /**
     * @param fileFolderService file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_PATH, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_PATH)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CREATE_RECORD_PATH, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_CREATE_RECORD_PATH)));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) &&
            (freezeService.isFrozen(actionedUponNodeRef) == false) &&
            (!ACTION_FILETO.equals(action.getActionDefinitionName()) || !recordService.isFiled(actionedUponNodeRef)) &&
            (!(ACTION_FILETO.equals(action.getActionDefinitionName()) && RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER.equals(nodeService.getType(actionedUponNodeRef)))))
        {
            boolean targetIsUnfiledRecord;
            if (ACTION_FILETO.equals(action.getActionDefinitionName()))
            {
                targetIsUnfiledRecord = false;
            }
            else
            {
                QName actionedUponType = nodeService.getType(actionedUponNodeRef);
                targetIsUnfiledRecord = (dictionaryService.isSubClass(actionedUponType, ContentModel.TYPE_CONTENT) && !recordService
                        .isFiled(actionedUponNodeRef))
                        || RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER.equals(actionedUponType);
            }

            // first look to see if the destination record folder has been specified
            NodeRef recordFolder = (NodeRef)action.getParameterValue(PARAM_DESTINATION_RECORD_FOLDER);
            if (recordFolder == null)
            {
                // get the reference to the record folder based on the relative path
                recordFolder = createOrResolvePath(action, actionedUponNodeRef, targetIsUnfiledRecord);
            }

            if (recordFolder == null)
            {
                throw new AlfrescoRuntimeException("Unable to execute file to action, because the destination record folder could not be determined.");
            }

            final NodeRef finalRecordFolder = recordFolder;
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    try
                    {
                        if(mode == CopyMoveFileToActionMode.MOVE)
                        {
                            fileFolderService.move(actionedUponNodeRef, finalRecordFolder, null);
                        }
                        else
                        {
                            fileFolderService.copy(actionedUponNodeRef, finalRecordFolder, null);
                        }
                    }
                    catch (FileNotFoundException fileNotFound)
                    {
                        throw new AlfrescoRuntimeException(
                                "Unable to execute file to action, because the " + (mode == CopyMoveFileToActionMode.MOVE ? "move" : "copy") + " operation failed.",
                                fileNotFound
                                );
                    }

                    return null;
                }
            });
        }
    }

    /**
     * Create or resolve the path specified in the action's path parameter
     *
     * @param action
     * @param actionedUponNodeRef
     * @param targetisUnfiledRecords  true is the target is in unfiled records
     * @return
     */
    private NodeRef createOrResolvePath(Action action, NodeRef actionedUponNodeRef, boolean targetisUnfiledRecords)
    {
        // get the starting context
        NodeRef context = getContext(action, actionedUponNodeRef, targetisUnfiledRecords);
        NodeRef path = context;

        // get the path we wish to resolve
        String pathParameter = (String)action.getParameterValue(PARAM_PATH);
        String[] pathElementsArray = StringUtils.tokenizeToStringArray(pathParameter, "/", false, true);
        if((pathElementsArray != null) && (pathElementsArray.length > 0))
        {
            // get the create parameter
            Boolean createValue = (Boolean)action.getParameterValue(PARAM_CREATE_RECORD_PATH);
            boolean create = createValue == null ? false : createValue.booleanValue();

            // create or resolve the specified path
            path = createOrResolvePath(action, context, actionedUponNodeRef, Arrays.asList(pathElementsArray), targetisUnfiledRecords, create, false);
        }
        return path;
    }

    /**
     * Create or resolve the specified path
     *
     * @param action  Action to use for reporting if anything goes wrong
     * @param parent  Parent of path to be created
     * @param actionedUponNodeRef  The node subject to the file/move/copy action
     * @param pathElements  The elements of the path to be created
     * @param targetisUnfiledRecords  true if the target is within unfiled records
     * @param create  true if the path should be creeated if it does not exist
     * @param creating  true if we have already created the parent and therefore can skip the check to see if the next path element already exists
     * @return
     */
    private NodeRef createOrResolvePath(Action action, NodeRef parent, NodeRef actionedUponNodeRef, List<String> pathElements, boolean targetisUnfiledRecords, boolean create, boolean creating)
    {
        NodeRef nodeRef = null;
        String childName = pathElements.get(0);
        boolean lastPathElement = pathElements.size() == 1;
        if(!creating)
        {
            nodeRef = getChild(parent, childName);
        }
        if(nodeRef == null)
        {
            if(create)
            {
                creating = true;
                nodeRef = createChild(
                        action,
                        parent,
                        childName,
                        targetisUnfiledRecords,
                        lastPathElement && (ContentModel.TYPE_CONTENT.equals(nodeService.getType(actionedUponNodeRef)) || RecordsManagementModel.TYPE_NON_ELECTRONIC_DOCUMENT.equals(nodeService.getType(actionedUponNodeRef))));
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to execute " + action.getActionDefinitionName() + " action, because the destination path could not be determined.");
            }
        }
        else
        {
            QName nodeType = nodeService.getType(nodeRef);
            if(nodeType.equals(RecordsManagementModel.TYPE_HOLD_CONTAINER) ||
                    nodeType.equals(RecordsManagementModel.TYPE_TRANSFER_CONTAINER) ||
                    nodeType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER))
            {
                throw new AlfrescoRuntimeException("Unable to execute " + action.getActionDefinitionName() + " action, because the destination path in invalid.");
            }
        }
        if(pathElements.size() > 1)
        {
            nodeRef = createOrResolvePath(action, nodeRef, actionedUponNodeRef, pathElements.subList(1, pathElements.size()), targetisUnfiledRecords, create, creating);
        }
        return nodeRef;
    }

    /**
     * Get the specified child node ref of the specified parent if it exists, otherwise return null
     *
     * @param parent
     * @param childName
     * @return
     */
    private NodeRef getChild(NodeRef parent, String childName)
    {
        NodeRef child = null;
        List<ChildAssociationRef> children = nodeService.getChildAssocs(parent);
        for (ChildAssociationRef childAssoc : children) {
            NodeRef childNodeRef = childAssoc.getChildRef();
            String existingChildName = (String)nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);
            if(existingChildName.equals(childName))
            {
                child = childNodeRef;
                break;
            }
        }
        return child;
    }

    /**
     * Create the specified child of the specified parent
     *
     * @param action  Action to use for reporting if anything goes wrong
     * @param parent  Parent of the child to be created
     * @param childName  The name of the child to be created
     * @param targetisUnfiledRecords  true if the child is being created in the unfiled directory (determines type as unfiled container child)
     * @param lastAsFolder  true if this is the last element of the pathe being created and it should be created as a folder. ignored if targetIsUnfiledRecords is true
     * @return
     */
    private NodeRef createChild(Action action, NodeRef parent, String childName, boolean targetisUnfiledRecords, boolean lastAsFolder)
    {
        NodeRef child = null;
        if(targetisUnfiledRecords)
        {
            child = this.fileFolderService.create(parent, childName, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER).getNodeRef();
        }
        else if(lastAsFolder)
        {
            child = recordFolderService.createRecordFolder(parent, childName);
        }
        else
        {
            if(RecordsManagementModel.TYPE_RECORD_FOLDER.equals(nodeService.getType(parent)))
            {
                throw new AlfrescoRuntimeException("Unable to execute " + action.getActionDefinitionName() + " action, because the destination path could not be created.");
            }
            child = this.filePlanService.createRecordCategory(parent, childName);
        }
        return child;

    }

    /**
     * Return the context. This will be the unfiled records container of the context if targetisUnfiledRecords is true
     *
     * @param action
     * @param actionedUponNodeRef
     * @param targetisUnfiledRecords
     * @return
     */
    private NodeRef getContext(Action action, NodeRef actionedUponNodeRef, boolean targetisUnfiledRecords)
    {
        NodeRef context = filePlanService.getFilePlan(actionedUponNodeRef);
        if(targetisUnfiledRecords && (context != null) && nodeService.exists(context))
        {
            context = filePlanService.getUnfiledContainer(context);
        }
        if((context == null) || (!nodeService.exists(context)))
        {
            throw new AlfrescoRuntimeException("Unable to execute " + action.getActionDefinitionName() + " action, because the path resolution context could not be determined.");
        }
        return context;
    }

}
