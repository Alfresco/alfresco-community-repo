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
package jsftest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

/**
 * JSF Managed Bean. Provides the backing for the userlist.jsp view. The view uses
 * the datatable control to bind to the List of User bean objects. Implements the
 * action events called by the view when the user clicks the Edit link or Add button.
 * 
 * @author kevinr
 */
public class UserListBean
{
   // ===========================================================================
   // Construction
   
   public UserListBean()
   {
      Calendar date = new GregorianCalendar(2002, 5, 10);
      m_users.add(new User("admin", "admin", "Administrator", new String[] {"admin","superuser"}, date.getTime()));
      date = new GregorianCalendar(2001, 7, 10);
      m_users.add(new User("kevinr", "kevinr", "Kevin Roast", new String[] {"admin","superuser","dev"}, date.getTime()));
      date = new GregorianCalendar(2003, 8, 15);
      m_users.add(new User("gavinc", "gavinc", "Gavin Cornwell", new String[] {"superuser","dev"}, date.getTime()));
      date = new GregorianCalendar(2003, 1, 1);
      m_users.add(new User("stever", "stever", "Steve Rigby", new String[] {"superuser","qa"}, date.getTime()));
   }
   
   
   // ===========================================================================
   // Bean methods
   
   public List getUsers()
   {
      return m_users;
   }
   
   public void setUsers(List<User> users)
   {
      m_users = users;
   }
   
   /**
    * Get the users list as a wrapped DataModel object
    * 
    * @return DataModel for use by the data-table components
    */
   public DataModel getUsersModel()
   {
      if (m_usersModel == null)
      {
         m_usersModel = new ListDataModel();
         m_usersModel.setWrappedData(m_users);
      }
      
      return m_usersModel;
   }
   
   public User getUser()
   {
      return m_currentUser;
   }
   
   public void setUser(User user)
   {
      m_currentUser = user;
   }
   
   /**
    * Get the isNewUser
    *
    * @return the isNewUser
    */
   public boolean getIsNewUser()
   {
      return m_isNewUser;
   }

   /**
    * Set the isNewUser
    *
    * @param isNewUser     the isNewUser
    */
   public void setIsNewUser(boolean isNewUser)
   {
      m_isNewUser = isNewUser;
   }
   
   /**
    * Get the rolesOutputText
    *
    * @return the rolesOutputText
    */
   public HtmlOutputText getRolesOutputText()
   {
      return m_rolesOutputText;
   }

   /**
    * Set the rolesOutputText
    *
    * @param rolesOutputText     the rolesOutputText component
    */
   public void setRolesOutputText(HtmlOutputText rolesOutputText)
   {
      m_rolesOutputText = rolesOutputText;
   }

   
   
   // ===========================================================================
   // Action event methods
   
   /**
    * Edit user action event listener
    * 
    * Specified directly on the appropriate tag such as commandLink or commandButton
    * e.g. actionListener="#{UserListBean.editUser}"
    * 
    * This listener cannot directly affect the navigation of the page - the command
    * tag has an "action" attribute of which the default handler will use the outcome
    * from the faces-config.xml by default or call a specifid controller method
    * returning the String outcome as usual.
    */
   public void editUser(ActionEvent event)
   {
      s_logger.debug("*****USERLIST: " + ((UIParameter)event.getComponent().findComponent("userId")).getValue().toString());
      
      // Get the username from the "param" tag component we added as a nested tag
      // to the command tag that fired this event.
      // So we can have a key to work out which item was clicked in the data table
      String usernameId = ((UIParameter)event.getComponent().findComponent("userId")).getValue().toString();
      
      // It is also possible to get the relevent row from the DataModel we created
      // wrapping our users list. But this is a weak solution as models which then
      // potentially sort or page data may not provide the correct row index.
      // e.g.
      //    m_usersModel.getWrappedData().get(m_usersModel.getRowIndex());
      
      for (Iterator i=m_users.iterator(); i.hasNext(); /**/)
      {
         User user = (User)i.next();
         if (user.getUsername().equals(usernameId))
         {
            // set the user as current so we know which one to edit etc.
            try
            {
               setUser((User)user.clone());
               setIsNewUser(false);
            }
            catch (CloneNotSupportedException e)
            {
               // will not happen - clone is supported for our own types
            }
         }
      }
   }
   
   /**
    * OK button action handler
    * 
    * @return outcome view name
    */
   public void editUserOK(ActionEvent event)
   {
      s_logger.debug("*****USERLIST: persisting user: " + getUser().getUsername());
      for (int i=0; i<m_users.size(); i++)
      {
         User user = (User)m_users.get(i);
         if (user.getUsername().equals(getUser().getUsername()))
         {
            // found modified user - persist changes
            m_users.set(i, getUser());
            m_usersModel = null;
            break;
         }
      }
   }
   
   /**
    * Add user action event listener
    */
   public void addUser(ActionEvent event)
   {
      // create a blank user template
      setUser(new User());
      setIsNewUser(true);
   }
   
   /**
    * OK button action handler
    * 
    * @return outcome view name
    */
   public void addUserOK(ActionEvent event)
   {
      // persist new user details
      s_logger.debug("*****USERLIST: creating user: " + getUser().getUsername());
      m_users.add(getUser());
      m_usersModel = null;
   }
   
   /**
    * Example of a value changed event handler
    * NOTE: Value changed events do not submit the form directly, either a command
    *       button or link submits the form or can be done manually with Javascript
    */
   public void roleValueChanged(ValueChangeEvent event)
   {
      s_logger.debug("*****USERLIST: Value change from: " + event.getOldValue() + " to: " + event.getNewValue());
      
      // example of the use of a direct component binding
      // in the JSP page, a outputText tag has used binding='beanmethod' so we
      // can now programatically modify the component as required
      if (m_rolesOutputText != null)
      {
         m_rolesOutputText.setValue(getUser().getRoles().toString());
      }
      
      // An alternative to using the component binding would be to lookup the
      // component via it's component Id:
      //    HtmlOutputText comp = (HtmlOutputText)event.getComponent().findComponent("roles-text");
      //    comp.setValue(...);
      
      // The attributes of a component are all stored in a Map, the Map features
      // attribute-property transparency which means typed attributes can be get/set
      // directly without using casts as the appropriate getters/setters will be
      // called for you by the framework.
      //    comp.getAttributes().put("style", "color:red");
   }
   
   
   // ===========================================================================
   // Validator methods 
   
   /**
    * Example of a specific validation method. Required as the basic validator
    * child tags are not sufficient for anything beyond very simple length checks etc.
    */
   public void validateUsername(FacesContext context, UIComponent component, Object value)
      throws ValidatorException
   {
      String username = (String)value;
      if (username.length() < 5 || username.length() > 12)
      {
         String err = "Username must be between 5 and 12 characters in length.";
         throw new ValidatorException(new FacesMessage(err));
      }
      else if (username.indexOf(' ') != -1 || username.indexOf('\t') != -1)
      {
         String err = "Username cannot contain space or whitespace characters.";
         throw new ValidatorException(new FacesMessage(err));
      }
   }
   
   
   // ===========================================================================
   // Private data
   
   private List<User> m_users = new ArrayList<User>();
   private DataModel m_usersModel = null;
   private User m_currentUser = null;
   private boolean m_isNewUser = false;
   
   /** the HTMLOutputText component */
   private HtmlOutputText m_rolesOutputText = null;
   
   protected final static Logger s_logger = Logger.getLogger(UserListBean.class);
}