
package org.alfresco.repo.publishing;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.publishing.MutablePublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.publishing.StatusUpdate;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingEventImpl implements PublishingEvent
{
    private final String id;
    private final Status status;
    private final String channelId;
    private final PublishingPackage publishingPackage;
    private final Date createdTime;
    private final String creator;
    private final Date modifiedTime;
    private final String modifier;
    private final StatusUpdate statusUpdate;
    protected final Calendar scheduledTime;
    protected String comment;
    
    public PublishingEventImpl(String id,
            Status status, String channelName,
            PublishingPackage publishingPackage,Date createdTime,
            String creator, Date modifiedTime,
            String modifier, Calendar scheduledTime, String comment,
            StatusUpdate statusUpdate)
    {
        this.id = id;
        this.status = status;
        this.channelId = channelName;
        this.publishingPackage = publishingPackage;
        this.createdTime = createdTime;
        this.creator = creator;
        this.modifiedTime = modifiedTime;
        this.modifier = modifier;
        this.scheduledTime = scheduledTime;
        this.comment = comment;
        this.statusUpdate = statusUpdate;
    }
    
    public PublishingEventImpl(PublishingEvent event)
    {
        this(event.getId(),
                event.getStatus(), event.getChannelId(),
                event.getPackage(), event.getCreatedTime(),
                event.getCreator(), event.getModifiedTime(),
                event.getModifier(), event.getScheduledTime(), event.getComment(),
                event.getStatusUpdate());
    }

    /**
    * {@inheritDoc}
    */
    public String getId()
    {
        return id;
    }

    /**
    * {@inheritDoc}
    */
    public Status getStatus()
    {
        return status;
    }

    /**
    * {@inheritDoc}
    */
    public String getChannelId()
    {
        return channelId;
    }
    
    /**
    * {@inheritDoc}
    */
    public Calendar getScheduledTime()
    {
        return (Calendar) scheduledTime.clone();
    }

    /**
    * {@inheritDoc}
    */
    public PublishingPackage getPackage()
    {
        return publishingPackage;
    }

    /**
    * {@inheritDoc}
    */
    public Date getCreatedTime()
    {
        return new Date(createdTime.getTime());
    }

    /**
    * {@inheritDoc}
    */
    public String getCreator()
    {
        return creator;
    }

    /**
    * {@inheritDoc}
    */
    public Date getModifiedTime()
    {
        return new Date(modifiedTime.getTime());
    }

    /**
    * {@inheritDoc}
    */
    public String getModifier()
    {
        return modifier;
    }

    /**
    * {@inheritDoc}
    */
    public String getComment()
    {
        return comment;
    }

    /**
    * {@inheritDoc}
    */
    public StatusUpdate getStatusUpdate()
    {
        return statusUpdate;
    }

    public MutablePublishingEvent edit()
    {
        return new MutablePublishingEventImpl(this);
    }

    /**
    * {@inheritDoc}
    */
    public int compareTo(PublishingEvent event)
    {
        if (event == null)
        {
            return 1;
        }
        Date eventTime = event.getCreatedTime();
        if (eventTime == null)
        {
            return 1;
        }
        return (int)(createdTime.getTime() - eventTime.getTime());
    }

}
