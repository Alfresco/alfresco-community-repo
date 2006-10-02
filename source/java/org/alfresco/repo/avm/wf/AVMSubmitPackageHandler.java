package org.alfresco.repo.avm.wf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

public class AVMSubmitPackageHandler extends JBPMSpringActionHandler implements
        Serializable 
{
    private static final long serialVersionUID = 4113360751217684995L;

    /**
     * The AVMService instance.
     */
    private AVMService fAVMService;
    
    /**
     * The AVMSyncService instance.
     */
    private AVMSyncService fAVMSyncService;

    /**
     * The NodeService reference.
     */
    private NodeService fNodeService;
    
    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        fAVMService = (AVMService)factory.getBean("AVMService");
        fAVMSyncService = (AVMSyncService)factory.getBean("AVMSyncService");
        fNodeService = (NodeService)factory.getBean("NodeService");
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
        String srcStoreName = (String)executionContext.getContextInstance().getVariable("storeName");
        NodeRef pkg = (NodeRef)executionContext.getContextInstance().getVariable("package");
        String webSiteName = 
            fAVMService.getStoreProperty(srcStoreName, QName.createQName(null, ".website.name")).getStringValue();
        String stagingName = webSiteName + "-staging";
        List<ChildAssociationRef> children = fNodeService.getChildAssocs(pkg);
        List<AVMDifference> diffs = new ArrayList<AVMDifference>();
        for (ChildAssociationRef child : children)
        {
            NodeRef childRef = child.getChildRef();
            Pair<Integer, String> childPath = AVMNodeConverter.ToAVMVersionPath(childRef);
            List<Pair<Integer, String>> possiblePaths = 
                fAVMService.getPathsInStoreHead(fAVMService.lookup(childPath.getFirst(), childPath.getSecond()), 
                                                srcStoreName);
            Pair<Integer, String> actualPath = possiblePaths.get(0);
            String [] pathParts = actualPath.getSecond().split(":");
            AVMDifference diff = 
                new AVMDifference(-1, srcStoreName + ":" + pathParts[1],
                                  -1, stagingName + ":" + pathParts[1],
                                  AVMDifference.NEWER);
            diffs.add(diff);
        }
        fAVMSyncService.update(diffs, true, true, false, false);
        fAVMSyncService.flatten(srcStoreName + ":/appBase",
                                stagingName + ":/appBase");
    }
}
