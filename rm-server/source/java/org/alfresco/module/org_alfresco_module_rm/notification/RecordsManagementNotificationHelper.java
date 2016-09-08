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
package org.alfresco.module.org_alfresco_module_rm.notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.Role;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Helper bean containing methods useful when sending records
 * management notifications via the {@link NotificationService}
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementNotificationHelper
{
    private static Log logger = LogFactory.getLog(RecordsManagementNotificationHelper.class);
    
    /** I18n */
    private static final String MSG_SUBJECT_RECORDS_DUE_FOR_REVIEW = "notification.dueforreview.subject";
    private static final String MSG_SUBJECT_RECORD_SUPERCEDED = "notification.superseded.subject";
    
    /** Defaults */
    private static final String DEFAULT_SITE = "rm";

    /** Services */
    private NotificationService notificationService;
    private RecordsManagementService recordsManagementService;
    private RecordsManagementSecurityService securityService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private SiteService siteService;
    private AuthorityService authorityService;
    private TenantAdminService tenantAdminService;
    private NodeService nodeService;
    
    /** Notification role */
    private String notificationRole;
    
    /** EMail notification templates */
    private NodeRef supersededTemplate = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "record_superseded_template");
    private NodeRef dueForReviewTemplate;
    
    /**
     * @param notificationService   notification service
     */
    public void setNotificationService(NotificationService notificationService)
    {
        this.notificationService = notificationService;
    }
    
    /**
     * @param recordsManagementService  rm service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    /**
     * @param securityService   rm security service
     */
    public void setSecurityService(RecordsManagementSecurityService securityService)
    {
        this.securityService = securityService;
    }
    
    /**
     * @param notificationRole  rm notification role
     */
    public void setNotificationRole(String notificationRole)
    {
        this.notificationRole = notificationRole;
    }
    
    /**
     * @param searchService search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param siteService   site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param tenantAdminService    tenant admin service
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    /**
     * @return  superseded email template
     */
    public NodeRef getSupersededTemplate()
    {
        return supersededTemplate;
    }
    
    /**
     * @return  due for review email template
     */
    public NodeRef getDueForReviewTemplate()
    {
        if (dueForReviewTemplate == null)
        {
            List<NodeRef> nodeRefs = 
                searchService.selectNodes(
                        getRootNode(), 
                        "app:company_home/app:dictionary/cm:records_management/cm:records_management_email_templates/cm:notify-records-due-for-review-email.ftl", null, 
                        namespaceService, 
                        false);
            if (nodeRefs.size() == 1)
            {
                dueForReviewTemplate = nodeRefs.get(0);
            }
        }
        return dueForReviewTemplate;
    }
    
    /**
     * Helper method to get root node in a tenant safe way.
     * 
     * @return  NodeRef root node of spaces store
     */
    private NodeRef getRootNode()
    {
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        return TenantUtil.runAsSystemTenant(new TenantRunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {                
                return nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            }
        }, tenantDomain);
    }
    
    /**
     * Sends records due for review email notification.
     * 
     * @param records   records due for review
     */
    public void recordsDueForReviewEmailNotification(final List<NodeRef> records)
    {
        ParameterCheck.mandatory("records", records);
        if (records.isEmpty() == false)
        {
            NodeRef root = getRMRoot(records.get(0));
            String groupName = getGroupName(root); 
            
            if (doesGroupContainUsers(groupName) == true)
            {            
                NotificationContext notificationContext = new NotificationContext();
                notificationContext.setSubject(I18NUtil.getMessage(MSG_SUBJECT_RECORDS_DUE_FOR_REVIEW));
                notificationContext.setAsyncNotification(false);
                notificationContext.setIgnoreNotificationFailure(true);
                
                notificationContext.setBodyTemplate(getDueForReviewTemplate());
                Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
                args.put("records", (Serializable)records); 
                args.put("site", getSiteName(root));
                notificationContext.setTemplateArgs(args);
                           
                notificationContext.addTo(groupName);        
                
                notificationService.sendNotification(EMailNotificationProvider.NAME, notificationContext);
            }
            else
            {
                if (logger.isWarnEnabled() == true)
                {
                    logger.warn("Unable to send record due for review email notification, because notification group was empty.");
                }
                
                throw new AlfrescoRuntimeException("Unable to send record due for review email notification, because notification group was empty.");
            }
        }
    }    
    
    /**
     * Sends record superseded email notification.
     * 
     * @param record    superseded record
     */
    public void recordSupersededEmailNotification(final NodeRef record)
    {
        ParameterCheck.mandatory("record", record);  
        
        NodeRef root = getRMRoot(record);
        String groupName = getGroupName(root);
        
        if (doesGroupContainUsers(groupName) == true)
        { 
            NotificationContext notificationContext = new NotificationContext();
            notificationContext.setSubject(I18NUtil.getMessage(MSG_SUBJECT_RECORD_SUPERCEDED));
            notificationContext.setAsyncNotification(false);
            notificationContext.setIgnoreNotificationFailure(true);
            
            notificationContext.setBodyTemplate(supersededTemplate);
            Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
            args.put("record", record);  
            args.put("site", getSiteName(root));
            notificationContext.setTemplateArgs(args);
            
            notificationContext.addTo(groupName);        
            
            notificationService.sendNotification(EMailNotificationProvider.NAME, notificationContext);
        }
        else
        {
            if (logger.isWarnEnabled() == true)
            {
                logger.warn("Unable to send record superseded email notification, because notification group was empty.");
            }
        }
    }
    
    /**
     * Gets the rm root given a context node.
     * 
     * @param context   context node reference
     * @return {@link NodeRef}  rm root node reference
     */
    private NodeRef getRMRoot(final NodeRef context)
    {
        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return recordsManagementService.getFilePlan(context);
                
            }
        }, AuthenticationUtil.getSystemUserName());
        
    }
    
    /**
     * Gets the group name for the notification role.
     * 
     * @param root  rm root node
     * @return String   notification role's group name
     */
    private String getGroupName(final NodeRef root)
    {
        return AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                // Find the authority for the given role
                Role role = securityService.getRole(root, notificationRole);
                return role.getRoleGroupName();                
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    private boolean doesGroupContainUsers(final String groupName)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
            @Override
            public Boolean doWork() throws Exception
            {
                Set<String> users = authorityService.getContainedAuthorities(AuthorityType.USER, groupName, true);
                return !users.isEmpty();
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Get the site name, default if none/undetermined.
     * 
     * @param root  rm root
     * @return String   site name
     */
    private String getSiteName(final NodeRef root)
    {
        return AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                String result = DEFAULT_SITE;
                
                SiteInfo siteInfo = siteService.getSite(root);
                if (siteInfo != null)
                {
                    result = siteInfo.getShortName();
                }
                
                return result;
            }
        }, AuthenticationUtil.getSystemUserName());
        
    }
}
