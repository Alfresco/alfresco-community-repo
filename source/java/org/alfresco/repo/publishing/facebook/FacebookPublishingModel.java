
package org.alfresco.repo.publishing.facebook;

import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 * @since 4.0
 */
public interface FacebookPublishingModel
{
    public static final String NAMESPACE = "http://www.alfresco.org/model/publishing/facebook/1.0";
    public static final String PREFIX = "facebook";
    
    public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannel");

    public static final QName ASPECT_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannelAspect");
}
