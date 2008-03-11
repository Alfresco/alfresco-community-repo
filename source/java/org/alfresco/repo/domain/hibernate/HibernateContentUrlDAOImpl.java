package org.alfresco.repo.domain.hibernate;

import java.util.Set;

import org.alfresco.repo.domain.ContentUrl;
import org.alfresco.repo.domain.ContentUrlDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.type.TypeFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate-specific implementation of the DAO layer for <b>Content URLs</b>.
 * 
 * @author Derek Hulley
 * @since 2.0
 */
public class HibernateContentUrlDAOImpl extends HibernateDaoSupport implements ContentUrlDAO
{
    private static final String QUERY_GET_ALL = "contentUrl.GetAll";
    private static final String UPDATE_DELETE_BY_URL = "contentUrl.DeleteByUrl";
    private static final String UPDATE_DELETE_IN_LIST = "contentUrl.DeleteInList";
    private static final String UPDATE_DELETE_ALL = "contentUrl.DeleteAll";
    
    private static Log logger = LogFactory.getLog(HibernateContentUrlDAOImpl.class);
    
    public ContentUrl createContentUrl(String contentUrl)
    {
        ContentUrl entity = new ContentUrlImpl();
        entity.setContentUrl(contentUrl);
        getSession().save(entity);
        return entity;
    }

    public void getAllContentUrls(final ContentUrlHandler handler)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                        .getNamedQuery(HibernateContentUrlDAOImpl.QUERY_GET_ALL)
                        .setCacheMode(CacheMode.IGNORE);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults results = (ScrollableResults) getHibernateTemplate().execute(callback);
        while (results.next())
        {
            String contentUrl = results.getText(0);
            handler.handle(contentUrl);
        }
    }

    public void deleteContentUrl(final String contentUrl)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                session.flush();
                Query query = session
                    .getNamedQuery(HibernateContentUrlDAOImpl.UPDATE_DELETE_BY_URL)
                    .setCacheMode(CacheMode.IGNORE)
                    .setString("contentUrl", contentUrl);
                return (Integer) query.executeUpdate();
            }
        };
        Integer deletedCount = (Integer) getHibernateTemplate().execute(callback);
        int entityCount = getSession().getStatistics().getEntityCount();
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + deletedCount + " ContentUrl entities.");
        }
    }

    public void deleteContentUrls(final Set<String> contentUrls)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                session.flush();
                Query query = session
                    .getNamedQuery(HibernateContentUrlDAOImpl.UPDATE_DELETE_IN_LIST)
                    .setCacheMode(CacheMode.IGNORE)
                    .setParameterList("contentUrls", contentUrls, TypeFactory.basic("string"));
                return (Integer) query.executeUpdate();
            }
        };
        Integer deletedCount = (Integer) getHibernateTemplate().execute(callback);
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + deletedCount + " ContentUrl entities.");
        }
    }

    public void deleteAllContentUrls()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                session.flush();
                Query query = session
                    .getNamedQuery(HibernateContentUrlDAOImpl.UPDATE_DELETE_ALL)
                    .setCacheMode(CacheMode.IGNORE);
                return (Integer) query.executeUpdate();
            }
        };
        Integer deletedCount = (Integer) getHibernateTemplate().execute(callback);
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + deletedCount + " ContentUrl entities.");
        }
    }
}
