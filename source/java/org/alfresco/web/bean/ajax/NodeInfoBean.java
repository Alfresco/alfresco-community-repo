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
package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.repo.template.CropContentMethod;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.web.app.servlet.BaseTemplateContentServlet;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
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
   
   private static final Log logger = LogFactory.getLog(NodeInfoBean.class);
   
   /**
    * Returns information on the node identified by the 'noderef'
    * parameter found in the ExternalContext. If no noderef is supplied, then the template
    * is executed without context.
    * <p>
    * The result is the formatted HTML to show on the client.
    */
   public void sendNodeInfo() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      
      Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
      String strNodeRef = (String)requestMap.get("noderef");
      String strTemplate = (String)requestMap.get("template");
      if (strTemplate == null || strTemplate.length() == 0)
      {
         strTemplate = "node_summary_panel.ftl";
      }
      
      NodeRef nodeRef = null;
      if (strNodeRef != null && strNodeRef.length() != 0)
      {
         nodeRef = new NodeRef(strNodeRef);
         if (this.nodeService.exists(nodeRef) == false)
         {
            out.write("<span class='errorMessage'>Node could not be found in the repository!</span>");
            return;
         }
      }
      Repository.getServiceRegistry(context).getTemplateService().processTemplate(
            "/alfresco/templates/client/" + strTemplate, getModel(nodeRef, requestMap), out);
   }

   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @param nodeService      The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   private Map<String, Object> getModel(NodeRef nodeRef, Map<String, String> requestMap)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      // create api methods
      model.put("date", new Date());
      model.put("cropContent", new CropContentMethod());
      model.put("url", new BaseTemplateContentServlet.URLHelper(
              context.getExternalContext().getRequestContextPath()));
      if (nodeRef != null)
      {
         model.put("node", new TemplateNode(
               nodeRef,
               Repository.getServiceRegistry(context),
               this.imageResolver));
      }
      
      // add URL arguments as a map called 'args' to the root of the model
      Map<String, String> args = new HashMap<String, String>(4, 1.0f);
      for (String name : requestMap.keySet())
      {
         args.put(name, requestMap.get(name));
      }
      model.put("args", args);    
      
      return model;
   }
   
   /** Template Image resolver helper */
   private TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
      public String resolveImagePathForName(String filename, FileTypeImageSize size)
      {
         return Utils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
      }
   };
}
