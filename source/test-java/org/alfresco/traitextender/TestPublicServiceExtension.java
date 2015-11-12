package org.alfresco.traitextender;

public class TestPublicServiceExtension extends TestPublicService
{
    @Override
    public String publicMethod1(String s)
    {
        return "X"+super.publicMethod1(s);
    }
    
    @Override
    public String publicMethod2(String s)
    {
        publicMethod1(s);
        return "X"+super.publicMethod2(s);
    }
}
