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
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

/**
 * JSF component that displays information about the workflows a node is involved in.
 * <p>
 * The node to show workflow information on.
 * 
 * @author gavinc
 */
public class UINodeWorkflowInfo extends SelfRenderingComponent
{
   protected Node value = null;
   
   // ------------------------------------------------------------------------------
   // Component Impl 

   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.NodeWorkflowInfo";
   }
   
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = (Node)values[1];
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
      
      // get the node to display the information for
      Node node = getValue();
      
      if (node != null)
      {
         // get the services we need
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         DictionaryService ddService = Repository.getServiceRegistry(context).getDictionaryService();
         WorkflowService workflowService = Repository.getServiceRegistry(context).getWorkflowService();
         ResponseWriter out = context.getResponseWriter();
         ResourceBundle bundle = Application.getBundle(context);
         
         // render simple workflow info
         renderSimpleWorkflowInfo(context, node, nodeService, ddService, out, bundle);
         
         // render advanced workflow info
         renderAdvancedWorkflowInfo(context, node, nodeService, ddService, workflowService, out, bundle);
      }
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Get the value, this will be a node representing a piece of content or a space
    *
    * @return the value
    */
   public Node getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.value = (Node)vb.getValue(getFacesContext());
      }
      
      return this.value;
   }

   /**
    * Set the value, either a space or content node.
    *
    * @param value     the value
    */
   public void setValue(Node value)
   {
      this.value = value;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Renders the simple workflow details for the given node.
    * 
    * @param context Faces context
    * @param node The node
    * @param nodeService The NodeService instance
    * @param ddService The Data Dictionary instance
    * @param out The response writer
    * @param bundle Message bundle to get strings from
    */
   protected void renderSimpleWorkflowInfo(FacesContext context, Node node, 
         NodeService nodeService, DictionaryService ddService,
         ResponseWriter out, ResourceBundle bundle)
         throws IOException
   {
      boolean isContent = true;
      
      QName type = nodeService.getType(node.getNodeRef());
      if (ddService.isSubClass(type, ContentModel.TYPE_FOLDER))
      {
         isContent = false;
      }
            
      // Render HTML for simple workflow
      if (isContent)
      {
         // TODO: for now we only support advanced workflow on content so only
         // render the simple workflow title if the node is a content node      
         out.write("<div class=\"nodeWorkflowInfoTitle\">");
         out.write(bundle.getString("simple_workflow"));
         out.write("</div>");
      }
      out.write("<div class=\"nodeWorkflowInfoText\">");

      if (node.hasAspect(ApplicationModel.ASPECT_SIMPLE_WORKFLOW))
      {
         // get the simple workflow aspect properties
         Map<String, Object> props = node.getProperties();

         String approveStepName = (String)props.get(
               ApplicationModel.PROP_APPROVE_STEP.toString());
         String rejectStepName = (String)props.get(
               ApplicationModel.PROP_REJECT_STEP.toString());
         
         Boolean approveMove = (Boolean)props.get(
               ApplicationModel.PROP_APPROVE_MOVE.toString());
         Boolean rejectMove = (Boolean)props.get(
               ApplicationModel.PROP_REJECT_MOVE.toString());
         
         NodeRef approveFolder = (NodeRef)props.get(
               ApplicationModel.PROP_APPROVE_FOLDER.toString());
         NodeRef rejectFolder = (NodeRef)props.get(
               ApplicationModel.PROP_REJECT_FOLDER.toString());
         
         String approveFolderName = null;
         String rejectFolderName = null;
         
         // get the approve folder name
         if (approveFolder != null)
         {
            Node approveNode = new Node(approveFolder);
            approveFolderName = approveNode.getName();
         }
         
         // get the reject folder name
         if (rejectFolder != null)
         {
            Node rejectNode = new Node(rejectFolder);
            rejectFolderName = rejectNode.getName();
         }
         
         // calculate the approve action string
         String action = null;
         if (approveMove.booleanValue())
         {
            action = Application.getMessage(FacesContext.getCurrentInstance(), "moved");
         }
         else
         {
            action = Application.getMessage(FacesContext.getCurrentInstance(), "copied");
         }
         
         String actionPattern = null;
         if (isContent)
         {
            actionPattern = Application.getMessage(FacesContext.getCurrentInstance(), "document_action");
         }
         else
         {
            actionPattern = Application.getMessage(FacesContext.getCurrentInstance(), "space_action");
         }
         Object[] params = new Object[] {action, approveFolderName, Utils.encode(approveStepName)};
         out.write(Utils.encode(MessageFormat.format(actionPattern, params)));
         
         // add details of the reject step if there is one
         if (rejectStepName != null && rejectMove != null && rejectFolderName != null)
         {
            if (rejectMove.booleanValue())
            {
               action = Application.getMessage(FacesContext.getCurrentInstance(), "moved");
            }
            else
            {
               action = Application.getMessage(FacesContext.getCurrentInstance(), "copied");
            }
            
            out.write("&nbsp;");
            params = new Object[] {action, rejectFolderName, Utils.encode(rejectStepName)};
            out.write(Utils.encode(MessageFormat.format(actionPattern, params)));
         }
      }
      else
      {
         // work out which no workflow message to show depending on the node type
         if (isContent)
         {
            out.write(bundle.getString("doc_not_in_simple_workflow"));
         }
         else
         {
            out.write(bundle.getString("space_not_in_simple_workflow"));
         }
      }
      out.write("</div>");
   }
   
   /**
    * Renders the advanced workflow details for the given node.
    * 
    * @param context Faces context
    * @param node The node
    * @param nodeService The NodeService instance
    * @param ddService The Data Dictionary instance
    * @param workflowService The WorkflowService instance
    * @param out The response writer
    * @param bundle Message bundle to get strings from
    */
   protected void renderAdvancedWorkflowInfo(FacesContext context, Node node, 
         NodeService nodeService, DictionaryService ddService, WorkflowService workflowService,
         ResponseWriter out, ResourceBundle bundle)
         throws IOException
   {
      boolean isContent = true;
      
      QName type = nodeService.getType(node.getNodeRef());
      if (ddService.isSubClass(type, ContentModel.TYPE_FOLDER))
      {
         isContent = false;
      }
      
      // TODO: for now we only support advanced workflow on content so don't render
      //       anything for other types
      if (isContent)
      {
         // Render HTML for advanved workflow
         out.write("<div class=\"nodeWorkflowInfoTitle\">");
         out.write(bundle.getString("advanced_workflows"));
         out.write("</div><div class=\"nodeWorkflowInfoText\">");
         
         List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(
               node.getNodeRef(), true);
         if (workflows != null && workflows.size() > 0)
         {
            // list out all the workflows the document is part of
            if (isContent)
            {
               out.write(bundle.getString("doc_part_of_advanced_workflows"));
            }
            else
            {
               out.write(bundle.getString("space_part_of_advanced_workflows"));
            }
            out.write(":<br/><ul>");
            for (WorkflowInstance wi : workflows)
            {
               out.write("<li>");
               out.write(wi.definition.title);
               if (wi.description != null && wi.description.length() > 0)
               {
                  out.write("&nbsp;(");
                  out.write(Utils.encode(wi.description));
                  out.write(")");
               }
               out.write(" ");
               if (wi.startDate != null)
               {
                  out.write(bundle.getString("started_on").toLowerCase());
                  out.write("&nbsp;");
                  out.write(Utils.getDateFormat(context).format(wi.startDate));
                  out.write(" ");
               }
               if (wi.initiator != null)
               {
                  out.write(bundle.getString("by"));
                  out.write("&nbsp;");
                  out.write(Utils.encode(User.getFullName(nodeService, wi.initiator)));
                  out.write(".");
               }
               out.write("</li>");
            }
            out.write("</ul>");
         }
         else
         {
            if (isContent)
            {
               out.write(bundle.getString("doc_not_in_advanced_workflow"));
            }
            else
            {
               out.write(bundle.getString("space_not_in_advanced_workflow"));
            }
         }
         out.write("</div>");
      }
   }
}
