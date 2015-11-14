
package org.alfresco.traitextender;

import java.util.List;

import org.alfresco.traitextender.SingletonExtension;

public class TestSingletonExtensionImpl extends SingletonExtension<TestExtension, TestTrait> implements TestExtension
{

    private String singletonId;

    public TestSingletonExtensionImpl(String singletonId)
    {
        super(TestTrait.class);
        this.singletonId = singletonId;
    }

    @Override
    public String privateServiceMethod1(String s)
    {
        return getTrait().traitImplOf_privateServiceMethod1(s) + " TestSingletonExtensionImpl.privateServiceMethod1("
                    + s + ")@" + singletonId;
    }

    @Override
    public String publicServiceMethod2(String s)
    {
        return getTrait().traitImplOf_publicServiceMethod2(s) + " TestSingletonExtensionImpl.publicServiceMethod2(" + s
                    + ")@" + singletonId;
    }

    @Override
    public String publicServiceMethod3(String s)
    {
        return getTrait().publicServiceMethod3(s) + " TestSingletonExtensionImpl.publicServiceMethod3(" + s
                    + ")@" + singletonId;
    }

    @Override
    public void publicServiceMethod3(TestService s, List<Integer> traitIdentities)
    {
        traitIdentities.add(System.identityHashCode(getTrait()));
        if (s != null)
        {
            s.publicServiceMethod3(null,
                                   traitIdentities);
        }
        traitIdentities.add(System.identityHashCode(getTrait()));
        getTrait().traitImplOf_publicServiceMethod3(s,
                                                    traitIdentities);
    }

}
