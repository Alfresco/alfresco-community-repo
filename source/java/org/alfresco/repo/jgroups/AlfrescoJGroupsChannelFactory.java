/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.jgroups;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelListener;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.TimeoutException;
import org.jgroups.UpHandler;
import org.jgroups.View;
import org.jgroups.protocols.LOOPBACK;
import org.jgroups.stack.ProtocolStack;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.util.ResourceUtils;

/**
 * A cache peer provider that does heartbeat sending and receiving using JGroups.
 * <p>
 * The cluster name needs to be set before any communication is possible.  This can be done using the
 * property {@link #setClusterName(String)}.
 * <p>
 * The channels provided to the callers will be proxies to underlying channels that will be hot-swappable.
 * This means that the client code can continue to use the channel references while the actual
 * implementation can be switched in and out as required.
 *  
 * @author Derek Hulley
 * @since 2.1.3
 */
public class AlfrescoJGroupsChannelFactory extends AbstractLifecycleBean
{
    /** A catch-all for unknown application regions. */
    public static final String APP_REGION_DEFAULT = "DEFAULT";
    /** The application region used by the EHCache heartbeat implementation over JGroups. */
    public static final String APP_REGION_EHCACHE_HEARTBEAT = "EHCACHE_HEARTBEAT";
    /** The UDP protocol config (default) */
    public static final String DEFAULT_CONFIG_UDP = "classpath:alfresco/jgroups/alfresco-jgroups-UDP.xml";
    /** The TCP protocol config */
    public static final String DEFAULT_CONFIG_TCP = "classpath:alfresco/jgroups/alfresco-jgroups-TCP.xml";
    
    private static Log logger = LogFactory.getLog(AlfrescoJGroupsChannelFactory.class);
    
    // Synchronization locks
    private static ReadLock readLock;
    private static WriteLock writeLock;
    
    // Values that are modified by the bean implementation
    private static String clusterNamePrefix;
    private static Map<String, String> configUrlsByAppRegion;
    
    // Derived data
    /** A map that stores channel information by the application region. */
    private static final Map<String, ChannelProxy> channelsByAppRegion;
    
    static
    {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);        // Fair
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
        
        channelsByAppRegion = new HashMap<String, ChannelProxy>(5);
        
