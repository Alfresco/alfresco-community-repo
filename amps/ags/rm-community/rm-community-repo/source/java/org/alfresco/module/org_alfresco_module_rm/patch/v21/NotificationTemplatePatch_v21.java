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

package org.alfresco.module.org_alfresco_module_rm.patch.v21;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Adds a new email template for rejected records to the existing templates
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class NotificationTemplatePatch_v21 extends RMv21PatchComponent
{
    /** Email template path */
    private static final String PATH_REJECTED = "alfresco/module/org_alfresco_module_rm/bootstrap/content/record-rejected-email.ftl";

    /** Reject template config node id*/
    private static final String CONFIG_NODEID = "record_rejected_template";

    /** Records management notification helper */
    private RecordsManagementNotificationHelper notificationHelper;

    /** Node service */
    private NodeService nodeService;

    /** Content service */
    private ContentService contentService;

    /**
     * @param notificationHelper    notification helper
     */
    public void setNotificationHelper(RecordsManagementNotificationHelper notificationHelper)
    {
        this.notificationHelper = notificationHelper;
    }

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

    @Override
    protected void executePatch()
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CONFIG_NODEID);
        // get the parent node
        NodeRef supersededTemplate = notificationHelper.getSupersededTemplate();
        if (!nodeService.exists(nodeRef) && nodeService.exists(supersededTemplate))
        {
            NodeRef parent = nodeService.getPrimaryParent(supersededTemplate).getParentRef();

            // build the node properties
            Map<QName, Serializable> props = new HashMap<>(4);
            props.put(ContentModel.PROP_DESCRIPTION, "Record superseded email template.");
            props.put(ContentModel.PROP_TITLE, "record-rejected-email.ftl");
            props.put(ContentModel.PROP_NAME, "record-rejected-email.ftl");
            props.put(ContentModel.PROP_NODE_UUID, "record_rejected_template");

            // get the assoc qname
            QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName("record-rejected-email.ftl"));

            // create the node
            ChildAssociationRef node = nodeService.createNode(parent,
                    ContentModel.ASSOC_CONTAINS,
                    assocQName,
                    ContentModel.TYPE_CONTENT,
                    props);

            // put the content
            ContentWriter writer = contentService.getWriter(node.getChildRef(), ContentModel.PROP_CONTENT, true);
            InputStream is = getClass().getClassLoader().getResourceAsStream(PATH_REJECTED);
            writer.putContent(is);
        }
    }
}
