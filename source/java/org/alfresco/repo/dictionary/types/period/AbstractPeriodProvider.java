package org.alfresco.repo.dictionary.types.period;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.PeriodProvider;
import org.springframework.beans.factory.InitializingBean;

/**
 * Common support for period implementations.
 * 
 * They are Spring beans that register in the bootstrap context.
 * 
 * @author andyh
 *
 */
public abstract class AbstractPeriodProvider implements PeriodProvider, InitializingBean
{
    protected static final String MSG_PREFIX = "period_provider.";
    
    /**
     * Default constructor
     */
    public AbstractPeriodProvider()
    {
        super();
    }
    
    public void afterPropertiesSet() throws Exception
    {
        Period.registerProvider(this);
    }

    /*
     * @see org.alfresco.service.cmr.repository.PeriodProvider#getDisplayLabel()
     */
    public String getDisplayLabel()
    {
        String label = I18NUtil.getMessage(MSG_PREFIX + getPeriodType());
        
        if (label == null)
        {
            label = getPeriodType();
        }
        
        return label;
    }
}
