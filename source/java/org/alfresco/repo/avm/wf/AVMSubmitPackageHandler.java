package org.alfresco.repo.avm.wf;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
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
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        fAVMService = (AVMService)factory.getBean("AVMService");
        fAVMSyncService = (AVMSyncService)factory.getBean("AVMSyncService");
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
        // TODO: Allow submit parameters to passed into this action handler
        //       rather than pulling directly from execution context
        
        NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().getVariable("bpm_package")).getNodeRef();
        Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);

        // submit the package changes
        String description = (String)executionContext.getContextInstance().getVariable("bpm_workflowDescription");
        String tag = (String)executionContext.getContextInstance().getVariable("wcmwf_label");
        AVMNodeDescriptor pkgDesc = fAVMService.lookup(pkgPath.getFirst(), pkgPath.getSecond());
        String targetPath = pkgDesc.getIndirection();
		List<AVMDifference> diff = fAVMSyncService.compare(pkgPath.getFirst(), pkgPath.getSecond(), -1, targetPath, null);
        fAVMSyncService.update(diff, null, true, true, false, false, tag, description);

        // flatten source folder where changes were submitted from
        String from = (String)executionContext.getContextInstance().getVariable("wcmwf_fromPath");
        if (from != null && from.length() > 0)
        {
            fAVMSyncService.flatten(from, targetPath);
        }
    }
}
