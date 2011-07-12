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
package org.alfresco.repo.service;

import java.util.Collection;

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISServices;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
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
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CrossRepositoryCopyService;
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
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.preview.PreviewURIService;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.webproject.WebProjectService;
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


    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getServices()
     */
    public Collection<QName> getServices()
    {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#isServiceProvided(org.alfresco.repo.ref.QName)
     */
    public boolean isServiceProvided(QName service)
    {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getService(org.alfresco.repo.ref.QName)
     */
    public Object getService(QName service)
    {
        return beanFactory.getBean(service.getLocalName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getDescriptorService()
     */
    public DescriptorService getDescriptorService()
    {
        return (DescriptorService)getService(DESCRIPTOR_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getNodeService()
     */
    public NodeService getNodeService()
    {
        return (NodeService)getService(NODE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getNodeService()
     */
    public MutableAuthenticationService getAuthenticationService()
    {
        return (MutableAuthenticationService)getService(AUTHENTICATION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getContentService()
     */
    public ContentService getContentService()
    {
        return (ContentService)getService(CONTENT_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getMimetypeService()
     */
    public MimetypeService getMimetypeService()
    {
        return (MimetypeService)getService(MIMETYPE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getVersionService()
     */
    public VersionService getVersionService()
    {
        return (VersionService)getService(VERSION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getLockService()
     */
    public LockService getLockService()
    {
        return (LockService)getService(LOCK_SERVICE);
    }

    public JobLockService getJobLockService()
    {
        return (JobLockService)getService(JOB_LOCK_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getDictionaryService()
     */
    public DictionaryService getDictionaryService()
    {
        return (DictionaryService)getService(DICTIONARY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getSearchService()
     */
    public SearchService getSearchService()
    {
        return (SearchService)getService(SEARCH_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getTransactionService()
     */
    public TransactionService getTransactionService()
    {
        return (TransactionService)getService(TRANSACTION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getRetryingTransactionHelper()
     */
    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        return (RetryingTransactionHelper)getService(RETRYING_TRANSACTION_HELPER);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCopyService()
     */
    public CopyService getCopyService()
    {
        return (CopyService)getService(COPY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCheckOutCheckInService()
     */
    public CheckOutCheckInService getCheckOutCheckInService()
    {
        return (CheckOutCheckInService)getService(COCI_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCategoryService()
     */
    public CategoryService getCategoryService()
    {
        return (CategoryService)getService(CATEGORY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getNamespaceService()
     */
    public NamespaceService getNamespaceService()
    {
        return (NamespaceService)getService(NAMESPACE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getImporterService()
     */
    public ImporterService getImporterService()
    {
        return (ImporterService)getService(IMPORTER_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getExporterService()
     */
    public ExporterService getExporterService()
    {
        return (ExporterService)getService(EXPORTER_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getRuleService()
     */
    public RuleService getRuleService()
    {
        return (RuleService)getService(RULE_SERVICE);
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getActionService()
     */
    public ActionService getActionService()
    {
        return (ActionService)getService(ACTION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getPermissionService()
     */
    public PermissionService getPermissionService()
    {
        return (PermissionService)getService(PERMISSIONS_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getAuthorityService()
     */
    public AuthorityService getAuthorityService()
    {
        return (AuthorityService)getService(AUTHORITY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getTemplateService()
     */
    public TemplateService getTemplateService()
    {
        return (TemplateService)getService(TEMPLATE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getTemplateService()
     */
    public FileFolderService getFileFolderService()
    {
        return (FileFolderService)getService(FILE_FOLDER_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getScriptService()
     */
    public ScriptService getScriptService()
    {
        return (ScriptService)getService(SCRIPT_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getWorkflowService()
     */
    public WorkflowService getWorkflowService()
    {
        return (WorkflowService)getService(WORKFLOW_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getWorkflowService()
     */
    public AuditService getAuditService()
    {
        return (AuditService)getService(AUDIT_SERVICE);
    }

    /**
     * Get the AVMService.
     * @return The AVMService or null if there is none.
     */
    public AVMService getAVMService()
    {
        return (AVMService)getService(AVM_SERVICE);
    }

    /**
     * Get the AVMService.
     * @return The AVMService or null if there is none.
     */
    public AVMService getAVMLockingAwareService()
    {
        return (AVMService)getService(AVM_LOCKING_AWARE_SERVICE);
    }
    
    /**
     * Get the AVM Sync Service.
     * @return The AVM Sync Service.
     */
    public AVMSyncService getAVMSyncService()
    {
        return (AVMSyncService)getService(AVM_SYNC_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getOwnableService()
     */
    public OwnableService getOwnableService()
    {
        return (OwnableService)getService(OWNABLE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getPersonService()
     */
    public PersonService getPersonService()
    {
        return (PersonService)getService(PERSON_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getSiteService()
     */
    public SiteService getSiteService()
    {
        return (SiteService) getService(SITE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCrossRepositoryCopyService()
     */
    public CrossRepositoryCopyService getCrossRepositoryCopyService()
    {
        return (CrossRepositoryCopyService)getService(CROSS_REPO_COPY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getAttributeService()
     */
    public AttributeService getAttributeService()
    {
        return (AttributeService)getService(ATTRIBUTE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getContentFilterLanguagesService()
     */
     public ContentFilterLanguagesService getContentFilterLanguagesService()
     {
     return (ContentFilterLanguagesService) getService(CONTENT_FILTER_LANGUAGES_SERVICE);
     }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getAVMLockingService()
     */
    public AVMLockingService getAVMLockingService()
    {
        return (AVMLockingService)getService(AVM_LOCKING_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getVirtServerRegistry()
     */
    public VirtServerRegistry getVirtServerRegistry()
    {
        return (VirtServerRegistry)getService(VIRT_SERVER_REGISTRY);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getEditionService()
     */
    public EditionService getEditionService()
    {
        return (EditionService) getService(EDITION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getMultilingualContentService()
     */
    public MultilingualContentService getMultilingualContentService()
    {
        return (MultilingualContentService) getService(MULTILINGUAL_CONTENT_SERVICE);
    }
    
    /**
     * @see org.alfresco.service.ServiceRegistry#getThumbnailService()
     */
    public ThumbnailService getThumbnailService()
    {
        return (ThumbnailService)getService(THUMBNAIL_SERVICE);
    }

    /**
     * @see org.alfresco.service.ServiceRegistry#getTaggingService()
     */
    public TaggingService getTaggingService()
    {
        return (TaggingService)getService(TAGGING_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getDeploymentService()
     */
    public DeploymentService getDeploymentService() 
    {
        return (DeploymentService) getService(DEPLOYMENT_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getWebProjectService()
     */
    public WebProjectService getWebProjectService()
    {
        return (WebProjectService)getService(WEBPROJECT_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getSandboxService()
     */
    public SandboxService getSandboxService()
    {
        return (SandboxService)getService(SANDBOX_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getAssetService()
     */
    public AssetService getAssetService()
    {
        return (AssetService)getService(ASSET_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getPreviewURIService()
     */
    public PreviewURIService getPreviewURIService()
    {
        return (PreviewURIService)getService(PREVIEW_URI_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getFormService()
     */
    public FormService getFormService() 
    {
        return (FormService)getService(FORM_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getRenditionService()
     */
    public RenditionService getRenditionService() 
    {
        return (RenditionService)getService(RENDITION_SERVICE);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getRatingService()
     */
    public RatingService getRatingService() 
    {
        return (RatingService)getService(RATING_SERVICE);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getNodeLocatorService()
     */
    public NodeLocatorService getNodeLocatorService() 
    {
        return (NodeLocatorService)getService(NODE_LOCATOR_SERVICE);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getBlogService()
     */
    public BlogService getBlogService() 
    {
        return (BlogService)getService(BLOG_SERVICE);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCalendarService()
     */
    public CalendarService getCalendarService() 
    {
        return (CalendarService)getService(CALENDAR_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getInvitationService()
     */
    public InvitationService getInvitationService() 
    {
         return (InvitationService)getService(INVITATION_SERVICE);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCMISService()
     */
    public CMISServices getCMISService() 
    {
         return (CMISServices)getService(CMIS_SERVICE);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCMISDictionaryService()
     */
    public CMISDictionaryService getCMISDictionaryService() 
    {
         return (CMISDictionaryService)getService(CMIS_DICTIONARY_SERVICE);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCMISQueryService()
     */
    public CMISQueryService getCMISQueryService() 
    {
         return (CMISQueryService)getService(CMIS_QUERY_SERVICE);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCMISQueryService()
     */
    public ImapService getImapService() 
    {
        return (ImapService)getService(IMAP_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getPublicServiceAccessService()
     */
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
}
