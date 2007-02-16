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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
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
   protected AVMService avmService;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   /**
    * @see org.alfresco.web.bean.spaces.EditSpaceDialog#initEditableNode()
    */
   @Override
   protected Node initEditableNode()
   {
      return new Node(this.avmBrowseBean.getAvmActionNode().getNodeRef());
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // update the existing node in the repository
      NodeRef nodeRef = this.editableNode.getNodeRef();
      Map<String, Object> editedProps = this.editableNode.getProperties();
      
      // handle the name property separately, it is a special case for AVM nodes
      String name = (String)editedProps.get(ContentModel.PROP_NAME);
      if (name != null)
      {
         editedProps.remove(ContentModel.PROP_NAME);
      }
      
      // get the current set of properties from the repository
      Map<QName, Serializable> repoProps = this.nodeService.getProperties(nodeRef);
      
      // add the "uifacets" aspect if required, properties will get set below
      if (this.nodeService.hasAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS) == false)
      {
         this.nodeService.addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, null);
      }
      
      // overwrite the current properties with the edited ones
      Iterator<String> iterProps = editedProps.keySet().iterator();
      while (iterProps.hasNext())
      {
         String propName = iterProps.next();
         QName qname = QName.createQName(propName);
         
         // make sure the property is represented correctly
         Serializable propValue = (Serializable)editedProps.get(propName);
         
         // check for empty strings when using number types, set to null in this case
         if ((propValue != null) && (propValue instanceof String) && 
             (propValue.toString().length() == 0))
         {
            PropertyDefinition propDef = this.dictionaryService.getProperty(qname);
            if (propDef != null)
            {
               if (propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE) || 
                   propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT) ||
                   propDef.getDataType().getName().equals(DataTypeDefinition.INT) || 
                   propDef.getDataType().getName().equals(DataTypeDefinition.LONG))
               {
                  propValue = null;
               }
            }
         }
         
         repoProps.put(qname, propValue);
      }
      
      // send the properties back to the repository
      this.nodeService.setProperties(nodeRef, repoProps);
      
      // perform the rename last as for an AVM it changes the NodeRef
      if (name != null)
      {
         this.fileFolderService.rename(nodeRef, name);
         editedProps.put(ContentModel.PROP_NAME.toString(), name);
      }
      
      return outcome;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // a rename may have occured - we need to reset the NodeRef of the modified AVM Node
      // as an AVM NodeRef contains the name as part of ref - which can therefore change! 
      String name = this.editableNode.getName();
      String oldPath = AVMNodeConverter.ToAVMVersionPath(this.editableNode.getNodeRef()).getSecond();
      String newPath = oldPath.substring(0, oldPath.lastIndexOf('/') + 1) + name;
      this.avmBrowseBean.setAvmActionNode(new AVMNode(this.avmService.lookup(-1, newPath)));
      
      return outcome;
   }
   
   public List<UIListItem> getIcons()
   {
      List<UIListItem> icons = new ArrayList<UIListItem>(1);
      
      UIListItem item = new UIListItem();
      item.setValue(DEFAULT_SPACE_ICON_NAME);
      item.setImage("/images/icons/" + DEFAULT_SPACE_ICON_NAME + ".gif");
      icons.add(item);
      
      return icons;
   }
}
