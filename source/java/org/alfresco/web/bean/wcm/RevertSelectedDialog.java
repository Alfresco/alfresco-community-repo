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
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMUndoSandboxListAction;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.util.VirtServerUtils;

/**
 * Revert (undo) the selected files in the current user sandbox.
 * 
 * @author Kevin Roast
 */
public class RevertSelectedDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -8432152646736206685L;

   private static final String MSG_REVERTSELECTED_SUCCESS = "revertselected_success";
   
   protected AVMBrowseBean avmBrowseBean;
   transient private ActionService actionService;

   // The virtualization server might need to be notified 
   // because one or more of the files reverted could alter 
   // the behavior the virtual webapp in the target of the submit.

   private String virtUpdatePath;     


   
   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param actionService The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }
   
   protected ActionService getActionService()
   {
      if (this.actionService == null)
      {
         this.actionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getActionService();
      }
      return actionService;
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      List<AVMNodeDescriptor> selected = this.avmBrowseBean.getSelectedSandboxItems();
      List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>();
      
      List<WorkflowTask> tasks = null;
      for (AVMNodeDescriptor node : selected)
      {
         if (tasks == null)
         {
            tasks = AVMWorkflowUtil.getAssociatedTasksForSandbox(AVMUtil.getStoreName(node.getPath()));
         }
         if (AVMWorkflowUtil.getAssociatedTasksForNode(node, tasks).size() == 0)
         {
            String revertPath = node.getPath();
            versionPaths.add(new Pair<Integer, String>(-1, revertPath));
            
            if ( (this.virtUpdatePath == null) &&
                  VirtServerUtils.requiresUpdateNotification(revertPath) )
            {
                this.virtUpdatePath = revertPath;
            }
         }
      }
      
      Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
      args.put(AVMUndoSandboxListAction.PARAM_NODE_LIST, (Serializable)versionPaths);
      Action action = this.getActionService().createAction(AVMUndoSandboxListAction.NAME, args);
      this.getActionService().executeAction(action, null);    // dummy action ref, list passed as action arg
      
      String msg = MessageFormat.format(Application.getMessage(
                  context, MSG_REVERTSELECTED_SUCCESS), this.avmBrowseBean.getUsername());
      FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
      context.addMessage(AVMBrowseBean.FORM_ID + ':' + AVMBrowseBean.COMPONENT_SANDBOXESPANEL, facesMsg);
      
      return outcome;
   }

   /**
    * Handle notification to the virtualization server 
    * (this needs to occur after the sandbox is updated).
    */
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {     
      // Force the update because we've already determined
      // that update_path requires virt server notification.
      if (this.virtUpdatePath != null)
      {
         AVMUtil.updateVServerWebapp(this.virtUpdatePath, true);
      }
      return outcome;
   }


   
   /**
    * @return the confirmation to display to the user
    */
   public String getConfirmMessage()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "revert_selected_confirm");
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
    */
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
}
