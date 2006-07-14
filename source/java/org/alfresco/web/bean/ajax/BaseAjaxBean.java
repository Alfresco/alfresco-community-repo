package org.alfresco.web.bean.ajax;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * Base class for all Ajax managed beans.
 * 
 * It is not necessary to extend this bean but it provides 
 * helper methods.
 * 
 * @author gavinc
 */
public abstract class BaseAjaxBean
{
   private ResponseWriter writer;
   
   /**
    * Writes the given string to the response writer for this bean
    * 
    * @param str The string to send back to the client
    */
   public void write(String str)
   {
      try
      {
         getWriter().write(str);
      }
      catch (IOException ioe)
      {
         // not much we can do here, ignore
      }
   }
   
   /**
    * Returns the ResponseWriter for this bean
    * 
    * @return The JSF ResponseWriter
    */
   public ResponseWriter getWriter()
   {
      if (this.writer == null)
      {
         this.writer = FacesContext.getCurrentInstance().getResponseWriter();
      }
      
      return this.writer;
   }
}
