/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.BaseSpringTest;

/**
 * Event service implementation unit test
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementEventServiceImplTest extends BaseSpringTest implements RecordsManagementModel
{    
    protected static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

	private RecordsManagementEventService rmEventService;
	private RetryingTransactionHelper transactionHelper;
	
	@Override
	protected void onSetUpInTransaction() throws Exception 
	{
		super.onSetUpInTransaction();

		// Get the service required in the tests
		this.rmEventService = (RecordsManagementEventService)this.applicationContext.getBean("RecordsManagementEventService");
        this.transactionHelper = (RetryingTransactionHelper)this.applicationContext.getBean("retryingTransactionHelper");

		// Set the current security context as admin
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
	}
	
	public void testGetEventTypes()
	{
        setComplete();
        endTransaction();

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        List<RecordsManagementEventType> eventTypes = rmEventService.getEventTypes();
                        assertNotNull(eventTypes);
                        for (RecordsManagementEventType eventType : eventTypes)
                        {
                            System.out.println(eventType.getName() + " - " + eventType.getDisplayLabel());
                        }
                        return null;
                    }          
                });        
	}
	
	public void testGetEvents()
	{
        setComplete();
        endTransaction();

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        List<RecordsManagementEvent> events = rmEventService.getEvents();
                        assertNotNull(events);
                        for (RecordsManagementEvent event : events)
                        {
                            System.out.println(event.getName());
                        }
                        return null;
                    }          
                });        
	}
	
	public void testAddRemoveEvents()
	{
        setComplete();
        endTransaction();

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        List<RecordsManagementEvent> events = rmEventService.getEvents();
                        assertNotNull(events);
                        assertFalse(containsEvent(events, "myEvent"));
                        
                        rmEventService.addEvent("rmEventType.simple", "myEvent", "My Event");
                        
                        events = rmEventService.getEvents();
                        assertNotNull(events);
                        assertTrue(containsEvent(events, "myEvent"));
                        
                        rmEventService.removeEvent("myEvent");
                        
                        events = rmEventService.getEvents();
                        assertNotNull(events);
                        assertFalse(containsEvent(events, "myEvent"));               
                        return null;
                    }          
                });        
	}
	
	private boolean containsEvent(List<RecordsManagementEvent> events, String eventName)
	{
	    boolean result = false;
	    for (RecordsManagementEvent event : events)
        {
            if (eventName.equals(event.getName()) == true)
            {
                result = true;
                break;
            }
        }
	    return result;
	}
}
