/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import org.alfresco.repo.avm.actions.AVMUndoSandboxListAction;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.alfresco.util.VirtServerUtils;
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
      String userStore = AVMConstants.buildStoreWebappPath(this.avmBrowseBean.getSandbox(), webapp);
      String stagingStore = AVMConstants.buildStoreWebappPath(this.avmBrowseBean.getStagingStore(), webapp);
      
      // calcluate the list of differences between the user store and the staging area
      List<AVMDifference> diffs = this.avmSyncService.compare(
            -1, userStore, -1, stagingStore, this.nameMatcher);

      List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>();

      for (AVMDifference diff : diffs)
      {
         String revertPath =  diff.getSourcePath();
         versionPaths.add(new Pair<Integer, String>(-1, revertPath) );

         if ( (this.virtUpdatePath == null) &&
               VirtServerUtils.requiresUpdateNotification(revertPath)
            )
         {
             this.virtUpdatePath = revertPath;
         }
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
