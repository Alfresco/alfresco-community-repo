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
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;

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
   transient private SandboxService sbService;
   
   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   public void setSandboxService(SandboxService sbService)
   {
      this.sbService = sbService;
   }

   protected SandboxService getSandboxService()
   {
      if (this.sbService == null)
      {
         this.sbService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSandboxService();
      }
      return this.sbService;
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      String userSandboxId = this.avmBrowseBean.getSandbox();
      
      List<AVMNodeDescriptor> selected = this.avmBrowseBean.getSelectedSandboxItems();
      if (selected != null && selected.size() > 0)
      {
          List<String> relativePaths = new ArrayList<String>(selected.size());
          for (AVMNodeDescriptor node : selected)
          {
              relativePaths.add(AVMUtil.getStoreRelativePath(node.getPath()));
          }
          
          getSandboxService().revertList(userSandboxId, relativePaths);
          
          String msg = MessageFormat.format(Application.getMessage(
                      context, MSG_REVERTSELECTED_SUCCESS), this.avmBrowseBean.getUsername());
          FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
          context.addMessage(AVMBrowseBean.FORM_ID + ':' + AVMBrowseBean.COMPONENT_SANDBOXESPANEL, facesMsg);
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
