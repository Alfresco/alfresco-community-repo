/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.management;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.GenericApplicationListenerAdapter;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.OrderComparator;

/**
 * Abstract implementation of the {@link ApplicationEventMulticaster} interface,
 * providing the basic listener registration facility.
 * 
 * <p>
 * Doesn't permit multiple instances of the same listener by default, as it
 * keeps listeners in a linked Set. The collection class used to hold
 * ApplicationListener objects can be overridden through the "collectionClass"
 * bean property.
 * 
 * <p>
 * Implementing ApplicationEventMulticaster's actual {@link #multicastEvent}
 * method is left to subclasses. {@link SimpleApplicationEventMulticaster}
 * simply multicasts all events to all registered listeners, invoking them in
 * the calling thread. Alternative implementations could be more sophisticated
 * in those respects.
 * 
 * @author Juergen Hoeller
 * @since 1.2.3
 * @see #getApplicationListeners(ApplicationEvent)
 * @see SimpleApplicationEventMulticaster
 */
public class SafeApplicationEventMulticaster implements ApplicationEventMulticaster, ApplicationContextAware
{
    private final Log log = LogFactory.getLog(SafeApplicationEventMulticaster.class);
    private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

    private final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = new ConcurrentHashMap<ListenerCacheKey, ListenerRetriever>();

    private ApplicationContext appContext;
    private Executor taskExecutor;

    /** Has the application started? */
    private boolean isApplicationStarted;

    /** The queued events that can't be broadcast until the application is started. */
    private List<ApplicationEvent> queuedEvents = new LinkedList<ApplicationEvent>();


    /**
     * Set the TaskExecutor to execute application listeners with.
     * <p>
     * Default is a SyncTaskExecutor, executing the listeners synchronously in
     * the calling thread.
     * <p>
     * Consider specifying an asynchronous TaskExecutor here to not block the
     * caller until all listeners have been executed. However, note that
     * asynchronous execution will not participate in the caller's thread
     * context (class loader, transaction association) unless the TaskExecutor
     * explicitly supports this.
     * 
     * @see org.springframework.core.task.SyncTaskExecutor
     * @see org.springframework.core.task.SimpleAsyncTaskExecutor
     */
    public void setTaskExecutor(Executor taskExecutor)
    {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Return the current TaskExecutor for this multicaster.
     */
    protected Executor getTaskExecutor()
    {
        return this.taskExecutor;
    }

    public void addApplicationListener(ApplicationListener listener)
    {
        synchronized (this.defaultRetriever)
        {
            this.defaultRetriever.applicationListeners.add(listener);
            this.retrieverCache.clear();
        }
    }

    public void addApplicationListenerBean(String listenerBeanName)
    {
        synchronized (this.defaultRetriever)
        {
            this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
            this.retrieverCache.clear();
        }
    }

    public void removeApplicationListener(ApplicationListener listener)
    {
        synchronized (this.defaultRetriever)
        {
            this.defaultRetriever.applicationListeners.remove(listener);
            this.retrieverCache.clear();
        }
    }

    public void removeApplicationListenerBean(String listenerBeanName)
    {
        synchronized (this.defaultRetriever)
        {
            this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
            this.retrieverCache.clear();
        }
    }

    public void removeAllListeners()
    {
        synchronized (this.defaultRetriever)
        {
            this.defaultRetriever.applicationListeners.clear();
            this.defaultRetriever.applicationListenerBeans.clear();
            this.retrieverCache.clear();
        }
    }

    private BeanFactory getBeanFactory()
    {
        if (this.appContext == null)
        {
            throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans "
                    + "because it is not associated with a BeanFactory");
        }
        return this.appContext;
    }

