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
package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.BaseActionWizard;
import org.alfresco.web.bean.actions.BaseActionWizard.RecipientWrapper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler implementation for the "mail" action.
 * 
 * @author gavinc
 */
public class MailHandler extends BaseActionHandler
{
   private static final long serialVersionUID = 622475325355334995L;
   
   public static final String PROP_TO = "to";
   public static final String PROP_FROM = "from";
   public static final String PROP_MESSAGE = "message";
   public static final String PROP_SUBJECT = "subject";
   public static final String PROP_TEMPLATE = "template";

   public String getJSPPath()
   {
      return getJSPPath(MailActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // get hold of the current wizard so we can extract some data from it
      BaseActionWizard wizard = (BaseActionWizard)Application.
            getWizardManager().getBean();
      
      // add the person(s) it's going to as a list of authorities
      List<RecipientWrapper> wizardRecipients = wizard.getEmailRecipients(actionProps);
      List<String> recipients = new ArrayList<String>(wizardRecipients.size());
      for (int i=0; i < wizardRecipients.size(); i++)
      {
         RecipientWrapper wrapper = wizardRecipients.get(i);
         recipients.add(wrapper.getAuthority());
      }
      
      repoProps.put(MailActionExecuter.PARAM_TO_MANY, (Serializable)recipients);
      
      // add the actual email text to send
      repoProps.put(MailActionExecuter.PARAM_TEXT, actionProps.get(PROP_MESSAGE));
          
      // add the subject for the email
      repoProps.put(MailActionExecuter.PARAM_SUBJECT, actionProps.get(PROP_SUBJECT));
      
      // add the from address
      String from = Application.getClientConfig(FacesContext.getCurrentInstance()).getFromEmailAddress();
      repoProps.put(MailActionExecuter.PARAM_FROM, from);
      
      // add the template if one was selected by the user
      if (wizard.getUsingTemplate() != null)
      {
         repoProps.put(MailActionExecuter.PARAM_TEMPLATE, new NodeRef(Repository.getStoreRef(), 
               wizard.getUsingTemplate()));
      }
   }

   @SuppressWarnings("unchecked")
public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // get hold of the current wizard so we can extract some data from it
      BaseActionWizard wizard = (BaseActionWizard)Application.
            getWizardManager().getBean();
      
      String subject = (String)repoProps.get(MailActionExecuter.PARAM_SUBJECT);
      actionProps.put(PROP_SUBJECT, subject);
      
      String message = (String)repoProps.get(MailActionExecuter.PARAM_TEXT);
      actionProps.put(PROP_MESSAGE, message);
      
      // handle single email or multiple authority recipients
      String to = (String)repoProps.get(MailActionExecuter.PARAM_TO);
      if (to != null)
      {
         actionProps.put(PROP_TO, to);
      }
      else 
      { 
         Object recipObj = repoProps.get(MailActionExecuter.PARAM_TO_MANY); 
        
         List<String> recipients = null; 
        
         if ( recipObj != null) 
         { 
             if ( recipObj instanceof String) 
             { 
                 recipients = new ArrayList<String>(); 
                 recipients.add((String) recipObj); 
             } 
             else if ( recipObj instanceof List) 
             { 
                 recipients = (List<String>) recipObj; 
             } 
         } 

         if (recipients != null && recipients.size() != 0) 
         {
            // rebuild the list of RecipientWrapper objects from the stored action
            for (String authority : recipients)
            {
               wizard.getEmailRecipients(actionProps).add(
                     new RecipientWrapper(wizard.displayLabelForAuthority(authority), 
                           authority));
            }
         }
      }
      
      NodeRef templateRef = (NodeRef)repoProps.get(MailActionExecuter.PARAM_TEMPLATE);
      if (templateRef != null)
      {
         actionProps.put(PROP_TEMPLATE, templateRef.getId());
         wizard.setUsingTemplate(templateRef.getId());
      }
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      BaseActionWizard actionWizard = (BaseActionWizard)wizard;
      
      String addresses = (String)actionProps.get(PROP_TO);
         
      if (addresses == null || addresses.length() == 0)
      {
         List<RecipientWrapper> wizardRecipients = actionWizard.getEmailRecipients(actionProps); 
         if (wizardRecipients.size() != 0)
         {
            StringBuilder builder = new StringBuilder();
            
            for (int i=0; i < wizardRecipients.size(); i++)
            {
               RecipientWrapper wrapper = wizardRecipients.get(i);
               if (i != 0)
               {
                  builder.append(", ");
               }
               builder.append(wrapper.getName());
            }
            
            addresses = builder.toString();
         }
      }
      
      return MessageFormat.format(Application.getMessage(context, "action_mail"),
            new Object[] {addresses});
   }
}
