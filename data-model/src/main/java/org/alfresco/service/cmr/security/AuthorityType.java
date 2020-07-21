/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.security;



import org.alfresco.api.AlfrescoPublicApi;



/**

 * The types of authority that are available.

 * <p>

 * </p>

 * Available types are:

 * <ol>

 * <li>USER - an authority that identifies a user

 * <li>GROUP - an authority that identifies a group

 * <li>OWNER - the special authority that applies to the owner of a node

 * <li>EVERYONE - the special authority that is interpreted as everyone

 * <li>GUEST - the special authority that applies to a GUEST (An unknown, unauthenticated user)

 * <li>WILDCARD - the set of all authorities (including the guest user)

 * </ol>

 * 

 * @author Andy Hind

 */

@AlfrescoPublicApi

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



        public int getOrderPosition()

        {

            return 0;

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



        public int getOrderPosition()

        {

            return 1;

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



        public int getOrderPosition()

        {

            return 2;

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



        public int getOrderPosition()

        {

            return 3;

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



        public int getOrderPosition()

        {

            return 4;

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



        public int getOrderPosition()

        {

            return 5;

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



        public int getOrderPosition()

        {

            return 6;

        }

    },

    WILDCARD

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



        public int getOrderPosition()

        {

            return 7;

        }

    };



    public abstract boolean isFixedString();



    public abstract String getFixedString();



    public abstract boolean isPrefixed();



    public abstract String getPrefixString();



    public abstract int getOrderPosition();



    public boolean equals(String authority)

    {

        return equals(getAuthorityType(authority));

    }



    public static AuthorityType getAuthorityType(String authority)

    {

        AuthorityType authorityType;



        if(null == authority)

        {

            authorityType = AuthorityType.WILDCARD;

        }

        else

        {

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

        }



        return authorityType;

    }

}

