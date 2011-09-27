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
package org.alfresco.web.bean.spaces;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.DeleteContentDialog;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Delete Space" dialog
 * 
 * @author gavinc
 */
public class DeleteSpaceDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 5960844637376808571L;

   private static final Log logger = LogFactory.getLog(DeleteContentDialog.class);
   
   private static final String DELETE_ALL = "all";
   private static final String DELETE_FILES = "files";
   private static final String DELETE_FOLDERS = "folders";
   private static final String DELETE_CONTENTS = "contents";
   
   private String deleteMode = DELETE_ALL; 
   private boolean executeRules = true;
   private boolean archiveNodes = true;
   
   protected boolean hasMultipleParents = false;


   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.hasMultipleParents = false;
      
      String strHasMultipleParents = this.parameters.get("hasMultipleParents");
      if (strHasMultipleParents != null && "true".equals(strHasMultipleParents))
      {
         this.hasMultipleParents = true;
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      final boolean isAdmin = this.navigator.getCurrentUser().isAdmin();
      // get the space to delete
      Node node = this.browseBean.getActionSpace();
      if (node != null)
      {
         // force cache of name property so we can use it after the delete
         node.getName();
         
         if (logger.isDebugEnabled())
            logger.debug("Trying to delete space: " + node.getId() + " using delete mode: " + this.deleteMode);
         
         try
         {
            if (isAdmin && !this.executeRules)
            {
               Repository.getServiceRegistry(context).getRuleService().disableRules();
            }
            if (DELETE_ALL.equals(this.deleteMode))
            {
               NodeRef nodeRef = node.getNodeRef();
               // Check the node still exists
               if (this.getNodeService().exists(nodeRef))
               {
                  if (isAdmin && !this.archiveNodes)
                  {
                     this.getNodeService().addAspect(node.getNodeRef(), ContentModel.ASPECT_TEMPORARY, null);
                  }
                  
                  // ensure the node still exists before deleting
                  if (this.getNodeService().exists(node.getNodeRef()))
                  {
                      this.getNodeService().deleteNode(node.getNodeRef());
                  }
               }
            }
            else
            {
               List<ChildAssociationRef> childRefs = this.getNodeService().getChildAssocs(node.getNodeRef(), 
                     ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
               List<NodeRef> deleteRefs = new ArrayList<NodeRef>(childRefs.size());
               for (ChildAssociationRef ref : childRefs)
               {
                  NodeRef nodeRef = ref.getChildRef();
                  
                  if (this.getNodeService().exists(nodeRef))
                  {
                     if (DELETE_CONTENTS.equals(this.deleteMode))
                     {
                        deleteRefs.add(nodeRef);
                     }
                     else
                     {
                        // find it's type so we can see if it's a node we are interested in
                        QName type = this.getNodeService().getType(nodeRef);
                        
                        // make sure the type is defined in the data dictionary
                        TypeDefinition typeDef = this.getDictionaryService().getType(type);
                        
                        if (typeDef != null)
                        {
                           if (DELETE_FOLDERS.equals(this.deleteMode))
                           {
                              // look for folder type
                              if (this.getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER) == true && 
                                  this.getDictionaryService().isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false)
                              {
                                 deleteRefs.add(nodeRef);
                              }
                           }
                           else if (DELETE_FILES.equals(this.deleteMode))
                           {
                              // look for content file type
                              if (this.getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT))
                              {
                                 deleteRefs.add(nodeRef);
                              }
                           }
                        }
                     }
                  }
               }
               
               // delete the list of refs
               TransactionService txService = Repository.getServiceRegistry(context).getTransactionService();
               for (NodeRef nodeRef : deleteRefs)
               {
                  UserTransaction tx = null;
         
                  try
                  {
                     tx = txService.getNonPropagatingUserTransaction();
                     tx.begin();
                     
                     if (isAdmin && !this.archiveNodes)
                     {
                        this.getNodeService().addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                     }
                     
                     // ensure the node still exists before deleting
                     if (this.getNodeService().exists(node.getNodeRef()))
                     {
                         this.getNodeService().deleteNode(nodeRef);
                     }
                     
                     tx.commit();
                  }
                  catch (Throwable err)
                  {
                     try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
                  }
               }
            }
         }
         finally
         {
            if (isAdmin && !this.executeRules)
            {
               Repository.getServiceRegistry(context).getRuleService().enableRules();
            }
         }
      }
      else
      {
         logger.warn("WARNING: delete called without a current Space!");
      }
      
      return outcome;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      Node node = this.browseBean.getActionSpace();
      
      if (node != null && this.getNodeService().exists(node.getNodeRef()) == false)
      {
         // remove this node from the breadcrumb if required
         this.browseBean.removeSpaceFromBreadcrumb(node);
         
         // clear action context
         this.browseBean.setActionSpace(null);
         
         // setting the outcome will show the browse view again
         return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
                AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browse";
      }
      else
      {
         return outcome;
      }
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_delete_space";
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
   protected String getConfirmMessageId()
   {
      return "delete_space_confirm";
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Returns the confirmation to display to the user before deleting the content.
    * 
    * @return The formatted message to display
    */
   public String getConfirmMessage()
   {
      String fileConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(), 
               getConfirmMessageId());
      
      Node node = this.browseBean.getActionSpace();
      if (node != null)
      {
         return MessageFormat.format(fileConfirmMsg, new Object[] {node.getName()});
      }
      else
      {
         return Application.getMessage(FacesContext.getCurrentInstance(), 
                  "delete_node_not_found");
      }
   }
   
   /**
    * @return Returns the delete operation mode.
    */
   public String getDeleteMode()
   {
      return this.deleteMode;
   }
   
   /**
    * @param deleteMode The delete operation mode to set.
    */
   public void setDeleteMode(String deleteMode)
   {
      this.deleteMode = deleteMode;
   }

   /**
    * @return true if the space has multiple parents
    */
   public boolean getHasMultipleParents()
   {
      return this.hasMultipleParents;
   }

   /**
    * @return true to execute rules during delete
    */
   public boolean getExecuteRules()
   {
      return this.executeRules;
   }
   
   /**
    * @param executeRules execute rules during delete
    */
   public void setExecuteRules(boolean executeRules)
   {
      this.executeRules = executeRules;
   }

   /**
    * @return true to archive nodes during delete
    */
   public boolean getArchiveNodes()
   {
      return this.archiveNodes;
   }

   /**
    * @param archiveNodes archive nodes during delete
    */
   public void setArchiveNodes(boolean archiveNodes)
   {
      this.archiveNodes = archiveNodes;
   }
}
