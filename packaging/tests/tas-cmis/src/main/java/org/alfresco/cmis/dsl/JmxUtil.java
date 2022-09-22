package org.alfresco.cmis.dsl;

import org.alfresco.cmis.CmisWrapper;
import org.alfresco.utility.network.Jmx;
import org.alfresco.utility.network.JmxClient;
import org.alfresco.utility.network.JmxJolokiaProxyClient;

/**
 * DSL for interacting with JMX (using direct JMX call see {@link JmxClient} or {@link JmxJolokiaProxyClient}
 */
public class JmxUtil
{
    @SuppressWarnings("unused")
    private CmisWrapper cmisWrapper;
    private Jmx jmx;
    private final String jmxAuditObjectName = "Alfresco:Type=Configuration,Category=Audit,id1=default";

    public JmxUtil(CmisWrapper cmisWrapper, Jmx jmx)
    {
        this.cmisWrapper = cmisWrapper;
        this.jmx = jmx;
    }

    public void enableCMISAudit() throws Exception
    {
        if(jmx.readProperty(jmxAuditObjectName, "audit.enabled").equals(String.valueOf(false)))
        {
            jmx.writeProperty(jmxAuditObjectName, "audit.enabled", String.valueOf(true));
        }
        jmx.writeProperty(jmxAuditObjectName, "audit.cmischangelog.enabled", String.valueOf(true));
        jmx.writeProperty(jmxAuditObjectName, "audit.alfresco-access.enabled", String.valueOf(true));
    }

    public void disableCMISAudit() throws Exception
    {
        jmx.writeProperty(jmxAuditObjectName, "audit.cmischangelog.enabled", String.valueOf(false));
        jmx.writeProperty(jmxAuditObjectName, "audit.alfresco-access.enabled", String.valueOf(false));
    }
}
