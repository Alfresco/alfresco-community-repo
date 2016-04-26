package org.alfresco.service.cmr.notification;

import org.alfresco.service.NotAuditable;

/**
 * Notification Provider interface.
 * 
 * @author Roy Wetherall
 * @since 4.0
 */
public interface NotificationProvider 
{
    /**
     * Gets the name of the notification provider.
     * 
     * @return  notification provider name
     */
    @NotAuditable
    String getName();

    /**
     * Sends a notification using the notification provider.
     * 
     * @param notificationContext   notification context
     */
    @NotAuditable
    void sendNotification(NotificationContext notificationContext);
}
