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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.avm.wf;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

public class AVMSubmitPackageHandler 
   extends JBPMSpringActionHandler 
   implements Serializable 
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
     * The AVMLockingService instance.
     */
    private AVMLockingService fAVMLockingService;

    /**
     * The AVMSubmitTransactionListener instance 
     * (for JMX notification of virtualization server after commit/rollback).
     */
    private AVMSubmitTransactionListener fAVMSubmitTransactionListener;

    
    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        fAVMService = (AVMService)factory.getBean(ServiceRegistry.AVM_SERVICE.getLocalName());
        fAVMSyncService = (AVMSyncService)factory.getBean(ServiceRegistry.AVM_SYNC_SERVICE.getLocalName());
        fAVMLockingService = (AVMLockingService)factory.getBean(ServiceRegistry.AVM_LOCKING_SERVICE.getLocalName());
        fAVMSubmittedAspect = (AVMSubmittedAspect)factory.getBean("AVMSubmittedAspect");
        fAVMSubmitTransactionListener = (AVMSubmitTransactionListener) factory.getBean("AVMSubmitTransactionListener");

        AlfrescoTransactionSupport.bindListener(fAVMSubmitTransactionListener);
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(final ExecutionContext executionContext) 
       throws Exception 
    {
        // TODO: Allow submit parameters to be passed into this action handler
        //       rather than pulling directly from execution context
        
        final NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().getVariable("bpm_package")).getNodeRef();
        final Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);

        // submit the package changes
        final String description = (String)executionContext.getContextInstance().getVariable("bpm_workflowDescription");
        final String tag = (String)executionContext.getContextInstance().getVariable("wcmwf_label");
        final AVMNodeDescriptor pkgDesc = fAVMService.lookup(pkgPath.getFirst(), pkgPath.getSecond());
        final String targetPath = pkgDesc.getIndirection();
        final Map<QName, PropertyValue> dnsProperties = this.fAVMService.queryStorePropertyKey(targetPath.split(":")[0], QName.createQName(null, ".dns%"));
        String webProject = dnsProperties.keySet().iterator().next().getLocalName();
        webProject = webProject.substring(webProject.lastIndexOf('.') + 1, webProject.length());
        final List<AVMDifference> stagingDiffs = fAVMSyncService.compare(pkgPath.getFirst(), pkgPath.getSecond(), -1, targetPath, null);
        for (final AVMDifference diff : stagingDiffs)
        {
            this.recursivelyRemoveLocksAndSubmittedAspect(webProject, 
                                                          diff.getSourceVersion(), 
                                                          diff.getSourcePath());
        }
        
        // Allow AVMSubmitTransactionListener to inspect the staging diffs
        // so it can notify the virtualization server via JMX if when this
        // submit succeeds or fails.   This allows virtual webapps devoted
        // to the workarea to be destroyed, and staging to be updated in
        // the event that some of the files alter the behavior of the
        // webapp itself (e.g.: WEB-INF/web.xml, WEB-INF/lib/*.jar), etc.

        AlfrescoTransactionSupport.bindResource("staging_diffs", stagingDiffs);

        fAVMSyncService.update(stagingDiffs, null, false, false, true, true, tag, description);

        // flatten source folder where changes were submitted from
        String from = (String)executionContext.getContextInstance().getVariable("wcmwf_fromPath");
        if (from != null && from.length() > 0)
        {
            // first, submit changes back to sandbox forcing addition of edits in workflow (and submission 
            // flag removal). second, flatten sandbox, removing modified items that have been submitted
            // TODO: Without locking on the sandbox, it's possible that a change to a "submitted" item
            //       may get lost when the item is finally approved
            List<AVMDifference> sandboxDiffs = fAVMSyncService.compare(pkgPath.getFirst(), pkgPath.getSecond(), -1, from, null);
            fAVMSyncService.update(sandboxDiffs, null, true, true, false, false, tag, description);
            AVMDAOs.Instance().fAVMNodeDAO.flush();
            fAVMSyncService.flatten(from, targetPath);
        }
    }

    /**
     * Recursively remove locks from a path. Walking child folders looking for files
     * to remove locks from.
     */
    private void recursivelyRemoveLocksAndSubmittedAspect(final String webProject, final int version, final String path)
    {
        fAVMSubmittedAspect.clearSubmitted(version, path);
        AVMNodeDescriptor desc = fAVMService.lookup(version, path, true);
        if (desc.isFile() || desc.isDeletedFile())
        {
            fAVMLockingService.removeLock(webProject, path.substring(path.indexOf(":") + 1));
        }
        else
        {
            for (final AVMNodeDescriptor child : fAVMService.getDirectoryListingArray(version, path, true))
            {
                this.recursivelyRemoveLocksAndSubmittedAspect(webProject, version, child.getPath());
            }
        }
    }
}
