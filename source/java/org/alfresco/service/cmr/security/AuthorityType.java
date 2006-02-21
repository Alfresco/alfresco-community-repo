/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.security;

/**
 * The types of authority that are available.
 * <p>
 * <p>
 * Available types are:
 * <ol>
 * <li>USER - an authority that identifies a user
 * <li>GROUP - an authority that identifies a group
 * <li>OWNER - the special authority that applies to the owner of a node
 * <li>EVERYONE - the special authority that is interpreted as everyone
 * <li>GUEST - the special authority that applies to a GUEST (An unknown,
 * unauthenticated user)
 * </ol>
 * 
 * @author Andy Hind
 */
public enum AuthorityType
{
    ADMIN
    {
        public boolean isFixedString()
        {
            return true;
        }

        public String getFixedString()
        {
            return PermissionService.ADMINISTRATOR_AUTHORITY;
        }

        public boolean isPrefixed()
        {
            return false;
        }

        public String getPrefixString()
        {
            return "";
        }
    },

    EVERYONE
    {
        public boolean isFixedString()
        {
            return true;
        }

        public String getFixedString()
        {
            return PermissionService.ALL_AUTHORITIES;
        }

        public boolean isPrefixed()
        {
            return false;
        }

        public String getPrefixString()
        {
            return "";
        }
    },
    OWNER
    {
        public boolean isFixedString()
        {
            return true;
        }

        public String getFixedString()
        {
            return PermissionService.OWNER_AUTHORITY;
        }

        public boolean isPrefixed()
        {
            return false;
        }

        public String getPrefixString()
        {
            return "";
        }
    },
    GUEST
    {
        public boolean isFixedString()
        {
            return true;
        }

        public String getFixedString()
        {
            return PermissionService.GUEST_AUTHORITY;
        }

        public boolean isPrefixed()
        {
            return false;
        }

        public String getPrefixString()
        {
            return "";
        }
    },
    GROUP
    {
        public boolean isFixedString()
        {
            return false;
        }

        public String getFixedString()
        {
            return "";
        }

        public boolean isPrefixed()
        {
            return true;
        }

        public String getPrefixString()
        {
            return PermissionService.GROUP_PREFIX;
        }
    },
    ROLE
    {

        public boolean isFixedString()
        {
            return false;
        }

        public String getFixedString()
        {
            return "";
        }

        public boolean isPrefixed()
        {
            return true;
        }

        public String getPrefixString()
        {
            return PermissionService.ROLE_PREFIX;
        }
    },
    USER
    {
        public boolean isFixedString()
        {
            return false;
        }

        public String getFixedString()
        {
            return "";
        }

        public boolean isPrefixed()
        {
            return false;
        }

        public String getPrefixString()
        {
            return "";
        }
    };

    public abstract boolean isFixedString();

    public abstract String getFixedString();

    public abstract boolean isPrefixed();

    public abstract String getPrefixString();

    public boolean equals(String authority)
    {
        return equals(getAuthorityType(authority));
    }

    public static AuthorityType getAuthorityType(String authority)
    {
        AuthorityType authorityType;
        if (authority.equals(PermissionService.ADMINISTRATOR_AUTHORITY))
        {
            authorityType = AuthorityType.ADMIN;
        }
        if (authority.equals(PermissionService.ALL_AUTHORITIES))
        {
            authorityType = AuthorityType.EVERYONE;
        }
        else if (authority.equals(PermissionService.OWNER_AUTHORITY))
        {
            authorityType = AuthorityType.OWNER;
        }
        else if (authority.equalsIgnoreCase(PermissionService.GUEST_AUTHORITY))
        {
            authorityType = AuthorityType.GUEST;
        }
        else if (authority.startsWith(PermissionService.GROUP_PREFIX))
        {
            authorityType = AuthorityType.GROUP;
        }
        else if (authority.startsWith(PermissionService.ROLE_PREFIX))
        {
            authorityType = AuthorityType.ROLE;
        }
        else
        {
            authorityType = AuthorityType.USER;
        }
        return authorityType;
    }
}
