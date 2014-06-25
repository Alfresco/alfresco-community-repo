package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

public class RenameSiteAuthorityDisplayName  extends AbstractPatch
{
    /** The title we give to the batch process in progress messages / JMX. */
    private static final String SUCCESS_MSG = "patch.renameSiteAuthorityDisplayName.result";
    private static final int BATCH_THREADS = 4;
    private static final int BATCH_SIZE = 250;
    
    /** Services */
    private SiteService siteService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private RuleService ruleService;
    
    /** The progress_logger. */
    private static Log progress_logger = LogFactory.getLog(PatchExecuter.class);
    
    /**
     * Set site service
     * 
     * @param siteService   the site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * Set the permission service
     * 
     * @param permissionService     the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * The authority service
     * 
     * @param authorityService  the authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * Sets the rule service.
     * 
     * @param ruleService
     *            the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    @Override
    protected String applyInternal() throws Exception
    {
     // NOTE: SiteService is not currently MT-enabled (eg. getSiteRoot) so skip if applied to tenant
        if (AuthenticationUtil.isRunAsUserTheSystemUser() || !AuthenticationUtil.isMtEnabled())
        {
            // Set all the sites in the repository
            List<SiteInfo> sites = this.siteService.listSites(null, null);
            renameDispayNames(sites);
        }
        // Report status
        return I18NUtil.getMessage(SUCCESS_MSG);
    }
    
    
    
    
    /**
     * Rename display names of authorities of sites.
     * 
     * @param siteInfos
     *            list of sites
     */
    private void renameDispayNames(final List<SiteInfo> siteInfos)
    {
        final String tenantDomain = tenantAdminService.getCurrentUserDomain();
        
        final Iterator<SiteInfo> pathItr = siteInfos.listIterator();
        
        BatchProcessWorkProvider<SiteInfo> siteWorkProvider = new BatchProcessWorkProvider<SiteInfo>()
        {
            
            @Override
            public int getTotalEstimatedWorkSize()
            {
                return siteInfos.size();
            }
            
            @Override
            public Collection<SiteInfo> getNextWork()
            {
                int batchCount = 0;
                
                List<SiteInfo> nodes = new ArrayList<SiteInfo>(BATCH_SIZE);
                while (pathItr.hasNext() && batchCount++ != BATCH_SIZE)
                {
                    nodes.add(pathItr.next());
                }
                return nodes;
            }
        };
        
        
        // prepare the batch processor and worker object
        BatchProcessor<SiteInfo> siteBatchProcessor = new BatchProcessor<SiteInfo>(
                "RenameSiteAuthorityDisplayName",
                this.transactionHelper,
                siteWorkProvider,
                BATCH_THREADS,
                BATCH_SIZE,
                this.applicationEventPublisher,
                progress_logger,
                BATCH_SIZE * 10);
        
        BatchProcessWorker<SiteInfo> worker = new BatchProcessWorker<SiteInfo>()
        {

            @Override
            public String getIdentifier(SiteInfo entry)
            {
                return entry.getShortName();
            }

            @Override
            public void beforeProcess() throws Throwable
            {
             // Disable rules
                ruleService.disableRules();
                // Authentication
                String systemUser = AuthenticationUtil.getSystemUserName();
                systemUser = tenantAdminService.getDomainUser(systemUser, tenantDomain);
                AuthenticationUtil.setRunAsUser(systemUser);
            }
            
            @Override
            public void afterProcess() throws Throwable
            {
                // Enable rules
                ruleService.enableRules();
                // Clear authentication
                AuthenticationUtil.clearCurrentSecurityContext();
            }
            
            @Override
            public void process(SiteInfo siteInfo) throws Throwable
            {
             // Set all the permissions of site
                Set<AccessPermission> sitePermissions = permissionService.getAllSetPermissions(siteInfo.getNodeRef());
                for (AccessPermission sitePermission : sitePermissions)
                {
                    // Use only GROUP authority
                    if (sitePermission.getAuthorityType() == AuthorityType.GROUP)
                    {
                        String authorityName = sitePermission.getAuthority();
                        String currDisplayName = authorityService.getAuthorityDisplayName(authorityName);
                        String necessaryName = ((SiteServiceImpl) siteService).getSiteRoleGroup(siteInfo.getShortName(), sitePermission.getPermission(),  false);
                        String alternativeName = ((SiteServiceImpl) siteService).getSiteRoleGroup(siteInfo.getShortName(), sitePermission.getPermission(),  true);
                        // check for correct displayName
                        if ((!necessaryName.equalsIgnoreCase(currDisplayName)) || (!alternativeName.equalsIgnoreCase(currDisplayName)))
                        {
                            // fix incorrect display name
                            authorityService.setAuthorityDisplayName(authorityName, necessaryName);
                        }
                    }
                }
            }
        };
        
        siteBatchProcessor.process(worker, true);
    }
}
