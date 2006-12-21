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
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Encapsulation of form instance data.
 *
 * @author Ariel Backenroth
 */
public interface FormInstanceData
   extends Serializable
{

   /** the form generate this form instance data */
   public Form getForm();

   /** the name of this instance data */
   public String getName();

   /** the path relative to the containing webapp */
   public String getWebappRelativePath();

   /** the path relative to the sandbox */
   public String getSandboxRelativePath();

   /** the path to the contents of this form instance data */
   public String getPath();

   /** the url to the asset */
   public String getUrl();

   /** the noderef containing the form instance data */
   public NodeRef getNodeRef();

   /** returns all renditions of this form instance data */
   public List<Rendition> getRenditions();
}
