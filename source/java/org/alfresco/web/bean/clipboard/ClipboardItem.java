/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.clipboard;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Simple class representing a single item added to the clipboard. 
 * 
 * @author Kevin Roast
 */
public interface ClipboardItem extends Serializable
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
