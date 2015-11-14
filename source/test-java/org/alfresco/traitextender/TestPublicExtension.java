
package org.alfresco.traitextender;

public interface TestPublicExtension
{
    String publicMethod1(String s);

    String publicMethod2(String s);
    
    void publicMethod3(boolean throwException, boolean throwExException) throws TestException;
    
    void publicMethod4(boolean throwRuntimeException,boolean throwExRuntimeException);
}
