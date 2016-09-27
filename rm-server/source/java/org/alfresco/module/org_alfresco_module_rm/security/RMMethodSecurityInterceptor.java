
package org.alfresco.module.org_alfresco_module_rm.security;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.acegisecurity.AccessDeniedException;
import net.sf.acegisecurity.intercept.InterceptorStatusToken;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.service.cmr.security.AccessStatus;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Records Management Method Security Interceptor.
 * <p>
 * Provides a way to record information about the capabilities being executed and report
 * when an access denied exception is thrown.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RMMethodSecurityInterceptor extends MethodSecurityInterceptor
{
    /** logger */
    protected static final Log LOGGER = LogFactory.getLog(RMMethodSecurityInterceptor.class);

    /**
     * Helper class to hold capability report information
     */
    private static class CapabilityReport
    {
        public String name;
        public AccessStatus status;
        public Map<String, Boolean> conditions = new HashMap<String, Boolean>();
    }

    /**
     * Helper method to translate vote to access status.
     *
     * @param vote  vote
     * @return {@link AccessStatus} access status
     */
    private static AccessStatus translate(int vote)
    {
        switch (vote)
        {
            case AccessDecisionVoter.ACCESS_ABSTAIN:
                return AccessStatus.UNDETERMINED;
            case AccessDecisionVoter.ACCESS_GRANTED:
                return AccessStatus.ALLOWED;
            case AccessDecisionVoter.ACCESS_DENIED:
                return AccessStatus.DENIED;
            default:
                return AccessStatus.UNDETERMINED;
        }
    }

    /**
     * Current capability report details.
     * <p>
     * Used to generate the capability error report.
     */
    private static final ThreadLocal<Map<String, CapabilityReport>> CAPABILITIES = new ThreadLocal<Map<String, CapabilityReport>>()
    {
        @Override
        protected Map<String, CapabilityReport> initialValue()
        {
            return new HashMap<String, CapabilityReport>();
        };
    };

    /**
     * Indicates whether this is an RM security check or not
     */
    private static final ThreadLocal<Boolean> IS_RM_SECURITY_CHECK = new ThreadLocal<Boolean>()
    {
        protected Boolean initialValue() {return false;};
    };
    
    /**
     * Messages to display in error report.
     */
    private static final ThreadLocal<List<String>> MESSAGES = new ThreadLocal<List<String>>()
    {
        protected List<String> initialValue() {return new ArrayList<String>();};
    };
    
    /**
     * Get capability report object from the thread local, creating one for
     * the given capability name if one does not already exist.
     *
     * @param name  capability name
     * @return {@link CapabilityReport} object containing information about the capability
     */
    private static CapabilityReport getCapabilityReport(String name)
    {
        Map<String, CapabilityReport> map = RMMethodSecurityInterceptor.CAPABILITIES.get();
        CapabilityReport capability = map.get(name);
        if (capability == null)
        {
            capability = new CapabilityReport();
            capability.name = name;

            map.put(name, capability);
        }
        return capability;
    }

    /**
     * Indicates whether this is a RM security check or not
     * 
     * @param newValue  true if RM security check, false otherwise
     */
    public static void isRMSecurityChecked(boolean newValue)
    {
        if (LOGGER.isDebugEnabled())
        {
            RMMethodSecurityInterceptor.IS_RM_SECURITY_CHECK.set(newValue);
        }
    }
    
    /**
     * Add a message to be displayed in the error report.
     * 
     * @param message   error message
     */
    public static void addMessage(String message)
    {
        if (LOGGER.isDebugEnabled())
        {
            List<String> messages = RMMethodSecurityInterceptor.MESSAGES.get();
            messages.add(message);
        }
    }
    
    public static void addMessage(String message, Object ... params)
    {
        if (LOGGER.isDebugEnabled())
        {
            addMessage(MessageFormat.format(message, params));
        }
    }
    
    /**
     * Report capability status.
     *
     * @param name      capability name
     * @param status    capability status
     */
    public static void reportCapabilityStatus(String name, int status)
    {
        if (LOGGER.isDebugEnabled())
        {
            CapabilityReport capability = getCapabilityReport(name);
            capability.status = translate(status);
        }
    }

    /**
     * Report capability condition.
     *
     * @param name              capability name
     * @param conditionName     capability condition name
     * @param expected          expected value
     * @param actual            actual value
     */
    public static void reportCapabilityCondition(String name, String conditionName, boolean expected, boolean actual)
    {
        if (LOGGER.isDebugEnabled())
        {
            CapabilityReport capability = getCapabilityReport(name);
            if (!expected)
            {
                conditionName = "!" + conditionName;
            }
            capability.conditions.put(conditionName, (expected == actual));
        }
    }

    /**
     * Gets the failure report for the currently recorded capabilities.
     *
     * @return  {@link String}  capability error report
     */
    public String getFailureReport()
    {
        String result = null;

        if (LOGGER.isDebugEnabled())
        {
            Collection<CapabilityReport> capabilities = RMMethodSecurityInterceptor.CAPABILITIES.get().values();

            if (!capabilities.isEmpty())
            {
                StringBuffer buffer = new StringBuffer("\n");
                for (CapabilityReport capability : capabilities)
                {
                    buffer.append("  ").append(capability.name).append(" (").append(capability.status).append(")\n");
                    if (!capability.conditions.isEmpty())
                    {
                        for (Map.Entry<String, Boolean> entry : capability.conditions.entrySet())
                        {
                            buffer.append("    - ").append(entry.getKey()).append(" (");
                            if (entry.getValue())
                            {
                                buffer.append("passed");
                            }
                            else
                            {
                                buffer.append("failed");
                            }
                            buffer.append(")\n");
                        }
                    }
                }

                result = buffer.toString();
            }
        }

        return result;
    }

    /**
     * @see net.sf.acegisecurity.intercept.AbstractSecurityInterceptor#beforeInvocation(java.lang.Object)
     */
    @Override
    protected InterceptorStatusToken beforeInvocation(Object object)
    {
        InterceptorStatusToken result = null;
        try
        {
            // clear the capability report information
            RMMethodSecurityInterceptor.CAPABILITIES.remove();
            RMMethodSecurityInterceptor.IS_RM_SECURITY_CHECK.remove();
            RMMethodSecurityInterceptor.MESSAGES.remove();

            // before invocation (where method security check takes place)
            result = super.beforeInvocation(object);
        }
        catch (AccessDeniedException exception)
        {
            if (LOGGER.isDebugEnabled())
            {
                MethodInvocation mi = (MethodInvocation)object;
                
                StringBuilder methodDetails = new StringBuilder("\n");
                if (RMMethodSecurityInterceptor.IS_RM_SECURITY_CHECK.get())
                {
                    methodDetails.append("RM method security check was performed.\n");
                }
                else
                {
                    methodDetails.append("Standard DM method security check was performed.\n");                    
                }
                
                boolean first = true;
                methodDetails.append("Failed on method:  ").append(mi.getMethod().getName()).append("(");
                for (Object arg : mi.getArguments())
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        methodDetails.append(", ");
                    }
                    
                    if (arg != null)
                    {
                        methodDetails.append(arg.toString());
                    }
                    else
                    {
                        methodDetails.append("null");
                    }
                }
                methodDetails.append(")\n");
                
                List<String> messages = RMMethodSecurityInterceptor.MESSAGES.get();
                for (String message : messages)
                {
                    methodDetails.append(message).append("\n");
                }
                
                String failureReport = getFailureReport();
                if (failureReport == null)
                {
                    // rethrow with additional information
                    throw new AccessDeniedException(exception.getMessage() + methodDetails, exception);
                }
                else
                {
                    // rethrow with additional information
                    throw new AccessDeniedException(exception.getMessage() + methodDetails + getFailureReport(), exception);
                }
            }
            else
            {
                throw exception;
            }
        }
        return result;
    }

    /**
     * @see net.sf.acegisecurity.intercept.method.aopalliance.MethodSecurityInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        Object result = null;
        InterceptorStatusToken token = beforeInvocation(mi);

        try
        {
            result = mi.proceed();
        }
        finally
        {
            result = super.afterInvocation(token, result);
        }

        return result;
    }
}
