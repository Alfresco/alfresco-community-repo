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


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.tests.basics.BasicsTestGroup;
import org.apache.chemistry.opencmis.tck.tests.basics.RootFolderTest;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class AlfrescoCMISBasicsTestGroup extends BasicsTestGroup
{
    @Override
    public void init(Map<String, String> parameters) throws Exception
    {
        super.init(parameters);

        replaceRootFolderTest();
    }

    private void replaceRootFolderTest() throws Exception {
        getTests().removeIf(t -> t instanceof RootFolderTest);
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
        CmisObject spiedObject = spy(target);
        when(spiedObject.getProperties()).then(a -> {
            List<Property<?>> properties = (List<Property<?>>) a.callRealMethod();
            return properties.stream()
                    .filter(p -> !p.getId().startsWith("cmis:lastMod"))
                    .collect(Collectors.toUnmodifiableList());
        });

        return spiedObject;
    }
}
