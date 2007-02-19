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
package org.alfresco.web.bean;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;

/**
 * Bean used by the error page, holds the last exception to be thrown by the system
 * 
 * @author gavinc
 */
public class ErrorBean
{
   public static final String ERROR_BEAN_NAME = "alfresco.ErrorBean";
   
   private String returnPage;
   private Throwable lastError;

   /**
    * @return Returns the page to go back to after the error has been displayed
    */
   public String getReturnPage()
   {
      return returnPage;
   }

   /**
    * @param returnPage The page to return to after showing the error
    */
   public void setReturnPage(String returnPage)
   {
      this.returnPage = returnPage;
   }

   /**
    * @return Returns the lastError.
    */
   public Throwable getLastError()
   {
      return lastError;
   }

   /**
    * @param error The lastError to set.
    */
   public void setLastError(Throwable error)
   {
      // servlet exceptions hide the actual error within the rootCause
      // variable, set the base error to that and throw away the 
      // ServletException wrapper
      if (error instanceof ServletException && 
            ((ServletException)error).getRootCause() != null)
      {
         this.lastError = ((ServletException)error).getRootCause();
      }
      else
      {
         this.lastError = error;
      }
   }
   
   /**
    * @return Returns the last error to occur in string form
    */
   public String getLastErrorMessage()
   {
      String message = "No error currently stored";
      
      if (this.lastError != null)
      {
         StringBuilder builder = new StringBuilder(this.lastError.toString());;
         Throwable cause = this.lastError.getCause();
         
         // build up stack trace of all causes
         while (cause != null)
         {
            builder.append("\ncaused by:\n");
            builder.append(cause.toString());
            
            if (cause instanceof ServletException && 
                  ((ServletException)cause).getRootCause() != null)
            {
               cause = ((ServletException)cause).getRootCause();
            }
            else
            {
               cause = cause.getCause();
            }  
         }
         
         message = builder.toString();
         
         // format the message for HTML display
         message = message.replaceAll("<", "&lt;");
         message = message.replaceAll(">", "&gt;");
         message = message.replaceAll("\n", "<br/>");
      }
      
      return message;
   }
   
   /**
    * @return Returns the stack trace for the last error
    */
   public String getStackTrace()
   {
      String trace = "No stack trace available";
      
      if (this.lastError != null)
      {
         StringWriter stringWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(stringWriter);
         this.lastError.printStackTrace(writer);
         
         // format the message for HTML display
         trace = stringWriter.toString();
         trace = trace.replaceAll("<", "&lt;");
         trace = trace.replaceAll(">", "&gt;");
         trace = trace.replaceAll("\n", "<br/>");
      }
      
      return trace;
   }
}
