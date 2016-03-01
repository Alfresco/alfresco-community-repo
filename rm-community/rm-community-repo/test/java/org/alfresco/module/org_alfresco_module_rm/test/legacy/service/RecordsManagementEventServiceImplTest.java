 
package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Event service implementation unit test
 *
 * @author Roy Wetherall
 */
public class RecordsManagementEventServiceImplTest extends BaseRMTestCase implements RecordsManagementModel
{
    protected static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    private RetryingTransactionHelper transactionHelper;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Get the service required in the tests
        this.transactionHelper = (RetryingTransactionHelper)this.applicationContext.getBean("retryingTransactionHelper");

        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }

    public void testGetEventTypes()
    {
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
            if (eventName.equals(event.getName()))
            {
                result = true;
                break;
            }
        }
        return result;
    }
}
