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
package org.alfresco.repo.activities.feed.local;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.activities.feed.RepoCtx;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.domain.activities.FeedControlDAO;
import org.alfresco.repo.domain.activities.FeedControlEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.ClassPathRepoTemplateLoader;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

/**
 * The local (ie. not grid) feed task processor is responsible for processing the individual feed job
 */
public class LocalFeedTaskProcessor extends FeedTaskProcessor implements ApplicationContextAware
{
    private static final Log logger = LogFactory.getLog(LocalFeedTaskProcessor.class);

    private ActivityPostDAO postDAO;
    private ActivityFeedDAO feedDAO;
    private FeedControlDAO feedControlDAO;

    // can call locally (instead of remote repo callback)
    private SiteService siteService;
    private NodeService nodeService;
    private ContentService contentService;
    private PermissionService permissionService;
    private SubscriptionService subscriptionService;
    private TenantService tenantService;

    private String defaultEncoding;
    private List<String> templateSearchPaths;
    private boolean useRemoteCallbacks;
    private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public void setPostDAO(ActivityPostDAO postDAO)
    {
        this.postDAO = postDAO;
    }

    public void setFeedDAO(ActivityFeedDAO feedDAO)
    {
        this.feedDAO = feedDAO;
    }

    public void setFeedControlDAO(FeedControlDAO feedControlDAO)
    {
        this.feedControlDAO = feedControlDAO;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setSubscriptionService(SubscriptionService subscriptionService)
    {
        this.subscriptionService = subscriptionService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setDefaultEncoding(String defaultEncoding)
    {
        this.defaultEncoding = defaultEncoding;
    }

    public void setTemplateSearchPaths(List<String> templateSearchPaths)
    {
        this.templateSearchPaths = templateSearchPaths;
    }

    public void setUseRemoteCallbacks(boolean useRemoteCallbacks)
    {
        this.useRemoteCallbacks = useRemoteCallbacks;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.resolver = applicationContext;
    }

    public void startTransaction() throws SQLException
    {
        // NOOP
    }

    public void commitTransaction() throws SQLException
    {
        // NOOP
    }

    public void rollbackTransaction() throws SQLException
    {
        // NOOP
    }

    public void endTransaction() throws SQLException
    {
        // NOOP
    }

    public List<ActivityPostEntity> selectPosts(ActivityPostEntity selector) throws SQLException
    {
        return postDAO.selectPosts(selector);
    }

    public long insertFeedEntry(ActivityFeedEntity feed) throws SQLException
    {
        return feedDAO.insertFeedEntry(feed);
    }

    public int updatePostStatus(long id, ActivityPostEntity.STATUS status) throws SQLException
    {
        return postDAO.updatePostStatus(id, status);
    }

    public List<FeedControlEntity> selectUserFeedControls(String userId) throws SQLException
    {
        return feedControlDAO.selectFeedControls(userId);
    }
    
    @Override
    protected String getTenantName(String name, String tenantDomain)
    {
        if (name == null)
        {
            return name;
        }
        
        String nameDomain = getTenantDomain(name);
        if (nameDomain.equals(TenantService.DEFAULT_DOMAIN))
        {
            if (! TenantService.DEFAULT_DOMAIN.equals(tenantDomain))
            {
                // no domain, so add it as a prefix (between two domain separators)
                name = TenantService.SEPARATOR + tenantDomain + TenantService.SEPARATOR + name;
            }
        }
        else
        {
            if (! tenantDomain.equals(nameDomain))
            {
                throw new AlfrescoRuntimeException("domain mismatch: expected = " + tenantDomain + ", actual = " + nameDomain);
            }
        }
        
        return name;
    }
    
    @Override
    protected String getTenantDomain(String name)
    {
        return tenantService.getDomain(name, false);
    }

    
    @Override
    protected Set<String> getSiteMembers(final RepoCtx ctx, String siteIdIn, final String tenantDomain) throws Exception
    {
        if (useRemoteCallbacks)
        {
            // as per 3.0, 3.1
            return super.getSiteMembers(ctx, siteIdIn, tenantDomain);
        } 
        else
        {
            final String siteId = tenantService.getBaseName(siteIdIn, true);
            
            // optimise for non-remote implementation - override remote repo callback (to "List Site Memberships" web script) with embedded call
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<String>>()
            {
                public Set<String> doWork() throws Exception
                {
                    Set<String> members = new HashSet<String>();
                    if ((siteId != null) && (siteId.length() != 0))
                    {
                        Map<String, String> mapResult = siteService.listMembers(siteId, null, null, 0, true);
                        
                        if ((mapResult != null) && (mapResult.size() != 0))
                        {
                            for (String userName : mapResult.keySet())
                            {
                                if (!ctx.isUserNamesAreCaseSensitive())
                                {
                                    userName = userName.toLowerCase();
                                }
                                members.add(userName);
                            }
                        }
                    }
                    
                    return members;
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
    }

    protected boolean canRead(RepoCtx ctx, final String connectedUser, Map<String, Object> model) throws Exception
    {
        if (useRemoteCallbacks)
        {
            // note: not implemented
            return super.canRead(ctx, connectedUser, model);
        }
        else
        {
            if (permissionService == null)
            {
                // if permission service not configured then fallback (ie. no read permission check)
                return true;
            }
            
            String nodeRefStr = (String) model.get(PostLookup.JSON_NODEREF);
            if (nodeRefStr == null)
            {
                nodeRefStr = (String) model.get(PostLookup.JSON_NODEREF_PARENT);
            }
            
            if (nodeRefStr != null)
            {
                final NodeRef nodeRef = new NodeRef(nodeRefStr);
                
                // MT share
                String tenantDomain = (String)model.get(PostLookup.JSON_TENANT_DOMAIN);
                if (tenantDomain == null) { tenantDomain = TenantService.DEFAULT_DOMAIN; }
                
                return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>()
                {
                    public Boolean doWork() throws Exception
                    {
                        return canReadImpl(connectedUser, nodeRef);
                    }
                }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
            }
            
            return true;
        }
    }

    private boolean canReadImpl(final String connectedUser, final NodeRef nodeRef) throws Exception
    {
        // check for read permission
        long start = System.currentTimeMillis();

        try
        {
            // note: deleted node does not exist (hence no permission, although default permission check would return true which is problematic)
            final NodeRef checkNodeRef;
            if (nodeService.exists(nodeRef))
            {
                checkNodeRef = nodeRef;
            } 
            else
            {
                // TODO: require ghosting - this is temp workaround (we should not rely on archive - may be permanently deleted, ie. not archived or already purged)
                NodeRef archiveNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, nodeRef.getId());
                if (!nodeService.exists(archiveNodeRef))
                {
                    return false;
                }
                checkNodeRef = archiveNodeRef;
            }

            if (connectedUser.equals(""))
            {
                // site feed (public site)
                Set<AccessPermission> perms = permissionService.getAllSetPermissions(checkNodeRef);
                for (AccessPermission perm : perms)
                {
                    if (perm.getAuthority().equals(PermissionService.ALL_AUTHORITIES) && 
                        perm.getAuthorityType().equals(AuthorityType.EVERYONE) && 
                        perm.getPermission().equals(PermissionService.READ_PERMISSIONS) && 
                        perm.getAccessStatus().equals(AccessStatus.ALLOWED))
                    {
                        return true;
                    }
                }
                return false;
            } 
            else
            {
                // user feed
                return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>()
                {
                    public Boolean doWork() throws Exception
                    {
                        return (permissionService.hasPermission(checkNodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
                    }
                }, connectedUser);
            }
        }
        finally
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("canRead: " + nodeRef + " in " + (System.currentTimeMillis() - start) + " msecs");
            }
        }
    }

    @Override
    protected Map<String, List<String>> getActivityTypeTemplates(String repoEndPoint, String ticket, String subPath) throws Exception
    {
        if (useRemoteCallbacks)
        {
            // as per 3.0, 3.1
            return super.getActivityTypeTemplates(repoEndPoint, ticket, subPath);
        } 
        else
        {
            // optimisation - override remote repo callback (to "Activities Templates" web script) with local/embedded call

            String path = "/";
            String templatePattern = "*.ftl";

            if ((subPath != null) && (subPath.length() > 0))
            {
                subPath = subPath + "*";

                int idx = subPath.lastIndexOf("/");
                if (idx != -1)
                {
                    path = subPath.substring(0, idx);
                    templatePattern = subPath.substring(idx + 1) + ".ftl";
                }
            }

            List<String> allTemplateNames = getDocumentPaths(path, false, templatePattern);

            return getActivityTemplates(allTemplateNames);
        }
    }

    @Override
    protected Configuration getFreemarkerConfiguration(RepoCtx ctx)
    {
        if (useRemoteCallbacks)
        {
            // as per 3.0, 3.1
            return super.getFreemarkerConfiguration(ctx);
        } 
        else
        {
            Configuration cfg = new Configuration();
            cfg.setObjectWrapper(new DefaultObjectWrapper());

            cfg.setTemplateLoader(new ClassPathRepoTemplateLoader(nodeService, contentService, defaultEncoding));

            // TODO review i18n
            cfg.setLocalizedLookup(false);

            return cfg;
        }
    }

    // Helper to get template document paths
    private List<String> getDocumentPaths(String path, boolean includeSubPaths, String documentPattern)
    {
        if ((path == null) || (path.length() == 0))
        {
            path = "/";
        }

        if (!path.startsWith("/"))
        {
            path = "/" + path;
        }

        if (!path.endsWith("/"))
        {
            path = path + "/";
        }

        if ((documentPattern == null) || (documentPattern.length() == 0))
        {
            documentPattern = "*";
        }

        List<String> documentPaths = new ArrayList<String>(0);

        for (String classPath : templateSearchPaths)
        {
            final StringBuilder pattern = new StringBuilder(128);
            pattern.append("classpath*:").append(classPath)
                   .append(path)
                   .append((includeSubPaths ? "**/" : ""))
                   .append(documentPattern);

            try
            {
                documentPaths.addAll(getPaths(pattern.toString(), classPath));
            } 
            catch (IOException e)
            {
                // Note: Ignore: no documents found
            }
        }

        return documentPaths;
    }

    // Helper to return a list of resource document paths based on a search pattern.
    private List<String> getPaths(String pattern, String classPath) throws IOException
    {
        Resource[] resources = resolver.getResources(pattern);
        List<String> documentPaths = new ArrayList<String>(resources.length);
        for (Resource resource : resources)
        {
            String resourcePath = resource.getURL().toExternalForm();
            
            int idx = resourcePath.lastIndexOf(classPath);
            if (idx != -1)
            {
                String documentPath = resourcePath.substring(idx);
                documentPath = documentPath.replace('\\', '/');
                if (logger.isTraceEnabled())
                {
                    logger.trace("Item resource path: " + resourcePath + " , item path: " + documentPath);
                }
                documentPaths.add(documentPath);
            }
        }
        return documentPaths;
    }
    
    protected Set<String> getFollowers(final String userId, String tenantDomain) throws Exception
    {
        final Set<String> result = new HashSet<String>();
        
        if (subscriptionService.isActive())
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    PagingFollowingResults fr = subscriptionService.getFollowers(userId, new PagingRequest(1000000, null));
                    
                    if (fr.getPage() != null)
                    {
                        result.addAll(fr.getPage());
                    }
                    
                    return null;
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
        
        return result;
    }
}
