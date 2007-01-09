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
   /** Action service bean reference */
   private ActionService actionService;
   
   
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
      return AVMConstants.buildAssetUrl(getAvmNode().getPath());
   }
   
   /**
    * @return The 32x32 filetype icon for the file
    */
   public String getFileType32()
   {
      return Utils.getFileTypeImage(getAvmNode().getName(), false);
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
      List<AVMNodeDescriptor> history = this.avmService.getHistory(avmNode.getDescriptor(), -1);
      List<Map<String, Object>> wrappers = new ArrayList<Map<String, Object>>(history.size());
      for (AVMNodeDescriptor record : history)
      {
         Map<String, Object> wrapper = new HashMap<String, Object>(8, 1.0f);
         
         wrapper.put("version", record.getVersionID());
         wrapper.put("strVersion", Integer.toString(record.getVersionID()));
         wrapper.put("modifiedDate", new Date(record.getModDate()));
         List<Pair<Integer, String>> paths = this.avmService.getPaths(record);
         if (paths.size() != 0)
         {
            // display the first path as any will show the same content
            Pair<Integer, String> path = paths.get(0);
            wrapper.put("url", DownloadContentServlet.generateBrowserURL(
                        AVMNodeConverter.ToNodeRef(path.getFirst(), path.getSecond()), avmNode.getName()));
         }
         wrapper.put("fileType16", Utils.getFileTypeImage(avmNode.getName(), true));
         
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
         List<AVMNodeDescriptor> history = this.avmService.getHistory(getAvmNode().getDescriptor(), -1);
         // the history list should contain the version ID we are looking for
         for (AVMNodeDescriptor record : history)
         {
            if (record.getVersionID() == version)
            {
               // the action expects the HEAD revision as the noderef and
               // the to-revert param as the previous version to revert to
               Action action = this.actionService.createAction(AVMRevertToVersionAction.NAME, args);
               args.put(AVMRevertToVersionAction.TOREVERT, record);
               this.actionService.executeAction(action, getAvmNode().getNodeRef());
               
               // clear the version history list after a revert ready for refresh
               UIRichList versionList = (UIRichList)link.findComponent("version-history-list");
               versionList.setValue(null);
               
               // reset the action node reference as the version ID has changed
               avmBrowseBean.setAvmActionNode(new AVMNode(avmService.lookup(-1, getAvmNode().getPath())));
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
}
