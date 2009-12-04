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
package org.alfresco.repo.audit.hibernate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.audit.AuditState;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionalDao;
import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.mapping.Column;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Assumes mimetype and encoding sent to the content store (we are not saving this anywhere)
 * 
 * @author Andy Hind
 */
public class HibernateAuditDAO extends HibernateDaoSupport implements AuditDAO, TransactionalDao
{
    /**
     * Logging
     */
    private static Log s_logger = LogFactory.getLog(HibernateAuditDAO.class);

    private static final String QUERY_LAST_AUDIT_DATE = "audit.GetLatestAuditDate";

    private static final String QUERY_AUDIT_DATE = "audit.GetAuditDate";

    private static final String QUERY_LAST_AUDIT_CONFIG = "audit.GetLatestAuditConfig";

    private static final String QUERY_AUDIT_APP_SOURCE = "audit.GetAuditSourceByApplication";

    private static final String QUERY_AUDIT_METHOD_SOURCE = "audit.GetAuditSourceByApplicationServiceMethod";

    private static final String QUERY_AUDIT_APP_SOURCE_APP = "application";

    private static final String QUERY_AUDIT_APP_SOURCE_SER = "service";

    private static final String QUERY_AUDIT_APP_SOURCE_MET = "method";

    private static final String QUERY_AUDIT_TRAIL = "audit.GetAuditTrailForNode";

    private static final String QUERY_AUDIT_PROTOCOL = "protocol";

    private static final String QUERY_AUDIT_STORE_ID = "store_id";

    private static final String QUERY_AUDIT_NODE_ID = "node_id";

    private static final String QUERY_AUDIT_NODE_REF = "nodeRef";

    private static final String QUERY_AUDIT_DATE_PARAM = "date";

    /** a uuid identifying this unique instance */
    private String uuid;

    private ContentStore contentStore;

    private LocalSessionFactoryBean localSessionFactory;

    public HibernateAuditDAO()
    {
        super();
        this.uuid = GUID.generate();
    }

    public ContentStore getContentStore()
    {
        return contentStore;
    }

    public void setContentStore(ContentStore contentStore)
    {
        this.contentStore = contentStore;
    }

    public void setLocalSessionFactory(LocalSessionFactoryBean localSessionFactory)
    {
        this.localSessionFactory = localSessionFactory;
    }

    public void audit(AuditState auditInfo)
    {
        if (auditInfo.getUserIdentifier() == null)
        {
            auditInfo.setUserIdentifier(AuthenticationUtil.getSystemUserName());
        }
        if (AuthenticationUtil.getRunAsUser() == null)
        {
            AuthenticationUtil.setRunAsUserSystem();
            try
            {
                audit0(auditInfo);
            }
            finally
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        }
        else
        {
            audit0(auditInfo);
        }
    }

