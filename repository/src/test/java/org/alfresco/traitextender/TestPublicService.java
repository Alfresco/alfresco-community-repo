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

import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.AJProxyTrait;
import org.alfresco.traitextender.Trait;

public class TestPublicService implements Extensible
{
    private final ExtendedTrait<TestPublicTrait> testPublicTrait;
    
    public TestPublicService()
    {
        this.testPublicTrait=new ExtendedTrait<TestPublicTrait>(AJProxyTrait.create(this, TestPublicTrait.class));
    }
    
    @Extend(traitAPI = TestPublicTrait.class, extensionAPI = TestPublicExtension.class)
    public String publicMethod1(String s)
    {
        return "PM1" + s;
    }

    @Extend(traitAPI = TestPublicTrait.class, extensionAPI = TestPublicExtension.class)
    public String publicMethod2(String s)
    {
        return "PM2" + s;
    }

    @Extend(traitAPI = TestPublicTrait.class, extensionAPI = TestPublicExtension.class)
    public void publicMethod3(boolean throwException, boolean throwExException) throws TestException
    {
        if (throwException) { throw new TestException(); }

    }

    @Extend(traitAPI = TestPublicTrait.class, extensionAPI = TestPublicExtension.class)
    public void publicMethod4(boolean throwRuntimeException, boolean throwExRuntimeException)
    {
            if (throwRuntimeException) { throw new TestRuntimeException(); }
    }

    @Override
    public <M extends Trait> ExtendedTrait<M> getTrait(Class<? extends M> traitAPI)
    {
        return (ExtendedTrait<M>) testPublicTrait;
    }

}
