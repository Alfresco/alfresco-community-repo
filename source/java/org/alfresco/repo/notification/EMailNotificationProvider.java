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
package org.alfresco.repo.notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationProvider;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ModelUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * EMail notification provider implementation
 * 
 * @author Roy Wetherall
 * @since 4.0
 */
public class EMailNotificationProvider implements NotificationProvider
{
    /** I18N */
    private static final String MSG_DEFAULT_SENDER_USED = "default-sender-used";
    private static final String MSG_NO_RECIPIENTS = "no-recipients";
    private static final String MSG_NO_BODY_OR_TEMPLATE = "no-body-or-template";
    
    /** Log */
    private static Log logger = LogFactory.getLog(EMailNotificationProvider.class); 
    
    /** Name of provider */
    public final static String NAME = "email";
    
    /** Notification service */
    private NotificationService notificationService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Action service */
    private ActionService actionService;
    
    /** Person service */
    private PersonService personService;
    
    /** Repository object */
    private Repository repository;

    /** File folder service */
    private FileFolderService fileFolderService;
    
    /** Repository administration service */
    private RepoAdminService repoAdminService;
    
    /**
     * @param notificationService   notification service
     */
    public void setNotificationService(NotificationService notificationService)
    {
        this.notificationService = notificationService;
    }
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param actionService action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * @param personService person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * @param repository    repository object
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
    /**
     * @param repoAdminService  repository administration serviceS
     */
    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }
    
    /**
     * @param fileFolderService file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * Init method registers provider with notification service.
     */
    public void init()
    {
        notificationService.register(this);
    }
    
    /**
     * @see org.alfresco.service.cmr.notification.NotificationProvider#getName()
     */
    @Override
    public String getName()
    {
        return NAME;
    }
    
    /**
     * @see org.alfresco.service.cmr.notification.NotificationProvider#sendNotification(org.alfresco.service.cmr.notification.NotificationContext)
     */
    @Override
    public void sendNotification(NotificationContext notificationContext)
    {
        Action mail = actionService.createAction(MailActionExecuter.NAME);
        
        // Set from parameter
        String from = notificationContext.getFrom();
        if (from != null && from.length() != 0)
        {
            String fromEMail = getEMailFromUser(from);
            if (fromEMail != null)
            {
                mail.setParameterValue(MailActionExecuter.PARAM_FROM, fromEMail);
            }
            else
            {
                if (logger.isWarnEnabled() == true)
                {
                    logger.warn(I18NUtil.getMessage(MSG_DEFAULT_SENDER_USED, from));
                }
            }
        }
        
        // Set to parameter
        List<String> to = notificationContext.getTo();
        if (to == null || to.size() == 0)
        {
            errorEncountered(notificationContext, 
                             I18NUtil.getMessage(MSG_NO_RECIPIENTS, notificationContext.getDocument()));
            return;
        }
        else
        {
            mail.setParameterValue(MailActionExecuter.PARAM_TO_MANY, (Serializable)to);
        }        
        
        // Set subject
        String subject = notificationContext.getSubject();
        if (subject != null)
        {
            mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subject);
        }
        
        // Set body 
        String body = notificationContext.getBody();
        if (body != null && body.length() != 0)
        {
            mail.setParameterValue(MailActionExecuter.PARAM_TEXT, body);
        }
        else
        {        
            // Check for template
            NodeRef template = notificationContext.getBodyTemplate();
            if (template == null)
            {
                errorEncountered(notificationContext, 
                                 I18NUtil.getMessage(MSG_NO_BODY_OR_TEMPLATE, notificationContext.getDocument()));
                return;
            }
            else
            {
                template = fileFolderService.getLocalizedSibling(template);
                mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, template);
                mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, 
                                       (Serializable)buildTemplateModel(notificationContext.getTemplateArgs()));
                
                
            }
        }
        
        // Set ignore failure
        mail.setParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, Boolean.valueOf(notificationContext.isIgnoreNotificationFailure()));
        
        // Execute mail action upon document
        actionService.executeAction(mail, notificationContext.getDocument(), false, notificationContext.isAsyncNotification());       
    }
    
    /**
     * Gets the email for the given user name.  Returns null if none set.
     * 
     * @param user  user name
     * @return {@link String}   user email
     */
    private String getEMailFromUser(String user)
    {
        String email =  null;
        
        NodeRef person = personService.getPerson(user);
        if (person != null)
        {
            email = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
        }
        
        return email;
    }
    
    /**
     * Build the model for the body template.
     * 
     * @param templateArgs  template args provided by the notification context
     * @return {@link Map}<{@link String},{@link Serializable}> template model values
     */
    private Map<String, Serializable> buildTemplateModel(Map<String, Serializable> templateArgs)
    {
        // Set the core model parts
        // Note - the user part is skipped, as that's implied via the run-as
        Map<String, Serializable> model = new HashMap<String, Serializable>();
        model.put(TemplateService.KEY_COMPANY_HOME, repository.getCompanyHome());
        NodeRef person = repository.getPerson();
        if (person != null)
        {
            model.put(TemplateService.KEY_PERSON, person);
            model.put(TemplateService.KEY_USER_HOME, repository.getUserHome(person));
        }
        model.put(TemplateService.KEY_PRODUCT_NAME, ModelUtil.getProductName(repoAdminService));
        
        // Put the notification context information in the model?
        // TODO
        
        if (templateArgs != null && templateArgs.size() != 0)
        {
            // Put the provided args in the model
            model.put("args", (Serializable)templateArgs);
        }
        
        // All done
        return model;
    }
    
    /**
     * Deals with an error when it is encountered
     * 
     * @param notificationContext   notification context
     * @param message               error message
     */
    private void errorEncountered(NotificationContext notificationContext, String message)
    {
        if (logger.isWarnEnabled() == true)
        {
            logger.warn(message);
        }
        
        if (notificationContext.isIgnoreNotificationFailure() == false)
        {
            throw new AlfrescoRuntimeException(message);
        } 
    }
}
