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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.ui.repo.component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.ui.common.Utils;

/**
 * JSF Ajax object picker for navigating through folders and selecting a file.
 * 
 * @author Kevin Roast
 */
public class UIAjaxFilePicker extends BaseAjaxItemPicker
{
   /** list of mimetypes to restrict the available file list */
   private String mimetypes = null;
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.AjaxFilePicker";
   }
   
      /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      super.restoreState(context, values[0]);
      this.mimetypes = (String)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[] {
         super.saveState(context),
         this.mimetypes};
      return values;
   }

   @Override
   protected String getServiceCall()
   {
      return "PickerBean.getFileFolderNodes";
   }

   @Override
   protected String getDefaultIcon()
   {
      // none required - we always return an icon name in the service call
      return null;
   }
   
   @Override
   protected String getRequestAttributes()
   {
      String mimetypes = getMimetypes();
      if (mimetypes != null)
      {
         try
         {
            return "mimetypes=" + URLEncoder.encode(mimetypes, "UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            throw new AlfrescoRuntimeException("Unsupported encoding.", e);
         }
      }
      else
      {
         return null;
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return Returns the mimetypes to restrict the file list.
    */
   public String getMimetypes()
   {
      ValueBinding vb = getValueBinding("mimetypes");
      if (vb != null)
      {
         this.mimetypes = (String)vb.getValue(getFacesContext());
      }
      
      return this.mimetypes;
   }
   
   /**
    * @param mimetypes The mimetypes restriction list to set.
    */
   public void setMimetypes(String mimetypes)
   {
      this.mimetypes = mimetypes;
   }
}
