/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.api.tests;


import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTestGroup;
import org.apache.chemistry.opencmis.tck.tests.basics.RepositoryInfoTest;
import org.apache.chemistry.opencmis.tck.tests.basics.RootFolderTest;
import org.apache.chemistry.opencmis.tck.tests.basics.SecurityTest;

class AlfrescoCMISBasicsTestGroup extends AbstractSessionTestGroup
{
    @Override
    public void init(Map<String, String> parameters) throws Exception
    {
        super.init(parameters);

        setName("Basics Test Group");
        setDescription("Basic tests.");

        addTest(new SecurityTest());
        addTest(new RepositoryInfoTest());
        addTest(new RootFolderTest()
        {
            @Override
            protected CmisTestResult assertEquals(CmisObject expected, CmisObject actual, CmisTestResult success, CmisTestResult failure, boolean checkAcls, boolean checkPolicies)
            {
                return super.assertEquals(hideAsynchronouslyChangedProperties(expected), hideAsynchronouslyChangedProperties(actual), success, failure, checkAcls, checkPolicies);
            }
        });
    }

    private CmisObject hideAsynchronouslyChangedProperties(final CmisObject target)
    {
        return (CmisObject) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { CmisObject.class }, (proxy, method, args) -> {
            Object result = method.invoke(target, args);
            if ("getProperties".equals(method.getName()))
            {
                List<Property<?>> properties = (List<Property<?>>) result;
                result = properties.stream().filter(p -> !p.getId().startsWith("cmis:lastMod")).collect(Collectors.toUnmodifiableList());
            }
            return result;
        });
    }
}
