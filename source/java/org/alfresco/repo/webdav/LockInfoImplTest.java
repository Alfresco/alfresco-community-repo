/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;


public class LockInfoImplTest
{
    @Test
    public void canSetTimeoutSeconds()
    {
        LockInfoImplEx lockInfo = new LockInfoImplEx();
        
        // This should add 7 seconds (7000 millis) to the expiry date.
        lockInfo.setTimeoutSeconds(7);
        
        // Check the new date.
        assertEquals(86407000, lockInfo.getExpires().getTime());
    }
    
    @Test
    public void canSetTimeoutSecondsToInfinity()
    {
        LockInfoImplEx lockInfo = new LockInfoImplEx();
        
        lockInfo.setTimeoutSeconds(WebDAV.TIMEOUT_INFINITY);
        
        // Check the new date.
        assertNull(lockInfo.getExpires());
    }
    
    @Test
    public void canSetTimeoutMinutes()
    {
        LockInfoImplEx lockInfo = new LockInfoImplEx();
        
        // This should add 5 minutes to the expiry date.
        lockInfo.setTimeoutMinutes(5);
        
        // Check the new date.
        assertEquals(86700000, lockInfo.getExpires().getTime());
    }
    
    @Test
    public void canSetTimeoutMinutesToInfinity()
    {
        LockInfoImplEx lockInfo = new LockInfoImplEx();
        
        lockInfo.setTimeoutMinutes(WebDAV.TIMEOUT_INFINITY);
        
        // Check the new date.
        assertNull(lockInfo.getExpires());
    }
    
    @Test
    public void canGetRemainingTimeoutSeconds()
    {
        LockInfoImplEx lockInfo = new LockInfoImplEx();
        
        lockInfo.setTimeoutSeconds(7);
        
        assertEquals(7, lockInfo.getRemainingTimeoutSeconds());
    }
    
    public static class LockInfoImplEx extends LockInfoImpl
    {
        public static final Date DATE_NOW = new Date(86400000);
        private static final long serialVersionUID = 1669378516554195322L;

        @Override
        protected Date dateNow()
        {
            return DATE_NOW;
        }
    }
}
