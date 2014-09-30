/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.service;

import java.util.Collection;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryService;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandlerRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.blog.BlogService;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PublicServiceAccessService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.webdav.WebDavService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;


/**
 * Implementation of a Service Registry based on the definition of
 * Services contained within a Spring Bean Factory.
 *
 * @author David Caruana
 */
public class ServiceDescriptorRegistry
    implements BeanFactoryAware, ServiceRegistry
{
    // Bean Factory within which the registry lives
    private BeanFactory beanFactory = null;


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public Collection<QName> getServices()
    {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean isServiceProvided(QName service)
    {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getService(QName service)
    {
        return beanFactory.getBean(service.getLocalName());
    }

    @Override
    public DescriptorService getDescriptorService()
    {
        return (DescriptorService)getService(DESCRIPTOR_SERVICE);
    }

    @Override
    public NodeService getNodeService()
    {
        return (NodeService)getService(NODE_SERVICE);
    }

    @Override
    public MutableAuthenticationService getAuthenticationService()
    {
        return (MutableAuthenticationService)getService(AUTHENTICATION_SERVICE);
    }

    @Override
    public ContentService getContentService()
    {
        return (ContentService)getService(CONTENT_SERVICE);
    }

    @Override
    public MimetypeService getMimetypeService()
    {
        return (MimetypeService)getService(MIMETYPE_SERVICE);
    }

    @Override
    public VersionService getVersionService()
    {
        return (VersionService)getService(VERSION_SERVICE);
    }

    @Override
    public LockService getLockService()
    {
        return (LockService)getService(LOCK_SERVICE);
    }

    public JobLockService getJobLockService()
    {
        return (JobLockService)getService(JOB_LOCK_SERVICE);
    }

    @Override
    public DictionaryService getDictionaryService()
    {
        return (DictionaryService)getService(DICTIONARY_SERVICE);
    }

    @Override
    public SearchService getSearchService()
    {
        return (SearchService)getService(SEARCH_SERVICE);
    }

    @Override
    public TransactionService getTransactionService()
    {
        return (TransactionService)getService(TRANSACTION_SERVICE);
    }

    @Override
    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        TransactionService txnService = (TransactionService) getService(TRANSACTION_SERVICE);
        return txnService.getRetryingTransactionHelper();
    }

    @Override
    public CopyService getCopyService()
    {
        return (CopyService)getService(COPY_SERVICE);
    }

    @Override
    public CheckOutCheckInService getCheckOutCheckInService()
    {
        return (CheckOutCheckInService)getService(COCI_SERVICE);
    }

    @Override
    public CategoryService getCategoryService()
    {
        return (CategoryService)getService(CATEGORY_SERVICE);
    }

    @Override
    public NamespaceService getNamespaceService()
    {
        return (NamespaceService)getService(NAMESPACE_SERVICE);
    }

    @Override
    public ImporterService getImporterService()
    {
        return (ImporterService)getService(IMPORTER_SERVICE);
    }

    @Override
    public ExporterService getExporterService()
    {
        return (ExporterService)getService(EXPORTER_SERVICE);
    }

    @Override
    public RuleService getRuleService()
    {
        return (RuleService)getService(RULE_SERVICE);
    }

    @Override
    public ActionService getActionService()
    {
        return (ActionService)getService(ACTION_SERVICE);
    }

    @Override
    public PermissionService getPermissionService()
    {
        return (PermissionService)getService(PERMISSIONS_SERVICE);
    }

    @Override
    public AuthorityService getAuthorityService()
    {
        return (AuthorityService)getService(AUTHORITY_SERVICE);
    }

    @Override
    public TemplateService getTemplateService()
    {
        return (TemplateService)getService(TEMPLATE_SERVICE);
    }

    @Override
    public FileFolderService getFileFolderService()
    {
        return (FileFolderService)getService(FILE_FOLDER_SERVICE);
    }

    @Override
    public ScriptService getScriptService()
    {
        return (ScriptService)getService(SCRIPT_SERVICE);
    }

    @Override
    public WorkflowService getWorkflowService()
    {
        return (WorkflowService)getService(WORKFLOW_SERVICE);
    }
    
    @Override
    public NotificationService getNotificationService()
    {
        return (NotificationService)getService(NOTIFICATION_SERVICE);
    }

    @Override
    public AuditService getAuditService()
    {
        return (AuditService)getService(AUDIT_SERVICE);
    }

    @Override
    public OwnableService getOwnableService()
    {
        return (OwnableService)getService(OWNABLE_SERVICE);
    }

    @Override
    public PersonService getPersonService()
    {
        return (PersonService)getService(PERSON_SERVICE);
    }

    @Override
    public SiteService getSiteService()
    {
        return (SiteService) getService(SITE_SERVICE);
    }

    @Override
    public AttributeService getAttributeService()
    {
        return (AttributeService)getService(ATTRIBUTE_SERVICE);
    }

    @Override
    public ContentFilterLanguagesService getContentFilterLanguagesService()
    {
        return (ContentFilterLanguagesService) getService(CONTENT_FILTER_LANGUAGES_SERVICE);
    }

    @Override
    public EditionService getEditionService()
    {
        return (EditionService) getService(EDITION_SERVICE);
    }

    @Override
    public MultilingualContentService getMultilingualContentService()
    {
        return (MultilingualContentService) getService(MULTILINGUAL_CONTENT_SERVICE);
    }
    
    @Override
    public ThumbnailService getThumbnailService()
    {
        return (ThumbnailService)getService(THUMBNAIL_SERVICE);
    }

    @Override
    public TaggingService getTaggingService()
    {
        return (TaggingService)getService(TAGGING_SERVICE);
    }
    
    @Override
    public FormService getFormService() 
    {
        return (FormService)getService(FORM_SERVICE);
    }
    
    @Override
    public RenditionService getRenditionService() 
    {
        return (RenditionService)getService(RENDITION_SERVICE);
    }
    
    @Override
    public RatingService getRatingService() 
    {
        return (RatingService)getService(RATING_SERVICE);
    }
    
    @Override
    public NodeLocatorService getNodeLocatorService() 
    {
        return (NodeLocatorService)getService(NODE_LOCATOR_SERVICE);
    }
    
    @Override
    public BlogService getBlogService() 
    {
        return (BlogService)getService(BLOG_SERVICE);
    }
    
    @Override
    public CalendarService getCalendarService() 
    {
        return (CalendarService)getService(CALENDAR_SERVICE);
    }
    
    @Override
    public InvitationService getInvitationService() 
    {
         return (InvitationService)getService(INVITATION_SERVICE);
    }

    @Override
    public CMISDictionaryService getCMISDictionaryService() 
    {
         return (CMISDictionaryService)getService(CMIS_DICTIONARY_SERVICE);
    }

    @Override
    public CMISQueryService getCMISQueryService() 
    {
         return (CMISQueryService)getService(CMIS_QUERY_SERVICE);
    }

    @Override
    public ImapService getImapService() 
    {
        return (ImapService)getService(IMAP_SERVICE);
    }

    @Override
    public PublicServiceAccessService getPublicServiceAccessService()
    {
        return (PublicServiceAccessService)getService(PUBLIC_SERVICE_ACCESS_SERVICE);
    }

    @Override
    public RepoAdminService getRepoAdminService()
    {
        return (RepoAdminService)getService(REPO_ADMIN_SERVICE);
    }
    
    @Override
    public SysAdminParams getSysAdminParams()
    {
        final String beanName = "sysAdminParams";
        return (SysAdminParams) beanFactory.getBean(beanName);
    }
    
    @Override
    public WebDavService getWebDavService()
    {
        return (WebDavService)getService(WEBDAV_SERVICE);
    }
    
    @Override
    public SolrFacetHelper getSolrFacetHelper()
    {
        final String beanName = "facet.solrFacetHelper";
        return (SolrFacetHelper) beanFactory.getBean(beanName);
    }
    
    @Override
    public FacetLabelDisplayHandlerRegistry getFacetLabelDisplayHandlerRegistry()
    {
        final String beanName = "facet.facetLabelDisplayHandlerRegistry";
        return (FacetLabelDisplayHandlerRegistry) beanFactory.getBean(beanName);
    }
}
