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
   /**
    * @return the mode status of the clipboard item, the enum can be either Cut or Copy
    */
   public ClipboardStatus getMode();
   
   /**
    * @return display label (cm:name) of the clipboard item
    */
   public String getName();
   
   /**
    * @return QName type of the clipboard item
    */
   public QName getType();
   
   /**
    * @return the app:icon property of the clipboard item
    */
   public String getIcon();
   
   /**
    * @return the NodeRef of the clipboard item
    */
   public NodeRef getNodeRef();
   
   /**
    * @return true if the item on the clipboard supports linking (.url) as a link type
    */
   public boolean supportsLink();
   
   /**
    * @param viewId     JSF View Id to check against
    * 
    * @return true if the clipboard item can be Copy pasted to the specified JSF view
    */
   public boolean canCopyToViewId(String viewId);
   
   /**
    * @param viewId     JSF View Id to check against
    * 
    * @return true if the clipboard item can be Move pasted to the specified JSF view
    */
   public boolean canMoveToViewId(String viewId);
   
   /**
    * @param fc         FacesContext
    * @param viewId     JSF View Id to paste into
    * @param action     Clipboard action constant (@see org.alfresco.web.ui.repo.component.shelf.UIClipboardShelfItem)
    * 
    * @return true on successful paste, false otherwise
    * 
    * @throws Throwable on fatal error during paste
    */
   public boolean paste(FacesContext fc, String viewId, int action) throws Throwable;
}
