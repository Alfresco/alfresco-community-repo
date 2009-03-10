/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.jgroups;

import java.io.FileNotFoundException;
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
import org.alfresco.util.AbstractLifecycleBean;
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
import org.jgroups.JChannelFactory;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.TimeoutException;
import org.jgroups.UpHandler;
import org.jgroups.View;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.ResourceUtils;

/**
 * A cache peer provider that does heartbeat sending and receiving using JGroups.
 * <p>
 * The cluster name needs to be set before any communication is possible.  This can be done using the
 * system property<br>
 *    {@link #PROP_CLUSTER_NAME_PREFIX -Dalfresco.cluster-name-prefix}=MyCluster
 * or by declaring a bean
 * <code><pre>
 *    <bean id="jchannelFactory" class="org.alfresco.repo.jgroups.AlfrescoJChannelFactory">
 *       <property name="clusterNamePrefix">
 *          <value>MyCluster</value>
 *       </property>
 *    </bean>
 * </pre></code>
 * <p>
 * The channels provided to the callers will be proxies to underlying channels that will be hot-swappable.
 * This means that the client code can continue to use the channel references while the actual
 * implementation can be switched in and out as required.
 *  
 * @see #PROP_CLUSTER_NAME_PREFIX
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
    /** The UDP protocol stack (default) */
    public static final String PROTOCOL_STACK_UDP = "UDP";
    /** The TCP protocol stack */
    public static final String PROTOCOL_STACK_TCP = "TCP";
    
    
    public static final String PROP_CLUSTER_NAME_PREFIX = "alfresco.cluster-name-prefix";
    public static final String CUSTOM_CONFIGURATION_FILE = "classpath:alfresco/extension/jgroups-custom.xml";
    public static final String DEFAULT_CONFIGURATION_FILE = "classpath:alfresco/jgroups-default.xml";
    
    private static Log logger = LogFactory.getLog(AlfrescoJGroupsChannelFactory.class);
    
    // Synchronization locks
    private static ReadLock readLock;
    private static WriteLock writeLock;
    
    // Values that are modified by the bean implementation
    private static String clusterNamePrefix;
    private static URL configUrl;
    private static Map<String, String> stacksByAppRegion;
    
    // Derived data
    /** A map that stores channel information by the application region. */
    private static final Map<String, ChannelProxy> channels;
    private static JChannelFactory channelFactory;
    
    static
    {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);        // Fair
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
        
        channels = new HashMap<String, ChannelProxy>(5);
        
        clusterNamePrefix = null;
        configUrl = null;
        stacksByAppRegion = new HashMap<String, String>(5);
        stacksByAppRegion.put(
                AlfrescoJGroupsChannelFactory.APP_REGION_EHCACHE_HEARTBEAT,
                AlfrescoJGroupsChannelFactory.PROTOCOL_STACK_UDP);
        stacksByAppRegion.put(
                AlfrescoJGroupsChannelFactory.APP_REGION_DEFAULT,
                AlfrescoJGroupsChannelFactory.PROTOCOL_STACK_UDP);
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
        for (Map.Entry<String, ChannelProxy> entry : channels.entrySet())
        {
            ChannelProxy channelProxy = entry.getValue();
            
            // Close the channel via the proxy
            try
            {
                channelProxy.close();
                channelProxy.shutdown();
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
     * Creates a channel for the cluster.  This method should not be heavily used
     * as the checks and synchronizations will slow the calls.  Returns channels can be
     * kept and will be modified directly using the factory-held references, if necessary.
     * <p>
     * The application region is used to determine the protocol stack to apply.
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
            ChannelProxy channelProxy = channels.get(appRegion);
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
            // Double check
            ChannelProxy channelProxy = channels.get(appRegion);
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
            channels.put(appRegion, channelProxy);
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
     * to determine the protocol stack to apply.
     * 
     * @param appRegion             the application region identifier.
     * @return                      Returns a channel
     */
    /* All calls to this are ultimately wrapped in the writeLock. */
    private static /*synchronized*/ Channel getChannelInternal(String appRegion)
    {
        Channel channel;
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
            JChannelFactory channelFactory = getChannelFactory();
            // Get the protocol stack to use
            String stack = stacksByAppRegion.get(appRegion);
            if (stack == null)
            {
                stack = stacksByAppRegion.get(AlfrescoJGroupsChannelFactory.APP_REGION_DEFAULT);
            }
            if (stack == null)
            {
                throw new AlfrescoRuntimeException(
                        "No protocol stack was found for application region: \n" +
                        "   Cluster prefix:  " + clusterNamePrefix + "\n" +
                        "   App region:      " + appRegion + "\n" +
                        "   Regions defined: " + stacksByAppRegion);
            }
            try
            {
                // Get the stack config from the factory (we are not using MUX)
                String config = channelFactory.getConfig(stack);
                channel = new JChannel(config);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException(
                        "Failed to create JGroups channel: \n" +
                        "   Cluster prefix:    " + clusterNamePrefix + "\n" +
                        "   App region:        " + appRegion + "\n" +
                        "   Protocol stack:    " + stack + "\n" +
                        "   Configuration URL: " + AlfrescoJGroupsChannelFactory.configUrl,
                        e);
            }
        }
        // Initialise the channel
        try
        {
            String clusterName = clusterNamePrefix + ":" + appRegion;
            // Set reconnect property
            channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
            // Don't accept messages from self
            channel.setOpt(Channel.LOCAL, Boolean.FALSE);
            // No state transfer
            channel.setOpt(Channel.AUTO_GETSTATE, Boolean.FALSE);
            // Connect
            channel.connect(clusterName, null, null, 5000L);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("\n" +
                        "Created JGroups channel: \n" +
                        "   Cluster prefix:    " + clusterNamePrefix + "\n" +
                        "   App region:        " + appRegion + "\n" +
                        "   Channel:           " + channel + "\n" +
                        "   Configuration URL: " + AlfrescoJGroupsChannelFactory.configUrl);
            }
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to initialise JGroups channel: \n" +
                    "   Cluster prefix:    " + clusterNamePrefix + "\n" +
                    "   App region:        " + appRegion + "\n" +
                    "   Channel:           " + channel + "\n" +
                    "   Configuration URL: " + AlfrescoJGroupsChannelFactory.configUrl,
                    e);
        }
        return channel;
    }
    
    /**
     * Builds and initializes a JChannelFactory
     */
    /* All calls to this are ultimately wrapped in the writeLock. */
    private static /*synchronized*/ JChannelFactory getChannelFactory()
    {
        if (AlfrescoJGroupsChannelFactory.channelFactory != null)
        {
            return AlfrescoJGroupsChannelFactory.channelFactory;
        }
        // Set the config location to use
        if (AlfrescoJGroupsChannelFactory.configUrl == null)
        {
            // This was not set by the bean so set it using the default mechanism
            try
            {
                AlfrescoJGroupsChannelFactory.configUrl = ResourceUtils.getURL(CUSTOM_CONFIGURATION_FILE);
            }
            catch (FileNotFoundException e)
            {
                // try the alfresco default
                try
                {
                    AlfrescoJGroupsChannelFactory.configUrl = ResourceUtils.getURL(DEFAULT_CONFIGURATION_FILE);
                }
                catch (FileNotFoundException ee)
                {
                    throw new AlfrescoRuntimeException("Missing default JGroups config: " + DEFAULT_CONFIGURATION_FILE);
                }
            }
        }
        try
        {
            // Construct factory
            AlfrescoJGroupsChannelFactory.channelFactory = new JChannelFactory();
            channelFactory.setMultiplexerConfig(AlfrescoJGroupsChannelFactory.configUrl);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to construct JChannelFactory using config: " + AlfrescoJGroupsChannelFactory.configUrl,
                    e);
        }
        // done
        if (logger.isInfoEnabled())
        {
            logger.info("\n" +
                    "Created JChannelFactory: \n" +
                    "   Cluster Name:  " + (AlfrescoJGroupsChannelFactory.clusterNamePrefix == null ? "" : AlfrescoJGroupsChannelFactory.clusterNamePrefix) + "\n" +
                    "   Stack Mapping: " + AlfrescoJGroupsChannelFactory.stacksByAppRegion + "\n" +
                    "   Configuration: " + AlfrescoJGroupsChannelFactory.configUrl);
        }
        return AlfrescoJGroupsChannelFactory.channelFactory;
    }
    
    /**
     * Throw away all calculated values and rebuild.  This means that the channel factory will
     * be reconstructed from scratch.  All the channels are reconstructed - but this will not
     * affect any references to channels held outside this class as the values returned are proxies
     * on top of hot swappable implementations.
     */
    /* All calls to this are ultimately wrapped in the writeLock. */
    private static /*synchronized*/ void rebuildChannels()
    {
        // First throw away the channel factory.  It will be fetched lazily.
        AlfrescoJGroupsChannelFactory.channelFactory = null;
        
        // Reprocess all the application regions with the new data
        for (Map.Entry<String, ChannelProxy> entry : channels.entrySet())
        {
            String appRegion = entry.getKey();
            ChannelProxy channelProxy = entry.getValue();
            
            // Create the new channel
            Channel newChannel = getChannelInternal(appRegion);
            
            // Now do the hot-swap
            Channel oldChannel = channelProxy.swap(newChannel);
            // Close the old channel
            try
            {
                oldChannel.close();
                oldChannel.shutdown();
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
        }
    }
    
    /**
     * Set the prefix used to identify the different clusters.  Each application region will
     * have a separate cluster name that will be:
     * <pre>
     *    clusterNamePrefix:appRegion
     * </pre>
     * If no cluster name prefix is declared, the cluster is effectively disabled.
     * 
     * @param clusterNamePrefix     a prefix to append to the cluster names used
     */
    public static void changeClusterNamePrefix(String clusterNamePrefix)
    {
        writeLock.lock();
        try
        {
            if (clusterNamePrefix == null || clusterNamePrefix.trim().length() == 0 || clusterNamePrefix.startsWith("${"))
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
     * Configure a mapping between the application regions and the available JGroup protocol stacks.
     * The map <b>must</b> contain a mapping for application region 'DEFAULT'.
     * 
     * @param protocolMap           a mapping from application region (keys) to protocol stacks (values)
     */
    public static void changeProtocolStackMapping(Map<String, String> protocolMap)
    {
        writeLock.lock();
        try
        {
            // Check that there is a mapping for default
            if (!protocolMap.containsKey(AlfrescoJGroupsChannelFactory.APP_REGION_DEFAULT))
            {
                throw new AlfrescoRuntimeException("A protocol stack must be defined for 'DEFAULT'");
            }
            stacksByAppRegion = protocolMap;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Set the URL location of the JGroups configuration file.  This must refer to a MUX-compatible
     * configuration file.
     * 
     * @param configUrl             a url of the form <b>file:...</b> or <b>classpath:</b>
     */
    public static void changeJgroupsConfigurationUrl(String configUrl)
    {
        writeLock.lock();
        try
        {
            AlfrescoJGroupsChannelFactory.configUrl = ResourceUtils.getURL(configUrl);
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to set property 'jgroupsConfigurationUrl'. The url is invalid: " + configUrl,
                    e);
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
     * @see AlfrescoJGroupsChannelFactory#changeProtocolStackMapping(Map)
     */
    public void setProtocolStackMapping(Map<String, String> protocolMap)
    {
        AlfrescoJGroupsChannelFactory.changeProtocolStackMapping(protocolMap);
    }

    /**
     * Set the URL location of the JGroups configuration file.  This must refer to a MUX-compatible
     * configuration file.
     * 
     * @param configUrl             a url of the form <b>file:...</b> or <b>classpath:</b>
     */
    public void setJgroupsConfigurationUrl(String configUrl)
    {
        try
        {
            AlfrescoJGroupsChannelFactory.configUrl = ResourceUtils.getURL(configUrl);
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException(
                    "Failed to set property 'jgroupsConfigurationUrl'. The url is invalid: " + configUrl,
                    e);
        }
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
            super("DUMMY_TP:UDP(mcast_addr=224.10.10.200;mcast_port=5679)");
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
        
        public ChannelProxy(Channel delegate)
        {
            this.delegate = delegate;
            this.delegateChannelListeners = new HashSet<ChannelListener>(7);
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
        public Channel swap(Channel channel)
        {
            // Remove the listeners from the old channel
            delegate.setReceiver(null);
            for (ChannelListener delegateChannelListener : delegateChannelListeners)
            {
                delegate.removeChannelListener(delegateChannelListener);
            }
            delegate.setUpHandler(null);
            
            Channel oldDelegage = delegate;
            
            // Assign the new delegate and carry the listeners over
            delegate = channel;
            delegate.setReceiver(delegateReceiver);
            for (ChannelListener delegateChannelListener : delegateChannelListeners)
            {
                delegate.addChannelListener(delegateChannelListener);
            }
            delegate.setUpHandler(delegateUpHandler);
            // Done
            return oldDelegage;
        }

        @Override
        protected Log getLog()
        {
            throw new UnsupportedOperationException();
        }

        public void setReceiver(Receiver r)
        {
            delegateReceiver = r;
            delegate.setReceiver(r);
        }

        public void addChannelListener(ChannelListener listener)
        {
            if (listener == null)
            {
                return;
            }
            delegateChannelListeners.add(listener);
            delegate.addChannelListener(listener);
        }

        public void removeChannelListener(ChannelListener listener)
        {
            if (listener != null)
            {
                delegateChannelListeners.remove(listener);
            }
            delegate.removeChannelListener(listener);
        }

        public void clearChannelListeners()
        {
            delegateChannelListeners.clear();
            delegate.clearChannelListeners();
        }

        public void setUpHandler(UpHandler up_handler)
        {
            delegateUpHandler = up_handler;
            delegate.setUpHandler(up_handler);
        }

        public void blockOk()
        {
            delegate.blockOk();
        }

        public void close()
        {
            delegate.close();
        }

        public void connect(String cluster_name, Address target, String state_id, long timeout) throws ChannelException
        {
            delegate.connect(cluster_name, target, state_id, timeout);
        }

        public void connect(String cluster_name) throws ChannelException
        {
            delegate.connect(cluster_name);
        }

        public void disconnect()
        {
            delegate.disconnect();
        }

        public void down(Event evt)
        {
            delegate.down(evt);
        }

        public Object downcall(Event evt)
        {
            return delegate.downcall(evt);
        }

        public String dumpQueue()
        {
            return delegate.dumpQueue();
        }

        @SuppressWarnings("unchecked")
        public Map dumpStats()
        {
            return delegate.dumpStats();
        }

        public boolean equals(Object obj)
        {
            return delegate.equals(obj);
        }

        public boolean flushSupported()
        {
            return delegate.flushSupported();
        }

        @SuppressWarnings("unchecked")
        public boolean getAllStates(Vector targets, long timeout) throws ChannelNotConnectedException, ChannelClosedException
        {
            return delegate.getAllStates(targets, timeout);
        }

        public String getChannelName()
        {
            return delegate.getChannelName();
        }

        public String getClusterName()
        {
            return delegate.getClusterName();
        }

        public Map<String, Object> getInfo()
        {
            return delegate.getInfo();
        }

        public Address getLocalAddress()
        {
            return delegate.getLocalAddress();
        }

        public int getNumMessages()
        {
            return delegate.getNumMessages();
        }

        public Object getOpt(int option)
        {
            return delegate.getOpt(option);
        }

        public boolean getState(Address target, long timeout) throws ChannelNotConnectedException, ChannelClosedException
        {
            return delegate.getState(target, timeout);
        }

        public boolean getState(Address target, String state_id, long timeout) throws ChannelNotConnectedException, ChannelClosedException
        {
            return delegate.getState(target, state_id, timeout);
        }

        public View getView()
        {
            return delegate.getView();
        }

        public int hashCode()
        {
            return delegate.hashCode();
        }

        public boolean isConnected()
        {
            return delegate.isConnected();
        }

        public boolean isOpen()
        {
            return delegate.isOpen();
        }

        public void open() throws ChannelException
        {
            delegate.open();
        }

        public Object peek(long timeout) throws ChannelNotConnectedException, ChannelClosedException, TimeoutException
        {
            return delegate.peek(timeout);
        }

        public Object receive(long timeout) throws ChannelNotConnectedException, ChannelClosedException, TimeoutException
        {
            return delegate.receive(timeout);
        }

        public void returnState(byte[] state, String state_id)
        {
            delegate.returnState(state, state_id);
        }

        public void returnState(byte[] state)
        {
            delegate.returnState(state);
        }

        public void send(Address dst, Address src, Serializable obj) throws ChannelNotConnectedException, ChannelClosedException
        {
            delegate.send(dst, src, obj);
        }

        public void send(Message msg) throws ChannelNotConnectedException, ChannelClosedException
        {
            delegate.send(msg);
        }

        public void setChannelListener(ChannelListener channel_listener)
        {
            delegate.setChannelListener(channel_listener);
        }

        public void setInfo(String key, Object value)
        {
            delegate.setInfo(key, value);
        }

        public void setOpt(int option, Object value)
        {
            delegate.setOpt(option, value);
        }

        public void shutdown()
        {
            delegate.shutdown();
        }

        public boolean startFlush(boolean automatic_resume)
        {
            return delegate.startFlush(automatic_resume);
        }

        public boolean startFlush(List<Address> flushParticipants, boolean automatic_resume)
        {
            return delegate.startFlush(flushParticipants, automatic_resume);
        }

        public boolean startFlush(long timeout, boolean automatic_resume)
        {
            return delegate.startFlush(timeout, automatic_resume);
        }

        public void stopFlush()
        {
            delegate.stopFlush();
        }

        public void stopFlush(List<Address> flushParticipants)
        {
            delegate.stopFlush(flushParticipants);
        }

        public String toString()
        {
            return delegate.toString();
        }
    }
}
