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
package org.alfresco.web.forms;

import java.io.Serializable;
import java.io.Writer;

import org.w3c.dom.Document;

/**
 * Generates a user interface for inputing data into a template.
 * @author Ariel Backenroth
 */
public interface FormProcessor
    extends Serializable
{
   
   /////////////////////////////////////////////////////////////////////////////
   
   /**
    * An abstraction layer around the xml content which allows
    * for reseting the xml content being collected by the input method.
    */
   public interface Session extends Serializable
   {

      /** Returns the set of file uploaded during the session. */
//      public NodeRef[] getUploadedFiles();

      /** Destroys the session and releases all resources used by it */
      public void destroy();

      /** Returns the form used by the session. */
      public Form getForm();

      /** Returns the current state of the form instance data */
      public Document getFormInstanceData();

      /** Returns the name of the form instance data being modified */
      public String getFormInstanceDataName();
   }

   /////////////////////////////////////////////////////////////////////////////

   /**
    * Exception for errors encoutered during form processing.
    */
   public static class ProcessingException
      extends Exception
   {

      private static final long serialVersionUID = -1067792684180745503L;

      public ProcessingException(final String msg)
      {
         super(msg);
      }

      public ProcessingException(final Exception cause)
      {
         super(cause);
      }

      public ProcessingException(final String msg, final Exception cause)
      {
         super(msg, cause);
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   /**
    * Processes a user interface for inputing data into a form.
    *
    * @param formInstanceData provides the xml instance data if available.
    * @param formInstanceDataName the name of the form instance data being modified.
    * @param form the form to generate for
    * @param out the writer to write the output to.
    */
   public Session process(final Document formInstanceData,
                          final String formInstanceDataName,
                          final Form form,
                          final Writer out)
      throws ProcessingException;

   /**
    * Processes a user interface for inputing data into a form.
    *
    * @param session the session to use.
    * @param out the writer to write the output to.
    */
   public void process(final Session session,
                       final Writer out)
      throws ProcessingException;
}
