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
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

/**
 * Revert snapshot
 * @author valerysh
 *
 */
public class RevertSnapshotDialog extends BaseDialogBean
{
   private static final String MSG_REVERT_SNAPSHOT_CONFIRM = "revert_snapshot_confirm";
   
   protected AVMBrowseBean avmBrowseBean;
   
   Map<String, String> parameters;

   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   @Override
   public void init(Map<String, String> parameters)
   {
       super.init(parameters);

       this.parameters = parameters;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      this.avmBrowseBean.revertSnapshot(parameters);
      return outcome;
   }
   
   /**
    * @return the confirmation to display to the user
    */
   public String getConfirmMessage()
   {
      return MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), MSG_REVERT_SNAPSHOT_CONFIRM), 
    		  parameters.get("version"));
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
