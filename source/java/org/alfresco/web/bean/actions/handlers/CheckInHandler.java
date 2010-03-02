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
package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.executer.CheckInActionExecuter;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler implementation for the "check-in" action.
 * 
 * @author gavinc
 */
public class CheckInHandler extends BaseActionHandler
{
   private static final long serialVersionUID = 9033071326749427779L;
   
   protected static final String PROP_CHECKIN_DESC = "checkinDescription";
   protected static final String PROP_CHECKIN_MINOR = "checkinMinorChange";
   
   @Override
   public void setupUIDefaults(Map<String, Serializable> actionProps)
   {
      actionProps.put(PROP_CHECKIN_MINOR, Boolean.TRUE);
   }

   public String getJSPPath()
   {
      return getJSPPath(CheckInActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      repoProps.put(CheckInActionExecuter.PARAM_DESCRIPTION, 
            actionProps.get(PROP_CHECKIN_DESC));
      
      repoProps.put(CheckInActionExecuter.PARAM_MINOR_CHANGE,
            actionProps.get(PROP_CHECKIN_MINOR));
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      String checkDesc = (String)repoProps.get(CheckInActionExecuter.PARAM_DESCRIPTION);
      actionProps.put(PROP_CHECKIN_DESC, checkDesc);
      
      Boolean minorChange = (Boolean)repoProps.get(CheckInActionExecuter.PARAM_MINOR_CHANGE);
      actionProps.put(PROP_CHECKIN_MINOR, minorChange);
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      String comment = (String)actionProps.get(PROP_CHECKIN_DESC);
      Boolean minorChange = (Boolean)actionProps.get(PROP_CHECKIN_MINOR);
      String change = null;
      if (minorChange != null && minorChange.booleanValue())
      {
         change = Application.getMessage(context, "minor_change");
      }
      else
      {
         change = Application.getMessage(context, "major_change");
      }
      
      return MessageFormat.format(Application.getMessage(context, "action_check_in"),
            new Object[] {change, comment});
   }
   
   /*
    * @see org.alfresco.web.bean.actions.IHandler#isAllowMultiple()
    */
   public boolean isAllowMultiple()
   {
      return false;
   }
}
