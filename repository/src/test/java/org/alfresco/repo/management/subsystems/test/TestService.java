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
