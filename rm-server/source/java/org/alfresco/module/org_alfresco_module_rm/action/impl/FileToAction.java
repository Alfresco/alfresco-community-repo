package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.util.StringUtils;

/**
 * File To action implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class FileToAction extends RMActionExecuterAbstractBase
{
    public static final String NAME = "fileTo";
    
    public static final String PARAM_DESTINATION_RECORD_FOLDER = "destinationRecordFolder";
    public static final String PARAM_PATH = "path";
    
    private FileFolderService fileFolderService;
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /*
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (recordService.isFiled(actionedUponNodeRef) == false)
        {
            // first look to see if the destination record folder has been specified
            NodeRef recordFolder = (NodeRef)action.getParameterValue(PARAM_DESTINATION_RECORD_FOLDER);
            if (recordFolder == null)
            {
                // get the reference to the record folder based on the relative path
                recordFolder = resolvePath(action, actionedUponNodeRef);
            }
            
            if (recordFolder == null)
            {
                throw new AlfrescoRuntimeException("Unable to execute file to action, because the destination record folder could not be determined.");
            }
             
            if (recordsManagementService.isRecordFolder(recordFolder) == true)
            {
                // TODO .. what if a record of the same name already exists in the destination record folder??
                try
                {
                    fileFolderService.move(actionedUponNodeRef, recordFolder, null);
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
        else
        {
            throw new AlfrescoRuntimeException("Unable to execute file to action, because the actioned upon node is not an unfiled record.");
        }
    }
    
    private NodeRef resolvePath(Action action, NodeRef actionedUponNodeRef)
    {
        // TODO we could provide a parameter to override the file plan as the path context
        
        // the file plan node always provides the context 
        NodeRef context = recordsManagementService.getFilePlan(actionedUponNodeRef);
        if (context == null)
        {
            throw new AlfrescoRuntimeException("Unable to execute file to action, because the path resolution context could not be found.");
        }
        
        // assume by default the result is the context
        NodeRef result = context;
        
        // look for the path parameter
        String path = (String)action.getParameterValue(PARAM_PATH);
        if (path != null && path.isEmpty() == false)
        {
            String[] pathValues = StringUtils.tokenizeToStringArray(path, "/", false, true);
            try
            {
                FileInfo fileInfo = fileFolderService.resolveNamePath(context, new ArrayList<String>(Arrays.asList(pathValues)));
                result = fileInfo.getNodeRef();                
            }
            catch (FileNotFoundException fileNotFound)
            {
                // TODO .. handle this exception by possibly creating the missing record folder ???
                
                throw new AlfrescoRuntimeException("Unable to execute file to action, because destination path could not be resolved.");
            }
        }
        
        return result;
    }
}
