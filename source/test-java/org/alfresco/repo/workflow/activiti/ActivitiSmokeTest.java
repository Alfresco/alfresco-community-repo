
package org.alfresco.repo.workflow.activiti;

import java.io.IOException;

import junit.framework.TestCase;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
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
        String properties = "org/alfresco/repo/workflow/activiti/activiti.cfg.xml";
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
