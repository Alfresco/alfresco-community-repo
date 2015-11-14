package org.alfresco.traitextender;

import org.springframework.beans.factory.InitializingBean;

public class SpringTraitExtenderConfigurator implements InitializingBean
{

    private boolean enableTraitExtender;
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
    }

}
