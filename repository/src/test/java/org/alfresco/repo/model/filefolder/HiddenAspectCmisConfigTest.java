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
package org.alfresco.repo.model.filefolder;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.junit.After;
import org.junit.Before;

/**
 * Associated tests methods for HiddenAspectTest with the non-default value for cmisDisableHide
 * 
 * @author Andreea Dragoi
 * @since 4.2.5
 *
 */
public class HiddenAspectCmisConfigTest extends HiddenAspectTest
{
    private static final String FILENAME_LEADING_DOT_PATTERN = "\\..*";
    
    @Before
    public void setup() throws SystemException, NotSupportedException
    {
        super.setup();
        //change cmisHiddenConfing default value
        switchCmisHiddenConfig();
        
    }
    
    @After
    public void tearDown() throws Exception
    {
        //revert to cmisHiddenConfing default value
        switchCmisHiddenConfig();
        super.tearDown();
    }
    
    /**
     * switch value for cmisDisableHide in order to tests the configuration in both states
     */
    private void switchCmisHiddenConfig()
    {
        for (HiddenFileInfo hiddenFileInfo : hiddenAspect.getPatterns())
        {
            if (FILENAME_LEADING_DOT_PATTERN.equals(hiddenFileInfo.getFilter()) && hiddenFileInfo instanceof ConfigurableHiddenFileInfo)
            {
                ConfigurableHiddenFileInfo configurableHiddenFileInfo = (ConfigurableHiddenFileInfo) hiddenFileInfo;
                configurableHiddenFileInfo.setCmisDisableHideConfig(!configurableHiddenFileInfo.isCmisDisableHideConfig());
                cmisDisableHide = !cmisDisableHide;
                break;
            }
        }
    }

}
