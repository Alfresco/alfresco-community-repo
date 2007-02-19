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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.alfresco.web.ui.repo.component.UITree;
import org.alfresco.web.ui.repo.component.UITree.TreeNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Renderer for the UITree component that outputs the necessary
 * JavaScript to use the Yahoo UI toolkit tree control.
 * 
 * @author gavinc
 */
public class YahooTreeRenderer extends BaseRenderer
{
   protected int nodeCounter;
   
   protected final static String TREE_SCRIPTS_WRITTEN = "_alfTreeScripts";
   
   private static final Log logger = LogFactory.getLog(YahooTreeRenderer.class);

   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(FacesContext context, UIComponent component) throws IOException
   {
      // get the root nodes
      UITree tree = (UITree)component;
      List<TreeNode> rootNodes = tree.getRootNodes();
      
      if (rootNodes != null && rootNodes.size() > 0)
      {
         ResponseWriter out = context.getResponseWriter();
         String treeContainerId = component.getClientId(context) + "Container";
         
         // output the scripts required by the component (checks are 
         // made to make sure the scripts are only written once)
         Utils.writeYahooScripts(context, out, null);
         
         // write out the JavaScript specific to the Tree component,
         // again, make sure it's only done once
         Object present = context.getExternalContext().getRequestMap().
            get(TREE_SCRIPTS_WRITTEN);
         if (present == null)
         {
            out.write("<link rel=\"stylesheet\" href=\"");
            out.write(context.getExternalContext().getRequestContextPath());
            out.write("/css/yahoo-tree.css\" type=\"text/css\" />");
            
            out.write("<script type=\"text/javascript\" src=\"");
            out.write(context.getExternalContext().getRequestContextPath());
            out.write("/scripts/ajax/yahoo/treeview/treeview-min.js\"> </script>\n");
            
            out.write("<script type=\"text/javascript\" src=\"");
            out.write(context.getExternalContext().getRequestContextPath());
            out.write("/scripts/ajax/yahoo-tree.js\"> </script>\n");
            
            context.getExternalContext().getRequestMap().put(
                  TREE_SCRIPTS_WRITTEN, Boolean.TRUE);
         }
         
         // output the div container for the tree
         out.write("<div id=\"");
         out.write(treeContainerId);
         out.write("\"></div>\n");
         
         // generate the startup 
         out.write("<script type=\"text/javascript\">\n");
         out.write("var tree;\n");
         if (tree.getRetrieveChildrenUrl() != null)
         {
            out.write("setLoadDataUrl('");
            out.write(tree.getRetrieveChildrenUrl());
            out.write("');\n");
         }
         if (tree.getNodeCollapsedUrl() != null)
         {
            out.write("setCollapseUrl('");
            out.write(tree.getNodeCollapsedUrl());
            out.write("');\n");
         }
         if (tree.getNodeSelectedCallback() != null)
         {
            out.write("setNodeSelectedHandler('");
            out.write(tree.getNodeSelectedCallback());
            out.write("');\n");
         }
         out.write("function initTree() {\n");
         out.write("      tree = new YAHOO.widget.TreeView(\"");
         out.write(treeContainerId);
         out.write("\");\n");
         out.write("      var root = tree.getRoot();\n");
         
         if (tree.getNodeExpandedCallback() != null)
         {
            out.write("      tree.subscribe('expand', ");
            out.write(tree.getNodeExpandedCallback());
            out.write(");\n");
         }
         if (tree.getNodeCollapsedCallback() != null)
         {
            out.write("      tree.subscribe('collapse', ");
            out.write(tree.getNodeCollapsedCallback());
            out.write(");\n");
         }
         
         // generate script for each root node
         this.nodeCounter = 0;
         for (TreeNode node : rootNodes)
         {
            generateNode(node, out, null);
         }

         out.write("      tree.draw();\n");
         out.write("      tree.setDynamicLoad(loadDataForNode);\n}\n");
         out.write("YAHOO.util.Event.on(window, \"load\", window.initTree);");
         out.write("</script>\n");
      }
      else if (logger.isDebugEnabled())
      {
         logger.debug("There weren't any nodes to render");
      }
   }
   
   /**
    * Generates the JavaScript required to create the branch of a tree from
    * the given node.
    * 
    * @param node The node to generate
    * @param out Response writer
    * @param parentVarName Name of the parent variable, null if the node has no parent
    */
   protected void generateNode(TreeNode node, ResponseWriter out, String parentVarName) 
         throws IOException
   {
      String currentVarName = getNextVarName();
      
      // generate the Javascript to create the given node using the
      // appropriate parent variable
      out.write("      var ");
      out.write(currentVarName);
      out.write(" = createYahooTreeNode(");
      
      if (node.getParent() == null)
      {
         out.write("root");
      }
      else
      {
         out.write(parentVarName);
      }
      
      out.write(", \"");
      out.write(node.getNodeRef());
      out.write("\", \"");
      out.write(node.getName());
      out.write("\", \"");
      out.write(node.getIcon());
      out.write("\", ");
      out.write(Boolean.toString(node.isExpanded()));
      out.write(", ");
      out.write(Boolean.toString(node.isSelected()));
      out.write(");\n");
      
      // iterate through the child nodes and generate them
      if (node.isExpanded() && node.getChildren().size() > 0)
      {
         // order the children
         List<TreeNode> children = node.getChildren();
         if (children.size() > 1)
         {
            QuickSort sorter = new QuickSort(children, "name", true, IDataContainer.SORT_CASEINSENSITIVE);
            sorter.sort();
         }
         
         for (TreeNode child : children)
         {
            generateNode(child, out, currentVarName);
         }
      }
   }
   
   protected String getNextVarName()
   {
      this.nodeCounter++;
      return "n" + Integer.toString(this.nodeCounter);
   }
}
