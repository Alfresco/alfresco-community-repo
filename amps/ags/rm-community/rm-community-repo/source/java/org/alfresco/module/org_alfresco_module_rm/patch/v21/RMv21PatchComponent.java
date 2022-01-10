/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.patch.v21;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.patch.compatibility.ModulePatchComponent;
import org.alfresco.repo.module.ModuleComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Convenience class to ensure all V2.0 patches are executed before v2.1
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@SuppressWarnings("deprecation")
public abstract class RMv21PatchComponent extends ModulePatchComponent
                                          implements ApplicationContextAware
{
    /** application context */
    private ApplicationContext applicationContext;

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    /**
     * init method
     */
    @Override
    public void init()
    {
        super.init();

        // manual addition of V20 patch dependencies
        List<ModuleComponent> depends = getDependsOn();
        addDependency(depends, "org_alfresco_module_rm_notificationTemplatePatch");
        addDependency(depends, "org_alfresco_module_rm_RMv2ModelPatch");
        addDependency(depends, "org_alfresco_module_rm_RMv2FilePlanNodeRefPatch");
        addDependency(depends, "org_alfresco_module_rm_RMv2SavedSearchPatch");
    }

    /**
     * @param depends   list of module dependencies
     * @param beanName  bean name
     */
    private void addDependency(List<ModuleComponent> depends, String beanName)
    {
        ModuleComponent moduleComponent = (ModuleComponent)applicationContext.getBean(beanName);
        depends.add(moduleComponent);
    }
}
