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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class to encapsulate audit information supplied to the DAO layer.
 * 
 * Null entries should be stored.
 * 
 * @author Andy Hind
 */
public class AuditInfo
{
    private static Log    s_logger = LogFactory.getLog(AuditInfo.class);
    
    /**
     * The user identifier for the person who caused this audit entry
     */
    private String userIdentifier;

    /**
     * The date for this audit entry
     */
    private Date date;

    /** 
     * The transaction id in which this entry was made
     */
    private String txId;

    /**
     * The session is for this action
     */
    private String sessionId;

    /**
     * The store in which the action occured.
     */
    private StoreRef keyStore;

    /**
     * For a node ref, the node for the action.
     */
    private String keyGUID;

    /**
     * The path of the key 
     */
    private String keyPath;

    /**
     * The audit application
     * Internal uses the "System" key and will only audit method information.
     */
    private String auditApplication;

    /**
     * The service holding the audited method.
     */
    private String auditService;

    /**
     * The name of the audited method.
     */
    private String auditMethod;

    /**
     * Did this entry passa filter?
     * If false - all entries were being recorded.
     */
    private boolean filtered;

    /**
     * The audit configuration in use at the time.
     */
    private AuditConfiguration auditConfiguration;

    /**
     * The object returned by the audited method.
     */
    private Serializable returnObject;

    /**
     * The arguments to the audited method.
     */
    private Serializable[] methodArguments;

    /**
     * Any Exception thrown by the audited method.
     */
    private Throwable throwable;

    /**
     * Did the audited method throw an exception?
     */
    private boolean fail;

    /**
     * The host address for where the audit was generated.
     */
    private InetAddress hostAddress;
    
    private static InetAddress s_hostAddress;

    /**
     * The client address causing the audit
     */
    private InetAddress clientAddress;

    /**
     * The properties of the key node before the method execution.
     */
    private Map<QName, Serializable> keyPropertiesBefore;

    /**
     * The properties of the key node after the method execution.
     */
    private Map<QName, Serializable> keyPropertiesAfter;

    /**
     * For general auditing - the audit message.
     */
    private String message;

    static
    {
        try
        {
            s_hostAddress = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            s_logger.error(e);
            s_hostAddress = null;
        }
    }
    
    /**
     * Create the default audit info from the audit configuration.
     * 
     * @param auditConfiguration
     */
    public AuditInfo(AuditConfiguration auditConfiguration)
    {
        super();
        // Add default information
        userIdentifier = AuthenticationUtil.getCurrentUserName();
        date = new Date();
        txId = AlfrescoTransactionSupport.getTransactionId();
        sessionId = "Unavailable";
        hostAddress = s_hostAddress;
    }

    /**
     * Get the name of the audited application.
     * 
     * @return - the name of the audited application.
     */
    public String getAuditApplication()
    {
        return auditApplication;
    }

    /**
     * Set the name of the audited application.
     * 
     * @param auditApplication
     */
    public void setAuditApplication(String auditApplication)
    {
        this.auditApplication = auditApplication;
    }

    /**
     * Get the audit configuration.
     * 
     * @return - the audit configuration.
     */
    public AuditConfiguration getAuditConfiguration()
    {
        return auditConfiguration;
    }

    /**
     * Set the audit configuration.
     * 
     * @param auditConfiguration
     */
    public void setAuditConfiguration(AuditConfiguration auditConfiguration)
    {
        this.auditConfiguration = auditConfiguration;
    }

    /**
     * Get the name of the audited method - if it makes sense in the uadited context.
     * 
     * @return - the name of the audited method or null
     */
    public String getAuditMethod()
    {
        return auditMethod;
    }

    /**
     * Set the name of the audited method.
     * 
     * @param auditMethod
     */
    public void setAuditMethod(String auditMethod)
    {
        this.auditMethod = auditMethod;
    }

    /**
     * Get the audit service.
     * 
     * @return - the audit service.
     */
    public String getAuditService()
    {
        return auditService;
    }

    /**
     * Set the audit service (IOC)
     * 
     * @param auditService
     */
    public void setAuditService(String auditService)
    {
        this.auditService = auditService;
    }

    
  /**
   * Get the address o which the client application is running if available
   * @return - the address or null.
   */
    public InetAddress getClientAddress()
    {
        return clientAddress;
    }

