package org.alfresco.repo.action.parameter;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Record metadata bootstrap bean.
 * <p>
 * This method of bootstrapping record metadata aspects into the RecordService deprecates the
 * previous practice of extending rma:recordMetaData.
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class NodeParameterSuggesterBootstrap
{
    /** namespace service */
    private NamespaceService namespaceService;

    /** configured node parameter processor aspect and type names, comma separated */
    private String nodeParameterProcessorAspectsNames;

    /** node parameter processor */
    private NodeParameterProcessor nodeParameterProcessor;

    /**
     * @param recordMetadataAspects map of record metadata aspects against file plan types
     */
    public void setNodeParameterProcessorAspects(String nodeParameterProcessorAspectsNames)
    {
        this.nodeParameterProcessorAspectsNames = nodeParameterProcessorAspectsNames;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param nodeParameterProcessor  Node parameter processor
     */
    public void setNodeParameterProcessor(NodeParameterProcessor nodeParameterProcessor)
    {
        this.nodeParameterProcessor = nodeParameterProcessor;
    }

    /**
     * Init method
     */
    public void init()
    {
        ParameterCheck.mandatory("namespaceService", namespaceService);

        if (nodeParameterProcessorAspectsNames != null)
        {
            String[] aspectsNames = this.nodeParameterProcessorAspectsNames.split(",");
            for (String name : aspectsNames)
            {
                if((name != null) && !"".equals(name.trim()))
                {
                    // convert to qname and save it
                    QName aspect = QName.createQName(name.trim(), namespaceService);

                    // register with node parameter processor
                    this.nodeParameterProcessor.addSuggestionDefinition(aspect);
                }
            }
        }
    }
}
