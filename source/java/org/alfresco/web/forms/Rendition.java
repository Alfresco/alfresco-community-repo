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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.xml.sax.SAXException;

/**
 * Encapsulation of a rendition.
 *
 * @author Ariel Backenroth
 */
public interface Rendition
   extends Serializable
{
   /** the name of this rendition */
   public String getName();

   /** the description of this rendition */
   public String getDescription();

   /** the path relative to the containing webapp */
   public String getWebappRelativePath();

   /** the path relative to the sandbox */
   public String getSandboxRelativePath();

   /** the primary form instance data used to generate this rendition */
   public FormInstanceData getPrimaryFormInstanceData();

   /** the rendering engine template that generated this rendition */
   public RenderingEngineTemplate getRenderingEngineTemplate();

   /** the path to the contents of this rendition */
   public String getPath();

   /** the url to the asset */
   public String getUrl();

   /** the file type image for the rendition */
   public String getFileTypeImage();

   /** the output stream for the rendition */
   public OutputStream getOutputStream();

   /** regenerates the contents of this rendition using the primary form instance data */
   public void regenerate()
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException;

   /** regenerates the contents of this rendition using the provided form instance data*/
   public void regenerate(final FormInstanceData formInstanceData)
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException;
}
