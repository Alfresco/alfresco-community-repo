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
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.apache.log4j.Logger;

/**
 * @author Kevin Roast
 */
public class UsersBean implements IContextListener
{
   private static Logger logger = Logger.getLogger(UsersBean.class);

   public static final String ERROR_PASSWORD_MATCH = "error_password_match";
   private static final String ERROR_DELETE = "error_delete_user";
   private static final String ERROR_USER_DELETE = "error_delete_user_object";
   
   private static final String DEFAULT_OUTCOME = "manageUsers";
   private static final String DIALOG_CLOSE = "dialog:close";

   /** NodeService bean reference */
   protected NodeService nodeService;

   /** SearchService bean reference */
   protected SearchService searchService;
   
   /** AuthenticationService bean reference */
   protected AuthenticationService authenticationService;

   /** PersonService bean reference */
   protected PersonService personService;
   
   /** Component reference for Users RichList control */
   protected UIRichList usersRichList;

   /** action context */
   private Node person = null;
   
   private List<Node> users = Collections.<Node>emptyList();
   
   private String password = null;
   private String oldPassword = null;
   private String confirm = null;
   private String searchCriteria = null;
   
   
   // ------------------------------------------------------------------------------
   // Construction

   /**
    * Default Constructor
    */
   public UsersBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }

   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters

   /**
    * @param nodeService        The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param searchService      the search service
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }

   /**
    * @param authenticationService  The AuthenticationService to set.
    */
   public void setAuthenticationService(AuthenticationService authenticationService)
   {
      this.authenticationService = authenticationService;
   }

   /**
    * @param personService  The PersonService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }

   /**
    * @return Returns the usersRichList.
    */
   public UIRichList getUsersRichList()
   {
      return this.usersRichList;
   }

   /**
    * @param usersRichList  The usersRichList to set.
    */
   public void setUsersRichList(UIRichList usersRichList)
   {
      this.usersRichList = usersRichList;
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
   
   /**
    * @return Returns the search criteria
    */
   public String getSearchCriteria()
   {
      return searchCriteria;
   }

   /**
    * @param searchCriteria The search criteria to select
    */
   public void setSearchCriteria(String searchCriteria)
   {
      this.searchCriteria = searchCriteria;
   }

   /**
    * @return Returns the confirm password.
    */
   public String getConfirm()
   {
      return this.confirm;
   }

   /**
    * @param confirm The confirm password to set.
    */
   public void setConfirm(String confirm)
   {
      this.confirm = confirm;
   }

   /**
    * @return Returns the password.
    */
   public String getPassword()
   {
      return this.password;
   }

   /**
    * @param password The password to set.
    */
   public void setPassword(String password)
   {
      this.password = password;
   }
   
   /**
    * @return Returns the old password.
    */
   public String getOldPassword()
   {
      return this.oldPassword;
   }

   /**
    * @param oldPassword The old password to set.
    */
   public void setOldPassword(String oldPassword)
   {
      this.oldPassword = oldPassword;
   }

   /**
    * @return Returns the person context.
    */
   public Node getPerson()
   {
      return this.person;
   }

   /**
    * @param person     The person context to set.
    */
   public void setPerson(Node person)
   {
      this.person = person;
   }

   /**
    * Action event called by all actions that need to setup a Person context on
    * the Users bean before an action page is called. The context will be a
    * Person Node in setPerson() which can be retrieved on the action page from
    * UsersBean.getPerson().
    */
   public void setupUserAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink) event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Setup for action, setting current Person to: " + id);

         try
         {
            // create the node ref, then our node representation
            NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
            Node node = new Node(ref);
            
            // remember the Person node
            setPerson(node);
            
            // clear the UI state in preparation for finishing the action
            // and returning to the main page
            contextUpdated();
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext
                  .getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] { id }));
         }
      }
      else
      {
         setPerson(null);
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
         String userName = (String)getPerson().getProperties().get("userName");
         
         // we only delete the user auth if Alfresco is managing the authentication 
         Map session = context.getExternalContext().getSessionMap();
         if (session.get(LoginBean.LOGIN_EXTERNAL_AUTH) == null)
         {
            // delete the User authentication
            try
            {
               authenticationService.deleteAuthentication(userName);
            }
            catch (AuthenticationException authErr)
            {
               Utils.addErrorMessage(Application.getMessage(context, ERROR_USER_DELETE));
            }
         }
         
         // delete the associated Person
         this.personService.deletePerson(userName);
         
         // re-do the search to refresh the list
         search();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context,
               ERROR_DELETE), e.getMessage()), e);
      }
      
      return DIALOG_CLOSE;
   }
   
   /**
    * Action handler called for OK button press on Change Password screen
    */
   public String changePasswordOK()
   {
      String outcome = DIALOG_CLOSE;
      
      if (this.password != null && this.confirm != null && this.password.equals(this.confirm))
      {
         try
         {
            String userName = (String)this.person.getProperties().get(ContentModel.PROP_USERNAME);
            this.authenticationService.setAuthentication(userName, this.password.toCharArray());
         }
         catch (Exception e)
         {
            outcome = null;
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext
                  .getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
         }
      }
      else
      {
         outcome = null;
         Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(),
               ERROR_PASSWORD_MATCH));
      }
      
      return outcome;
   }
   
   /**
    * Action handler called for OK button press on Change My Password screen
    * For this screen the user is required to enter their old password - effectively login.
    */
   public String changeMyPasswordOK()
   {
      String outcome = DIALOG_CLOSE;
      
      if (this.password != null && this.confirm != null && this.password.equals(this.confirm))
      {
         try
         {
            String userName = (String)this.person.getProperties().get(ContentModel.PROP_USERNAME);
            this.authenticationService.updateAuthentication(userName, this.oldPassword.toCharArray(), this.password.toCharArray());
         }
         catch (Exception e)
         {
            outcome = null;
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext
                  .getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
         }
      }
      else
      {
         outcome = null;
         Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(),
               ERROR_PASSWORD_MATCH));
      }
      
      return outcome;
   }
   
   /**
    * Action handler called for the OK button press 
    */
   public String changeUserDetails()
   {
      String outcome = DIALOG_CLOSE;
      
      FacesContext context = FacesContext.getCurrentInstance();
      try
      {
         Map<QName, Serializable> props = this.nodeService.getProperties(getPerson().getNodeRef());
         props.put(ContentModel.PROP_FIRSTNAME,
               (String)getPerson().getProperties().get(ContentModel.PROP_FIRSTNAME));
         props.put(ContentModel.PROP_LASTNAME,
               (String)getPerson().getProperties().get(ContentModel.PROP_LASTNAME));
         props.put(ContentModel.PROP_EMAIL,
               (String)getPerson().getProperties().get(ContentModel.PROP_EMAIL));
         
         // persist changes
         this.nodeService.setProperties(getPerson().getNodeRef(), props);
         
         // if the above call was successful, then reset Person Node in the session
         Application.getCurrentUser(context).reset();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), err.getMessage()), err );
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
      this.usersRichList.setValue(null);
      
      if (this.searchCriteria == null || this.searchCriteria.length() == 0)
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
            String search = ISO9075.encode(this.searchCriteria);
            String query = "( TYPE:\"{http://www.alfresco.org/model/content/1.0}person\") AND " + 
                           "((@\\{http\\://www.alfresco.org/model/content/1.0\\}firstName:" + search + 
                           "*) OR (@\\{http\\://www.alfresco.org/model/content/1.0\\}lastName:" + search + 
                           "*) OR (@\\{http\\://www.alfresco.org/model/content/1.0\\}userName:" + search + 
                           "*)))";
            
            if (logger.isDebugEnabled())
               logger.debug("Query: " + query);
   
            // define the search parameters
            SearchParameters params = new SearchParameters();
            params.setLanguage(SearchService.LANGUAGE_LUCENE);
            params.addStore(Repository.getStoreRef());
            params.setQuery(query);
            
            List<NodeRef> people = this.searchService.query(params).getNodeRefs();
            
            if (logger.isDebugEnabled())
               logger.debug("Found " + people.size() + " users");
            
            this.users = new ArrayList<Node>(people.size());
            
            for (NodeRef nodeRef : people)
            {
               // create our Node representation
               MapNode node = new MapNode(nodeRef);
               
               // set data binding properties
               // this will also force initialisation of the props now during the UserTransaction
               // it is much better for performance to do this now rather than during page bind
               Map<String, Object> props = node.getProperties(); 
               props.put("fullName", ((String)props.get("firstName")) + ' ' + ((String)props.get("lastName")));
               NodeRef homeFolderNodeRef = (NodeRef)props.get("homeFolder");
               if (homeFolderNodeRef != null)
               {
                  props.put("homeSpace", homeFolderNodeRef);
               }
               
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
   
   /**
    * Action handler to show all the users currently in the system
    * 
    * @return The outcome
    */
   public String showAll()
   {
      this.usersRichList.setValue(null);
      
      this.users = Repository.getUsers(FacesContext.getCurrentInstance(), 
            this.nodeService, this.searchService);
      
      // return null to stay on the same page
      return null;
   }
   
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation

   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      if (this.usersRichList != null)
      {
         this.usersRichList.setValue(null);
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
