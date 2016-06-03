package org.alfresco.repo.publishing;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface ChannelTypePublishingOperations
{
    void publish(NodeRef nodeToPublish, Map<QName, Serializable> channelProperties);
    void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> channelProperties);
}
