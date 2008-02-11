/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantDeployerService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base implementation of the patch.  This class ensures that the patch is
 * thread- and transaction-safe.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractPatch implements Patch
{
    /**
     * I18N message when properties not set.
     * <ul>
     *   <li>{0} = property name</li>
     *   <li>{1} = patch instance</li>
     * </ul>
     */
    public static final String ERR_PROPERTY_NOT_SET = "patch.general.property_not_set";
    
    private static Log logger = LogFactory.getLog(AbstractPatch.class);
    
    private String id;
    private int fixesFromSchema;
    private int fixesToSchema;
    private int targetSchema;
    private String description;
    /** a list of patches that this one depends on */
    private List<Patch> dependsOn;
    /** flag indicating if the patch was successfully applied */
    private boolean applied;
    private boolean applyToTenants = true; // by default, apply to each tenant, if tenant service is enabled
    /** the service to register ourselves with */
    private PatchService patchService;
    /** used to ensure a unique transaction per execution */
    protected TransactionService transactionService;
    /** support service */
    protected NamespaceService namespaceService;
    /** support service */
    protected NodeService nodeService;
    /** support service */
    protected SearchService searchService;
    /** support service */
    protected AuthenticationComponent authenticationComponent;
    /** support service */
    protected TenantDeployerService tenantDeployerService;
    

    public AbstractPatch()
    {
        this.fixesFromSchema = -1;
        this.fixesToSchema = -1;
        this.targetSchema = -1;
        this.applied = false;
        this.dependsOn = Collections.emptyList();
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
    }

    /**
     * Set a generally-used service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Set a generally-used service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set a generally-used service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Set a generally-used service
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
    
    public void setTenantDeployerService(TenantDeployerService tenantDeployerService)
    {
        this.tenantDeployerService = tenantDeployerService;
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
     * 
     * @param id the unique ID of the patch.  This dictates the order in which patches are applied.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    public int getFixesFromSchema()
    {
        return fixesFromSchema;
    }

    /**
     * Set the smallest schema number that this patch may be applied to.
     * 
     * @param version a schema number not smaller than 0
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
     * @param version a schema version number not smaller than the
     *      {@link #setFixesFromSchema(int) from version} number.
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
     * Set the schema version that this patch attempts to take the existing schema to.
     * This is for informational purposes only, acting as an indicator of intention rather
     * than having any specific effect.
     * 
     * @param version a schema version number that must be greater than the
     *      {@link #fixesToSchema max fix schema number}
     */
    public void setTargetSchema(int version)
    {
        if (version <= fixesToSchema)
        {
            throw new IllegalArgumentException("'targetSchema' must be greater than 'fixesToSchema'");
        }
        this.targetSchema = version;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * @param description a thorough description of the patch
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
     * Set all the dependencies for this patch.  It should not be executed
     * before all the dependencies have been applied.
     * 
     * @param dependsOn a list of dependencies
     */
    public void setDependsOn(List<Patch> dependsOn)
    {
        this.dependsOn = dependsOn;
    }

    public boolean applies(int version)
    {
        return ((this.fixesFromSchema <= version) && (version <= fixesToSchema));
    }
    
    /**
     * Performs a null check on the supplied value.
     * 
     * @param value value to check
     * @param name name of the property to report
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
     * Check that the schema version properties have been set appropriately.
     * Derived classes can override this method to perform their own validation provided
     * that this method is called by the derived class.
     */
    protected void checkProperties()
    {
        // check that the necessary properties have been set
        checkPropertyNotNull(id, "id");
        checkPropertyNotNull(description, "description");
        checkPropertyNotNull(transactionService, "transactionService");
        checkPropertyNotNull(namespaceService, "namespaceService");
        checkPropertyNotNull(nodeService, "nodeService");
        checkPropertyNotNull(searchService, "searchService");
        checkPropertyNotNull(authenticationComponent, "authenticationComponent");
        if (fixesFromSchema == -1 || fixesToSchema == -1 || targetSchema == -1)
        {
            throw new AlfrescoRuntimeException(
                    "Patch properties 'fixesFromSchema', 'fixesToSchema' and 'targetSchema' have not all been set on this patch: \n" +
                    "   patch: " + this);
        }
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
            throw new AlfrescoRuntimeException("The patch has already been executed: \n" +
                    "   patch: " + this);
        }
        // check properties
        checkProperties();
        // execute in a transaction
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("\n" +
                        "Patch will be applied: \n" +
                        "   patch: " + this);
            }
            AuthenticationUtil.RunAsWork<String> authorisedPathWork = new AuthenticationUtil.RunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                    RetryingTransactionCallback<String> patchWork = new RetryingTransactionCallback<String>()
                    {
                        public String execute() throws Exception
                        {

                            // downgrade integrity checking
                            IntegrityChecker.setWarnInTransaction();

                            String report = applyInternal();
                            
                        	if ((tenantDeployerService != null) && tenantDeployerService.isEnabled() && applyToTenants)
                            {
                            	List<Tenant> tenants = tenantDeployerService.getAllTenants();	                            	
                                for (Tenant tenant : tenants)
                                {          
                                	String tenantDomain = tenant.getTenantDomain();
                                	String tenantReport = AuthenticationUtil.runAs(new RunAsWork<String>()
                                    {
                                		public String doWork() throws Exception
                                        {
                                			return applyInternal();
                                        }
                                    }, tenantDeployerService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
                                	
                                	report = report + "\n" + tenantReport + " (for tenant: " + tenantDomain + ")";
                                }
                                
                                return report;
                            }

	                        // done
	                        return report;
                        }
                    };
                    return transactionService.getRetryingTransactionHelper().doInTransaction(patchWork);
                }
            };
            String report = AuthenticationUtil.runAs(authorisedPathWork, AuthenticationUtil.getSystemUserName());
            // the patch was successfully applied
            applied = true;
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("\n" +
                        "Patch successfully applied: \n" +
                        "   patch: " + this + "\n" +
                        "   report: " + report);
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
     * @param e the throwable
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
     * This method does the work.  All transactions and thread-safety will be taken care of by this class.
     * Any exception will result in the transaction being rolled back.  Integrity checks are downgraded
     * for the duration of the transaction.
     * 
     * @return Returns the report (only success messages).
     * @see #apply()
     * @throws Exception anything can be thrown.  This must be used for all failures.
     */
    protected abstract String applyInternal() throws Exception;
}
