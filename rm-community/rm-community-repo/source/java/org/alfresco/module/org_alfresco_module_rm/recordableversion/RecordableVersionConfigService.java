 
package org.alfresco.module.org_alfresco_module_rm.recordableversion;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.script.slingshot.Version;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Recordable version config service interface
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public interface RecordableVersionConfigService
{
    /**
     * Gets the recordable versions
     *
     * @param nodeRef The node reference for which the recordable versions should be retrieved
     * @return The list of recordable versions
     */
    List<Version> getVersions(NodeRef nodeRef);

    /**
     * Sets the recordable version for the given node
     *
     * @param nodeRef The node reference for which the recorable version should be set
     * @param version The version to be set
     */
    void setVersion(NodeRef nodeRef, String version);
}
