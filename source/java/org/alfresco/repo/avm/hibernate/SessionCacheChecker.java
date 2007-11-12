/**
 *
 */
package org.alfresco.repo.avm.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.EntityKey;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author britt
 */
public class SessionCacheChecker extends HibernateDaoSupport
{
    public static SessionCacheChecker instance = null;

    private static Log fgLogger = LogFactory.getLog(SessionCacheChecker.class);

    private int fCount = 0;

    public SessionCacheChecker()
    {
        instance = this;
    }

    public void check()
    {
        if (!fgLogger.isDebugEnabled())
        {
            return;
        }
        if (fCount % 1000 == 0)
        {
            Map<String, Integer> types = new HashMap<String, Integer>();
            Set<EntityKey> keys = (Set<EntityKey>)getSession().getStatistics().getEntityKeys();
            if (keys.size() > 200)
            {
                for (EntityKey key : keys)
                {
                    String name = key.getEntityName();
                    if (!types.containsKey(name))
                    {
                        types.put(name, 0);
                    }
                    types.put(name, types.get(name) + 1);
                }
                fgLogger.debug(types);
//                for (Object it : Thread.currentThread().getStackTrace())
//                {
//                    fgLogger.debug(it);
//                }
//                fCount = 0;
            }
        }
        fCount++;
    }
}
