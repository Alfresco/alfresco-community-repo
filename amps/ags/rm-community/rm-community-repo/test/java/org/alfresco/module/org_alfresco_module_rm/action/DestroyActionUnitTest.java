/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.JUnit4TestShouldUseTestAnnotation"})
public class DestroyActionUnitTest extends BaseRMTestCase
{

    private DestroyAction destroyAction;

    @Override
    protected String[] getConfigLocations()
    {
        List<String> list = new ArrayList<>(Arrays.asList(super.getConfigLocations()));
        list.add("classpath:alfresco/module/org_alfresco_module_rm/rm-action-context.xml");
        return list.toArray(new String[0]);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        destroyAction = (DestroyAction) applicationContext.getBean("destroy");
    }

    public void testDestroyActionAuditedImmediatelyTrue()
    {
        assertTrue("DestroyAction bean should be auditedImmediately so ghosted and non-ghosted records are recorded",
                destroyAction.isAuditedImmediately());
    }
}
