/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.forms;

import java.io.Serializable;
import java.io.Writer;
import org.alfresco.service.cmr.repository.NodeRef;
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
   public interface Session
   {

      public NodeRef[] getUploadedFiles();

      public void destroy();

      public Form getForm();

      public Document getFormInstanceData();
   }

   /////////////////////////////////////////////////////////////////////////////

   public static class ProcessingException
      extends Exception
   {

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
    * @param form the form to generate for
    * @param out the writer to write the output to.
    */
   public Session process(final Document formInstanceData,
                          final Form form,
                          final Writer out)
      throws ProcessingException;

   public void process(final Session session,
                       final Writer out)
      throws ProcessingException;
}
