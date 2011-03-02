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
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSF component that displays historic information about a workflow.
 * 
 * @author gavinc
 */
public class UIWorkflowHistory extends SelfRenderingComponent
{
   protected WorkflowInstance value = null;
   
   private static final Log logger = LogFactory.getLog(UIWorkflowHistory.class);

   private static final String MSG_DESCRIPTION = "description";
   private static final String MSG_TASK = "task_type";
   private static final String MSG_ID = "id";
   private static final String MSG_CREATED = "created";
   private static final String MSG_ASSIGNEE = "assignee";
   private static final String MSG_COMMENT = "comment";
   private static final String MSG_DATE_COMPLETED = "completed_on";
   private static final String MSG_OUTCOME = "outcome";
   private static final String MSG_NO_HISTORY = "no_workflow_history";
   
   // ------------------------------------------------------------------------------
   // Component Impl 

   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.WorkflowHistory";
   }
   
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = (WorkflowInstance)values[1];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.value;
      return values;
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      WorkflowInstance wi = getValue();
      
      if (wi != null)
      {
         ResponseWriter out = context.getResponseWriter();
         ResourceBundle bundle = Application.getBundle(context);

         if (logger.isDebugEnabled())
            logger.debug("Retrieving workflow history for workflow instance: " + wi);
         
         WorkflowTaskQuery query = new WorkflowTaskQuery();
         query.setActive(null);
         query.setProcessId(wi.id);
         query.setTaskState(WorkflowTaskState.COMPLETED);
         query.setOrderBy(new WorkflowTaskQuery.OrderBy[] { 
                  WorkflowTaskQuery.OrderBy.TaskCreated_Desc, 
                  WorkflowTaskQuery.OrderBy.TaskActor_Asc });
         List<WorkflowTask> tasks = Repository.getServiceRegistry(context).
                  getWorkflowService().queryTasks(query);
         
         if (tasks.size() == 0)
         {
            out.write("<div style='margin-left:18px;margin-top: 6px;'>");
            out.write(bundle.getString(MSG_NO_HISTORY));
            out.write("</div>");
         }
         else
         {
            // output surrounding table and style if necessary
            out.write("<table cellspacing='2' cellpadding='1' border='0'");
            if (this.getAttributes().get("style") != null)
            {
               out.write(" style=\"");
               out.write((String)this.getAttributes().get("style"));
               out.write("\"");
            }
            if (this.getAttributes().get("styleClass") != null)
            {
               out.write(" class=\"");
               out.write((String)this.getAttributes().get("styleClass"));
               out.write("\"");
            }
            out.write(">");
            
            // output a header row
            out.write("<tr align=left><th>");
            out.write(bundle.getString(MSG_DESCRIPTION));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_TASK));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_ID));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_CREATED));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_ASSIGNEE));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_COMMENT));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_DATE_COMPLETED));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_OUTCOME));
            out.write("</th></tr>");
            
            // output a row for each previous completed task
            for (WorkflowTask task : tasks)
            {
               String id = null;
               Serializable idObject = task.properties.get(WorkflowModel.PROP_TASK_ID);
               if (idObject instanceof Long)
               {
                   id = ((Long)idObject).toString();
               }
               else
               {
                   id = idObject.toString();
               }
               
               String desc = (String)task.properties.get(WorkflowModel.PROP_DESCRIPTION);
               Date createdDate = (Date)task.properties.get(ContentModel.PROP_CREATED);
               String owner = (String)task.properties.get(ContentModel.PROP_OWNER);
               String comment = (String)task.properties.get(WorkflowModel.PROP_COMMENT);
               Date completedDate = (Date)task.properties.get(WorkflowModel.PROP_COMPLETION_DATE);
               String transition = (String)task.properties.get(WorkflowModel.PROP_OUTCOME);
               String outcome = "";
               if (transition != null)
               {
                  WorkflowTransition[] transitions = task.definition.node.transitions;
                  for (WorkflowTransition trans : transitions)
                  {
                     if (trans.id.equals(transition))
                     {
                        outcome = trans.title;
                        break;
                     }
                  }
               }
               
               if ((outcome == null || outcome.equals("")) && transition != null)
               {
                  // it's possible in Activiti to have tasks without an outcome set,
                  // in this case default to the transition, if there is one.
               	  outcome = transition;
               }
               
               out.write("<tr><td>");
               out.write(desc == null ? "" : Utils.encode(desc));
               out.write("</td><td>");
               out.write(Utils.encode(task.title));
               out.write("</td><td>");
               out.write(id);
               out.write("</td><td>");
               out.write(Utils.getDateTimeFormat(context).format(createdDate));
               out.write("</td><td>");
               out.write(owner == null ? "" : owner);
               out.write("</td><td>");
               out.write(comment == null ? "" : Utils.encode(comment));
               out.write("</td><td>");
               out.write(Utils.getDateTimeFormat(context).format(completedDate));
               out.write("</td><td>");
               out.write(outcome);
               out.write("</td></tr>");
            }
            
            // output the end of the table
            out.write("</table>");
         }
      }
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
   }
   
   @Override
   public boolean getRendersChildren()
   {
      return false;
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Returns the workflow instance this component is showing information on
    *
    * @return The workflow instance
    */
   public WorkflowInstance getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.value = (WorkflowInstance)vb.getValue(getFacesContext());
      }
      
      return this.value;
   }

   /**
    * Sets the workflow instance to show the summary for
    *
    * @param value The workflow instance
    */
   public void setValue(WorkflowInstance value)
   {
      this.value = value;
   }
}
