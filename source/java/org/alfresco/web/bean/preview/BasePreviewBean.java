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
package org.alfresco.web.bean.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.ExpiringValueCache;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.TemplateSupportBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;

/**
 * Backing bean for the Preview Document in Template action page
 * 
 * @author Kevin Roast
 */
public abstract class BasePreviewBean
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
      public String resolveImagePathForName(String filename, boolean small)
      {
         return Utils.getFileTypeImage(filename, small);
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
