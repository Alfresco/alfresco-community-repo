/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.audit;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import net.sf.acegisecurity.Authentication;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.StackTraceUtil;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.transaction.TransactionService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * A method interceptor to wrap method invocations with auditing.
 * <p/>
 * <b><u>V3.2 Configuration</u>:</b>
 * As of V3.2, the pre- and post-invocation values are passed to the audit component
 * for processing.  Individual applications have to extract the desired audit values.
 * Values are audited before and after the invocation so that applications that desire
 * to extract derived data before the invocation can have a chance to do so; generally,
 * however, the post-invocation values will be the most useful.
 * <p/>
 * The values passed to the audit component (assuming auditing is enabled and the
 * new configuration is being used) are:
 * <pre>
 * /alfresco-api
 *    /pre
 *       /&lt;service&gt;
 *          /&lt;method&gt;
 *             /args
 *                /&lt;arg-name&gt;=&lt;value&gt;
 *                /&lt;arg-name&gt;=&lt;value&gt;
 *                ...
 *       /service
 *    /post
 *       /&lt;service&gt;
 *          /&lt;method&gt;
 *             /args
 *                /&lt;arg-name&gt;=&lt;value&gt;
 *                /&lt;arg-name&gt;=&lt;value&gt;
 *                ...
 *             /result=&lt;value&gt;
 *             /error=&lt;value&gt;
 *             /no-error=&lt;null&gt;
 * 
 * </pre>
 * Applications can remap the paths onto their configurations as appropriate.
 * <p/>
 * TODO: Audit configuration mapping needs to support conditionals
 * 
 * @author Andy Hind
 * @author Derek Hulley
 */
public class AuditMethodInterceptor implements MethodInterceptor
{
    public static final String AUDIT_PATH_API_ROOT = "/alfresco-api";
    public static final String AUDIT_PATH_API_PRE = AUDIT_PATH_API_ROOT + "/pre";
    public static final String AUDIT_PATH_API_POST = AUDIT_PATH_API_ROOT + "/post";
    public static final String AUDIT_SNIPPET_ARGS = "/args";
    public static final String AUDIT_SNIPPET_PRE_CALL_DATA = "/preCallData";
    public static final String AUDIT_SNIPPET_RESULT = "/result";
    public static final String AUDIT_SNIPPET_ERROR = "/error";
    public static final String AUDIT_SNIPPET_NO_ERROR = "/no-error";
    
    private static final String NOT_RECORDABLE = "not auditable";
    
    @SuppressWarnings("rawtypes")
    private static final Class[] NON_RECORDABLE_CLASSES = new Class[] { InputStream.class,  OutputStream.class };
    
    private static final Log logger = LogFactory.getLog(AuditMethodInterceptor.class);

    private BeanIdentifier beanIdentifier;
    private AuditComponent auditComponent;
    private TransactionService transactionService;
    
    private ThreadPoolExecutor threadPoolExecutor;

    private final ThreadLocal<Boolean> inAudit = new ThreadLocal<Boolean>();
    
    public AuditMethodInterceptor()
    {
        super();
    }
    
    /**
     * Use the new audit configuration (default: <b>false</b>)
     * 
     * @param useNewConfig              <tt>true</tt> to use the new audit configuration
     */
    public void setUseNewConfig(boolean useNewConfig)
    {
        logger.warn("Property 'useNewConfig' is no longer used.");
    }

    public void setBeanIdentifier(BeanIdentifier beanIdentifier)
    {
        this.beanIdentifier = beanIdentifier;
    }

