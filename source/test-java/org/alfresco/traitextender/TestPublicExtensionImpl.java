
package org.alfresco.traitextender;

import org.alfresco.traitextender.InstanceExtension;

public class TestPublicExtensionImpl extends InstanceExtension<TestPublicExtension,TestPublicTrait> implements TestPublicExtension
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
        if (throwExRuntimeException) { throw new TestRuntimeException(); }
        else { trait.publicMethod4(throwRuntimeException, throwExRuntimeException);}
    }

}
