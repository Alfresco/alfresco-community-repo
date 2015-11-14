
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