        clusterNamePrefix = null;
        configUrlsByAppRegion = new HashMap<String, String>(5);
        configUrlsByAppRegion.put(
                AlfrescoJGroupsChannelFactory.APP_REGION_DEFAULT,
                AlfrescoJGroupsChannelFactory.DEFAULT_CONFIG_UDP);
    }
    
    /**
     * Check if a cluster name was provided.
     * 
     * @return          Returns <tt>true</tt> if the cluster configuration is active,
     *                  i.e. a cluster name was provided
     */
    public static boolean isClusterActive()
    {
        readLock.lock();
        try
        {
            return clusterNamePrefix != null;
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    /**
     * Close all channels.  All the channels will be closed and will cease to function.
     */
    private static void closeChannels()
    {
        for (Map.Entry<String, ChannelProxy> entry : channelsByAppRegion.entrySet())
        {
            ChannelProxy channelProxy = entry.getValue();
            
            // Close the channel via the proxy
            try
            {
                channelProxy.close();
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "Closed channel: " + channelProxy);
                }
            }
            catch (Throwable e)
            {
                logger.warn(
                        "Unable to close channel: \n" +
                        "   Channel: " + channelProxy,
                        e);
            }
        }
    }

   /**
    * Returns the configuration URL to use for the given application region.  This might default to the
    * {@link #APP_REGION_DEFAULT default app region}.
    */
   private static String getConfigUrl(String appRegion)
   {
       readLock.lock();
       try
       {
           // Get the configuration to use
           String configUrlStr = configUrlsByAppRegion.get(appRegion);
           if (!PropertyCheck.isValidPropertyString(configUrlStr))
           {
               configUrlStr = configUrlsByAppRegion.get(AlfrescoJGroupsChannelFactory.APP_REGION_DEFAULT);
           }
           if (configUrlStr == null)
           {
               throw new AlfrescoRuntimeException(
                       "No protocol configuration was found for application region: \n" +
                       "   Cluster prefix:  " + clusterNamePrefix + "\n" +
                       "   App region:      " + appRegion + "\n" +
                       "   Regions defined: " + configUrlsByAppRegion);
           }
           return configUrlStr;
       }
       finally
       {
           readLock.unlock();
       }
   }
   
   /**
    /**
     * Creates a channel for the cluster.  This method should not be heavily used
     * as the checks and synchronizations will slow the calls.  Returned channels can be
     * kept and will be modified directly using the factory-held references, if necessary.
     * <p>
     * The application region is used to determine the protocol configuration to apply.
     * <p>
     * This method returns a dummy channel if no cluster name has been provided.
     * 
     * @param appRegion             the application region identifier.
     * @return                      Returns a channel
     */
    public static Channel getChannel(String appRegion)
    {
        readLock.lock();
        try
        {
            ChannelProxy channelProxy = channelsByAppRegion.get(appRegion);
            if (channelProxy != null)
            {
                // This will do
                return channelProxy;
            }
        }
        finally
        {
            readLock.unlock();
        }
        // Being here means that there is no channel yet
        // Go write
        writeLock.lock();
        try
        {
            ChannelProxy channelProxy = channelsByAppRegion.get(appRegion);
            if (channelProxy != null)
            {
                // This will do
                return channelProxy;
            }
            // Get the channel
            Channel channel = getChannelInternal(appRegion);
            // Proxy the channel
            channelProxy = new ChannelProxy(channel);
            // Store the channel to the map
            channelsByAppRegion.put(appRegion, channelProxy);
            // Done
            return channelProxy;
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Creates a channel for the given cluster.  The application region is used
     * to determine the protocol configuration to apply.
     * 
     * @param appRegion             the application region identifier.
     * @return                      Returns a channel
     */
    /* All calls to this are ultimately wrapped in the writeLock. */
    private static /*synchronized*/ Channel getChannelInternal(String appRegion)
    {
        Channel channel;
        URL configUrl = null;
        // If there is no cluster defined (yet) then we define a dummy channel
        if (AlfrescoJGroupsChannelFactory.clusterNamePrefix == null)
        {
            try
            {
                channel = new DummyJChannel();
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException(
                        "Failed to create dummy JGroups channel: \n" +
                        "   Cluster prefix:    " + clusterNamePrefix + "\n" +
                        "   App region:        " + appRegion,
                        e);
            }
        }
        else                    // Create real channel
        {
            // Get the protocol configuration to use
            String configUrlStr = getConfigUrl(appRegion);
            try
            {
                // Construct the JChannel directly
                configUrl = ResourceUtils.getURL(configUrlStr);
                channel = new JChannel(configUrl);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException(
                        "Failed to create JGroups channel: \n" +
                        "   Cluster prefix:    " + clusterNamePrefix + "\n" +
                        "   App region:        " + appRegion + "\n" +
                        "   Regions defined: " + configUrlsByAppRegion + "\n" +
                        "   Configuration URL: " + configUrlStr,
                        e);
            }
        }
        // Initialise the channel
        try
        {
            String clusterName = clusterNamePrefix + ":" + appRegion;
            // Don't accept messages from self
            channel.setOpt(Channel.LOCAL, Boolean.FALSE);
            // Connect
            channel.connect(clusterName);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("\n" +
                        "Created JGroups channel: \n" +
                        "   Cluster prefix:    " + clusterNamePrefix + "\n" +
                        "   App region:        " + appRegion + "\n" +
                        "   Regions defined: " + configUrlsByAppRegion + "\n" +
                        "   Channel:           " + channel + "\n" +
                        "   Configuration URL: " + configUrl);
            }
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to initialise JGroups channel: \n" +
                    "   Cluster prefix:    " + clusterNamePrefix + "\n" +
                    "   App region:        " + appRegion + "\n" +
                    "   Channel:           " + channel + "\n" +
                    "   Configuration URL: " + configUrl,
                    e);
        }
        return channel;
    }
    
    /**
     * Rebuild all the channels using the current cluster name and configuration mappings.
     */
    public static void rebuildChannels()
    {
        writeLock.lock();
        try
        {
            rebuildChannelsInternal();
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Throw away all calculated values and rebuild.  This means that the channel factory will
     * be reconstructed from scratch.  All the channels are reconstructed - but this will not
     * affect any references to channels held outside this class as the values returned are proxies
     * on top of hot swappable implementations.
     * <p>
     * The old channel is closed before the new one is created, so it is possible for a channel
     * held by client code to be rendered unusable during the switch-over.
     */
    /* All calls to this are ultimately wrapped in the writeLock. */
    private static /*synchronized*/ void rebuildChannelsInternal()
    {
        // Reprocess all the application regions with the new data
        for (Map.Entry<String, ChannelProxy> entry : channelsByAppRegion.entrySet())
        {
            String appRegion = entry.getKey();
            ChannelProxy channelProxy = entry.getValue();
            
            // Get the old channel
            Channel oldChannel = channelProxy.getDelegate();
            // Close the old channel.
            try
            {
                oldChannel.close();
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "Closed old channel during channel rebuild: \n" +
                            "   Old channel: " + oldChannel);
                }
            }
            catch (Throwable e)
            {
                logger.warn(
                        "Unable to close old channel during channel rebuild: \n" +
                        "   Old channel: " + oldChannel,
                        e);
            }
            
            // Create the new channel
            Channel newChannel = getChannelInternal(appRegion);
            
            // Now do the hot-swap
            channelProxy.swap(newChannel);
        }
    }
    
    /**
     * Set the prefix used to identify the different clusters.  Each application region will
     * have a separate cluster name that will be:
     * <pre>
     *    clusterNamePrefix:appRegion
     * </pre>
     * If no cluster name prefix is declared, the cluster is effectively disabled.
     * <p>
     * <b>NOTE: </b>The channels must be {@link #rebuildChannels() rebuilt}.
     * 
     * @param clusterNamePrefix     a prefix to append to the cluster names used
     */
    public static void changeClusterNamePrefix(String clusterNamePrefix)
    {
        writeLock.lock();
        try
        {
            if (!PropertyCheck.isValidPropertyString(clusterNamePrefix))
            {
                // Clear everything out
                AlfrescoJGroupsChannelFactory.clusterNamePrefix = null;
            }
            else
            {
                AlfrescoJGroupsChannelFactory.clusterNamePrefix = clusterNamePrefix;
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Configure a mapping between the application regions and the available JGroup protocol configurations.
     * The map <b>must</b> contain a mapping for application region 'DEFAULT'.
     * <p>
     * <b>NOTE: </b>The channels must be {@link #rebuildChannels() rebuilt}.
     * 
     * @param configUrlsByAppRegion     a mapping from application region (keys) to protocol configuration URLs (values)
     */
    private static void changeConfigUrlsMapping(Map<String, String> configUrlsByAppRegion)
    {
        writeLock.lock();
        try
        {
            // Check that there is a mapping for default
            if (!configUrlsByAppRegion.containsKey(AlfrescoJGroupsChannelFactory.APP_REGION_DEFAULT))
            {
                throw new AlfrescoRuntimeException("A configuration URL must be defined for 'DEFAULT'");
            }
            AlfrescoJGroupsChannelFactory.configUrlsByAppRegion = configUrlsByAppRegion;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Bean-enabling constructor
     */
    public AlfrescoJGroupsChannelFactory()
    {
    }
    
    /**
     * @see AlfrescoJGroupsChannelFactory#changeClusterNamePrefix(String)
     */
    public void setClusterName(String clusterName)
    {
        AlfrescoJGroupsChannelFactory.changeClusterNamePrefix(clusterName);
    }
    
    /**
     * @see AlfrescoJGroupsChannelFactory#changeConfigUrlsMapping(Map)
     */
    public void setConfigUrlsByAppRegion(Map<String, String> configUrlsByAppRegion)
    {
        AlfrescoJGroupsChannelFactory.changeConfigUrlsMapping(configUrlsByAppRegion);
    }

    /**
     * @deprecated      Use {@link #setConfigUrlsByAppRegion(Map)}
     */
    public void setProtocolStackMapping(Map<String, String> unused)
    {
        throw new AlfrescoRuntimeException(
                "Properties 'protocolStackMapping' and 'jgroupsConfigurationUrl'" +
                " have been deprecated in favour of 'configUrlsByAppRegion'.");
    }
    
    /**
     * @deprecated      Use {@link #setConfigUrlsByAppRegion(Map)}
     */
    public void setJgroupsConfigurationUrl(String configUrl)
    {
        throw new AlfrescoRuntimeException(
                "Properties 'protocolStackMapping' and 'jgroupsConfigurationUrl'" +
        		" have been deprecated in favour of 'configUrlsByAppRegion'.");
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        AlfrescoJGroupsChannelFactory.rebuildChannels();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        AlfrescoJGroupsChannelFactory.closeChannels();
    }

    /**
     * A no-op JChannel using the "DUMMY_TP" protocol only
     * 
     * @author Derek Hulley
     * @since 2.1.3
     */
    private static class DummyJChannel extends JChannel
    {
        public DummyJChannel() throws ChannelException
        {
            super("org.alfresco.repo.jgroups.AlfrescoJGroupsChannelFactory$DummyProtocol");
        }
    }
    
    public static class DummyProtocol extends LOOPBACK
    {
        @Override
        public String getName()
        {
            return "ALF_DUMMY";
        }

        @Override
        public Object down(Event evt)
        {
            return null;
        }

        @Override
        public Object up(Event evt)
        {
            return null;
        }
    }
    
    /**
     * A proxy channel that can be used to hot-swap underlying channels.  All listeners
     * and the receiver will be carried over to the new underlying channel when it is
     * swapped out.
     * 
     * @author Derek Hulley
     */
    @SuppressWarnings("deprecation")
    public static class ChannelProxy extends Channel
    {
        /*
         * Not synchronizing.  Mostly swapping will be VERY rare and if there is a bit
         * of inconsistency it is not important.
         */
        private Channel delegate;
        private UpHandler delegateUpHandler;
        private Set<ChannelListener> delegateChannelListeners;
        private Receiver delegateReceiver;

        /**
         * @param delegate  the real channel that will do the work
         */
        public ChannelProxy(Channel delegate)
        {
            this.delegate = delegate;
            this.delegateChannelListeners = new HashSet<ChannelListener>(7);
        }
        
        /**
         * @return          Returns the channel to which the implementation will delegate
         */
        public Channel getDelegate()
        {
            return delegate;
        }
        
        /**
         * Swap the channel.  The old delegate will be disconnected before the swap occurs.
         * This guarantees data consistency, assuming that any failures will be handled.
         * <p>
         * Note that the old delegate is not closed or shutdown.
         * 
         * @param           the new delegate
         * @return          the old, disconnected delegate
         */
        public synchronized Channel swap(Channel channel)
        {
            // Remove the listeners from the old channel
            delegate.setReceiver(null);
            for (ChannelListener delegateChannelListener : delegateChannelListeners)
            {
                delegate.removeChannelListener(delegateChannelListener);
            }
            delegate.setUpHandler(null);
            
            Channel oldDelegate = delegate;
            
            // Assign the new delegate and carry the listeners over
            delegate = channel;
            delegate.setReceiver(delegateReceiver);
            for (ChannelListener delegateChannelListener : delegateChannelListeners)
            {
                delegate.addChannelListener(delegateChannelListener);
            }
            delegate.setUpHandler(delegateUpHandler);
            // Done
            return oldDelegate;
        }

        @Override
        protected org.jgroups.logging.Log getLog()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Address getAddress()
        {
            return delegate.getAddress();
        }

        @Override
        public String getName()
        {
            return delegate.getName();
        }

        @Override
        public ProtocolStack getProtocolStack()
        {
            return delegate.getProtocolStack();
        }

        @Override
        public synchronized void setReceiver(Receiver r)
        {
            delegateReceiver = r;
            delegate.setReceiver(r);
        }

        @Override
        public synchronized void addChannelListener(ChannelListener listener)
        {
            if (listener == null)
            {
                return;
            }
            delegateChannelListeners.add(listener);
            delegate.addChannelListener(listener);
        }

        @Override
        public synchronized void removeChannelListener(ChannelListener listener)
        {
            if (listener != null)
            {
                delegateChannelListeners.remove(listener);
            }
            delegate.removeChannelListener(listener);
        }

        @Override
        public synchronized void clearChannelListeners()
        {
            delegateChannelListeners.clear();
            delegate.clearChannelListeners();
        }

        @Override
        public synchronized void setUpHandler(UpHandler up_handler)
        {
            delegateUpHandler = up_handler;
            delegate.setUpHandler(up_handler);
        }

        @Override
        public void blockOk()
        {
            delegate.blockOk();
        }

        @Override
        public void close()
        {
            delegate.close();
        }

        @Override
        public void connect(String cluster_name, Address target, String state_id, long timeout) throws ChannelException
        {
            delegate.connect(cluster_name, target, state_id, timeout);
        }

        @Override
        public void connect(String cluster_name) throws ChannelException
        {
            delegate.connect(cluster_name);
        }

        @Override
        public void disconnect()
        {
            delegate.disconnect();
        }

        @Override
        public void down(Event evt)
        {
            delegate.down(evt);
        }

        @Override
        public Object downcall(Event evt)
        {
            return delegate.downcall(evt);
        }

        @Override
        public String dumpQueue()
        {
            return delegate.dumpQueue();
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Map dumpStats()
        {
            return delegate.dumpStats();
        }

        @Override
        public boolean equals(Object obj)
        {
            return delegate.equals(obj);
        }

        @Override
        public boolean flushSupported()
        {
            return delegate.flushSupported();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean getAllStates(Vector targets, long timeout) throws ChannelNotConnectedException, ChannelClosedException
        {
            return delegate.getAllStates(targets, timeout);
        }

        @Override
        public String getChannelName()
        {
            return delegate.getChannelName();
        }

        @Override
        public String getClusterName()
        {
            return delegate.getClusterName();
        }

        @Override
        public Map<String, Object> getInfo()
        {
            return delegate.getInfo();
        }

        @Override
        public Address getLocalAddress()
        {
            return delegate.getLocalAddress();
        }

        @Override
        public int getNumMessages()
        {
            return delegate.getNumMessages();
        }

        @Override
        public Object getOpt(int option)
        {
            return delegate.getOpt(option);
        }

        @Override
        public boolean getState(Address target, long timeout) throws ChannelNotConnectedException, ChannelClosedException
        {
            return delegate.getState(target, timeout);
        }

        @Override
        public boolean getState(Address target, String state_id, long timeout) throws ChannelNotConnectedException, ChannelClosedException
        {
            return delegate.getState(target, state_id, timeout);
        }

        @Override
        public View getView()
        {
            return delegate.getView();
        }

        @Override
        public int hashCode()
        {
            return delegate.hashCode();
        }

        @Override
        public boolean isConnected()
        {
            return delegate.isConnected();
        }

        @Override
        public boolean isOpen()
        {
            return delegate.isOpen();
        }

        @Override
        public void open() throws ChannelException
        {
            delegate.open();
        }

        @Override
        public Object peek(long timeout) throws ChannelNotConnectedException, ChannelClosedException, TimeoutException
        {
            return delegate.peek(timeout);
        }

        @Override
        public Object receive(long timeout) throws ChannelNotConnectedException, ChannelClosedException, TimeoutException
        {
            return delegate.receive(timeout);
        }

        @Override
        public void returnState(byte[] state, String state_id)
        {
            delegate.returnState(state, state_id);
        }

        @Override
        public void returnState(byte[] state)
        {
            delegate.returnState(state);
        }

        @Override
        public void send(Address dst, Address src, Serializable obj) throws ChannelNotConnectedException, ChannelClosedException
        {
            delegate.send(dst, src, obj);
        }

        @Override
        public void send(Message msg) throws ChannelNotConnectedException, ChannelClosedException
        {
            delegate.send(msg);
        }

        @Override
        public void setChannelListener(ChannelListener channel_listener)
        {
            delegate.setChannelListener(channel_listener);
        }

        @Override
        public void setInfo(String key, Object value)
        {
            delegate.setInfo(key, value);
        }

        @Override
        public void setOpt(int option, Object value)
        {
            delegate.setOpt(option, value);
        }

        @Override
        public void shutdown()
        {
            delegate.shutdown();
        }

        @Override
        public boolean startFlush(boolean automatic_resume)
        {
            return delegate.startFlush(automatic_resume);
        }

        @Override
        public boolean startFlush(List<Address> flushParticipants, boolean automatic_resume)
        {
            return delegate.startFlush(flushParticipants, automatic_resume);
        }

        @Override
        public boolean startFlush(long timeout, boolean automatic_resume)
        {
            return delegate.startFlush(timeout, automatic_resume);
        }

        @Override
        public void stopFlush()
        {
            delegate.stopFlush();
        }

        @Override
        public void stopFlush(List<Address> flushParticipants)
        {
            delegate.stopFlush(flushParticipants);
        }

        @Override
        public synchronized String toString()
        {
            if (delegate instanceof DummyJChannel)
            {
                return delegate.toString() + "(dummy)";
            }
            else
            {
                return delegate.toString();
            }
        }

        @Override
        public String getName(Address member)
        {
            return delegate.getName(member);
        }

        @Override
        public void send(Address dst, Address src, byte[] buf) throws ChannelNotConnectedException, ChannelClosedException
        {
            delegate.send(dst, src, buf);
        }

        @Override
        public void send(Address dst, Address src, byte[] buf, int offset, int length) throws ChannelNotConnectedException, ChannelClosedException
        {
            delegate.send(dst, src, buf, offset, length);
        }

        @Override
        public void setName(String name)
        {
            delegate.setName(name);
        }
    }
}
