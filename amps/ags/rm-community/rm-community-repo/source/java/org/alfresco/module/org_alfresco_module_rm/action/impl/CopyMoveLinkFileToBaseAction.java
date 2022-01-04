/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

/**
 * File To action implementation.
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public abstract class CopyMoveLinkFileToBaseAction extends RMActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(CopyMoveLinkFileToBaseAction.class);

    /** action parameters */
    public static final String PARAM_DESTINATION_RECORD_FOLDER = "destinationRecordFolder";
    public static final String PARAM_PATH = "path";
    public static final String PARAM_CREATE_RECORD_PATH = "createRecordPath";
    public static final String ACTION_FILETO = "fileTo";
    public static final String ACTION_LINKTO = "linkTo";

    /** file folder service */
    private FileFolderService fileFolderService;

    /** file plan service */
    private FilePlanService filePlanService;

    /** action modes */
    public enum CopyMoveLinkFileToActionMode
    {
        COPY, MOVE, LINK
    }

    /** Action Mode */
    private CopyMoveLinkFileToActionMode mode;

    /**
     * @return Action Mode
     */
    protected CopyMoveLinkFileToActionMode getMode()
    {
        return this.mode;
    }

    /**
     * Sets the action mode
     *
     * @param mode Action mode
     */
    protected void setMode(CopyMoveLinkFileToActionMode mode)
    {
        this.mode = mode;
    }

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
    protected synchronized void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        String actionName = action.getActionDefinitionName();
        if (isOkToProceedWithAction(actionedUponNodeRef, actionName))
        {
            QName actionedUponType = getNodeService().getType(actionedUponNodeRef);

            boolean targetIsUnfiledRecords;
            if (ACTION_FILETO.equals(action.getActionDefinitionName()))
            {
                targetIsUnfiledRecords = false;
            }
            else
            {
                targetIsUnfiledRecords = (getDictionaryService().isSubClass(actionedUponType, ContentModel.TYPE_CONTENT) && !getRecordService().isFiled(actionedUponNodeRef))
                        || TYPE_UNFILED_RECORD_FOLDER.equals(actionedUponType);
            }

            // first look to see if the destination record folder has been specified
            NodeRef recordFolder = (NodeRef)action.getParameterValue(PARAM_DESTINATION_RECORD_FOLDER);
            if (recordFolder == null)
            {
                recordFolder = createOrResolvePath(action, actionedUponNodeRef, targetIsUnfiledRecords);
            }

            // now we have the reference to the target folder we can do some final checks to see if the action is valid
            validateActionPostPathResolution(actionedUponNodeRef, recordFolder, actionName, targetIsUnfiledRecords);

            final NodeRef finalRecordFolder = recordFolder;
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    try
                    {
                        if (getMode() == CopyMoveLinkFileToActionMode.MOVE)
                        {
                            fileFolderService.move(actionedUponNodeRef, finalRecordFolder, null);
                        }
                        else if (getMode() == CopyMoveLinkFileToActionMode.COPY)
                        {
                            fileFolderService.copy(actionedUponNodeRef, finalRecordFolder, null);
                        }
                        else if (getMode() == CopyMoveLinkFileToActionMode.LINK)
                        {
                            getRecordService().link(actionedUponNodeRef, finalRecordFolder);
                        }
                    }
                    catch (FileNotFoundException fileNotFound)
                    {
                        throw new AlfrescoRuntimeException("Unable to execute file to action, because the " + (mode == CopyMoveLinkFileToActionMode.MOVE ? "move" : "copy") + " operation failed.", fileNotFound);
                    }

                    return null;
                }
            });
        }
    }

    /**
     * Return true if the passed parameters to the action are valid for the given action
     *
     * @param actionedUponNodeRef
     * @param actionName
     * @return
     */
    private boolean isOkToProceedWithAction(NodeRef actionedUponNodeRef, String actionName)
    {
        // Check that the incoming parameters are valid prior to performing any action
        boolean okToProceed = false;
        if(getNodeService().exists(actionedUponNodeRef) && !getFreezeService().isFrozen(actionedUponNodeRef))
        {
            QName actionedUponType = getNodeService().getType(actionedUponNodeRef);
            if(ACTION_FILETO.equals(actionName))
            {
                // file to action can only be performed on unfiled records
                okToProceed = !getRecordService().isFiled(actionedUponNodeRef) && getDictionaryService().isSubClass(actionedUponType, ContentModel.TYPE_CONTENT);
                if(!okToProceed && logger.isDebugEnabled())
                {
                    logger.debug("Unable to run " + actionName + " action on a node that isn't unfiled and a sub-class of content type");
                }
            }
            else if(ACTION_LINKTO.equals(actionName))
            {
                // link to action can only be performed on filed records
                okToProceed = getRecordService().isFiled(actionedUponNodeRef) && getDictionaryService().isSubClass(actionedUponType, ContentModel.TYPE_CONTENT);
                if(!okToProceed && logger.isDebugEnabled())
                {
                    logger.debug("Unable to run " + actionName + " action on a node that isn't filed and a sub-class of content type");
                }
            }
            else
            {
                okToProceed = true;
            }
        }
        return okToProceed;
    }

    /**
     * Do a final validation for the parameters and the resolve target path
     *
     * @param actionedUponNodeRef
     * @param target
     * @param actionName
     * @param targetIsUnfiledRecords
     */
    private void validateActionPostPathResolution(NodeRef actionedUponNodeRef, NodeRef target, String actionName, boolean targetIsUnfiledRecords)
    {
        QName actionedUponType = getNodeService().getType(actionedUponNodeRef);
        // now we have the reference to the target folder we can do some final checks to see if the action is valid
        if (target == null)
        {
            throw new AlfrescoRuntimeException("Unable to run " + actionName + " action, because the destination record folder could not be determined.");
        }
        if(targetIsUnfiledRecords)
        {
            QName targetFolderType = getNodeService().getType(target);
            if(!TYPE_UNFILED_RECORD_CONTAINER.equals(targetFolderType) && !TYPE_UNFILED_RECORD_FOLDER.equals(targetFolderType))
            {
                throw new AlfrescoRuntimeException("Unable to run " + actionName + " action, because the destination record folder is an inappropriate type.");
            }
        }
        else
        {
            if(getRecordFolderService().isRecordFolder(target) && !getDictionaryService().isSubClass(actionedUponType, ContentModel.TYPE_CONTENT) && (getRecordFolderService().isRecordFolder(actionedUponNodeRef) || filePlanService.isRecordCategory(actionedUponNodeRef)))
            {
                throw new AlfrescoRuntimeException("Unable to run " + actionName + " action, because the destination record folder is an inappropriate type. A record folder cannot contain another folder or a category");
            }
            else if(filePlanService.isRecordCategory(target) && getDictionaryService().isSubClass(actionedUponType, ContentModel.TYPE_CONTENT))
            {
                throw new AlfrescoRuntimeException("Unable to run " + actionName + " action, because the destination record folder is an inappropriate type. A record category cannot contain a record");
            }
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
    private NodeRef createOrResolvePath(final Action action, final NodeRef actionedUponNodeRef, final boolean targetisUnfiledRecords)
    {
        // get the starting context
        final NodeRef context = getContext(action, actionedUponNodeRef, targetisUnfiledRecords);
        NodeRef path = context;

        // get the path we wish to resolve
        String pathParameter = (String)action.getParameterValue(PARAM_PATH);
        final String[] pathElementsArray = StringUtils.tokenizeToStringArray(pathParameter, "/", false, true);
        if((pathElementsArray != null) && (pathElementsArray.length > 0))
        {
            // get the create parameter
            Boolean createValue = (Boolean)action.getParameterValue(PARAM_CREATE_RECORD_PATH);
            final boolean create = createValue == null ? false : createValue.booleanValue();
            QName type = getNodeService().getType(actionedUponNodeRef);
            final boolean isRecord = getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT);

            // create or resolve the specified path
            path = getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    return createOrResolvePath(action, context, actionedUponNodeRef, isRecord, Arrays.asList(pathElementsArray), targetisUnfiledRecords, create, false);
                }
            }, false, true);
        }
        return path;
    }

    /**
     * Create or resolve the specified path
     *
     * @param action  Action to use for reporting if anything goes wrong
     * @param parent  Parent of path to be created
     * @param actionedUponNodeRef  The node subject to the file/move/copy action
     * @param isRecord true if node is a CONTENT SubType
     * @param pathElements  The elements of the path to be created
     * @param targetisUnfiledRecords  true if the target is within unfiled records
     * @param create  true if the path should be creeated if it does not exist
     * @param creating  true if we have already created the parent and therefore can skip the check to see if the next path element already exists
     * @return
     */
    private NodeRef createOrResolvePath(Action action, NodeRef parent, NodeRef actionedUponNodeRef, boolean isRecord, List<String> pathElements, boolean targetisUnfiledRecords, boolean create, boolean creating)
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
                boolean lastAsFolder = lastPathElement && isRecord;
                nodeRef = createChild(action, parent, childName, targetisUnfiledRecords, lastAsFolder);
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to execute " + action.getActionDefinitionName() + " action, because the destination path could not be determined.");
            }
        }
        else
        {
            QName nodeType = getNodeService().getType(nodeRef);
            if(nodeType.equals(RecordsManagementModel.TYPE_HOLD_CONTAINER) ||
                    nodeType.equals(RecordsManagementModel.TYPE_TRANSFER_CONTAINER) ||
                    nodeType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER))
            {
                throw new AlfrescoRuntimeException("Unable to execute " + action.getActionDefinitionName() + " action, because the destination path in invalid.");
            }
        }
        if(pathElements.size() > 1)
        {
            nodeRef = createOrResolvePath(action, nodeRef, actionedUponNodeRef, isRecord, pathElements.subList(1, pathElements.size()), targetisUnfiledRecords, create, creating);
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
        return getNodeService().getChildByName(parent, ContentModel.ASSOC_CONTAINS, childName);
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
    private NodeRef createChild(final Action action, final NodeRef parent, final String childName, final boolean targetisUnfiledRecords, final boolean lastAsFolder)
    {
        // double check that the child hasn't been created by another thread
        NodeRef child = getChild(parent, childName);
        if (child == null)
        {
            if (targetisUnfiledRecords)
            {
                // create unfiled folder
                child = fileFolderService.create(parent, childName, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER).getNodeRef();
            }
            else if (lastAsFolder)
            {
                // create record folder
                child = getRecordFolderService().createRecordFolder(parent, childName);
            }
            else
            {
                // ensure we are not trying to create a record category in a record folder
                if (RecordsManagementModel.TYPE_RECORD_FOLDER.equals(getNodeService().getType(parent)))
                {
                    throw new AlfrescoRuntimeException("Unable to execute " + action.getActionDefinitionName() + " action, because the destination path has a record category within a record folder.");
                }

                // create record category
                child = filePlanService.createRecordCategory(parent, childName);
            }
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
        if(targetisUnfiledRecords && (context != null) && getNodeService().exists(context))
        {
            context = filePlanService.getUnfiledContainer(context);
        }
        if((context == null) || (!getNodeService().exists(context)))
        {
            throw new AlfrescoRuntimeException("Unable to execute " + action.getActionDefinitionName() + " action, because the path resolution context could not be determined.");
        }
        return context;
    }

}
