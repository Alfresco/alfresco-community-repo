/**
 * 
 */
package org.alfresco.repo.avm.actions;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

/**
 * Reverts a node and everything underneath it to a specified version.
 * @author britt
 */
public class AVMRevertStoreAction extends ActionExecuterAbstractBase 
{
    @SuppressWarnings("unused")
    private static Logger fgLogger = Logger.getLogger(AVMRevertStoreAction.class);
    
    public static final String NAME = "avm-revert-store";
    public static final String PARAM_VERSION = "version";
    
    /**
     * The AVM Synchronization Service.
     */
    private AVMSyncService fSyncService;
 
    /**
     * Set the Sync Service.
     */
    public void setAvmSyncService(AVMSyncService service)
    {
        fSyncService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
    {
        // All this does is an override submit from the older version
        // to head of the store implied in the path.
        Pair<Integer, String> pathVersion = 
            AVMNodeConverter.ToAVMVersionPath(actionedUponNodeRef);
        int revertVersion = (Integer)action.getParameterValue(PARAM_VERSION);
        List<AVMDifference> diffs =
            fSyncService.compare(revertVersion, pathVersion.getSecond(), 
                                 -1, pathVersion.getSecond(), null);
        String message = "Reverted to Version " + revertVersion + ".";
        fSyncService.update(diffs, null, false, false, true, true, message, message);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(
            new ParameterDefinitionImpl(PARAM_VERSION,                              
                                        DataTypeDefinition.INT,                       
                                        true,                                           
                                        getParamDisplayLabel(PARAM_VERSION)));      
    }
}
