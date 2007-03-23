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
package org.alfresco.web.bean;

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.component.template.DefaultModelHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * @author Kevin Roast
 */
public class TemplateMailHelperBean
{
   private static Log logger = LogFactory.getLog(TemplateMailHelperBean.class);
   
   /** JavaMailSender bean reference */
   protected JavaMailSender mailSender;
   
   /** NodeService bean reference */
   protected NodeService nodeService;
   
   /** dialog state */
   private String subject = null;
   private String body = null;
   private String automaticText = null;
   private String template = null;
   private String usingTemplate = null;
   private String finalBody;
   
   /**
    * @param mailSender       The JavaMailSender to set.
    */
   public void setMailSender(JavaMailSender mailSender)
   {
      this.mailSender = mailSender;
   }
   
   /**
    * @param nodeService      The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * Initialises the bean
    */
   public TemplateMailHelperBean()
   {
      subject = "";
      body = "";
      automaticText = "";
      template = null;
      usingTemplate = null;
   }
   
   /**
    * Send an email notification to the specified User authority
    * 
    * @param person     Person node representing the user
    * @param node       Node they are invited too
    * @param from       From text message
    * @param roleText   The role display label for the user invite notification
    */
   public void notifyUser(NodeRef person, NodeRef node, final String from, String roleText)
   {
      final String to = (String)this.nodeService.getProperty(person, ContentModel.PROP_EMAIL);
      
      if (to != null && to.length() != 0)
      {
         String body = this.body;
         if (this.usingTemplate != null)
         {
            FacesContext fc = FacesContext.getCurrentInstance();
            
            // use template service to format the email
            NodeRef templateRef = new NodeRef(Repository.getStoreRef(), this.usingTemplate);
            ServiceRegistry services = Repository.getServiceRegistry(fc);
            Map<String, Object> model = DefaultModelHelper.buildDefaultModel(
                  services, Application.getCurrentUser(fc), templateRef);
            model.put("role", roleText);
            model.put("space", new TemplateNode(node, Repository.getServiceRegistry(fc), null));
            
            body = services.getTemplateService().processTemplate("freemarker", templateRef.toString(), model);
         }
         this.finalBody = body;
         
         MimeMessagePreparator mailPreparer = new MimeMessagePreparator()
         {
            public void prepare(MimeMessage mimeMessage) throws MessagingException
            {
               MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
               message.setTo(to);
               message.setSubject(subject);
               message.setText(finalBody);
               message.setFrom(from);
            }
         };
         
         if (logger.isDebugEnabled())
            logger.debug("Sending notification email to: " + to + "\n...with subject:\n" + subject + "\n...with body:\n" + body);
         
         try
         {
            // Send the message
            this.mailSender.send(mailPreparer);
         }
         catch (Throwable e)
         {
            // don't stop the action but let admins know email is not getting sent
            logger.error("Failed to send email to " + to, e);
         }
      }
   }
   
   /**
    * Action handler called to insert a template as the email body
    */
   public void insertTemplate(ActionEvent event)
   {
      if (this.template != null && this.template.equals(TemplateSupportBean.NO_SELECTION) == false)
      {
         // get the content of the template so the user can get a basic preview of it
         try
         {
            NodeRef templateRef = new NodeRef(Repository.getStoreRef(), this.template);
            ContentService cs = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentService();
            ContentReader reader = cs.getReader(templateRef, ContentModel.PROP_CONTENT);
            if (reader != null && reader.exists())
            {
               this.body = reader.getContentString();
               
               this.usingTemplate = this.template;
            }
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         }
      }
   }
   
   /**
    * Action handler called to discard the template from the email body
    */
   public void discardTemplate(ActionEvent event)
   {
      this.body = this.automaticText;
      usingTemplate = null;
   }
   
   /**
    * @return Returns the email body text.
    */
   public String getBody()
   {
      return this.body;
   }

   /**
    * @param body The email body text to set.
    */
   public void setBody(String body)
   {
      this.body = body;
   }

   /**
    * @return Returns the email subject text.
    */
   public String getSubject()
   {
      return this.subject;
   }

   /**
    * @param subject The email subject text to set.
    */
   public void setSubject(String subject)
   {
      this.subject = subject;
   }
   
   /**
    * @return Returns the automatic text.
    */
   public String getAutomaticText()
   {
      return this.automaticText;
   }

   /**
    * @param automaticText The automatic text to set.
    */
   public void setAutomaticText(String automaticText)
   {
      this.automaticText = automaticText;
   }

   /**
    * @return Returns the email template Id
    */
   public String getTemplate()
   {
      return this.template;
   }

   /**
    * @param template The email template to set.
    */
   public void setTemplate(String template)
   {
      this.template = template;
   }
   
   /**
    * @return Returns if a template has been inserted by a user for email body.
    */
   public String getUsingTemplate()
   {
      return this.usingTemplate;
   }

   /**
    * @param usingTemplate Template that has been inserted by a user for the email body.
    */
   public void setUsingTemplate(String usingTemplate)
   {
      this.usingTemplate = usingTemplate;
   }
}
