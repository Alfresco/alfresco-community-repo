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
package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

public class CCCheckinFileDialog extends CheckinCheckoutDialog
{

   private static final long serialVersionUID = -3591701539727090905L;
   
   private static final String MSG_CHECK_IN = "check_in";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return checkinFileOK(context, outcome);
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CHECK_IN);
   }
   
   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_CHECK_IN) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)       
           + property.getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
   @Override
   protected String getErrorOutcome(Throwable exception)
   {
      return "dialog:close";
   }
}
