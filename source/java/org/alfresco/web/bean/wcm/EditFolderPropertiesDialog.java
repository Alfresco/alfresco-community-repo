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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.spaces.EditSpaceDialog;
import org.alfresco.web.ui.common.component.UIListItem;

/**
 * Backing bean for the Edit Folder Properties dialog.
 * 
 * @author Kevin Roast
 */
public class EditFolderPropertiesDialog extends EditSpaceDialog
{
   protected AVMBrowseBean avmBrowseBean;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   /**
    * @see org.alfresco.web.bean.spaces.EditSpaceDialog#initEditableNode()
    */
   @Override
   protected Node initEditableNode()
   {
      return new Node(this.avmBrowseBean.getAvmNode().getNodeRef());
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      return outcome;
   }
   
   public List<UIListItem> getIcons()
   {
      List<UIListItem> icons = new ArrayList<UIListItem>(1);
      
      UIListItem item = new UIListItem();
      item.setValue(DEFAULT_SPACE_ICON_NAME);
      item.getAttributes().put("image", "/images/icons/" + DEFAULT_SPACE_ICON_NAME + ".gif");
      icons.add(item);
      
      return icons;
   }
}
