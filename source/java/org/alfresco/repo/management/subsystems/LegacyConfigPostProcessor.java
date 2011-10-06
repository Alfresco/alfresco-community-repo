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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * A {@link BeanFactoryPostProcessor} that upgrades old-style Spring overrides that add location paths to the
 * <code>repository-properties</code> or <code>hibernateConfigProperties</code> beans to instead add these paths to the
 * <code>global-properties</code> bean. To avoid the warning messages output by this class, new property overrides
 * should be added to alfresco-global.properties without overriding any bean definitions.
 * 
 * @author dward
 */
public class LegacyConfigPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered
{
    /** The name of the bean that, in new configurations, holds all properties */
    private static final String BEAN_NAME_GLOBAL_PROPERTIES = "global-properties";

    /** The name of the bean that expands repository properties. These should now be defaulted from global-properties. */
    private static final String BEAN_NAME_REPOSITORY_PROPERTIES = "repository-properties";

    /** The name of the bean that holds hibernate properties. These should now be overriden by global-properties. */
    private static final String BEAN_NAME_HIBERNATE_PROPERTIES = "hibernateConfigProperties";

    /** The name of the property on a Spring property loader that holds a list of property file location paths. */
    private static final String PROPERTY_LOCATIONS = "locations";

    /** The name of the property on a Spring property loader that holds a local property map. */
    private static final String PROPERTY_PROPERTIES = "properties";

