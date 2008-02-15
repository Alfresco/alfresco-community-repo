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

import java.util.Date;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.dialog.IDialogBean;

/**
 * Bean implementation for the Change Expiration Date dialog.
 * 
 * @author gavinc
 */
public class ChangeExpirationDateDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 7052061252811577796L;
   
   private String path;
   private Date expirationDate;
   private Map<String, Date> expirationDates;
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.path = parameters.get("avmpath");
      
      // get hold of the "SubmitDialog" and retrieve the map of expiration dates
      IDialogBean bean = (IDialogBean)FacesHelper.getManagedBean(
               FacesContext.getCurrentInstance(), "SubmitDialog");
      if (bean != null)
      {
         SubmitDialog dialog = (SubmitDialog)bean;
         this.expirationDates = dialog.getExpiredDates();
         this.expirationDate = this.expirationDates.get(this.path);
      }
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (this.expirationDates != null)
      {
         this.expirationDates.put(this.path, this.expirationDate);
      }
      
      return outcome;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   /**
    * @return The expiration date
    */
   public Date getExpirationDate()
   {
      return this.expirationDate;
   }

   /**
    * @param expirationDate The expiration date
    */
   public void setExpirationDate(Date expirationDate)
   {
      this.expirationDate = expirationDate;
   }
}
