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
package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.executer.CopyToWebProjectActionExecuter;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.WebProject;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler implementation for the "copy-to-web-project" action.
 * 
 * @author gavinc
 */
public class CopyToWebProjectHandler extends BaseActionHandler
{
   private static final long serialVersionUID = -6725139406646296868L;

   public String getJSPPath()
   {
      return getJSPPath(CopyToWebProjectActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // get the destination selected by the user
      NodeRef destNodeRef = (NodeRef)actionProps.get(PROP_DESTINATION);
      
      // if the destination is a workspace node the use the root of the
      // webapp for the web project
      if (destNodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
      {
         WebProject webProject = new WebProject(destNodeRef);
         String storeName = AVMUtil.buildUserMainStoreName(webProject.getStoreId(), 
                  Application.getCurrentUser(FacesContext.getCurrentInstance()).getUserName());
         
         String rootPath = AVMUtil.buildStoreWebappPath(storeName, AVMUtil.DIR_ROOT);
         destNodeRef = AVMNodeConverter.ToNodeRef(-1, rootPath);
      }
      
      // setup the destination parameter
      repoProps.put(CopyToWebProjectActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      NodeRef destNodeRef = (NodeRef)repoProps.get(CopyToWebProjectActionExecuter.PARAM_DESTINATION_FOLDER);
      actionProps.put(PROP_DESTINATION, destNodeRef);
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      NodeRef dest = (NodeRef)actionProps.get(PROP_DESTINATION);
      
      String folder = "/";
      String webProject = "?";
      
      if (dest.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
      {
         // get the destination path
         Pair<Integer, String> avmNode = AVMNodeConverter.ToAVMVersionPath(dest);
         String avmPath = avmNode.getSecond();
         folder = avmPath.substring(avmPath.indexOf(AVMUtil.DIR_ROOT)+4);
         
         // get the destination web project name
         NodeRef webProjectNode = AVMUtil.getWebProjectNodeFromPath(avmPath);
         webProject = Repository.getNameForNode(
                  Repository.getServiceRegistry(context).getNodeService(), webProjectNode);
      }
      else if (dest.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
      {
         // get the destination web project name
         webProject = Repository.getNameForNode(
                  Repository.getServiceRegistry(context).getNodeService(), dest);
      }
      
      return MessageFormat.format(Application.getMessage(context, "action_copy_to_web_project_folder"),
            new Object[] {folder, webProject});
   }

}
