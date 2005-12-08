/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.smb.server.notify;

import java.util.Vector;

import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.NotifyChange;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.server.NTTransPacket;
import org.alfresco.filesys.smb.server.SMBSrvPacket;
import org.alfresco.filesys.smb.server.SMBSrvSession;
import org.alfresco.filesys.util.DataPacker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Notify Change Handler Class
 */
public class NotifyChangeHandler implements Runnable
{
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    // Change notification request list and global filter mask

    private NotifyRequestList m_notifyList;
    private int m_globalNotifyMask;

    // Associated disk device context

    private DiskDeviceContext m_diskCtx;

    // Change notification processing thread

    private Thread m_procThread;

    // Change events queue

    private NotifyChangeEventList m_eventList;

    // Debug output enable

    private boolean m_debug = false;

    // Shutdown request flag

    private boolean m_shutdown;

    /**
     * Class constructor
     * 
     * @param diskCtx DiskDeviceContext
     */
    public NotifyChangeHandler(DiskDeviceContext diskCtx)
    {

        // Save the associated disk context details

        m_diskCtx = diskCtx;

        // Allocate the events queue

        m_eventList = new NotifyChangeEventList();

        // Create the processing thread

        m_procThread = new Thread(this);

        m_procThread.setDaemon(true);
        m_procThread.setName("Notify_" + m_diskCtx.getDeviceName());

        m_procThread.start();
    }

    /**
     * Add a request to the change notification list
     * 
     * @param req NotifyRequest
     */
    public final void addNotifyRequest(NotifyRequest req)
    {

        // Check if the request list has been allocated

        if (m_notifyList == null)
            m_notifyList = new NotifyRequestList();

        // Add the request to the list

        req.setDiskContext(m_diskCtx);
        m_notifyList.addRequest(req);

        // Regenerate the global notify change filter mask

        m_globalNotifyMask = m_notifyList.getGlobalFilter();
    }

    /**
     * Remove a request from the notify change request list
     * 
     * @param req NotifyRequest
     */
    public final void removeNotifyRequest(NotifyRequest req)
    {
        removeNotifyRequest(req, true);
    }

    /**
     * Remove a request from the notify change request list
     * 
     * @param req NotifyRequest
     * @param updateMask boolean
     */
    public final void removeNotifyRequest(NotifyRequest req, boolean updateMask)
    {

        // Check if the request list has been allocated

        if (m_notifyList == null)
            return;

        // Remove the request from the list

        m_notifyList.removeRequest(req);

        // Regenerate the global notify change filter mask

        if (updateMask == true)
            m_globalNotifyMask = m_notifyList.getGlobalFilter();
    }

    /**
     * Remove all notification requests owned by the specified session
     * 
     * @param sess SMBSrvSession
     */
    public final void removeNotifyRequests(SMBSrvSession sess)
    {

        // Remove all requests owned by the session

        m_notifyList.removeAllRequestsForSession(sess);

        // Recalculate the global notify change filter mask

        m_globalNotifyMask = m_notifyList.getGlobalFilter();
    }

    /**
     * Determine if the filter has file name change notification, triggered if a file is created,
     * renamed or deleted
     * 
     * @return boolean
     */
    public final boolean hasFileNameChange()
    {
        return hasFilterFlag(NotifyChange.FileName);
    }

    /**
     * Determine if the filter has directory name change notification, triggered if a directory is
     * created or deleted.
     * 
     * @return boolean
     */
    public final boolean hasDirectoryNameChange()
    {
        return hasFilterFlag(NotifyChange.DirectoryName);
    }

    /**
     * Determine if the filter has attribute change notification
     * 
     * @return boolean
     */
    public final boolean hasAttributeChange()
    {
        return hasFilterFlag(NotifyChange.Attributes);
    }

    /**
     * Determine if the filter has file size change notification
     * 
     * @return boolean
     */
    public final boolean hasFileSizeChange()
    {
        return hasFilterFlag(NotifyChange.Size);
    }

    /**
     * Determine if the filter has last write time change notification
     * 
     * @return boolean
     */
    public final boolean hasFileWriteTimeChange()
    {
        return hasFilterFlag(NotifyChange.LastWrite);
    }

    /**
     * Determine if the filter has last access time change notification
     * 
     * @return boolean
     */
    public final boolean hasFileAccessTimeChange()
    {
        return hasFilterFlag(NotifyChange.LastAccess);
    }

