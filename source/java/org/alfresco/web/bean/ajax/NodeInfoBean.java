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
package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean used by an AJAX control to send information back on the 
 * requested node.
 * 
 * @author gavinc
 */
public class NodeInfoBean
{
   private NodeService nodeService;
   private ContentService contentService;
   
   private static final Log logger = LogFactory.getLog(NodeInfoBean.class);
   
   /**
    * Returns information on the node identified by the 'noderef'
    * parameter found in the ExternalContext.
    * <p>
    * The result is the formatted HTML to show on the client.
    */
   public void sendNodeInfo() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      
      String nodeRef = (String)context.getExternalContext().getRequestParameterMap().get("noderef");
      
      if (nodeRef == null || nodeRef.length() == 0)
      {
         throw new IllegalArgumentException("'noderef' parameter is missing");
      }
      
      NodeRef repoNode = new NodeRef(nodeRef);
      
      if (this.nodeService.exists(repoNode))
      {
         // get the client side node representation and its properties
         Node clientNode = new Node(repoNode);
         Map props = clientNode.getProperties();
         
         // get the content size
         Object content = props.get(ContentModel.PROP_CONTENT);
         
         // start the containing table
         out.write("<table cellpadding='3' cellspacing='0'>");
         
         // write out information about the node as table rows
         out.write("<tr><td colspan='2' class='mainSubTitle'>Summary</td></tr>");
         
         // add debug information to the summary if debug is enabled
         if (logger.isDebugEnabled())
         {
            writeRow(out, "Id:", clientNode.getId());
            writeRow(out, "Type:", clientNode.getType().toPrefixString());
         }
         
         writeRow(out, "Description:", (String)props.get(ContentModel.PROP_DESCRIPTION));
         writeRow(out, "Title:", (String)props.get(ContentModel.PROP_TITLE));
         writeRow(out, "Created:", props.get("created").toString());
         writeRow(out, "Modified:", props.get("modified").toString());
         
         // close the <table> and <div> tags
         out.write("<table>");
      }
      else
      {
         out.write("<span class='errorMessage'>Node could not be found in the repository!</span>");
      }
   }

   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Writes a table row with the given data
    * 
    * @param nameColumn The name of the data item
    * @param dataColumn The data
    */
   protected void writeRow(ResponseWriter out, String nameColumn, String dataColumn)
      throws IOException
   {
      out.write("<tr><td>");
      out.write(nameColumn);
      out.write("</td><td>");
      if (dataColumn != null)
      {
         out.write(dataColumn);
      }
      else
      {
         out.write("&nbsp;");
      }
      out.write("</td></tr>");
   }
}
