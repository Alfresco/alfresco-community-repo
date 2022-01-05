/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.model.compatibility;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Dictionary bootstap post processor.
 * <p>
 * Ensures compatibility with 4.2 and 4.2.1 as well as 4.2.2.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class DictionaryBootstrapPostProcessor implements BeanFactoryPostProcessor
{
    /** bean id's */
    private static final String BEAN_SITESERVICE_BOOTSTRAP = "siteService_dictionaryBootstrap";
    private static final String BEAN_RM_DICTIONARY_BOOTSTRAP = "org_alfresco_module_rm_dictionaryBootstrap";

    /**
     * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
    {
        // if the site service bootstrap bean and the RM dictionary bean are present in the bean factory
        if (beanFactory.containsBean(BEAN_SITESERVICE_BOOTSTRAP) &&
            beanFactory.containsBean(BEAN_RM_DICTIONARY_BOOTSTRAP))
        {
            // get the RM dictionary bootstrap bean definition
            BeanDefinition beanDef = beanFactory.getBeanDefinition(BEAN_RM_DICTIONARY_BOOTSTRAP);

            // set the dependency
            beanDef.setDependsOn(new String[]{BEAN_SITESERVICE_BOOTSTRAP});
        }
    }

}
