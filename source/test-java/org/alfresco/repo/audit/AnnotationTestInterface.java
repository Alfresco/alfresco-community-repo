package org.alfresco.repo.audit;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

/**
 * An interface to test the use of the auditable annotation.
 * 
 * @author Andy Hind
 */
public interface AnnotationTestInterface
{
    @Auditable()
    public void noArgs();
    
    @Auditable(parameters = {"one", "two"})
    public String getString(String one, String two); 
    
    @Auditable(parameters = {"one"})
    public String getAnotherString(String one); 
}
