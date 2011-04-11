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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.context.ApplicationContext;

/**
 * A default {@link ChildApplicationContextManager} implementation that manages a 'chain' of
 * {@link ChildApplicationContextFactory} objects, perhaps corresponding to the components of a chained subsystem such
 * as authentication. As with other {@link PropertyBackedBean}s, can be stopped, reconfigured, started and tested. Its
 * one special <code>chain</code> property allows an ordered list of {@link ChildApplicationContextFactory} objects to
 * be managed. This property is a comma separated list with the format:
 * <ul>
 * <li>&lt;id1>:&lt;typeName1>,&lt;id2>:&lt;typeName2>,...,&lt;id<i>n</i>>:&lt;typeName<i>n</i>>
 * </ul>
 * See {@link ChildApplicationContextManager} for the meanings of &lt;id> and &lt;typeName>. In the enterprise edition,
 * this property is editable at runtime via JMX. If a new &lt;id> is included in the list then a new
 * {@link ChildApplicationContextFactory} will be brought into existence. Similarly, if one is removed from the list,
 * then the corresponding instance will be destroyed. For Alfresco community edition, the chain is best configured
 * through the {@link #setChain(String)} method via Spring configuration.
 * 
 * @author dward
 */
public class DefaultChildApplicationContextManager extends AbstractPropertyBackedBean implements
        ChildApplicationContextManager
{

    /** The name of the special property that holds the ordering of child instance names. */
    private static final String ORDER_PROPERTY = "chain";

    /** The default type name. */
    private String defaultTypeName;

    /** The default chain. */
    private String defaultChain;

    /**
     * Instantiates a new default child application context manager.
     */
    public DefaultChildApplicationContextManager()
    {
        setInstancePath(Collections.singletonList("manager"));
    }

    /**
     * Sets the default type name. This is used when a type name is not included after an instance ID in a chain string.
     * 
     * @param defaultTypeName
     *            the new default type name
     */
    public void setDefaultTypeName(String defaultTypeName)
    {
        this.defaultTypeName = defaultTypeName;
    }

    /**
     * Configures the default chain of {@link ChildApplicationContextFactory} instances. May be set on initialization by
     * the Spring container.
     * 
     * @param defaultChain
     *            a comma separated list in the following format:
     *            <ul>
     *            <li>&lt;id1>:&lt;typeName1>,&lt;id2>:&lt;typeName2>,...,&lt;id<i>n</i>>:&lt;typeName<i>n</i>>
     *            </ul>
     */
    public void setDefaultChain(String defaultChain)
    {
        this.defaultChain = defaultChain;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.AbstractPropertyBackedBean#getDescription(java.lang.String)
     */
    @Override
    public String getDescription(String name)
    {
        return "Comma separated list of name:type pairs";
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.AbstractPropertyBackedBean#destroy(boolean)
     */
    @Override
    public void destroy(boolean permanent)
    {
        if (getState(false) != null)
        {
            boolean hadWriteLock = this.lock.isWriteLockedByCurrentThread();
            if (!hadWriteLock)
            {
                this.lock.readLock().unlock();
                this.lock.writeLock().lock();
            }
            try
            {
                ApplicationContextManagerState state = (ApplicationContextManagerState) getState(false);

                if (state != null)
                {
                    // Cascade the destroy / shutdown
                    for (String id : state.getInstanceIds())
                    {
                        ChildApplicationContextFactory factory = state.getApplicationContextFactory(id);
                        factory.lock.writeLock().lock();
                        try
                        {
                            factory.destroy(permanent);
                        }
                        finally
                        {
                            factory.lock.writeLock().unlock();
                        }
                    }
                }

                super.destroy(permanent);
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
        else
        {
            super.destroy(permanent);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.AbstractPropertyBackedBean#createInitialState()
     */
    @Override
    protected PropertyBackedBeanState createInitialState() throws IOException
    {
        return new ApplicationContextManagerState(this.defaultChain, this.defaultTypeName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.ChildApplicationContextManager#getInstanceIds()
     */
    public Collection<String> getInstanceIds()
    {
        this.lock.readLock().lock();
        try
        {
            return ((ApplicationContextManagerState) getState(true)).getInstanceIds();
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.ChildApplicationContextManager#getApplicationContext(java.lang.String)
     */
    public ApplicationContext getApplicationContext(String id)
    {
        this.lock.readLock().lock();
        try
        {
            return ((ApplicationContextManagerState) getState(true)).getApplicationContext(id);
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    /**
     * The Class ApplicationContextManagerState.
     */
    protected class ApplicationContextManagerState implements PropertyBackedBeanState
    {
        
        /** The instance ids. */
        private List<String> instanceIds = new ArrayList<String>(10);

        /** The child application contexts. */
        private Map<String, ChildApplicationContextFactory> childApplicationContexts = new TreeMap<String, ChildApplicationContextFactory>();

        /** The default type name. */
        private String defaultTypeName;

        /**
         * Instantiates a new application context manager state.
         * 
         * @param defaultChain
         *            the default chain
         * @param defaultTypeName
         *            the default type name
         */
        protected ApplicationContextManagerState(String defaultChain, String defaultTypeName)
        {
            // Work out what the default type name should be; either specified explicitly or implied by the first member
            // of the default chain
            if (defaultChain != null && defaultChain.length() > 0)
            {
                // Use the first type as the default, unless one is specified explicitly
                if (defaultTypeName == null)
                {
                    updateOrder(defaultChain, AbstractPropertyBackedBean.DEFAULT_INSTANCE_NAME);
                    this.defaultTypeName = this.childApplicationContexts.get(this.instanceIds.get(0)).getTypeName();
                }
                else
                {
                    this.defaultTypeName = defaultTypeName;
                    updateOrder(defaultChain, defaultTypeName);
                }
            }
            else if (defaultTypeName == null)
            {
                this.defaultTypeName = AbstractPropertyBackedBean.DEFAULT_INSTANCE_NAME;
            }
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getProperty(java.lang.String)
         */
        public String getProperty(String name)
        {
            if (!name.equals(DefaultChildApplicationContextManager.ORDER_PROPERTY))
            {
                return null;
            }
            return getOrderString();
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getPropertyNames()
         */
        public Set<String> getPropertyNames()
        {
            return Collections.singleton(DefaultChildApplicationContextManager.ORDER_PROPERTY);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#setProperty(java.lang.String,
         * java.lang.String)
         */
        public void setProperty(String name, String value)
        {
            if (!name.equals(DefaultChildApplicationContextManager.ORDER_PROPERTY))
            {
                throw new IllegalStateException("Illegal attempt to write to property \"" + name + "\"");
            }
            updateOrder(value, this.defaultTypeName);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#start()
         */
        public void start()
        {
            for (String instance : getInstanceIds())
            {
                getApplicationContext(instance);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#stop()
         */
        public void stop()
        {
            // Nothing to do
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.ChildApplicationContextManager#getInstanceIds()
         */
        /**
         * Gets the instance ids.
         * 
         * @return the instance ids
         */
        public Collection<String> getInstanceIds()
        {
            return Collections.unmodifiableList(this.instanceIds);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.ChildApplicationContextManager#getApplicationContext(java.lang.String)
         */
        /**
         * Gets the application context.
         * 
         * @param id
         *            the id
         * @return the application context
         */
        public ApplicationContext getApplicationContext(String id)
        {
            ChildApplicationContextFactory child = this.childApplicationContexts.get(id);
            return child == null ? null : child.getApplicationContext();
        }

        /**
         * Gets the application context factory.
         * 
         * @param id
         *            the id
         * @return the application context factory
         */
        protected ChildApplicationContextFactory getApplicationContextFactory(String id)
        {
            return this.childApplicationContexts.get(id);
        }

        /**
         * Gets the order string.
         * 
         * @return the order string
         */
        private String getOrderString()
        {
            StringBuilder orderString = new StringBuilder(100);
            for (String id : this.instanceIds)
            {
                if (orderString.length() > 0)
                {
                    orderString.append(",");
                }
                orderString.append(id).append(':').append(this.childApplicationContexts.get(id).getTypeName());
            }
            return orderString.toString();
        }

        /**
         * Updates the order from a comma or whitespace separated string.
         * 
         * @param orderString
         *            the order as a comma or whitespace separated string
         * @param defaultTypeName
         *            the default type name
         */
        private void updateOrder(String orderString, String defaultTypeName)
        {
            try
            {
                StringTokenizer tkn = new StringTokenizer(orderString, ", \t\n\r\f");
                List<String> newInstanceIds = new ArrayList<String>(tkn.countTokens());
                while (tkn.hasMoreTokens())
                {
                    String instance = tkn.nextToken();
                    int sepIndex = instance.indexOf(':');
                    String id = sepIndex == -1 ? instance : instance.substring(0, sepIndex);
                    String typeName = sepIndex == -1 || sepIndex + 1 >= instance.length() ? defaultTypeName : instance
                            .substring(sepIndex + 1);
                    newInstanceIds.add(id);

                    // Look out for new or updated children
                    ChildApplicationContextFactory factory = this.childApplicationContexts.get(id);

                    // If we have the same instance ID but a different type, treat that as a destroy and remove
                    if (factory != null && !factory.getTypeName().equals(typeName))
                    {
                        factory.lock.writeLock().lock();
                        try
                        {
                            factory.destroy(true);
                        }
                        finally
                        {
                            factory.lock.writeLock().unlock();
                        }
                        factory = null;
                    }
                    if (factory == null)
                    {
                        // Generate a unique ID within the category
                        List<String> childId = new ArrayList<String>(2);
                        childId.add("managed");
                        childId.add(id);
                        this.childApplicationContexts.put(id, new ChildApplicationContextFactory(getParent(),
                                getRegistry(), getPropertyDefaults(), getCategory(), typeName, childId));
                    }
                }

                // Destroy any children that have been removed
                Set<String> idsToRemove = new TreeSet<String>(this.childApplicationContexts.keySet());
                idsToRemove.removeAll(newInstanceIds);
                for (String id : idsToRemove)
                {
                    ChildApplicationContextFactory factory = this.childApplicationContexts.remove(id);
                    factory.lock.writeLock().lock();
                    try
                    {
                        factory.destroy(true);
                    }
                    finally
                    {
                        factory.lock.writeLock().unlock();
                    }
                }
                this.instanceIds = newInstanceIds;
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

    }
}
