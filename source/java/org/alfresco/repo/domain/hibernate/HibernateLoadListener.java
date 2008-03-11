package org.alfresco.repo.domain.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.event.LoadEvent;
import org.hibernate.event.LoadEventListener;
import org.hibernate.proxy.HibernateProxy;
import net.sf.cglib.proxy.Enhancer;

public class HibernateLoadListener implements LoadEventListener
{

    public void onLoad(LoadEvent event, LoadType loadType) throws HibernateException
    {
        Object obj = event.getResult();
        if (obj instanceof HibernateProxy) {
            Enhancer.registerCallbacks(obj.getClass(),null);
        }


    }

}
