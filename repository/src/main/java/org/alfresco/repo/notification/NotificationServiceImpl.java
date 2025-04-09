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
package org.alfresco.repo.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationProvider;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.util.ParameterCheck;

/**
 * Notification service implementation.
 * 
 * @author Roy Wetherall
 * @since 4.0
 */
public class NotificationServiceImpl implements NotificationService
{
    /** I18N */
    private static final String MSG_NP_DOES_NOT_EXIST = "np-does-not-exist";

    /** Log */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(EMailNotificationProvider.class);

    /** Map of registered notification providers */
    private Map<String, NotificationProvider> providers = new HashMap<String, NotificationProvider>(3);

    /**
     * @see org.alfresco.service.cmr.notification.NotificationService#register(org.alfresco.service.cmr.notification.NotificationProvider)
     */
    @Override
    public void register(NotificationProvider notificationProvider)
    {
        // Check mandatory params
        ParameterCheck.mandatory("notificationProvider", notificationProvider);

        // Add the notification provider to the map
        providers.put(notificationProvider.getName(), notificationProvider);
    }

    /**
     * @see org.alfresco.service.cmr.notification.NotificationService#exists(java.lang.String)
     */
    @Override
    public boolean exists(String notificationProvider)
    {
        // Check the mandatory params
        ParameterCheck.mandatory("notificationProvider", notificationProvider);

        return providers.containsKey(notificationProvider);
    }

    /**
     * @see org.alfresco.service.cmr.notification.NotificationService#getNotificationProviders()
     */
    @Override
    public List<String> getNotificationProviders()
    {
        return new ArrayList<String>(providers.keySet());
    }

    /**
     * @see org.alfresco.service.cmr.notification.NotificationService#sendNotification(java.lang.String, org.alfresco.service.cmr.notification.NotificationContext)
     */
    @Override
    public void sendNotification(String notificationProvider, NotificationContext notificationContext)
    {
        // Check the mandatory params
        ParameterCheck.mandatory("notificationProvider", notificationProvider);
        ParameterCheck.mandatory("notificationContext", notificationContext);

        // Check that the notificaiton provider exists
        if (exists(notificationProvider) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NP_DOES_NOT_EXIST, notificationProvider));
        }

        // Get the notification provider and send notification
        NotificationProvider provider = providers.get(notificationProvider);
        provider.sendNotification(notificationContext);
    }
}
