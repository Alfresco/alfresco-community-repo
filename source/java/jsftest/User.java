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
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;


/**
 * Class representing a single User bean instance.
 * 
 * @author kevinr
 */
public class User implements Cloneable
{
   public User()
   {
      setRoles(new ArrayList<String>(4));
   }
   
   public User(String username, String password, String name, String[] roles, Date joined)
   {
      setUsername(username);
      setPassword(password);
      setName(name);
      setDateJoined(joined);
      List<String> rolesList = new ArrayList<String>(roles.length);
      for (int i=0; i<roles.length; i++)
      {
         rolesList.add(roles[i]);
      }
      setRoles(rolesList);
   }
   
   /**
    * Private copy constructor
    * 
    * @param u    User to clone
    */
   private User(User u)
   {
      setUsername(u.getUsername());
      setPassword(u.getPassword());
      setName(u.getName());
      setDateJoined(u.getDateJoined());
      setRoles(new ArrayList<String>(u.getRoles()));
   }
   
   /**
    * @see java.lang.Object#clone()
    */
   protected Object clone() throws CloneNotSupportedException
   {
      // invoke copy constructor
      return new User(this);
   }
   
   /**
    * Get the username
    *
    * @return the username
    */
   public String getUsername()
   {
      return m_username;
   }

   /**
    * Set the username
    *
    * @param username      the username
    */
   public void setUsername(String username)
   {
      m_username = username;
   }

   /**
    * Get the name
    *
    * @return the name
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set the name
    *
    * @param name     the name
    */
   public void setName(String name)
   {
      m_name = name;
   }

   /**
    * Get the roles
    *
    * @return the roles
    */
   public List<String> getRoles()
   {
      return m_roles;
   }

   /**
    * Set the roles
    *
    * @param roles     the roles
    */
   public void setRoles(List<String> roles)
   {
      m_roles = roles;
   }
   
   /**
    * Get the password
    *
    * @return the password
    */
   public String getPassword()
   {
      return m_password;
   }

   /**
    * Set the password
    *
    * @param password     the password
    */
   public void setPassword(String password)
   {
      m_password = password;
   }
   
   /**
    * Get the All Roles List
    *
    * @return the allRolesList
    */
   public List getAllRolesList()
   {
      return m_allRolesList;
   }

   /**
    * Set the allRolesList
    *
    * @param allRolesList     the allRolesList
    */
   public void setAllRolesList(List<SelectItem> allRolesList)
   {
      m_allRolesList = allRolesList;
   }
   
   /**
    * Get the dateJoined
    *
    * @return the dateJoined
    */
   public Date getDateJoined()
   {
      return m_dateJoined;
   }

   /**
    * Set the dateJoined
    *
    * @param dateJoined     the dateJoined
    */
   public void setDateJoined(Date dateJoined)
   {
      m_dateJoined = dateJoined;
   }


   /** the allRolesList enum list */
   private static List<SelectItem> m_allRolesList = new ArrayList<SelectItem>(8);
   static
   {
      m_allRolesList.add(new SelectItem("admin", "Administrator"));
      m_allRolesList.add(new SelectItem("superuser", "Super User"));
      m_allRolesList.add(new SelectItem("dev", "Developer"));
      m_allRolesList.add(new SelectItem("qa", "QA"));
      m_allRolesList.add(new SelectItem("standard", "Basic User"));
   }


   /** the password */
   private String m_password;

   /** the username */
   private String m_username;

   /** the name */
   private String m_name;

   /** the roles */
   private List<String> m_roles;
   
   /** the date joined */
   private Date m_dateJoined;
}