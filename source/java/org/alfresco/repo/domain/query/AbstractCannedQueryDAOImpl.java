package org.alfresco.repo.domain.query;

import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DAO implementation providing canned query support.
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public abstract class AbstractCannedQueryDAOImpl implements CannedQueryDAO
{
    protected Log logger = LogFactory.getLog(this.getClass());
    
    protected ControlDAO controlDAO;

    /**
     * @param controlDAO        the DAO that allows controlled rollback, if required
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }
    
    /**
     * Checks that properties have been set
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "controlDAO", controlDAO);
    }
}
