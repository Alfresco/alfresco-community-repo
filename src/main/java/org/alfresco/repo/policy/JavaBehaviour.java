/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.policy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.alfresco.api.AlfrescoPublicApi;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Java based Behaviour.
 * 
 * A behavior acts like a delegate (a method pointer).  The pointer is
 * represented by an instance object and method name.
 * 
 * @author David Caruana
 *
 */
@AlfrescoPublicApi
public class JavaBehaviour extends BaseBehaviour
{
    // The object instance holding the method
    Object instance;
    
    // The method name
    String method;

    /**
     * Construct.
     * 
     * @param instance  the object instance holding the method
     * @param method  the method name
     */
    public JavaBehaviour(Object instance, String method)
    {
        this(instance, method, NotificationFrequency.EVERY_EVENT);
    }

    /**
     * Construct.
     * 
     * @param instance  the object instance holding the method
     * @param method  the method name
     */
    public JavaBehaviour(Object instance, String method, NotificationFrequency frequency)
    {
    	super(frequency);
    	ParameterCheck.mandatory("Instance", instance);
        ParameterCheck.mandatory("Method", method);
        this.method = method;
        this.instance = instance;
    }


    @Override
    public String toString()
    {
        return "Java method[class=" + instance.getClass().getName() + ", method=" + method + "]";
    }
    
    @SuppressWarnings("unchecked")
	public synchronized <T> T getInterface(Class<T> policy) 
	{
	    ParameterCheck.mandatory("Policy class", policy);
	    Object proxy = proxies.get(policy);
	    if (proxy == null)
	    {
	        InvocationHandler handler = getInvocationHandler(instance, method, policy);
	        proxy = Proxy.newProxyInstance(policy.getClassLoader(), new Class[]{policy}, handler);
	        proxies.put(policy, proxy);
	    }
	    return (T)proxy;
	}

    /**
     * Gets the Invocation Handler.
     * 
     * @param <T>  the policy interface class
     * @param instance  the object instance
     * @param method  the method name
     * @param policyIF  the policy interface class  
     * @return  the invocation handler
     */
    <T> InvocationHandler getInvocationHandler(Object instance, String method, Class<T> policyIF)
    {
        Method[] policyIFMethods = policyIF.getMethods();
        if (policyIFMethods.length != 1)
        {
            throw new PolicyException("Policy interface " + policyIF.getCanonicalName() + " must have only one method");
        }

        try
        {
            Class instanceClass = instance.getClass();
            Method delegateMethod = instanceClass.getMethod(method, (Class[])policyIFMethods[0].getParameterTypes());
            return new JavaMethodInvocationHandler(this, delegateMethod);
        }
        catch (NoSuchMethodException e)
        {
            throw new PolicyException("Method " + method + " not found or accessible on " + instance.getClass(), e);
        }
    }    
    
    /**
     * Java Method Invocation Handler
     * 
     * @author David Caruana
     */
    private static class JavaMethodInvocationHandler implements InvocationHandler
    {
        private JavaBehaviour behaviour;
        private Method delegateMethod;
        
        /**
         * Constuct.
         * 
         * @param behaviour  the java behaviour
         * @param delegateMethod  the method to invoke
         */
        private JavaMethodInvocationHandler(JavaBehaviour behaviour, Method delegateMethod)
        {
            this.behaviour = behaviour;
            this.delegateMethod = delegateMethod;
        }

        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            // Handle Object level methods
            if (method.getName().equals("toString"))
            {
                return toString();
            }
            else if (method.getName().equals("hashCode"))
            {
                return hashCode();
            }
            else if (method.getName().equals("equals"))
            {
                if (Proxy.isProxyClass(args[0].getClass()))
                {
                    return equals(Proxy.getInvocationHandler(args[0]));
                }
                return false;
            }
            
            // Delegate to designated method pointer
            if (behaviour.isEnabled())
            {
                try
                {
                    behaviour.disable();
                    return delegateMethod.invoke(behaviour.instance, args);
                }
                catch (InvocationTargetException e)
                {
                    throw e.getTargetException();
                }
                finally
                {
                    behaviour.enable();
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            else if (obj == null || !(obj instanceof JavaMethodInvocationHandler))
            {
                return false;
            }
            JavaMethodInvocationHandler other = (JavaMethodInvocationHandler)obj;
            return behaviour.instance.equals(other.behaviour.instance) && delegateMethod.equals(other.delegateMethod);
        }

        @Override
        public int hashCode()
        {
            return 37 * behaviour.instance.hashCode() + delegateMethod.hashCode();
        }

        @Override
        public String toString()
        {
            return "JavaBehaviour[instance=" + behaviour.instance.hashCode() + ", method=" + delegateMethod.toString() + "]";
        }
    }
    
}
