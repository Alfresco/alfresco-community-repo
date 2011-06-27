/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.web.bean.users;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.dialog.ChangeViewSupport;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.UIModeList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class UsersDialog extends BaseDialogBean implements IContextListener, ChangeViewSupport
{
   private static final long serialVersionUID = 7613786955971599967L;

   private static Log    logger = LogFactory.getLog(UsersDialog.class);
   
   public static String BEAN_NAME = "UsersDialog";

   public static final String ERROR_PASSWORD_MATCH = "error_password_match";
   public static final String ERROR_NEGATIVE_QUOTA = "error_negative_quota";
   private static final String ERROR_DELETE = "error_delete_user";
   
   private static final String DIALOG_CLOSE = "dialog:close";
   
   private static final String VIEW_DETAILS = "user_details";
   private static final String LABEL_VIEW_DETAILS = "user_details";
   
   /** RichList view mode */
   protected String viewMode = VIEW_DETAILS;

   protected UsersBeanProperties properties;
   private List<Node> users = Collections.<Node>emptyList();
   
   
   // ------------------------------------------------------------------------------
   // Construction

   /**
    * Default Constructor
    */
   public UsersDialog()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }

   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters

   /**
    * @param properties the properties to set
    */
   public void setProperties(UsersBeanProperties properties)
   {
      this.properties = properties;
   }

   /**
    * @return the list of user Nodes to display
    */
   public List<Node> getUsers()
   {
      if (this.users == null)
      {
         search();
      }
      
      return this.users;
   }
   
   public int getUsersSize()
   {
      return getUsers().size();
   }
   
   public Long getUsersTotalUsage()
   {
       Long totalUsage = null;
       List<Node> users = getUsers();
       for(Node user : users)
       {
           Long sizeLatest = (Long)properties.getUserUsage((String)user.getProperties().get("userName"));
           if ((sizeLatest != null) && (sizeLatest != -1L))
           {
        	  if (totalUsage == null) { totalUsage = 0L; }
              totalUsage += sizeLatest;
           }
       }
       return totalUsage;
   }
   
   public Long getUsersTotalQuota()
   {
       Long totalQuota = null;
       List<Node> users = getUsers();
       for(Node user : users)
       {
           Long sizeCurrent = (Long)user.getProperties().get("sizeQuota");
           if ((sizeCurrent != null) && (sizeCurrent != -1L))
           {
        	   if (totalQuota == null) { totalQuota = 0L; }
               totalQuota += sizeCurrent;
           }
       }
       return totalQuota;
   }
   
   /**
    * Action event called by all actions that need to setup a Person context on
    * the Users bean before an action page is called. The context will be a
    * Person Node in setPerson() which can be retrieved on the action page from
    * UsersDialog.getPerson().
    */
   public void setupUserAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink) event.getComponent();
      Map<String, String> params = link.getParameterMap();
      setupUserAction(params.get("id"));
   }

   /**
    * Called in preparation for actions that need to setup a Person context on
    * the Users bean before an action page is called. 
    * 
    * @param personId
    */
   public void setupUserAction(String personId)
   {
      if (personId != null && personId.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Setup for action, setting current Person to: " + personId);

         try
         {
            // create the node ref, then our node representation
            NodeRef ref = new NodeRef(Repository.getStoreRef(), personId);
            Node node = new Node(ref);
            
            // remember the Person node
            properties.setPerson(node);
            
            // clear the UI state in preparation for finishing the action
            // and returning to the main page
            contextUpdated();
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext
                  .getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] { personId }));
         }
      }
      else
      {
         properties.setPerson(null);
      }
   }

   /**
    * Action handler called when the OK button is clicked on the Delete User page
    */
   public String deleteOK()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      try
      {
         String userName = (String) properties.getPerson().getProperties().get("userName");
                  
         // delete the associated Person
         properties.getPersonService().deletePerson(userName);
         
         // re-do the search to refresh the list
         search();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context,
               ERROR_DELETE), e.getMessage()), e);
         ReportedException.throwIfNecessary(e);
      }
      
      return DIALOG_CLOSE;
   }
   
   /**
    * Action handler called for the OK button press
    */
   public String changeUserDetails()
   {
      String outcome = DIALOG_CLOSE;
      
      FacesContext context = FacesContext.getCurrentInstance();
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context);
         tx.begin();

         Map<QName, Serializable> props = properties.getNodeService().getProperties(properties.getPerson().getNodeRef());
         props.put(ContentModel.PROP_FIRSTNAME,
               (String) properties.getPerson().getProperties().get(ContentModel.PROP_FIRSTNAME));
         props.put(ContentModel.PROP_LASTNAME,
               (String) properties.getPerson().getProperties().get(ContentModel.PROP_LASTNAME));
         props.put(ContentModel.PROP_EMAIL,
               (String) properties.getPerson().getProperties().get(ContentModel.PROP_EMAIL));
         
         // persist changes
         properties.getNodeService().setProperties(properties.getPerson().getNodeRef(), props);
         
         tx.commit();
         
         // if the above call was successful, then reset Person Node in the session
         Application.getCurrentUser(context).reset();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), err.getMessage()), err );
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return outcome;
   }

   /**
    * Event handler called when the user wishes to search for a user
    * 
    * @return The outcome
    */
   public String search()
   {
      properties.getUsersRichList().setValue(null);
      
      if (properties.getSearchCriteria() == null || properties.getSearchCriteria().trim().length() == 0)
      {
         this.users = Collections.<Node>emptyList();
      }
      else
      {
         FacesContext context = FacesContext.getCurrentInstance();
         UserTransaction tx = null;
         
         try
         {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // define the query to find people by their first or last name
            String search = properties.getSearchCriteria();
            if (logger.isDebugEnabled())
            {
               logger.debug("Query filter: " + search);
            }
            
            List<PersonInfo> persons = properties.getPersonService().getPeople(
                  Utils.generatePersonFilter(search),
                  true,
                  Utils.generatePersonSort(),
                  new PagingRequest(Utils.getPersonMaxResults(), null)
            ).getPage();
            
            if (logger.isDebugEnabled())
            {
               logger.debug("Found " + persons.size() + " users");
            }
            
            this.users = new ArrayList<Node>(persons.size());
            
            for (PersonInfo person : persons)
            {
               // create our Node representation
               MapNode node = new MapNode(person.getNodeRef());
               
               // set data binding properties
               // this will also force initialisation of the props now during the UserTransaction
               // it is much better for performance to do this now rather than during page bind
               Map<String, Object> props = node.getProperties(); 
               String firstName = (String)props.get("firstName");
               String lastName = (String)props.get("lastName");
               props.put("fullName", (firstName != null ? firstName : "") + ' ' + (lastName != null ? lastName : ""));
               NodeRef homeFolderNodeRef = (NodeRef)props.get("homeFolder");
               if (homeFolderNodeRef != null)
               {
                  props.put("homeSpace", homeFolderNodeRef);
               }
               
               node.addPropertyResolver("sizeLatest", this.resolverUserSizeLatest);
               node.addPropertyResolver("quota", this.resolverUserQuota);
               
               this.users.add(node);
            }
            
            // commit the transaction
            tx.commit();
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  context, Repository.ERROR_NODEREF), new Object[] {"root"}) );
            this.users = Collections.<Node>emptyList();
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
         catch (Exception err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  context, Repository.ERROR_GENERIC), err.getMessage()), err );
            this.users = Collections.<Node>emptyList();
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
      
      // return null to stay on the same page
      return null;
   }
   
   public NodePropertyResolver resolverUserSizeLatest = new NodePropertyResolver() {
      public Object get(Node personNode) {
         return (Long)properties.getUserUsage((String)personNode.getProperties().get("userName"));
      }
   };
   
   public NodePropertyResolver resolverUserQuota = new NodePropertyResolver() {
      public Object get(Node personNode) {
         Long quota = (Long)personNode.getProperties().get("sizeQuota");
         return (quota != null && quota != -1L) ? quota : null;
      }
   };

   public NodePropertyResolver resolverUserMutable = new NodePropertyResolver()
   {
      public Object get(Node personNode)
      {
         return properties.getAuthenticationService().isAuthenticationMutable(
               (String) personNode.getProperties().get("userName"));
      }
   };
   
   /**
    * Action handler to show all the users currently in the system
    * 
    * @return The outcome
    */
   public String showAll()
   {
      properties.getUsersRichList().setValue(null);
      
      this.users = Repository.getUsers(FacesContext.getCurrentInstance(), 
            properties.getNodeService(), properties.getPersonService());
      
      for (Node node : this.users)
      {
         node.addPropertyResolver("sizeLatest", this.resolverUserSizeLatest);
         node.addPropertyResolver("quota", this.resolverUserQuota);
         node.addPropertyResolver("isMutable", this.resolverUserMutable);
      }
      
      // return null to stay on the same page
      return null;
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
   
   @Override
   public String cancel()
   {
      contextUpdated();
      
      return super.cancel();
   }
   
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   @Override
   public Object getActionsContext()
   {
      return this;
   }
   
   public List<UIListItem> getViewItems()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      List<UIListItem> items = new ArrayList<UIListItem>(1);
      
      UIListItem item1 = new UIListItem();
      item1.setValue(VIEW_DETAILS);
      item1.setLabel(Application.getMessage(context, LABEL_VIEW_DETAILS));
      items.add(item1);
      
      return items;
   }


   public String getViewMode()
   {
      return this.viewMode;
   }


   public void setViewMode(String viewMode)
   {
      this.viewMode = viewMode;
   }


   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      
      // update view mode from user selection
      setViewMode(viewList.getValue().toString());
   }
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation

   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      if (properties.getUsersRichList() != null)
      {
         properties.getUsersRichList().setValue(null);
         this.users = null;
      }
   }
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#areaChanged()
    */
   public void areaChanged()
   {
      // nothing to do
   }

   /**
    * @see org.alfresco.web.app.context.IContextListener#spaceChanged()
    */
   public void spaceChanged()
   {
      // nothing to do
   }
}
