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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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
    
    @Test(expected=IllegalStateException.class)
    public void cannotChangeSharedLockToExclusive()
    {
        LockInfoImpl lockInfo = new LockInfoImpl();
        lockInfo.addSharedLockToken("shared-token");
        
        // Not allowed
        lockInfo.setExclusiveLockToken("token");
    }
    
    @Test(expected=IllegalStateException.class)
    public void cannotChangeExclusiveLockToShared()
    {
        LockInfoImpl lockInfo = new LockInfoImpl();
        lockInfo.setExclusiveLockToken("token");

        // Not allowed
        lockInfo.addSharedLockToken("shared-token");
    }
    
    public void canSetShared()
    {
        LockInfoImpl lockInfo = new LockInfoImpl();
        lockInfo.setExclusiveLockToken("exc-token");
        
        assertEquals("exc-token", lockInfo.getExclusiveLockToken());
    }
    
    public void canSetExclusive()
    {
        LockInfoImpl lockInfo = new LockInfoImpl();
        lockInfo.addSharedLockToken("shared1");
        lockInfo.addSharedLockToken("shared2");
        
        assertEquals(2, lockInfo.getSharedLockTokens().size());
        assertTrue(lockInfo.getSharedLockTokens().contains("shared1"));
        assertTrue(lockInfo.getSharedLockTokens().contains("shared2"));
    }

    @Test
    public void canGenerateJSON() throws JsonParseException, JsonMappingException, IOException
    {
        // Exclusive lock
        LockInfoImpl lockInfo = new LockInfoImpl();
        lockInfo.setExclusiveLockToken("opaque-lock-token");
        lockInfo.setDepth(WebDAV.INFINITY);
        lockInfo.setScope(WebDAV.XML_EXCLUSIVE);
        
        String json = lockInfo.toJSON();
        ObjectMapper objectMapper = new ObjectMapper();
        LockInfoImpl parsed = objectMapper.readValue(json, LockInfoImpl.class);
        assertEquals("opaque-lock-token", parsed.getExclusiveLockToken());
        assertEquals(WebDAV.INFINITY, parsed.getDepth());
        assertEquals(WebDAV.XML_EXCLUSIVE, parsed.getScope());
        
        // Shared lock
        lockInfo = new LockInfoImpl();
        lockInfo.addSharedLockToken("opaque-lock-token-1");
        lockInfo.addSharedLockToken("opaque-lock-token-2");
        lockInfo.addSharedLockToken("opaque-lock-token-3");
        lockInfo.setDepth(WebDAV.ZERO);
        lockInfo.setScope(WebDAV.XML_SHARED);
        
        json = lockInfo.toJSON();
        parsed = objectMapper.readValue(json, LockInfoImpl.class);
        Set<String> sortedTokens = new TreeSet<String>(parsed.getSharedLockTokens());
        Iterator<String> tokenIt = sortedTokens.iterator();
        assertEquals("opaque-lock-token-1", tokenIt.next());
        assertEquals("opaque-lock-token-2", tokenIt.next());
        assertEquals("opaque-lock-token-3", tokenIt.next());        
        assertEquals(WebDAV.ZERO, parsed.getDepth());
        assertEquals(WebDAV.XML_SHARED, parsed.getScope());
    }
    
    @Test
    public void canParseJSON() throws JsonParseException, JsonMappingException, IOException
    {
        // Exclusive lock
        LockInfoImpl lockInfo = new LockInfoImpl();
        lockInfo.setExclusiveLockToken("opaque-lock-token");
        lockInfo.setDepth(WebDAV.INFINITY);
        lockInfo.setScope(WebDAV.XML_EXCLUSIVE);
        
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(lockInfo);
        // Execute the factory method we're testing
        LockInfo parsed = LockInfoImpl.fromJSON(json);
        assertEquals("opaque-lock-token", parsed.getExclusiveLockToken());
        assertEquals(WebDAV.INFINITY, parsed.getDepth());
        assertEquals(WebDAV.XML_EXCLUSIVE, parsed.getScope());
        
        // Shared lock
        lockInfo = new LockInfoImpl();
        lockInfo.addSharedLockToken("opaque-lock-token-1");
        lockInfo.addSharedLockToken("opaque-lock-token-2");
        lockInfo.addSharedLockToken("opaque-lock-token-3");
        lockInfo.setDepth(WebDAV.ZERO);
        lockInfo.setScope(WebDAV.XML_SHARED);
        
        json = objectMapper.writeValueAsString(lockInfo);
        // Execute the factory method we're testing
        parsed = LockInfoImpl.fromJSON(json);
        Set<String> sortedTokens = new TreeSet<String>(parsed.getSharedLockTokens());
        Iterator<String> tokenIt = sortedTokens.iterator();
        assertEquals("opaque-lock-token-1", tokenIt.next());
        assertEquals("opaque-lock-token-2", tokenIt.next());
        assertEquals("opaque-lock-token-3", tokenIt.next());        
        assertEquals(WebDAV.ZERO, parsed.getDepth());
        assertEquals(WebDAV.XML_SHARED, parsed.getScope());
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
