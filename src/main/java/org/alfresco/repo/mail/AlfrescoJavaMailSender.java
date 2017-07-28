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
package org.alfresco.repo.mail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

import org.alfresco.util.PropertyCheck;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;


/**
 * This class extends Spring's {@link JavaMailSenderImpl} to pool the {@link Transport}s used to send emails.
 * 
 * This is to overcome problems reported in CLOUD-313.
 *
 * @author Alex Miller
 */
public class AlfrescoJavaMailSender extends JavaMailSenderImpl
{
    private static final long DEAFULT_TIME_BETWEEN_EVICTION_RUNS = 30000l;
    
    private static final Logger log = LoggerFactory.getLogger(AlfrescoJavaMailSender.class);

    /**
     * {@link KeyedPoolableObjectFactory} which uses the {@link Session} returned by {@link JavaMailSenderImpl#getSession()} to create a new
     * {@link Transport}. 
     */
    private final class TransportFactory implements KeyedPoolableObjectFactory
    {
        /**
         * Create a new {@link Transport} using the {@link Session} returned by {@link JavaMailSenderImpl#getSession() getSession()}.
         * 
         * @param key A {@link URLName} containing the connection details
         * @return A new {@link Transport}
         */
        @Override
        public Object makeObject(Object key) throws Exception
        {
            if ((key instanceof URLName) == false)
            {
                throw new IllegalArgumentException("Invlid key type");
            }
            log.debug("Creating new Transport");
            URLName urlName = (URLName) key;
            Transport transport = getSession().getTransport(urlName.getProtocol()); 
            transport.connect(urlName.getHost(), urlName.getPort(), urlName.getUsername(), urlName.getPassword());
            return transport;
        }

        /**
         * Disconnects the pooled {@link Transport} object.
         * 
         * @param key {@link URLName} containing the connection details.
         * @param object Pooled {@link Transport}
         */
        @Override
        public void destroyObject(Object key, Object object) throws Exception
        {
            if (object instanceof Transport == false)
            {
                throw new IllegalArgumentException("Unexpected object type");
            }
            log.debug("Destroying Transpaort");
            Transport transport = (Transport)object;
            transport.close();
        }

        /**
         * Checks to see if the pooled {@link Transport} is still connected.
         * 
         * @param key {@link URLName} containing the connection details.
         * @param object Pooled {@link Transport}
         * 
         * @return true if the pooled transport is still connected, or false,  otherwise.
         */
        @Override
        public boolean validateObject(Object key, Object object)
        {
            if (object instanceof Transport == false)
            {
                throw new IllegalArgumentException("Unexpected object type");
            }
            log.debug("Validating transport");
            Transport transport = (Transport)object;
            return transport.isConnected();
        }

        /**
         * Do nothing
         */
        @Override
        public void activateObject(Object key, Object obj) throws Exception
        {
        }

        /**
         * Do Noting
         */
        @Override
        public void passivateObject(Object key, Object obj) throws Exception
        {
        }
    }

    /**
     * Wrapper implementation of {@link Transport}, which borrows from a pool on connection, and returns to the pool on close.  
     * 
     * @see AlfrescoJavaMailSender#getTransport(Session)
     */
    private static class PooledTransportWrapper extends Transport
    {
        private Transport wrapped = null;
        private String protocol;
        private GenericKeyedObjectPool pool;
        
        /**
         * Default constructor.
         * 
         * @param transportPool Pool to borrow/return transports to/from.
         * @param session {@link Session} used to create new transports.
         * @param protocol Mail protocol for transport.
         */
        private PooledTransportWrapper(GenericKeyedObjectPool transportPool, Session session, String protocol)
        {
            super(session, new URLName(protocol, null, 0, null, null, null));
            this.pool = transportPool;
            this.protocol = protocol;
        }

        /**
         * Send a message, using the borrowed {@link Transport}.
         */
        @Override
        public void sendMessage(Message message, Address[] addresses) throws MessagingException
        {
            if (wrapped == null)
            {
                throw new IllegalStateException ("Not connected!");
            }
            wrapped.sendMessage(message, addresses);
        }

