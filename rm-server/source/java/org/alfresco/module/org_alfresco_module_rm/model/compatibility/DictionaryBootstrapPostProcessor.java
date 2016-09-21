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
