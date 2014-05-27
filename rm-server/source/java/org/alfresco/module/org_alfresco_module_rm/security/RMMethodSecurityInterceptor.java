
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.acegisecurity.AccessDeniedException;
import net.sf.acegisecurity.intercept.InterceptorStatusToken;
import net.sf.acegisecurity.intercept.method.aopalliance.MethodSecurityInterceptor;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

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
    protected static Log logger = LogFactory.getLog(RMMethodSecurityInterceptor.class);

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
     * Used to getnerate the capability error report.
     */
    private static final ThreadLocal<Map<String, CapabilityReport>> capabilities = new ThreadLocal<Map<String, CapabilityReport>>()
    {
        @Override
        protected Map<String, CapabilityReport> initialValue()
        {
            return new HashMap<String, CapabilityReport>();
        };
    };

    /**
     * Get capability report object from the thread local, creating one for
     * the given capability name if one does not already exist.
     *
     * @param name  capability name
     * @return {@link CapabilityReport} object containing information about the capability
     */
    private static final CapabilityReport getCapabilityReport(String name)
    {
        Map<String, CapabilityReport> map = RMMethodSecurityInterceptor.capabilities.get();
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
     * Report capability status.
     *
     * @param name      capability name
     * @param status    capability status
     */
    public static void reportCapabilityStatus(String name, int status)
    {
        if (logger.isDebugEnabled())
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
        if (logger.isDebugEnabled())
        {
            CapabilityReport capability = getCapabilityReport(name);
            if (expected == false)
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

        if (logger.isDebugEnabled())
        {
            Collection<CapabilityReport> capabilities = RMMethodSecurityInterceptor.capabilities.get().values();

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
                            if (entry.getValue() == true)
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
            RMMethodSecurityInterceptor.capabilities.remove();

            result = super.beforeInvocation(object);
        }
        catch (AccessDeniedException exception)
        {
            String failureReport = getFailureReport();
            if (failureReport == null)
            {
                throw exception;
            }
            else
            {
                // rethrow with additional information
                throw new AccessDeniedException(exception.getMessage() + getFailureReport(), exception);
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
