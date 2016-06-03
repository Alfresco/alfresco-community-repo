
package org.alfresco.repo.publishing;

import java.util.Calendar;

import org.alfresco.service.cmr.publishing.MutablePublishingEvent;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class MutablePublishingEventImpl extends PublishingEventImpl implements MutablePublishingEvent
{
    /**
     * @param event PublishingEventImpl
     */
    public MutablePublishingEventImpl(PublishingEventImpl event)
    {
        super(event);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void setScheduledTime(Calendar time)
    {
        this.scheduledTime.setTimeInMillis(time.getTimeInMillis());
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
