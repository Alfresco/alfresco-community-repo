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

package org.alfresco.repo.index;

import org.alfresco.repo.index.shard.ShardMethodEnum;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test to verify enum works as expected.
 *
 * @author Michael Suzuki
 * @author agazzarini
 */
public class ShardMethodEnumTest
{
    @Test
    public void testTypeACLLegacy()
    {
        Assert.assertEquals(ShardMethodEnum.MOD_ACL_ID, ShardMethodEnum.getShardMethod("MOD_ACL_ID"));
    }

    @Test
    public void testDBIDRange()
    {
        Assert.assertEquals(ShardMethodEnum.DB_ID_RANGE, ShardMethodEnum.getShardMethod("DB_ID_RANGE"));
    }

    @Test
    public void testTypeACLBasedOnMurmurHash()
    {
        Assert.assertEquals(ShardMethodEnum.ACL_ID, ShardMethodEnum.getShardMethod("ACL_ID"));
    }

    @Test
    public void testTypeDBID()
    {
        Assert.assertEquals(ShardMethodEnum.DB_ID, ShardMethodEnum.getShardMethod("DB_ID"));
    }

    @Test
    public void testTypeEXPLICITID()
    {
        Assert.assertEquals(ShardMethodEnum.EXPLICIT_ID, ShardMethodEnum.getShardMethod("EXPLICIT_ID"));
        Assert.assertEquals(ShardMethodEnum.EXPLICIT_ID, ShardMethodEnum.getShardMethod("EXPLICIT_ID_FALLBACK_DBID"));
    }

    @Test
    public void testTypeLRIS()
    {
        Assert.assertEquals(ShardMethodEnum.LAST_REGISTERED_INDEXING_SHARD, ShardMethodEnum.getShardMethod("LRIS"));
        Assert.assertEquals(ShardMethodEnum.LAST_REGISTERED_INDEXING_SHARD, ShardMethodEnum.getShardMethod("LAST_REGISTERED_INDEXING_SHARD"));
    }

    @Test
    public void testTypeEXPLICIT_ID_FALLBACK_LRIS()
    {
        Assert.assertEquals(ShardMethodEnum.EXPLICIT_ID_FALLBACK_LRIS, ShardMethodEnum.getShardMethod("EXPLICIT_ID_FALLBACK_LRIS"));
    }

    @Test
    public void testTypeDateTimeStamp()
    {
        Assert.assertEquals(ShardMethodEnum.DATE, ShardMethodEnum.getShardMethod("DATE"));
    }

    @Test
    public void testTypeProperty()
    {
        Assert.assertEquals(ShardMethodEnum.PROPERTY, ShardMethodEnum.getShardMethod("PROPERTY"));
    }

    @Test
    public void testUnknown()
    {
        Assert.assertEquals(ShardMethodEnum.UNKOWN, ShardMethodEnum.getShardMethod("UNKOWN"));
        Assert.assertEquals(ShardMethodEnum.UNKOWN, ShardMethodEnum.getShardMethod("Some else unknown value"));
    }

    @Test
    public void testNull()
    {
        Assert.assertEquals(ShardMethodEnum.UNKOWN, ShardMethodEnum.getShardMethod(null));
    }

    @Test
    public void testEmpty()
    {
        Assert.assertEquals(ShardMethodEnum.UNKOWN, ShardMethodEnum.getShardMethod(""));
    }

    @Test
    public void matchIsCaseInsensitive()
    {
        Assert.assertEquals(ShardMethodEnum.MOD_ACL_ID, ShardMethodEnum.getShardMethod("MoD_aCl_id"));
        Assert.assertEquals(ShardMethodEnum.DB_ID_RANGE, ShardMethodEnum.getShardMethod("db_id_range"));
        Assert.assertEquals(ShardMethodEnum.ACL_ID, ShardMethodEnum.getShardMethod("Acl_Id"));
        Assert.assertEquals(ShardMethodEnum.DB_ID, ShardMethodEnum.getShardMethod("Db_Id"));
        Assert.assertEquals(ShardMethodEnum.EXPLICIT_ID, ShardMethodEnum.getShardMethod("Explicit_Id"));
        Assert.assertEquals(ShardMethodEnum.EXPLICIT_ID, ShardMethodEnum.getShardMethod("explicit_ID_fallback_DBID"));
        Assert.assertEquals(ShardMethodEnum.LAST_REGISTERED_INDEXING_SHARD, ShardMethodEnum.getShardMethod("LRIS"));
        Assert.assertEquals(ShardMethodEnum.LAST_REGISTERED_INDEXING_SHARD, ShardMethodEnum.getShardMethod("LAST_REGISTERED_INDEXING_SHARD"));
        Assert.assertEquals(ShardMethodEnum.DATE, ShardMethodEnum.getShardMethod("datE"));
        Assert.assertEquals(ShardMethodEnum.PROPERTY, ShardMethodEnum.getShardMethod("PropertY"));
        Assert.assertEquals(ShardMethodEnum.UNKOWN, ShardMethodEnum.getShardMethod("Unknown"));
    }
}
