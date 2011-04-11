/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Base implementation of the patch. This class ensures that the patch is thread- and transaction-safe.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractPatch implements Patch,  ApplicationEventPublisherAware
{
    /**
     * I18N message when properties not set.
     * <ul>
     * <li>{0} = property name</li>
     * <li>{1} = patch instance</li>
     * </ul>
     */
    public static final String ERR_PROPERTY_NOT_SET = "patch.general.property_not_set";
    private static final String MSG_PROGRESS = "patch.progress";

    private static final long RANGE_10 = 1000 * 60 * 90;
    private static final long RANGE_5 = 1000 * 60 * 60 * 4;
    private static final long RANGE_2 = 1000 * 60 * 90 * 10;

    private static Log logger = LogFactory.getLog(AbstractPatch.class);
    private static Log progress_logger = LogFactory.getLog(PatchExecuter.class);

    private String id;
    private int fixesFromSchema;
    private int fixesToSchema;
    private int targetSchema;
    private boolean force;
    private String description;
    /** a list of patches that this one depends on */
    private List<Patch> dependsOn;
    /** a list of patches that, if already present, mean that this one should be ignored */
    private List<Patch> alternatives;
    /** flag indicating if the patch was successfully applied */
    private boolean applied;
    private boolean applyToTenants;
    /** track completion * */
    int percentComplete = 0;
    /** start time * */
    long startTime;
    
    // Does the patch require an enclosing transaction?
    private boolean requiresTransaction = true;

    /** the service to register ourselves with */
    private PatchService patchService;
    /** used to ensure a unique transaction per execution */
    protected TransactionService transactionService;
    /** Use this helper to ensure that patches can execute even on a read-only system */
    protected RetryingTransactionHelper transactionHelper;
    protected NamespaceService namespaceService;
    protected NodeService nodeService;
    protected SearchService searchService;
    protected AuthenticationContext authenticationContext;
    protected TenantAdminService tenantAdminService;
    /** Publishes batch event notifications for JMX viewing */
    protected ApplicationEventPublisher applicationEventPublisher;

    public AbstractPatch()
    {
        this.fixesFromSchema = -1;
        this.fixesToSchema = -1;
        this.targetSchema = -1;
        this.force = false;
        this.applied = false;
        this.applyToTenants = true;     // by default, apply to each tenant, if tenant service is enabled
        this.dependsOn = Collections.emptyList();
        this.alternatives = Collections.emptyList();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Patch")
          .append("[ id=").append(id)
          .append(", description=").append(description)
          .append(", fixesFromSchema=").append(fixesFromSchema)
          .append(", fixesToSchema=").append(fixesToSchema)
          .append(", targetSchema=").append(targetSchema)
          .append("]");
        return sb.toString();
    }

    /**
     * Set the service that this patch will register with for execution.
     */
    public void setPatchService(PatchService patchService)
    {
        this.patchService = patchService;
    }

    /**
     * Set the transaction provider so that each execution can be performed within a transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
        this.transactionHelper = transactionService.getRetryingTransactionHelper();
        this.transactionHelper.setForceWritable(true);
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }
    
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * Set automatically
     */
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * This ensures that this bean gets registered with the appropriate {@link PatchService service}.
     */
    public void init()
    {
        if (patchService == null)
        {
            throw new AlfrescoRuntimeException("Mandatory property not set: patchService");
        }
        patchService.registerPatch(this);
    }

    public String getId()
    {
        return id;
    }

    /**
     * @param id
     *            the unique ID of the patch. This dictates the order in which patches are applied.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    public int getFixesFromSchema()
    {
        return fixesFromSchema;
    }

    public void setRequiresTransaction(boolean requiresTransaction)
    {
    	this.requiresTransaction = requiresTransaction;
    }
    
    public boolean requiresTransaction()
    {
    	return requiresTransaction;
    }
    
    /**
     * Set the smallest schema number that this patch may be applied to.
     * 
     * @param version
     *            a schema number not smaller than 0
     */
    public void setFixesFromSchema(int version)
    {
        if (version < 0)
        {
            throw new IllegalArgumentException("The 'fixesFromSchema' property may not be less than 0");
        }
        this.fixesFromSchema = version;
        // auto-adjust the to version
        if (fixesToSchema < fixesFromSchema)
        {
            setFixesToSchema(this.fixesFromSchema);
        }
    }

    public int getFixesToSchema()
    {
        return fixesToSchema;
    }

    /**
     * Set the largest schema version number that this patch may be applied to.
     * 
     * @param version
     *            a schema version number not smaller than the {@link #setFixesFromSchema(int) from version} number.
     */
    public void setFixesToSchema(int version)
    {
        if (version < fixesFromSchema)
        {
            throw new IllegalArgumentException("'fixesToSchema' must be greater than or equal to 'fixesFromSchema'");
        }
        this.fixesToSchema = version;
    }

    public int getTargetSchema()
    {
        return targetSchema;
    }

    /**
     * Set the schema version that this patch attempts to take the existing schema to. This is for informational
     * purposes only, acting as an indicator of intention rather than having any specific effect.
     * 
     * @param version
     *            a schema version number that must be greater than the {@link #fixesToSchema max fix schema number}
     */
    public void setTargetSchema(int version)
    {
        if (version <= fixesToSchema)
        {
            throw new IllegalArgumentException("'targetSchema' must be greater than 'fixesToSchema'");
        }
        this.targetSchema = version;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isForce()
    {
        return force;
    }

    /**
     * Set the flag that forces the patch to be forcefully applied.  This allows patches to be overridden to induce execution
     * regardless of the upgrade or installation versions, or even if the patch has been executed before.
     * 
     * @param force         <tt>true</tt> to force the patch to be applied
     */
    public void setForce(boolean force)
    {
        this.force = force;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * @param description
     *            a thorough description of the patch
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<Patch> getDependsOn()
    {
        return this.dependsOn;
    }

    /**
     * Set all the dependencies for this patch. It should not be executed before all the dependencies have been applied.
     * 
     * @param dependsOn
     *            a list of dependencies
     */
    public void setDependsOn(List<Patch> dependsOn)
    {
        this.dependsOn = dependsOn;
    }

    public List<Patch> getAlternatives()
    {
        return alternatives;
    }

    /**
     * Set all anti-dependencies.  If any of the patches in the list have already been executed, then
     * this one need not be.
     * 
     * @param alternatives          a list of alternative patches
     */
    public void setAlternatives(List<Patch> alternatives)
    {
        this.alternatives = alternatives;
    }

    public boolean applies(int version)
    {
        return ((this.fixesFromSchema <= version) && (version <= fixesToSchema));
    }

    /**
     * Performs a null check on the supplied value.
     * 
     * @param value
     *            value to check
     * @param name
     *            name of the property to report
     */
    protected final void checkPropertyNotNull(Object value, String name)
    {
        if (value == null)
        {
            throw new PatchException(ERR_PROPERTY_NOT_SET, name, this);
        }
    }
    
    public void setApplyToTenants(boolean applyToTenants)
    {
        this.applyToTenants = applyToTenants;
    }

    /**
     * Check that the schema version properties have been set appropriately. Derived classes can override this method to
     * perform their own validation provided that this method is called by the derived class.
     */
    protected void checkProperties()
    {
        // check that the necessary properties have been set
        checkPropertyNotNull(id, "id");
        checkPropertyNotNull(description, "description");
        checkPropertyNotNull(transactionService, "transactionService");
        checkPropertyNotNull(transactionHelper, "transactionHelper");
        checkPropertyNotNull(namespaceService, "namespaceService");
        checkPropertyNotNull(nodeService, "nodeService");
        checkPropertyNotNull(searchService, "searchService");
        checkPropertyNotNull(authenticationContext, "authenticationContext");
        checkPropertyNotNull(tenantAdminService, "tenantAdminService");
        checkPropertyNotNull(applicationEventPublisher, "applicationEventPublisher");
        if (fixesFromSchema == -1 || fixesToSchema == -1 || targetSchema == -1)
        {
            throw new AlfrescoRuntimeException(
                    "Patch properties 'fixesFromSchema', 'fixesToSchema' and 'targetSchema' " +
                    "have not all been set on this patch: \n"
                    + "   patch: " + this);
        }
    }

    private String applyImpl() throws Exception
    {
        // downgrade integrity checking
        IntegrityChecker.setWarnInTransaction();

        String report = applyInternal();
        
    	if ((tenantAdminService != null) && tenantAdminService.isEnabled() && applyToTenants)
        {
        	List<Tenant> tenants = tenantAdminService.getAllTenants();	                            	
            for (Tenant tenant : tenants)
            {          
            	String tenantDomain = tenant.getTenantDomain();
            	String tenantReport = AuthenticationUtil.runAs(new RunAsWork<String>()
                {
            		public String doWork() throws Exception
                    {
            			return applyInternal();
                    }
                }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
            	
            	report = report + "\n" + tenantReport + " (for tenant: " + tenantDomain + ")";
            }
            
            return report;
        }

        // done?
    	return report;
    }

    /**
     * Sets up the transaction and ensures thread-safety.
     * 
     * @see #applyInternal()
     */
    public synchronized String apply() throws PatchException
    {
        // ensure that this has not been executed already
        if (applied)
        {
            throw new AlfrescoRuntimeException("The patch has already been executed: \n" + "   patch: " + this);
        }
        // check properties
        checkProperties();

        if (logger.isDebugEnabled())
        {
            logger.debug("\n" + "Patch will be applied: \n" + "   patch: " + this);
        }

        try
        {
            AuthenticationUtil.RunAsWork<String> applyPatchWork = new AuthenticationUtil.RunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                	if(requiresTransaction())
                	{
                        // execute in a transaction
                		RetryingTransactionCallback<String> patchWork = new RetryingTransactionCallback<String>()
	                    {
	                        public String execute() throws Exception
	                        {
	                        	return applyImpl();
	                        }
	                    };
	                    return transactionService.getRetryingTransactionHelper().doInTransaction(patchWork, false, true);
                	}
                	else
                	{
                		return applyImpl();
                	}
                }
            };
            startTime = System.currentTimeMillis();
            String report = AuthenticationUtil.runAs(applyPatchWork, AuthenticationUtil.getSystemUserName());
            // the patch was successfully applied
            applied = true;
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("\n" + "Patch successfully applied: \n" + "   patch: " + this + "\n" + "   report: " + report);
            }
            return report;
        }
        catch (PatchException e)
        {
            // no need to extract the exception
            throw e;
        }
        catch (Throwable e)
        {
            // check whether there is an embedded patch exception
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof PatchException)
            {
                throw (PatchException) cause;
            }
            // need to generate a message from the exception
            String report = makeReport(e);
            // generate the correct exception
            throw new PatchException(report);
        }
    }

    /**
     * Dumps the error's full message and trace to the String
     * 
     * @param e
     *            the throwable
     * @return Returns a String representative of the printStackTrace method
     */
    private String makeReport(Throwable e)
    {
        StringWriter stringWriter = new StringWriter(1024);
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        try
        {
            e.printStackTrace(printWriter);
            return stringWriter.toString();
        }
        finally
        {
            printWriter.close();
        }
    }

    /**
     * This method does the work. All transactions and thread-safety will be taken care of by this class. Any exception
     * will result in the transaction being rolled back. Integrity checks are downgraded for the duration of the
     * transaction.
     * 
     * @return Returns the report (only success messages).
     * @see #apply()
     * @throws Exception
     *             anything can be thrown. This must be used for all failures.
     */
    protected abstract String applyInternal() throws Exception;

    /**
     * Support to report patch completion and estimated completion time.
     * 
     * @param estimatedTotal
     * @param currentInteration
     */
    protected void reportProgress(long estimatedTotal, long currentInteration)
    {
        if (progress_logger.isDebugEnabled())
        {
            progress_logger.debug(currentInteration + "/" + estimatedTotal);
        }
        if (currentInteration == 0)
        {
            // No point reporting the start - we have already done that elsewhere ....
            percentComplete = 0;
        }
        else if (currentInteration * 100l / estimatedTotal > percentComplete)
        {
            int previous = percentComplete;
            percentComplete = (int) (currentInteration * 100l / estimatedTotal);

            if (percentComplete < 100)
            {
                // conditional report

                long currentTime = System.currentTimeMillis();
                long timeSoFar = currentTime - startTime;
                long timeRemaining = timeSoFar * (100 - percentComplete) / percentComplete;

                int report = -1;

                if (timeRemaining > 60000)
                {
                    int reportInterval = getReportingInterval(timeSoFar, timeRemaining);

                    for (int i = previous + 1; i <= percentComplete; i++)
                    {
                        if (i % reportInterval == 0)
                        {
                            report = i;
                        }
                    }
                    if (report > 0)
                    {
                        Date end = new Date(currentTime + timeRemaining);

                        String msg = I18NUtil.getMessage(MSG_PROGRESS, getId(), report, end);
                        progress_logger.info(msg);
                    }
                }
            }
        }
    }

    private int getReportingInterval(long soFar, long toGo)
    {
        long total = soFar + toGo;
        if (total < RANGE_10)
        {
            return 10;
        }
        else if (total < RANGE_5)
        {
            return 5;
        }
        else if (total < RANGE_2)
        {
            return 2;
        }
        else
        {
            return 1;
        }

    }
}
