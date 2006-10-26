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

import org.alfresco.service.cmr.repository.NodeRef;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import org.w3c.dom.Document;

/**
 * Serializes the xml instance data collected by a form to a writer.
 *
 * @author Ariel Backenroth
 */
public interface RenderingEngine
   extends Serializable
{
   /////////////////////////////////////////////////////////////////////////////

   public static class RenderingException
      extends Exception
   {

      public RenderingException(final Exception cause)
      {
         super(cause);
      }

      public RenderingException(final String msg, final Exception cause)
      {
         super(msg, cause);
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   /** 
    * XXXarielb this shouldn't be in the interface... i'll figure out a 
    * different id scheme once i make rendering engines configurable.
    *
    * the noderef associated with this output method 
    */
   public NodeRef getNodeRef();

   /**
    * Renders the xml data in to a presentation format.
    *
    * @param formInstanceData the xml content to serialize
    * @param form the form that collected the xml content.
    * @param parameters the set of parameters to the rendering engine
    * @param out the output stream to serialize to.
    */
   public void render(final Document formInstanceData,
                      final Map<String, String> parameters,
                      final OutputStream out)
      throws IOException, RenderingException;

   /**
    * Returns the file extension to use when generating content for this
    * output method.
    *
    * @return the file extension to use when generating content for this
    * output method, such as html, xml, pdf.
    */
   public String getFileExtensionForRendition();

   /**
    * Returns the mimetype to use when generating content for this
    * output method.
    *
    * @return the mimetype to use when generating content for this
    * output method, such as text/html, text/xml, application/pdf.
    */
   public String getMimetypeForRendition();
}
