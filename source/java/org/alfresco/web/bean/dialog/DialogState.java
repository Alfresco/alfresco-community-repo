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
package org.alfresco.web.bean.dialog;

import java.io.Serializable;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.config.DialogsConfigElement.DialogConfig;

/**
 * Object responsible for holding the current state of an active dialog.
 * 
 * @author gavinc
 */
public final class DialogState implements Serializable
{
   private static final long serialVersionUID = -5007635589636930602L;
   
   private DialogConfig config;
   private IDialogBean dialog;
   
   /**
    * Default constructor
    * 
    * @param config The configuration for the dialog
    * @param dialog The dialog bean instance
    */
   public DialogState(DialogConfig config, IDialogBean dialog)
   {
      this.config = config;
      this.dialog = dialog;
   }
   
   /**
    * Returns the configuration for the dialog
    * 
    * @return The dialog configuration
    */
   public DialogConfig getConfig()
   {
      return config;
   }
   
   /**
    * Returns the bean representing the dialog instance
    * 
    * @return The dialog bean instance
    */
   public IDialogBean getDialog()
   {
      return dialog;
   }

   @Override
   public String toString()
   {
      return AlfrescoNavigationHandler.DIALOG_PREFIX + this.config.getName();
   }
}
