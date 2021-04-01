/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.workflow.activiti;

import java.io.IOException;

import junit.framework.TestCase;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.alfresco.util.testing.category.NeverRunsTests;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
@Category(NeverRunsTests.class)
public class ActivitiSmokeTest extends TestCase
{
    public void testDeploy() throws Exception
    {
        ProcessEngine engine = buildProcessEngine();

        RepositoryService repoService = engine.getRepositoryService();

        Deployment deployment = deployDefinition(repoService);

        assertNotNull(deployment);

        RuntimeService runtimeService = engine.getRuntimeService();
        try
        {
            ProcessInstance instance = runtimeService.startProcessInstanceByKey("testTask");
            assertNotNull(instance);
            
            String instanceId = instance.getId();
            ProcessInstance instanceInDb = findProcessInstance(runtimeService, instanceId);
            assertNotNull(instanceInDb);
            runtimeService.deleteProcessInstance(instanceId, "");
        }
        finally
        {
            
//            List<Deployment> deployments = repoService.createDeploymentQuery().list();
//            for (Deployment deployment2 : deployments)
//            {
//                repoService.deleteDeployment(deployment2.getId());
//            }
            
            repoService.deleteDeployment(deployment.getId());
        }
    }

    private Deployment deployDefinition(RepositoryService repoService) throws IOException
    {
        ClassPathResource resource = new ClassPathResource("org/alfresco/repo/workflow/activiti/testTransaction.bpmn20.xml");
        Deployment deployment = repoService.createDeployment()
        .addInputStream(resource.getFilename(), resource.getInputStream())
        .deploy();
        return deployment;
    }

    private ProcessEngine buildProcessEngine()
    {
        String properties = "activiti/activiti.cfg.xml";
        ProcessEngine engine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource(properties).buildProcessEngine();
        return engine;
    }

    private ProcessInstance findProcessInstance(RuntimeService runtimeService, String instanceId)
    {
        ProcessInstance instanceInDb = runtimeService.createProcessInstanceQuery()
        .processInstanceId(instanceId)
        .singleResult();
        return instanceInDb;
    }
}
