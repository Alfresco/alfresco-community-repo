package org.alfresco.repo.jscript;

import java.io.Serializable;

import org.alfresco.repo.admin.SysAdminParams;

public class ScriptUrls implements Serializable
{
    private static final long serialVersionUID = 690400883682643830L;

    private SysAdminParams sysAdminParams;

    public ScriptUrls(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public UrlResolver getAlfresco()
    {
        return new UrlResolver()
        {
            private static final long serialVersionUID = -3325783930102513154L;

            public String getContext()
            {
                return sysAdminParams.getAlfrescoContext();
            }

            public String getHost()
            {
                return sysAdminParams.getAlfrescoHost();
            }

            public Integer getPort()
            {
                return sysAdminParams.getAlfrescoPort();
            }

            public String getProtocol()
            {
                return sysAdminParams.getAlfrescoProtocol();
            }
        };
    }

    public UrlResolver getShare()
    {
        return new UrlResolver()
        {
            private static final long serialVersionUID = -5853699981548697768L;

            public String getContext()
            {
                return sysAdminParams.getShareContext();
            }

            public String getHost()
            {
                return sysAdminParams.getShareHost();
            }

            public Integer getPort()
            {
                return sysAdminParams.getSharePort();
            }

            public String getProtocol()
            {
                return sysAdminParams.getShareProtocol();
            }
        };
    }

    public interface UrlResolver extends Serializable
    {
        public String getContext();

        public String getHost();

        public Integer getPort();

        public String getProtocol();
    }
}
