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
 * Adds a new transfer/accession report template to the existing report templates
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class RMv22ReportTemplatePatch extends AbstractModulePatch
{
    /** Report template path */
    private static final String REPORT_TEMPLATE_PATH = "alfresco/module/org_alfresco_module_rm/bootstrap/report/report_rmr_transferReport.html.ftl";

    /** Report template config node IDs */
    private static final String TRANSFER_REPORT = "rmr_transferReport";
    private static final String DESTRUCTION_REPORT = "rmr_destructionReport";

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
        NodeRef transferReport = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TRANSFER_REPORT);
        NodeRef destructionReport = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DESTRUCTION_REPORT);
        if (!nodeService.exists(transferReport) && nodeService.exists(destructionReport))
        {
            NodeRef parent = nodeService.getPrimaryParent(destructionReport).getParentRef();

            // get the assoc qname
            QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName("report_rmr_transferReport.html.ftl"));

            // build the node properties
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(4);
            props.put(ContentModel.PROP_DESCRIPTION, "Transfer report template.");
            props.put(ContentModel.PROP_TITLE, "Transfer Report Template");
            props.put(ContentModel.PROP_NAME, "report_rmr_transferReport.html.ftl");
            props.put(ContentModel.PROP_NODE_UUID, "rmr_transferReport");

            // create the node
            ChildAssociationRef node = nodeService.createNode(parent,
                    ContentModel.ASSOC_CONTAINS,
                    assocQName,
                    ContentModel.TYPE_CONTENT,
                    props);

            // put the content
            ContentWriter writer = contentService.getWriter(node.getChildRef(), ContentModel.PROP_CONTENT, true);
            InputStream is = getClass().getClassLoader().getResourceAsStream(REPORT_TEMPLATE_PATH);
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent(is);
        }
    }
}
