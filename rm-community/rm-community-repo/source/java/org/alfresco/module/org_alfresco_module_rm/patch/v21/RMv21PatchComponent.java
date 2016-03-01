 
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
