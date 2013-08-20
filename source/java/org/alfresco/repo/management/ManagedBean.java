package org.alfresco.repo.management;

/**
 * Describes an object to be exported as a JMX MBean.
 * 
 * @author mrogers
 *
 */
public interface ManagedBean
{
    public void setBeanName(String name);

    public void setObjectName(String objectName);
    
    public void setResource(Object resource);

}
