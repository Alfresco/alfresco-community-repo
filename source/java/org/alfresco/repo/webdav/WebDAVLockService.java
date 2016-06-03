
package org.alfresco.repo.webdav;

import javax.servlet.http.HttpSession;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * WebDAVLockService is used to manage file locks for WebDAV and Sharepoint protocol. It ensures a lock never persists
 * for more than 24 hours, and also ensures locks are timed out on session timeout.
 * 
 * @author Pavel.Yurkevich
 * @author Matt Ward
 */
public interface WebDAVLockService
{
    static final String BEAN_NAME = "webDAVLockService";

    @SuppressWarnings("unchecked")
    void sessionDestroyed();

    /**
     * Shared method for webdav/vti protocols to lock node. If node is locked for more than 24 hours it is automatically added
     * to the current session locked resources list.
     * 
     * @param nodeRef the node to lock
     * @param userName the current user's user name
     * @param timeout the number of seconds before the locks expires
     */
    void lock(NodeRef nodeRef, String userName, int timeout);

    void lock(NodeRef nodeRef, LockInfo lockInfo);
    
    /**
     * Shared method for webdav/vti to unlock node. Unlocked node is automatically removed from
     * current sessions's locked resources list.
     * 
     * @param nodeRef the node to lock
     */
    void unlock(NodeRef nodeRef);

    /**
     * Gets the lock info for the node reference relative to the current user.
     * 
     * @see LockService#getLockStatus(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     * 
     * @param nodeRef    the node reference
     * @return           the lock status
     */
    LockInfo getLockInfo(NodeRef nodeRef);
    
    /**
     * Determines if the node is locked AND it's not a WRITE_LOCK for the current user.<p>
     *
     * @return true if the node is locked AND it's not a WRITE_LOCK for the current user
     */
    public boolean isLockedAndReadOnly(NodeRef nodeRef);
    
    /**
     * Caches current session in a thread local variable.
     * 
     * @param session HttpSession
     */
    void setCurrentSession(HttpSession session);
}