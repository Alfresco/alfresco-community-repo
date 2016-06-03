package org.alfresco.repo.audit;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Identify a bean upon which a method invocation has been made. Originally
 * this was only public services but has been relaxed to be any bean.
 *  
 * @author Andy Hind, David Ward, Alan Davis
 */
public interface BeanIdentifier
{
    /**
     * Get the name of the bean (normally a service) for the method invocation.
     * 
     * @param mi                the method invocation 
     * @return                  Returns the name of the bean or <tt>null</tt> if it is
     *                          not recognized
     */
    public String getBeanName(MethodInvocation mi);
}
