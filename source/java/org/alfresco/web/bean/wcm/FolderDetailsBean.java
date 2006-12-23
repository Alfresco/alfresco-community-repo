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
