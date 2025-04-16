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
package org.alfresco.repo.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.PropertyCheck;

/**
 * Implementation of a {@link org.alfresco.repo.module.ModuleComponent} to provide the basic necessities.
 * 
 * @see #executeInternal()
 * 
 * @author Roy Wetherall
 * @author Derek Hulley
 * @since 2.0
 */
@AlfrescoPublicApi
public abstract class AbstractModuleComponent implements ModuleComponent, BeanNameAware
{
    private static final String ERR_ALREADY_EXECUTED = "module.err.already_executed";
    private static final String ERR_EXECUTION_FAILED = "module.err.execution_failed";

    // Supporting components
    protected ServiceRegistry serviceRegistry;
    protected AuthenticationComponent authenticationComponent;
    protected ModuleService moduleService;
    private TenantAdminService tenantAdminService;

    private String moduleId;
    private String name;
    private String description;
    private ModuleVersionNumber sinceVersion;
    private ModuleVersionNumber appliesFromVersion;
    private ModuleVersionNumber appliesToVersion;
    private List<ModuleComponent> dependsOn;
    /** Defaults to <tt>true</tt> */
    private boolean executeOnceOnly;
    private Map<String, Boolean> executed;

    public AbstractModuleComponent()
    {
        sinceVersion = ModuleVersionNumber.VERSION_ZERO;
        appliesFromVersion = ModuleVersionNumber.VERSION_ZERO;
        appliesToVersion = ModuleVersionNumber.VERSION_BIG;
        dependsOn = new ArrayList<ModuleComponent>(0);
        executeOnceOnly = true;
        executed = new HashMap<String, Boolean>(1);
    }

    /**
     * Checks for the presence of all generally-required properties.
     */
    protected void checkProperties()
    {
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        PropertyCheck.mandatory(this, "authenticationComponent", authenticationComponent);
        PropertyCheck.mandatory(this, "moduleId", moduleId);
        PropertyCheck.mandatory(this, "name", name);
        PropertyCheck.mandatory(this, "sinceVersion", sinceVersion);
        PropertyCheck.mandatory(this, "appliesFromVersion", appliesFromVersion);
        PropertyCheck.mandatory(this, "appliesToVersion", appliesToVersion);
    }

    /**
     * @see #getModuleId()
     * @see #getName()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ModuleComponent")
                .append("[ module=").append(moduleId)
                .append(", name=").append(name)
                .append(", since=").append(sinceVersion)
                .append(", appliesFrom=").append(appliesFromVersion)
                .append(", appliesTo=").append(appliesToVersion)
                .append(", onceOnly=").append(executeOnceOnly)
                .append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (false == obj instanceof ModuleComponent)
        {
            return false;
        }
        ModuleComponent that = (ModuleComponent) obj;
        return (EqualsHelper.nullSafeEquals(this.moduleId, that.getModuleId())
                && EqualsHelper.nullSafeEquals(this.name, that.getName()));
    }

    @Override
    public int hashCode()
    {
        return moduleId.hashCode() + 17 * name.hashCode();
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Set the module service to register with. If not set, the component will not be automatically started.
     * 
     * @param moduleService
     *            the service to register against. This is optional.
     */
    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * {@inheritDoc}
     */
    public String getModuleId()
    {
        return moduleId;
    }

    /**
     * @param moduleId
     *            the globally unique module name.
     */
    public void setModuleId(String moduleId)
    {
        this.moduleId = moduleId;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the component name, which must be unique within the context of the module. If the is not set, then the bean name will be used.
     * 
     * @param name
     *            the name of the component within the module.
     * 
     * @see #setBeanName(String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Convenience method that will set the name of the component to match the bean name, unless the {@link #setName(String) name} has been explicitly set.
     */
    public void setBeanName(String name)
    {
        setName(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the component's description. This will automatically be I18N'ized, so it may just be a resource bundle key.
     * 
     * @param description
     *            a description of the component.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * {@inheritDoc}
     */
    public ModuleVersionNumber getSinceVersionNumber()
    {
        return sinceVersion;
    }

    /**
     * Set the version number for which this component was added.
     */
    public void setSinceVersion(String version)
    {
        this.sinceVersion = new ModuleVersionNumber(version);
    }

    /**
     * {@inheritDoc}
     */
    public ModuleVersionNumber getAppliesFromVersionNumber()
    {
        return appliesFromVersion;
    }

    /**
     * Set the minimum module version number to which this component applies. Default <b>0.0</b>.
     */
    public void setAppliesFromVersion(String version)
    {
        this.appliesFromVersion = new ModuleVersionNumber(version);
    }

    /**
     * {@inheritDoc}
     */
    public ModuleVersionNumber getAppliesToVersionNumber()
    {
        return appliesToVersion;
    }

    /**
     * Set the minimum module version number to which this component applies. Default <b>999.0</b>.
     */
    public void setAppliesToVersion(String version)
    {
        this.appliesToVersion = new ModuleVersionNumber(version);
    }

    /**
     * {@inheritDoc}
     */
    public List<ModuleComponent> getDependsOn()
    {
        return dependsOn;
    }

    /**
     * @param dependsOn
     *            a list of modules that must be executed before this one
     */
    public void setDependsOn(List<ModuleComponent> dependsOn)
    {
        this.dependsOn = dependsOn;
    }

    /**
     * {@inheritDoc}
     * 
     * @return Returns <tt>true</tt> always. Override as required.
     */
    public boolean isExecuteOnceOnly()
    {
        return executeOnceOnly;
    }

    /**
     * @param executeOnceOnly
     *            <tt>true</tt> to force execution of this component with each startup or <tt>false</tt> if it must only be executed once.
     */
    public void setExecuteOnceOnly(boolean executeOnceOnly)
    {
        this.executeOnceOnly = executeOnceOnly;
    }

    public void init()
    {
        // Ensure that the description gets I18N'ized
        description = I18NUtil.getMessage(description);
        // Register the component with the service
        if (moduleService != null) // Allows optional registration of the component
        {
            moduleService.registerComponent(this);
        }
    }

    /**
     * The method that performs the actual work. For the most part, derived classes will only have to override this method to be fully functional.
     * 
     * @throws Throwable
     *             any problems, just throw them
     */
    protected abstract void executeInternal() throws Throwable;

    /**
     * {@inheritDoc}
     * 
     * @see #executeInternal() the abstract method to be implemented by subclasses
     */
    public final synchronized void execute()
    {
        // ensure that this has not been executed already
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        if (!executed.containsKey(tenantDomain))
        {
            executed.put(tenantDomain, false);
        }

        if (executed.get(tenantDomain))
        {
            throw AlfrescoRuntimeException.create(ERR_ALREADY_EXECUTED, moduleId, name);
        }
        // Ensure properties have been set
        checkProperties();
        // Execute
        try
        {
            executeInternal();
        }
        catch (Throwable e)
        {
            throw AlfrescoRuntimeException.create(e, ERR_EXECUTION_FAILED, name, e.getMessage());
        }
        finally
        {
            // There are no second chances
            executed.put(tenantDomain, true);
        }
    }

    // from Thor
    public final synchronized void shutdown()
    {
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        executed.put(tenantDomain, false);
    }
}
