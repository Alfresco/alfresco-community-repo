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
package org.alfresco.rest.api.discovery;

import org.alfresco.rest.api.model.DiscoveryDetails;
import org.alfresco.rest.api.model.ModulePackage;
import org.alfresco.rest.api.model.RepositoryInfo;
import org.alfresco.rest.api.model.RepositoryInfo.LicenseInfo;
import org.alfresco.rest.api.model.RepositoryInfo.StatusInfo;
import org.alfresco.rest.api.model.RepositoryInfo.VersionInfo;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.rest.framework.tools.ResponseWriter;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.PropertyCheck;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class DiscoveryApiWebscript extends AbstractWebScript implements RecognizedParamsExtractor, ResponseWriter, InitializingBean
{
    private DescriptorService descriptorService;
    private RepoAdminService repoAdminService;
    private AuditService auditService;
    private QuickShareService quickShareService;
    private ModuleService moduleService;
    private ApiAssistant assistant;
    private ThumbnailService thumbnailService;

    private boolean enabled = true;
    private final static String DISABLED = "Not Implemented";

    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }

    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareService = quickShareService;
    }

    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }

    public void setAssistant(ApiAssistant assistant)
    {
        this.assistant = assistant;
    }

    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "descriptorService", descriptorService);
        PropertyCheck.mandatory(this, "repoAdminService", repoAdminService);
        PropertyCheck.mandatory(this, "auditService", auditService);
        PropertyCheck.mandatory(this, "quickShareService", quickShareService);
        PropertyCheck.mandatory(this, "moduleService", moduleService);
        PropertyCheck.mandatory(this, "assistant", assistant);
        PropertyCheck.mandatory(this, "thumbnailService", thumbnailService);
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException
    {
        try
        {
            checkEnabled();

            DiscoveryDetails discoveryDetails = new DiscoveryDetails(getRepositoryInfo());
            // Write response
            setResponse(webScriptResponse, DEFAULT_SUCCESS);
            renderJsonResponse(webScriptResponse, discoveryDetails, assistant.getJsonHelper());
        }
        catch (Exception exception)
        {
            renderException(exception, webScriptResponse, assistant);
        }
    }

    protected RepositoryInfo getRepositoryInfo()
    {
        LicenseInfo licenseInfo = null;
        if(descriptorService.getLicenseDescriptor() != null)
        {
            licenseInfo = new LicenseInfo(descriptorService.getLicenseDescriptor());
        }
        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
        return new RepositoryInfo()
                    .setEdition(serverDescriptor.getEdition())
                    .setVersion(new VersionInfo(serverDescriptor))
                    .setLicense(licenseInfo)
                    .setModules(getModules())
                    .setStatus(new StatusInfo()
                                .setReadOnly(repoAdminService.getUsage().isReadOnly())
                                .setAuditEnabled(auditService.isAuditEnabled())
                                .setQuickShareEnabled(quickShareService.isQuickShareEnabled())
                                .setThumbnailGenerationEnabled(thumbnailService.getThumbnailsEnabled()));
    }

    private List<ModulePackage> getModules()
    {
        List<ModuleDetails> details = moduleService.getAllModules();
        if (details.isEmpty())
        {
            return null;
        }
        List<ModulePackage> packages = new ArrayList<>(details.size());
        for (ModuleDetails detail : details)
        {
            packages.add(ModulePackage.fromModuleDetails(detail));
        }
        return packages;
    }

    @Override
    public void renderJsonResponse(final WebScriptResponse res, final Object toSerialize, final JacksonHelper jsonHelper) throws IOException
    {
        jsonHelper.withWriter(res.getOutputStream(), (generator, objectMapper) -> {
            JSONObject obj = new JSONObject();
            obj.put("entry", toSerialize);
            objectMapper.writeValue(generator, obj);
        });
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    private void checkEnabled()
    {
        if (!enabled)
        {
            throw new DisabledServiceException(DISABLED);
        }
    }
}
