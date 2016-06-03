
package org.alfresco.repo.publishing.flickr;

import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 *
 */
public interface FlickrPublishingModel
{
    public static final String NAMESPACE = "http://www.alfresco.org/model/publishing/flickr/1.0";
    public static final String PREFIX = "flickr";
    
    public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannel");

    public static final QName ASPECT_ASSET = QName.createQName(NAMESPACE, "AssetAspect");
}
