/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.users;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeleteUserDialog extends BaseDialogBean
{

   private static final long serialVersionUID = -4977064287365766306L;

   private static Log logger = LogFactory.getLog(DeleteUserDialog.class);

   private static final String ERROR_DELETE = "error_delete_user";

   private static final String BUTTON_YES = "yes";

   private static final String MSG_TITLE_DELETE_USER = "title_delete_user";

   private static final String BUTTON_NO = "no";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

   transient private AuthenticationService authenticationService;

   private List<Node> users = Collections.<Node> emptyList();

   transient private PersonService personService;

   private Node person = null;

   private String searchCriteria = null;

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      try
      {
         String userName = (String) getPerson().getProperties().get("userName");

         // delete the associated Person
         getPersonService().deletePerson(userName);

         // re-do the search to refresh the list
         search();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, ERROR_DELETE), e.getMessage()), e);
         ReportedException.throwIfNecessary(e);
      }

      return outcome;
   }

   public String search()
   {

      if (this.searchCriteria == null || this.searchCriteria.length() == 0)
      {
         this.users = Collections.<Node> emptyList();
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
            String query = "( TYPE:\"{http://www.alfresco.org/model/content/1.0}person\") AND " + "((@\\{http\\://www.alfresco.org/model/content/1.0\\}firstName:" + search
                  + "*) OR (@\\{http\\://www.alfresco.org/model/content/1.0\\}lastName:" + search + "*) OR (@\\{http\\://www.alfresco.org/model/content/1.0\\}userName:" + search
                  + "*)))";

            if (logger.isDebugEnabled())
               logger.debug("Query: " + query);

            // define the search parameters
            SearchParameters params = new SearchParameters();
            params.setLanguage(SearchService.LANGUAGE_LUCENE);
            params.addStore(Repository.getStoreRef());
            params.setQuery(query);
            
            ResultSet results = this.getSearchService().query(params);
            List<NodeRef> people;
            try
            {
               people = results.getNodeRefs();
            }
            finally
            {
               results.close();
            }
            
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
               props.put("fullName", ((String) props.get("firstName")) + ' ' + ((String) props.get("lastName")));
               NodeRef homeFolderNodeRef = (NodeRef) props.get("homeFolder");
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
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, Repository.ERROR_NODEREF), new Object[] { "root" }));
            this.users = Collections.<Node> emptyList();
            try
            {
               if (tx != null)
               {
                  tx.rollback();
               }
            }
            catch (Exception tex)
            {
            }
         }
         catch (Exception err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, Repository.ERROR_GENERIC), err.getMessage()), err);
            this.users = Collections.<Node> emptyList();
            try
            {
               if (tx != null)
               {
                  tx.rollback();
               }
            }
            catch (Exception tex)
            {
            }
         }
      }

      // return null to stay on the same page
      return null;
   }

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
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] { id }));
         }
      }
      else
      {
         setPerson(null);
      }
   }

   /**
    *@return authenticationService
    */
   public AuthenticationService getAuthenticationService()
   {
    //check for null for cluster environment
      if (authenticationService == null)
      {
         authenticationService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthenticationService();
      }
      return authenticationService;
   }

   public void setAuthenticationService(AuthenticationService authenticationService)
   {
      this.authenticationService = authenticationService;
   }

   public PersonService getPersonService()
   {
      if (personService == null)
      {
         personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService();
      }
      return personService;
   }

   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }

   public Node getPerson()
   {
      return person;
   }

   public void setPerson(Node person)
   {
      this.person = person;
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), BUTTON_NO);
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), BUTTON_YES);
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_TITLE_DELETE_USER) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
               + getPerson().getProperties().get("userName") + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
}
