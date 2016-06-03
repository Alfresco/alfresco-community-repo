package org.alfresco.repo.management.subsystems.test;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;

/**
 * A class to test out subsystem property setting.
 * 
 * @see ChildApplicationContextFactory
 */
public class TestService
{
    private TestBean[] testBeans;

    private String simpleProp1;
    private Boolean simpleProp2;
    private String simpleProp3;

    public TestBean[] getTestBeans()
    {
        return this.testBeans;
    }

    public void setTestBeans(TestBean[] testBeans)
    {
        this.testBeans = testBeans;
    }

    public String getSimpleProp1()
    {
        return this.simpleProp1;
    }

    public void setSimpleProp1(String simpleProp1)
    {
        this.simpleProp1 = simpleProp1;
    }

    public Boolean getSimpleProp2()
    {
        return this.simpleProp2;
    }

    public void setSimpleProp2(Boolean simpleProp2)
    {
        this.simpleProp2 = simpleProp2;
    }

    public String getSimpleProp3()
    {
        return this.simpleProp3;
    }

    public void setSimpleProp3(String simpleProp3)
    {
        this.simpleProp3 = simpleProp3;
    }
}
