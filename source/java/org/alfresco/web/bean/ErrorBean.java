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
