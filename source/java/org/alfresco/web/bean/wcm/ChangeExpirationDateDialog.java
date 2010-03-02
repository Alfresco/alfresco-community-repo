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
