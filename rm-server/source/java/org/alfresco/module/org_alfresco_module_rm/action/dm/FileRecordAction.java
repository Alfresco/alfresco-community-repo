package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;

public class FileRecordAction extends ActionExecuterAbstractBase
{
    public static final String NAME = "file-record";
    public static final String PARAM_DESTINATION_RECORD_FOLDER = "destination-record-folder";
    
    /**
     * FileFolder service
     */
    private FileFolderService fileFolderService;
    
    public void setFileFolderService(FileFolderService fileFolderService) 
    {
        this.fileFolderService = fileFolderService;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(
                                PARAM_DESTINATION_RECORD_FOLDER, 
                                DataTypeDefinition.NODE_REF, 
                                true, 
                                getParamDisplayLabel(PARAM_DESTINATION_RECORD_FOLDER)));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.ref.NodeRef)
     */
    public void executeImpl(final Action ruleAction, final NodeRef actionedUponNodeRef)
    {
        final NodeRef destinationParent = (NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_RECORD_FOLDER);
        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                try
                {
                    fileFolderService.move(actionedUponNodeRef, destinationParent, null);
                }
                catch (FileNotFoundException e)
                {
                    throw new AlfrescoRuntimeException("Could not file record.", e);
                }
                
                return null;
            }
            
        });
        
        
    }
}
