package org.alfresco.traitextender;

import java.util.List;

import org.alfresco.traitextender.Trait;


public interface TestTrait extends Trait
{
    String traitImplOf_privateServiceMethod1(String s);
    
    String traitImplOf_publicServiceMethod2(String s);
    
    String publicServiceMethod3(String s);
    
    void traitImplOf_publicServiceMethod3(TestService s,List<Integer> traitIdentities);
}
