/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import static org.alfresco.repo.content.transform.TransformerConfig.ANY;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;


/**
 * Implementation of a {@link TransformerStatistics}.
 * 
 * @author Alan Davis
 */
// TODO These values should be made visible via JMX.
public class TransformerStatisticsImpl implements TransformerStatistics
{
    private final MimetypeService mimetypeService;
    private final String sourceMimetype;
    private final String targetMimetype;
    private final ContentTransformer transformer;
    private final TransformerStatistics parent;
    private final long errorTime;
    
    private double averageTime;
    private long count = 0L;
    private long errorCount = 0L;
    
    public TransformerStatisticsImpl(MimetypeService mimetypeService, String sourceMimetype, String targetMimetype,
            ContentTransformer transformer, TransformerStatistics parent, long errorTime,
            long initialAverageTime, long initialCount)
    {
        this.mimetypeService = mimetypeService;
        this.sourceMimetype = sourceMimetype;
        this.targetMimetype = targetMimetype;
        this.transformer = transformer;
        this.parent = parent;
        this.errorTime = errorTime;
        
        averageTime = initialAverageTime;
        count = initialCount;
    }

    @Override
    public String getSourceExt()
    {
        return ANY.equals(sourceMimetype) ? ANY : mimetypeService.getExtension(sourceMimetype);
    }

    @Override
    public String getTargetExt()
    {
        return ANY.equals(targetMimetype) ? ANY : mimetypeService.getExtension(targetMimetype);
    }

    @Override
    public String getTransformerName()
    {
        return transformer == null ? TransformerConfig.SUMMARY_TRANSFORMER_NAME : transformer.getName();
    }

    @Override
    public synchronized void recordTime(long transformationTime)
    {
        if (count == Long.MAX_VALUE)
        {
            // we have reached the max count - reduce it by half
            // the average fluctuation won't be extreme
            count /= 2L;
        }
        // adjust the average
        count++;
        double diffTime = ((double) transformationTime) - averageTime;
        averageTime += diffTime / (double) count;
        
        if (parent != null)
        {
            parent.recordTime(transformationTime);
        }
    }

    @Override
    public synchronized void recordError()
    {
        if (errorCount < Long.MAX_VALUE)
        {
            errorCount++;
        }
        if (errorTime > 0)
        {
            recordTime(errorTime);
        }
        if (parent != null)
        {
            parent.recordError();
        }
    }
    
    @Override
    public long getCount()
    {
        return count;
    }

    @Override
    public void setCount(long count)
    {
        this.count = count;
    }

    @Override
    public long getErrorCount()
    {
        return errorCount;
    }

    @Override
    public void setErrorCount(long errorCount)
    {
        this.errorCount = errorCount;
    }

    @Override
    public long getAverageTime()
    {
        return (long)averageTime;
    }

    @Override
    public void setAverageTime(long averageTime)
    {
        this.averageTime = (double)averageTime;
    }

    public boolean isSummary()
    {
        return TransformerConfig.ANY.equals(sourceMimetype) && TransformerConfig.ANY.equals(targetMimetype);
    }
    
    
    
    
    
    
    //////////////////////////////////////// TODO Split into summary class ///////////////////////////////////
    
    
    
    
    
//    private enum Property
//    {
//        priority(true)
//        {
//            String getValue(TransformerData bean)
//            {
//                return Integer.toString(bean.getPriority());
//            }
//            void setValue(TransformerData bean, String value)
//            {
//                bean.setPriority(Integer.valueOf(value));
//            }
//        },
//        
//        averageTime(false)
//        {
//            String getValue(TransformerData bean)
//            {
//                return Integer.toString(bean.getPriority());
//            }
//        },
//        
//        count(false)
//        {
//            String getValue(TransformerData bean)
//            {
//                return Integer.toString(bean.getPriority());
//            }
//        },
//        
//        errors(false)
//        {
//            String getValue(TransformerData bean)
//            {
//                return Integer.toString(bean.getPriority());
//            }
//        };
//        
//        private final boolean updatable;
//
//        Property(boolean updatable)
//        {
//            this.updatable = updatable;
//        }
//        
//        abstract String getValue(TransformerData bean);
//
//        void setValue(TransformerData bean, String value)
//        {
//        }
//        
//        public boolean isUpdatable()
//        {
//            return updatable;
//        }
//
//        public static Set<String> getNames()
//        {
//            Set<String> names = new HashSet<String>();
//            for (Property property: Property.class.getEnumConstants())
//            {
//                names.add(property.name());
//            }
//            return names;
//        }
//    };
//    
//    public List<String> getId()
//    {
//        List<String> id = super.getId();
//        
//        id.add(transformerName);
//        
//        if (TransformerConfig.ANY.equals(sourceExt))
//        {
//            id.add(sourceExt);
//        }
//        
//        if (TransformerConfig.ANY.equals(targetExt))
//        {
//            id.add(targetExt);
//        }
//
//        return id;
//    }
//    
//    public boolean isUpdateable(String name)
//    {
//        return Enum.valueOf(Property.class, name).isUpdatable();
//    }
//
//    @Override
//    protected PropertyBackedBeanState createInitialState() throws IOException
//    {
//        return new PropertyBackedBeanState()
//        {
//
//            @Override
//            public Set<String> getPropertyNames()
//            {
//                return Property.getNames();
//            }
//
//            @Override
//            public String getProperty(String name)
//            {
//                return Enum.valueOf(Property.class, name).getValue(TransformerDataImpl.this);
//            }
//
//            @Override
//            public void setProperty(String name, String value)
//            {
//                Enum.valueOf(Property.class, name).setValue(TransformerDataImpl.this, value);
//            }
//
//            @Override
//            public void start()
//            {
//                ;
//            }
//
//            @Override
//            public void stop()
//            {
//                ;
//            }
//        };
//    }
}
