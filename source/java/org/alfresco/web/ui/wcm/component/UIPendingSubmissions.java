/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.AVMWorkflowUtil;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.ConstantMethodBinding;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.UIActions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to display the list of pending submissions for a web project.
 * 
 * @author Gavin Cornwell
 */
public class UIPendingSubmissions extends SelfRenderingComponent
{
   private static final String ACT_DETAILS = "pending_details";
   private static final String ACT_PREVIEW = "pending_preview";
   private static final String ACT_DIFF = "pending_diff";
   private static final String ACT_PROMOTE = "pending_promote";
   private static final String ACT_ABORT = "pending_abort";
   
   private static final String REQUEST_TASKID = "_taskid";
   private static final String REQUEST_PREVIEW_REF = "_prevhref";
   private static final String REQUEST_WORKFLOWID = "_workflowid";

   private static Log logger = LogFactory.getLog(UIPendingSubmissions.class);
   
   private static final String MSG_LABEL = "name";
   private static final String MSG_DESCRIPTION = "description";
   private static final String MSG_DATE = "date";
   private static final String MSG_USERNAME = "username";
   private static final String MSG_LAUNCH_DATE = "launch_date";
   private static final String MSG_ACTIONS = "actions";
   private static final String MSG_NO_PENDING = "no_pending_submissions";
   
