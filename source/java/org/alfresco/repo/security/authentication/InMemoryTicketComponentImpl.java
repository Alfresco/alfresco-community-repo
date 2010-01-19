/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authentication;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.util.GUID;
import org.apache.commons.codec.binary.Hex;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Store tickets in memory. They can be distributed in a cluster via the cache
 * 
 * @author andyh
 */
public class InMemoryTicketComponentImpl implements TicketComponent
{
    /**
     * Ticket prefix
     */
    public static final String GRANTED_AUTHORITY_TICKET_PREFIX = "TICKET_";

    private static ThreadLocal<String> currentTicket = new ThreadLocal<String>();

    private boolean ticketsExpire;

    private Duration validDuration;

    private boolean oneOff;
    
    private String guid;

    private SimpleCache<String, Ticket> ticketsCache; // Can't use Ticket as it's private
    
    private ExpiryMode expiryMode = ExpiryMode.AFTER_FIXED_TIME;

    /**
     * IOC constructor
     */
    public InMemoryTicketComponentImpl()
    {
        super();
        guid = GUID.generate();
    }

    /**
     * Set the ticket cache to support clustering
     * 
     * @param ticketsCache
     */
    public void setTicketsCache(SimpleCache<String, Ticket> ticketsCache)
    {
        this.ticketsCache = ticketsCache;
    }

    public String getNewTicket(String userName, String sessionId) throws AuthenticationException
    {
        Date expiryDate = null;
        if (ticketsExpire)
        {
            expiryDate = Duration.add(new Date(), validDuration);
        }
        Ticket ticket = new Ticket(ticketsExpire ? expiryMode : ExpiryMode.DO_NOT_EXPIRE, expiryDate, userName,
                validDuration, sessionId == null ? Collections.<String> emptySet() : Collections.singleton(sessionId));
        ticketsCache.put(ticket.getTicketId(), ticket);
        String ticketString = GRANTED_AUTHORITY_TICKET_PREFIX + ticket.getTicketId();
        currentTicket.set(ticketString);
        return ticketString;
    }

    public String validateTicket(String ticketString, String sessionId) throws AuthenticationException
    {
        String ticketKey = getTicketKey(ticketString);
        Ticket ticket = this.ticketsCache.get(ticketKey);
        if (ticket == null)
        {
            throw new AuthenticationException("Missing ticket for " + ticketString);
        }
        if (ticket.hasExpired())
        {
            throw new TicketExpiredException("Ticket expired for " + ticketString);
        }
        // TODO: Recheck the user details here
        // TODO: Strengthen ticket as GUID is predicatble
        if (oneOff)
        {
            ticketsCache.remove(ticketKey);
        }
        // Make sure the association with the session is recorded
        else if (sessionId != null)
        {
            Ticket newTicket = ticket.addSessionId(sessionId);
            if (newTicket != ticket)
            {
                ticketsCache.put(ticketKey, newTicket);
                ticket = newTicket;
            }
        }
        currentTicket.set(ticketString);
        return ticket.getUserName();
    }

    /**
     * Helper method to find a ticket
     * 
     * @param ticketString
     * @return - the ticket
     */
    private Ticket getTicketByTicketString(String ticketString)
    {
        Ticket ticket = ticketsCache.get(getTicketKey(ticketString));
        return ticket;
    }

    /**
     * Helper method to extract the ticket id from the ticket string
     * 
     * @param ticketString
     * @return - the ticket key
     */
    private String getTicketKey(String ticketString)
    {
        if (ticketString.length() < GRANTED_AUTHORITY_TICKET_PREFIX.length())
        {
            throw new AuthenticationException(ticketString + " is an invalid ticket format");
        }
        String key = ticketString.substring(GRANTED_AUTHORITY_TICKET_PREFIX.length());
        return key;
    }

