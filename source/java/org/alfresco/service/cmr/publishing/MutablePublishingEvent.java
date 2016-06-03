
package org.alfresco.service.cmr.publishing;

import java.util.Calendar;

/**
 * An extension of the {@link PublishingEvent} interface that allows some changes to be made. 
 * 
 * @author Brian
 * @since 4.0
 */
public interface MutablePublishingEvent extends PublishingEvent
{
    void setScheduledTime(Calendar time);
    void setComment(String comment);
}
