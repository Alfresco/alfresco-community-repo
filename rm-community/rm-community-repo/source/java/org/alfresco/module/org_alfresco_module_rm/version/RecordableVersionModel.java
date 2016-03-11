package org.alfresco.module.org_alfresco_module_rm.version;

import org.alfresco.service.namespace.QName;

/**
 * Helper class containing recordable version model qualified names
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public interface RecordableVersionModel
{
    /** Namespace details */
    String RMV_URI = "http://www.alfresco.org/model/recordableversion/1.0";
    String RMV_PREFIX = "rmv";

    /** versionable aspect */
    QName ASPECT_VERSIONABLE = QName.createQName(RMV_URI, "versionable");
    QName PROP_RECORDABLE_VERSION_POLICY = QName.createQName(RMV_URI, "recordableVersionPolicy");
    QName PROP_FILE_PLAN = QName.createQName(RMV_URI, "filePlan");

    /** recorded version aspect */
    QName ASPECT_RECORDED_VERSION = QName.createQName(RMV_URI, "recordedVersion");
    QName PROP_RECORD_NODE_REF = QName.createQName(RMV_URI, "recordNodeRef");
    QName PROP_FROZEN_OWNER = QName.createQName(RMV_URI, "frozenOwner");
    QName PROP_DESTROYED = QName.createQName(RMV_URI, "destroyed");
    
    /** version record aspect */
    QName ASPECT_VERSION_RECORD = QName.createQName(RMV_URI, "versionRecord");
    QName PROP_VERSIONED_NODEREF = QName.createQName(RMV_URI, "versionedNodeRef");
    QName PROP_VERSION_LABEL = QName.createQName(RMV_URI, "versionLabel");
    QName PROP_VERSION_DESCRIPTION = QName.createQName(RMV_URI, "versionDescription");
}
