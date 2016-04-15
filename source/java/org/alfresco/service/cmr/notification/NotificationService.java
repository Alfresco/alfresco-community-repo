
package org.alfresco.service.cmr.notification;

import java.util.List;

import org.alfresco.service.NotAuditable;

/**
 * Notification Service Interface.
 * 
 * @author Roy Wetherall
 * @since 4.0
 */
public interface NotificationService 
{
    /**
     * Registers a notification provider with the notification service.
     * 
     * @param notificationProvider  notification provider
     */
    @NotAuditable
    void register(NotificationProvider notificationProvider);

    /**
     * Gets a list of all the currently available notification providers.
     * 
     * @return {@link List}<{@link String}> notification provider names
     */
    @NotAuditable
    List<String> getNotificationProviders();

    /**
     * Indicates whether a notification provider exists or not.
     * 
     * @param notificationProvider  notification provider
     * @return boolean              true if exists, false otherwise
     */
    @NotAuditable
    boolean exists(String notificationProvider);

    /**
     * Send notification using the names notification provider and notification context.
     * 
     * @param notificationProvider  notification provider
     * @param notificationContext   notification context
     */
    @NotAuditable
    void sendNotification(String notificationProvider, NotificationContext notificationContext);	
}
