/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
      StringWriter stringWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(stringWriter);
      this.lastError.printStackTrace(writer);
      
      // format the message for HTML display
      String trace = stringWriter.toString();
      trace = trace.replaceAll("<", "&lt;");
      trace = trace.replaceAll(">", "&gt;");
      trace = trace.replaceAll("\n", "<br/>");
      
      return trace;
   }
}
