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

package org.alfresco.traitextender;

import java.util.List;

import org.alfresco.traitextender.InstanceExtension;

public class TestExtensionImpl extends InstanceExtension<TestExtension,TestTrait> implements TestExtension
{

    public TestExtensionImpl(TestTrait tarit)
    {
        super(tarit);
    }

    @Override
    public String privateServiceMethod1(String s)
    {
        return trait.traitImplOf_privateServiceMethod1(s) + " TestExtensionImpl.privateServiceMethod1(" + s + ")";
    }

    @Override
    public String publicServiceMethod2(String s)
    {
        return trait.traitImplOf_publicServiceMethod2(s) + " TestExtensionImpl.privateServiceMethod1(" + s + ")";
    }

    @Override
    public String publicServiceMethod3(String s)
    {
        return "EX" + trait.publicServiceMethod3("TestExtensionImpl.publicServiceMethod3(" + s + ")");
    }

    @Override
    public void publicServiceMethod3(TestService s, List<Integer> traitIdentities)
    {
        traitIdentities.add(System.identityHashCode(trait));
        if (s != null)
        {
            s.publicServiceMethod3(null, traitIdentities);
        }
        traitIdentities.add(System.identityHashCode(trait));
        trait.traitImplOf_publicServiceMethod3(s, traitIdentities);
    }
}
