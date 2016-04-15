package org.alfresco.web.bean;

import java.io.Serializable;

import javax.servlet.ServletException;

/**
 * Bean used by the error page, holds the last exception to be thrown by the system
 * 
 * @author gavinc
 */
public class ErrorBean implements Serializable
{
   private static final long serialVersionUID = -5101720299256547100L;

   public static final String ERROR_BEAN_NAME = "alfresco.ErrorBean";
   
   private String returnPage;
   private Throwable lastError;
   private String errorMessageKey;

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
      this.errorMessageKey = null;
   }
         
   /**
    * Gets the error message key.
    * 
    * @return the error message key
    */
   public String getErrorMessageKey()
   {
      return errorMessageKey;
   }

   /**
    * Sets the error message key.
    * 
    * @param errorMessageKey
    *           the new error message key
    */
   public void setErrorMessageKey(String errorMessageKey)
   {
      this.errorMessageKey = errorMessageKey;
      this.lastError = null;
   }

}
