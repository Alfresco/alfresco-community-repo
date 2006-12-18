/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.jscript;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.policy.BaseBehaviour;
import org.alfresco.repo.policy.PolicyException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.util.ParameterCheck;


/**
 * JavaScript behaviour implementation
 * 
 * @author Roy Wetherall
 */
public class ScriptBehaviour extends BaseBehaviour
{	
	private ServiceRegistry serviceRegistry;
	
	private ScriptLocation location;
	
	public ScriptBehaviour()
	{
		super();
	}
	
    public ScriptBehaviour(ServiceRegistry serviceRegistry, ScriptLocation location)
    {
        this(serviceRegistry, location, NotificationFrequency.EVERY_EVENT);
    }

    public ScriptBehaviour(ServiceRegistry serviceRegistry, ScriptLocation location, NotificationFrequency frequency)
    {
    	super(frequency);
        ParameterCheck.mandatory("Location", location);
        ParameterCheck.mandatory("ServiceRegistry", serviceRegistry);
        this.location = location;
        this.serviceRegistry = serviceRegistry;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) 
    {
		this.serviceRegistry = serviceRegistry;
	}
    
    public void setLocation(ScriptLocation location) 
    {
		this.location = location;
	}


    @Override
    public String toString()
    {
        return "JavaScript behaviour[location = " + this.location.toString() + "]";
    }
    
    @SuppressWarnings("unchecked")
	public synchronized <T> T getInterface(Class<T> policy) 
	{
	    ParameterCheck.mandatory("Policy class", policy);
	    Object proxy = proxies.get(policy);
	    if (proxy == null)
	    {
	    	Method[] policyIFMethods = policy.getMethods();
	        if (policyIFMethods.length != 1)
	        {
	            throw new PolicyException("Policy interface " + policy.getCanonicalName() + " must have only one method");
	        }
	        
	        InvocationHandler handler = new JavaScriptInvocationHandler(this);
	        proxy = Proxy.newProxyInstance(policy.getClassLoader(), new Class[]{policy}, handler);
	        proxies.put(policy, proxy);
	    }
	    return (T)proxy;
	}   
    
    /**
     * JavaScript Invocation Handler
     * 
     * @author Roy Wetherall
     */
    private static class JavaScriptInvocationHandler implements InvocationHandler
    {
        private ScriptBehaviour behaviour;
        
        private JavaScriptInvocationHandler(ScriptBehaviour behaviour)
        {
            this.behaviour = behaviour;
        }

        /**
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
                    return invokeScript(method, args);
                }
                finally
                {
                    behaviour.enable();
                }
            }
            return null;
        }

        private Object invokeScript(Method method, Object[] args) 
        {
        	// Build the model
        	Map<String, Object> model = new HashMap<String, Object>(1);
        	model.put("behaviour", new org.alfresco.repo.jscript.Behaviour(this.behaviour.serviceRegistry, method.getName(), args));
        	
        	// Execute the script
        	return this.behaviour.serviceRegistry.getScriptService().executeScript(this.behaviour.location, model);
		}

		@Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            else if (obj == null || !(obj instanceof JavaScriptInvocationHandler))
            {
                return false;
            }
            JavaScriptInvocationHandler other = (JavaScriptInvocationHandler)obj;
            return  behaviour.location.equals(other.behaviour.location);
        }

        @Override
        public int hashCode()
        {
            return 37 * behaviour.location.hashCode();
        }

        @Override
        public String toString()
        {
            return "JavaScriptBehaviour[location=" + behaviour.location.toString() + "]";
        }
    }  
}
