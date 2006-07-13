/*
 * Copyright (C) 2005 Alfresco, Inc.
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

        
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        jbpmTemplate = (JbpmTemplate)applicationContext.getBean("jbpm.template");
        descriptorService = (DescriptorService)applicationContext.getBean("DescriptorService");
    }
    
        
    public void testHelloWorld()
        throws Exception
    {
        // Between the 3 method calls below, all data is passed via the 
        // database.  Here, in this unit test, these 3 methods are executed
        // right after each other because we want to test a complete process
        // scenario.  But in reality, these methods represent different 
        // requests to a server.
        
        // Since we start with a clean, empty in-memory database, we have to 
        // deploy the process first.  In reality, this is done once by the 
        // process developer.
        deployProcessDefinition();

        // Suppose we want to start a process instance (=process execution)
        // when a user submits a form in a web application...
        processInstanceIsCreatedWhenUserSubmitsWebappForm();

        // Then, later, upon the arrival of an asynchronous message the 
        // execution must continue.
        theProcessInstanceContinuesWhenAnAsyncMessageIsReceived();
    }

    public void testStep0()
        throws Exception
    {
        deployProcessDefinition();
        setComplete();
    }

    public void testStep1()
        throws Exception
    {
        processInstanceIsCreatedWhenUserSubmitsWebappForm();
        setComplete();
    }
    
    public void testStep2()
        throws Exception
    {
        theProcessInstanceContinuesWhenAnAsyncMessageIsReceived();
        setComplete();
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
              List processInstances = graphSession.findProcessInstances(processDefinition.getId());
              
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

