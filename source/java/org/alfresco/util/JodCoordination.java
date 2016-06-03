package org.alfresco.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class coordinates with the OOoJodconverter subsystem so that the OOoDirect subsystem does not
 * start up its own soffice process it that would clash with the JOD one. It also does not make a
 * connection to a JOD started soffice process.
 *  
 * @author Alan Davis
 */
public class JodCoordination
{
    private static Log logger = LogFactory.getLog(JodCoordination.class);
    
    private Boolean start;
    
    private boolean oooEnabled;
    private boolean oooLocalhost;
    private String oooPort;
    private JodConfig jodConfig;

    public void setOooEnabled(boolean oooEnabled)
    {
        this.oooEnabled = oooEnabled;
    }

    public void setOooHost(String oooHost)
    {
        oooLocalhost = oooHost == null || oooHost.equals(SocketOpenOfficeConnection.DEFAULT_HOST);
    }

    public void setOooPort(String oooPort)
    {
        this.oooPort = oooPort;
    }

    public void setJodConfig(JodConfig jodConfig)
    {
        this.jodConfig = jodConfig;
    }

    /**
     * Returns {@code true} if the direct OOo should be started. This should not take
     * place if both direct ooo and jod are enabled and using the same port.
     */
    public boolean startOpenOffice()
    {
        if (start == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("OOoJodconverter subsystem will "+
                        (jodConfig.isEnabled() ? "" : "NOT ") + "start an OpenOffice process");
            }
            
            start = oooEnabled && oooLocalhost;
            if (start)
            {
                if (jodConfig.isEnabled() && jodConfig.getPortsCollection().contains(oooPort))
                {
                    start = false;
                    logger.error("Both OOoDirect and OOoJodconverter subsystems are enabled and have specified " +
                            "the same port number on the localhost.");
                    logger.error("   ooo.enabled=true");
                    logger.error("   ooo.host=localhost");
                    logger.error("   ooo.port=" + oooPort);
                    logger.error("   jodconverter.portNumbers=" + jodConfig.getPorts());
                    logger.error("   jodconverter.enabled=true");
                    logger.error("The OOoDirect subsystem will not start its OpenOffice process as a result.");
                }
                else
                {
                    logger.debug("OOoDirect subsystem will start an OpenOffice process");
                }
            }
            else
            {
                logger.debug("OOoDirect subsystem will NOT start an OpenOffice process");
            }
        }
        return start;
    }
    
    /**
     * Returns {@code true} if the direct OOo connection listener should be started. This
     * should only take place if a remote host is being used or the direct OOo will be started
     * on the local host.
     */
    public boolean startListener()
    {
        return (oooEnabled && !oooLocalhost) || startOpenOffice();
    }
}