    public void setAuditComponent(AuditComponent auditComponent)
    {
        this.auditComponent = auditComponent;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)
    {
        this.threadPoolExecutor = threadPoolExecutor;
    }
    
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        // Shortcut if no audit values are required
        if(!auditComponent.areAuditValuesRequired(AUDIT_PATH_API_ROOT))
        {
            return mi.proceed();
        }
        else
        {
            // New configuration will be used
            return proceed(mi);
        }
    }
    
    /**
     * Allow the given method invocation to proceed, auditing values before invocation and
     * after returning or throwing.
     * 
     * @param mi                the invocation
     * @return                  Returns the method return (if a value is not thrown)
     * @throws Throwable        rethrows any exception generated by the invocation
     * 
     * @since 3.2
     */
    private Object proceed(MethodInvocation mi) throws Throwable
    {
        // Are we in a nested audit?
        Boolean wasInAudit = inAudit.get();
        try
        {
            // If we are already in a nested audit call, there is nothing to do
            if (Boolean.TRUE.equals(wasInAudit))
            {
                return mi.proceed();
            }

            Auditable auditableDef = mi.getMethod().getAnnotation(Auditable.class);
            if (auditableDef == null)
            {
                // No annotation, so just continue as normal
                return mi.proceed();
            }
            
            // First get the argument map, if present
            Object[] args = mi.getArguments();
            Map<String, Serializable> namedArguments = getInvocationArguments(auditableDef, args);
            // Get the service name
            String serviceName = beanIdentifier.getBeanName(mi);
            if (serviceName == null)
            {
                // Not a public service
                return mi.proceed();
            }
            String methodName = mi.getMethod().getName();
            
            return proceedWithAudit(mi, auditableDef, serviceName, methodName, namedArguments);
        }
        finally
        {
            inAudit.set(wasInAudit);                       
        }
    }
    
    private Object proceedWithAudit(
            MethodInvocation mi,
            Auditable auditableDef,
            String serviceName,
            String methodName,
            Map<String, Serializable> namedArguments) throws Throwable
    {
        Boolean wasInAudit = inAudit.get();
        Map<String, Serializable> preAuditedData = null;
        // Record that we have entered an audit method
        inAudit.set(Boolean.TRUE);
        try
        {
            preAuditedData = auditInvocationBefore(serviceName, methodName, namedArguments);
        }
        catch (Throwable e)
        {
            // Failure to audit must stop the process: ALF-11139
            throw new AlfrescoRuntimeException(
                    "Failed to audit pre-invocation: \n" +
                    "   Invocation: " + mi,
                    e);
        }
        finally
        {
            inAudit.set(wasInAudit);
        }        
        // Execute the call
        Object ret = null;
        Throwable thrown = null;
        try
        {
            ret = mi.proceed();
        }
        catch (Throwable e)
        {
            thrown = e;
        }
        
        // We don't ALWAYS want to record the return value
        Object auditRet = auditableDef.recordReturnedObject() ? ret : null;
        // Record that we have entered an audit method
        inAudit.set(Boolean.TRUE);
        try
        {
            auditInvocationAfter(serviceName, methodName, namedArguments, auditRet, thrown, preAuditedData);
        }
        catch (Throwable e)
        {
            // Failure to audit must stop the process: ALF-11139
            throw new AlfrescoRuntimeException(
                    "Failed to audit post-invocation: \n" +
                    "   Invocation: " + mi,
                    e);
        }
        finally
        {
            inAudit.set(wasInAudit);
        }        
        // Done
        if (thrown != null)
        {
            throw thrown;
        }
        else
        {
            return ret;
        }
    }
    
    /**
     * @return                  Returns the arguments mapped by name
     * 
     * @since 3.2
     */
    private Map<String, Serializable> getInvocationArguments(Auditable auditableDef, Object[] args)
    {
        // Use the annotation to name the arguments
        String[] params = auditableDef.parameters();
        boolean[] recordable = auditableDef.recordable();
        
        Map<String, Serializable> namedArgs = new HashMap<String, Serializable>(args.length * 2);
        for (int i = 0; i < args.length; i++)
        {
            if (i >= params.length)
            {
                // The name list is finished.  Unnamed arguments are not recorded.
                break;
            }
            if (i < recordable.length)
            {
                // Arguments are recordable by default
                if (!recordable[i])
                {
                    // Don't record the argument
                    continue;
                }
            }
            Serializable value = getRecordableValue(args[i]);
            if (value == NOT_RECORDABLE)
            {
                // No viable conversion
                continue;
            }

            // It is named and recordable
            namedArgs.put(params[i], value);
        }
        // Done
        return namedArgs;
    }
    
    /**
     * Returns a value that may be included in audit map data. Called for method arguments and return
     * values. Not all values may be audited. Some cannot be converted to something that can be
     * serialised or may only be read once (so would be damaged). String audit values may be trimmed.
     * @param value to be audited.
     * @return Return {@link NOT_AUDITABLE} if it is not possible to audit the value,a value that may be audited. May be null.
     * @throws TypeConversionException if not possible to convert the original object.
     */
    private Serializable getRecordableValue(Object value)
    {
        if (value == null)
        {
            return null;
        }

        Serializable audit;
        if (instanceofNonAuditClass(value))
        {
            // We will not be recording it
            audit = NOT_RECORDABLE;
        }
        else
        {
            if(value instanceof List)
            {
                List valueList = (List) value;
                List<Serializable> recordableList = new ArrayList<Serializable>();
                for (Object valueListItem : valueList)
                {
                    recordableList.add(getRecordableValue(valueListItem));
                }
                audit = (Serializable) recordableList;
            }
            // TODO we won't bother with Maps, yet.
            else if (value instanceof Serializable)
            {
                audit = (Serializable) value;
            }
            else
            {
                try
                {
                    audit = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                }
                catch (TypeConversionException e)
                {
                    return NOT_RECORDABLE;
                }
            }
            // Trim strings
            if (audit instanceof String)
            {
                audit = SchemaBootstrap.trimStringForTextFields((String) audit);
            }
            // trim MLText
            else if (audit instanceof MLText)
            {
                MLText mltext = (MLText) audit;
                Set<Locale> locales = mltext.getLocales();
                for (Locale locale : locales)
                {
                    mltext.put(locale, SchemaBootstrap.trimStringForTextFields(mltext.getValue(locale)));
                }
            }
        }
        return audit;
    }
    
    // Returns a non null value if the supplied value is a non auditable class.
    private boolean instanceofNonAuditClass(Object value)
    {
        for (@SuppressWarnings("rawtypes") Class clazz: NON_RECORDABLE_CLASSES)
        {
            if (clazz.isInstance(value))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Audit values before the invocation
     * 
     * @param serviceName       the service name
     * @param methodName        the method name
     * @param namedArguments    the named arguments passed to the invocation
     * 
     * @since 3.2
     */
    private Map<String, Serializable> auditInvocationBefore(
            final String serviceName,
            final String methodName,
            final Map<String, Serializable> namedArguments)
    {
        String rootPath;
        Map<String, Serializable> auditData;
        if (namedArguments == null || namedArguments.isEmpty())
        {
            rootPath = AuditApplication.buildPath(AUDIT_PATH_API_PRE, serviceName, methodName);
            auditData = new HashMap<String, Serializable>(1);
            auditData.put(AUDIT_SNIPPET_ARGS, null);
        }
        else
        {
            rootPath = AuditApplication.buildPath(AUDIT_PATH_API_PRE, serviceName, methodName, AUDIT_SNIPPET_ARGS);
            auditData = namedArguments;
        }
        
        // Audit in a read-write txn
        Map<String, Serializable> auditedData = auditComponent.recordAuditValues(rootPath, auditData);
        // Done
        if (logger.isDebugEnabled() && auditedData.size() > 0)
        {
            logger.debug(
                    "Audited before invocation: \n" +
                    "   Values: " + auditedData);
        }
        return auditedData;
    }
    
    /**
     * Audit values after the invocation
     * 
     * @param serviceName       the service name
     * @param methodName        the method name
     * @param namedArguments    the named arguments passed to the invocation
     * @param ret               the result of the execution (may be <tt>null</tt>)
     * @param thrown            the error thrown by the invocation (may be <tt>null</tt>)
     * @param preAuditedData    the audited data from before the method call.
     * 
     * @since 3.2
     */
    private void auditInvocationAfter(
            String serviceName, String methodName, Map<String, Serializable> namedArguments,
            Object ret, final Throwable thrown, Map<String, Serializable> preAuditedData)
    {
        final String rootPath = AuditApplication.buildPath(AUDIT_PATH_API_POST, serviceName, methodName);
        
        final Map<String, Serializable> auditData = new HashMap<String, Serializable>(23);
        if (namedArguments.isEmpty())
        {
            auditData.put(AUDIT_SNIPPET_ARGS, null);
        }
        else
        {
            for (Map.Entry<String, Serializable> entry : namedArguments.entrySet())
            {
                String argName = entry.getKey();
                Serializable argValue = entry.getValue();
                auditData.put(
                        AuditApplication.buildPath(AUDIT_SNIPPET_ARGS, argName),
                        argValue);
            }
        }
        
        // Add audited data prior to the method call, so it may be repeated
        for (Entry<String, Serializable> entry: preAuditedData.entrySet())
        {
            String path = AuditApplication.buildPath(AUDIT_SNIPPET_PRE_CALL_DATA, entry.getKey());
            auditData.put(path, entry.getValue());
        }
        
        Serializable value = getRecordableValue(ret);
        if (value != null && value != NOT_RECORDABLE)
        {
            auditData.put(AUDIT_SNIPPET_RESULT, value);
        }

        if (thrown != null)
        {
        	// ALF-3055: an exception has occurred - make sure the audit occurs in a new thread
        	//     rather than a nested transaction to avoid contention for the same audit table
            // ALF-12638: ensure that the new thread context matches the current thread context
            final Authentication authContext = AuthenticationUtil.getFullAuthentication();
            threadPoolExecutor.execute(new Runnable()
            {
				public void run()
				{
			        Map<String, Serializable> auditedData;

		            StringBuilder sb = new StringBuilder(1024);
		            StackTraceUtil.buildStackTrace(
		                    thrown.getMessage(), thrown.getStackTrace(), sb, Integer.MAX_VALUE);
		            auditData.put(AUDIT_SNIPPET_ERROR, SchemaBootstrap.trimStringForTextFields(sb.toString()));

		            // Ensure we have a transaction
		            RetryingTransactionCallback<Map<String, Serializable>> auditCallback =
		                    new RetryingTransactionCallback<Map<String, Serializable>>()
		            {
		                public Map<String, Serializable> execute() throws Throwable
		                {
                            // Record thrown exceptions regardless of userFilter in case of failed authentication
                            // see MNT-11760
                            if (thrown instanceof AuthenticationException)
                            {
                                  return auditComponent.recordAuditValuesWithUserFilter(rootPath, auditData, false);
                            }
                            else
                            {
                                  return auditComponent.recordAuditValues(rootPath, auditData);
                            }

		                }
		            };
		            try
		            {
                        AuthenticationUtil.setFullAuthentication(authContext);
		                auditedData = transactionService.getRetryingTransactionHelper().doInTransaction(auditCallback, false, true);
		            }
		            finally
		            {
		                AuthenticationUtil.clearCurrentSecurityContext();
		            }
		            
			        // Done
			        if (logger.isDebugEnabled() && auditedData.size() > 0)
			        {
			            logger.debug(
			                    "Audited after invocation: \n" +
			                    (thrown == null ? "" : "   Exception: " + thrown.getMessage() + "\n") +
			                    "   Values: " + auditedData);
			        }
				}
            });
        }
        else
        {
            Map<String, Serializable> auditedData;
            // Add the "no error" indicator
            auditData.put(AUDIT_SNIPPET_NO_ERROR, null);
            // The current transaction will be fine
            auditedData = auditComponent.recordAuditValues(rootPath, auditData);

	        // Done
	        if (logger.isDebugEnabled() && auditedData.size() > 0)
	        {
	            logger.debug(
	                    "Audited after invocation: \n" +
	                    (thrown == null ? "" : "   Exception: " + thrown.getMessage() + "\n") +
	                    "   Values: " + auditedData);
	        }
        }
    }
}
