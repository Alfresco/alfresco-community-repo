/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.support.ManagedList;

/**
 * Classification method interceptor bean factory post processor.
 * <p>
 * Bean factory post processor that inspects available beans and adds the classification method interceptor
 * to all public services.
 *
 * @author Roy Wetherall
 * @since 3.0.a
 */
public class ClassificationMethodInterceptorPostProcessor implements BeanFactoryPostProcessor
{
    private static final String PROP_INTERCEPTOR_NAMES = "interceptorNames";
    private static final String TYPE_PROXY_FACTORY_BEAN = "ProxyFactoryBean";
    private static final String POSTFIX_SERVICE = "Service";
    private static final String BEAN_NAME_CLASSIFICATION_METHOD_INTERCEPTOR = "classificationMethodInterceptor";

    /**
     * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        // get all bean definition names
        String beans[] = beanFactory.getBeanDefinitionNames();
        for (String bean : beans)
        {
            // get bean definition
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(bean);

            // only modify proxy factory beans that follow the public service naming postfix convention
            if (beanDefinition.getBeanClassName() != null &&
                beanDefinition.getBeanClassName().endsWith(TYPE_PROXY_FACTORY_BEAN) &&
                bean.endsWith(POSTFIX_SERVICE) &&
                Character.isUpperCase(bean.charAt(0)))
            {
                // get the property values for the bean definition
                MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                if (propertyValues.contains(PROP_INTERCEPTOR_NAMES))
                {
                    // get the current list of interceptor names
                    PropertyValue value = propertyValues.getPropertyValue(PROP_INTERCEPTOR_NAMES);
                    ManagedList<RuntimeBeanNameReference> list = (ManagedList<RuntimeBeanNameReference>)value.getValue();
                    if (!list.isEmpty())
                    {
                        // add reference to classification method interceptor
                        RuntimeBeanNameReference beanReference = new RuntimeBeanNameReference(BEAN_NAME_CLASSIFICATION_METHOD_INTERCEPTOR);
                        list.add(beanReference);
                    }
                }
            }
        }
    }
}
