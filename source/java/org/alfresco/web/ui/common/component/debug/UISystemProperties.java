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
