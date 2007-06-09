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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.audit.AuditConfiguration;
import org.alfresco.repo.audit.AuditDAO;
import org.alfresco.repo.audit.AuditState;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionalDao;
import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Assumes mimetype and encoding sent to the content store (we are not saving this anywhere)
 * 
 * @author Andy Hind
 */
public class HibernateAuditDAO extends HibernateDaoSupport implements AuditDAO, TransactionalDao
{
    public static final String QUERY_LAST_AUDIT_DATE = "audit.GetLatestAuditDate";

    public static final String QUERY_LAST_AUDIT_CONFIG = "audit.GetLatestAuditConfig";

    public static final String QUERY_AUDIT_APP_SOURCE = "audit.GetAuditSourceByApplication";

    public static final String QUERY_AUDIT_METHOD_SOURCE = "audit.GetAuditSourceByApplicationServiceMethod";

    public static final String QUERY_AUDIT_APP_SOURCE_APP = "application";

    public static final String QUERY_AUDIT_APP_SOURCE_SER = "service";

    public static final String QUERY_AUDIT_APP_SOURCE_MET = "method";

    public static final String QUERY_AUDIT_TRAIL = "audit.GetAuditTrailForNode";
    
    public static final String QUERY_AUDIT_PROTOCOL = "protocol";
    
    public static final String QUERY_AUDIT_STORE_ID = "store_id";
    
    public static final String QUERY_AUDIT_NODE_ID = "node_id";
    
    public static final String QUERY_AUDIT_NODE_REF = "nodeRef";

    /** a uuid identifying this unique instance */
    private String uuid;

    private ContentStore contentStore;

    private ThreadLocal<AuditConfiguration> auditConfiguration = new ThreadLocal<AuditConfiguration>();

    private ThreadLocal<Long> auditConfigImplId = new ThreadLocal<Long>();

    private ThreadLocal<Long> auditDateImplId = new ThreadLocal<Long>();
    
    private ThreadLocal<HashMap<SourceKey, Long>> sourceIds = new ThreadLocal<HashMap<SourceKey, Long>>();

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

    public void audit(AuditState auditInfo)
    {
        if(auditInfo.getUserIdentifier() == null)
        {
            auditInfo.setUserIdentifier(AuthenticationUtil.getSystemUserName());
        }
        if(AuthenticationUtil.getCurrentUserName() == null)
        {
            AuthenticationUtil.setSystemUserAsCurrentUser();
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
                auditFact.setArg5(getStringOrNull(args[4]));
            case 4:
                auditFact.setArg4(getStringOrNull(args[3]));
            case 3:
                auditFact.setArg3(getStringOrNull(args[2]));
            case 2:
                auditFact.setArg2(getStringOrNull(args[1]));
            case 1:
                auditFact.setArg1(getStringOrNull(args[0]));
            case 0:
            }
        }

        auditFact.setClientInetAddress(auditInfo.getClientAddress() == null ? null : auditInfo.getClientAddress()
                .toString());
        auditFact.setDate(auditInfo.getDate());
        auditFact.setException(auditInfo.getThrowable() == null ? null : auditInfo.getThrowable().getMessage());
        auditFact.setFail(auditInfo.isFail());
        auditFact.setFiltered(auditInfo.isFiltered());
        auditFact.setHostInetAddress(auditInfo.getHostAddress() == null ? null : auditInfo.getHostAddress().toString());
        auditFact.setMessage(auditInfo.getMessage());
        auditFact.setNodeUUID(auditInfo.getKeyGUID());
        auditFact.setPath(auditInfo.getPath());
        auditFact.setReturnValue(auditInfo.getReturnObject() == null ? null : auditInfo.getReturnObject().toString());
        // auditFact.setSerialisedURL()
        auditFact.setSessionId(auditInfo.getSessionId());
        if (auditInfo.getKeyStore() != null)
        {
            auditFact.setStoreId(auditInfo.getKeyStore().getIdentifier());
            auditFact.setStoreProtocol(auditInfo.getKeyStore().getProtocol());
        }
        auditFact.setTransactionId(auditInfo.getTxId());
        auditFact.setUserId(auditInfo.getUserIdentifier());