    @Override
    public void multicastEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent && event.getSource() == this.appContext)
        {
            this.isApplicationStarted = true;
            for (ApplicationEvent queuedEvent : this.queuedEvents)
            {
                multicastEventInternal(queuedEvent);
            }
            this.queuedEvents.clear();
            multicastEventInternal(event);
        }
        else if (event instanceof ContextClosedEvent && event.getSource() == this.appContext)
        {
            this.isApplicationStarted = false;
            multicastEventInternal(event);
        }
        else if (this.isApplicationStarted)
        {
            multicastEventInternal(event);
        }
        else
        {
            this.queuedEvents.add(event);
        }
    }

    @SuppressWarnings("unchecked")
    protected void multicastEventInternal(final ApplicationEvent event) {
        for (final ApplicationListener listener : getApplicationListeners(event)) {
            Executor executor = getTaskExecutor();
            if (executor != null) {
                executor.execute(new Runnable() {
                    public void run() {
                        listener.onApplicationEvent(event);
                    }
                });
            }
            else {
                listener.onApplicationEvent(event);
            }
        }
    }

    /**
     * Return a Collection containing all ApplicationListeners.
     * 
     * @return a Collection of ApplicationListeners
     * @see org.springframework.context.ApplicationListener
     */
    protected Collection<ApplicationListener> getApplicationListeners()
    {
        return this.defaultRetriever.getApplicationListeners();
    }

    /**
     * Return a Collection of ApplicationListeners matching the given event
     * type. Non-matching listeners get excluded early.
     * 
     * @param event
     *            the event to be propagated. Allows for excluding non-matching
     *            listeners early, based on cached matching information.
     * @return a Collection of ApplicationListeners
     * @see org.springframework.context.ApplicationListener
     */
    protected Collection<ApplicationListener> getApplicationListeners(ApplicationEvent event)
    {
        Class<? extends ApplicationEvent> eventType = event.getClass();
        Class sourceType = event.getSource().getClass();
        ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);
        ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
        if (retriever != null)
        {
            return retriever.getApplicationListeners();
        }
        else
        {
            retriever = new ListenerRetriever(true);
            LinkedList<ApplicationListener> allListeners = new LinkedList<ApplicationListener>();
            synchronized (this.defaultRetriever)
            {
                if (!this.defaultRetriever.applicationListenerBeans.isEmpty())
                {
                    BeanFactory beanFactory = getBeanFactory();
                    for (String listenerBeanName : this.defaultRetriever.applicationListenerBeans)
                    {
                        ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                        if (supportsEvent(listener, eventType, sourceType))
                        {
                            retriever.applicationListenerBeans.add(listenerBeanName);
                            allListeners.add(listener);
                        }
                    }
                }
                for (ApplicationListener listener : this.defaultRetriever.applicationListeners)
                {
                    if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType))
                    {
                        retriever.applicationListeners.add(listener);
                        allListeners.add(listener);
                    }
                }
                OrderComparator.sort(allListeners);
                this.retrieverCache.put(cacheKey, retriever);
            }
            if (log.isDebugEnabled())
            {
                log.debug(allListeners.toString());
            }
            return allListeners;
        }
    }

    /**
     * Determine whether the given listener supports the given event.
     * <p>
     * The default implementation detects the {@link SmartApplicationListener}
     * interface. In case of a standard {@link ApplicationListener}, a
     * {@link GenericApplicationListenerAdapter} will be used to introspect the
     * generically declared type of the target listener.
     * 
     * @param listener
     *            the target listener to check
     * @param eventType
     *            the event type to check against
     * @param sourceType
     *            the source type to check against
     * @return whether the given listener should be included in the candidates
     *         for the given event type
     */
    protected boolean supportsEvent(ApplicationListener listener, Class<? extends ApplicationEvent> eventType,
            Class sourceType)
    {

        SmartApplicationListener smartListener = (listener instanceof SmartApplicationListener ? (SmartApplicationListener) listener
                : new GenericApplicationListenerAdapter(listener));
        return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
    }

    /**
     * Cache key for ListenerRetrievers, based on event type and source type.
     */
    private static class ListenerCacheKey
    {

        private final Class eventType;

        private final Class sourceType;

        public ListenerCacheKey(Class eventType, Class sourceType)
        {
            this.eventType = eventType;
            this.sourceType = sourceType;
        }

        @Override
        public boolean equals(Object other)
        {
            if (this == other)
            {
                return true;
            }
            ListenerCacheKey otherKey = (ListenerCacheKey) other;
            return (this.eventType.equals(otherKey.eventType) && this.sourceType.equals(otherKey.sourceType));
        }

        @Override
        public int hashCode()
        {
            return this.eventType.hashCode() * 29 + this.sourceType.hashCode();
        }
    }

    /**
     * Helper class that encapsulates a specific set of target listeners,
     * allowing for efficient retrieval of pre-filtered listeners.
     * <p>
     * An instance of this helper gets cached per event type and source type.
     */
    private class ListenerRetriever
    {

        public final Set<ApplicationListener> applicationListeners;

        public final Set<String> applicationListenerBeans;

        private final boolean preFiltered;

        public ListenerRetriever(boolean preFiltered)
        {
            this.applicationListeners = new LinkedHashSet<ApplicationListener>();
            this.applicationListenerBeans = new LinkedHashSet<String>();
            this.preFiltered = preFiltered;
        }

        public Collection<ApplicationListener> getApplicationListeners()
        {
            LinkedList<ApplicationListener> allListeners = new LinkedList<ApplicationListener>();
            if (!this.applicationListenerBeans.isEmpty())
            {
                BeanFactory beanFactory = getBeanFactory();
                for (String listenerBeanName : this.applicationListenerBeans)
                {
                    ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                    allListeners.add(listener);
                }
            }
            for (ApplicationListener listener : this.applicationListeners)
            {
                if (this.preFiltered || !allListeners.contains(listener))
                {
                    allListeners.add(listener);
                }
            }
            OrderComparator.sort(allListeners);
            if (log.isDebugEnabled())
            {
                log.debug(allListeners.toString());
            }
            return allListeners;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.appContext = applicationContext;
    }

}
