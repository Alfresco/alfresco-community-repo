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
package org.alfresco.repo.audit.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

public class AuditInfoImpl implements AuditInfo
{
    private String auditApplication;
    
    private String auditMethod;
    
    private String auditService;
    
    private String clientAddress;
    
    private Date date;
    
    private boolean fail;
    
    private boolean filtered;
    
    private String hostAddress;
    
    private String keyGUID;
    
    private Map<QName, Serializable> keyPropertiesAfter;

    private Map<QName, Serializable> keyPropertiesBefore;

    private StoreRef keyStore;
 
    private String message;
    
    private Serializable[] methodArguments;

    private String[] methodArgumentsAsStrings;

    private String path;
   
    private Serializable returnObject;
 
    private String returnObjectAsString;
  
    private String sessionId;
   
    private Throwable throwable;
   
    private String throwableAsString;

    private String txId;

    private String userIdentifier;
  
    public AuditInfoImpl(AuditFact auditFact)
    {
        super();
        this.auditApplication = auditFact.getAuditSource().getApplication();
        this.auditMethod = auditFact.getAuditSource().getMethod();
        this.auditService = auditFact.getAuditSource().getService();
        this.clientAddress = auditFact.getClientInetAddress();
        this.date = auditFact.getDate();
        this.fail = auditFact.isFail();
        this.filtered = auditFact.isFiltered();
        this.hostAddress= auditFact.getHostInetAddress();
        this.keyGUID = auditFact.getNodeUUID();
        this.keyPropertiesAfter = null;
        this.keyPropertiesBefore = null;
        if((auditFact.getStoreProtocol() != null) && (auditFact.getStoreId() != null))
        {
            this.keyStore = new StoreRef(auditFact.getStoreProtocol(), auditFact.getStoreId());
        }
        else
        {
            this.keyStore = null;
        }
        this.message = auditFact.getMessage();
        this.methodArguments = null;
        this.methodArgumentsAsStrings = new String[5];
        this.methodArgumentsAsStrings[0] = auditFact.getArg1();
        this.methodArgumentsAsStrings[1] = auditFact.getArg2();
        this.methodArgumentsAsStrings[2] = auditFact.getArg3();
        this.methodArgumentsAsStrings[3] = auditFact.getArg4();
        this.methodArgumentsAsStrings[4] = auditFact.getArg5();
        this.path = auditFact.getPath();
        this.returnObject = null;
        this.returnObjectAsString = auditFact.getReturnValue();
        this.sessionId = auditFact.getSessionId();
        this.throwable = null;
        this.throwableAsString = auditFact.getException();
        this.txId = auditFact.getTransactionId();
        this.userIdentifier = auditFact.getUserId();
    }

    public String getAuditApplication()
    {
      return auditApplication;
    }

    public String getAuditMethod()
    {
        return auditMethod;
    }

    public String getAuditService()
    {
        return auditService;
    }

    public String getClientAddress()
    {
        return clientAddress;
    }

    public Date getDate()
    {
        return date;
    }

    public boolean isFail()
    {
       return fail;
    }

    public boolean isFiltered()
    {
       return filtered;
    }

    public String getHostAddress()
    {
        return hostAddress;
    }

    public String getKeyGUID()
    {
        return keyGUID;
    }

    public Map<QName, Serializable> getKeyPropertiesAfter()
    {
        return keyPropertiesAfter;
    }

    public Map<QName, Serializable> getKeyPropertiesBefore()
    {
        return keyPropertiesBefore;
    }

    public StoreRef getKeyStore()
    {
        return keyStore;
    }

    public String getMessage()
    {
       return message;
    }

    public Serializable[] getMethodArguments()
    {
        return methodArguments;
    }

    public String[] getMethodArgumentsAsStrings()
    {
        return methodArgumentsAsStrings;
    }

    public String getPath()
    {
        return path;
    }

    public Serializable getReturnObject()
    {
       return returnObject;
    }

    public String getReturnObjectAsString()
    {
       return returnObjectAsString;
    }

    public String getSessionId()
    {
      return sessionId;
    }

    public Throwable getThrowable()
    {
        return throwable;
    }

    public String getThrowableAsString()
    {
       return throwableAsString;
    }

    public String getTxId()
    {
        return txId;
    }

    public String getUserIdentifier()
    {
       return userIdentifier;
    }

    
}
