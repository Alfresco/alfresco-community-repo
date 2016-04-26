package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.schemacomp.model.AbstractDbObject;
import org.alfresco.util.schemacomp.model.DbObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Tests for the {@link RedundantDbObject} class.
 * 
 * @author Matt Ward
 */
public class RedundantDbObjectTest
{
    @Before
    public void setUp()
    {
        I18NUtil.registerResourceBundle("alfresco.messages.system-messages");
    }
    
    @Test
    public void describe()
    {
        DbObject reference = new MyDbObject("reference");
        List<DbObject> matches = makeMatches(3);
        
        RedundantDbObject redundantDBO = new RedundantDbObject(reference, matches);
        
        assertEquals("Redundancy: 3 items matching MyDbObject[name=reference], " +
                    "matches: MyDbObject[name=match1], MyDbObject[name=match2], MyDbObject[name=match3]",
                    redundantDBO.describe());
    }

    @Test
    public void describeTooManyMatches()
    {
        DbObject reference = new MyDbObject("reference");
        List<DbObject> matches = makeMatches(4);
        
        RedundantDbObject redundantDBO = new RedundantDbObject(reference, matches);
        
        assertEquals("4 redundant items? reference: MyDbObject[name=reference], " +
                    "matches: MyDbObject[name=match1], MyDbObject[name=match2], MyDbObject[name=match3] and 1 more...",
                    redundantDBO.describe());
    }

    /**
     * @return
     */
    private List<DbObject> makeMatches(int numMatches)
    {
        List<DbObject> matches = new ArrayList<DbObject>();
        for (int i = 0; i < numMatches; i++)
        {
            matches.add(new MyDbObject("match" + (i+1)));
        }
        return matches;
    }
    
    
    private static class MyDbObject extends AbstractDbObject
    {
        public MyDbObject(String name)
        {
            super(null, name);
        }

        @Override
        public void accept(DbObjectVisitor visitor)
        {
        }   
    }
}