        /**
         * Return the borrowed {@link Transport} to the pool.
         */
        @Override
        public synchronized void close() throws MessagingException
        {
            if (this.wrapped == null ||
                isConnected() == false) 
            {
                throw new IllegalStateException("Already closed");
            }
            try
            {
                log.debug("Returning transport to pool");
                pool.returnObject(getURLName(), wrapped);
                wrapped = null;
            }
            catch (Exception error)
            {
                throw new MessagingException("Unexpected exception returning transport to pool", error);
            }
            finally
            {
                super.close();
            }
        }

        /**
         * Borrow a transport from the pool.
         */
        @Override
        protected boolean protocolConnect(String host, int port, String username, String password) throws MessagingException
        {
            URLName name = new URLName(protocol, host, port, null, username, password);
            try
            {
                log.debug("Borrowing object from pool");
                wrapped = (Transport) pool.borrowObject(name);
                return true;
            }
            catch (MessagingException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new MessagingException("Unexpected exception borrowing connection from pool", ex);
            }
        }

        
    }
    
    private GenericKeyedObjectPool transportPool = new GenericKeyedObjectPool(new TransportFactory());

    public AlfrescoJavaMailSender() 
    {
        transportPool.setMaxActive(-1);
        transportPool.setTestOnBorrow(true);
        transportPool.setTestOnReturn(true);
        transportPool.setTimeBetweenEvictionRunsMillis(DEAFULT_TIME_BETWEEN_EVICTION_RUNS);
        transportPool.setMinEvictableIdleTimeMillis(DEAFULT_TIME_BETWEEN_EVICTION_RUNS);
    }
    
    /**
     * @return A new {@code PooledTransportWrapper} which borrows a pooled {@link Transport} on connect, and returns it to
     *             the pool on close.  
     */
    @Override
    protected Transport getTransport(Session session) throws NoSuchProviderException
    {
        return new PooledTransportWrapper(transportPool, session, getProtocol());

    }
    

    /**
     * Set the maximum number of active transports, managed by the pool. Use a negative value for no limit. Default is -1.
     */
    public void setMaxActive(int maxActive)
    {
        transportPool.setMaxActive(maxActive);
    }
    
    /**
     * Set the maximum number of transports that can sit idle in the pool. Use a negative value for no limit. Default is 8. 
     */
    public void setMaxIdle(int maxIdle)
    {
        transportPool.setMaxIdle(maxIdle);
    }

    /**
     * Set the maximum amount of time (in milliseconds) to wait for a transport to be returned from the pool.
     * Set to 0 or less to block indefinitely. 
     */
    public void setMaxWait(long maxWait)
    {
        transportPool.setMaxWait(maxWait);
    }
    
    /**
     * Set the time (in milliseconds) between runs of the idle object eviction thread. Set to non-positive for no eviction.
     * Default value is 30 seconds.
     */
    public void setTimeBetweenEvictionRuns(long time)
    {
        transportPool.setTimeBetweenEvictionRunsMillis(time);
    }
    
    /**
     * Set the minimum amount of time a transport may sit idle, before it is eligible for eviction. Set to non positive prevent
     * eviction based on idle time.
     * 
     * This value has no affect if timeBetweenEvictionRuns is non positive.
     * 
     * Default value is 30 seconds.
     */
    public void setMinEvictableIdleTime(long time)
    {
        transportPool.setMinEvictableIdleTimeMillis(time);
    }

    @Override
    public void setUsername(String userName)
    {
        if (PropertyCheck.isValidPropertyString(userName))
        {
            super.setUsername(userName);
        }
        else
        {
            super.setUsername(null);
        }
    }

    @Override
    public void setPassword(String password)
    {
        if (PropertyCheck.isValidPropertyString(password))
        {
            super.setPassword(password);
        }
        else
        {
            super.setPassword(null);
        }
    }
}
