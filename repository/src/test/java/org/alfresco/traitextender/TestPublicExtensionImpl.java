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

public class TestPublicExtensionImpl extends InstanceExtension<TestPublicExtension, TestPublicTrait> implements TestPublicExtension
{

    public TestPublicExtensionImpl(TestPublicTrait trait)
    {
        super(trait);
    }

    @Override
    public String publicMethod1(String s)
    {
        return "EPM1" + trait.publicMethod1(s);
    }

    @Override
    public String publicMethod2(String s)
    {
        return "EPM2" + trait.publicMethod2(s);
    }

    @Override
    public void publicMethod3(boolean throwException, boolean throwExException) throws TestException
    {
        if (throwExException)
        {
            throw new TestException();
        }
        else
        {
            trait.publicMethod3(throwException, throwExException);
        }
    }

    @Override
    public void publicMethod4(boolean throwRuntimeException, boolean throwExRuntimeException)
    {
        if (throwExRuntimeException)
        {
            throw new TestRuntimeException();
        }
        else
        {
            trait.publicMethod4(throwRuntimeException, throwExRuntimeException);
        }
    }

}
