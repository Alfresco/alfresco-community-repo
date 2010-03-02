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
package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.repo.component.property.PropertySheetItem;

/**
 * Generates a component to represent a separator that gets rendered 
 * as a header.
 * 
 * @author gavinc
 */
public class HeaderSeparatorGenerator extends SeparatorGenerator
{
   protected String getHtml(UIComponent component, PropertySheetItem item)
   {
      String html = "<div class='wizardSectionHeading mainSubTitle' style='margin-top: 6px; margin-bottom: 6px;'>&nbsp;" + 
                    item.getDisplayLabel() + "</div>";
      
      return html;
   }
}
