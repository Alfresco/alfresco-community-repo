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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.wcm;

import java.util.List;

import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;

/**
 * Backing bean for Folder Details page.
 * 
 * @author Kevin Roast
 */
public class FolderDetailsBean extends AVMDetailsBean
{
   /**
    * @see org.alfresco.web.bean.wcm.AVMDetailsBean#getAvmNode()
    */
   @Override
   public AVMNode getAvmNode()
   {
      return this.avmBrowseBean.getAvmActionNode();
   }
   
   /**
    * @return a Node wrapper of the AVM Folder Node - for property sheet support
    */
   public Node getFolder()
   {
      return new Node(getAvmNode().getNodeRef());
   }
   
   /**
    * Returns the virtualisation server URL to the content for the current document
    *  
    * @return Preview url for the current document
    */
   public String getPreviewUrl()
   {
      return AVMConstants.buildAssetUrl(getAvmNode().getPath());
   }

   /**
    * @see org.alfresco.web.bean.wcm.AVMDetailsBean#getNodes()
    */
   @Override
   protected List<AVMNode> getNodes()
   {
      return (List)this.avmBrowseBean.getFolders();
   }
}
