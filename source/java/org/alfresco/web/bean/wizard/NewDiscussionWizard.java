/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.wizard;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean class used to create discussions for documents
 * 
 * @author gavinc
 */
public class NewDiscussionWizard extends NewTopicWizard
{
   private static final Log logger = LogFactory.getLog(NewDiscussionWizard.class);
   
   private NodeRef discussingNodeRef;

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#startWizard(javax.faces.event.ActionEvent)
    */
   @Override
   public void startWizard(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id == null || id.length() == 0)
      {
         throw new AlfrescoRuntimeException("startDiscussion called without an id");
      }
      
      FacesContext context = FacesContext.getCurrentInstance();
      UserTransaction tx = null;
      NodeRef forumNodeRef = null;
      
      try
      {
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         this.discussingNodeRef = new NodeRef(Repository.getStoreRef(), id);
         
         if (this.nodeService.hasAspect(this.discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE))
         {
            throw new AlfrescoRuntimeException("startDiscussion called for an object that already has a discussion!");
         }
         
         // add the discussable aspect
         this.nodeService.addAspect(this.discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
         
         // create a child forum space using the child association just introduced by
         // adding the discussable aspect
         String name = (String)this.nodeService.getProperty(this.discussingNodeRef, 
               ContentModel.PROP_NAME);
         String msg = Application.getMessage(FacesContext.getCurrentInstance(), "discussion_for");
         String forumName = MessageFormat.format(msg, new Object[] {name});
         
         Map<QName, Serializable> forumProps = new HashMap<QName, Serializable>(1);
         forumProps.put(ContentModel.PROP_NAME, forumName);
         ChildAssociationRef childRef = this.nodeService.createNode(this.discussingNodeRef, 
               ForumModel.ASSOC_DISCUSSION,
               QName.createQName(ForumModel.FORUMS_MODEL_URI, "discussion"), 
               ForumModel.TYPE_FORUM, forumProps);
         
         forumNodeRef = childRef.getChildRef();

         // apply the uifacets aspect
         Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);
         uiFacetsProps.put(ContentModel.PROP_ICON, "forum_large");
         this.nodeService.addAspect(forumNodeRef, ContentModel.ASPECT_UIFACETS, uiFacetsProps);
         
         if (logger.isDebugEnabled())
            logger.debug("created forum for content: " + forumNodeRef.toString());
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), e.getMessage()), e);
      }
      
      // finally setup the context for the forum we just created
      if (forumNodeRef != null)
      {
         this.browseBean.clickSpace(forumNodeRef);
         
         // now initialise the wizard and navigate to it
         super.startWizard(event);
         context.getApplication().getNavigationHandler().handleNavigation(context, null, "dialog:createDiscussion");
      }
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#cancel()
    */
   @Override
   public String cancel()
   {
      // if we cancel the creation of a discussion all the setup that was done 
      // when the wizard started needs to be undone i.e. removing the created forum
      // and the discussable aspect
      
      FacesContext context = FacesContext.getCurrentInstance();
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // remove the discussable aspect from the node we were going to discuss!
         this.nodeService.removeAspect(this.discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE);
         
         // delete the forum space created when the wizard started         
         this.browseBean.setActionSpace(this.navigator.getCurrentNode());
         this.browseBean.deleteSpaceOK();
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), e.getMessage()), e);
      }
      
      // do cancel processing
      super.cancel();
      
      // as we are cancelling the creation of a discussion we know we need to go back
      // to the browse screen, this also makes sure we don't end up in the forum that
      // just got deleted!
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
             AlfrescoNavigationHandler.DIALOG_SEPARATOR + "browse";
   }
   
   
}
