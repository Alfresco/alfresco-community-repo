
package org.alfresco.repo.action.constraint;

import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;

/**
 * Mimetype parameter constraint
 * 
 * @author Roy Wetherall
 */
public class MimetypeParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "ac-mimetypes";
    
    /** Mimetype map */
    private MimetypeMap mimetypeMap;
    
    /**
     * Sets the mimetype map
     * 
     * @param mimetypeMap MimetypeMap
     */
    public void setMimetypeMap(MimetypeMap mimetypeMap)
    {
        this.mimetypeMap = mimetypeMap;
    }
          
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {                  
        return mimetypeMap.getDisplaysByMimetype();        
    }    
    
    
}
