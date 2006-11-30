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
    * Returns the rendering engines name.
    *
    * @return the name of the rendering engine.
    */
   public String getName();


   /**
    * Returns the default file extension for rendering engine templates for this
    * rendering engine.
    *
    * @return the default file extension for rendering engine templates for this
    * rendering engine.
    */
   public String getDefaultTemplateFileExtension();

   /**
    * Renders the xml data in to a presentation format.
    *
    * @param formInstanceData the xml content to serialize.
    * @param ret the rendering engine template
    * @param form the form that collected the xml content.
    * @param parameters the set of parameters to the rendering engine
    * @param out the output stream to serialize to.
    */
   public void render(final Document formInstanceData,
                      final RenderingEngineTemplate ret,
                      final Map<String, String> parameters,
                      final OutputStream out)
      throws IOException, RenderingException;
}
