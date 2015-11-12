
package org.alfresco.traitextender;

import org.alfresco.traitextender.AJExtender;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtensionTargetException;
import org.alfresco.traitextender.RouteExtensions;

public aspect RunTestExtensions
{
    declare precedence: RunTestExtensions,RouteExtensions;

    pointcut throwsTestException(Extend meAnnotation):execution(@org.alfresco.traitextender.Extend * *(..) throws TestException) && (@annotation(meAnnotation));

    after(Extend meAnnotation)  throwing(ExtensionTargetException ete) throws TestException :throwsTestException(meAnnotation){
        Throwable exception = AJExtender.asCheckThrowable(ete.getCause(), TestException.class);
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
