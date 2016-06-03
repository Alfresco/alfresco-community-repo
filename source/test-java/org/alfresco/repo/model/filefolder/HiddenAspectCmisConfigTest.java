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