   /** sandbox to show pending submissions for */
   private String value;
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.PendingSubmissions";
   }
   
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = (String)values[1];
   }
   
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.value;
      return values;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
    */
   public void encodeChildren(FacesContext context) throws IOException
   {
      // the child components are rendered explicitly during the encodeBegin()
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      ResourceBundle bundle = Application.getBundle(context);
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         String sandbox = getValue();
         if (sandbox == null)
         {
            throw new IllegalArgumentException("Sandbox must be specified.");
         }
         
         // get the noderef representing the web project
         // TODO: pass this in via a property from the JSP page
         AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(
                  context, "AVMBrowseBean");
         NodeRef webProject = avmBrowseBean.getWebsite().getNodeRef();
         
         // get the list of pending tasks for this project
         WorkflowTaskQuery query = new WorkflowTaskQuery();
         query.setTaskName(QName.createQName(NamespaceService.WCMWF_MODEL_1_0_URI, 
                  "submitpendingTask"));
         query.setTaskState(WorkflowTaskState.IN_PROGRESS);
         Map<QName, Object> processProps = new HashMap<QName, Object>();
         processProps.put(AVMWorkflowUtil.ASSOC_WEBPROJECT, webProject);
         query.setProcessCustomProps(processProps);
         query.setOrderBy(new WorkflowTaskQuery.OrderBy[] { 
                  WorkflowTaskQuery.OrderBy.TaskDue_Desc, 
                  WorkflowTaskQuery.OrderBy.TaskActor_Asc });
         List<WorkflowTask> pendingTasks = Repository.getServiceRegistry(context).
                  getWorkflowService().queryTasks(query); 
         
         if (pendingTasks.size() == 0)
         {
            out.write(bundle.getString(MSG_NO_PENDING));
         }
         else
         {
            // TODO: apply tag style - removed hardcoded
            out.write("<table class='pendingSubmissionsList' cellspacing=2 cellpadding=1 border=0 width=100%>");
            // header row
            out.write("<tr align=left><th>");
            out.write(bundle.getString(MSG_LABEL));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_DESCRIPTION));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_DATE));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_USERNAME));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_LAUNCH_DATE));
            out.write("</th><th>");
            out.write(bundle.getString(MSG_ACTIONS));
            out.write("</th></tr>");
            
            // output the pending submissions and their actions
            for (WorkflowTask task : pendingTasks)
            {
               String desc = (String)task.properties.get(WorkflowModel.PROP_DESCRIPTION);
               String label = (String)task.properties.get(AVMWorkflowUtil.PROP_LABEL);
               String started = Utils.getDateTimeFormat(context).format(task.path.instance.startDate);
               String username = (String)Repository.getServiceRegistry(context).getNodeService().
                     getProperty(task.path.instance.initiator, ContentModel.PROP_USERNAME);
               Date launchDate = (Date)task.properties.get(AVMWorkflowUtil.PROP_LAUNCH_DATE);
               String launch = Utils.getDateTimeFormat(context).format(launchDate);
                  
               out.write("<tr><td>");
               out.write(desc == null ? "&nbsp;" : desc);
               out.write("</td><td>");
               out.write(label);
               out.write("</td><td>");
               out.write(started);
               out.write("</td><td>");
               out.write(username);
               out.write("</td><td>");
               out.write(launch);
               out.write("</td><td><nobr>");
               
               // details action
               // TODO
               
               // preview action
               NodeRef pkg = task.path.instance.workflowPackage;
               Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);
               String workflowStore = AVMUtil.getStoreName(pkgPath.getSecond());
               String workflowPreviewUrl = AVMUtil.buildStoreUrl(workflowStore);
               Map requestMap = context.getExternalContext().getRequestMap();
               requestMap.put(REQUEST_PREVIEW_REF, workflowPreviewUrl);
               
               UIActionLink preview = findAction(ACT_PREVIEW, sandbox);
               if (preview == null)
               {
                  preview = createAction(context, sandbox, ACT_PREVIEW, 
                           "/images/icons/preview_website.gif", null, null, 
                           "#{" + REQUEST_PREVIEW_REF + "}", null);
               }

               Utils.encodeRecursive(context, preview);
               requestMap.remove(REQUEST_PREVIEW_REF);
               out.write("&nbsp;&nbsp;");
               
               // visual diff action (only if there is a live deployed snapshot)
               // TODO
               
               // promote action
               UIActionLink promote = findAction(ACT_PROMOTE, sandbox);
               if (promote == null)
               {
                  Map<String, String> params = new HashMap<String, String>(1);
                  params.put("taskId", "#{" + REQUEST_TASKID + "}");
                  promote = createAction(context, sandbox, ACT_PROMOTE,
                           "/images/icons/promote_submission.gif", 
                           "#{AVMBrowseBean.promotePendingSubmission}",
                           null, null, params);
               }
               
               requestMap.put(REQUEST_TASKID, task.id);
               Utils.encodeRecursive(context, promote);
               requestMap.remove(REQUEST_TASKID);
               out.write("&nbsp;&nbsp;");
               
               // abort action
               UIActionLink abort = findAction(ACT_ABORT, sandbox);
               if (abort == null)
               {
                  Map<String, String> params = new HashMap<String, String>(1);
                  params.put("workflowInstanceId", "#{" + REQUEST_WORKFLOWID + "}");
                  abort = createAction(context, sandbox, ACT_ABORT,
                           "/images/icons/abort_submission.gif", 
                           "#{AVMBrowseBean.cancelPendingSubmission}",
                           null, null, params);
               }
               
               requestMap.put(REQUEST_WORKFLOWID, task.path.instance.id);
               Utils.encodeRecursive(context, abort);
               requestMap.remove(REQUEST_WORKFLOWID);
               out.write("&nbsp;&nbsp;");
               
               out.write("</nobr></td></tr>");
            }
            
            out.write("</table>");
         }
         
         tx.commit();
      }
      catch (Throwable err)
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         throw new RuntimeException(err);
      }
   }
   
   /**
    * Locate a child UIActionLink component by name.
    * 
    * @param name       Of the action component to find
    * @param sandbox    Sandbox the action component is tied to
    * 
    * @return UIActionLink component if found, else null if not created yet
    */
   @SuppressWarnings("unchecked")
   private UIActionLink findAction(String name, String sandbox)
   {
      UIActionLink action = null;
      String actionId = name + '_' + sandbox;
      if (logger.isDebugEnabled())
         logger.debug("Finding action Id: " + actionId);
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            if (logger.isDebugEnabled())
               logger.debug("...found action Id: " + actionId);
            break;
         }
      }
      return action;
   }
   
   /**
    * Create a UIActionLink child component.
    * 
    * @param fc               FacesContext
    * @param sandbox          Root sandbox name
    * @param name             Action name - will be used for I18N message lookup
    * @param icon             Icon to display for the actio n
    * @param actionListener   Actionlistener for the action
    * @param outcome          Navigation outcome for the action
    * @param url              HREF URL for the action
    * @param params           Parameters name/values for the action listener args
    * 
    * @return UIActionLink child component
    */
   @SuppressWarnings("unchecked")
   private UIActionLink createAction(FacesContext fc, String sandbox, String name, String icon,
         String actionListener, String outcome, String url, Map<String, String> params)
   {
      javax.faces.application.Application facesApp = fc.getApplication();
      UIActionLink control = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
      
      String id = name + '_' + sandbox;
      
      if (logger.isDebugEnabled())
         logger.debug("...creating action Id: " + id);
      
      control.setRendererType(UIActions.RENDERER_ACTIONLINK);
      control.setId(id);
      control.setValue(Application.getMessage(fc, name));
      control.setShowLink(icon != null ? false : true);
      control.setImage(icon);
      
      if (actionListener != null)
      {
         control.setActionListener(facesApp.createMethodBinding(
               actionListener, UIActions.ACTION_CLASS_ARGS));
         
         // add sandbox as the default action listener parameter
         if (params == null)
         {
            UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
            param.setId(id + "_1");
            param.setName("sandbox");
            param.setValue(sandbox);
            control.getChildren().add(param);
         }
         else
         {
            // if a specific set of parameters are supplied, then add them instead
            int idIndex = 1;
            for (String key : params.keySet())
            {
               UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
               param.setId(id + '_' + Integer.toString(idIndex++));
               param.setName(key);
               String value = params.get(key);
               if (value.startsWith("#{") == true)
               {
                  ValueBinding vb = facesApp.createValueBinding(value);
                  param.setValueBinding("value", vb);
               }
               else
               {
                  param.setValue(params.get(key));
               }
               control.getChildren().add(param);
            }
         }
      }
      if (outcome != null)
      {
         control.setAction(new ConstantMethodBinding(outcome));
      }
      if (url != null)
      {
         if (url.startsWith("#{"))
         {
            ValueBinding vb = facesApp.createValueBinding(url);
            control.setValueBinding("href", vb);
         }
         else
         {
            control.setHref(url);
         }
         
         control.setTarget("new");
      }
      
      this.getChildren().add(control);
      
      return control;
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Returns the Sandbox to show the snapshots for
    *
    * @return The Sandbox name
    */
   public String getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.value = (String)vb.getValue(getFacesContext());
      }
      
      return this.value;
   }
   
   /**
    * Sets the Sandbox to show the snapshots for
    *
    * @param value   The Sandbox name
    */
   public void setValue(String value)
   {
      this.value = value;
   }
}
