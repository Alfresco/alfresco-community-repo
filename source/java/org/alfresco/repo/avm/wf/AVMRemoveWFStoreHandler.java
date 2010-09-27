/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.avm.wf;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.alfresco.wcm.sandbox.SandboxFactory;
import org.alfresco.wcm.util.WCMUtil;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;


/**
 * Remove WF sandbox
 * 
 * @author brittp
 */
public class AVMRemoveWFStoreHandler extends JBPMSpringActionHandler 
{
    private static final long serialVersionUID = 4113360751217684995L;
    
    private SandboxFactory sandboxFactory;
    
    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        sandboxFactory = (SandboxFactory)factory.getBean("sandboxFactory");
    }
    
    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
        // TODO: Allow submit parameters to be passed into this action handler
        //       rather than pulling directly from execution context
        
        // retrieve submitted package
        NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().getVariable("bpm_package")).getNodeRef();
        Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);
        
        String workflowName = executionContext.getProcessDefinition().getName();
        
        // optimization: direct submits no longer virtualize the workflow sandbox
        final boolean isSubmitDirectWorkflowSandbox = ((workflowName != null) && (workflowName.equals(WCMUtil.WORKFLOW_SUBMITDIRECT_NAME)));
        
        // Now delete the stores in the WCM workflow sandbox
        final String avmPath = pkgPath.getSecond();
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                sandboxFactory.deleteSandbox(WCMUtil.getSandboxStoreId(avmPath), isSubmitDirectWorkflowSandbox);
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}
