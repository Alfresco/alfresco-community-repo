package org.alfresco.repo.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationProvider;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

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
     *  @see org.alfresco.service.cmr.notification.NotificationService#register(org.alfresco.service.cmr.notification.NotificationProvider)
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
     *  @see org.alfresco.service.cmr.notification.NotificationService#getNotificationProviders()
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
