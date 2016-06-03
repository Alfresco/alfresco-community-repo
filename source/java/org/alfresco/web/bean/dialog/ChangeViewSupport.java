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
package org.alfresco.web.bean.dialog;

import java.util.List;
import javax.faces.event.ActionEvent;
import org.alfresco.web.ui.common.component.UIListItem;

/**
 * Interface definition for dialog beans that wish to use the view
 * drop down to change the layout of the dialog.
 * 
 * @author gavinc
 */
public interface ChangeViewSupport
{
   public List<UIListItem> getViewItems();
   
   public String getViewMode();
   
   public void setViewMode(String viewMode);
   
   public void viewModeChanged(ActionEvent event);
}