    /** The logger. */
    private static Log logger = LogFactory.getLog(LegacyConfigPostProcessor.class);

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.
     * beans.factory.config.ConfigurableListableBeanFactory)
     */
    @SuppressWarnings("unchecked")
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        try
        {
            // Look up the global-properties bean and its locations list
            MutablePropertyValues globalProperties = beanFactory.getBeanDefinition(
                    LegacyConfigPostProcessor.BEAN_NAME_GLOBAL_PROPERTIES).getPropertyValues();
            PropertyValue pv = globalProperties.getPropertyValue(LegacyConfigPostProcessor.PROPERTY_LOCATIONS);
            Collection<Object> globalPropertyLocations;
            Object value;

            // Use the locations list if there is one, otherwise associate a new empty list
            if (pv != null && (value = pv.getValue()) != null && value instanceof Collection)
            {
                globalPropertyLocations = (Collection<Object>) value;
            }
            else
            {
                globalPropertyLocations = new ManagedList(10);
                globalProperties
                        .addPropertyValue(LegacyConfigPostProcessor.PROPERTY_LOCATIONS, globalPropertyLocations);
            }

            // Move location paths added to repository-properties
            MutablePropertyValues repositoryProperties = processLocations(beanFactory, globalPropertyLocations,
                    LegacyConfigPostProcessor.BEAN_NAME_REPOSITORY_PROPERTIES, new String[]
                    {
                        "classpath:alfresco/version.properties"
                    });
            // Fix up additional properties to enforce correct order of precedence
            repositoryProperties.addPropertyValue("ignoreUnresolvablePlaceholders", Boolean.TRUE);
            repositoryProperties.addPropertyValue("localOverride", Boolean.FALSE);
            repositoryProperties.addPropertyValue("valueSeparator", null);
            repositoryProperties.addPropertyValue("systemPropertiesModeName", "SYSTEM_PROPERTIES_MODE_NEVER");

            // Move location paths added to hibernateConfigProperties
            MutablePropertyValues hibernateProperties = processLocations(beanFactory, globalPropertyLocations,
                    LegacyConfigPostProcessor.BEAN_NAME_HIBERNATE_PROPERTIES, new String[]
                    {
                        "classpath:alfresco/domain/hibernate-cfg.properties"
                    });
            // Fix up additional properties to enforce correct order of precedence
            hibernateProperties.addPropertyValue("localOverride", Boolean.TRUE);

            // Because Spring gets all post processors in one shot, the bean may already have been created. Let's try to
            // fix it up!
            PropertyPlaceholderConfigurer repositoryConfigurer = (PropertyPlaceholderConfigurer) beanFactory
                    .getSingleton(LegacyConfigPostProcessor.BEAN_NAME_REPOSITORY_PROPERTIES);
            if (repositoryConfigurer != null)
            {
                // Reset locations list
                repositoryConfigurer.setLocations(null);

                // Invalidate cached merged bean definitions
                ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(
                        LegacyConfigPostProcessor.BEAN_NAME_REPOSITORY_PROPERTIES, beanFactory
                                .getBeanDefinition(LegacyConfigPostProcessor.BEAN_NAME_REPOSITORY_PROPERTIES));

                // Reconfigure the bean according to its new definition
                beanFactory.configureBean(repositoryConfigurer,
                        LegacyConfigPostProcessor.BEAN_NAME_REPOSITORY_PROPERTIES);
            }
        }
        catch (NoSuchBeanDefinitionException e)
        {
            // Ignore and continue
        }
    }

    /**
     * Given a bean name (assumed to implement {@link org.springframework.core.io.support.PropertiesLoaderSupport})
     * checks whether it already references the <code>global-properties</code> bean. If not, 'upgrades' the bean by
     * appending all additional resources it mentions in its <code>locations</code> property to
     * <code>globalPropertyLocations</code>, except for those resources mentioned in <code>newLocations</code>. A
     * reference to <code>global-properties</code> will then be added and the resource list in
     * <code>newLocations<code> will then become the new <code>locations</code> list for the bean.
     * 
     * @param beanFactory
     *            the bean factory
     * @param globalPropertyLocations
     *            the list of global property locations to be appended to
     * @param beanName
     *            the bean name
     * @param newLocations
     *            the new locations to be set on the bean
     * @return the mutable property values
     */
    @SuppressWarnings("unchecked")
    private MutablePropertyValues processLocations(ConfigurableListableBeanFactory beanFactory,
            Collection<Object> globalPropertyLocations, String beanName, String[] newLocations)
    {
        // Get the bean an check its existing properties value
        MutablePropertyValues beanProperties = beanFactory.getBeanDefinition(beanName).getPropertyValues();
        PropertyValue pv = beanProperties.getPropertyValue(LegacyConfigPostProcessor.PROPERTY_PROPERTIES);
        Object value;

        // If the properties value already references the global-properties bean, we have nothing else to do. Otherwise,
        // we have to 'upgrade' the bean definition.
        if (pv == null || (value = pv.getValue()) == null || !(value instanceof BeanReference)
                || ((BeanReference) value).getBeanName().equals(LegacyConfigPostProcessor.BEAN_NAME_GLOBAL_PROPERTIES))
        {
            // Convert the array of new locations to a managed list of type string values, so that it is
            // compatible with a bean definition
            Collection<Object> newLocationList = new ManagedList(newLocations.length);
            if (newLocations != null && newLocations.length > 0)
            {
                for (String preserveLocation : newLocations)
                {
                    newLocationList.add(new TypedStringValue(preserveLocation));
                }
            }

            // If there is currently a locations list, process it
            pv = beanProperties.getPropertyValue(LegacyConfigPostProcessor.PROPERTY_LOCATIONS);
            if (pv != null && (value = pv.getValue()) != null && value instanceof Collection)
            {
                Collection<Object> locations = (Collection<Object>) value;

                // Compute the set of locations that need to be added to globalPropertyLocations (preserving order) and
                // warn about each
                Set<Object> addedLocations = new LinkedHashSet<Object>(locations);
                addedLocations.removeAll(globalPropertyLocations);
                addedLocations.removeAll(newLocationList);

                for (Object location : addedLocations)
                {
                    LegacyConfigPostProcessor.logger.warn("Legacy configuration detected: adding "
                            + (location instanceof TypedStringValue ? ((TypedStringValue) location).getValue()
                                    : location.toString()) + " to global-properties definition");
                    globalPropertyLocations.add(location);
                }

            }
            // Ensure the bean now references global-properties
            beanProperties.addPropertyValue(LegacyConfigPostProcessor.PROPERTY_PROPERTIES, new RuntimeBeanReference(
                    LegacyConfigPostProcessor.BEAN_NAME_GLOBAL_PROPERTIES));

            // Ensure the new location list is now set on the bean
            if (newLocationList.size() > 0)
            {
                beanProperties.addPropertyValue(LegacyConfigPostProcessor.PROPERTY_LOCATIONS, newLocationList);
            }
            else
            {
                beanProperties.removePropertyValue(LegacyConfigPostProcessor.PROPERTY_LOCATIONS);
            }
        }
        return beanProperties;
    }

    public int getOrder()
    {
        // This has to run before any other post-processor
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
