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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm.wf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.config.JNDIConstants;
import org.alfresco.linkvalidation.HrefValidationProgress;
import org.alfresco.linkvalidation.LinkValidationAction;
import org.alfresco.linkvalidation.LinkValidationReport;
import org.alfresco.linkvalidation.LinkValidationService;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * Performs a link validaton check for the workflow sandbox being used
 * for a submisson process.
 * 
 * @author gavinc
 */
public class AVMSubmitLinkChecker extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 1442635948148675461L;

    private static Logger logger = Logger.getLogger(AVMSubmitLinkChecker.class);
    
    /**
     * The AVMService.
     */
    private AVMService fAVMService;
    
    /**
     * The ActionService.
     */
    private ActionService fActionService;
    
    /**
     * Set any bean references necessary.
     * @param factory The BeanFactory from which to get beans.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        this.fActionService = (ActionService)factory.getBean("ActionService");
        this.fAVMService = (AVMService)factory.getBean("AVMService");
    }

    /**
     * Do the actual link validation check.
     * 
     * @param executionContext The jBPM context.
     */
    public void execute(ExecutionContext executionContext) throws Exception
    {
        // retrieve the workflow sandbox (the workflow package)
        NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().getVariable("bpm_package")).getNodeRef();
        Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);

        // remove the trailing www from the path
        String path = pkgPath.getSecond();
        path = path.substring(0, (path.length()-JNDIConstants.DIR_DEFAULT_WWW.length()));
        NodeRef storePath = AVMNodeConverter.ToNodeRef(-1, path);
        
        // get the store name
        String storeName = pkg.getStoreRef().getIdentifier();

        if (logger.isDebugEnabled())
            logger.info("Found workflow store to check links for: " + path);

        // create and execute the action in the background
        Throwable cause = null;
        int brokenLinks = -1;
        
        try
        {
           HrefValidationProgress monitor = new HrefValidationProgress();
           Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
           args.put(LinkValidationAction.PARAM_MONITOR, monitor);
           Action action = this.fActionService.createAction(LinkValidationAction.NAME, args);
           this.fActionService.executeAction(action, storePath, false, false);
           
           // retrieve the deployment report from the store property
           PropertyValue val = this.fAVMService.getStoreProperty(storeName, 
                  SandboxConstants.PROP_LINK_VALIDATION_REPORT);
           if (val != null)
           {
               LinkValidationReport report = (LinkValidationReport)val.getSerializableValue();
               if (report != null && report.wasSuccessful())
               {
                  brokenLinks = report.getNumberBrokenLinks();
               }
               else
               {
                  cause = report.getError();
               }
           }
           
           if (logger.isDebugEnabled())
               logger.debug("Link validation check found " + brokenLinks + " broken links");
        }
        catch (Throwable err)
        {
           cause = err;
        }
        
        // set the number of broken links in a variable
        if (brokenLinks == -1)
        {
           // TODO: Decide how to handle errors,
           //       for now just return -1 and the workflow can decide
        }
        
        executionContext.setVariable("wcmwf_brokenLinks", brokenLinks);
    }
}
