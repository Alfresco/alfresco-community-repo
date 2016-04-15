
package org.alfresco.filesys.alfresco;

/**
 * Desktop Action Exception Class
 *
 * @author gkspencer
 */
public class DesktopActionException extends Exception {

	private static final long serialVersionUID = 1006648817889605047L;
 
	// Status code
	
	private int m_stsCode;
	
	/**
	 * Class constructor
	 * 
	 * @param sts numeric status code.
	 * @param msg readable error message
	 */
	public DesktopActionException(int sts, String msg)
	{
		super(msg);
		m_stsCode = sts;
	}
	
	/**
     * Class constructor
     * 
     * @param s String
     */
    public DesktopActionException(String s)
    {
        super(s);
    }

    /**
     * Class constructor
     * 
     * @param s String
     * @param ex Exception
     */
    public DesktopActionException(String s, Throwable ex)
    {
        super(s, ex);
    }
    
    /**
     * Return the status code
     * 
     * @return int
     */
    public final int getStatusCode()
    {
    	return m_stsCode;
    }
}
