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
