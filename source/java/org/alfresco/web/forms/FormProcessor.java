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
