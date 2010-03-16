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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMRevertToVersionAction;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.data.UIRichList;

/**
 * Backing bean for File Details page.
 * 
 * @author Kevin Roast
 */
public class FileDetailsBean extends AVMDetailsBean
{
   private static final long serialVersionUID = -3263315503769148385L;
   
   /** Action service bean reference */
   transient private ActionService actionService;
   
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   
   // ------------------------------------------------------------------------------
   // Construction 
   
   /**
    * Default constructor
    */
   public FileDetailsBean()
   {
      super();
      
      // initial state of some panels that don't use the default
      panels.put("version-history-panel", false);
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @param actionService    The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }
   
   private ActionService getActionService()
   {
      if (actionService == null)
      {
         actionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getActionService();
      }
      return actionService;
   }
   
   /**
    * @see org.alfresco.web.bean.wcm.AVMDetailsBean#getAvmNode()
    */
   @Override
   public AVMNode getAvmNode()
   {
      return this.avmBrowseBean.getAvmActionNode();
   }
   
   /**
    * @return a Node wrapper of the AVM File Node - for property sheet support
    */
   public Node getDocument()
   {
      return new Node(getAvmNode().getNodeRef());
   }
   
   /**
    * Returns the URL to the content for the current document
    *  
    * @return Content url to the current document
    */
   public String getBrowserUrl()
   {
      return DownloadContentServlet.generateBrowserURL(getAvmNode().getNodeRef(), getAvmNode().getName());
   }

   /**
    * Returns the download URL to the content for the current document
    *  
    * @return Download url to the current document
    */
   public String getDownloadUrl()
   {
      return DownloadContentServlet.generateDownloadURL(getAvmNode().getNodeRef(), getAvmNode().getName());
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
    * @return The 32x32 filetype icon for the file
    */
   public String getFileType32()
   {
      return FileTypeImageUtils.getFileTypeImage(getAvmNode().getName(), false);
   }

   /**
    * @see org.alfresco.web.bean.wcm.AVMDetailsBean#getNodes()
    */
   @Override
   protected List<AVMNode> getNodes()
   {
      return (List)this.avmBrowseBean.getFiles();
   }
   
   /**
    * @return version history list for a node
    */
   public List<Map<String, Object>> getVersionHistory()
   {
      AVMNode avmNode = getAvmNode();
      List<AVMNodeDescriptor> history = this.getAvmService().getHistory(avmNode.getDescriptor(), -1);
      List<Map<String, Object>> wrappers = new ArrayList<Map<String, Object>>(history.size());
      for (AVMNodeDescriptor record : history)
      {
         Map<String, Object> wrapper = new HashMap<String, Object>(8, 1.0f);
         
         wrapper.put("version", record.getVersionID());
         wrapper.put("strVersion", Integer.toString(record.getVersionID()));
         wrapper.put("modifiedDate", new Date(record.getModDate()));
         Pair<Integer, String> path = this.getAvmService().getAPath(record);
         if (path != null)
         {
            wrapper.put("url", DownloadContentServlet.generateBrowserURL(
                        AVMNodeConverter.ToNodeRef(path.getFirst(), path.getSecond()), avmNode.getName()));
         }
         wrapper.put("fileType16", FileTypeImageUtils.getFileTypeImage(avmNode.getName(), true));
         
         wrappers.add(wrapper);
      }
      return wrappers;
   }
   
   /**
    * Revert a node back to a previous version
    */
   public void revertNode(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      int version = Integer.parseInt(params.get("version"));
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, false);
         tx.begin();
         
         Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
         List<AVMNodeDescriptor> history = this.getAvmService().getHistory(getAvmNode().getDescriptor(), -1);
         // the history list should contain the version ID we are looking for
         for (AVMNodeDescriptor record : history)
         {
            if (record.getVersionID() == version)
            {
               // the action expects the HEAD revision as the noderef and
               // the to-revert param as the previous version to revert to
               Action action = this.getActionService().createAction(AVMRevertToVersionAction.NAME, args);
               args.put(AVMRevertToVersionAction.TOREVERT, record);
               this.getActionService().executeAction(action, getAvmNode().getNodeRef());
               
               // clear the version history list after a revert ready for refresh
               UIRichList versionList = (UIRichList)link.findComponent("version-history-list");
               versionList.setValue(null);
               
               // reset the action node reference as the version ID has changed
               avmBrowseBean.setAvmActionNode(new AVMNode(getAvmService().lookup(-1, getAvmNode().getPath())));
               break;
            }
         }
         
         tx.commit();
      }
      catch (Throwable err)
      {
         err.printStackTrace(System.err);
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
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
      if ( getAvmNode() != null && AVMUtil.isWorkflowStore(AVMUtil.getStoreName(getAvmNode().getPath())) )
      {
          return "dialog:close:dialog:workflowShowFileDetails";
      }
      else
      {
          return "dialog:close:dialog:showFileDetails";
      }
   }
}