        // Save
        getSession().save(auditFact);

    }

    private String getStringOrNull(Object o)
    {
        if (o == null)
        {
            return null;
        }
        else
        {
            try
            {
               return o.toString();
            }
            catch(Throwable t)
            {
                return "Throwable in toString implementation for "+o.getClass() + " was "+t.getMessage();
            }
        }
    }

    private AuditSource getAuditSource(AuditState auditInfo)
    {
        AuditSource auditSourceImpl;
        
        SourceKey sourceKey = new SourceKey(auditInfo.getAuditApplication(), auditInfo.getAuditService(), auditInfo.getAuditMethod());
        if(sourceIds.get() == null)
        {
            sourceIds.set(new HashMap<SourceKey, Long>());
        }
        Long id = sourceIds.get().get(sourceKey);
        if(id != null)
        {
            auditSourceImpl = (AuditSource) getSession().get(AuditSourceImpl.class, id.longValue());
            if(auditSourceImpl != null)
            {
                return auditSourceImpl;
            }
        }
      
        if ((auditInfo.getAuditService() != null)
                && (auditInfo.getAuditService().length() > 0) && (auditInfo.getAuditMethod() != null)
                && (auditInfo.getAuditMethod().length() > 0))
        {
            auditSourceImpl = AuditSourceImpl.getApplicationSource(getSession(), auditInfo.getAuditApplication(),
                    auditInfo.getAuditService(), auditInfo.getAuditMethod());
            if (auditSourceImpl == null)
            {
                auditSourceImpl = new AuditSourceImpl();
                auditSourceImpl.setApplication(auditInfo.getAuditApplication());
                auditSourceImpl.setService(auditInfo.getAuditService());
                auditSourceImpl.setMethod(auditInfo.getAuditMethod());
                getSession().save(auditSourceImpl);
            }
        }
        else
        {
            auditSourceImpl = AuditSourceImpl.getApplicationSource(getSession(), auditInfo.getAuditApplication());
            if (auditSourceImpl == null)
            {
                auditSourceImpl = new AuditSourceImpl();
                auditSourceImpl.setApplication(auditInfo.getAuditApplication());
                getSession().save(auditSourceImpl);
            }
        }
        sourceIds.get().put(sourceKey, Long.valueOf(auditSourceImpl.getId()));
        return auditSourceImpl;
    }

    private AuditDate getAuditDate(AuditState auditInfo)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(auditInfo.getDate());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        Date required = cal.getTime();

        AuditDate auditDate;
        if (auditDateImplId.get() == null)
        {
            auditDate = AuditDateImpl.getLatestDate(getSession());
            if (auditDate == null)
            {
                // The first entry ever so we just make it
                auditDate = new AuditDateImpl(auditInfo.getDate());
                getSession().save(auditDate);
            }
            auditDateImplId.set(Long.valueOf(auditDate.getId()));
        }
        else
        {
            auditDate = (AuditDate) getSession().get(AuditDateImpl.class, auditDateImplId.get().longValue());
            if ((auditDate == null) || (!required.equals(auditDate.getDate())))
            {
                auditDate = AuditDateImpl.getLatestDate(getSession());
                if (auditDate == null)
                {
                    // The first entry ever so we just make it
                    auditDate = new AuditDateImpl(auditInfo.getDate());
                    getSession().save(auditDate);
                }
                auditDateImplId.set(Long.valueOf(auditDate.getId()));
            }
        }
        while (!required.equals(auditDate.getDate()))
        {
            Date nextDate = Duration.add(auditDate.getDate(), new Duration("P1D"));
            auditDate = new AuditDateImpl(nextDate);
            getSession().save(auditDate);
            auditDateImplId.set(Long.valueOf(auditDate.getId()));
        }
        return auditDate;
    }

    private AuditConfig getAuditConfig(AuditState auditInfo)
    {
        AuditConfig auditConfig;
        if ((auditConfiguration.get() == null) || (auditConfiguration.get() != auditInfo.getAuditConfiguration()))
        {
            auditConfig = AuditConfigImpl.getLatestConfig(getSession());
            if (auditConfig == null)
            {
                auditConfig = createNewAuditConfigImpl(auditInfo);
            }
            else
            {
                InputStream current = new BufferedInputStream(auditInfo.getAuditConfiguration().getInputStream());
                ContentReader reader = contentStore.getReader(auditConfig.getConfigURL());
                reader.setMimetype(MimetypeMap.MIMETYPE_XML);
                reader.setEncoding("UTF-8");
                InputStream last = new BufferedInputStream(reader.getContentInputStream());
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
                    throw new AlfrescoRuntimeException(
                            "Failed to read and validate current audit configuration against the last", e);
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
            auditConfigImplId.set(Long.valueOf(auditConfig.getId()));
            auditConfiguration.set(auditInfo.getAuditConfiguration());
        }
        else
        {
            auditConfig = (AuditConfig) getSession()
                    .get(AuditConfigImpl.class, auditConfigImplId.get().longValue());
            if (auditConfig == null)
            {
                auditConfig = createNewAuditConfigImpl(auditInfo);
            }
        }
        return auditConfig;
    }

    private AuditConfigImpl createNewAuditConfigImpl(AuditState auditInfo)
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
                    && EqualsHelper.nullSafeEquals(this.service, other.service)
                    && EqualsHelper.nullSafeEquals(this.method, other.method);
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
        if(nodeRef == null)
        {
            return Collections.<AuditInfo>emptyList();
        }
        List<? extends AuditFact> internalTrail = AuditFactImpl.getAuditTrail(getSession(), nodeRef);
        
        ArrayList<AuditInfo> answer = new ArrayList<AuditInfo>(internalTrail.size());
        for(AuditFact auditFact : internalTrail)
        {
            AuditInfo info = new AuditInfoImpl(auditFact);
            answer.add(info);
        }
        return answer;
    }
    
    
}
