/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import org.alfresco.events.types.ExceptionGeneratedEvent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.messaging.MessageProducer;
import org.gytheio.messaging.MessagingException;

public class ExceptionEventsServiceImpl extends AbstractEventsService implements ExceptionEventsService
{
    private static Log logger = LogFactory.getLog(ExceptionEventsServiceImpl.class);

    private MessageProducer messageProducer;
	private boolean enabled;
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void setMessageProducer(MessageProducer messageProducer)
	{
		this.messageProducer = messageProducer;
	}

	public void init()
	{
	}

	@Override
	public void exceptionGenerated(String txnId, Throwable t)
	{
		if(enabled)
		{
			try
			{
				long timestamp = System.currentTimeMillis();
				String networkId = TenantUtil.getCurrentDomain();
				String username = AuthenticationUtil.getFullyAuthenticatedUser();
				ExceptionGeneratedEvent event = new ExceptionGeneratedEvent(nextSequenceNumber(), txnId, timestamp,
						networkId, t, username);
				messageProducer.send(event);
			}
			catch(MessagingException e)
			{
				logger.error(e);
			}
		}
	}
}
