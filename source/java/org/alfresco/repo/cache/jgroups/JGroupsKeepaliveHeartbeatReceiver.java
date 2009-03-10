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
package org.alfresco.repo.cache.jgroups;

import net.sf.ehcache.distribution.MulticastKeepaliveHeartbeatSender;

/**
 * Receives heartbeats from any {@link MulticastKeepaliveHeartbeatSender}s out there.
 * <p/>
 * Our own multicast heartbeats are ignored.
 *
 * @author Greg Luck
 * @version $Id: MulticastKeepaliveHeartbeatReceiver.java 556 2007-10-29 02:06:30Z gregluck $
 */
public abstract class JGroupsKeepaliveHeartbeatReceiver
{
//    private static final Log LOG = LogFactory.getLog(MulticastKeepaliveHeartbeatReceiver.class.getName());
//
//    private ExecutorService processingThreadPool;
//    private Set rmiUrlsProcessingQueue = Collections.synchronizedSet(new HashSet());
//    private final InetAddress groupMulticastAddress;
//    private final Integer groupMulticastPort;
//    private MulticastReceiverThread receiverThread;
//    private MulticastSocket socket;
//    private boolean stopped;
//    private final MulticastRMICacheManagerPeerProvider peerProvider;
//
//    /**
//     * Constructor.
//     *
//     * @param peerProvider
//     * @param multicastAddress
//     * @param multicastPort
//     */
//    public MulticastKeepaliveHeartbeatReceiver(
//            MulticastRMICacheManagerPeerProvider peerProvider, InetAddress multicastAddress, Integer multicastPort) {
//        this.peerProvider = peerProvider;
//        this.groupMulticastAddress = multicastAddress;
//        this.groupMulticastPort = multicastPort;
//    }
//
//    /**
//     * Start.
//     *
//     * @throws IOException
//     */
//    final void init() throws IOException {
//        socket = new MulticastSocket(groupMulticastPort.intValue());
//        socket.joinGroup(groupMulticastAddress);
//        receiverThread = new MulticastReceiverThread();
//        receiverThread.start();
//        processingThreadPool = Executors.newCachedThreadPool();
//    }
//
//    /**
//     * Shutdown the heartbeat.
//     */
//    public final void dispose() {
//        LOG.debug("dispose called");
//        processingThreadPool.shutdownNow();
//        stopped = true;
//        receiverThread.interrupt();
//    }
//
//    /**
//     * A multicast receiver which continously receives heartbeats.
//     */
//    private final class MulticastReceiverThread extends Thread {
//
//        /**
//         * Constructor
//         */
//        public MulticastReceiverThread() {
//            super("Multicast Heartbeat Receiver Thread");
//            setDaemon(true);
//        }
//
//        public final void run() {
//            byte[] buf = new byte[PayloadUtil.MTU];
//            try {
//                while (!stopped) {
//                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
//                    try {
//                        socket.receive(packet);
//                        byte[] payload = packet.getData();
//                        processPayload(payload);
//
//
//                    } catch (IOException e) {
//                        if (!stopped) {
//                            LOG.error("Error receiving heartbeat. " + e.getMessage() +
//                                    ". Initial cause was " + e.getMessage(), e);
//                        }
//                    }
//                }
//            } catch (Throwable t) {
//                LOG.error("Multicast receiver thread caught throwable. Cause was " + t.getMessage() + ". Continuing...");
//            }
//        }
//
//        private void processPayload(byte[] compressedPayload) {
//            byte[] payload = PayloadUtil.ungzip(compressedPayload);
//            String rmiUrls = new String(payload);
//            if (self(rmiUrls)) {
//                return;
//            }
//            rmiUrls = rmiUrls.trim();
//            if (LOG.isTraceEnabled()) {
//                LOG.trace("rmiUrls received " + rmiUrls);
//            }
//            processRmiUrls(rmiUrls);
//        }
//
//        /**
//         * This method forks a new executor to process the received heartbeat in a thread pool.
//         * That way each remote cache manager cannot interfere with others.
//         * <p/>
//         * In the worst case, we have as many concurrent threads as remote cache managers.
//         *
//         * @param rmiUrls
//         */
//        private void processRmiUrls(final String rmiUrls) {
//            if (rmiUrlsProcessingQueue.contains(rmiUrls)) {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("We are already processing these rmiUrls. Another heartbeat came before we finished: " + rmiUrls);
//                }
//                return;
//            }
//
//            if (processingThreadPool == null) {
//                return;
//            }
//
//            processingThreadPool.execute(new Runnable() {
//                public void run() {
//                    try {
//                        // Add the rmiUrls we are processing.
//                        rmiUrlsProcessingQueue.add(rmiUrls);
//                        for (StringTokenizer stringTokenizer = new StringTokenizer(rmiUrls,
//                                PayloadUtil.URL_DELIMITER); stringTokenizer.hasMoreTokens();) {
//                            if (stopped) {
//                                return;
//                            }
//                            String rmiUrl = stringTokenizer.nextToken();
//                            registerNotification(rmiUrl);
//                            if (!peerProvider.peerUrls.containsKey(rmiUrl)) {
//                                if (LOG.isDebugEnabled()) {
//                                    LOG.debug("Aborting processing of rmiUrls since failed to add rmiUrl: " + rmiUrl);
//                                }
//                                return;
//                            }
//                        }
//                    } finally {
//                        // Remove the rmiUrls we just processed
//                        rmiUrlsProcessingQueue.remove(rmiUrls);
//                    }
//                }
//            });
//        }
//
//
//        /**
//         * @param rmiUrls
//         * @return true if our own hostname and listener port are found in the list. This then means we have
//         *         caught our onw multicast, and should be ignored.
//         */
//        private boolean self(String rmiUrls) {
//            CacheManager cacheManager = peerProvider.getCacheManager();
//            CacheManagerPeerListener cacheManagerPeerListener = cacheManager.getCachePeerListener();
//            if (cacheManagerPeerListener == null) {
//                return false;
//            }
//            List boundCachePeers = cacheManagerPeerListener.getBoundCachePeers();
//            if (boundCachePeers == null || boundCachePeers.size() == 0) {
//                return false;
//            }
//            CachePeer peer = (CachePeer) boundCachePeers.get(0);
//            String cacheManagerUrlBase = null;
//            try {
//                cacheManagerUrlBase = peer.getUrlBase();
//            } catch (RemoteException e) {
//                LOG.error("Error geting url base");
//            }
//            int baseUrlMatch = rmiUrls.indexOf(cacheManagerUrlBase);
//            return baseUrlMatch != -1;
//        }
//
//        private void registerNotification(String rmiUrl) {
//            peerProvider.registerPeer(rmiUrl);
//        }
//
//
//        /**
//         * {@inheritDoc}
//         */
//        public final void interrupt() {
//            try {
//                socket.leaveGroup(groupMulticastAddress);
//            } catch (IOException e) {
//                LOG.error("Error leaving group");
//            }
//            socket.close();
//            super.interrupt();
//        }
//    }
//
//
}
