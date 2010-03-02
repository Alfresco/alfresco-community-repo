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

/**
 * Generates a component to represent a separator using the HTML <code>&lt;hr/&gt;</code> element.
 * 
 * @author gavinc
 */
public class SeparatorGenerator extends HtmlSeparatorGenerator
{
   public SeparatorGenerator()
   {
      // For the standard separator just show a <hr/> element
      
      this.html = "<div style='margin-top: 6px; margin-bottom: 6px;'><hr></div>";
   }
}