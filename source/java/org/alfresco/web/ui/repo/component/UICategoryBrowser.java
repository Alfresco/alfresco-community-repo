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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.CategoryBrowserBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.bean.ajax.CategoryBrowserPluginBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.repo.component.UITree.TreeNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UICategoryBrowser extends SelfRenderingComponent
{
   private static final Log logger = LogFactory.getLog(UICategoryBrowser.class);
 
   public static final String COMPONENT_TYPE = "org.alfresco.faces.CategoryBrowser";
 
   private static final String AJAX_URL_START = "/ajax/invoke/" + CategoryBrowserPluginBean.BEAN_NAME;
 
   private static final String SUBCATEGORIES_PARAM = "include-subcategories-checkbox";
 
   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }
 
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[]) state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
   }
 
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      return values;
   }
 
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getClientId(context);
      String value = (String) requestMap.get(fieldId);
 
      if (value != null && value.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Received post back: " + value);
 
         // work out whether a panel or a node was selected
         String item = value;
 
         String subcategoriesStr = (String) requestMap.get(SUBCATEGORIES_PARAM);
         boolean includeSubcategories = "1".equals(subcategoriesStr);
         logger.debug("Booléen = " + includeSubcategories);
 
         // queue an event to be handled later
         CategoryBrowserEvent event = new CategoryBrowserEvent(this, item, includeSubcategories);
         this.queueEvent(event);
      }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.alfresco.extension.web.ui.repo.component.UINavigator#broadcast(javax.faces.event.FacesEvent)
    */
   @Override
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof CategoryBrowserEvent)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         CategoryBrowserEvent categoryBrowseEvent = (CategoryBrowserEvent) event;
         NodeRef nodeClicked = new NodeRef(categoryBrowseEvent.getItem());
         boolean subcategories = categoryBrowseEvent.isIncludeSubcategories();
         if (logger.isDebugEnabled())
            logger.debug("Selected category: " + nodeClicked + " subcategories? " + subcategories);
 
         CategoryBrowserBean categoryBrowserBean = (CategoryBrowserBean) FacesHelper.getManagedBean(context,
               CategoryBrowserBean.BEAN_NAME);
         categoryBrowserBean.setCurrentCategory(nodeClicked);
         categoryBrowserBean.setIncludeSubcategories(subcategories);
         SearchContext categorySearch = categoryBrowserBean.generateCategorySearchContext();
 
         NavigationBean nb = (NavigationBean) FacesHelper.getManagedBean(context, NavigationBean.BEAN_NAME);
         nb.setSearchContext(categorySearch);
         context.getApplication().getNavigationHandler().handleNavigation(context, null, "category-browse");
      }
      else
      {
         super.broadcast(event);
      }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.alfresco.web.ui.repo.component.UINavigator#encodeBegin(javax.faces.context.FacesContext)
    */
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered())
         return;
 
      // TODO: pull width and height from user preferences and/or the main config,
      // if present override below using the style attribute
 
      ResponseWriter out = context.getResponseWriter();
      CategoryBrowserPluginBean categoryBrowserPluginBean = (CategoryBrowserPluginBean) FacesHelper.getManagedBean(
            context, CategoryBrowserPluginBean.BEAN_NAME);
      CategoryBrowserBean categoryBrowserBean = (CategoryBrowserBean) FacesHelper.getManagedBean(context,
            CategoryBrowserBean.BEAN_NAME);
 
      List<TreeNode> rootNodes = null;
 
      rootNodes = categoryBrowserPluginBean.getCategoryRootNodes();
      // order the root nodes by the tree label
      if (rootNodes != null && rootNodes.size() > 1)
      {
         QuickSort sorter = new QuickSort(rootNodes, "name", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
 
      // main container div
      out.write("<div id=\"category-navigator\" class=\"navigator\">");
 
      // Subcategories parameter
      String includeSub = Application.getMessage(context, "category_browser_plugin_include_subcategories");
      out.write("<input type='checkbox' id='" + SUBCATEGORIES_PARAM + "' name='" + SUBCATEGORIES_PARAM + "' value=1 "
            + (categoryBrowserBean.isIncludeSubcategories() ? "checked" : "") + "/>");
      out.write("<label for='" + SUBCATEGORIES_PARAM + "'>" + includeSub + "</label>");
 
      // generate the javascript method to capture the tree node click events
      out.write("<script type=\"text/javascript\">");
      out.write("function treeNodeSelected(nodeRef) {");
      out.write(Utils.generateFormSubmit(context, this, getClientId(context), "nodeRef", true, null));
      out.write("}</script>");
 
      // generate the active panel containing the tree
      out.write("<div class=\"navigatorPanelBody\">");
      UITree tree = (UITree) context.getApplication().createComponent(UITree.COMPONENT_TYPE);
      tree.setId("tree");
      tree.setRootNodes(rootNodes);
      tree.setRetrieveChildrenUrl(AJAX_URL_START + ".retrieveChildren?");
      tree.setNodeCollapsedUrl(AJAX_URL_START + ".nodeCollapsed?");
      tree.setNodeSelectedCallback("treeNodeSelected");
      tree.setNodeCollapsedCallback("informOfCollapse");
      Utils.encodeRecursive(context, tree);
      out.write("</div>");
 
      out.write("</div>");
   }
 
   @Override
   public void encodeChildren(FacesContext context) throws IOException
   {
      if (!isRendered())
         return;
      for (Iterator i = this.getChildren().iterator(); i.hasNext();)
      {
         UIComponent child = (UIComponent) i.next();
         Utils.encodeRecursive(context, child);
      }
   }
 
   @Override
   public boolean getRendersChildren()
   {
      return true;
   }
 
   /**
    * Class representing the clicking of a tree node.
    */
   @SuppressWarnings("serial")
   public static class CategoryBrowserEvent extends ActionEvent
   {
      private String item;
 
      private boolean includeSubcategories;
 
      public CategoryBrowserEvent(UIComponent component, String item, boolean include)
      {
         super(component);
 
         this.item = item;
         this.includeSubcategories = include;
      }
 
      public String getItem()
      {
         return item;
      }
 
      public boolean isIncludeSubcategories()
      {
         return includeSubcategories;
      }
   }
}
