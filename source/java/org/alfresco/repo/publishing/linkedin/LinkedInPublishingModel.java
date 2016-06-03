
package org.alfresco.repo.publishing.linkedin;

import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 4.0
 */
public interface LinkedInPublishingModel
{
    public static final String NAMESPACE = "http://www.alfresco.org/model/publishing/linkedin/1.0";
    public static final String PREFIX = "linkedin";
    
    public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannel");

    public static final QName ASPECT_ASSET = QName.createQName(NAMESPACE, "AssetAspect");
}
