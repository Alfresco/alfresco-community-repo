/**
 * 
 */
package org.alfresco.repo.avm.actions;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

/**
 * This action handles reverting a selected set of nodes to a particular version.
 * The set of of nodes is passed in as a packed string (Obtained by VersionPathStuffer).
 * The actionedUponNodeRef is a dummy and can be null.
 * @author britt
 */
public class AVMRevertListAction extends ActionExecuterAbstractBase 
{
    private static Logger fgLogger = Logger.getLogger(AVMRevertListAction.class);
    
    public static final String NAME = "avm-revert-list";
    // The version to revert to.
    public static final String PARAM_VERSION = "version";
    // The encoded list of nodes.
    public static final String PARAM_NODE_LIST = "node-list";
    // Flag for whether we should flatten after revert.
    public static final String PARAM_FLATTEN = "flatten";
    // If we are flattening, then this holds the staging store name.
    public static final String PARAM_STAGING = "staging";
    // If we are flattening, then this holds the reverted store's name
    public static final String PARAM_STORE = "store";
    // If we are flattening, then this holds the path "/foo/bar/baz" to flatten.
    public static final String PARAM_FLATTEN_PATH = "flatten-path";
    
    /**
     * The sync service.
     */
    private AVMSyncService fSyncService;
    
    /**
     * Set the sync service.
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
        int revertVersion = (Integer)action.getParameterValue(PARAM_VERSION);
        List<Pair<Integer, String>> versionPaths = 
            (List<Pair<Integer, String>>)action.getParameterValue(PARAM_NODE_LIST);
        List<AVMDifference> diffs = new ArrayList<AVMDifference>();
        for (Pair<Integer, String> item : versionPaths)
        {
            List<AVMDifference> diffSet = 
                fSyncService.compare(revertVersion, item.getSecond(), 
                                     -1, item.getSecond());
            diffs.addAll(diffSet);
        }
        String message = "Reverted to version " + revertVersion;
        fSyncService.update(diffs, false, false, true, true, message, message);
        if (!(Boolean)action.getParameterValue(PARAM_FLATTEN))
        {
            return;
        }
        String storeName = (String)action.getParameterValue(PARAM_STORE);
        String flattenPath = (String)action.getParameterValue(PARAM_FLATTEN_PATH);
        String stagingName = (String)action.getParameterValue(PARAM_STAGING);
        fSyncService.flatten(storeName + ":" + flattenPath, 
                             stagingName + ":" + flattenPath);
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
        paramList.add(
            new ParameterDefinitionImpl(PARAM_NODE_LIST,
                                        DataTypeDefinition.ANY,
                                        true,
                                        getParamDisplayLabel(PARAM_NODE_LIST)));
        paramList.add(
            new ParameterDefinitionImpl(PARAM_FLATTEN,
                                        DataTypeDefinition.BOOLEAN,
                                        false,
                                        getParamDisplayLabel(PARAM_FLATTEN)));
        paramList.add(
            new ParameterDefinitionImpl(PARAM_STAGING,
                                        DataTypeDefinition.TEXT,
                                        false,
                                        getParamDisplayLabel(PARAM_STAGING)));
        paramList.add(
                new ParameterDefinitionImpl(PARAM_STORE,
                                            DataTypeDefinition.TEXT,
                                            false,
                                            getParamDisplayLabel(PARAM_STORE)));
        paramList.add(
            new ParameterDefinitionImpl(PARAM_FLATTEN_PATH,
                                        DataTypeDefinition.TEXT,
                                        false,
                                        getParamDisplayLabel(PARAM_FLATTEN_PATH)));
    }
}
