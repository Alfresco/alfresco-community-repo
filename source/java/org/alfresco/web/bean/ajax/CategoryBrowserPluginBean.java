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
package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.component.UITree.TreeNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CategoryBrowserPluginBean
{
   public static final String BEAN_NAME = "CategoryBrowserPluginBean";
   
   private static final Log logger = LogFactory.getLog(CategoryBrowserPluginBean.class);
 
   private CategoryService categoryService;
 
   private NodeService nodeService;
 
   private List<TreeNode> categoryRootNodes;
 
   private Map<String, TreeNode> categoryNodes;
 
   protected NodeRef previouslySelectedNode;
 
   /**
    * @param categoryService
    *           the categoryService to set
    */
   public void setCategoryService(CategoryService categoryService)
   {
      this.categoryService = categoryService;
   }
 
   /**
    * @param nodeService
    *           the nodeService to set
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
 
   public List<TreeNode> getCategoryRootNodes()
   {
      if (this.categoryRootNodes == null)
      {
         this.categoryRootNodes = new ArrayList<TreeNode>();
         this.categoryNodes = new HashMap<String, TreeNode>();
 
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
            tx.begin();
 
            Collection<ChildAssociationRef> childRefs = this.categoryService.getRootCategories(
                  Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE);
 
            for (ChildAssociationRef ref : childRefs)
            {
               NodeRef child = ref.getChildRef();
               TreeNode node = createTreeNode(child);
               this.categoryRootNodes.add(node);
               this.categoryNodes.put(node.getNodeRef(), node);
            }
 
            tx.commit();
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage("NavigatorPluginBean exception in getCompanyHomeRootNodes()", err);
            try
            {
               if (tx != null)
               {
                  tx.rollback();
               }
            }
            catch (Exception tex)
            {
            }
         }
      }
 
      return this.categoryRootNodes;
   }
 
   protected Map<String, TreeNode> getNodesMap()
   {
      return this.categoryNodes;
   }
 
   /**
    * Retrieves the child folders for the noderef given in the 'noderef' parameter and caches the nodes against the area
    * in the 'area' parameter.
    */
   public void retrieveChildren() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
 
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
 
         Map params = context.getExternalContext().getRequestParameterMap();
         String nodeRefStr = (String) params.get("nodeRef");
 
         // work out which list to cache the nodes in
         Map<String, TreeNode> currentNodes = getNodesMap();
 
         if (nodeRefStr != null && currentNodes != null)
         {
            // get the given node's details
            NodeRef parentNodeRef = new NodeRef(nodeRefStr);
            TreeNode parentNode = currentNodes.get(parentNodeRef.toString());
            parentNode.setExpanded(true);
 
            if (logger.isDebugEnabled())
               logger.debug("retrieving children for noderef: " + parentNodeRef);
 
            // remove any existing children as the latest ones will be added
            // below
            parentNode.removeChildren();
 
            // get all the child folder objects for the parent
            List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(parentNodeRef,
                  ContentModel.ASSOC_SUBCATEGORIES, RegexQNamePattern.MATCH_ALL);
            List<TreeNode> sortedNodes = new ArrayList<TreeNode>();
            for (ChildAssociationRef ref : childRefs)
            {
               NodeRef nodeRef = ref.getChildRef();
               logger.debug("retrieving child : " + nodeRef);
               // build the XML representation of the child node
               TreeNode childNode = createTreeNode(nodeRef);
               parentNode.addChild(childNode);
               currentNodes.put(childNode.getNodeRef(), childNode);
               sortedNodes.add(childNode);
            }
 
            // order the tree nodes by the tree label
            if (sortedNodes.size() > 1)
            {
               QuickSort sorter = new QuickSort(sortedNodes, "name", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
 
            // generate the XML representation
            StringBuilder xml = new StringBuilder(
                  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><nodes>");
            for (TreeNode childNode : sortedNodes)
            {
               xml.append(childNode.toXML());
            }
            xml.append("</nodes>");
 
            // send the generated XML back to the tree
            out.write(xml.toString());
 
            if (logger.isDebugEnabled())
               logger.debug("returning XML: " + xml.toString());
         }
 
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         try
         {
            if (tx != null)
            {
               tx.rollback();
            }
         }
         catch (Exception tex)
         {
         }
      }
   }
 
   /**
    * Sets the state of the node given in the 'nodeRef' parameter to collapsed
    */
   public void nodeCollapsed() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      Map params = context.getExternalContext().getRequestParameterMap();
      String nodeRefStr = (String) params.get("nodeRef");
 
      // work out which list to cache the nodes in
      Map<String, TreeNode> currentNodes = getNodesMap();
 
      if (nodeRefStr != null && currentNodes != null)
      {
         TreeNode treeNode = currentNodes.get(nodeRefStr);
         if (treeNode != null)
         {
            treeNode.setExpanded(false);
 
            // we need to return something for the client to be happy!
            out.write("<ok/>");
 
            if (logger.isDebugEnabled())
               logger.debug("Set node " + treeNode + " to collapsed state");
         }
      }
   }
 
   public void selectNode(NodeRef selectedNode, String area)
   {
      // if there is a currently selected node, get hold of
      // it (from any of the areas) and reset to unselected
      if (this.previouslySelectedNode != null)
      {
         TreeNode node = this.categoryNodes.get(this.previouslySelectedNode.toString());
         if (node != null)
         {
            node.setSelected(false);
         }
      }
 
      // find the node just selected and set its state to selected
      if (selectedNode != null)
      {
         TreeNode node = this.categoryNodes.get(selectedNode.toString());
         if (node != null)
         {
            node.setSelected(true);
         }
      }
 
      this.previouslySelectedNode = selectedNode;
 
      if (logger.isDebugEnabled())
         logger.debug("Selected node: " + selectedNode);
   }
 
   /**
    * Resets the selected node
    */
   public void resetSelectedNode()
   {
      if (this.previouslySelectedNode != null)
      {
         TreeNode node = this.categoryNodes.get(this.previouslySelectedNode.toString());
         if (node != null)
         {
            node.setSelected(false);
         }
      }
      if (logger.isDebugEnabled())
         logger.debug("Reset selected node: " + this.previouslySelectedNode);
   }
 
   /**
    * Resets all the caches held by the bean.
    */
   public void reset()
   {
      this.categoryNodes = null;
      this.categoryRootNodes = null;
 
      resetSelectedNode();
   }
 
   /**
    * Creates a TreeNode object from the given NodeRef
    * 
    * @param nodeRef
    *           The NodeRef to create the TreeNode from
    */
   protected TreeNode createTreeNode(NodeRef nodeRef)
   {
      TreeNode node = new TreeNode(nodeRef.toString(), Repository.getNameForNode(this.nodeService, nodeRef),
            (String) this.nodeService.getProperty(nodeRef, ApplicationModel.PROP_ICON));
 
      return node;
   }
}
