
package org.alfresco.traitextender;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("org.alfresco.traitextender.RouteTestExtesnions,org.alfresco.traitextender.RouteExtensions")
public class RouteTestExtesnions
{
    @AfterThrowing(pointcut = "execution(@org.alfresco.traitextender.Extend * *(..) throws TestException) && (@annotation(extendAnnotation))", throwing = "ete")
    public void intercept(Extend extendAnnotation, ExtensionTargetException ete) throws TestException
    {
        Throwable exception = AJExtender.asCheckThrowable(ete.getCause(),
                                                          TestException.class);
        if (exception instanceof TestException)
        {
            throw (TestException) exception;
        }
        else
        {
            throw ete;
        }
    }
}
