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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.apache.commons.codec.binary.Hex;
import org.doomdark.uuid.UUIDGenerator;

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

    private SimpleCache<String, Ticket> ticketsCache; // Can't use Ticket as it's private

    /**
     * IOC constructor
     *
     */
    public InMemoryTicketComponentImpl()
    {
        super();
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

    public String getNewTicket(String userName) throws AuthenticationException
    {
        Date expiryDate = null;
        if (ticketsExpire)
        {
            expiryDate = Duration.add(new Date(), validDuration);
        }
        Ticket ticket = new Ticket(ticketsExpire, expiryDate, userName);
        ticketsCache.put(ticket.getTicketId(), ticket);
        String ticketString = GRANTED_AUTHORITY_TICKET_PREFIX + ticket.getTicketId();
        currentTicket.set(ticketString);
        return ticketString;
    }

    public String validateTicket(String ticketString) throws AuthenticationException
    {
        Ticket ticket = getTicketByTicketString(ticketString);
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
            ticketsCache.remove(getTicketKey(ticketString));
        }
        currentTicket.set(ticketString);
        return ticket.getUserName();
    }

    /**
     * Helper method to find a ticket
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

    public void invalidateTicketById(String ticketString)
    {
        String key = ticketString.substring(GRANTED_AUTHORITY_TICKET_PREFIX.length());
        ticketsCache.remove(key);
    }

    public void invalidateTicketByUser(String userName)
    {
        Set<String> toRemove = new HashSet<String>();

        for (String key : ticketsCache.getKeys())
        {
            Ticket ticket = ticketsCache.get(key);
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

    /**
     * Ticket
     * @author andyh
     *
     */
    public static class Ticket implements Serializable
    {
        private static final long serialVersionUID = -5904510560161261049L;

        private boolean expires;

        private Date expiryDate;

        private String userName;

        private String ticketId;

        private String guid;

        Ticket(boolean expires, Date expiryDate, String userName)
        {
            this.expires = expires;
            this.expiryDate = expiryDate;
            this.userName = userName;
            this.guid = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
            

            String encode = (expires ? "T" : "F") + 
                            ((expiryDate == null) ? new Date().toString(): expiryDate.toString()) + 
                            userName + guid; 
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
                   bytes[0] = (byte)(value & 0xFF);
                   value >>>= 4;
                   bytes[1] = (byte)(value & 0xFF);
                   value >>>= 4;
                   bytes[2] = (byte)(value & 0xFF);
                   value >>>= 4;
                   bytes[3] = (byte)(value & 0xFF);
                   this.ticketId = new String(Hex.encodeHex(bytes));
                }
            }
        }

        /**
         * Has the tick expired
         * 
         * @return - if expired
         */
        boolean hasExpired()
        {
            if (expires && (expiryDate != null) && (expiryDate.compareTo(new Date()) < 0))
            {
                return true;
            }
            else
            {
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
            return (this.expires == t.expires)
                    && this.expiryDate.equals(t.expiryDate) && this.userName.equals(t.userName)
                    && this.ticketId.equals(t.ticketId);
        }

        public int hashCode()
        {
            return ticketId.hashCode();
        }

        protected boolean getExpires()
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

    }

   /**
    * Are tickets single use
    * @param oneOff
    */
    public void setOneOff(boolean oneOff)
    {
        this.oneOff = oneOff;
    }

    /**
     * Do tickets expire
     * @param ticketsExpire
     */
    public void setTicketsExpire(boolean ticketsExpire)
    {
        this.ticketsExpire = ticketsExpire;
    }

    /**
     * How long are tickets valid (XML duration as a string)
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

    public String getCurrentTicket(String userName)
    {
        String ticket = currentTicket.get();
        if(ticket == null)
        {
            return getNewTicket(userName);
        }
        String ticketUser = getAuthorityForTicket(ticket);
        if(userName.equals(ticketUser))
        {
            return ticket;
        }
        else
        {
            return getNewTicket(userName);
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
}
