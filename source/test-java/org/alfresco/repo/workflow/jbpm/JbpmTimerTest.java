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

package org.alfresco.repo.workflow.jbpm;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowAdminServiceImpl;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowTestHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.hibernate.HibernateException;
import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class JbpmTimerTest extends TestCase
{
    private static final String simpleDefLocation = "jbpmresources/test_simpleTimer.xml";
    private static final String exceptionDefLocation = "jbpmresources/test_timerException.xml";
    
    private WorkflowService workflowService;
    private WorkflowTestHelper testHelper;
    private String defId;
    
    public void testTimerException() throws Exception
    {
        defId = deployDefinition(exceptionDefLocation);
        
        NodeRef pckg = workflowService.createPackage(null);
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_PACKAGE, pckg);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, AuthenticationUtil.getAdminUserName());
        
        WorkflowPath path = workflowService.startWorkflow(defId, params);
        String instanceId = path.getInstance().getId();
        WorkflowTask start = workflowService.getStartTask(instanceId);
        workflowService.endTask(start.getId(), null);
        Thread.sleep(30000);
        System.out.println("Done!");
    }

    public void testTimerIsReassignable() throws Exception
    {
        defId = deployDefinition(simpleDefLocation);
        
        NodeRef pckg = workflowService.createPackage(null);
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_PACKAGE, pckg);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, AuthenticationUtil.getAdminUserName());
        
        WorkflowPath path = workflowService.startWorkflow(defId, params);
        String instanceId = path.getInstance().getId();
        WorkflowTask start = workflowService.getStartTask(instanceId);
        workflowService.endTask(start.getId(), null);
        
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        WorkflowTask task = tasks.get(0);
        assertTrue(workflowService.isTaskReassignable(task, AuthenticationUtil.getAdminUserName()));
        
        // Wait for timer to end task
        Thread.sleep(30000);
        assertFalse(workflowService.isTaskReassignable(task, AuthenticationUtil.getAdminUserName()));
    }
    
    @Override
    protected void setUp() throws Exception
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        ServiceRegistry services = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        workflowService = services.getWorkflowService();
        WorkflowAdminServiceImpl adminService = (WorkflowAdminServiceImpl) ctx.getBean(WorkflowAdminServiceImpl.NAME);

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        testHelper = new WorkflowTestHelper(adminService, JBPMEngine.ENGINE_ID, false);
        testHelper.setVisible(true);
    }

    /**
     * @return
     */
    private String deployDefinition(String location)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream(exceptionDefLocation);
        input = classLoader.getResourceAsStream(location);
        WorkflowDeployment deployment 
            = workflowService.deployDefinition(JBPMEngine.ENGINE_ID, input, MimetypeMap.MIMETYPE_XML);
        return deployment.getDefinition().getId();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        workflowService.undeployDefinition(defId);
        testHelper.tearDown();
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public static void throwException() throws HibernateException
    {
        throw new HibernateException("My Timer Exception");
    }
}
