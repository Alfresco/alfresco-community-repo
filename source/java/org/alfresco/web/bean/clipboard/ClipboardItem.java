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
package org.alfresco.web.bean.clipboard;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Simple class representing a single item added to the clipboard. 
 * 
 * @author Kevin Roast
 */
public interface ClipboardItem
{
   public ClipboardStatus getMode();
   
   public String getName();
   
   public QName getType();
   
   public String getIcon();
   
   public String getId();
   
   public NodeRef getNodeRef();
   
   public boolean supportsLink();
   
   public boolean canPasteToViewId(String viewId);
   
   public boolean paste(FacesContext fc, String viewId, int action) throws Throwable;
}
