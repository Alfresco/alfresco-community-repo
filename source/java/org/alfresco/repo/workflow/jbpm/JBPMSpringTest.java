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

import java.util.List;

import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.BaseSpringTest;
import org.jbpm.JbpmContext;
import org.jbpm.db.GraphSession;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.springmodules.workflow.jbpm31.JbpmCallback;
import org.springmodules.workflow.jbpm31.JbpmTemplate;


/**
 * Test Usage of jBPM within Alfresco Spring Context
 * 
 * @author davidc
 */
public class JBPMSpringTest extends BaseSpringTest
{
    JbpmTemplate jbpmTemplate;
    DescriptorService descriptorService;

        
    //@Override
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        jbpmTemplate = (JbpmTemplate)applicationContext.getBean("jbpm_template");
        descriptorService = (DescriptorService)applicationContext.getBean("DescriptorService");
    }
    
    public void testHelloWorld()
        throws Exception
    {
        deployProcessDefinition();
        processInstanceIsCreatedWhenUserSubmitsWebappForm();
        theProcessInstanceContinuesWhenAnAsyncMessageIsReceived();
        undeployProcessDefinition();
    }
    
    private void deployProcessDefinition()
    {
        // This test shows a process definition and one execution 
        // of the process definition.  The process definition has 
        // 3 nodes: an unnamed start-state, a state 's' and an 
        // end-state named 'end'.
        final ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
          "<process-definition name='hello world'>" +
          "  <start-state name='start'>" +
          "    <transition to='s' />" +
          "  </start-state>" +
          "  <node name='s'>" +
          "    <action class='org.alfresco.repo.workflow.jbpm.JBPMTestSpringActionHandler' config-type='bean'>" +
          "       <value>a test value</value>" +
          "    </action>" +
          "    <transition to='end' />" +
          "  </node>" +
          "  <end-state name='end' />" +
          "</process-definition>"
        );

        
        jbpmTemplate.execute(new JbpmCallback()
        {
            public Object doInJbpm(JbpmContext context)
            {
                context.deployProcessDefinition(processDefinition);
                return null;
            }
        });
    }

    
    private void undeployProcessDefinition()
    {
        jbpmTemplate.execute(new JbpmCallback()
        {
            public Object doInJbpm(JbpmContext context)
            {
                GraphSession graphSession = context.getGraphSession();
                ProcessDefinition processDefinition = graphSession.findLatestProcessDefinition("hello world");
                graphSession.deleteProcessDefinition(processDefinition.getId());
                return null;
            }
        });
    }
    
    private void processInstanceIsCreatedWhenUserSubmitsWebappForm()
    {
        jbpmTemplate.execute(new JbpmCallback()
        {
            public Object doInJbpm(JbpmContext context)
            {
                GraphSession graphSession = context.getGraphSession();
                ProcessDefinition processDefinition = graphSession.findLatestProcessDefinition("hello world");
    
                // With the processDefinition that we retrieved from the database, we 
                // can create an execution of the process definition just like in the 
                // hello world example (which was without persistence).
                ProcessInstance processInstance = new ProcessInstance(processDefinition);
                
                Token token = processInstance.getRootToken(); 
                assertEquals("start", token.getNode().getName());
                // Let's start the process execution
                token.signal();
                // Now the process is in the state 's'.
                assertEquals("s", token.getNode().getName());
                // Spring based action has been called, check the result by looking at the 
                // process variable set by the action
                String result = "Repo: " + descriptorService.getServerDescriptor().getVersion() + ", Value: a test value, Node: s, Token: /";
                assertEquals(result, processInstance.getContextInstance().getVariable("jbpm.test.action.result"));
                
                context.save(processInstance);
                return null;
            }
        });
    }

    private void theProcessInstanceContinuesWhenAnAsyncMessageIsReceived()
    {
      jbpmTemplate.execute(new JbpmCallback()
      {
          public Object doInJbpm(JbpmContext context)
          {
              GraphSession graphSession = context.getGraphSession();

              // First, we need to get the process instance back out of the database.
              // There are several options to know what process instance we are dealing 
              // with here.  The easiest in this simple test case is just to look for 
              // the full list of process instances.  That should give us only one 
              // result.  So let's look up the process definition.
              ProcessDefinition processDefinition = graphSession.findLatestProcessDefinition("hello world");

              // Now, we search for all process instances of this process definition.
              List<?> processInstances = graphSession.findProcessInstances(processDefinition.getId());
              
              // Because we know that in the context of this unit test, there is 
              // only one execution.  In real life, the processInstanceId can be 
              // extracted from the content of the message that arrived or from 
              // the user making a choice.
              ProcessInstance processInstance = (ProcessInstance) processInstances.get(0);
              
              // Now we can continue the execution.  Note that the processInstance
              // delegates signals to the main path of execution (=the root token).
              processInstance.signal();

              // After this signal, we know the process execution should have 
              // arrived in the end-state.
              assertTrue(processInstance.hasEnded());
              
              // Now we can update the state of the execution in the database
              context.save(processInstance);
              return null;
          }
      });
    }    

}