    /**
     * Set the client address that casued the audit.
     * @param clientAddress
     */
    public void setClientAddress(InetAddress clientAddress)
    {
        this.clientAddress = clientAddress;
    }

    /**
     * Get the date for the audit entry/
     * 
     * @return - the date for the audit entry.
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Set the date for the audit entry
     * @param date
     */
    public void setDate(Date date)
    {
        this.date = date;
    }

    /**
     * Is this an audit of a failed method invocation?
     * @return - true if the audited methoed threw any kind of exception.
     */
    public boolean isFail()
    {
        return fail;
    }

    /**
     * Set that this is an audit of a failed method invoation.
     * 
     * @param fail
     */
    public void setFail(boolean fail)
    {
        this.fail = fail;
    }

    /**
     * Could some audit information have been filtered?
     * If true there may have been some unaudited operations of the same type.
     *  
     * @return - true if there were any filter definitions in the audit model; false otherwise.
     */
    public boolean isFiltered()
    {
        return filtered;
    }

    /**
     * Set if a filter was present for this audit entry
     * 
     * @param filtered
     */
    public void setFiltered(boolean filtered)
    {
        this.filtered = filtered;
    }

    /**
     * Get the host address where the repository is running.
     * @return - the host address.
     */
    public InetAddress getHostAddress()
    {
        return hostAddress;
    }

    /**
     * Set the host address where the repository is running.
     * @param hostAddress
     */
    public void setHostAddress(InetAddress hostAddress)
    {
        this.hostAddress = hostAddress;
    }

    /**
     * Get the GUID for the key node ref
     * @return - the guid part of the node ref
     */
    public String getKeyGUID()
    {
        return keyGUID;
    }

    /**
     * Set the GUID for the key node ref in the audited method invoation.
     * @param keyGUID
     */
    public void setKeyGUID(String keyGUID)
    {
        this.keyGUID = keyGUID;
    }

    /**
     * Get the properies of the key node after the method invoation. 
     * @return - the properties to be stored in the audit trail 
     */
    public Map<QName, Serializable> getKeyPropertiesAfter()
    {
        return keyPropertiesAfter;
    }

    /**
     * Set the preperties to be stored in the audit trail for the key node ref after the audited method has been invoked. 
     * @param keyPropertiesAfter
     */
    public void setKeyPropertiesAfter(Map<QName, Serializable> keyPropertiesAfter)
    {
        this.keyPropertiesAfter = keyPropertiesAfter;
    }
    
    /**
     * Get the properies of the key node before the method invoation. 
     * @return - the properties to be stored in the audit trail 
     */
    public Map<QName, Serializable> getKeyPropertiesBefore()
    {
        return keyPropertiesBefore;
    }

    /**
     * Set the preperties to be stored in the audit trail for the key node ref before the audited method has been invoked. 
     * @param keyPropertiesAfter
     */
    public void setKeyPropertiesBefore(Map<QName, Serializable> keyPropertiesBefore)
    {
        this.keyPropertiesBefore = keyPropertiesBefore;
    }

    public StoreRef getKeyStore()
    {
        return keyStore;
    }

    public void setKeyStore(StoreRef keyStore)
    {
        this.keyStore = keyStore;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Serializable[] getMethodArguments()
    {
        return methodArguments;
    }

    public void setMethodArguments(Serializable[] methodArguments)
    {
        this.methodArguments = methodArguments;
    }

    public String getPath()
    {
        return keyPath;
    }

    public void setPath(String keyPath)
    {
        this.keyPath = keyPath;
    }

    public Serializable getReturnObject()
    {
        return returnObject;
    }

    public void setReturnObject(Serializable returnObject)
    {
        this.returnObject = returnObject;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public Throwable getThrowable()
    {
        return throwable;
    }

    public void setThrowable(Throwable throwable)
    {
        this.throwable = throwable;
    }

    public String getTxId()
    {
        return txId;
    }

    public void setTxId(String txId)
    {
        this.txId = txId;
    }

    public String getUserIdentifier()
    {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier)
    {
        this.userIdentifier = userIdentifier;
    }

}
