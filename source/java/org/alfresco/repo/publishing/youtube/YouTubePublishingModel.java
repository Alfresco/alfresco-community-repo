
package org.alfresco.repo.publishing.youtube;

import org.alfresco.service.namespace.QName;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface YouTubePublishingModel
{
    public static final String NAMESPACE = "http://www.alfresco.org/model/publishing/youtube/1.0";
    public static final String PREFIX = "youtube";
    
    public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannel");

    public static final QName ASPECT_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannelAspect");
    
    public static final QName ASPECT_ASSET = QName.createQName(NAMESPACE, "AssetAspect");
}
