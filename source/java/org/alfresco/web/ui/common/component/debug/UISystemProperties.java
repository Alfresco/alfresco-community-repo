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
