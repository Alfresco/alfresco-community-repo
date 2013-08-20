/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;

/**
 * Implements the WebDAV UNLOCK method
 * 
 * @author gavinc
 */
public class UnlockMethod extends WebDAVMethod
{
    private String m_strLockToken = null;

    /**
     * Default constructor
     */
    public UnlockMethod()
    {
    }

    /**
     * Return the lock token of an existing lock
     * 
     * @return String
     */
    protected final String getLockToken()
    {
        return m_strLockToken;
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // Get the lock token, if any
        String strLockTokenHeader = m_request.getHeader(WebDAV.HEADER_LOCK_TOKEN);

        // DEBUG
        if (logger.isDebugEnabled())
            logger.debug("Parsing Lock-Token header: " + strLockTokenHeader);

        // Validate the lock token
        if (strLockTokenHeader != null)
        {
            if (!(strLockTokenHeader.startsWith("<") && strLockTokenHeader.endsWith(">")))
            {
                // ALF-13904: Header isn't correctly enclosed in < and > characters. Try correcting this
                // to allow for Windows 7 + OpenOffice.org bug.
                strLockTokenHeader = "<" + strLockTokenHeader + ">";
            }
            if (strLockTokenHeader.startsWith("<" + WebDAV.OPAQUE_LOCK_TOKEN) && strLockTokenHeader.endsWith(">"))
            {
                try
                {
                    m_strLockToken = strLockTokenHeader.substring(
                            WebDAV.OPAQUE_LOCK_TOKEN.length() + 1,
                            strLockTokenHeader.length() - 1);
                }
                catch (IndexOutOfBoundsException e)
                {
                    logger.warn("Failed to parse If header: " + strLockTokenHeader);
	            }
	        }
        }
        // If there is no token this is a bad request so send an error back
        if (m_strLockToken == null)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // Nothing to do in this method
    }

    /**
     * Execute the request
     * 
     * @exception WebDAVServerException
     */
    protected void executeImpl() throws WebDAVServerException
    {
        RuleService ruleService = getServiceRegistry().getRuleService();
        try
        {
            // Temporarily disable update rules.
            ruleService.disableRuleType(RuleType.UPDATE);
            attemptUnlock();
        }
        finally
        {
            // Re-instate update rules.
            ruleService.enableRuleType(RuleType.UPDATE);
        }
    }

    /**
     * The main unlock implementation.
     * 
     * @throws WebDAVServerException
     */
    protected void attemptUnlock() throws WebDAVServerException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Unlock node; path=" + getPath() + ", token=" + getLockToken());
        }

        FileInfo lockNodeInfo = null;
        try
        {
            lockNodeInfo = getNodeForPath(getRootNodeRef(), getPath());
        }
        catch (FileNotFoundException e)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_NOT_FOUND);
        }

        // Parse the lock token
        String[] lockInfoFromRequest = WebDAV.parseLockToken(getLockToken());
        if (lockInfoFromRequest == null)
        {
            // Bad lock token
            throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
        }

        NodeRef nodeRef = lockNodeInfo.getNodeRef();        
        LockInfo lockInfo = getDAVLockService().getLockInfo(nodeRef);
        
        if (lockInfo == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unlock token=" + getLockToken() + " Not locked - no info in lock store.");
            }
            // Node is not locked
            throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
        }
        
        
        if (!lockInfo.isLocked())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unlock token=" + getLockToken() + " Not locked");
            }
            // Node is not locked
            throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
        }
        else if (lockInfo.isExpired())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unlock token=" + getLockToken() + " Lock expired");
            }
            // Return a success status
            m_response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            removeNoContentAspect(nodeRef);
        }
        else if (lockInfo.isExclusive())
        {
            String currentUser = getAuthenticationService().getCurrentUserName();
            if (currentUser.equals(lockInfo.getOwner()))
            {
            	try
                {
                    getDAVLockService().unlock(nodeRef);
                }
                catch (UnableToReleaseLockException e)
                {
                    throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED, e);
                }
    
                // Indicate that the unlock was successful
                m_response.setStatus(HttpServletResponse.SC_NO_CONTENT);            
                removeNoContentAspect(nodeRef);
    
                if (logger.isDebugEnabled())
                {
                    logger.debug("Unlock token=" + getLockToken() + " Successful");
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Unlock token=" + getLockToken() + " Not lock owner");
                }
                // Node is not locked
                throw new WebDAVServerException(HttpServletResponse.SC_PRECONDITION_FAILED);
            }
        }
        else if (lockInfo.isShared())
        {
            Set<String> sharedLocks = lockInfo.getSharedLockTokens();
            if (sharedLocks.contains(m_strLockToken))
            {
                sharedLocks.remove(m_strLockToken);

                // Indicate that the unlock was successful
                m_response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                removeNoContentAspect(nodeRef);

                // DEBUG
                if (logger.isDebugEnabled())
                {
                    logger.debug("Unlock token=" + getLockToken() + " Successful");
                }
            }
        }
        else
        {
            throw new IllegalStateException("Invalid LockInfo state: " + lockInfo);
        }
    }

    // This method removes a new zero byte node that has been locked, where the
    // PUT has not taken place but the client has issued an UNLOCK. The Timer
    // started in the LOCK will delete the node, but this is faster.
    // I have seen it with MS Office 2003 Excel. Almost impossible to reproduce.
    // Think Excel responds to a 'kill' request between the LOCK and PUT requests
    // and tries to tidy down as it exits.
    private void removeNoContentAspect(NodeRef nodeRef)
    {
        if (getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT))
        {
            getNodeService().removeAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT);
        }
        if (getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_WEBDAV_NO_CONTENT))
        {
            getNodeService().removeAspect(nodeRef, ContentModel.ASPECT_WEBDAV_NO_CONTENT);
            getNodeService().deleteNode(nodeRef);

            if (logger.isDebugEnabled())
            {
                logger.debug("Unlock Timer DISABLE and DELETE " + getPath());
            }
        }
    }
}
