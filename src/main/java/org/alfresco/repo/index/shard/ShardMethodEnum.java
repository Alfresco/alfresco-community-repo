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
package org.alfresco.repo.index.shard;
/**
 * Enum that details sharding type
 * @author Andy
 * @author Michael Suzuki
 *
 */
public enum ShardMethodEnum
{
    MOD_ACL_ID,
    ACL_ID,
    DB_ID,
    DATE,//Time stamp
    DATE_YEAR,
    DATE_MONTH,
    UNKOWN;
    
    public static ShardMethodEnum getShardMethod(String shardMethod)
    {
        if(shardMethod == null)
        {
            return UNKOWN;
        }
        ShardMethodEnum shardMethodEnum;
        switch (shardMethod)
        {
            //MOD_ACL_ID legacy acl used in Alfresco 5.1
            case "MOD_ACL_ID":
                shardMethodEnum = MOD_ACL_ID;
                break;
            //ACL id based on murmur hash.
            case "ACL_ID":
                shardMethodEnum = ACL_ID;
                break;
            case "DB_ID":
                shardMethodEnum = DB_ID;
                break;
            case "DATE":
                shardMethodEnum = DATE;
                break;
            case "DATE_YEAR":
                shardMethodEnum = DATE_YEAR;
                break;
            case "DATE_MONTH":
                shardMethodEnum = DATE_MONTH;
                break;
            default:
                shardMethodEnum = UNKOWN;
                break;
        }
        return shardMethodEnum;
    }
}
