/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
