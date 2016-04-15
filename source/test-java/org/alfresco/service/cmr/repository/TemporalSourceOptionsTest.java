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

