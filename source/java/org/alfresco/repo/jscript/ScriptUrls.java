/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
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
