/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.management.subsystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
    /**
     *  When true, calls to setProperties / setProperty are persisted to the MBean if it exists.
     */
    private boolean saveSetProperty = false;

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
    
    /** Property defaults provided by the JASYPT decryptor. */
    private Properties encryptedPropertyDefaults;

    /** Resolves placeholders in the property defaults. */
    private DefaultResolver defaultResolver = new DefaultResolver();

    /** The lifecycle states. */
    protected enum RuntimeState {UNINITIALIZED, STOPPED, PENDING_BROADCAST_START, STARTED};
    
    protected RuntimeState runtimeState = RuntimeState.UNINITIALIZED;

    /** The state. */
    private PropertyBackedBeanState state;
    
    /** Lock for concurrent access. */
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private Map<String, SubsystemEarlyPropertyChecker> earlyPropertyCheckers;
    
    public void setEarlyPropertyCheckers(Map<String, SubsystemEarlyPropertyChecker> earlyPropertyCheckers)
    {
        this.earlyPropertyCheckers = earlyPropertyCheckers;
    }
    
    /**
     * Used in conjunction with {@link #localSetProperties} to control setting of
     * properties from either a JMX client or by code in the local Alfresco
     * node calling {@link AbstractPropertyBackedBean#setProperties(Map)} or
     * {@link AbstractPropertyBackedBean#setProperty(String, String)}.
     * Is <code>true</code> when there is a nested call to either of these
     * methods. This is the case when there is an MBean AND one of these method was
     * NOT originally called from that MBean (it is a local code).
     */
    private ThreadLocal<Boolean> nestedCall = new ThreadLocal<Boolean>()
    {
        @Override
        protected Boolean initialValue()
        {
            return false;
        }
    };

   /**
    * Used in conjunction with {@link #nestedCall} to control setting of
    * properties from either a JMX client or by code in the local Alfresco
    * node calling {@link AbstractPropertyBackedBean#setProperties(Map)} or
    * {@link AbstractPropertyBackedBean#setProperty(String, String)}.
    * Is set to <code>true</code> when there is a nested call back from
    * a JMX bean.
    */
    private ThreadLocal<Boolean> localSetProperties = new ThreadLocal<Boolean>()
    {
        @Override
        protected Boolean initialValue()
        {
            return false;
        }
    };
    
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
    
    public void setEncryptedPropertyDefaults(Properties propertyDefaults)
    {
        this.encryptedPropertyDefaults = propertyDefaults;
        
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

    protected Properties getEncryptedPropertyDefaults()
    {
        return this.encryptedPropertyDefaults;
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
        Properties props = new Properties();
        
        if(propertyDefaults != null)
        {
            for( Object key : propertyDefaults.keySet())
            {
                props.setProperty((String)key, propertyDefaults.getProperty((String)key));
            }
        }
        
        if(encryptedPropertyDefaults != null)
        {
            for( Object key : encryptedPropertyDefaults.keySet())
            {
                props.setProperty((String)key, encryptedPropertyDefaults.getProperty((String)key));
            }
        }
        
        String value = props.getProperty(name);
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
     *  When set to true, calls to setProperties / setProperty are persisted to the MBean if it exists.
     */
    public void setSaveSetProperty(boolean saveSetProperty)
    {
        this.saveSetProperty = saveSetProperty;
    }

    /**
     * Check properties for invalid values using {@link SubsystemEarlyPropertyChecker}s
     * @param properties
     * @return The complete error message in case of exceptions or empty string otherwise
     */
    public String performEarlyPropertyChecks(Map<String, String> properties)
    {
        if (properties != null && !properties.isEmpty() && earlyPropertyCheckers != null)
        {
            List<InvalidPropertyValueException> exceptions = new ArrayList<InvalidPropertyValueException>();

            for (String property : properties.keySet())
            {
                if (earlyPropertyCheckers.containsKey(property))
                {
                    try
                    {
                        SubsystemEarlyPropertyChecker propertyChecker = earlyPropertyCheckers.get(property);

                        if (propertyChecker != null)
                        {
                            if (propertyChecker.getPairedPropertyName() != null
                                    && properties.containsKey(propertyChecker.getPairedPropertyName()))
                            {
                                propertyChecker.checkPropertyValue(property, properties.get(property),
                                        properties.get(propertyChecker.getPairedPropertyName()));
                            }
                            else
                            {
                                propertyChecker.checkPropertyValue(property, properties.get(property), null);
                            }
                        }
                    }
                    catch (InvalidPropertyValueException ipve)
                    {
                        exceptions.add(ipve);
                    }
                }
            }

            if (exceptions.size() > 0)
            {
                String allExceptionsMessages = "";
                
                for (InvalidPropertyValueException ipve : exceptions)
                {
                    if (!allExceptionsMessages.equals(""))
                    {
                        allExceptionsMessages += " | ";
                    }

                    allExceptionsMessages += ipve.getLocalizedMessage();
                }

                return allExceptionsMessages;
            }
        }
        
        return "";
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
                    logger.debug("doInit() createInitialState");
                    this.state = createInitialState();
                    logger.debug("doInit() applyDefaultOverrides "+state);
                    applyDefaultOverrides(this.state);
                    this.runtimeState = RuntimeState.STOPPED;
                    logger.debug("doInit() register");
                    this.registry.register(this);
                    logger.debug("doInit() done");
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                logger.debug("doInit() state="+runtimeState);
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
                    logger.debug("destroy() stop state="+runtimeState);
                    stop(false);
                    logger.debug("destroy() deregister "+isPermanent);
                    this.registry.deregister(this, isPermanent);
                    this.state = null;
                    this.runtimeState = RuntimeState.UNINITIALIZED;
                    logger.debug("destroy() done");
                }
            }
            finally
            {
                logger.debug("destroy() state="+runtimeState);
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

    private void setPropertyInternal(String name, String value)
    {
        // Bring down the bean. The caller may have already broadcast this across the cluster
        stop(false);
        doInit();
        this.state.setProperty(name, value);
    }

    private void setPropertiesInternal(Map<String, String> properties)
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
   
    /**
     * {@inheritDoc}
     * 
     * <p> When called from code within the local node the values are saved to the
     * database in the Enterprise edition and will be visible in a JMX client.<p>
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, String value)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("setProperty("+name+','+value+")");
        }
        if (!nestedCall.get())
        {
            nestedCall.set(true);
            this.lock.writeLock().lock();
            try
            {
                boolean mBeanInfoChange = !getPropertyNames().contains(name);

                // When setting properties locally AND there is an MBean, the following broadcast
                // results in a call to the MBean's setAttributes method, which in turn results
                // in a nested call back. The call back sets the values in this bean and
                // localSetProperties will be set to true. The MBean persists the changes and the
                // broadcast method returns. If there is no MBean (community edition) OR when
                // initiated from the MBean (say setting a value via JConsole), nothing happens
                // as a result of the broadcast.
                if (saveSetProperty)
                {
                    logger.debug("setProperty() broadcastSetProperties");
                    this.registry.broadcastSetProperty(this, name, value);
                }

                if (localSetProperties.get())
                {
                    if (mBeanInfoChange)
                    {
                        // Re register the bean so new properties are visible in JConsole which does
                        // not check MBeanInfo for changes otherwise.
                        logger.debug("setProperty() destroy");
//                      Commented out to avoid "UserTransaction is not visible from class loader" as it drops the context.
//                      So we have to just live with the JConsole as it is.
//                      destroy(false);

                        // Attempt to start locally
                        start(false, true);
                    }
                }
                else
                {
                    logger.debug("setProperty() setPropertyInternal");
                    setPropertyInternal(name, value);
                }
            }
            finally
            {
                localSetProperties.set(false);
                nestedCall.set(false);
                this.lock.writeLock().unlock();
            }
        }
        else
        {
            // A nested call indicates there is a MBean and that this method was
            // NOT originally called from that MBean.
            localSetProperties.set(true);

            logger.debug("setProperty() callback setPropertyInternal");
            setPropertyInternal(name, value);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p> When called from code within the local node the values are saved to the
     * database in the Enterprise edition and will be visible in a JMX client.<p>
     *
     * @param properties to be saved.
     */
    public void setProperties(Map<String, String> properties)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("setProperties("+properties+")");
        }
        if (!nestedCall.get())
        {
            nestedCall.set(true);
            
            boolean hadWriteLock = this.lock.isWriteLockedByCurrentThread();
            if (!hadWriteLock)
            {
                this.lock.writeLock().lock();
            }
            try
            {
                boolean mBeanInfoChange = !getPropertyNames().containsAll(properties.keySet());

                // When setting properties locally AND there is an MBean, the following broadcast
                // results in a call to the MBean's setAttributes method, which in turn results
                // in a nested call back. The call back sets the values in this bean and
                // localSetProperties will be set to true. The MBean persists the changes and the
                // broadcast method returns. If there is no MBean (community edition) OR when
                // initiated from the MBean (say setting a value via JConsole), nothing happens
                // as a result of the broadcast.
                if (saveSetProperty)
                {
                    logger.debug("setProperties() broadcastSetProperties");
                    this.registry.broadcastSetProperties(this, properties);
                }

                if (localSetProperties.get())
                {
                    if (mBeanInfoChange)
                    {
                        // Re register the bean so new properties are visible in JConsole which does
                        // not check MBeanInfo for changes otherwise.
                        logger.debug("setProperties() destroy");
//                      Commented out to avoid "UserTransaction is not visible from class loader" as it drops the context.
//                      So we have to just live with the JConsole as it is.
//                      destroy(false);

                        // Attempt to start locally
                        start(true, false);
                    }
                }
                else
                {
                    logger.debug("setProperties() setPropertiesInternal");
                    setPropertiesInternal(properties);
                }
            }
            finally
            {
                localSetProperties.set(false);
                nestedCall.set(false);
                if (!hadWriteLock)
                {
                    this.lock.writeLock().unlock();
                }
            }
        }
        else
        {
            // A nested call indicates there is a MBean and that this method was
            // NOT originally called from that MBean.
            localSetProperties.set(true);

            logger.debug("setProperties() callback setPropertiesInternal");
            setPropertiesInternal(properties);
        }
    }
    
    /**
     * Removes a property added by code within the local node.
     *
     * @param name to be removed.
     */
    public void removeProperty(String name)
    {
        removeProperties(Collections.singleton(name));
    }
    
    /**
     * Removes properties added by code within the local node.
     *
     * @param properties to be removed.
     */
    public void removeProperties(Collection<String> properties)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("removeProperties("+properties+")");
        }
        if (!nestedCall.get())
        {
            nestedCall.set(true);
            
            boolean hadWriteLock = this.lock.isWriteLockedByCurrentThread();
            if (!hadWriteLock)
            {
                this.lock.writeLock().lock();
            }
            try
            {
                boolean mBeanInfoChange = !getPropertyNames().containsAll(properties);

                // When setting properties locally AND there is an MBean, the following broadcast
                // results in a call to the MBean's setAttributes method, which in turn results
                // in a nested call back. The call back sets the values in this bean and
                // localSetProperties will be set to true. The MBean persists the changes and the
                // broadcast method returns. If there is no MBean (community edition) OR when
                // initiated from the MBean (say setting a value via JConsole), nothing happens
                // as a result of the broadcast.
                if (saveSetProperty)
                {
                    logger.debug("removeProperties() broadcastRemoveProperties");
                    this.registry.broadcastRemoveProperties(this, properties);
                }

                if (localSetProperties.get())
                {
                    if (mBeanInfoChange)
                    {
                        // Re register the bean so new properties are visible in JConsole which does
                        // not check MBeanInfo for changes otherwise.
                        logger.debug("removeProperties() destroy");
//                      Commented out to avoid "UserTransaction is not visible from class loader" as it drops the context.
//                      So we have to just live with the JConsole as it is.
//                      destroy(false);

                        // Attempt to start locally
                        start(true, false);
                    }
                }
                else
                {
                    logger.debug("removeProperties() removePropertiesInternal");
                    removePropertiesInternal(properties);
                }
            }
            finally
            {
                localSetProperties.set(false);
                nestedCall.set(false);
                if (!hadWriteLock)
                {
                    this.lock.writeLock().unlock();
                }
            }
        }
        else
        {
            // A nested call indicates there is a MBean and that this method was
            // NOT originally called from that MBean.
            localSetProperties.set(true);

            logger.debug("removeProperties() callback removePropertiesInternal");
            removePropertiesInternal(properties);
        }
    }

    private void removePropertiesInternal(Collection<String> properties)
    {
        // Bring down the bean. The caller may have already broadcast this across the cluster
        stop(false);
        doInit();

        Map<String, String> previousValues = new HashMap<String, String>(properties.size() * 2);
        try
        {
            // Set each of the properties and back up their previous values just in case
            for (String property : properties)
            {
                String previousValue = state.getProperty(property);
                this.state.removeProperty(property);
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
            Properties props = new Properties();
            
            if(propertyDefaults != null)
            {
                for( Object key : propertyDefaults.keySet())
                {
                    props.setProperty((String)key, propertyDefaults.getProperty((String)key));
                }
            }
            
            if(encryptedPropertyDefaults != null)
            {
                for( Object key : encryptedPropertyDefaults.keySet())
                {
                    props.setProperty((String)key, encryptedPropertyDefaults.getProperty((String)key));
                }
            }
            return replacePlaceholders(val, props);
        }

    }
}
