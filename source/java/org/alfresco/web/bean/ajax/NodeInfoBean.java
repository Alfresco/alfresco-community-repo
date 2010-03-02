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
package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.content.transform.TransformerInfoException;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.web.app.servlet.BaseTemplateContentServlet;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean used by an AJAX control to send information back on the 
 * requested node.
 * 
 * @author gavinc
 */
public class NodeInfoBean implements Serializable
{
   private static final long serialVersionUID = 137294178658919187L;

   transient private NodeService nodeService;
   
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
         if (this.getNodeService().exists(nodeRef) == false)
         {
            out.write("<span class='errorMessage'>Node could not be found in the repository!</span>");
            return;
         }
      }
      try
      {
          Repository.getServiceRegistry(context).getTemplateService().processTemplate(
            "/alfresco/templates/client/" + strTemplate, getModel(nodeRef, requestMap), out);
      }
      catch (TemplateException ex)
      {
         // Try to catch TransformerInfoException to display it in NodeInfo pane.
         // Fix bug reported in https://issues.alfresco.com/jira/browse/ETWOTWO-440
         Throwable cause = ex.getCause();
         while (cause != null)
         {
            cause = cause.getCause();
            if (cause instanceof TransformerInfoException)
            {
               out.write("<tr><td colspan=\"2\"><span class='errorMessage'>" + cause.getMessage() + "</span></td></tr>");
               return;
            }
         }
         
         throw ex;
      }
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
   
   private NodeService getNodeService()
   {
      if (nodeService == null)
      {
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return nodeService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   private Map<String, Object> getModel(NodeRef nodeRef, Map<String, String> requestMap) throws ContentIOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      I18NUtil.registerResourceBundle("alfresco.messages.webclient"); 
      // create api methods
      model.put("date", new Date());
      model.put("msg", new I18NMessageMethod());
      model.put("url", new BaseTemplateContentServlet.URLHelper(context));
      model.put("locale", I18NUtil.getLocale());
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
         return FileTypeImageUtils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
      }
   };
 
}
