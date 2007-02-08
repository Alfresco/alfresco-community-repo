/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.audit.hibernate;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * An Audit fact Rely on standard equals and hash code as they should all be unique.
 * 
 * @author Andy Hind
 */
public class AuditFactImpl implements AuditFact
{
    private long id;

    private AuditDate auditDate;

    private AuditConfig auditConfig;

    private AuditSource auditSource;

    private String userId;

    private Date date;

    private String transactionId;

    private String sessionId;

    private String storeProtocol;

    private String storeId;

    private String nodeUUID;

    private String path;

    private boolean filtered;

    private String returnValue;

    private String arg1;

    private String arg2;

    private String arg3;

    private String arg4;

    private String arg5;

    private boolean fail;

    private String serialisedURL;

    private String exception;

    private String hostInetAddress;

    private String clientInetAddress;

    private String message;

    public AuditFactImpl()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getArg1()
     */
    public String getArg1()
    {
        return arg1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setArg1(java.lang.String)
     */
    public void setArg1(String arg1)
    {
        this.arg1 = arg1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getArg2()
     */
    public String getArg2()
    {
        return arg2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setArg2(java.lang.String)
     */
    public void setArg2(String arg2)
    {
        this.arg2 = arg2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getArg3()
     */
    public String getArg3()
    {
        return arg3;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setArg3(java.lang.String)
     */
    public void setArg3(String arg3)
    {
        this.arg3 = arg3;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getArg4()
     */
    public String getArg4()
    {
        return arg4;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setArg4(java.lang.String)
     */
    public void setArg4(String arg4)
    {
        this.arg4 = arg4;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getArg5()
     */
    public String getArg5()
    {
        return arg5;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setArg5(java.lang.String)
     */
    public void setArg5(String arg5)
    {
        this.arg5 = arg5;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getAuditConfig()
     */
    public AuditConfig getAuditConfig()
    {
        return auditConfig;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setAuditConfig(org.alfresco.repo.audit.hibernate.AuditConfig)
     */
    public void setAuditConfig(AuditConfig auditConfig)
    {
        this.auditConfig = auditConfig;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getAuditDate()
     */
    public AuditDate getAuditDate()
    {
        return auditDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setAuditDate(org.alfresco.repo.audit.hibernate.AuditDate)
     */
    public void setAuditDate(AuditDate auditDate)
    {
        this.auditDate = auditDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getAuditSource()
     */
    public AuditSource getAuditSource()
    {
        return auditSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setAuditSource(org.alfresco.repo.audit.hibernate.AuditSource)
     */
    public void setAuditSource(AuditSource auditSource)
    {
        this.auditSource = auditSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getClientInetAddress()
     */
    public String getClientInetAddress()
    {
        return clientInetAddress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setClientInetAddress(java.net.InetAddress)
     */
    public void setClientInetAddress(String clientInetAddress)
    {
        this.clientInetAddress = clientInetAddress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getDate()
     */
    public Date getDate()
    {
        return date;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setDate(java.util.Date)
     */
    public void setDate(Date date)
    {
        this.date = date;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getException()
     */
    public String getException()
    {
        return exception;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setException(java.lang.String)
     */
    public void setException(String exception)
    {
        this.exception = exception;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#isFail()
     */
    public boolean isFail()
    {
        return fail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setFail(boolean)
     */
    public void setFail(boolean fail)
    {
        this.fail = fail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#isFiltered()
     */
    public boolean isFiltered()
    {
        return filtered;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setFiltered(boolean)
     */
    public void setFiltered(boolean filtered)
    {
        this.filtered = filtered;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getHostInetAddress()
     */
    public String getHostInetAddress()
    {
        return hostInetAddress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setHostInetAddress(java.net.InetAddress)
     */
    public void setHostInetAddress(String hostInetAddress)
    {
        this.hostInetAddress = hostInetAddress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getId()
     */
    public long getId()
    {
        return id;
    }

    protected void setId(long id)
    {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getMessage()
     */
    public String getMessage()
    {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setMessage(java.lang.String)
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getNodeGUID()
     */
    public String getNodeUUID()
    {
        return nodeUUID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setNodeGUID(java.lang.String)
     */
    public void setNodeUUID(String nodeUUID)
    {
        this.nodeUUID = nodeUUID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getPath()
     */
    public String getPath()
    {
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setPath(java.lang.String)
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getReturnValue()
     */
    public String getReturnValue()
    {
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setReturnValue(java.lang.String)
     */
    public void setReturnValue(String returnValue)
    {
        this.returnValue = returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getSerialisedURL()
     */
    public String getSerialisedURL()
    {
        return serialisedURL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setSerialisedURL(java.lang.String)
     */
    public void setSerialisedURL(String serialisedURL)
    {
        this.serialisedURL = serialisedURL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getSessionId()
     */
    public String getSessionId()
    {
        return sessionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setSessionId(java.lang.String)
     */
    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getStoreId()
     */
    public String getStoreId()
    {
        return storeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setStoreId(java.lang.String)
     */
    public void setStoreId(String storeId)
    {
        this.storeId = storeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getStoreProtocol()
     */
    public String getStoreProtocol()
    {
        return storeProtocol;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setStoreProtocol(java.lang.String)
     */
    public void setStoreProtocol(String storeProtocol)
    {
        this.storeProtocol = storeProtocol;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getTransactionId()
     */
    public String getTransactionId()
    {
        return transactionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setTransactionId(java.lang.String)
     */
    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#getUserId()
     */
    public String getUserId()
    {
        return userId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditFact#setUserId(java.lang.String)
     */
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    /**
     * Helper method to get all the audit entries for a node.
     */
    @SuppressWarnings("unchecked")
    public static List<AuditFact> getAuditTrail(Session session, NodeRef nodeRef)
    {
        Query query = session.getNamedQuery(HibernateAuditDAO.QUERY_AUDIT_TRAIL);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_PROTOCOL, nodeRef.getStoreRef().getProtocol());
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_STORE_ID, nodeRef.getStoreRef().getIdentifier());
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_NODE_ID, nodeRef.getId());
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_NODE_REF, "%"+nodeRef.toString()+"%");
        return (List<AuditFact>) query.list();
    }
}