    public void invalidateTicketById(String ticketString, String sessionId)
    {
        String ticketKey = getTicketKey(ticketString);
        // If we are dissassociating the ticket from an app server session, it may still not be time to expire it, as it
        // may be in use by other sessions
        if (sessionId != null)
        {
            Ticket ticketObj = ticketsCache.get(ticketKey);
            if (ticketObj != null)
            {
                ticketObj = ticketObj.removeSessionId(sessionId);
                if (ticketObj == null)
                {
                    ticketsCache.remove(ticketKey);
                }
                else
                {
                    ticketsCache.put(ticketKey, ticketObj);
                }
            }
        }
        else
        {
            ticketsCache.remove(ticketKey);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.TicketComponent#getUsersWithTickets(boolean)
     */
    public Set<String> getUsersWithTickets(boolean nonExpiredOnly) 
    {
    	Set<String> users = new HashSet<String>();
    	for (String key : ticketsCache.getKeys())
        {
    		Ticket ticket = ticketsCache.get(key);
    		if (ticket != null)
    		{
        		if ((nonExpiredOnly == false) || (! ticket.hasExpired()))
        		{
        			users.add(ticket.getUserName());
        		}
    		}
        }
    	return users;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.TicketComponent#countTickets(boolean)
     */
    public int countTickets(boolean nonExpiredOnly)
    {
    	if (nonExpiredOnly)
    	{
    		int count = 0;
    		for (String key : ticketsCache.getKeys())
            {
    			Ticket ticket = ticketsCache.get(key);
    			if (! ticket.hasExpired())
    			{
    				count++;
    			}
            }
    		return count;
    	}
    	else
    	{
    		return ticketsCache.getKeys().size();
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.TicketComponent#invalidateTickets(boolean)
     */
    public int invalidateTickets(boolean expiredOnly)
    {
    	int count = 0;
    	if (! expiredOnly)
    	{
    		count = ticketsCache.getKeys().size();
    		ticketsCache.clear();
    	}
    	else
    	{
	    	for (String key : ticketsCache.getKeys())
	        {
	    		Ticket ticket = ticketsCache.get(key);
	    		if (ticket == null || ticket.hasExpired())
	    		{
	    			count++;
	    			ticketsCache.remove(key);
	    		}
	        }
    	}
    	return count;
    }

    public void invalidateTicketByUser(String userName)
    {
        Set<String> toRemove = new HashSet<String>();

        for (String key : ticketsCache.getKeys())
        {
            Ticket ticket = ticketsCache.get(key);
            // Hack: The getKeys() call might return keys for null marker objects, yielding null values
            if (ticket == null)
            {
                continue;
            }
            if (ticket.getUserName().equals(userName))
            {
                toRemove.add(ticket.getTicketId());
            }
        }

        for (String id : toRemove)
        {
            ticketsCache.remove(id);
        }
    }


    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((guid == null) ? 0 : guid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final InMemoryTicketComponentImpl other = (InMemoryTicketComponentImpl) obj;
        if (guid == null)
        {
            if (other.guid != null)
                return false;
        }
        else if (!guid.equals(other.guid))
            return false;
        return true;
    }
    
    
    /**
     * Ticket
     * 
     * @author andyh
     */
    public static class Ticket implements Serializable
    {
        private static final long serialVersionUID = -5904510560161261049L;

        private ExpiryMode expires;

        private Date expiryDate;

        private String userName;

        private String ticketId;

        private String guid;
        
        private Duration validDuration;
        
        private Set<String> sessionIds;

        private Ticket(Ticket copy, Set<String> sessionIds)
        {
            this.expires = copy.expires;
            this.expiryDate = copy.expiryDate;
            this.userName = copy.userName;
            this.validDuration = copy.validDuration;
            this.guid = copy.guid;
            this.ticketId = copy.ticketId;
            this.sessionIds = sessionIds;            
        }

        Ticket(ExpiryMode expires, Date expiryDate, String userName, Duration validDuration, Set<String> sessionIds)
        {
            this.expires = expires;
            this.expiryDate = expiryDate;
            this.userName = userName;
            this.validDuration = validDuration;
            this.sessionIds = sessionIds;
            this.guid = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();

            String encode = (expires.toString()) + ((expiryDate == null) ? new Date().toString() : expiryDate.toString()) + userName + guid;
            MessageDigest digester;
            try
            {
                digester = MessageDigest.getInstance("SHA-1");
                this.ticketId = new String(Hex.encodeHex(digester.digest(encode.getBytes())));
            }
            catch (NoSuchAlgorithmException e)
            {
                try
                {
                    digester = MessageDigest.getInstance("MD5");
                    this.ticketId = new String(Hex.encodeHex(digester.digest(encode.getBytes())));
                }
                catch (NoSuchAlgorithmException e1)
                {
                    CRC32 crc = new CRC32();
                    crc.update(encode.getBytes());
                    byte[] bytes = new byte[4];
                    long value = crc.getValue();
                    bytes[0] = (byte) (value & 0xFF);
                    value >>>= 4;
                    bytes[1] = (byte) (value & 0xFF);
                    value >>>= 4;
                    bytes[2] = (byte) (value & 0xFF);
                    value >>>= 4;
                    bytes[3] = (byte) (value & 0xFF);
                    this.ticketId = new String(Hex.encodeHex(bytes));
                }
            }
        }

        public Ticket addSessionId(String sessionId)
        {
            if (this.sessionIds.contains(sessionId))
            {
                return this;
            }
            Set<String> newSessionIds = new HashSet<String>(this.sessionIds.size() * 2 + 2);
            newSessionIds.addAll(this.sessionIds);
            newSessionIds.add(sessionId);
            return new Ticket(this, newSessionIds);
        }

        public Ticket removeSessionId(String sessionId)
        {
            if (this.sessionIds.contains(sessionId))
            {
                Set<String> newSessionIds;
                if (this.sessionIds.size() > 1)
                {
                    newSessionIds = new HashSet<String>(this.sessionIds.size() * 2 - 2);
                    newSessionIds.addAll(this.sessionIds);
                    newSessionIds.remove(sessionId);
                    return new Ticket(this, newSessionIds);
                }
                return null;
            }
            return this;
        }

        /**
         * Has the ticket expired
         * 
         * @return - if expired
         */
        boolean hasExpired()
        {
            switch (expires)
            {
                case AFTER_FIXED_TIME:
                    if ((expiryDate != null) && (expiryDate.compareTo(new Date()) < 0))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                    
                case AFTER_INACTIVITY:
                    Date now = new Date();
                    if ((expiryDate != null) && (expiryDate.compareTo(now) < 0))
                    {
                        return true;
                    }
                    else
                    {
                        expiryDate = Duration.add(now, validDuration);
                        return false;
                    }
                    
                case DO_NOT_EXPIRE:
                default:
                    return false;
            }
        }

        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }
            if (!(o instanceof Ticket))
            {
                return false;
            }
            Ticket t = (Ticket) o;
            return (this.expires == t.expires) && this.expiryDate.equals(t.expiryDate) && this.userName.equals(t.userName) && this.ticketId.equals(t.ticketId);
        }

        public int hashCode()
        {
            return ticketId.hashCode();
        }

        protected ExpiryMode getExpires()
        {
            return expires;
        }

        protected Date getExpiryDate()
        {
            return expiryDate;
        }

        protected String getTicketId()
        {
            return ticketId;
        }

        protected String getUserName()
        {
            return userName;
        }

        protected Set<String> getSessionIds()
        {
            return sessionIds;
        }
    }

    /**
     * Are tickets single use
     * 
     * @param oneOff
     */
    public void setOneOff(boolean oneOff)
    {
        this.oneOff = oneOff;
    }

    /**
     * Do tickets expire
     * 
     * @param ticketsExpire
     */
    public void setTicketsExpire(boolean ticketsExpire)
    {
        this.ticketsExpire = ticketsExpire;
    }

    
    /**
     * How should tickets expire.
     * @param exipryMode
     */
    public void setExpiryMode(String expiryMode)
    {
        this.expiryMode = ExpiryMode.valueOf(expiryMode);
    }

    /**
     * How long are tickets valid (XML duration as a string)
     * 
     * @param validDuration
     */
    public void setValidDuration(String validDuration)
    {
        this.validDuration = new Duration(validDuration);
    }

    public String getAuthorityForTicket(String ticketString)
    {
        Ticket ticket = getTicketByTicketString(ticketString);
        if (ticket == null)
        {
            return null;
        }
        return ticket.getUserName();
    }

    public String getCurrentTicket(String userName, String sessionId, boolean autoCreate)
    {
        String ticketString = currentTicket.get();
        if (ticketString == null)
        {
            return autoCreate ? getNewTicket(userName, sessionId) : null;
        }
        String ticketKey = getTicketKey(ticketString);
        Ticket ticketObj = this.ticketsCache.get(ticketKey);
        if (ticketObj != null && userName.equals(ticketObj.getUserName()))       
        {
            if (sessionId != null)
            {
                // A current, as yet unclaimed valid ticket. Make the association with the session now
                if (ticketObj.getSessionIds().isEmpty())
                {
                    this.ticketsCache.put(ticketKey, ticketObj.addSessionId(sessionId));
                }
                // The ticket is already claimed by at least one other session, so start a new one
                else
                {
                    return autoCreate ? getNewTicket(userName, sessionId) : null;
                }
            }
            return ticketString;
        }
        else
        {
            return autoCreate ? getNewTicket(userName, sessionId) : null;
        }
    }

    public void clearCurrentTicket()
    {
        clearCurrentSecurityContext();
    }

    public static void clearCurrentSecurityContext()
    {
        currentTicket.set(null);
    }

    public enum ExpiryMode
    {
        AFTER_INACTIVITY, AFTER_FIXED_TIME, DO_NOT_EXPIRE;
    }
}
