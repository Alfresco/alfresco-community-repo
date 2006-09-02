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
import org.apache.log4j.Logger;

/**
 * A class to encapsulate audit information supplied to the DAO layer.
 * 
 * Null entries should be stored.
 * 
 * @author Andy Hind
 */
public class AuditInfo
{
    private static Logger s_logger = Logger.getLogger(AuditInfo.class);
    
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

    public String getAuditApplication()
    {
        return auditApplication;
    }

    public void setAuditApplication(String auditApplication)
    {
        this.auditApplication = auditApplication;
    }

    public AuditConfiguration getAuditConfiguration()
    {
        return auditConfiguration;
    }

    public void setAuditConfiguration(AuditConfiguration auditConfiguration)
    {
        this.auditConfiguration = auditConfiguration;
    }

    public String getAuditMethod()
    {
        return auditMethod;
    }

    public void setAuditMethod(String auditMethod)
    {
        this.auditMethod = auditMethod;
    }

    public String getAuditService()
    {
        return auditService;
    }

    public void setAuditService(String auditService)
    {
        this.auditService = auditService;
    }

    public InetAddress getClientAddress()
    {
        return clientAddress;
    }

    public void setClientAddress(InetAddress clientAddress)
    {
        this.clientAddress = clientAddress;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public boolean isFail()
    {
        return fail;
    }

    public void setFail(boolean fail)
    {
        this.fail = fail;
    }

    public boolean isFiltered()
    {
        return filtered;
    }

    public void setFiltered(boolean filtered)
    {
        this.filtered = filtered;
    }

    public InetAddress getHostAddress()
    {
        return hostAddress;
    }

    public void setHostAddress(InetAddress hostAddress)
    {
        this.hostAddress = hostAddress;
    }

    public String getKeyGUID()
    {
        return keyGUID;
    }

    public void setKeyGUID(String keyGUID)
    {
        this.keyGUID = keyGUID;
    }

    public Map<QName, Serializable> getKeyPropertiesAfter()
    {
        return keyPropertiesAfter;
    }

    public void setKeyPropertiesAfter(Map<QName, Serializable> keyPropertiesAfter)
    {
        this.keyPropertiesAfter = keyPropertiesAfter;
    }

    public Map<QName, Serializable> getKeyPropertiesBefore()
    {
        return keyPropertiesBefore;
    }

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
