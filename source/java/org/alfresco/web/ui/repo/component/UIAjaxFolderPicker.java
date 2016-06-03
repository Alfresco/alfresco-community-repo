/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.web.ui.repo.component;

/**
 * JSF Ajax object picker for navigating through and selecting folders.
 * 
 * @author Kevin Roast
 */
public class UIAjaxFolderPicker extends BaseAjaxItemPicker
{
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.AjaxFolderPicker";
   }

   @Override
   protected String getServiceCall()
   {
      return "PickerBean.getFolderNodes";
   }

   @Override
   protected String getDefaultIcon()
   {
      // none required - we always return an icon name in the service call
      return null;
   }
}
