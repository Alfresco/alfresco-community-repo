/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.rendition;

import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryService;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rendition2.RenditionService2;
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
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.DocumentLinkService;
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

public class MockedTestServiceRegistry implements ServiceRegistry
{
    private final ActionService actionService = mock(ActionService.class);
    private final ContentService contentService = mock(ContentService.class);
    private final NodeService nodeService = mock(NodeService.class);
    private final TemplateService templateService = mock(TemplateService.class);
    private final PersonService personService = mock(PersonService.class);
    private final MutableAuthenticationService authenticationService = mock(MutableAuthenticationService.class);
    private final NamespaceService namespaceService = mock(NamespaceService.class);

    @Override
    public boolean isServiceProvided(QName service)
    {
        // A mock response
        return false;
    }

    @Override
    public WorkflowService getWorkflowService()
    {
        // A mock response
        return null;
    }

    @Override
    public NotificationService getNotificationService()
    {
        // A mock response
        return null;
    }

    @Override
    public VersionService getVersionService()
    {
        // A mock response
        return null;
    }

    @Override
    public TransactionService getTransactionService()
    {
        // A mock response
        return null;
    }

    @Override
    public ThumbnailService getThumbnailService()
    {
        // A mock response
        return null;
    }

    @Override
    public TemplateService getTemplateService()
    {
        return this.templateService;
    }

    @Override
    public TaggingService getTaggingService()
    {
        // A mock response
        return null;
    }

    @Override
    public SiteService getSiteService()
    {
        // A mock response
        return null;
    }

    @Override
    public Collection<QName> getServices()
    {
        // A mock response
        return null;
    }

    @Override
    public Object getService(QName service)
    {
        // A mock response
        return null;
    }

    @Override
    public SearchService getSearchService()
    {
        // A mock response
        return null;
    }

    @Override
    public ScriptService getScriptService()
    {
        // A mock response
        return null;
    }

    @Override
    public RuleService getRuleService()
    {
        // A mock response
        return null;
    }

    @Override
    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        // A mock response
        return null;
    }

    @Override
    public PublicServiceAccessService getPublicServiceAccessService()
    {
        // A mock response
        return null;
    }

    @Override
    public PersonService getPersonService()
    {
        return personService;
    }

    @Override
    public PermissionService getPermissionService()
    {
        // A mock response
        return null;
    }

    @Override
    public OwnableService getOwnableService()
    {
        // A mock response
        return null;
    }

    @Override
    public NodeService getNodeService()
    {
        return nodeService;
    }

    @Override
    public NamespaceService getNamespaceService()
    {
        // A mock response
        return namespaceService;
    }

    @Override
    public MultilingualContentService getMultilingualContentService()
    {
        // A mock response
        return null;
    }

    @Override
    public MimetypeService getMimetypeService()
    {
        // A mock response
        return null;
    }

    @Override
    public LockService getLockService()
    {
        // A mock response
        return null;
    }

    @Override
    public JobLockService getJobLockService()
    {
        // A mock response
        return null;
    }

    @Override
    public InvitationService getInvitationService()
    {
        // A mock response
        return null;
    }

    @Override
    public ImporterService getImporterService()
    {
        // A mock response
        return null;
    }

    @Override
    public ImapService getImapService()
    {
        // A mock response
        return null;
    }

    @Override
    public FormService getFormService()
    {
        // A mock response
        return null;
    }

    @Override
    public RenditionService getRenditionService()
    {
        // A mock response
        return null;
    }

    @Override
    public RatingService getRatingService()
    {
        // A mock response
        return null;
    }

    @Override
    public NodeLocatorService getNodeLocatorService()
    {
        // A mock response
        return null;
    }

    @Override
    public CalendarService getCalendarService()
    {
        // A mock response
        return null;
    }

    @Override
    public FileFolderService getFileFolderService()
    {
        // A mock response
        return null;
    }

    @Override
    public ExporterService getExporterService()
    {
        // A mock response
        return null;
    }

    @Override
    public EditionService getEditionService()
    {
        // A mock response
        return null;
    }

    @Override
    public DictionaryService getDictionaryService()
    {
        // A mock response
        return null;
    }

    @Override
    public DescriptorService getDescriptorService()
    {
        // A mock response
        return null;
    }

    @Override
    public CopyService getCopyService()
    {
        // A mock response
        return null;
    }

    @Override
    public ContentService getContentService()
    {
        // A mock response
        return contentService;
    }

    @Override
    public ContentFilterLanguagesService getContentFilterLanguagesService()
    {
        // A mock response
        return null;
    }

    @Override
    public CheckOutCheckInService getCheckOutCheckInService()
    {
        // A mock response
        return null;
    }

    @Override
    public CategoryService getCategoryService()
    {
        // A mock response
        return null;
    }

    @Override
    public CMISQueryService getCMISQueryService()
    {
        // A mock response
        return null;
    }

    @Override
    public CMISDictionaryService getCMISDictionaryService()
    {
        // A mock response
        return null;
    }

    @Override
    public AuthorityService getAuthorityService()
    {
        // A mock response
        return null;
    }

    @Override
    public MutableAuthenticationService getAuthenticationService()
    {
        // A mock response
        return authenticationService;
    }

    @Override
    public AuditService getAuditService()
    {
        // A mock response
        return null;
    }

    @Override
    public AttributeService getAttributeService()
    {
        // A mock response
        return null;
    }

    @Override
    public ActionService getActionService()
    {
        // A mock response
        return actionService;
    }

    @Override
    public RepoAdminService getRepoAdminService()
    {
        // A mock response
        return null;
    }

    @Override
    public SysAdminParams getSysAdminParams()
    {
        // A mock response
        return null;
    }

    @Override
    public BlogService getBlogService()
    {
        // A mock response
        return null;
    }

    @Override
    public WebDavService getWebDavService()
    {
        // A mock response
        return null;
    }

    @Override
    public SolrFacetHelper getSolrFacetHelper()
    {
        // A mock response
        return null;
    }

    @Override
    public FacetLabelDisplayHandlerRegistry getFacetLabelDisplayHandlerRegistry()
    {
        // A mock response
        return null;
    }

    @Override
    public ModuleService getModuleService()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageService getMessageService()
    {
        // A mock response
        return null;
    }

    @Override
    public DocumentLinkService getDocumentLinkService()
    {
        // A mock response
        return null;
    }

    @Override
    public PolicyComponent getPolicyComponent()
    {
        // A mock response
        return null;
    }

    @Override
    public RenditionService2 getRenditionService2()
    {
        // A mock response
        return null;
    }
}
