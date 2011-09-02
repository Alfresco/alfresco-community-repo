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
package org.alfresco.service.cmr.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Notification context.  Provides the contextual information about a notification.
 * 
 * @author Roy Wetherall
 * @since 4.0
 */
public class NotificationContext 
{
    /** Authority name notification is being sent from */
    private String from;
    
    /** Authorities notification is being sent to */
    private List<String> to;
    
    /** Subject of the notification */
    private String subject;
    
    /** Body of the notification */
    private String body;
    
    /** Template node used to generate the body of the notification */
    private NodeRef bodyTemplate;
    
    /** Template arguments (appear as map under 'arg' property in template model) */
    private Map<String, Serializable> templateArgs;
    
    /** Document giving notification context */
    private NodeRef document;
    
    /** Indicates whether notification failure should be ignored or not */
    private boolean ignoreNotificationFailure = true;
    
    /** Indicates whether the notification should be sent asynchronously or not */
    private boolean asyncNotification = false;

    /**
     * Default constructor
     */
    public NotificationContext()
    {
        to = new ArrayList<String>(1);
    }
    
    /**
     * @param from  from authority
     */
    public void setFrom(String from)
    {
        this.from = from;
    }
    
    /**
     * @return  {@link String}  from authority
     */
    public String getFrom()
    {
        return from;
    }
    
    /**
     * @param to    to authorities
     */
    public void addTo(String to)
    {
        this.to.add(to);
    }
    
    /**
     * @return  {@link List}<{@link String}>    to authorities
     */
    public List<String> getTo()
    {
        return to;
    }
    
    /**
     * @param subject   subject of notification
     */
    public void setSubject(String subject)
    {
        this.subject = subject;
    }
    
    /**
     * @return  subject of notification
     */
    public String getSubject()
    {
        return subject;
    }
    
    /**
     * Note:  this takes presendence over the body template if both are set
     * 
     * @param body  body of notification.
     */
    public void setBody(String body)
    {
        this.body = body;
    }
    
    /**
     * @return  {@link String}  body of notification
     */
    public String getBody()
    {
        return body;
    }
    
    /**
     * The body template is a node reference to a template that can be executed with the given
     * template arguments to produce the body of the notification.
     * 
     * @param bodyTemplate  body template
     */
    public void setBodyTemplate(NodeRef bodyTemplate)
    {
        this.bodyTemplate = bodyTemplate;
    }
    
    /**
     * @return {@link NodeRef}  body template
     */
    public NodeRef getBodyTemplate()
    {
        return bodyTemplate;
    }    
    
    /**
     * The template arguments are used as context for the body template when it is executed.  Any values placed in this map will
     * be available in the template from the root object 'args'.  For example '${args.workflowDescription}'.
     * 
     * @param templateArgs  template arguments
     */
    public void setTemplateArgs(Map<String, Serializable> templateArgs)
    {
        this.templateArgs = templateArgs;
    }
    
    /**
     * @return {@link Map}<{@link String}, {@link Serializable}>    template arguments
     */
    public Map<String, Serializable> getTemplateArgs()
    {
        return templateArgs;
    }
    
    /**
     * Document that the notification relates to.  This does not have to be set.  Will be used to populate the 'document' root object accessable within the body template 
     * if set.
     * 
     * @param document  related document
     */
    public void setDocument(NodeRef document)
    {
        this.document = document;
    }
    
    /**
     * @return {@link NodeRef}  related document
     */
    public NodeRef getDocument()
    {
        return document;
    }
    
    /**
     * Indicates whether to ignore a notification failure or not.
     * 
     * @param ignoreNotificationFailure true if ignore notification failure, false otherwise
     */
    public void setIgnoreNotificationFailure(boolean ignoreNotificationFailure)
    {
        this.ignoreNotificationFailure = ignoreNotificationFailure;
    }
    
    /**
     * @return boolean  true if ignore notification failure, false otherwise
     */
    public boolean isIgnoreNotificationFailure()
    {
        return ignoreNotificationFailure;
    }
    
    /**
     * Indicates whether the notification will be sent asynchronously or not.
     * 
     * @param asyncNotification true if notification sent asynchronously, false otherwise
     */
    public void setAsyncNotification(boolean asyncNotification)
    {
        this.asyncNotification = asyncNotification;
    }
    
    /**
     * @return boolean  true if notification send asynchronously, false otherwise
     */
    public boolean isAsyncNotification()
    {
        return asyncNotification;
    }
}
