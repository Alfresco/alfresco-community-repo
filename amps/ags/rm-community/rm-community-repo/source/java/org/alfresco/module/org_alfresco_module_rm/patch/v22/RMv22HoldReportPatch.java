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

package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Adds the hold report.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RMv22HoldReportPatch extends AbstractModulePatch
{
    /** Report template path */
    private static final String REPORT_TEMPLATE_PATH = "alfresco/module/org_alfresco_module_rm/bootstrap/report/report_rmr_holdReport.html.ftl";

    /** Report template config node IDs */
    private static final NodeRef REPORT_FOLDER = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_report_templates");
    private static final NodeRef REPORT = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rmr_holdReport");

    /** Node service */
    private NodeService nodeService;

    /** Content service */
    private ContentService contentService;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        if (!nodeService.exists(REPORT))
        {
            // get the assoc qname
            QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName("report_rmr_holdReport.html.ftl"));

            // build the node properties
            Map<QName, Serializable> props = new HashMap<>(4);
            props.put(ContentModel.PROP_DESCRIPTION, "Hold report template.");
            props.put(ContentModel.PROP_TITLE, "Hold Report Template");
            props.put(ContentModel.PROP_NAME, "report_rmr_holdReport.html.ftl");
            props.put(ContentModel.PROP_NODE_UUID, "rmr_holdReport");

            // create the node
            ChildAssociationRef node = nodeService.createNode(
                    REPORT_FOLDER,
                    ContentModel.ASSOC_CONTAINS,
                    assocQName,
                    ContentModel.TYPE_CONTENT,
                    props);

            // put the content
            ContentWriter writer = contentService.getWriter(node.getChildRef(), ContentModel.PROP_CONTENT, true);
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            InputStream is = getClass().getClassLoader().getResourceAsStream(REPORT_TEMPLATE_PATH);
            writer.putContent(is);
        }
    }
}
