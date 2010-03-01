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

import org.alfresco.util.BaseSpringTest;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.db.GraphSession;
import org.jbpm.db.TaskMgmtSession;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;


/**
 * Unit Test for reproducing constraint violation during JBPM process deletion
 *
 * http://jira.jboss.com/jira/browse/JBPM-757
 *  
 * @author davidc
 */

public class JBPMDeleteProcessTest extends BaseSpringTest {

    JbpmConfiguration jbpmConfiguration; 
    long processId = -1L;
    String currentTokenPath;

    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        jbpmConfiguration = (JbpmConfiguration) getApplicationContext().getBean("jbpm_configuration");
    }

    public void testDelete() {
      deployProcessDefinition();

      startProcess();
      step2TaskEnd();
      deleteProcess();
    }

    public void deployProcessDefinition() {
      ProcessDefinition processDefinition = ProcessDefinition.parseXmlString
      (
        "<process-definition name='deletetest'>" +
        "  <start-state name='start'> " +  
        "    <task name='startTask'> " +
        "      <controller> " +
        "        <variable name='var1' access='write'/> " +
        "      </controller> " +
        "    </task> " +
        "   <transition name='' to='step2'/> " +
        "  </start-state> " +
        "  <task-node name='step2'> " +
        "    <task name='step2Task'/> " +
        "    <transition name='' to='step3'/> " +
        "  </task-node>" +
        "  <task-node name='step3'> " +
        "    <task name='step3Task'/> " +
        "    <transition name='' to='end'/> " +
        "  </task-node> " +      
        "  <end-state name='end' />" +
        "</process-definition>"
      );
      
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        jbpmContext.deployProcessDefinition(processDefinition);
      } finally {
        jbpmContext.close();
      }
    }

    public void startProcess() {

      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {

        GraphSession graphSession = jbpmContext.getGraphSession();
        
        ProcessDefinition processDefinition = graphSession.findLatestProcessDefinition("deletetest");
        ProcessInstance processInstance = new ProcessInstance(processDefinition);
        processId = processInstance.getId();

        TaskInstance taskInstance = processInstance.getTaskMgmtInstance().createStartTaskInstance();
        taskInstance.setVariableLocally("var1", "var1Value");
        taskInstance.end();
        Token token = taskInstance.getToken();
        currentTokenPath = token.getFullName();
        
        jbpmContext.save(processInstance);
      } finally {
        jbpmContext.close();
      }
    }

    public void step2TaskEnd() {

        JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
        try {

          GraphSession graphSession = jbpmContext.getGraphSession();
          ProcessInstance processInstance = graphSession.loadProcessInstance(processId);
          Token token = processInstance.findToken(currentTokenPath);
          TaskMgmtSession taskSession = jbpmContext.getTaskMgmtSession();
          List tasks = taskSession.findTaskInstancesByToken(token.getId());
          TaskInstance taskInstance = (TaskInstance)tasks.get(0);
          
          //
          // Uncomment the following line to force constraint violation
          //
          taskInstance.setVariableLocally("var1", "var1TaskValue");
          
          taskInstance.setVariableLocally("var2", "var2UpdatedValue");
          taskInstance.end();
          token = taskInstance.getToken();
          currentTokenPath = token.getFullName();
          
          jbpmContext.save(processInstance);
        } finally {
          jbpmContext.close();
        }
      }

    
    public void deleteProcess()
    {
        JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
        try {

          GraphSession graphSession = jbpmContext.getGraphSession();
          ProcessInstance processInstance = graphSession.loadProcessInstance(processId);
          graphSession.deleteProcessInstance(processInstance, true, true);
        } finally {
          jbpmContext.close();
        }
    }
    
}