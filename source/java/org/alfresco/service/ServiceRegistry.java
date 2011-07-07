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
package org.alfresco.service;

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
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
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


/**
 * This interface represents the registry of public Repository Services.
 * The registry provides meta-data about each service and provides
 * access to the service interface.
 *
 * @author David Caruana
 */
public interface ServiceRegistry
{
    // Service Bean Names

    static final String SERVICE_REGISTRY = "ServiceRegistry";

    static final QName REGISTRY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ServiceRegistry");
    static final QName DESCRIPTOR_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "DescriptorService");
    static final QName TRANSACTION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "TransactionService");
    static final QName RETRYING_TRANSACTION_HELPER = QName.createQName(NamespaceService.ALFRESCO_URI, "retryingTransactionHelper");
    static final QName AUTHENTICATION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AuthenticationService");
    static final QName NAMESPACE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "NamespaceService");
    static final QName DICTIONARY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "DictionaryService");
    static final QName NODE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "NodeService");
    static final QName CONTENT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ContentService");
    static final QName MIMETYPE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "MimetypeService");
    static final QName CONTENT_FILTER_LANGUAGES_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ContentFilterLanguagesService");
    static final QName MULTILINGUAL_CONTENT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "MultilingualContentService");
    static final QName EDITION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "EditionService");
    static final QName SEARCH_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "SearchService");
    static final QName CATEGORY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CategoryService");
    static final QName COPY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CopyService");
    static final QName LOCK_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "LockService");
    static final QName JOB_LOCK_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "JobLockService");
    static final QName VERSION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "VersionService");
    static final QName COCI_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CheckoutCheckinService");
    static final QName RULE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RuleService");
    static final QName IMPORTER_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ImporterService");
    static final QName EXPORTER_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ExporterService");
    static final QName ACTION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ActionService");
    static final QName PERMISSIONS_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "PermissionService");
    static final QName AUTHORITY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AuthorityService");
    static final QName TEMPLATE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "TemplateService");
    static final QName FILE_FOLDER_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "FileFolderService");
    static final QName SCRIPT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ScriptService");
    static final QName WORKFLOW_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "WorkflowService");
    static final QName AUDIT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AuditService");
    static final QName OWNABLE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "OwnableService");
    static final QName PERSON_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "PersonService");
    static final QName SITE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "SiteService");
    static final QName ATTRIBUTE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AttributeService");
    static final QName THUMBNAIL_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ThumbnailService");
    static final QName TAGGING_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "TaggingService");
    static final QName FORM_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "FormService");
    static final QName INVITATION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "InvitationService");
    static final QName PREFERENCE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "PreferenceService");
    static final QName RENDITION_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RenditionService");
    static final QName RATING_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RatingService");
    static final QName REPO_ADMIN_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "RepoAdminService");
    static final QName NODE_LOCATOR_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "nodeLocatorService");
    static final QName CALENDAR_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CalendarService");
    
    // WCM / AVM
    static final QName AVM_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AVMService");
    static final QName AVM_LOCKING_AWARE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AVMLockingAwareService");
    static final QName AVM_SYNC_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AVMSyncService");
    static final QName CROSS_REPO_COPY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CrossRepositoryCopyService");
    static final QName AVM_LOCKING_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AVMLockingService");
    static final QName VIRT_SERVER_REGISTRY = QName.createQName(NamespaceService.ALFRESCO_URI, "VirtServerRegistry");
    static final QName DEPLOYMENT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "DeploymentService");
    static final QName WEBPROJECT_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "WebProjectService");
    static final QName SANDBOX_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "SandboxService");
    static final QName ASSET_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "AssetService");
    static final QName PREVIEW_URI_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "WCMPreviewURIService");
    
    // CMIS
    static final QName CMIS_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CMISService");
    static final QName CMIS_DICTIONARY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CMISDictionaryService");
    static final QName CMIS_QUERY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "CMISQueryService");
    static final QName IMAP_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "ImapService");
    
    static final QName PUBLIC_SERVICE_ACCESS_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "PublicServiceAccessService");
    
    

    /**
     * Get the list of services provided by the Repository
     *
     * @return  list of provided Services
     */
    @NotAuditable
    Collection<QName> getServices();

    /**
     * Is the specified service provided by the Repository?
     *
     * @param service  name of service to test provision of
     * @return true => provided, false => not provided
     */
    @NotAuditable
    boolean isServiceProvided(QName service);

    /**
     * Get the specified service.
     *
     * @param service  name of service to retrieve
     * @return the service interface (must cast to interface as described in service meta-data)
     */
    @NotAuditable
    Object getService(QName service);

    /**
     * @return the descriptor service
     */
    @NotAuditable
    DescriptorService getDescriptorService();

    /**
     * @return the transaction service
     */
    @NotAuditable
    TransactionService getTransactionService();

    /**
     * @return the transaction service
     */
    @NotAuditable
    RetryingTransactionHelper getRetryingTransactionHelper();

    /**
     * @return the namespace service (or null, if one is not provided)
     */
    @NotAuditable
    NamespaceService getNamespaceService();

    /**
     * @return the authentication service (or null, if one is not provided)
     */
    @NotAuditable
    MutableAuthenticationService getAuthenticationService();

    /**
     * @return the node service (or null, if one is not provided)
     */
    @NotAuditable
    NodeService getNodeService();

    /**
     * @return the content service (or null, if one is not provided)
     */
    @NotAuditable
    ContentService getContentService();

    /**
     * @return the mimetype service (or null, if one is not provided)
     */
    @NotAuditable
    MimetypeService getMimetypeService();

    /**
     * @return the content filter languages service (or null, if one is not provided)
     */
    @NotAuditable
    ContentFilterLanguagesService getContentFilterLanguagesService();

    /**
     * @return the search service (or null, if one is not provided)
     */
    @NotAuditable
    SearchService getSearchService();

    /**
     * @return the version service (or null, if one is not provided)
     */
    @NotAuditable
    VersionService getVersionService();

    /**
     * @return the lock service (or null, if one is not provided)
     */
    @NotAuditable
    LockService getLockService();

    /**
     * @return the job lock service (or null, if one is not provided)
     */
    @NotAuditable
    JobLockService getJobLockService();

    /**
     * @return the dictionary service (or null, if one is not provided)
     */
    @NotAuditable
    DictionaryService getDictionaryService();

    /**
     * @return the copy service (or null, if one is not provided)
     */
    @NotAuditable
    CopyService getCopyService();

    /**
     * @return the checkout / checkin service (or null, if one is not provided)
     */
    @NotAuditable
    CheckOutCheckInService getCheckOutCheckInService();

    /**
     * @return the category service (or null, if one is not provided)
     */
    @NotAuditable
    CategoryService getCategoryService();

    /**
     * @return the importer service or null if not present
     */
    @NotAuditable
    ImporterService getImporterService();

    /**
     * @return the exporter service or null if not present
     */
    @NotAuditable
    ExporterService getExporterService();

    /**
     * @return the rule service (or null, if one is not provided)
     */
    @NotAuditable
    RuleService getRuleService();

    /**
     * @return the action service (or null if one is not provided)
     */
    @NotAuditable
    ActionService getActionService();

    /**
     * @return the permission service (or null if one is not provided)
     */
    @NotAuditable
    PermissionService getPermissionService();

    /**
     * @return the authority service (or null if one is not provided)
     */
    @NotAuditable
    AuthorityService getAuthorityService();

    /**
     * @return the template service (or null if one is not provided)
     */
    @NotAuditable
    TemplateService getTemplateService();

    /**
     * @return the file-folder manipulation service (or null if one is not provided)
     */
    @NotAuditable
    FileFolderService getFileFolderService();

    /**
     * @return the script execution service (or null if one is not provided)
     */
    @NotAuditable
    ScriptService getScriptService();

    /**
     * @return the workflow service (or null if one is not provided)
     */
    @NotAuditable
    WorkflowService getWorkflowService();

    /**
     * @return the audit service (or null if one is not provided)
     */
    @NotAuditable
    AuditService getAuditService();

    /**
     * Get the AVMService.
     * @return The AVM service (or null if one is not provided);
     */
    @NotAuditable
    AVMService getAVMService();

    /**
    * Get the AVMLockingAwareService.
     * @return The AVM locking aware service (or null if one is not provided);    
     */
    @NotAuditable
    AVMService getAVMLockingAwareService();
    
    /**
     * Get the AVM Sync Service.
     * @return The AVM Sync Service.
     */
    @NotAuditable
    AVMSyncService getAVMSyncService();

    /**
     * Get the ownable service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    OwnableService getOwnableService();

    /**
     * Get the person service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    PersonService getPersonService();

    /**
     * Get the site service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    SiteService getSiteService();

    /**
     * Get the cross repository copy service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    CrossRepositoryCopyService getCrossRepositoryCopyService();

    /**
     * Get the attribute service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    AttributeService getAttributeService();

    /**
     * Get the AVM locking service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    AVMLockingService getAVMLockingService();

    /**
     * Get the Virtualisation Server registry service bean
     * @return
     */
    @NotAuditable
    VirtServerRegistry getVirtServerRegistry();

    /**
     * Get the Multilingual Content Service
     * @return
     */
    @NotAuditable
    MultilingualContentService getMultilingualContentService();

    /**
     * Get the Edition Service
     * @return
     */
    @NotAuditable
    EditionService getEditionService();
    
    /**
     * Get the Thumbnail Service
     * @return
     */
    @NotAuditable
    ThumbnailService getThumbnailService();
    
    /**
     * Get the Tagging Service
     * @return
     */
    @NotAuditable
    TaggingService getTaggingService();
    
    /**
     * Get the WCM Deployment Service
     * @return the deployment service (or null, if one is not provided)
     */
    @NotAuditable
    DeploymentService getDeploymentService();
    
    /**
     * Get the WCM WebProject Service
     * @return
     */
    @NotAuditable
    WebProjectService getWebProjectService();
    
    /**
     * Get the WCM Sandbox Service
     * @return
     */
    @NotAuditable
    SandboxService getSandboxService();
    
    /**
     * Get the WCM Asset Service
     * @return
     */
    @NotAuditable
    AssetService getAssetService();
    
    /**
     * Get the WCM Preview URI Service
     * @return
     */
    @NotAuditable
    PreviewURIService getPreviewURIService();
    
    /**
     * Get the form service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    FormService getFormService();

    /**
     * Get the rendition service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    RenditionService getRenditionService();

    /**
     * Get the rating service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    RatingService getRatingService();
    
    /**
     * Get the node locator service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    NodeLocatorService getNodeLocatorService();
    
    /**
     * Get the calendar service (or null if one is not provided)
     * @return
     */
    @NotAuditable
    CalendarService getCalendarService();
    
    /**
     * Get the invitation service (or null if one is not provided)
     * @return the invitation service
     */
    @NotAuditable
    InvitationService getInvitationService();

    /**
     * Get the CMIS service (or null if one is not provided)
     * @return the CMIS service
     */
    @NotAuditable
    CMISServices getCMISService();
    
    /**
     * Get the CMIS Dictionary service (or null if one is not provided)
     * @return the CMIS Dictionary service
     */
    @NotAuditable
    CMISDictionaryService getCMISDictionaryService();

    /**
     * Get the CMIS Query service (or null if one is not provided)
     * @return the CMIS Query service
     */
    @NotAuditable
    CMISQueryService getCMISQueryService();

    /**
     * Get the IMAP service (or null if one is not provided)
     * @return the IMAP service
     */
    @NotAuditable
    ImapService getImapService();
    
    /**
     * Get the IMAP service (or null if one is not provided)
     * @return the IMAP service
     */
    @NotAuditable
    PublicServiceAccessService getPublicServiceAccessService();
    
    /**
     * Get the repo admin service (or null if one is not provided)
     * @return the invitation service
     */
    @NotAuditable
    RepoAdminService getRepoAdminService();

    /**
     * Get the sys admin params helper bean.
     * @return the sys admin params bean.
     */
    @NotAuditable
    SysAdminParams getSysAdminParams();
}
