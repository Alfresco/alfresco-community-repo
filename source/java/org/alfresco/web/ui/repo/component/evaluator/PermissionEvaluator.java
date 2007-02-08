/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.ui.repo.component.evaluator;

import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.component.evaluator.BaseEvaluator;

/**
 * Evaulator for returning whether a Node is Allowed/Denied a list of permissions
 * 
 * @author Kevin Roast
 */
public class PermissionEvaluator extends BaseEvaluator
{
   /**
    * Evaluate against the component attributes. Return true to allow the inner
    * components to render, false to hide them during rendering.
    * 
    * IN: Value - either a Node (preferred) or NodeRef to test
    *     Allow - Permission(s) (comma separated) to test against
    *     Deny  - Permission(s) (comma separated) to test against
    * 
    * @return true to allow rendering of child components, false otherwise
    */
   public boolean evaluate()
   {
      boolean result = false;
      
      // TODO: implement Deny permissions checking (as required...)
      
      try
      {
         Object obj = getValue();
         if (obj instanceof Node)
         {
            // used the cached permissions checks in the Node instance
            // this means multiple calls to evaluators don't need to keep calling services
            // and permissions on a Node shouldn't realistically change over the life of an instance
            String[] allow = getAllowPermissions();
            if (allow.length != 0)
            {
               result = true;
               for (int i=0; i<allow.length; i++)
               {
                  result = result & ((Node)obj).hasPermission(allow[i]);
               }
            }
         }
         else if (obj instanceof NodeRef)
         {
            // perform the check for permissions here against NodeRef using service
            PermissionService service = Repository.getServiceRegistry(getFacesContext()).getPermissionService();
            String[] allow = getAllowPermissions();
            if (allow.length != 0)
            {
               result = true;
               for (int i=0; i<allow.length; i++)
               {
                  result = result & (AccessStatus.ALLOWED == service.hasPermission(((NodeRef)obj), allow[i]));
               }
            }
         }
      }
      catch (Exception err)
      {
         // return default value on error
         s_logger.debug("Error during PermissionEvaluator evaluation: " + err.getMessage());
      }
      
      return result;
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.allow = (String)values[1];
      this.deny = (String)values[2];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.allow;
      values[2] = this.deny;
      return (values);
   }
   
   /**
    * @return the array of Allow permissions
    */
   private String[] getAllowPermissions()
   {
      String[] allowPermissions;
      
      String allow = getAllow();
      if (allow != null)
      {
         if (allow.indexOf(',') == -1)
         {
            // simple case - one permission
            allowPermissions = new String[1];
            allowPermissions[0] = allow;
         }
         else
         {  
            // complex case - multiple permissions
            StringTokenizer t = new StringTokenizer(allow, ",");
            allowPermissions = new String[t.countTokens()];
            for (int i=0; i<allowPermissions.length; i++)
            {
               allowPermissions[i] = t.nextToken();
            }
         }
      }
      else
      {
         allowPermissions = new String[0];
      }
      
      return allowPermissions;
   }
   
   /**
    * Get the allow permissions to match value against
    * 
    * @return the allow permissions to match value against
    */
   public String getAllow()
   {
      ValueBinding vb = getValueBinding("allow");
      if (vb != null)
      {
         this.allow = (String)vb.getValue(getFacesContext());
      }
      
      return this.allow;
   }
   
   /**
    * Set the allow permissions to match value against
    * 
    * @param allow     allow permissions to match value against
    */
   public void setAllow(String allow)
   {
      this.allow = allow;
   }
   
   /**
    * Get the deny permissions to match value against
    * 
    * @return the deny permissions to match value against
    */
   public String getDeny()
   {
      ValueBinding vb = getValueBinding("deny");
      if (vb != null)
      {
         this.deny = (String)vb.getValue(getFacesContext());
      }
      
      return this.deny;
   }
   
   /**
    * Set the deny permissions to match value against
    * 
    * @param deny     deny permissions to match value against
    */
   public void setDeny(String deny)
   {
      this.deny = deny;
   }
   
   
   /** the deny permissions to match value against */
   private String deny = null;
   
   /** the allow permissions to match value against */
   private String allow = null;
}
