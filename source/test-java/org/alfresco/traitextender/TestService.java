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

import org.alfresco.traitextender.AJExtender;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;

public class TestService implements Extensible
{

    private String psmv2;

    private final ExtendedTrait<TestTrait> testTrait;

    public TestService(String psmv2)
    {
        super();
        this.testTrait = new ExtendedTrait<TestTrait>(AJProxyTrait.create(createTestTrait(),
                                                                          TestTrait.class));
        this.psmv2 = psmv2;
    }

    @Extend(traitAPI = TestTrait.class, extensionAPI = TestExtension.class)
    private String privateServiceMethod1(String s)
    {
        return "TestService.privateServiceMethod1(" + s + ")";
    }

    public String publicServiceMethod1(String s)
    {
        return privateServiceMethod1(s);
    }

    @Extend(traitAPI = TestTrait.class, extensionAPI = TestExtension.class)
    public String publicServiceMethod3(String s)
    {
        return "PSM3" + privateServiceMethod1(s);
    }

    @Extend(traitAPI = TestTrait.class, extensionAPI = TestExtension.class)
    public String publicServiceMethod2(String s)
    {
        return this.psmv2;
    }

    @Extend(traitAPI = TestTrait.class, extensionAPI = TestExtension.class)
    public void publicServiceMethod3(TestService s, List<Integer> traitIdentities)
    {
        traitIdentities.add(System.identityHashCode(this));
    }

    @Override
    public <M extends Trait> ExtendedTrait<M> getTrait(Class<? extends M> traitAPI)
    {
        return (ExtendedTrait<M>) testTrait;
    }

    private TestTrait createTestTrait()
    {
        return new TestTrait()
        {

            @Override
            public String traitImplOf_privateServiceMethod1(final String s)
            {
                return AJExtender.run(new AJExtender.ExtensionBypass<String>()
                {

                    @Override
                    public String run()
                    {
                        return privateServiceMethod1(s);
                    };
                });
            }

            @Override
            public String traitImplOf_publicServiceMethod2(final String s)
            {
                return AJExtender.run(new AJExtender.ExtensionBypass<String>()
                {

                    @Override
                    public String run()
                    {
                        return publicServiceMethod2(s);
                    };
                });
            }

            @Override
            public String publicServiceMethod3(final String s)
            {
                return publicServiceMethod3(s);
            }

            @Override
            public void traitImplOf_publicServiceMethod3(final TestService s, final List<Integer> traitIdentities)
            {
                AJExtender.run(new AJExtender.ExtensionBypass<Void>()
                {

                    @Override
                    public Void run()
                    {
                        TestService.this.publicServiceMethod3(s,
                                                              traitIdentities);
                        return null;
                    };
                });
            }

        };
    }
}
