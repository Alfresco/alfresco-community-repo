/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.domain.propval;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.alfresco.util.Pair;

/**
 * Creates property values suitable for use in testing.
 * 
 * @author Matt Ward
 */
public class PropValGenerator
{
    private static final Random rand = new Random();
    private final PropertyValueDAO propertyValueDAO;
    private final DoubleGen doubleGen = new DoubleGen();
    private final DateGen dateGen = new DateGen();
    private final SerializableGen serGen = new SerializableGen();
    
    public PropValGenerator(PropertyValueDAO propertyValueDAO)
    {
        this.propertyValueDAO = propertyValueDAO;
    }
    
    public String createUniqueString()
    {
        // No need to do anything more clever than create a UUID.
        return UUID.randomUUID().toString();
    }
    
    public Double createUniqueDouble()
    {
        return doubleGen.getUnusedValue();
    }
    
    public Date createUniqueDate()
    {
        return dateGen.getUnusedValue();
    }
    
    public Serializable createUniqueSerializable()
    {
        return serGen.getUnusedValue();
    }
    
    
    private class DoubleGen extends UniqueValueGenerator<Double>
    {
        @Override
        protected Double createValue()
        {
            return (Math.pow(2,32)) + (rand.nextDouble() * (Math.pow(2,32) - Math.pow(2,31)));
        }

        @Override
        protected Pair<Long, Double> getExistingValue(Double value)
        {
            return propertyValueDAO.getPropertyDoubleValue(value);
        }
    }
    
    private class DateGen extends UniqueValueGenerator<Date>
    {
        @Override
        protected Date createValue()
        {
            Random rand = new Random();
            Date date = new Date(rand.nextLong());
            // Dates are stored to day precision, make sure we return the
            // same value that will be stored, for comparison in assert statements etc.
            Date truncDate = PropertyDateValueEntity.truncateDate(date);
            return truncDate;
        }

        @Override
        protected Pair<Long, Date> getExistingValue(Date value)
        {
            return propertyValueDAO.getPropertyDateValue(value);
        }
    }

    private class SerializableGen extends UniqueValueGenerator<Serializable>
    {
        @Override
        protected Serializable createValue()
        {
            return new MySerializable();
        }

        @Override
        protected Pair<Long, Serializable> getExistingValue(Serializable value)
        {
            return propertyValueDAO.getPropertyValue(value);
        }
    }
        
    private static class MySerializable implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final Long val;
        
        public MySerializable()
        {
            val = rand.nextLong();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.val == null) ? 0 : this.val.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            MySerializable other = (MySerializable) obj;
            if (this.val == null)
            {
                if (other.val != null) return false;
            }
            else if (!this.val.equals(other.val)) return false;
            return true;
        }
    }
    
    /**
     * Generate values that aren't currently in the properties tables. By trying random values
     * several times until an unused value is used. This is to help avoid red builds, since the
     * assumption by the orphaned property cleanup test is that the properties are not in use
     * (otherwise they won't be cleaned up!)
     */
    private abstract class UniqueValueGenerator<T>
    {
        private final int maxTries = 5;
        protected abstract T createValue();
        protected abstract Pair<Long, T> getExistingValue(T value);
        
        public T getUnusedValue()
        {
            int tries = 0;
            T value = null;
            boolean exists = true;
            while (exists)
            {
                if (++tries > maxTries)
                {
                    throw new RuntimeException("Unable to generate unused value in " + maxTries + " tries.");
                }
                value = createValue();
                assertNotNull("Value generator should not generate a null value, but did.", value);
                // Make sure the value isn't already present in the properties tables.
                exists = (getExistingValue(value) != null);
            }
            return value;
        }
    }
}
