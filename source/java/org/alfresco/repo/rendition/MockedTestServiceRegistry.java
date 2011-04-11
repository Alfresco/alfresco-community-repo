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
package org.alfresco.repo.rendition;

import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISServices;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.repo.lock.JobLockService;
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

public class MockedTestServiceRegistry implements ServiceRegistry
{
    private final ActionService actionService = mock(ActionService.class);
    private final ContentService contentService = mock(ContentService.class);
    private final NodeService nodeService = mock(NodeService.class);
    private final TemplateService templateService = mock(TemplateService.class);
    private final PersonService personService = mock(PersonService.class);
    private final MutableAuthenticationService authenticationService = mock(MutableAuthenticationService.class);
    private final NamespaceService namespaceService = mock(NamespaceService.class);
    
    public boolean isServiceProvided(QName service)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    
    public WorkflowService getWorkflowService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public WebProjectService getWebProjectService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public VirtServerRegistry getVirtServerRegistry()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public VersionService getVersionService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public TransactionService getTransactionService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public ThumbnailService getThumbnailService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public TemplateService getTemplateService()
    {
        return this.templateService;
    }
    
    
    public TaggingService getTaggingService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public SiteService getSiteService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public Collection<QName> getServices()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public Object getService(QName service)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public SearchService getSearchService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public ScriptService getScriptService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public SandboxService getSandboxService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public RuleService getRuleService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public PublicServiceAccessService getPublicServiceAccessService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public PreviewURIService getPreviewURIService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public PersonService getPersonService()
    {
        return personService;
    }
    
    
    public PermissionService getPermissionService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public OwnableService getOwnableService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public NodeService getNodeService()
    {
        return nodeService;
    }
    
    
    public NamespaceService getNamespaceService()
    {
        // TODO Auto-generated method stub
        return namespaceService;
    }
    
    
    public MultilingualContentService getMultilingualContentService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public MimetypeService getMimetypeService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public LockService getLockService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public JobLockService getJobLockService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public InvitationService getInvitationService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public ImporterService getImporterService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public ImapService getImapService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public FormService getFormService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public RenditionService getRenditionService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public RatingService getRatingService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public FileFolderService getFileFolderService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public ExporterService getExporterService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public EditionService getEditionService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public DictionaryService getDictionaryService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public DescriptorService getDescriptorService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public DeploymentService getDeploymentService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public CrossRepositoryCopyService getCrossRepositoryCopyService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public CopyService getCopyService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public ContentService getContentService()
    {
        // TODO Auto-generated method stub
        return contentService;
    }
    
    
    public ContentFilterLanguagesService getContentFilterLanguagesService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public CheckOutCheckInService getCheckOutCheckInService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public CategoryService getCategoryService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public CMISServices getCMISService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public CMISQueryService getCMISQueryService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public CMISDictionaryService getCMISDictionaryService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public AuthorityService getAuthorityService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public MutableAuthenticationService getAuthenticationService()
    {
        // TODO Auto-generated method stub
        return authenticationService;
    }
    
    
    public AuditService getAuditService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public AttributeService getAttributeService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public AssetService getAssetService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public ActionService getActionService()
    {
        // TODO Auto-generated method stub
        return actionService;
    }
    
    
    public AVMSyncService getAVMSyncService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public AVMService getAVMService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public AVMLockingService getAVMLockingService()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public AVMService getAVMLockingAwareService()
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public RepoAdminService getRepoAdminService()
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public SysAdminParams getSysAdminParams()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
