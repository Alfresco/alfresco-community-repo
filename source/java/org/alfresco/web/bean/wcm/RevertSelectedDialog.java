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
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.util.VirtServerUtils;

/**
 * Revert (undo) the selected files in the current user sandbox.
 * 
 * @author Kevin Roast
 */
public class RevertSelectedDialog extends BaseDialogBean
{
   private static final String MSG_REVERTSELECTED_SUCCESS = "revertselected_success";
   
   protected AVMBrowseBean avmBrowseBean;
   protected ActionService actionService;

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
   
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      List<AVMNodeDescriptor> selected = this.avmBrowseBean.getSelectedSandboxItems();
      List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>();


      for (AVMNodeDescriptor node : selected)
      {
         String revertPath = node.getPath();
         versionPaths.add(new Pair<Integer, String>(-1, revertPath ));

         if ( (this.virtUpdatePath == null) &&
               VirtServerUtils.requiresUpdateNotification(revertPath)
            )
         {
             this.virtUpdatePath = revertPath;
         }
      }

      Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
      args.put(AVMUndoSandboxListAction.PARAM_NODE_LIST, (Serializable)versionPaths);
      for (AVMNodeDescriptor node : selected)
      {
         Action action = this.actionService.createAction(AVMUndoSandboxListAction.NAME, args);
         this.actionService.executeAction(action, AVMNodeConverter.ToNodeRef(-1, node.getPath()));
      }
      
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
         AVMConstants.updateVServerWebapp(this.virtUpdatePath, true);
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
