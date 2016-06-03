package org.alfresco.repo.web.scripts.calendar;

import org.alfresco.repo.calendar.CalendarHelpersTest;
import org.alfresco.repo.calendar.CalendarServiceImplTest;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the {@link CalendarService}.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Nick Burch
 * @since 4.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CalendarServiceImplTest.class,
    CalendarHelpersTest.class,
    CalendarRestApiTest.class
})
public class AllCalendarTests
{
    // Intentionally empty
}
