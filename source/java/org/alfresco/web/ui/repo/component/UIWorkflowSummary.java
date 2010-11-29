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
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

/**
 * JSF component that displays summary information about a workflow.
 * 
 * @author gavinc
 */
public class UIWorkflowSummary extends SelfRenderingComponent
{
   protected WorkflowInstance value = null;
   
   // ------------------------------------------------------------------------------
   // Component Impl 

   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.WorkflowSummary";
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
      Object values[] = new Object[8];
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
         
         // output surrounding table and style if necessary
         out.write("<table");
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
         
         // output the title and description
         out.write("<tr><td>");
         out.write(bundle.getString("title"));
         out.write(":</td><td>");
         out.write(wi.definition.title);
         if (wi.definition.description != null && wi.definition.description.length() > 0)
         {
            out.write("&nbsp;(");
            out.write(Utils.encode(wi.definition.description));
            out.write(")");
         }
         out.write("</td></tr><tr><td>");
         out.write(bundle.getString("initiated_by"));
         out.write(":</td><td>");
         NodeService nodeService = getNodeService(context);
         if (wi.initiator != null)
         {
            if (nodeService.exists(wi.initiator))
            {
               out.write(Utils.encode(User.getFullName(Repository.getServiceRegistry(
                        context).getNodeService(), wi.initiator)));
            }
            else
            {
               out.write("&lt;");
               out.write(bundle.getString("unknown"));
               out.write("&gt;");
            }
         }
         out.write("</td></tr><tr><td>");
         out.write(bundle.getString("started_on"));
         out.write(":</td><td>");
         if (wi.startDate != null)
         {
            out.write(Utils.getDateFormat(context).format(wi.startDate));
         }
         out.write("</td></tr><tr><td>");
         out.write(bundle.getString("completed_on"));
         out.write(":</td><td>");
         if (wi.endDate != null)
         {
            out.write(Utils.getDateFormat(context).format(wi.endDate));
         }
         else
         {
            out.write("&lt;");
            out.write(bundle.getString("in_progress"));
            out.write("&gt;");
         }
         out.write("</td></tr></table>");
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
   
   private NodeService getNodeService(FacesContext fc)
   {
      return Repository.getServiceRegistry(fc).getNodeService();
   }
}
