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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper bean used to temporarily store the last item added in a multi
 * value editor component.
 * 
 * A Map is used so that multiple components on the same page can use the
 * same backing bean.
 * 
 * @author gavinc
 */
public class MultiValueEditorBean implements Serializable
{
   private static final long serialVersionUID = -5180578793877515158L;
   
   private Map<String, Object> lastItemsAdded = new HashMap<String, Object>(10);
   
   public Map<String, Object> getLastItemsAdded()
   {
      return lastItemsAdded;
   }
}
