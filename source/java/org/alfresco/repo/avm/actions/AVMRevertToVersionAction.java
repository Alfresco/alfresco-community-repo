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
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

/**
 * Revert a single path to a specified node. The path in head is passed
 * as actionedUponNodeRef.  The node to revert to is passed as an AVMNodeDescriptor
 * parameter.
 * @author britt
 */
public class AVMRevertToVersionAction extends ActionExecuterAbstractBase 
{
    private static Logger fgLogger = Logger.getLogger(AVMRevertToVersionAction.class);
    
    public static final String NAME = "avm-revert-to-version";
    // The node to revert to. Passed as an AVMNodeDescriptor.
    public static final String TOREVERT = "to-revert";
    
    private AVMService fAVMService;

    /**
     * Set the AVMService.
     */
    public void setAvmService(AVMService service)
    {
        fAVMService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
    {
        Pair<Integer, String> versionPath = 
            AVMNodeConverter.ToAVMVersionPath(actionedUponNodeRef);
        AVMNodeDescriptor toRevert = 
            (AVMNodeDescriptor)action.getParameterValue(TOREVERT);
        fAVMService.revert(versionPath.getSecond(), toRevert);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(
                new ParameterDefinitionImpl(TOREVERT,
                                            DataTypeDefinition.ANY,
                                            true,
                                            getParamDisplayLabel(TOREVERT)));
    }
}
