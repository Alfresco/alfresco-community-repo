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
package org.alfresco.web.bean.preview;

import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.TemplateSupportBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Backing bean for the Preview Document in Template action page
 * 
 * @author Kevin Roast
 */
public abstract class BasePreviewBean extends BaseDialogBean
{
   /** BrowseBean instance */
   protected BrowseBean browseBean;
   
   /** The NavigationBean bean reference */
   protected NavigationBean navigator;
   
   /** The selected Template Id */
   protected NodeRef template;
   

   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }
   
   /**
    * Returns the node this bean is currently working with
    * 
    * @return The current Node
    */
   public abstract Node getNode();
   
   /**
    * Returns the id of the current node
    * 
    * @return The id
    */
   public String getId()
   {
      return getNode().getId();
   }
   
   /**
    * Returns the name of the current node
    * 
    * @return Name of the current node
    */
   public String getName()
   {
      return getNode().getName();
   }
   
   /**
    * Returns a model for use by the template on the Preview page.
    * 
    * @return model containing current document/space info.
    */
   public abstract Map getTemplateModel();
   
   /** Template Image resolver helper */
   protected TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
      public String resolveImagePathForName(String filename, FileTypeImageSize size)
      {
         return FileTypeImageUtils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
      }
   };

   /**
    * @return the current template as a full NodeRef
    */
   public NodeRef getTemplateRef()
   {
      return this.template;
   }
   
   /**
    * @return Returns the template Id.
    */
   public String getTemplate()
   {
      return (this.template != null ? this.template.getId() : null);
   }

   /**
    * @param template The template Id to set.
    */
   public void setTemplate(String template)
   {
      if (template != null && template.equals(TemplateSupportBean.NO_SELECTION) == false)
      {
         this.template = new NodeRef(Repository.getStoreRef(), template);
      }
   }
   
   private int findNextPreviewNode(List<Node> nodes, int start)
   {
      // search from start to end of list
      for (int i=start; i<nodes.size(); i++)
      {
         Node next = nodes.get(i);
         if (next.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
         {
            return i;
         }
      }
      // search from zero index to start - 1 (to skip original node)
      for (int i=0; i<start - 1; i++)
      {
         Node next = nodes.get(i);
         if (next.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
         {
            return i;
         }
      }
      return -1;
   }
   
   private int findPrevPreviewNode(List<Node> nodes, int start)
   {
      // search from start to beginning of list
      for (int i=start; i>=0; i--)
      {
         Node next = nodes.get(i);
         if (next.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
         {
            return i;
         }
      }
      // end of list to start + 1 (to skip original node)
      for (int i=nodes.size() - 1; i>start; i--)
      {
         Node next = nodes.get(i);
         if (next.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
         {
            return i;
         }
      }
      return -1;
   }
}
