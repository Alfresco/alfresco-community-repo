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
package org.alfresco.repo.management.subsystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * A base class for {@link PropertyBackedBean}s. Gets its category from its Spring bean name and automatically
 * propagates and resolves property defaults on initialization. Automatically destroys itself on server shutdown.
 * Communicates its creation and destruction and start and stop events to a {@link PropertyBackedBeanRegistry}. Listens
 * for start and stop events from remote nodes in order to keep the bean in sync with edits made on a remote node. On
 * receiving a start event from a remote node, the bean is completely reinitialized, allowing it to be resynchronized
 * with any persisted changes.
 * 
 * @author dward
 */
public abstract class AbstractPropertyBackedBean implements PropertyBackedBean, ApplicationContextAware,
        ApplicationListener<ApplicationEvent>, InitializingBean, DisposableBean, BeanNameAware
{

    /** The default final part of an ID. */
    protected static final String DEFAULT_INSTANCE_NAME = "default";

    /** The parent application context. */
    private ApplicationContext parent;

    /** The registry of all property backed beans. */
    private PropertyBackedBeanRegistry registry;

    /** The category (first part of the ID). */
    private String category;
    
    /** The bean name if we have been initialized by Spring. */
    private String beanName;

    /** The hierarchical instance path within the category (second part of the ID). */
    private List<String> instancePath = Collections.singletonList(AbstractPropertyBackedBean.DEFAULT_INSTANCE_NAME);

    /** The combined unique id. */
    private List<String> id;

    /** Should the application context be started on startup of the parent application?. */
    private boolean autoStart;

    /** Property defaults provided by the installer or System properties. */
    private Properties propertyDefaults;

    /** Resolves placeholders in the property defaults. */
    private DefaultResolver defaultResolver = new DefaultResolver();

    /** The lifecycle states. */
    private enum RuntimeState {UNINITIALIZED, STOPPED, PENDING_BROADCAST_START, STARTED};
    
    private RuntimeState runtimeState = RuntimeState.UNINITIALIZED;

    /** The state. */
    private PropertyBackedBeanState state;
    
    /** Lock for concurrent access. */
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** The logger. */
    private static Log logger = LogFactory.getLog(AbstractPropertyBackedBean.class);

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.
     * ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.parent = applicationContext;
    }

    /**
     * Sets the registry of all property backed beans.
     * 
     * @param registry
     *            the registry of all property backed beans
     */
    public void setRegistry(PropertyBackedBeanRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Gets the registry of all property backed beans.
     * 
     * @return the registry of all property backed beans
     */
    protected PropertyBackedBeanRegistry getRegistry()
    {
        return this.registry;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.beanName = name;
    }
    
    /**
     * Sets the category (first part of the ID).
     * 
     * @param category
     *            the category
     */
    public void setCategory(String category)
    {
        this.category = category;
    }

    /**
     * Sets the hierarchical instance path within the category (second part of the ID)..
     * 
     * @param instancePath
     *            the instance path
     */
    public void setInstancePath(List<String> instancePath)
    {
        this.instancePath = instancePath;
    }

    /**
     * Indicates whether the bean should be started on startup of the parent application context.
     * 
     * @param autoStart
     *            <code>true</code> if the bean should be started on startup of the parent application context
     */
    public void setAutoStart(boolean autoStart)
    {
        this.autoStart = autoStart;
    }

    /**
     * Sets the property defaults provided by the installer or System properties.
     * 
     * @param propertyDefaults
     *            the property defaults
     */
    public void setPropertyDefaults(Properties propertyDefaults)
    {
        this.propertyDefaults = propertyDefaults;
    }

    /**
     * Gets the property defaults provided by the installer or System properties.
     * 
     * @return the property defaults
     */
    protected Properties getPropertyDefaults()
    {
        return this.propertyDefaults;
    }

    /**
     * Resolves the default value of a property, if there is one, expanding placholders as necessary.
     * 
     * @param name
     *            the property name
     * @return the resolved default value or <code>null</code> if there isn't one
     */
    protected String resolveDefault(String name)
    {
        String value = this.propertyDefaults.getProperty(name);
        if (value != null)
        {
            value = this.defaultResolver.resolveValue(value);
        }
        return value;
    }

    /**
     * Gets the parent application context.
     * 
     * @return the parent application context
     */
    protected ApplicationContext getParent()
    {
        return this.parent;
    }

    /**
     * Gets the state.
     * 
     * @param start
     *            are we making use of the state? I.e. should we start it if it has not been already?
     * @return the state
     */
    protected PropertyBackedBeanState getState(boolean start)
    {
        if (start)
        {
            start(true, false);
        }
        return this.state;
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception
    {
        // Default the category to the bean name
        if (this.category == null)
        {
            if (this.beanName == null)
            {
                throw new IllegalStateException("Category not provided");
            }
            this.category = this.beanName;
        }

        // Derive the unique ID from the category and instance path
        List<String> path = getInstancePath();
        this.id = new ArrayList<String>(path.size() + 1);
        this.id.add(this.category);
        this.id.addAll(getInstancePath());

        init();
    }

    /**
     * Initializes or resets the bean and its state.
     */
    public final void init()
    {
        this.lock.writeLock().lock();
        try
        {
            doInit();
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Initializes or resets the bean and its state.
     */
    protected void doInit()
    {
        boolean hadWriteLock = this.lock.isWriteLockedByCurrentThread();
        if (this.runtimeState == RuntimeState.UNINITIALIZED)
        {
            if (!hadWriteLock)
            {
                this.lock.readLock().unlock();
                this.lock.writeLock().lock();
            }
            try
            {
                if (this.runtimeState == RuntimeState.UNINITIALIZED)
                {
                    this.state = createInitialState();
                    applyDefaultOverrides(this.state);
                    this.runtimeState = RuntimeState.STOPPED;
                    this.registry.register(this);
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                if (!hadWriteLock)
                {
                    this.lock.readLock().lock();
                    this.lock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void revert()
    {
        this.lock.writeLock().lock();
        try
        {
            stop(true);
            destroy(true);
            doInit();
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Creates the initial state.
     * 
     * @return the property backed bean state
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected abstract PropertyBackedBeanState createInitialState() throws IOException;

    /**
     * Applies default overrides to the initial state.
     * 
     * @param state
     *            the state
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void applyDefaultOverrides(PropertyBackedBeanState state) throws IOException
    {
        for (String name : state.getPropertyNames())
        {
            String override = resolveDefault(name);
            if (override != null)
            {
                state.setProperty(name, override);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getId()
    {
        return this.id;
    }

    /**
     * Gets the category.
     * 
     * @return the category
     */
    protected String getCategory()
    {
        return this.category;
    }

    /**
     * Gets the hierarchical instance path within the category (second part of the ID).
     * 
     * @return the instance path
     */
    protected List<String> getInstancePath()
    {
        return this.instancePath;
    }

    /**
     * {@inheritDoc}
     */
    public final void destroy()
    {
        this.lock.writeLock().lock();
        try
        {
            destroy(false);
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Releases any resources held by this component.
     * 
     * @param isPermanent
     *            is the component being destroyed forever, i.e. should persisted values be removed? On server shutdown,
     *            this value would be <code>false</code>, whereas on the removal of a dynamically created instance, this
     *            value would be <code>true</code>.
     */
    protected void destroy(boolean isPermanent)
    {
        if (this.runtimeState != RuntimeState.UNINITIALIZED)
        {
            boolean hadWriteLock = this.lock.isWriteLockedByCurrentThread();
            if (!hadWriteLock)
            {
                this.lock.readLock().unlock();
                this.lock.writeLock().lock();
            }
            try
            {
                if (this.runtimeState != RuntimeState.UNINITIALIZED)
                {
                    stop(false);
                    this.registry.deregister(this, isPermanent);
                    this.state = null;
                    this.runtimeState = RuntimeState.UNINITIALIZED;
                }
            }
            finally
            {
                if (!hadWriteLock)
                {
                    this.lock.readLock().lock();
                    this.lock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUpdateable(String name)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription(String name)
    {
        return isUpdateable(name) ? "Editable Property " + name : "Read-only Property " + name;
    }

    /**
     * {@inheritDoc}
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (this.autoStart && event instanceof ContextRefreshedEvent && event.getSource() == this.parent)
        {
            this.lock.writeLock().lock();
            try
            {
                start(false, false);
            }
            catch (Exception e)
            {
                // Let's log and swallow auto-start exceptions so that they are non-fatal. This means that the system
                // can hopefully be brought up to a level where its configuration can be edited and corrected
                logger.error("Error auto-starting subsystem", e);
            }
            finally
            {
                this.lock.writeLock().unlock();
            }
        }
        else if (event instanceof PropertyBackedBeanStartedEvent)
        {
            this.lock.writeLock().lock();
            try
            {
                // If we aren't started, reinitialize so that we pick up state changes from the database
                switch (this.runtimeState)
                {
                case PENDING_BROADCAST_START:
                case STOPPED:
                    destroy(false);
                    // fall through
                case UNINITIALIZED:
                    start(false, false);                
                }
            }
            finally
            {
                this.lock.writeLock().unlock();                
            }
        }
        else if (event instanceof PropertyBackedBeanStoppedEvent)
        {
            this.lock.writeLock().lock();
            try
            {
                // Completely destroy the state so that it will have to be reinitialized should the bean be put back in
                // to use by this node
                destroy(false);
            }
            finally
            {
                this.lock.writeLock().unlock();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getProperty(String name)
    {
        this.lock.readLock().lock();
        try
        {
            doInit();
            return this.state.getProperty(name);
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getPropertyNames()
    {
        this.lock.readLock().lock();
        try
        {
            doInit();
            return this.state.getPropertyNames();
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setProperty(String name, String value)
    {
        this.lock.writeLock().lock();
        try
        {
            // Bring down the bean. The caller may have already broadcast this across the cluster
            stop(false);
            doInit();
            this.state.setProperty(name, value);
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    public void setProperties(Map<String, String> properties)
    {
        this.lock.writeLock().lock();
        try
        {
            // Bring down the bean. The caller may have already broadcast this across the cluster
            stop(false);
            doInit();

            Map<String, String> previousValues = new HashMap<String, String>(properties.size() * 2);
            try
            {
                // Set each of the properties and back up their previous values just in case
                for (Map.Entry<String, String> entry : properties.entrySet())
                {
                    String property = entry.getKey();
                    String previousValue = this.state.getProperty(property);
                    this.state.setProperty(property, entry.getValue());
                    previousValues.put(property, previousValue);
                }

                // Attempt to start locally
                start(false, true);

                // We still haven't broadcast the start - a persist is required first so this will be done by the caller
            }
            catch (Exception e)
            {
                // Oh dear - something went wrong. So restore previous state before rethrowing
                for (Map.Entry<String, String> entry : previousValues.entrySet())
                {
                    this.state.setProperty(entry.getKey(), entry.getValue());
                }
                
                // Bring the bean back up across the cluster
                start(true, false);
                if (e instanceof RuntimeException)
                {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException(e);
            }
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
       
    }
    /**
     * {@inheritDoc}
     */
    public final void start()
    {
        this.lock.writeLock().lock();
        try
        {
            start(true, false);
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Starts the bean, optionally broadcasting the event to remote nodes.
     * 
     * @param broadcastNow
     *            Should the event be broadcast immediately?
     * @param broadcastLater
     *            Should the event be broadcast ever?
     */
    protected void start(boolean broadcastNow, boolean broadcastLater)
    {
        boolean hadWriteLock = this.lock.isWriteLockedByCurrentThread();
        if (this.runtimeState != RuntimeState.STARTED)
        {
            if (!hadWriteLock)
            {
                this.lock.readLock().unlock();
                this.lock.writeLock().lock();
            }
            try
            {
                switch (this.runtimeState)
                {
                case UNINITIALIZED:
                    doInit();
                    // fall through
                case STOPPED:
                    this.state.start();
                    this.runtimeState = broadcastLater ? RuntimeState.PENDING_BROADCAST_START : RuntimeState.STARTED;
                    // fall through
                case PENDING_BROADCAST_START:
                    if (broadcastNow)
                    {
                        this.registry.broadcastStart(this);
                        this.runtimeState = RuntimeState.STARTED;
                    }
                }
            }
            finally
            {
                if (!hadWriteLock)
                {
                    this.lock.readLock().lock();
                    this.lock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void stop()
    {
        this.lock.writeLock().lock();
        try
        {
            stop(true);
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Stops the bean, optionally broadcasting the event to remote nodes.
     * 
     * @param broadcast
     *            Should the event be broadcast?
     */
    protected void stop(boolean broadcast)
    {
        boolean hadWriteLock = this.lock.isWriteLockedByCurrentThread();
        switch (this.runtimeState)
        {
        case PENDING_BROADCAST_START:
        case STARTED:
            if (!hadWriteLock)
            {
                this.lock.readLock().unlock();
                this.lock.writeLock().lock();
            }
            try
            {
                switch (this.runtimeState)
                {
                case STARTED:
                    if (broadcast)
                    {
                        this.registry.broadcastStop(this);
                    }
                    // fall through
                case PENDING_BROADCAST_START:
                    this.state.stop();
                    this.runtimeState = RuntimeState.STOPPED;
                }
            }
            finally
            {
                if (!hadWriteLock)
                {
                    this.lock.readLock().lock();
                    this.lock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * Uses a Spring {@link PropertyPlaceholderHelper} to resolve placeholders in the property defaults. This means
     * that placeholders need not be displayed in the configuration UI or JMX console.
     */
    public class DefaultResolver extends PropertyPlaceholderHelper
    {

        /**
         * Instantiates a new default resolver.
         */
        public DefaultResolver()
        {
            super("${", "}", ":", true);
        }

        /**
         * Expands the given value, resolving any ${} placeholders using the property defaults.
         * 
         * @param val
         *            the value to expand
         * @return the expanded value
         */
        public String resolveValue(String val)
        {
            return AbstractPropertyBackedBean.this.propertyDefaults == null ? null : replacePlaceholders(
                    val, AbstractPropertyBackedBean.this.propertyDefaults);
        }

    }
}
