package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.StringUtils;

/**
 * File To action implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class FileToAction extends RMActionExecuterAbstractBase
{
    /** action name */
    public static final String NAME = "fileTo";
    
    /** action parameters */
    public static final String PARAM_DESTINATION_RECORD_FOLDER = "destinationRecordFolder";
    public static final String PARAM_PATH = "path";
    public static final String PARAM_CREATE_RECORD_FOLDER = "createRecordFolder";
    
    /** file folder service */
    private FileFolderService fileFolderService;
    
    /** file plan service */
    private FilePlanService filePlanService;
    
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
        paramList.add(new ParameterDefinitionImpl(PARAM_CREATE_RECORD_FOLDER, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_CREATE_RECORD_FOLDER)));
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == true &&
            freezeService.isFrozen(actionedUponNodeRef) == false)
        {
            if (recordService.isFiled(actionedUponNodeRef) == false)
            {
                // first look to see if the destination record folder has been specified
                NodeRef recordFolder = (NodeRef)action.getParameterValue(PARAM_DESTINATION_RECORD_FOLDER);
                if (recordFolder == null)
                {
                    // get the reference to the record folder based on the relative path
                    recordFolder = createOrResolveRecordFolder(action, actionedUponNodeRef);
                }
                
                if (recordFolder == null)
                {
                    throw new AlfrescoRuntimeException("Unable to execute file to action, because the destination record folder could not be determined.");
                }
                 
                if (recordsManagementService.isRecordFolder(recordFolder) == true)
                {
                    final NodeRef finalRecordFolder = recordFolder;                 
                    try
                    {
                        fileFolderService.move(actionedUponNodeRef, finalRecordFolder, null);
                    }
                    catch (FileNotFoundException fileNotFound)
                    {
                       throw new AlfrescoRuntimeException("Unable to execute file to action, because the move operation failed.", fileNotFound);
                    }
                }
                else
                {
                    throw new AlfrescoRuntimeException("Unable to execute file to action, becuase the destination was not a record folder.");
                }
            }
        }
    }
    
    /**
     * 
     * @param action
     * @param actionedUponNodeRef
     * @return
     */
    private NodeRef createOrResolveRecordFolder(Action action, NodeRef actionedUponNodeRef)
    {
        // TODO check the action for a context node reference
        // the file plan node always provides the context 
        NodeRef context = filePlanService.getFilePlan(actionedUponNodeRef);        
        if (context == null)
        {
            throw new AlfrescoRuntimeException("Unable to execute fileTo action, because the path resolution context could not be found.");
        }
        else if (nodeService.exists(context) == false)
        {
            throw new AlfrescoRuntimeException("Unable to execute fileTo action, because the context for the relative path does not exist.");
        }
        
        // look for the path parameter
        String path = (String)action.getParameterValue(PARAM_PATH);
        String[] pathValues = ArrayUtils.EMPTY_STRING_ARRAY;
        
        if (path != null && path.isEmpty() == false)
        {
            pathValues = StringUtils.tokenizeToStringArray(path, "/", false, true);
        }
        
        // look for the creation strategy
        boolean create = false;
        Boolean createValue = (Boolean)action.getParameterValue(PARAM_CREATE_RECORD_FOLDER);
        if (createValue != null)
        {
            create = createValue.booleanValue();
        }
        
        // try and get child
        NodeRef recordFolder = resolvePath(context, pathValues);
        
        if (recordFolder == null)
        {
            if (create == true)
            {
                // get the parent into which we are going to create the new record folder
                NodeRef parent = resolveParent(context, pathValues);
                if (parent == null)
                {
                    throw new AlfrescoRuntimeException("Unable to create new record folder, because destination parent could not be found.");
                }
                
                // ensure we are trying to create a record folder in a record category
                if (filePlanService.isRecordCategory(parent) == false)
                {
                    throw new AlfrescoRuntimeException("Unable to create nre record folder, beacuse the parent is not a record category.");
                }
                
                // get the new record folders name
                String recordFolderName = pathValues[pathValues.length-1];
                recordFolder = recordsManagementService.createRecordFolder(parent, recordFolderName);
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to execute FileTo action, because the destination record folder does not exist.");
            }            
        }
        
        return recordFolder;        
    }
    
    /**
     * 
     * @param context
     * @param pathValues
     * @return
     */
    private NodeRef resolvePath(final NodeRef context, final String[] pathValues)
    {
        NodeRef result = null;
        FileInfo fileInfo = null;
        try
        {
            fileInfo = fileFolderService.resolveNamePath(context, new ArrayList<String>(Arrays.asList(pathValues)), false);
        }
        catch (FileNotFoundException e)
        {
            // ignore, checking for null
        }   
        if (fileInfo != null)
        {
            result = fileInfo.getNodeRef();
        }
        return result;
    }
    
    /**
     * 
     * @param context
     * @param pathValues
     * @return
     */
    private NodeRef resolveParent(NodeRef context, String[] pathValues)
    {
        NodeRef result = null;
        
        if (ArrayUtils.isEmpty(pathValues) == true)
        {
            // this should never occur since if the path is empty then the context it the resolution of the 
            // path .. the context must already exist
            throw new AlfrescoRuntimeException("Unable to resolve the parent, because no valid path was specified.");
        }
        else if (pathValues.length == 1)
        {
            // the context is the parent
            result = context;
        }
        else
        {
            pathValues = (String[])ArrayUtils.remove(pathValues, pathValues.length-1);
            result = resolvePath(context, pathValues);
        }
        
        return result;
    }
    

}
