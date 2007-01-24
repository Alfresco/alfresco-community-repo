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

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Base class representing a single item added to the clipboard. 
 * 
 * @author Kevin Roast
 */
abstract class AbstractClipboardItem implements ClipboardItem
{
   /**
    * Constructor
    * 
    * @param ref        The ref of the item on the clipboard
    * @param mode       The ClipboardStatus enum value
    */
   public AbstractClipboardItem(NodeRef ref, ClipboardStatus mode)
   {
      this.ref = ref;
      this.mode = mode;
   }
   
   public ClipboardStatus getMode()
   {
      return this.mode;
   }
   
   public String getName()
   {
      if (this.name == null)
      {
         this.name = (String)getNodeService().getProperty(this.ref, ContentModel.PROP_NAME);
      }
      return this.name;
   }
   
   public QName getType()
   {
      if (this.type == null)
      {
         this.type = getNodeService().getType(this.ref);
      }
      return this.type;
   }
   
   public String getIcon()
   {
      if (this.icon == null)
      {
         this.icon = (String)getNodeService().getProperty(this.ref, ApplicationModel.PROP_ICON);
      }
      return this.icon;
   }
   
   public String getId()
   {
      return this.ref.getId();
   }
   
   public NodeRef getNodeRef()
   {
      return this.ref;
   }
   
   /**
    * Override equals() to compare NodeRefs
    */
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof ClipboardItem)
      {
         return ((ClipboardItem)obj).getNodeRef().equals(this.ref);
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Override hashCode() to use the internal NodeRef hashcode instead
    */
   public int hashCode()
   {
      return ref.hashCode();
   }
   
   protected static NodeService getNodeService()
   {
      return Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
   }
   
   protected NodeRef ref;
   protected ClipboardStatus mode;
   
   // cached values
   private String name;
   private QName type;
   private String icon;
}
