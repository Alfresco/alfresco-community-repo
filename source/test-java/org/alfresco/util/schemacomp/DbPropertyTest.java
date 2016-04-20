package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.alfresco.util.schemacomp.model.AbstractDbObject;
import org.alfresco.util.schemacomp.model.DbObject;
import org.junit.Test;
import org.mockito.Mockito;

public class DbPropertyTest
{
    @Test(expected = IllegalArgumentException.class)
    public void cannotHaveNullDbObject()
    {
        new DbProperty(null, "theProperty");
    }


    @Test
    public void propertyValueCanBeRetrievedByReflection()
    {
        DbObjectWithIndexedProp dbo = Mockito.mock(DbObjectWithIndexedProp.class);
        Mockito.when(dbo.getTheProperty()).thenReturn("This is the property value");
        
        DbProperty dbProperty = new DbProperty(dbo, "theProperty");
        
        assertEquals("This is the property value", dbProperty.getPropertyValue());
    }
    
    @Test
    public void indexedPropertyValueCanBeRetrievedByReflection()
    {
        DbObjectWithIndexedProp dbo = Mockito.mock(DbObjectWithIndexedProp.class);
        Mockito.when(dbo.getColours()).thenReturn(Arrays.asList("red", "green", "blue"));
        
        DbProperty greenProperty = new DbProperty(dbo, "colours[1]");
        DbProperty blueProperty = new DbProperty(dbo, "colours", 2);
        
        assertEquals("green", greenProperty.getPropertyValue());
        assertEquals("blue", blueProperty.getPropertyValue());
    }
    
    @Test
    public void canGetPath()
    {   
        new MyDbObject("root", 1).
            add(new MyDbObject("child", 2)).
            add(new MyDbObject("grandchild", 3)).
            add(new MyDbObject("greatgrandchild", 4));
        
        DbProperty levelProp = new DbProperty(MyDbObject.lastAdded, "level");
        assertEquals("Incorrect path", "root.child.grandchild.greatgrandchild.level", levelProp.getPath());

        DbProperty greatGrandChildProp = new DbProperty(MyDbObject.lastAdded);
        assertEquals("Incorrect path", "root.child.grandchild.greatgrandchild", greatGrandChildProp.getPath());
    }
    
    
    private interface DbObjectWithIndexedProp extends DbObject
    {
        String getTheProperty();
        List<String> getColours();
    }
    
    public static class MyDbObject extends AbstractDbObject
    {
        public static MyDbObject lastAdded;
        private int level;
        
        public MyDbObject(String name, int level)
        {
            super(null, name);
            this.level = level;
        }
        
        @Override
        public void accept(DbObjectVisitor visitor)
        {
        }
        
        public MyDbObject add(MyDbObject child)
        {
            child.setParent(this);
            lastAdded = child;
            return child;
        }

        public int getLevel()
        {
            return this.level;
        }
    }
}