    /**
     * Determine if the filter has creation time change notification
     * 
     * @return boolean
     */
    public final boolean hasFileCreateTimeChange()
    {
        return hasFilterFlag(NotifyChange.Creation);
    }

    /**
     * Determine if the filter has the security descriptor change notification
     * 
     * @return boolean
     */
    public final boolean hasSecurityDescriptorChange()
    {
        return hasFilterFlag(NotifyChange.Security);
    }

    /**
     * Check if debug output is enabled
     * 
     * @return boolean
     */
    public final boolean hasDebug()
    {
        return m_debug;
    }

    /**
     * Return the global notify filter mask
     * 
     * @return int
     */
    public final int getGlobalNotifyMask()
    {
        return m_globalNotifyMask;
    }

    /**
     * Return the notify request queue size
     * 
     * @return int
     */
    public final int getRequestQueueSize()
    {
        return m_notifyList != null ? m_notifyList.numberOfRequests() : 0;
    }

    /**
     * Check if the change filter has the specified flag enabled
     * 
     * @param flag
     * @return boolean
     */
    private final boolean hasFilterFlag(int flag)
    {
        return (m_globalNotifyMask & flag) != 0 ? true : false;
    }

    /**
     * File changed notification
     * 
     * @param action int
     * @param path String
     */
    public final void notifyFileChanged(int action, String path)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || hasFileNameChange() == false)
            return;

        // Queue the change notification

        queueNotification(new NotifyChangeEvent(NotifyChange.FileName, action, path, false));
    }

    /**
     * File/directory renamed notification
     * 
     * @param oldName String
     * @param newName String
     */
    public final void notifyRename(String oldName, String newName)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || (hasFileNameChange() == false && hasDirectoryNameChange() == false))
            return;

        // Queue the change notification event

        queueNotification(new NotifyChangeEvent(NotifyChange.FileName, NotifyChange.ActionRenamedNewName, oldName,
                newName, false));
    }

    /**
     * Directory changed notification
     * 
     * @param action int
     * @param path String
     */
    public final void notifyDirectoryChanged(int action, String path)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || hasDirectoryNameChange() == false)
            return;

        // Queue the change notification event

        queueNotification(new NotifyChangeEvent(NotifyChange.DirectoryName, action, path, true));
    }

    /**
     * Attributes changed notification
     * 
     * @param path String
     * @param isdir boolean
     */
    public final void notifyAttributesChanged(String path, boolean isdir)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || hasAttributeChange() == false)
            return;

        // Queue the change notification event

        queueNotification(new NotifyChangeEvent(NotifyChange.Attributes, NotifyChange.ActionModified, path, isdir));
    }

    /**
     * File size changed notification
     * 
     * @param path String
     */
    public final void notifyFileSizeChanged(String path)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || hasFileSizeChange() == false)
            return;

        // Send the change notification

        queueNotification(new NotifyChangeEvent(NotifyChange.Size, NotifyChange.ActionModified, path, false));
    }

    /**
     * Last write time changed notification
     * 
     * @param path String
     * @param isdir boolean
     */
    public final void notifyLastWriteTimeChanged(String path, boolean isdir)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || hasFileWriteTimeChange() == false)
            return;

        // Send the change notification

        queueNotification(new NotifyChangeEvent(NotifyChange.LastWrite, NotifyChange.ActionModified, path, isdir));
    }

    /**
     * Last access time changed notification
     * 
     * @param path String
     * @param isdir boolean
     */
    public final void notifyLastAccessTimeChanged(String path, boolean isdir)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || hasFileAccessTimeChange() == false)
            return;

        // Send the change notification

        queueNotification(new NotifyChangeEvent(NotifyChange.LastAccess, NotifyChange.ActionModified, path, isdir));
    }

    /**
     * Creation time changed notification
     * 
     * @param path String
     * @param isdir boolean
     */
    public final void notifyCreationTimeChanged(String path, boolean isdir)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || hasFileCreateTimeChange() == false)
            return;

        // Send the change notification

        queueNotification(new NotifyChangeEvent(NotifyChange.Creation, NotifyChange.ActionModified, path, isdir));
    }

    /**
     * Security descriptor changed notification
     * 
     * @param path String
     * @param isdir boolean
     */
    public final void notifySecurityDescriptorChanged(String path, boolean isdir)
    {

        // Check if file change notifications are enabled

        if (getGlobalNotifyMask() == 0 || hasSecurityDescriptorChange() == false)
            return;

        // Send the change notification

        queueNotification(new NotifyChangeEvent(NotifyChange.Security, NotifyChange.ActionModified, path, isdir));
    }

    /**
     * Enable debug output
     * 
     * @param ena boolean
     */
    public final void setDebug(boolean ena)
    {
        m_debug = ena;
    }

    /**
     * Shutdown the change notification processing thread
     */
    public final void shutdownRequest()
    {

        // Check if the processing thread is valid

        if (m_procThread != null)
        {

            // Set the shutdown flag

            m_shutdown = true;

            // Wakeup the processing thread

            synchronized (m_eventList)
            {
                m_eventList.notifyAll();
            }
        }
    }

    /**
     * Send buffered change notifications for a session
     * 
     * @param req NotifyRequest
     * @param evtList NotifyChangeEventList
     */
    public final void sendBufferedNotifications(NotifyRequest req, NotifyChangeEventList evtList)
    {

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("Send buffered notifications, req=" + req + ", evtList="
                    + (evtList != null ? "" + evtList.numberOfEvents() : "null"));

        // Initialize the notification request timeout

        long tmo = System.currentTimeMillis() + NotifyRequest.DefaultRequestTimeout;

        // Allocate the NT transaction packet to send the asynchronous notification

        NTTransPacket ntpkt = new NTTransPacket();

        // Build the change notification response SMB

        ntpkt.setParameterCount(18);
        ntpkt.resetBytePointerAlign();

        int pos = ntpkt.getPosition();
        ntpkt.setNTParameter(1, 0); // total data count
        ntpkt.setNTParameter(3, pos - 4); // offset to parameter block

        // Check if the notify enum status is set

        if (req.hasNotifyEnum())
        {

            // Set the parameter block length

            ntpkt.setNTParameter(0, 0); // total parameter block count
            ntpkt.setNTParameter(2, 0); // parameter block count for this packet
            ntpkt.setNTParameter(6, pos - 4); // data block offset
            ntpkt.setByteCount();

            ntpkt.setCommand(PacketType.NTTransact);

            ntpkt.setFlags(SMBSrvPacket.FLG_CANONICAL + SMBSrvPacket.FLG_CASELESS);
            ntpkt.setFlags2(SMBSrvPacket.FLG2_UNICODE + SMBSrvPacket.FLG2_LONGERRORCODE);

            // Set the notification request id to indicate that it has completed

            req.setCompleted(true, tmo);
            req.setNotifyEnum(false);

            // Set the response for the current notify request

            ntpkt.setMultiplexId(req.getMultiplexId());
            ntpkt.setTreeId(req.getTreeId());
            ntpkt.setUserId(req.getUserId());
            ntpkt.setProcessId(req.getProcessId());

            try
            {

                // Send the response to the current session

                if (req.getSession().sendAsynchResponseSMB(ntpkt, ntpkt.getLength()) == false)
                {

                    // Asynchronous request was queued, clone the request packet

                    ntpkt = new NTTransPacket(ntpkt);
                }
            }
            catch (Exception ex)
            {
            }
        }
        else if (evtList != null)
        {

            // Pack the change notification events

            for (int i = 0; i < evtList.numberOfEvents(); i++)
            {

                // Get the current event from the list

                NotifyChangeEvent evt = evtList.getEventAt(i);

                // Get the relative file name for the event

                String relName = FileName.makeRelativePath(req.getWatchPath(), evt.getFileName());
                if (relName == null)
                    relName = evt.getShortFileName();

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug())
                    logger.debug("  Notify evtPath=" + evt.getFileName() + ", reqPath=" + req.getWatchPath()
                            + ", relative=" + relName);

                // Pack the notification structure

                ntpkt.packInt(0); // offset to next structure
                ntpkt.packInt(evt.getAction()); // action
                ntpkt.packInt(relName.length() * 2); // file name length
                ntpkt.packString(relName, true, false);

                // Check if the event is a file/directory rename, if so then add the old
                // file/directory details

                if (evt.getAction() == NotifyChange.ActionRenamedNewName && evt.hasOldFileName())
                {

                    // Set the offset from the first structure to this structure

                    int newPos = DataPacker.longwordAlign(ntpkt.getPosition());
                    DataPacker.putIntelInt(newPos - pos, ntpkt.getBuffer(), pos);

                    // Get the old file name

                    relName = FileName.makeRelativePath(req.getWatchPath(), evt.getOldFileName());
                    if (relName == null)
                        relName = evt.getOldFileName();

                    // Add the old file/directory name details

                    ntpkt.packInt(0); // offset to next structure
                    ntpkt.packInt(NotifyChange.ActionRenamedOldName);
                    ntpkt.packInt(relName.length() * 2); // file name length
                    ntpkt.packString(relName, true, false);
                }

                // Calculate the parameter block length, longword align the buffer position

                int prmLen = ntpkt.getPosition() - pos;
                ntpkt.alignBytePointer();
                pos = (pos + 3) & 0xFFFFFFFC;

                // Set the parameter block length

                ntpkt.setNTParameter(0, prmLen); // total parameter block count
                ntpkt.setNTParameter(2, prmLen); // parameter block count for this packet
                ntpkt.setNTParameter(6, ntpkt.getPosition() - 4);
                // data block offset
                ntpkt.setByteCount();

                ntpkt.setCommand(PacketType.NTTransact);

                ntpkt.setFlags(SMBSrvPacket.FLG_CANONICAL + SMBSrvPacket.FLG_CASELESS);
                ntpkt.setFlags2(SMBSrvPacket.FLG2_UNICODE + SMBSrvPacket.FLG2_LONGERRORCODE);

                // Set the notification request id to indicate that it has completed

                req.setCompleted(true, tmo);

                // Set the response for the current notify request

                ntpkt.setMultiplexId(req.getMultiplexId());
                ntpkt.setTreeId(req.getTreeId());
                ntpkt.setUserId(req.getUserId());
                ntpkt.setProcessId(req.getProcessId());

                try
                {

                    // Send the response to the current session

                    if (req.getSession().sendAsynchResponseSMB(ntpkt, ntpkt.getLength()) == false)
                    {

                        // Asynchronous request was queued, clone the request packet

                        ntpkt = new NTTransPacket(ntpkt);
                    }
                }
                catch (Exception ex)
                {
                }
            }
        }

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("sendBufferedNotifications() done");
    }

    /**
     * Queue a change notification event for processing
     * 
     * @param evt NotifyChangeEvent
     */
    protected final void queueNotification(NotifyChangeEvent evt)
    {

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("Queue notification event=" + evt.toString());

        // Queue the notification event to the main notification handler thread

        synchronized (m_eventList)
        {

            // Add the event to the list

            m_eventList.addEvent(evt);

            // Notify the processing thread that there are events to process

            m_eventList.notifyAll();
        }
    }

    /**
     * Send change notifications to sessions with notification enabled that match the change event.
     * 
     * @param evt NotifyChangeEvent
     * @return int
     */
    protected final int sendChangeNotification(NotifyChangeEvent evt)
    {

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("sendChangeNotification event=" + evt);

        // Get a list of notification requests that match the type/path

        Vector<NotifyRequest> reqList = findMatchingRequests(evt.getFilter(), evt.getFileName(), evt.isDirectory());
        if (reqList == null || reqList.size() == 0)
            return 0;

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("  Found " + reqList.size() + " matching change listeners");

        // Initialize the notification request timeout

        long tmo = System.currentTimeMillis() + NotifyRequest.DefaultRequestTimeout;

        // Allocate the NT transaction packet to send the asynchronous notification

        NTTransPacket ntpkt = new NTTransPacket();

        // Send the notify response to each client in the list

        for (int i = 0; i < reqList.size(); i++)
        {

            // Get the current request

            NotifyRequest req = reqList.get(i);

            // Build the change notification response SMB

            ntpkt.setParameterCount(18);
            ntpkt.resetBytePointerAlign();

            int pos = ntpkt.getPosition();
            ntpkt.setNTParameter(1, 0); // total data count
            ntpkt.setNTParameter(3, pos - 4); // offset to parameter block

            // Get the relative file name for the event

            String relName = FileName.makeRelativePath(req.getWatchPath(), evt.getFileName());
            if (relName == null)
                relName = evt.getShortFileName();

            // DEBUG

            if (logger.isDebugEnabled() && hasDebug())
                logger.debug("  Notify evtPath=" + evt.getFileName() + ", reqPath=" + req.getWatchPath()
                        + ", relative=" + relName);

            // Pack the notification structure

            ntpkt.packInt(0); // offset to next structure
            ntpkt.packInt(evt.getAction()); // action
            ntpkt.packInt(relName.length() * 2); // file name length
            ntpkt.packString(relName, true, false);

            // Check if the event is a file/directory rename, if so then add the old file/directory
            // details

            if (evt.getAction() == NotifyChange.ActionRenamedNewName && evt.hasOldFileName())
            {

                // Set the offset from the first structure to this structure

                int newPos = DataPacker.longwordAlign(ntpkt.getPosition());
                DataPacker.putIntelInt(newPos - pos, ntpkt.getBuffer(), pos);

                // Get the old file name

                relName = FileName.makeRelativePath(req.getWatchPath(), evt.getOldFileName());
                if (relName == null)
                    relName = evt.getOldFileName();

                // Add the old file/directory name details

                ntpkt.packInt(0); // offset to next structure
                ntpkt.packInt(NotifyChange.ActionRenamedOldName);
                ntpkt.packInt(relName.length() * 2); // file name length
                ntpkt.packString(relName, true, false);
            }

            // Calculate the parameter block length, longword align the buffer position

            int prmLen = ntpkt.getPosition() - pos;
            ntpkt.alignBytePointer();
            pos = (pos + 3) & 0xFFFFFFFC;

            // Set the parameter block length

            ntpkt.setNTParameter(0, prmLen); // total parameter block count
            ntpkt.setNTParameter(2, prmLen); // parameter block count for this packet
            ntpkt.setNTParameter(6, ntpkt.getPosition() - 4);
            // data block offset
            ntpkt.setByteCount();

            ntpkt.setCommand(PacketType.NTTransact);

            ntpkt.setFlags(SMBSrvPacket.FLG_CANONICAL + SMBSrvPacket.FLG_CASELESS);
            ntpkt.setFlags2(SMBSrvPacket.FLG2_UNICODE + SMBSrvPacket.FLG2_LONGERRORCODE);

            // Check if the request is already complete

            if (req.isCompleted() == false)
            {

                // Set the notification request id to indicate that it has completed

                req.setCompleted(true, tmo);

                // Set the response for the current notify request

                ntpkt.setMultiplexId(req.getMultiplexId());
                ntpkt.setTreeId(req.getTreeId());
                ntpkt.setUserId(req.getUserId());
                ntpkt.setProcessId(req.getProcessId());

                // DEBUG

                // ntpkt.DumpPacket();

                try
                {

                    // Send the response to the current session

                    if (req.getSession().sendAsynchResponseSMB(ntpkt, ntpkt.getLength()) == false)
                    {

                        // Asynchronous request was queued, clone the request packet

                        ntpkt = new NTTransPacket(ntpkt);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            else
            {

                // Buffer the event so it can be sent when the client resets the notify request

                req.addEvent(evt);

                // DEBUG

                if (logger.isDebugEnabled() && req.getSession().hasDebug(SMBSrvSession.DBG_NOTIFY))
                    logger.debug("Buffered notify req=" + req + ", event=" + evt + ", sess="
                            + req.getSession().getSessionId());
            }

            // Reset the notification pending flag for the session

            req.getSession().setNotifyPending(false);

            // DEBUG

            if (logger.isDebugEnabled() && req.getSession().hasDebug(SMBSrvSession.DBG_NOTIFY))
                logger
                        .debug("Asynch notify req=" + req + ", event=" + evt + ", sess="
                                + req.getSession().getUniqueId());
        }

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("sendChangeNotification() done");

        // Return the count of matching requests

        return reqList.size();
    }

    /**
     * Find notify requests that match the type and path
     * 
     * @param typ int
     * @param path String
     * @param isdir boolean
     * @return Vector<NotifyRequest>
     */
    protected final synchronized Vector<NotifyRequest> findMatchingRequests(int typ, String path, boolean isdir)
    {

        // Create a vector to hold the matching requests

        Vector<NotifyRequest> reqList = new Vector<NotifyRequest>();

        // Normalise the path string

        String matchPath = path.toUpperCase();

        // Search for matching requests and remove them from the main request list

        int idx = 0;
        long curTime = System.currentTimeMillis();

        boolean removedReq = false;

        while (idx < m_notifyList.numberOfRequests())
        {

            // Get the current request

            NotifyRequest curReq = m_notifyList.getRequest(idx);

            // DEBUG

            if (logger.isDebugEnabled() && hasDebug())
                logger.debug("findMatchingRequests() req=" + curReq.toString());

            // Check if the request has expired

            if (curReq.hasExpired(curTime))
            {

                // Remove the request from the list

                m_notifyList.removeRequestAt(idx);

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug())
                {
                    logger.debug("Removed expired request req=" + curReq.toString());
                    if (curReq.getBufferedEventList() != null)
                    {
                        NotifyChangeEventList bufList = curReq.getBufferedEventList();
                        logger.debug("  Buffered events = " + bufList.numberOfEvents());
                        for (int b = 0; b < bufList.numberOfEvents(); b++)
                            logger.debug("    " + (b + 1) + ": " + bufList.getEventAt(b));
                    }
                }

                // Indicate that q request has been removed from the queue, the global filter mask
                // will need
                // to be recalculated

                removedReq = true;

                // Restart the loop

                continue;
            }

            // Check if the request matches the filter

            if (curReq.hasFilter(typ))
            {

                // DEBUG

                if (logger.isDebugEnabled() && hasDebug())
                    logger.debug("  hasFilter typ=" + typ + ", watchTree=" + curReq.hasWatchTree() + ", watchPath="
                            + curReq.getWatchPath() + ", matchPath=" + matchPath + ", isDir=" + isdir);

                // Check if the path matches or is a subdirectory and the whole tree is being
                // watched

                boolean wantReq = false;

                if (matchPath.length() == 0 && curReq.hasWatchTree())
                    wantReq = true;
                else if (curReq.hasWatchTree() == true && matchPath.startsWith(curReq.getWatchPath()) == true)
                    wantReq = true;
                else if (isdir == true && matchPath.compareTo(curReq.getWatchPath()) == 0)
                    wantReq = true;
                else if (isdir == false)
                {

                    // Strip the file name from the path and compare

                    String[] paths = FileName.splitPath(matchPath);

                    if (paths != null && paths[0] != null)
                    {

                        // Check if the directory part of the path is the directory being watched

                        if (curReq.getWatchPath().equalsIgnoreCase(paths[0]))
                            wantReq = true;
                    }
                }

                // Check if the request is required

                if (wantReq == true)
                {

                    // For all notify requests in the matching list we set the 'notify pending'
                    // state on the associated SMB
                    // session so that any socket writes on those sessions are synchronized until
                    // the change notification
                    // response has been sent.

                    curReq.getSession().setNotifyPending(true);

                    // Add the request to the matching list

                    reqList.add(curReq);

                    // DEBUG

                    if (logger.isDebugEnabled() && hasDebug())
                        logger.debug("  Added request to matching list");
                }
            }

            // Move to the next request in the list

            idx++;
        }

        // If requests were removed from the queue the global filter mask must be recalculated

        if (removedReq == true)
            m_globalNotifyMask = m_notifyList.getGlobalFilter();

        // Return the matching request list

        return reqList;
    }

    /**
     * Asynchronous change notification processing thread
     */
    public void run()
    {

        // Loop until shutdown

        while (m_shutdown == false)
        {

            // Wait for some events to process

            synchronized (m_eventList)
            {
                try
                {
                    m_eventList.wait();
                }
                catch (InterruptedException ex)
                {
                }
            }

            // Check if the shutdown flag has been set

            if (m_shutdown == true)
                break;

            // Loop until all pending events have been processed

            while (m_eventList.numberOfEvents() > 0)
            {

                // Remove the event at the head of the queue

                NotifyChangeEvent evt = null;

                synchronized (m_eventList)
                {
                    evt = m_eventList.removeEventAt(0);
                }

                // Check if the event is valid

                if (evt == null)
                    break;

                try
                {

                    // Send out change notifications to clients that match the filter/path

                    int cnt = sendChangeNotification(evt);

                    // DEBUG

                    if (logger.isDebugEnabled() && hasDebug())
                        logger.debug("Change notify event=" + evt.toString() + ", clients=" + cnt);
                }
                catch (Throwable ex)
                {
                    logger.error("NotifyChangeHandler thread", ex);
                }
            }
        }

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("NotifyChangeHandler thread exit");
    }
}
