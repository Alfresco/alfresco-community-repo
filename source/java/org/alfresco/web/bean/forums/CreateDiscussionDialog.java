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
package org.alfresco.web.bean.forums;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Create Discusssion Dialog".
 * 
 * @author gavinc
 */
public class CreateDiscussionDialog extends CreateTopicDialog
{
   private static final long serialVersionUID = 3500493916528264014L;

   protected NodeRef discussingNodeRef;
   
   private static final Log logger = LogFactory.getLog(CreateDiscussionDialog.class);
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // get the id of the node we are creating the discussion for
      String id = parameters.get("id");
      if (id == null || id.length() == 0)
      {
         throw new AlfrescoRuntimeException("createDiscussion called without an id");
      }
      
      // create the topic to hold the discussions
      createTopic(id);
   }
   
   @Override
   public String cancel()
   {
      // if the user cancels the creation of a discussion all the setup that was done 
      // when the dialog started needs to be undone i.e. removing the created forum
      // and the discussable aspect
      deleteTopic();
      
      // as we are cancelling the creation of a discussion we know we need to go back
      // to the browse screen, this also makes sure we don't end up in the forum that
      // just got deleted!
      FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(
              AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);
      return getDefaultCancelOutcome();
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods

   /**
    * Creates a topic for the node with the given id
    * 
    * @param id The id of the node to discuss
    */
   protected void createTopic(final String id)
   {
      RetryingTransactionCallback<NodeRef> createTopicCallback = new RetryingTransactionCallback<NodeRef>()
      {
         public NodeRef execute() throws Throwable
         {
            NodeRef forumNodeRef = null;
            discussingNodeRef = new NodeRef(Repository.getStoreRef(), id);
            
            if (getNodeService().hasAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE))
            {
               throw new AlfrescoRuntimeException("createDiscussion called for an object that already has a discussion!");
            }
            
            // Add the discussable aspect
            getNodeService().addAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
            // The discussion aspect create the necessary child
            List<ChildAssociationRef> destChildren = getNodeService().getChildAssocs(
                  discussingNodeRef,
                  ForumModel.ASSOC_DISCUSSION,
                  RegexQNamePattern.MATCH_ALL);
            // Take the first one
            if (destChildren.size() == 0)
            {
               // Drop the aspect and recreate it.  This should not happen, but just in case ...
               getNodeService().removeAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE);
               getNodeService().addAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
               // The discussion aspect create the necessary child
               destChildren = getNodeService().getChildAssocs(
                     discussingNodeRef,
                     ForumModel.ASSOC_DISCUSSION,
                     RegexQNamePattern.MATCH_ALL);
            }
            if (destChildren.size() == 0)
            {
               throw new AlfrescoRuntimeException("The discussable aspect behaviour is not creating a topic");
            }
            else
            {
               // We just take the first one
               ChildAssociationRef discussionAssoc = destChildren.get(0);
               forumNodeRef = discussionAssoc.getChildRef();
            }
            
            if (logger.isDebugEnabled())
               logger.debug("created forum for content: " + discussingNodeRef.toString());
            
            return forumNodeRef;
         }
      };
      
      FacesContext context = FacesContext.getCurrentInstance();
      NodeRef forumNodeRef = null;
      try
      {
         forumNodeRef = getTransactionService().getRetryingTransactionHelper().doInTransaction(
               createTopicCallback, false);
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
         throw new AbortProcessingException("Invalid node reference");
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), e.getMessage()), e);
         ReportedException.throwIfNecessary(e);
      }
      // finally setup the context for the forum we just created
      if (forumNodeRef != null)
      {
         this.browseBean.clickSpace(forumNodeRef);
      }
   }
   
   /**
    * Deletes the setup performed during the initialisation of the dialog.
    */
   protected void deleteTopic()
   {
      RetryingTransactionCallback<Object> deleteTopicCallback = new RetryingTransactionCallback<Object>()
      {
         public Object execute() throws Throwable
         {
            // remove this node from the breadcrumb if required
            Node forumNode = navigator.getCurrentNode();
            browseBean.removeSpaceFromBreadcrumb(forumNode);
            
            // remove the discussable aspect from the node we were going to discuss!
            // AWC-1519: removing the aspect that defines the child association now does the 
            //           cascade delete so we no longer have to delete the child explicitly
            getNodeService().removeAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE);
            // Done
            return null;
         }
      };
      FacesContext context = FacesContext.getCurrentInstance();
      try
      {
         getTransactionService().getRetryingTransactionHelper().doInTransaction(deleteTopicCallback, false);
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), e.getMessage()), e);
         ReportedException.throwIfNecessary(e);
      }
   }
}
