/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.avm.wf;

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


/**
 * Clear "submitted" mark from (source of) items within the WCM Workflow Package
 * 
 * @author davidc
 */
public class AVMClearSubmittedHandler extends JBPMSpringActionHandler 
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
     * The AVMSubmittedAspect instance.
     */
    private AVMSubmittedAspect fAVMSubmittedAspect;
    

    
    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        fAVMService = (AVMService)factory.getBean("AVMService");
        fAVMSyncService = (AVMSyncService)factory.getBean("AVMSyncService");
        fAVMSubmittedAspect = (AVMSubmittedAspect)factory.getBean("AVMSubmittedAspect");
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
        // TODO: Allow submit parameters to be passed into this action handler
        //       rather than pulling directly from execution context
        
        // NOTE: Submitted items can only be marked as "submitted" if we know where they came from
        String from = (String)executionContext.getContextInstance().getVariable("wcmwf_fromPath");
        if (from != null && from.length() > 0)
        {
            // retrieve list of changes in submitted package
            NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().getVariable("bpm_package")).getNodeRef();
            Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);
            AVMNodeDescriptor pkgDesc = fAVMService.lookup(pkgPath.getFirst(), pkgPath.getSecond());
            String targetPath = pkgDesc.getIndirection();
            List<AVMDifference> diffs = fAVMSyncService.compare(pkgPath.getFirst(), pkgPath.getSecond(), -1, targetPath, null);

            // for each change, mark original as submitted
            for (AVMDifference diff : diffs)
            {
                String submittedPath = from + diff.getSourcePath().substring(pkgPath.getSecond().length());
                fAVMSubmittedAspect.clearSubmitted(-1, submittedPath);
            }
        }
    }
    
}
