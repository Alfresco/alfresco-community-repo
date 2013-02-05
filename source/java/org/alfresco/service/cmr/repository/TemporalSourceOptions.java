/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.AbstractTransformationSourceOptions;

/**
 * Time-based content conversion options to specify an offset and duration.
 * Useful for audio and video.
 * <p>
 * If only the offset is specified transformers should attempt
 * a transform from that offset to the end if possible.
 * <p>
 * If only a duration is specified transformers should attempt
 * a transform from the start until that duration is reached if possible.
 * 
 * @author Ray Gauss II
 */
public class TemporalSourceOptions extends AbstractTransformationSourceOptions
{

    /** The offset time code from which to start the transformation */
    private String offset;
    
    /** The duration of the target video after the transformation */
    private String duration;
    
    @Override
    public boolean isApplicableForMimetype(String sourceMimetype)
    {
        return ((sourceMimetype != null && 
                sourceMimetype.startsWith(MIMETYPE_VIDEO_PREFIX) || 
                sourceMimetype.startsWith(MIMETYPE_AUDIO_PREFIX)) ||
                super.isApplicableForMimetype(sourceMimetype));
    }
    
    /**
     * Gets the offset time code from which to start the transformation 
     * with a format of hh:mm:ss[.xxx]
     * 
     * @return the offset
     */
    public String getOffset()
    {
        return offset;
    }

    /**
     * Sets the offset time code from which to start the transformation 
     * with a format of hh:mm:ss[.xxx]
     * 
     * @param offset
     */
    public void setOffset(String offset)
    {
        this.offset = offset;
    }

    /**
     * Gets the duration of the source to read 
     * with a format of hh:mm:ss[.xxx]
     * 
     * @return
     */
    public String getDuration()
    {
        return duration;
    }

    /**
     * Sets the duration of the source to read 
     * with a format of hh:mm:ss[.xxx]
     * 
     * @param duration
     */
    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    @Override
    public TransformationSourceOptions mergedOptions(TransformationSourceOptions overridingOptions)
    {
        if (overridingOptions instanceof TemporalSourceOptions)
        {
            TemporalSourceOptions mergedOptions = (TemporalSourceOptions) super.mergedOptions(overridingOptions);

            if (((TemporalSourceOptions) overridingOptions).getOffset() != null)
            {
                mergedOptions.setOffset(((TemporalSourceOptions) overridingOptions).getOffset());
            }
            if (((TemporalSourceOptions) overridingOptions).getDuration() != null)
            {
                mergedOptions.setDuration(((TemporalSourceOptions) overridingOptions).getDuration());
            }
            return mergedOptions;
        }
        return null;
    }
    
    @Override
    public TransformationSourceOptionsSerializer getSerializer()
    {
        return TemporalSourceOptions.createSerializerInstance();
    }
    
    /**
     * Creates an instance of the options serializer
     * 
     * @return the options serializer
     */
    public static TransformationSourceOptionsSerializer createSerializerInstance()
    {
        return (new TemporalSourceOptions()).new TemporalSourceOptionsSerializer();
    }
    
    /**
     * Serializer for temporal source options
     */
    public class TemporalSourceOptionsSerializer implements TransformationSourceOptionsSerializer
    {
        public static final String PARAM_SOURCE_TIME_OFFSET = "source_time_offset";
        public static final String PARAM_SOURCE_TIME_DURATION = "source_time_duration";
        
        @Override
        public TransformationSourceOptions deserialize(SerializedTransformationOptionsAccessor serializedOptions)
        {
            String offset = serializedOptions.getCheckedParam(PARAM_SOURCE_TIME_OFFSET, String.class);
            String duration = serializedOptions.getCheckedParam(PARAM_SOURCE_TIME_DURATION, String.class);
            
            if (offset == null && duration == null)
            {
                return null;
            }
            
            TemporalSourceOptions sourceOptions = new TemporalSourceOptions();
            sourceOptions.setOffset(offset);
            sourceOptions.setDuration(duration);
            return sourceOptions;
        }

        @Override
        public void serialize(TransformationSourceOptions sourceOptions, 
                Map<String, Serializable> parameters)
        {
            if (parameters == null || sourceOptions == null)
                return;
            TemporalSourceOptions temporalSourceOptions = (TemporalSourceOptions) sourceOptions;
            parameters.put(PARAM_SOURCE_TIME_OFFSET, temporalSourceOptions.getOffset());
            parameters.put(PARAM_SOURCE_TIME_DURATION, temporalSourceOptions.getDuration());
        }
    }


}
