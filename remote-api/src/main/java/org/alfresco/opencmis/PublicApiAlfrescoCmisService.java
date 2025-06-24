/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.opencmis;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.tenant.Network;
import org.alfresco.repo.tenant.NetworksService;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;

/**
 * Override OpenCMIS service object - for public api
 * 
 * @author sglover
 * @since PublicApi1.0
 */
public class PublicApiAlfrescoCmisService extends AlfrescoCmisServiceImpl
{
    protected CMISConnector connector;
    protected TenantAdminService tenantAdminService;
    protected NetworksService networksService;

    public PublicApiAlfrescoCmisService(CMISConnector connector, TenantAdminService tenantAdminService, NetworksService networksService)
    {
        super(connector);

        this.connector = connector;
        this.networksService = networksService;
        this.tenantAdminService = tenantAdminService;
    }

    @Override
    public String create(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState,
            List<String> policies, ExtensionsData extension)
    {
        FileFilterMode.setClient(Client.cmis);
        try
        {
            return super.create(
                    repositoryId,
                    properties,
                    folderId,
                    contentStream,
                    versioningState,
                    policies,
                    extension);
        }
        finally
        {
            FileFilterMode.clearClient();
        }
    }

    /**
     * Overridden to capture content upload for publishing to analytics service.
     */
    @Override
    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState,
            List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension)
    {
        String newId = super.createDocument(
                repositoryId,
                properties,
                folderId,
                contentStream,
                versioningState,
                policies,
                addAces,
                removeAces,
                extension);
        return newId;
    }

    /**
     * Overridden to capture content upload for publishing to analytics service.
     */
    @Override
    public void setContentStream(String repositoryId, Holder<String> objectId,
            Boolean overwriteFlag, Holder<String> changeToken, ContentStream contentStream,
            ExtensionsData extension)
    {
        FileFilterMode.setClient(Client.cmis);
        try
        {
            super.setContentStream(repositoryId, objectId, overwriteFlag, changeToken, contentStream, extension);
        }
        finally
        {
            FileFilterMode.clearClient();
        }
    }

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension)
    {
        // for currently authenticated user
        PagingResults<Network> networks = networksService.getNetworks(new PagingRequest(0, Integer.MAX_VALUE));
        List<Network> page = networks.getPage();
        final List<RepositoryInfo> repoInfos = new ArrayList<RepositoryInfo>(page.size() + 1);
        for (Network network : page)
        {
            repoInfos.add(getRepositoryInfo(network));
        }
        return repoInfos;
    }

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension)
    {
        Network network = null;

        try
        {
            checkRepositoryId(repositoryId);
            network = networksService.getNetwork(repositoryId);
        }
        catch (Exception e)
        {
            // ACE-2540: Avoid information leak. Same response if repository does not exist or if user is not a member
            throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
        }

        return getRepositoryInfo(network);
    }

    private RepositoryInfo getRepositoryInfo(final Network network)
    {
        final String networkId = network.getTenantDomain();
        final String tenantDomain = (networkId.equals(TenantUtil.SYSTEM_TENANT) || networkId.equals(TenantUtil.DEFAULT_TENANT)) ? TenantService.DEFAULT_DOMAIN : networkId;

        return TenantUtil.runAsSystemTenant(new TenantRunAsWork<RepositoryInfo>() {
            public RepositoryInfo doWork()
            {
                RepositoryInfoImpl repoInfo = (RepositoryInfoImpl) connector.getRepositoryInfo(getContext().getCmisVersion());

                repoInfo.setId(!networkId.equals("") ? networkId : TenantUtil.SYSTEM_TENANT);
                repoInfo.setName(tenantDomain);
                repoInfo.setDescription(tenantDomain);

                return repoInfo;
            }
        }, tenantDomain);
    }

    @Override
    public void checkRepositoryId(String repositoryId)
    {
        if (repositoryId.equals(TenantUtil.DEFAULT_TENANT) || repositoryId.equals(TenantUtil.SYSTEM_TENANT))
        {
            // TODO check for super admin
            return;
        }

        if (!tenantAdminService.existsTenant(repositoryId) || !tenantAdminService.isEnabledTenant(repositoryId))
        {
            throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
        }
    }

    @Override
    public void beforeCall()
    {
        // NOTE: Don't invoke super beforeCall to exclude authentication which is already supported by
        // Web Script F/W
        // super.beforeCall();
    }

    @Override
    public void afterCall()
    {
        // NOTE: Don't invoke super afterCall to exclude authentication which is already supported by
        // Web Script F/W
        // super.afterCall();
    }

    @Override
    public void close()
    {
        super.close();
    }
}
