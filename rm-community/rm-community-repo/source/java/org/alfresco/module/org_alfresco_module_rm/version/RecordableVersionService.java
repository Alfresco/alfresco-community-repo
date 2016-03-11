package org.alfresco.module.org_alfresco_module_rm.version;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;

/**
 * Recordable version service interface.
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public interface RecordableVersionService 
{
    /**
     * Indicates whether the current version of a node is recorded or not.
     * <p>
     * Returns false if not versionable or no version.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if latest version recorded, false otherwise
     */
    boolean isCurrentVersionRecorded(NodeRef nodeRef);
    
    /**
     * Indicates whether a version is recorded or not.
     * 
     * @param version   version
     * @return boolean  true if recorded version, false otherwise
     */
    boolean isRecordedVersion(Version version);
    
    /**
     * If the version is a recorded version, gets the related version 
     * record.
     * 
     * @param  version   version
     * @return NodeRef   node reference of version record
     */
    NodeRef getVersionRecord(Version version);
    
    /**
     * Gets the version that relates to the version record
     * 
     * @param versionRecord version record node reference
     * @return Version  version or null if not found
     */
    Version getRecordedVersion(NodeRef record);
    
    /**
     * Creates a record from the latest version, marking it as recorded.
     * <p>
     * Does not create a record if the node is not versionable or the latest
     * version is already recorded.
     * 
     * @param nodeRef   node reference
     * @return NodeRef  node reference to the created record.
     */
    NodeRef createRecordFromLatestVersion(NodeRef filePlan, NodeRef nodeRef);
    
    /**
     * Indicates whether a record version is destroyed or not.
     * 
     * @param version   version
     * @return boolean  true if destroyed, false otherwise
     */
    boolean isRecordedVersionDestroyed(Version version);
    
    /**
     * Marks a recorded version as destroyed.
     * <p>
     * Note this method does not destroy the associated record, instead it marks the 
     * version as destroyed.
     * 
     * @param version   version
     */
    void destroyRecordedVersion(Version version);

}
