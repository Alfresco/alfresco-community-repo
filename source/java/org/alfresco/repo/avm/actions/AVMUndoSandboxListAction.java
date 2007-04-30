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
 * Undos a list of changed nodes in a user sandbox. The set of nodes to undo is
 * passed in as a packed string (Obtained by VersionPathStuffer).
 * The actionedUponNodeRef is a dummy and can be null.
 * @author britt
 */
public class AVMUndoSandboxListAction extends ActionExecuterAbstractBase 
{
    @SuppressWarnings("unused")
    private static Logger fgLogger = Logger.getLogger(AVMUndoSandboxListAction.class);
    
    public static final String NAME = "avm-undo-list";
    // The encoded list of nodes.
    public static final String PARAM_NODE_LIST = "node-list";
    
    /**
     * The AVM Service reference.
     */
    private AVMService fAVMService;
    
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
        List<Pair<Integer, String>> versionPaths = 
            (List<Pair<Integer, String>>)action.getParameterValue(PARAM_NODE_LIST);
        for (Pair<Integer, String> item : versionPaths)
        {
            AVMNodeDescriptor desc = fAVMService.lookup(-1, item.getSecond(), true);
            if (desc == null)
            {
                continue;
            }
            String [] parentChild = AVMNodeConverter.SplitBase(item.getSecond());
            if (parentChild.length != 2)
            {
                continue;
            }
            AVMNodeDescriptor parent = fAVMService.lookup(-1, parentChild[0], true);
            if (parent.isLayeredDirectory())
            {
                fAVMService.makeTransparent(parentChild[0], parentChild[1]);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(
                new ParameterDefinitionImpl(PARAM_NODE_LIST,
                                            DataTypeDefinition.ANY,
                                            true,
                                            getParamDisplayLabel(PARAM_NODE_LIST)));
    }
}
