package org.alfresco.service.cmr.repository;

import static org.junit.Assert.*;

import org.alfresco.error.AlfrescoRuntimeException;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link TemporalSourceOptions}.
 * 
 * @author Ray Gauss II
 */
public class TemporalSourceOptionsTest
{
    private TemporalSourceOptions temporalSourceOptions;
    
    private String[] expectedFailures = new String[] {
            "not even close",
            "2 hours",
            "01:00", // Incomplete: hours, minutes, and seconds required
            "0.01", // Incomplete: hours, minutes, and seconds required
            "00.00.01", // Delimiter is incorrect
            "1:30:15", // Hours, minutes, and seconds must have leading zeros
            "00:99:99" // Minutes and seconds can not be greater than 60
    };
    
    private String[] expectedSuccesses = new String[] {
            "00:00:00.05",
            "00:01:30",
            "01:01:01",
            "01:01:01.1",
            "99:59:59.999",
            "99:00:00"
    };
    
    @Before
    public void setUp() throws Exception
    {
        temporalSourceOptions = new TemporalSourceOptions();
    }

    protected void setDurationWithExpectedFailure(String value)
    {
        try
        {
            temporalSourceOptions.setDuration(value);
            fail("'" + value + "' should be invalid");
        }
        catch (AlfrescoRuntimeException e)
        {
            // expected
        }
    }
    
    protected void setDurationWithExpectedSuccess(String value)
    {
        try
        {
            temporalSourceOptions.setDuration(value);
        }
        catch (AlfrescoRuntimeException e)
        {
            fail(e.getMessage());
        }
    }
    
    protected void setOffsetWithExpectedFailure(String value)
    {
        try
        {
            temporalSourceOptions.setOffset(value);
            fail("'" + value + "' should be invalid");
        }
        catch (AlfrescoRuntimeException e)
        {
            // expected
        }
    }
    
    protected void setOffsetWithExpectedSuccess(String value)
    {
        try
        {
            temporalSourceOptions.setOffset(value);
        }
        catch (AlfrescoRuntimeException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testDurationValidation() throws Exception
    {
        for (String expectedFailure : expectedFailures)
        {
            setDurationWithExpectedFailure(expectedFailure);
        }
        for (String expectedSuccess : expectedSuccesses)
        {
            setDurationWithExpectedSuccess(expectedSuccess);
        }
    }
    
    @Test
    public void testOffsetValidation() throws Exception
    {
        for (String expectedFailure : expectedFailures)
        {
            setOffsetWithExpectedFailure(expectedFailure);
        }
        for (String expectedSuccess : expectedSuccesses)
        {
            setOffsetWithExpectedSuccess(expectedSuccess);
        }
    }

}

