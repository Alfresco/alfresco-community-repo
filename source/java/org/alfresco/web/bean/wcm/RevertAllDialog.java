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
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

/**
 * Revert (undo) all files in the current user sandbox.
 * 
 * @author Kevin Roast
 */
public class RevertAllDialog extends BaseDialogBean
{
   private static final String MSG_REVERTALL_SUCCESS = "revertall_success";
   
   protected AVMBrowseBean avmBrowseBean;
   protected AVMSyncService avmSyncService;
   protected ActionService actionService;
   protected NameMatcher nameMatcher;
   
   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmSyncService   The AVMSyncService to set.
    */
   public void setAvmSyncService(AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }
   
   /**
    * @param actionService The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }
   
   /**
    * @param nameMatcher The nameMatcher to set.
    */
   public void setNameMatcher(NameMatcher nameMatcher)
   {
      this.nameMatcher = nameMatcher;
   }
   
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      String webapp = this.avmBrowseBean.getWebapp();
      String userStore = AVMConstants.buildAVMStoreWebappPath(this.avmBrowseBean.getSandbox(), webapp);
      String stagingStore = AVMConstants.buildAVMStoreWebappPath(this.avmBrowseBean.getStagingStore(), webapp);
      
      // calcluate the list of differences between the user store and the staging area
      List<AVMDifference> diffs = this.avmSyncService.compare(
            -1, userStore, -1, stagingStore, this.nameMatcher);
      List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>();
      for (AVMDifference diff : diffs)
      {
         versionPaths.add(new Pair<Integer, String>(-1, diff.getSourcePath()));
      }
      Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
      args.put(AVMUndoSandboxListAction.PARAM_NODE_LIST, (Serializable)versionPaths);
      Action action = this.actionService.createAction(AVMUndoSandboxListAction.NAME, args);
      this.actionService.executeAction(action, null); // dummy action ref
      
      String msg = MessageFormat.format(Application.getMessage(
            context, MSG_REVERTALL_SUCCESS), this.avmBrowseBean.getUsername());
      FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
      context.addMessage(AVMBrowseBean.FORM_ID + ':' + AVMBrowseBean.COMPONENT_SANDBOXESPANEL, facesMsg);
      
      return outcome;
   }
   
   /**
    * @return the confirmation to display to the user
    */
   public String getConfirmMessage()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "revert_all_confirm");
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
