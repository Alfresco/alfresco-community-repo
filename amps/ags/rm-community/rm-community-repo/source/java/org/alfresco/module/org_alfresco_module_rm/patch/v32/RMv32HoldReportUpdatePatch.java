/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.patch.v32;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;

/**
 * Patch to update the template for generating the hold report
 * See: https://issues.alfresco.com/jira/browse/RM-7003
 *
 * @author Ramona Popa
 * @since 3.2
 */
public class RMv32HoldReportUpdatePatch extends AbstractModulePatch
{
    /**
     * Hold report template path
     */
    private static final String HOLD_REPORT_TEMPLATE_PATH = "alfresco/module/org_alfresco_module_rm/bootstrap/report/report_rmr_holdReport.html.ftl";

    /**
     * Hold report template config node IDs
     */
    private static final NodeRef HOLD_REPORT = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rmr_holdReport");

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Content service
     */
    private ContentService contentService;

    /**
     * Version service
     */
    private VersionService versionService;

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param versionService version service
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        if (nodeService.exists(HOLD_REPORT))
        {

            // Make sure the template is versionable
            if (!nodeService.hasAspect(HOLD_REPORT, ContentModel.ASPECT_VERSIONABLE))
            {
                nodeService.addAspect(HOLD_REPORT, ContentModel.ASPECT_VERSIONABLE, null);

                // Create version (before template is updated)
                Map<String, Serializable> versionProperties = new HashMap<>(2);
                versionProperties.put(Version.PROP_DESCRIPTION, "Template updated");
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionService.createVersion(HOLD_REPORT, versionProperties);
            }

            // Update the content of the template
            InputStream is = getClass().getClassLoader().getResourceAsStream(HOLD_REPORT_TEMPLATE_PATH);
            ContentWriter writer = contentService.getWriter(HOLD_REPORT, ContentModel.PROP_CONTENT, true);
            writer.putContent(is);

        }
    }

}
