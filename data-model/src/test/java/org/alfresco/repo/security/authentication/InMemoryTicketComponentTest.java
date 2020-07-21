package org.alfresco.repo.security.authentication;

import org.alfresco.service.cmr.repository.datatype.Duration;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * This test class is not exhaustive.
 * Please add here tests that are relevant for the
 * org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl and
 * org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl.Ticket classes
 */
public class InMemoryTicketComponentTest
{
    @Test
    public void testTicketCollectionReadinessDoNotExpire()
    {
        final Duration validDuration = new Duration("PT1H");
        final InMemoryTicketComponentImpl.ExpiryMode expireMode = InMemoryTicketComponentImpl.ExpiryMode.DO_NOT_EXPIRE;
        final String randomUserName = "someUserName";

        final Date someDate = null;

        checkEqualsAndHashCode(validDuration, expireMode, someDate, randomUserName);
    }

    @Test
    public void testTicketCollectionReadinessAfterInactivity()
    {
        final Duration validDuration = new Duration("PT1H");
        final InMemoryTicketComponentImpl.ExpiryMode expireMode = InMemoryTicketComponentImpl.ExpiryMode.AFTER_INACTIVITY;
        final String randomUserName = "someUserName";

        final Date someDate = new Date();

        checkEqualsAndHashCode(validDuration, expireMode, someDate, randomUserName);

        checkInvalidExpireDateParameter(validDuration, expireMode, randomUserName);
    }

    @Test
    public void testTicketCollectionReadinessAfterFixTime()
    {
        final Duration validDuration = new Duration("PT1H");
        final InMemoryTicketComponentImpl.ExpiryMode expireMode = InMemoryTicketComponentImpl.ExpiryMode.AFTER_FIXED_TIME;
        final String randomUserName = "someUserName";

        final Date someDate = new Date();

        checkEqualsAndHashCode(validDuration, expireMode, someDate, randomUserName);

        checkInvalidExpireDateParameter(validDuration, expireMode, randomUserName);
    }

    private void checkEqualsAndHashCode(Duration validDuration, InMemoryTicketComponentImpl.ExpiryMode expireMode, Date someDate,
        String randomUserName)
    {
        InMemoryTicketComponentImpl.Ticket ticket1 = new InMemoryTicketComponentImpl.Ticket(expireMode, someDate, randomUserName, validDuration);
        InMemoryTicketComponentImpl.Ticket ticket2 = new InMemoryTicketComponentImpl.Ticket(expireMode, someDate, randomUserName, validDuration);
        ticket1.equals(ticket2);

        final Set<InMemoryTicketComponentImpl.Ticket> aSet = new HashSet<>();
        for (int i = 0; i < 100000; ++i)
        {
            InMemoryTicketComponentImpl.Ticket ticket = new InMemoryTicketComponentImpl.Ticket(expireMode, someDate, randomUserName, validDuration);
            aSet.add(ticket);
        }
    }

    private void checkInvalidExpireDateParameter(Duration validDuration, InMemoryTicketComponentImpl.ExpiryMode expireMode, String randomUserName)
    {
        try
        {
            checkEqualsAndHashCode(validDuration, expireMode, null, randomUserName);
            fail("expire date should not be allowed as null in this  expireMode case");
        }
        catch (IllegalArgumentException e)
        {
            //we expect this, we sent the date to be null
        }
    }
}
