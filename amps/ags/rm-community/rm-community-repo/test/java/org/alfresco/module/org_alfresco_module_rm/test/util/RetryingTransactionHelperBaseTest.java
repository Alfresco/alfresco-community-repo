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

package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.RetryingTransactionHelperTestCase;
import org.springframework.context.ApplicationContext;

/**
 * Base unit test for a simple retrying transaction helper test case.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RetryingTransactionHelperBaseTest extends RetryingTransactionHelperTestCase
{
    /** Application context */
    protected static final String[] CONFIG_LOCATIONS = new String[]
    {
        "classpath:alfresco/application-context.xml",
        "classpath:test-context.xml"
    };
    protected ApplicationContext applicationContext;
    
    /** retrying transaction helper */
    protected RetryingTransactionHelper retryingTransactionHelper;

    /**
     * @see org.alfresco.util.RetryingTransactionHelperTestCase#getRetryingTransactionHelper()
     */
    @Override
    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        return retryingTransactionHelper;
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        // Get the application context
        applicationContext = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
        
        // get the retrying transaction helper
        retryingTransactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");
    }

}
