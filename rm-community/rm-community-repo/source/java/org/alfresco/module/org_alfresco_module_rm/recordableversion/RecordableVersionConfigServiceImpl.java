package org.alfresco.module.org_alfresco_module_rm.recordableversion;

import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.NONE;
import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.script.slingshot.Version;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Recordable version config service
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordableVersionConfigServiceImpl implements RecordableVersionConfigService, RecordableVersionModel
{
    /** Node service */
    private NodeService nodeService;

    /**
     * Gets the node service
     *
     * @return The node service
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * Sets the node service
     *
     * @param nodeService The node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordableversion.RecordableVersionConfigService#getVersions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<Version> getVersions(NodeRef nodeRef)
    {
        mandatory("nodeRef", nodeRef);

        RecordableVersionPolicy[] recordableVersionPolicies = RecordableVersionPolicy.values();
        List<Version> versions = new ArrayList<Version>(recordableVersionPolicies.length);

        for (RecordableVersionPolicy recordableVersionPolicy : recordableVersionPolicies)
        {
            String policy = recordableVersionPolicy.toString();
            boolean selected = isVersionPolicySelected(recordableVersionPolicy, nodeRef);
            versions.add(new Version(policy, selected));
        }

        return versions;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordableversion.RecordableVersionConfigService#setVersion(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public void setVersion(NodeRef nodeRef, String version)
    {
        mandatory("nodeRef", nodeRef);
        mandatoryString("recordedVersion", version);

        RecordableVersionPolicy recordableVersionPolicy = RecordableVersionPolicy.valueOf(version);
        getNodeService().setProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY, recordableVersionPolicy);
    }

    /**
     * Checks if the specified recordable version policy has been selected for the document
     *
     * @param recordableVersionPolicy The recordable version policy
     * @param nodeRef Node reference of the document
     * @return <code>true</code> if the specified recordable version policy has been selected for the document, <code>false</code> otherwise
     */
    private boolean isVersionPolicySelected(RecordableVersionPolicy recordableVersionPolicy, NodeRef nodeRef)
    {
        boolean isVersionPolicySelected = false;
        String policy = (String) getNodeService().getProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY);
        if (isNotBlank(policy))
        {
            if (RecordableVersionPolicy.valueOf(policy).equals(recordableVersionPolicy))
            {
                isVersionPolicySelected = true;
            }
        }
        else
        {
            if (recordableVersionPolicy.equals(NONE))
            {
                isVersionPolicySelected = true;
            }
        }
        return isVersionPolicySelected;
    }
}
