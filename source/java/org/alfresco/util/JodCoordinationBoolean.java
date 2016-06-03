package org.alfresco.util;

import org.alfresco.util.bean.BooleanBean;

/**
 * Wraps a {@link org.alfresco.util.JodCoordination} object to return one of its boolean methods,
 * so that it may be used as the input to another bean.
 *  
 * @author Alan Davis
 */
public class JodCoordinationBoolean implements BooleanBean
{
    private JodCoordination jodCoordination;
    private String returnValue;

    public void setJodCoordination(JodCoordination jodCoordination)
    {
        this.jodCoordination = jodCoordination;
    }
    
    public void setReturnValue(String returnValue)
    {
        this.returnValue = returnValue;
    }
    
    @Override
    public boolean isTrue()
    {
        if ("startOpenOffice".equals(returnValue))
        {
            return jodCoordination.startOpenOffice();
        }
        else if ("startListener".equals(returnValue))
        {
            return jodCoordination.startListener();
        }
        else
        {
            throw new IllegalArgumentException("Expected \"startOpenOffice\" or \"startListener\" " +
                "as the returnValue property, but it was \""+returnValue+"\"");
        }
    }
}
