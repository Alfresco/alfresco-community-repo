/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.security.authentication;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;

import com.fasterxml.uuid.Generators;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;

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

    private static Log logger = LogFactory.getLog(InMemoryTicketComponentImpl.class);

    private static ThreadLocal<String> currentTicket = new ThreadLocal<String>();
    private boolean ticketsExpire;
    private Duration validDuration;
    private boolean oneOff;
    private String guid;
    private SimpleCache<String, Ticket> ticketsCache; // Can't use Ticket as it's private
    private SimpleCache<String, String> usernameToTicketIdCache;
    private ExpiryMode expiryMode = ExpiryMode.AFTER_INACTIVITY;
    private boolean useSingleTicketPerUser = true;

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
     */
    public void setTicketsCache(SimpleCache<String, Ticket> ticketsCache)
    {
        this.ticketsCache = ticketsCache;
    }

    /**
     * Set the usernameToTicketIdCache as secondary map for supporting cache clustering
     */
    public void setUsernameToTicketIdCache(SimpleCache<String, String> usernameToTicketIdCache)
    {
        this.usernameToTicketIdCache = usernameToTicketIdCache;
    }

    /**
     * @param useSingleTicketPerUser
     *            the useSingleTicketPerUser to set
     */
    public void setUseSingleTicketPerUser(boolean useSingleTicketPerUser)
    {
        this.useSingleTicketPerUser = useSingleTicketPerUser;
    }

    /**
     * @return the useSingleTicketPerUser
     */
    public boolean getUseSingleTicketPerUser()
    {
        return useSingleTicketPerUser;
    }

    /**
     * Are tickets single use
     */
    @Deprecated
    public void setOneOff(boolean oneOff)
    {
        this.oneOff = oneOff;
        if (this.oneOff)
        {
            logger.warn("The 'oneOff' feature has been deprecated and will be removed in a future version. "
                    + "This feature may not work as intended even in the current version");
        }
    }

    /**
     * Do tickets expire
     */
    public void setTicketsExpire(boolean ticketsExpire)
    {
        this.ticketsExpire = ticketsExpire;
    }

    /**
     * How should tickets expire.
     */
    public void setExpiryMode(String expiryMode)
    {
        this.expiryMode = ExpiryMode.valueOf(expiryMode);
    }

    /**
     * How long are tickets valid (XML duration as a string)
     */
    public void setValidDuration(String validDuration)
    {
        this.validDuration = new Duration(validDuration);
    }

    /**
     * All put operations into ticketsCache and usernameToTicketIdCache should go through this method, so we can debug/trace ticket problems easier from the logs
     */
    private void putIntoCache(Ticket ticket)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Putting into ticketsCache " + ticketsCache.toString() + " ticket: " + ticket);
        }
        ticketsCache.put(ticket.getTicketId(), ticket);

        if (logger.isTraceEnabled())
        {
            logger.trace("Putting into usernameToTicketIdCache " + usernameToTicketIdCache.toString() + " username and ticketId of: " + ticket);
        }
        usernameToTicketIdCache.put(ticket.getUserName(), ticket.getTicketId());
    }

    /**
     * All remove operations from ticketsCache and usernameToTicketIdCache should go through this method, so we can debug/trace ticket problems easier from the logs
     */
    private void removeFromCache(String ticketId)
    {
        Ticket ticket = null;
        if (ticketId != null)
        {
            ticket = ticketsCache.get(ticketId);
        }

        if (logger.isTraceEnabled())
        {
            logger.trace("Removing ticket from ticketsCache: " + ticketId);
        }
        ticketsCache.remove(ticketId);

        if (ticket != null)
        {
            String username = ticket.getUserName();
            String actualUserTicketIdFromCache = usernameToTicketIdCache.get(username);
            if (ticketId.equals(actualUserTicketIdFromCache))
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Removing ticketId from usernameToTicketIdCache for: " + username);
                }
                usernameToTicketIdCache.remove(username);
            }
        }
    }

    @Override
    public String getNewTicket(String userName) throws AuthenticationException
    {
        Ticket ticket = null;
        if (useSingleTicketPerUser)
        {
            ticket = findNonExpiredUserTicket(userName);
        }

        if (ticket == null)
        {
            Date expiryDate = null;
            if (ticketsExpire)
            {
                expiryDate = Duration.add(new Date(), validDuration);
            }
            ticket = new Ticket(ticketsExpire ? expiryMode : ExpiryMode.DO_NOT_EXPIRE, expiryDate, userName, validDuration);
            putIntoCache(ticket);
        }

        String ticketString = GRANTED_AUTHORITY_TICKET_PREFIX + ticket.getTicketId();
        currentTicket.set(ticketString);
        if (logger.isTraceEnabled())
        {
            logger.trace("Setting the current ticket for this thread: " + Thread.currentThread().getName() + " to: " + ticketString);
        }
        return ticketString;
    }

    private Ticket findNonExpiredUserTicket(String userName)
    {
        String userTicketIdFromCache = usernameToTicketIdCache.get(userName);
        if (userTicketIdFromCache != null)
        {
            Ticket ticketFromCache = ticketsCache.get(userTicketIdFromCache);
            if (ticketFromCache != null)
            {
                Ticket newTicket = ticketFromCache.getNewEntry();
                if (newTicket != null)
                {
                    if (newTicket != ticketFromCache)
                    {
                        putIntoCache(newTicket);
                    }
                    return newTicket;
                }
            }
        }
        return null;
    }

    @Override
    public String validateTicket(String ticketString) throws AuthenticationException
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Validating ticket: " + ticketString);
        }
        String ticketKey = getTicketKey(ticketString);
        Ticket ticket = ticketsCache.get(ticketKey);
        if (ticket == null)
        {
            final String msg = "Missing ticket for " + ticketString;
            if (logger.isDebugEnabled())
            {
                logger.debug(msg);
            }
            throw new AuthenticationException(msg);
        }
        Ticket newTicket = ticket.getNewEntry();
        if (newTicket == null)
        {
            final String msg = "Ticket expired for " + ticketString;
            if (logger.isDebugEnabled())
            {
                logger.debug(msg);
            }
            throw new TicketExpiredException(msg);
        }
        if (oneOff)
        {
            // this feature is deprecated

            removeFromCache(ticketKey);
        }
        else if (newTicket != ticket)
        {
            putIntoCache(newTicket);
        }
        currentTicket.set(ticketString);
        if (logger.isTraceEnabled())
        {
            logger.trace("Setting the current ticket for this thread: " + Thread.currentThread().getName() + " to: " + ticketString);
        }
        return newTicket.getUserName();
    }

    /**
     * Helper method to find a ticket
     * 
     * @param ticketString
     *            String
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
     *            String
     * @return - the ticket key
     */
    private String getTicketKey(String ticketString)
    {
        if (ticketString == null)
        {
            return null;
        }
        else if (ticketString.length() < GRANTED_AUTHORITY_TICKET_PREFIX.length())
        {
            throw new AuthenticationException(ticketString + " is an invalid ticket format");
        }

        String key = ticketString.substring(GRANTED_AUTHORITY_TICKET_PREFIX.length());
        return key;
    }

    @Override
    public void invalidateTicketById(String ticketString)
    {
        String key = ticketString.substring(GRANTED_AUTHORITY_TICKET_PREFIX.length());
        removeFromCache(key);
    }

    @Override
    public Set<String> getUsersWithTickets(boolean nonExpiredOnly)
    {
        Date now = new Date();
        Set<String> users = new HashSet<String>();
        for (String key : ticketsCache.getKeys())
        {
            Ticket ticket = ticketsCache.get(key);
            if (ticket != null)
            {
                if ((nonExpiredOnly == false) || !ticket.hasExpired(now))
                {
                    users.add(ticket.getUserName());
                }
            }
        }
        return users;
    }

    @Override
    public int countTickets(boolean nonExpiredOnly)
    {
        Date now = new Date();
        if (nonExpiredOnly)
        {
            int count = 0;
            for (String key : ticketsCache.getKeys())
            {
                Ticket ticket = ticketsCache.get(key);
                if (ticket != null && !ticket.hasExpired(now))
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

    @Override
    public int invalidateTickets(boolean expiredOnly)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Invalidate all tickets, expired only: " + expiredOnly);
        }
        Date now = new Date();
        int count = 0;
        if (!expiredOnly)
        {
            count = ticketsCache.getKeys().size();
            if (logger.isTraceEnabled())
            {
                logger.trace("Clearing all tickets from the ticketsCache, that used to have size: " + count);
            }
            ticketsCache.clear();
            usernameToTicketIdCache.clear();
        }
        else
        {
            Set<String> toRemove = new HashSet<>();
            for (String key : ticketsCache.getKeys())
            {
                Ticket ticket = ticketsCache.get(key);
                if (ticket == null || ticket.hasExpired(now))
                {
                    count++;
                    toRemove.add(key);
                }
            }
            for (String id : toRemove)
            {
                removeFromCache(id);
            }
        }
        return count;
    }

    @Override
    public void invalidateTicketByUser(String userName)
    {
        Set<String> toRemove = new HashSet<>();

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
            removeFromCache(id);
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

        private final ExpiryMode expires;

        private final Date expiryDate;

        private final String userName;

        private final String ticketId;

        private final Duration validDuration;

        private final Duration testDuration;

        Ticket(ExpiryMode expires, Date expiryDate, String userName, Duration validDuration)
        {
            checkValidTicketParameters(expires, expiryDate, userName, validDuration);
            this.expires = expires;
            this.expiryDate = expiryDate;
            this.userName = userName;
            this.validDuration = validDuration;
            this.testDuration = validDuration.divide(2);
            final String guid = Generators.randomBasedGenerator().generate().toString();

            this.ticketId = computeTicketId(expires, expiryDate, userName, guid);

            if (logger.isTraceEnabled())
            {
                logger.trace("Creating new ticket for user: " + AuthenticationUtil.maskUsername(userName) + " with ticketId: " + this.ticketId);
            }
        }

        private static String computeTicketId(ExpiryMode expires, Date expiryDate, String userName, String guid)
        {
            String encode = expires.toString() + getExpireDateAsString(expiryDate) + userName + guid;
            MessageDigest digester;
            String ticketId;
            try
            {
                digester = MessageDigest.getInstance("SHA-1");
                ticketId = new String(Hex.encodeHex(digester.digest(encode.getBytes())));
            }
            catch (NoSuchAlgorithmException e)
            {
                try
                {
                    digester = MessageDigest.getInstance("MD5");
                    ticketId = new String(Hex.encodeHex(digester.digest(encode.getBytes())));
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
                    ticketId = new String(Hex.encodeHex(bytes));
                }
            }
            return ticketId;
        }

        private Ticket(ExpiryMode expires, Date expiryDate, String userName, Duration validDuration, String ticketId)
        {
            checkValidTicketParameters(expires, expiryDate, userName, validDuration);
            ParameterCheck.mandatory("ticketId", ticketId);

            this.expires = expires;
            this.expiryDate = expiryDate;
            this.userName = userName;
            this.validDuration = validDuration;
            Duration tenPercent = validDuration.divide(10);
            this.testDuration = validDuration.subtract(tenPercent);
            this.ticketId = ticketId;

            if (logger.isTraceEnabled())
            {
                logger.trace(
                        "Creating (cloning) new ticket for user: " + AuthenticationUtil.maskUsername(userName) + " with ticketId: " + this.ticketId);
            }
        }

        private static void checkValidTicketParameters(ExpiryMode expires, Date expiryDate, String userName, Duration validDuration)
        {
            ParameterCheck.mandatory("expires mode", expires);
            ParameterCheck.mandatoryString("userName", userName);
            ParameterCheck.mandatory("validDuration", validDuration);
            if (!ExpiryMode.DO_NOT_EXPIRE.equals(expires))
            {
                ParameterCheck.mandatory("expiryDate", expiryDate);
            }
        }

        private static String getExpireDateAsString(Date expiryDate)
        {
            return (expiryDate == null) ? "" : expiryDate.toString();
        }

        boolean hasExpired(Date now)
        {
            return ((expiryDate != null) && (expiryDate.compareTo(now) < 0));
        }

        Ticket getNewEntry()
        {
            switch (expires)
            {
            case AFTER_FIXED_TIME:
                if (hasExpired(new Date()))
                {
                    return null;
                }
                else
                {
                    return this;
                }

            case AFTER_INACTIVITY:
                Date now = new Date();
                if (hasExpired(now))
                {
                    return null;
                }
                else
                {
                    Duration remaining = new Duration(now, expiryDate);
                    if (remaining.compareTo(testDuration) < 0)
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace("AFTER_INACTIVITY case, Creating new ticket based on the current one that expires at: " + expiryDate);
                        }
                        return new Ticket(expires, Duration.add(now, validDuration), userName, validDuration, ticketId);
                    }
                    else
                    {
                        return this;
                    }
                }

            case DO_NOT_EXPIRE:
            default:
                return this;
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

            return this.ticketId.equals(t.ticketId) && this.userName.equals(t.userName) && this.expires.equals(t.expires) && areTheExpiryDatesEqual(t);
        }

        private boolean areTheExpiryDatesEqual(Ticket t)
        {
            if (ExpiryMode.DO_NOT_EXPIRE.equals(this.expires))
            {
                // if we have tickets that do not expire, we don't care about their expiration date
                // this.expiryDate should be null
                return true;
            }

            // in this case this.expiryDate should not ever be null; There are checks in the constructors that validate this;
            // but just in case, somehow it is null: print a warning, then fail
            if (this.expiryDate == null)
            {
                logger.warn("expiryDate should not be null in this case");
            }
            return this.expiryDate.equals(t.expiryDate);
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

        @Override
        public String toString()
        {
            return "Ticket with ticketId: " + ticketId + " for user: " + AuthenticationUtil.maskUsername(userName) + " ExpiryMode: " + expires
                    + " expire date: " + expiryDate + " validDuration: " + validDuration;
        }
    }

    @Override
    public String getAuthorityForTicket(String ticketString)
    {
        Ticket ticket = getTicketByTicketString(ticketString);
        if (ticket == null)
        {
            return null;
        }
        return ticket.getUserName();
    }

    @Override
    public String getCurrentTicket(String userName, boolean autoCreate)
    {
        String ticket = currentTicket.get();
        if (ticket == null)
        {
            return autoCreate ? getNewTicket(userName) : null;
        }
        String ticketUser = getAuthorityForTicket(ticket);
        if (userName.equals(ticketUser))
        {
            return ticket;
        }
        else
        {
            return autoCreate ? getNewTicket(userName) : null;
        }
    }

    public void clearCurrentTicket()
    {
        clearCurrentSecurityContext();
    }

    public static void clearCurrentSecurityContext()
    {
        String prevTicket = currentTicket.get();
        currentTicket.set(null);
        if (logger.isTraceEnabled())
        {
            logger.trace("Clearing the current ticket for this thread: " + Thread.currentThread().getName() + " . Previous ticket was: " + prevTicket);
        }
    }

    public enum ExpiryMode
    {
        AFTER_INACTIVITY, AFTER_FIXED_TIME, DO_NOT_EXPIRE;
    }
}
