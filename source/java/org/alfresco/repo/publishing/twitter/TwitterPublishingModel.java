
package org.alfresco.repo.publishing.twitter;

import org.alfresco.service.namespace.QName;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface TwitterPublishingModel
{
    public static final String NAMESPACE = "http://www.alfresco.org/model/publishing/twitter/1.0";
    public static final String PREFIX = "twitter";
    
    public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannel");

    public static final QName ASPECT_ASSET = QName.createQName(NAMESPACE, "AssetAspect");
}
