package org.alfresco.traitextender;

public class TestServiceExtension extends TestService
{

    public TestServiceExtension(String psmv2)
    {
        super(psmv2);
    }

    @Override
    public String publicServiceMethod3(String s)
    {
        return "x"+super.publicServiceMethod3(s);
    }
}
