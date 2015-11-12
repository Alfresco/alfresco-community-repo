package org.alfresco.traitextender;

import java.util.List;

public interface TestExtension
{
    String privateServiceMethod1(String s);
    
    String publicServiceMethod2(String s);
    
    String publicServiceMethod3(String s);

    void publicServiceMethod3(TestService s,List<Integer> traitIdentities);
}
