
package org.alfresco.service.cmr.publishing;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public interface PublishingEvent extends Comparable<PublishingEvent>
{
    /**
     * @return a unique {@link String} identifier for this {@link PublishingEvent}
     */
    String getId();
    
    /**
     * @return the current {@link Status} of this {@link PublishingEvent}.
     */
    Status getStatus();
    
    /**
     * @return the date and time when this {@link PublishingEvent} is scheduled to publish its content.
     */
    Calendar getScheduledTime();
    
    /**
     * @return the {@link PublishingPackage} containing all the {@link NodeRef}s to be published and unpublished.
     */
    PublishingPackage getPackage();
    
    /**
     * @return the date and time when this {@link PublishingEvent} was created.
     */
    Date getCreatedTime();
    
    /**
     * @return the name of the user who created this {@link PublishingEvent}.
     */
    String getCreator();
    
    /**
     * @return the date and time when this {@link PublishingEvent} was last modified.
     */
    Date getModifiedTime();
    
    /**
     * @return the name of the user who last modified this {@link PublishingEvent}.
     */
    String getModifier();
    
    /**
     * @return the comment associatied with this {@link PublishingEvent}.
     */
    String getComment();
    
    /**
     * @return a unique identifier indicating which {@link Channel} this {@link PublishingEvent} publishes to.
     */
    String getChannelId();
    
    /**
     * @return the {@link StatusUpdate} information, if any.
     */
    StatusUpdate getStatusUpdate();
}
