package org.alfresco.module.org_alfresco_module_rm.model.compatibility;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;


/**
 * Dictionary bootstrap post processor unit test.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class DictionaryBootstrapPostProcessorUnitTest extends BaseUnitTest
{
    /** bean id's */
    private static final String BEAN_SITESERVICE_BOOTSTRAP = "siteService_dictionaryBootstrap";
    private static final String BEAN_RM_DICTIONARY_BOOTSTRAP = "org_alfresco_module_rm_dictionaryBootstrap";
    
    @Mock private ConfigurableListableBeanFactory mockedBeanFactory;
    @Mock private BeanDefinition mockedBeanDefinition;
    
    @InjectMocks private DictionaryBootstrapPostProcessor postProcessor;
    
    /** 
     * given the bean factory does not contain the site service bootstrap bean then ensure that it is
     * not added as a dependency
     */
    @Test
    public void noSiteServiceBootstrapBeanAvailable()
    {
        // === given ====        
        doReturn(false).when(mockedBeanFactory).containsBean(BEAN_SITESERVICE_BOOTSTRAP);
       
        // === when ===
        postProcessor.postProcessBeanFactory(mockedBeanFactory);
        
        // === then ===
        verify(mockedBeanFactory, times(1)).containsBean(BEAN_SITESERVICE_BOOTSTRAP);
        verifyNoMoreInteractions(mockedBeanFactory);
        verifyZeroInteractions(mockedBeanDefinition);
    }
    
    /**
     * given that the site service bootstrap bean is contained within the bean factory, ensure that
     * it is added as a dependency
     */
    @Test
    public void siteServiceBootstrapBeanAvailable()
    {
        // === given ====        
        doReturn(true).when(mockedBeanFactory).containsBean(BEAN_SITESERVICE_BOOTSTRAP);
        doReturn(true).when(mockedBeanFactory).containsBean(BEAN_RM_DICTIONARY_BOOTSTRAP);
        doReturn(mockedBeanDefinition).when(mockedBeanFactory).getBeanDefinition(BEAN_RM_DICTIONARY_BOOTSTRAP);
       
        // === when ===
        postProcessor.postProcessBeanFactory(mockedBeanFactory);
        
        // === then ===
        verify(mockedBeanFactory, times(1)).containsBean(BEAN_SITESERVICE_BOOTSTRAP);
        verify(mockedBeanFactory, times(1)).containsBean(BEAN_RM_DICTIONARY_BOOTSTRAP);
        
        verify(mockedBeanFactory, times(1)).getBeanDefinition(BEAN_RM_DICTIONARY_BOOTSTRAP);
        verify(mockedBeanDefinition, times(1)).setDependsOn(new String[]{BEAN_SITESERVICE_BOOTSTRAP});        
        
        verifyNoMoreInteractions(mockedBeanFactory, mockedBeanDefinition);
        
    }
}
