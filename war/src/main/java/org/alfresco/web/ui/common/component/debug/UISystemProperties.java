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
package org.alfresco.web.ui.common.component.debug;

import java.util.Map;
import java.util.TreeMap;

/**
 * Component which displays the system properties of the VM
 * 
 * @author gavinc
 */
public class UISystemProperties extends BaseDebugComponent
{
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.debug.SystemProperties";
   }

   /**
    * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
    */
   @SuppressWarnings("unchecked")
   public Map getDebugData()
   {
      // note: sort properties
      Map properties = new TreeMap();
      
      // add the jvm system properties
      Map systemProperties = System.getProperties();
      properties.putAll(systemProperties);
      
      // add heap size properties
      properties.put("heap.size", formatBytes(Runtime.getRuntime().totalMemory()));
      properties.put("heap.maxsize", formatBytes(Runtime.getRuntime().maxMemory()));
      properties.put("heap.free", formatBytes(Runtime.getRuntime().freeMemory()));
      
      return properties; 
   }
   
   /**
    * Helper to format bytes for human output
    * 
    * @param bytes  bytes
    * @return  formatted string
    */
   private static String formatBytes(long bytes)
   {
       float f = bytes / 1024l;
       f = f / 1024l;
       return String.format("%.3fMB (%d bytes)", f, bytes);
   }
}
