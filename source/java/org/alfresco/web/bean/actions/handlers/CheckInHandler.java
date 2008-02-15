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
}
