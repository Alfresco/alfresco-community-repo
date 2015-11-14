
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
