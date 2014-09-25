/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events.jmx;

import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * MBean exposing Alfresco Events
 * 
 * @since 5.0
 * @author sglover
 *
 */
@ManagedResource
public class EventsManager
{
	public EventsManager()
	{
	}
}