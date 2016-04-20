package org.alfresco.opencmis;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor to manage threads and perform other menial jobs that are common to all
 * calls made to the service.  It also provides detailed logging of values passing
 * in and out of the service.
 * <p/>
 * <b>DEBUG</b> shows authentication and inbound arguments.  <b>TRACE</b> shows full
 * return results as well.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AlfrescoCmisServiceInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(AlfrescoCmisServiceInterceptor.class);

    public AlfrescoCmisServiceInterceptor()
    {
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
    	String methodName = invocation.getMethod().getName();
    	Object[] args = invocation.getArguments();

        // Keep note of whether debug is required
        boolean debug = logger.isDebugEnabled();
        boolean trace = logger.isTraceEnabled();
        StringBuilder sb = null;
        if (debug || trace)
        {
            sb = new StringBuilder("\n" +
                        "CMIS invocation:         \n" +
                        "   Method:                 " + methodName + "\n" +
                        "   Arguments:            \n");
            for (Object arg : args)
            {
                sb.append("      ").append(arg).append("\n");
            }
        }

        Object ret = null;
        AlfrescoCmisService service = (AlfrescoCmisService) invocation.getThis();

        try
        {
            // Wrap with pre- and post-method calls
            try
            {
                if(debug || trace)
                {
                    sb.append(
                            "   Pre-call authentication: \n" +
                            "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                            "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
                }

                if(!methodName.equalsIgnoreCase("close"))
                {
                    service.beforeCall();
                }

                if(debug || trace)
                {
                    sb.append(
                            "   In-call authentication: \n" +
                            "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                            "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
                }

                FileFilterMode.setClient(Client.cmis);

                ret = invocation.proceed();
            }
            finally
            {
            	FileFilterMode.clearClient();

                if(!methodName.equalsIgnoreCase("close"))
                {
                    service.afterCall();
                }

                if(debug || trace)
                {
                    sb.append(
                            "   Post-call authentication: \n" +
                            "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                            "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
                }
            }
            if (trace)
            {
                sb.append(
                        "   Returning:              ").append(ret).append("\n");
                logger.debug(sb);
            }
            // Done
            return ret;
        }
        catch (Throwable e)
        {
            if (debug)
            {
                sb.append("   Throwing:             " + e.getMessage());
                logger.debug(sb, e);
            }
            // Rethrow
            throw e;
        }
    }
}
