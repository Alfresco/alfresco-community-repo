/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.avm.wf;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
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
            // Now delete the stores in the workflow sandbox.
            String [] storePath = pkgPath.getSecond().split(":");
            // Get the sandbox id for the package.
            Map<QName, PropertyValue> matches = fAVMService.queryStorePropertyKey(storePath[0], QName.createQName(null, ".sandbox-id%"));
            QName sandboxID = matches.keySet().iterator().next();
            // Get all the stores in the sandbox.
            Map<String, Map<QName, PropertyValue>> stores = fAVMService.queryStoresPropertyKeys(sandboxID);
            for (String storeName : stores.keySet())
            {
                fAVMService.purgeStore(storeName);
            }
        }
    }
    
}
