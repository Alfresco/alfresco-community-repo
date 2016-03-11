package org.alfresco.module.org_alfresco_module_rm.patch.v21;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Report service patch, adding report structure in data dictionary and report templates.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@SuppressWarnings("deprecation")
public class RMv21ReportServicePatch extends RMv21PatchComponent
                                     implements BeanNameAware
{
    private static final NodeRef TEMPLATE_ROOT = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_report_templates");
    private static final NodeRef RM_CONFIG_FOLDER = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_config_folder");

    private static final String PATH_DESTRUCTION_TEMPLATE = "alfresco/module/org_alfresco_module_rm/bootstrap/report/report_rmr_destructionReport.html.ftl";

    /** node service */
    private NodeService nodeService;

    /** content service */
    private ContentService contentService;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    @Override
    protected void executePatch()
    {
        // check whether report dir exists or not
        if (nodeService.exists(RM_CONFIG_FOLDER) && !nodeService.exists(TEMPLATE_ROOT))
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(" ... adding template root folder");
            }            

            NodeRef reportTemplates = createNode(
                    RM_CONFIG_FOLDER,
                    ContentModel.TYPE_FOLDER,
                    "rm_report_templates",
                    "Records Management Report Templates",
                    "rm_report_templates",
                    "Records Management Report Templates",
                    "Records Management Report Templates");
            nodeService.addAspect(reportTemplates, ContentModel.ASPECT_TITLED, null);
            nodeService.addAspect(reportTemplates, ContentModel.ASPECT_AUTHOR, null); 
            
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(" ... adding destruction report template");
            }

            // create report templates
            NodeRef destructionTemplate = createNode(
                    TEMPLATE_ROOT,
                    ContentModel.TYPE_CONTENT,
                    "rmr_destructionReport",
                    "report_rmr_destructionReport.html.ftl",
                    "report_rmr_destructionReport.html.ftl",
                    "Destruction Report Template",
                    "Desruction report template.");
            nodeService.addAspect(destructionTemplate, ContentModel.ASPECT_TITLED, null);
            nodeService.addAspect(destructionTemplate, ContentModel.ASPECT_AUTHOR, null);

            // put the content
            ContentWriter writer = contentService.getWriter(destructionTemplate, ContentModel.PROP_CONTENT, true);
            InputStream is = getClass().getClassLoader().getResourceAsStream(PATH_DESTRUCTION_TEMPLATE);
            writer.setEncoding("UTF-8");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.putContent(is);
        }
    }

    private NodeRef createNode(NodeRef parent, QName type, String id, String name, String assocName,  String title, String description)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(4);
        props.put(ContentModel.PROP_DESCRIPTION, description);
        props.put(ContentModel.PROP_TITLE, title);
        props.put(ContentModel.PROP_NAME, name);
        props.put(ContentModel.PROP_NODE_UUID, id);

        // get the assoc qname
        QName assocQName = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName(assocName));

        // create the node
       return nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                assocQName,
                type,
                props).getChildRef();
    }
}