    private void audit0(AuditState auditInfo)
    {
        // Find/Build the configuraton entry
        AuditConfig auditConfig = getAuditConfig(auditInfo);

        // Find/Build any dates
        AuditDate auditDate = getAuditDate(auditInfo);

        // Find/Build the source
        AuditSource auditSource = getAuditSource(auditInfo);

        // Build the new audit fact information
        AuditFactImpl auditFact = new AuditFactImpl();
        auditFact.setAuditConfig(auditConfig);
        auditFact.setAuditDate(auditDate);
        auditFact.setAuditSource(auditSource);

        // Properties

        Serializable[] args = auditInfo.getMethodArguments();

        if (args != null)
        {
            switch (args.length)
            {
            default:
            case 5:
                auditFact.setArg5(getStringOrNull(args[4], getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "arg5")));
            case 4:
                auditFact.setArg4(getStringOrNull(args[3], getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "arg4")));
            case 3:
                auditFact.setArg3(getStringOrNull(args[2], getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "arg3")));
            case 2:
                auditFact.setArg2(getStringOrNull(args[1], getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "arg2")));
            case 1:
                auditFact.setArg1(getStringOrNull(args[0], getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "arg1")));
            case 0:
            }
        }

        auditFact.setClientInetAddress(getStringOrNull(auditInfo.getClientAddress(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "clientInetAddress")));
        auditFact.setDate(auditInfo.getDate());
        auditFact.setException(getStringOrNull((auditInfo.getThrowable() == null ? null : auditInfo.getThrowable().getMessage()), getColumnLength(
                "org.alfresco.repo.audit.hibernate.AuditFactImpl", "exception")));
        auditFact.setFail(auditInfo.isFail());
        auditFact.setFiltered(auditInfo.isFiltered());
        auditFact.setHostInetAddress(getStringOrNull(auditInfo.getHostAddress(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "hostInetAddress")));
        auditFact.setMessage(getStringOrNull(auditInfo.getMessage(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "message")));
        auditFact.setNodeUUID(getStringOrNull(auditInfo.getKeyGUID(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "nodeUUID")));
        auditFact.setPath(getStringOrNull(auditInfo.getPath(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "path")));
        auditFact.setReturnValue(getStringOrNull(auditInfo.getReturnObject(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "returnValue")));
        // auditFact.setSerialisedURL()
        auditFact.setSessionId(getStringOrNull(auditInfo.getSessionId(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "sessionId")));
        if (auditInfo.getKeyStore() != null)
        {
            auditFact.setStoreId(getStringOrNull(auditInfo.getKeyStore().getIdentifier(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "storeId")));
            auditFact.setStoreProtocol(getStringOrNull(auditInfo.getKeyStore().getProtocol(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "storeProtocol")));
        }
        auditFact.setTransactionId(getStringOrNull(auditInfo.getTxId(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "transactionId")));
        auditFact.setUserId(getStringOrNull(auditInfo.getUserIdentifier(), getColumnLength("org.alfresco.repo.audit.hibernate.AuditFactImpl", "userId")));

        // Save
        getSession().save(auditFact);

    }

    private int getColumnLength(String entityName, String propertyName)
    {
        int length = -1;
        Iterator it = localSessionFactory.getConfiguration().getClassMapping(entityName).getProperty(propertyName).getValue().getColumnIterator();
        if (it.hasNext())
        {
            Column col = (Column) it.next();
            length = col.getLength();
        }
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug(entityName + " " + propertyName + " is of length " + length);
        }
        return length;
    }

    private String getStringOrNull(Object o, int size)
    {
        if (o == null)
        {
            return null;
        }
        else
        {
            try
            {
                String answer = o.toString();
                if ((size > -1) && (answer.length() > size))
                {
                    answer = answer.substring(0, size);
                }
                return answer;
            }
            catch (Throwable t)
            {
                String answer = "Throwable in toString implementation for " + o.getClass() + " was " + t.getMessage();
                if ((size > -1) && (answer.length() > size))
                {
                    answer = answer.substring(0, size);
                }
                return answer;
            }
        }
    }

    private AuditSource getAuditSource(final AuditState auditInfo)
    {

        AuditSource auditSourceImpl;

        if ((auditInfo.getAuditService() != null)
                && (auditInfo.getAuditService().length() > 0) && (auditInfo.getAuditMethod() != null) && (auditInfo.getAuditMethod().length() > 0))
        {
            auditSourceImpl = queryApplicationSource(auditInfo.getAuditApplication(), auditInfo.getAuditService(), auditInfo.getAuditMethod());
            if (auditSourceImpl == null)
            {
                auditSourceImpl = new AuditSourceImpl();
                auditSourceImpl.setApplication(auditInfo.getAuditApplication());
                auditSourceImpl.setService(auditInfo.getAuditService());
                auditSourceImpl.setMethod(auditInfo.getAuditMethod());
                Long id = (Long) getSession().save(auditSourceImpl);
            }
        }
        else
        {
            auditSourceImpl = queryApplicationSource(auditInfo.getAuditApplication());
            if (auditSourceImpl == null)
            {
                auditSourceImpl = new AuditSourceImpl();
                auditSourceImpl.setApplication(auditInfo.getAuditApplication());
                Long id = (Long) getSession().save(auditSourceImpl);
            }
        }

        return auditSourceImpl;

    }

    private AuditDate getAuditDate(final AuditState auditInfo)
    {

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(auditInfo.getDate());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        Date required = cal.getTime();

        AuditDate auditDate;

        auditDate = queryLatestDate(required);
        if (auditDate == null)
        {
            auditDate = queryLatestDate();
            if (auditDate == null)
            {
                // The first entry ever so we just make it
                auditDate = new AuditDateImpl(auditInfo.getDate());
                Long id = (Long) getSession().save(auditDate);
            }
            else
            {
                if (required.compareTo(auditDate.getDate()) < 0)
                {
                    auditDate = new AuditDateImpl(required);
                    Long id = (Long) getSession().save(auditDate);
                }
                else if (required.compareTo(auditDate.getDate()) == 0)
                {
                    // no action
                }
                else
                {
                    while (!required.equals(auditDate.getDate()))
                    {
                        Date nextDate = Duration.add(auditDate.getDate(), new Duration("P1D"));
                        auditDate = new AuditDateImpl(nextDate);
                        Long id = (Long) getSession().save(auditDate);

                    }
                }
            }
        }
        else
        {
            // no action
        }
        return auditDate;

    }

    private AuditConfig getAuditConfig(final AuditState auditInfo)
    {

        AuditConfig auditConfig;
        auditConfig = queryLatestConfig(getSession());
        if (auditConfig == null)
        {
            auditConfig = createNewAuditConfigImpl(auditInfo);
        }
        else
        {
            InputStream current = null;
            InputStream last = null;
            try
            {
                current = new BufferedInputStream(auditInfo.getAuditConfiguration().getInputStream());
                ContentReader reader = contentStore.getReader(auditConfig.getConfigURL());
                reader.setMimetype(MimetypeMap.MIMETYPE_XML);
                reader.setEncoding("UTF-8");
                last = new BufferedInputStream(reader.getContentInputStream());
                int currentValue = -2;
                int lastValue = -2;
                try
                {
                    while ((currentValue != -1) && (lastValue != -1) && (currentValue == lastValue))
                    {
                        currentValue = current.read();
                        lastValue = last.read();

                    }
                }
                catch (IOException e)
                {
                    throw new AlfrescoRuntimeException("Failed to read and validate current audit configuration against the last", e);
                }
                if (currentValue != lastValue)
                {
                    // Files are different - require a new entry
                    auditConfig = createNewAuditConfigImpl(auditInfo);
                }
                else
                {
                    // No change
                }
            }
            finally
            {
                if (current != null)
                {
                    try
                    {
                        current.close();
                    }
                    catch (IOException e)
                    {
                        s_logger.warn(e);
                    }
                }

                if (last != null)
                {
                    try
                    {
                        last.close();
                    }
                    catch (IOException e)
                    {
                        s_logger.warn(e);
                    }
                }

            }
        }

        return auditConfig;

    }

    private AuditConfig createNewAuditConfigImpl(final AuditState auditInfo)
    {
        AuditConfigImpl auditConfig = new AuditConfigImpl();
        InputStream is = new BufferedInputStream(auditInfo.getAuditConfiguration().getInputStream());
        ContentWriter writer = contentStore.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding("UTF-8");
        writer.putContent(is);
        String contentUrl = writer.getContentUrl();
        auditConfig.setConfigURL(contentUrl);
        getSession().save(auditConfig);

        return auditConfig;
    }

    /**
     * Checks equality by type and uuid
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof HibernateAuditDAO))
        {
            return false;
        }
        HibernateAuditDAO that = (HibernateAuditDAO) obj;
        return this.uuid.equals(that.uuid);
    }

    /**
     * @see #uuid
     */
    public int hashCode()
    {
        return uuid.hashCode();
    }

    /**
     * Does this <tt>Session</tt> contain any changes which must be synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty()
    {
        // create a callback for the task
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                return session.isDirty();
            }
        };
        // execute the callback
        return ((Boolean) getHibernateTemplate().execute(callback)).booleanValue();
    }

    /**
     * Just flushes the session
     */
    public void flush()
    {
        getSession().flush();
    }

    /**
     * NO-OP
     */
    public void beforeCommit()
    {
    }

    static class SourceKey
    {
        String application;

        String service;

        String method;

        SourceKey(String application, String service, String method)
        {
            this.application = application;
            this.service = service;
            this.method = method;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(this instanceof SourceKey))
            {
                return false;
            }
            SourceKey other = (SourceKey) o;
            return EqualsHelper.nullSafeEquals(this.application, other.application)
                    && EqualsHelper.nullSafeEquals(this.service, other.service) && EqualsHelper.nullSafeEquals(this.method, other.method);
        }

        @Override
        public int hashCode()
        {
            int hash = application.hashCode();
            if (service != null)
            {
                hash = (hash * 37) + service.hashCode();
            }
            if (method != null)
            {
                hash = (hash * 37) + method.hashCode();
            }
            return hash;
        }
    }

    public List<AuditInfo> getAuditTrail(NodeRef nodeRef)
    {
        if (nodeRef == null)
        {
            return Collections.<AuditInfo> emptyList();
        }
        List<? extends AuditFact> internalTrail = queryAuditTrail(nodeRef);

        ArrayList<AuditInfo> answer = new ArrayList<AuditInfo>(internalTrail.size());
        for (AuditFact auditFact : internalTrail)
        {
            AuditInfo info = new AuditInfoImpl(auditFact);
            answer.add(info);
        }
        return answer;
    }

    /**
     * Helper method to get the latest audit config
     */
    public static AuditConfig queryLatestConfig(Session session)
    {
        Query query = session.getNamedQuery(QUERY_LAST_AUDIT_CONFIG);
        return (AuditConfig) query.uniqueResult();
    }

    /**
     * Helper method to get the latest audit date
     */
    public AuditDate queryLatestDate()
    {
        Query query = getSession().getNamedQuery(QUERY_LAST_AUDIT_DATE);
        return (AuditDate) query.uniqueResult();
    }

    /**
     * Helper method to get all the audit entries for a node.
     */
    @SuppressWarnings("unchecked")
    public List<AuditFact> queryAuditTrail(NodeRef nodeRef)
    {
        Query query = getSession().getNamedQuery(QUERY_AUDIT_TRAIL);
        query.setParameter(QUERY_AUDIT_PROTOCOL, nodeRef.getStoreRef().getProtocol());
        query.setParameter(QUERY_AUDIT_STORE_ID, nodeRef.getStoreRef().getIdentifier());
        query.setParameter(QUERY_AUDIT_NODE_ID, nodeRef.getId());
        query.setParameter(QUERY_AUDIT_NODE_REF, "%" + nodeRef.toString() + "%");
        return (List<AuditFact>) query.list();
    }

    /**
     * Helper method to get the application source
     * 
     * @param application
     * @return
     */
    public AuditSource queryApplicationSource(String application)
    {
        Query query = getSession().getNamedQuery(QUERY_AUDIT_APP_SOURCE);
        query.setParameter(QUERY_AUDIT_APP_SOURCE_APP, application);
        return (AuditSource) query.uniqueResult();
    }

    /**
     * Helper method to get the application source
     * 
     * @param application
     * @return
     */
    public AuditSource queryApplicationSource(String application, String service, String method)
    {
        Query query = getSession().getNamedQuery(HibernateAuditDAO.QUERY_AUDIT_METHOD_SOURCE);
        query.setParameter(QUERY_AUDIT_APP_SOURCE_APP, application);
        query.setParameter(QUERY_AUDIT_APP_SOURCE_SER, service);
        query.setParameter(QUERY_AUDIT_APP_SOURCE_MET, method);
        return (AuditSource) query.uniqueResult();
    }

    /**
     * Helper method to get the latest audit date
     */
    public AuditDate queryLatestDate(Date date)
    {
        Query query = getSession().getNamedQuery(HibernateAuditDAO.QUERY_AUDIT_DATE);
        query.setParameter(QUERY_AUDIT_DATE_PARAM, date);
        return (AuditDate) query.uniqueResult();
    }
    
    /*
     * V3.2 from here on.  Put all fixes to the older audit code before this point, please.
     */

    /**
     * Fallout implementation from new audit DAO
     * 
     * @throws UnsupportedOperationException always
     * @since 3.2
     */
    public Pair<Long, ContentData> getOrCreateAuditModel(URL url)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Fallout implementation from new audit DAO
     * 
     * @throws UnsupportedOperationException always
     * @since 3.2
     */
    public AuditApplicationInfo createAuditApplication(String application, Long modelId)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Fallout implementation from new audit DAO
     * 
     * @throws UnsupportedOperationException always
     * @since 3.2
     */
    public AuditApplicationInfo getAuditApplication(String applicationName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Fallout implementation from new audit DAO
     * 
     * @throws UnsupportedOperationException always
     * @since 3.2
     */
    public void updateAuditApplicationModel(Long id, Long modelId)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Fallout implementation from new audit DAO
     * 
     * @throws UnsupportedOperationException always
     * @since 3.2
     */
    public void updateAuditApplicationDisabledPaths(Long id, Set<String> disabledPaths)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Fallout implementation from new audit DAO
     * 
     * @throws UnsupportedOperationException always
     * @since 3.2
     */
    public void deleteAuditEntries(Long applicationId, Long from, Long to)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Fallout implementation from new audit DAO
     * 
     * @throws UnsupportedOperationException always
     * @since 3.2
     */
    public Long createAuditEntry(Long applicationId, long time, String username, Map<String, Serializable> values)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Fallout implementation from new audit DAO
     * 
     * @throws UnsupportedOperationException always
     * @since 3.2
     */
    public void findAuditEntries(AuditQueryCallback callback, AuditQueryParameters parameters, int maxResults)
    {
        throw new UnsupportedOperationException();
    }
}