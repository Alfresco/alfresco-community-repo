/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.wcm;

import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

/**
 * Backing bean for Folder Details page.
 * 
 * @author Kevin Roast
 */
public class FolderDetailsBean extends AVMDetailsBean
{
   private static final long serialVersionUID = -2668158215990649862L;

   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

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
      return AVMUtil.getPreviewURI(getAvmNode().getPath());
   }
   
   /**
    * @return true if the folder is a layered folder with a primary indirection
    */
   public boolean getIsPrimaryLayeredFolder()
   {
      boolean result = false;
      
      String path = getAvmNode().getPath();
      AVMNodeDescriptor nodeDesc = getAvmService().lookup(-1, path);
      if (nodeDesc != null)
      {
         result = (nodeDesc.getType() == AVMNodeType.LAYERED_DIRECTORY && nodeDesc.isPrimary());
      }
      
      return result;
   }

   /**
    * @see org.alfresco.web.bean.wcm.AVMDetailsBean#getNodes()
    */
   @Override
   protected List<AVMNode> getNodes()
   {
      return (List)this.avmBrowseBean.getFolders();
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, "details_of") + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   public String getCurrentItemId()
   {
      return getAvmNode().getId();
   }

   public String getOutcome()
   {
      return "dialog:close:dialog:showFolderDetails";
   }
}
