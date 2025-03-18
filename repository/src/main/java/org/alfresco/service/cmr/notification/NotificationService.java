/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
     * @param notificationProvider
     *            notification provider
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
     * @param notificationProvider
     *            notification provider
     * @return boolean true if exists, false otherwise
     */
    @NotAuditable
    boolean exists(String notificationProvider);

    /**
     * Send notification using the names notification provider and notification context.
     * 
     * @param notificationProvider
     *            notification provider
     * @param notificationContext
     *            notification context
     */
    @NotAuditable
    void sendNotification(String notificationProvider, NotificationContext notificationContext);
}
