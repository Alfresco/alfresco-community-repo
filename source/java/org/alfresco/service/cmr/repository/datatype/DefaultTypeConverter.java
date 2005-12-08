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
package org.alfresco.service.cmr.repository.datatype;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;

/**
 * Support for generic conversion between types.
 * 
 * Additional conversions may be added. Basic interoperabikitynos supported.
 * 
 * Direct conversion and two stage conversions via Number are supported. We do
 * not support conversion by any route at the moment
 * 
 * TODO: Add support for Path 
 * 
 * TODO: Add support for lucene
 * 
 * TODO: Add suport to check of a type is convertable
 * 
 * TODO: Support for dynamically managing conversions
 * 
 * @author andyh
 * 
 */
public class DefaultTypeConverter
{

    /**
     * Default Type Converter
     */
    public static TypeConverter INSTANCE = new TypeConverter();

    /**
     * Initialise default set of Converters
     */
    static
    {
        
        //
        // From string
        //

        INSTANCE.addConverter(String.class, Boolean.class, new TypeConverter.Converter<String, Boolean>()
        {
            public Boolean convert(String source)
            {
                return Boolean.valueOf(source);
            }
        });

        INSTANCE.addConverter(String.class, Character.class, new TypeConverter.Converter<String, Character>()
        {
            public Character convert(String source)
            {
                if ((source == null) || (source.length() == 0))
                {
                    return null;
                }
                return Character.valueOf(source.charAt(0));
            }
        });

        INSTANCE.addConverter(String.class, Number.class, new TypeConverter.Converter<String, Number>()
        {
            public Number convert(String source)
            {
                try
                {
                    return DecimalFormat.getNumberInstance().parse(source);
                }
                catch (ParseException e)
                {
                    throw new TypeConversionException("Failed to parse number " + source, e);
                }
            }
        });

        INSTANCE.addConverter(String.class, Byte.class, new TypeConverter.Converter<String, Byte>()
        {
            public Byte convert(String source)
            {
                return Byte.valueOf(source);
            }
        });

        INSTANCE.addConverter(String.class, Short.class, new TypeConverter.Converter<String, Short>()
        {
            public Short convert(String source)
            {
                return Short.valueOf(source);
            }
        });

        INSTANCE.addConverter(String.class, Integer.class, new TypeConverter.Converter<String, Integer>()
        {
            public Integer convert(String source)
            {
                return Integer.valueOf(source);
            }
        });

        INSTANCE.addConverter(String.class, Long.class, new TypeConverter.Converter<String, Long>()
        {
            public Long convert(String source)
            {
                return Long.valueOf(source);
            }
        });

        INSTANCE.addConverter(String.class, Float.class, new TypeConverter.Converter<String, Float>()
        {
            public Float convert(String source)
            {
                return Float.valueOf(source);
            }
        });

        INSTANCE.addConverter(String.class, Double.class, new TypeConverter.Converter<String, Double>()
        {
            public Double convert(String source)
            {
                return Double.valueOf(source);
            }
        });

        INSTANCE.addConverter(String.class, BigInteger.class, new TypeConverter.Converter<String, BigInteger>()
        {
            public BigInteger convert(String source)
            {
                return new BigInteger(source);
            }
        });

        INSTANCE.addConverter(String.class, BigDecimal.class, new TypeConverter.Converter<String, BigDecimal>()
        {
            public BigDecimal convert(String source)
            {
                return new BigDecimal(source);
            }
        });

        INSTANCE.addConverter(String.class, Date.class, new TypeConverter.Converter<String, Date>()
        {
            public Date convert(String source)
            {
                Date date = ISO8601DateFormat.parse(source);
                if (date == null)
                {
                    throw new TypeConversionException("Failed to parse date " + source);
                }
                return date;
            }
        });

        INSTANCE.addConverter(String.class, Duration.class, new TypeConverter.Converter<String, Duration>()
        {
            public Duration convert(String source)
            {
                return new Duration(source);
            }
        });
        
        INSTANCE.addConverter(String.class, QName.class, new TypeConverter.Converter<String, QName>()
        {
            public QName convert(String source)
            {
                return QName.createQName(source);
            }
        });
        
        INSTANCE.addConverter(String.class, ContentData.class, new TypeConverter.Converter<String, ContentData>()
        {
            public ContentData convert(String source)
            {
                return ContentData.createContentProperty(source);
            }
    
        });

        INSTANCE.addConverter(String.class, NodeRef.class, new TypeConverter.Converter<String, NodeRef>()
                {
                    public NodeRef convert(String source)
                    {
                        return new NodeRef(source);
                    }
            
                });

        INSTANCE.addConverter(String.class, InputStream.class, new TypeConverter.Converter<String, InputStream>()
        {
            public InputStream convert(String source)
            {
                try
                {
                    return new ByteArrayInputStream(source.getBytes("UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    throw new TypeConversionException("Encoding not supported", e);
                }
            }
        });

        
        //
        // Number to Subtypes and Date
        //

        INSTANCE.addConverter(Number.class, Byte.class, new TypeConverter.Converter<Number, Byte>()
        {
            public Byte convert(Number source)
            {
                return Byte.valueOf(source.byteValue());
            }
        });

        INSTANCE.addConverter(Number.class, Short.class, new TypeConverter.Converter<Number, Short>()
        {
            public Short convert(Number source)
            {
                return Short.valueOf(source.shortValue());
            }
        });

        INSTANCE.addConverter(Number.class, Integer.class, new TypeConverter.Converter<Number, Integer>()
        {
            public Integer convert(Number source)
            {
                return Integer.valueOf(source.intValue());
            }
        });

        INSTANCE.addConverter(Number.class, Long.class, new TypeConverter.Converter<Number, Long>()
        {
            public Long convert(Number source)
            {
                return Long.valueOf(source.longValue());
            }
        });

        INSTANCE.addConverter(Number.class, Float.class, new TypeConverter.Converter<Number, Float>()
        {
            public Float convert(Number source)
            {
                return Float.valueOf(source.floatValue());
            }
        });

        INSTANCE.addConverter(Number.class, Double.class, new TypeConverter.Converter<Number, Double>()
        {
            public Double convert(Number source)
            {
                return Double.valueOf(source.doubleValue());
            }
        });

        INSTANCE.addConverter(Number.class, Date.class, new TypeConverter.Converter<Number, Date>()
        {
            public Date convert(Number source)
            {
                return new Date(source.longValue());
            }
        });

        INSTANCE.addConverter(Number.class, String.class, new TypeConverter.Converter<Number, String>()
        {
            public String convert(Number source)
            {
                return source.toString();
            }
        });

        INSTANCE.addConverter(Number.class, BigInteger.class, new TypeConverter.Converter<Number, BigInteger>()
        {
            public BigInteger convert(Number source)
            {
                if (source instanceof BigDecimal)
                {
                    return ((BigDecimal) source).toBigInteger();
                }
                else
                {
                    return BigInteger.valueOf(source.longValue());
                }
            }
        });

        INSTANCE.addConverter(Number.class, BigDecimal.class, new TypeConverter.Converter<Number, BigDecimal>()
        {
            public BigDecimal convert(Number source)
            {
                if (source instanceof BigInteger)
                {
                    return new BigDecimal((BigInteger) source);
                }
                else
                {
                    return BigDecimal.valueOf(source.longValue());
                }
            }
        });
        
        INSTANCE.addDynamicTwoStageConverter(Number.class, String.class, InputStream.class);
        
        //
        // Date, Timestamp ->
        //

        INSTANCE.addConverter(Timestamp.class, Date.class, new TypeConverter.Converter<Timestamp, Date>()
        {
            public Date convert(Timestamp source)
            {
                return new Date(source.getTime());
            }
        });
        
        INSTANCE.addConverter(Date.class, Number.class, new TypeConverter.Converter<Date, Number>()
        {
            public Number convert(Date source)
            {
                return Long.valueOf(source.getTime());
            }
        });

        INSTANCE.addConverter(Date.class, String.class, new TypeConverter.Converter<Date, String>()
        {
            public String convert(Date source)
            {
                return ISO8601DateFormat.format(source);
            }
        });
        
        INSTANCE.addConverter(Date.class, Calendar.class, new TypeConverter.Converter<Date, Calendar>()
        {
            public Calendar convert(Date source)
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(source);
                return calendar;
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Date.class, String.class, InputStream.class);

        //
        // Boolean ->
        //

        INSTANCE.addConverter(Boolean.class, String.class, new TypeConverter.Converter<Boolean, String>()
        {
            public String convert(Boolean source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Boolean.class, String.class, InputStream.class);

        //
        // Character ->
        //

        INSTANCE.addConverter(Character.class, String.class, new TypeConverter.Converter<Character, String>()
        {
            public String convert(Character source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Character.class, String.class, InputStream.class);

        //
        // Duration ->
        //

        INSTANCE.addConverter(Duration.class, String.class, new TypeConverter.Converter<Duration, String>()
        {
            public String convert(Duration source)
            {
                return source.toString();
            }

        });

        INSTANCE.addDynamicTwoStageConverter(Duration.class, String.class, InputStream.class);

        //
        // Byte
        //
        
        INSTANCE.addConverter(Byte.class, String.class, new TypeConverter.Converter<Byte, String>()
        {
            public String convert(Byte source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Byte.class, String.class, InputStream.class);
        
        //
        // Short
        //

        INSTANCE.addConverter(Short.class, String.class, new TypeConverter.Converter<Short, String>()
        {
            public String convert(Short source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Short.class, String.class, InputStream.class);

        //
        // Integer
        //

        INSTANCE.addConverter(Integer.class, String.class, new TypeConverter.Converter<Integer, String>()
        {
            public String convert(Integer source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Integer.class, String.class, InputStream.class);
        
        //
        // Long
        //

        INSTANCE.addConverter(Long.class, String.class, new TypeConverter.Converter<Long, String>()
        {
            public String convert(Long source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Long.class, String.class, InputStream.class);

        //
        // Float
        //

        INSTANCE.addConverter(Float.class, String.class, new TypeConverter.Converter<Float, String>()
        {
            public String convert(Float source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Float.class, String.class, InputStream.class);

        //
        // Double
        //

        INSTANCE.addConverter(Double.class, String.class, new TypeConverter.Converter<Double, String>()
        {
            public String convert(Double source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Double.class, String.class, InputStream.class);

        //
        // BigInteger
        //

        INSTANCE.addConverter(BigInteger.class, String.class, new TypeConverter.Converter<BigInteger, String>()
        {
            public String convert(BigInteger source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(BigInteger.class, String.class, InputStream.class);

        //
        // Calendar
        //
        
        INSTANCE.addConverter(Calendar.class, Date.class, new TypeConverter.Converter<Calendar, Date>()
        {
            public Date convert(Calendar source)
            {
                return source.getTime();
            }
        });
        
        INSTANCE.addConverter(Calendar.class, String.class, new TypeConverter.Converter<Calendar, String>()
        {
            public String convert(Calendar source)
            {
                return ISO8601DateFormat.format(source.getTime());
            }
        });
        
        //
        // BigDecimal
        //

        INSTANCE.addConverter(BigDecimal.class, String.class, new TypeConverter.Converter<BigDecimal, String>()
        {
            public String convert(BigDecimal source)
            {
                return source.toString();
            }
        });
        
        INSTANCE.addDynamicTwoStageConverter(BigDecimal.class, String.class, InputStream.class);

        //
        // QName
        //

        INSTANCE.addConverter(QName.class, String.class, new TypeConverter.Converter<QName, String>()
        {
            public String convert(QName source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(QName.class, String.class, InputStream.class);

        //
        // NodeRef
        //
        
        INSTANCE.addConverter(NodeRef.class, String.class, new TypeConverter.Converter<NodeRef, String>()
        {
            public String convert(NodeRef source)
            {
                return source.toString();
            }
        });
        
        INSTANCE.addDynamicTwoStageConverter(NodeRef.class, String.class, InputStream.class);
        
        //
        // ContentData
        //

        INSTANCE.addConverter(ContentData.class, String.class, new TypeConverter.Converter<ContentData, String>()
        {
            public String convert(ContentData source)
            {
                return source.toString();
            }
        });
                
        INSTANCE.addDynamicTwoStageConverter(ContentData.class, String.class, InputStream.class);
        
        //
        // Path
        //
        
        INSTANCE.addConverter(Path.class, String.class, new TypeConverter.Converter<Path, String>()
        {
            public String convert(Path source)
            {
                return source.toString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(Path.class, String.class, InputStream.class);
        
        //
        // Content Reader
        //
        
        INSTANCE.addConverter(ContentReader.class, InputStream.class, new TypeConverter.Converter<ContentReader, InputStream>()
        {
            public InputStream convert(ContentReader source)
            {
                return source.getContentInputStream();
            }
        });
        
        INSTANCE.addConverter(ContentReader.class, String.class, new TypeConverter.Converter<ContentReader, String>()
        {
            public String convert(ContentReader source)
            {
                String encoding = source.getEncoding();
                if (encoding == null || !encoding.equals("UTF-8"))
                {
                    throw new TypeConversionException("Cannot convert non UTF-8 streams to String.");
                }
                
                // TODO: Throw error on size limit
        
                return source.getContentString();
            }
        });

        INSTANCE.addDynamicTwoStageConverter(ContentReader.class, String.class, Date.class);
        
        INSTANCE.addDynamicTwoStageConverter(ContentReader.class, String.class, Double.class);
        
        INSTANCE.addDynamicTwoStageConverter(ContentReader.class, String.class, Long.class);

        INSTANCE.addDynamicTwoStageConverter(ContentReader.class, String.class, Boolean.class);

        INSTANCE.addDynamicTwoStageConverter(ContentReader.class, String.class, QName.class);

        INSTANCE.addDynamicTwoStageConverter(ContentReader.class, String.class, Path.class);

        INSTANCE.addDynamicTwoStageConverter(ContentReader.class, String.class, NodeRef.class);

        //
        // Input Stream
        //
        
        INSTANCE.addConverter(InputStream.class, String.class, new TypeConverter.Converter<InputStream, String>()
        {
            public String convert(InputStream source)
            {
                try
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = source.read(buffer)) > 0)
                    {
                        out.write(buffer, 0, read);
                    }
                    byte[] data = out.toByteArray();
                    return new String(data, "UTF-8");
                } 
                catch (UnsupportedEncodingException e)
                {
                    throw new TypeConversionException("Cannot convert input stream to String.", e);
                }
                catch (IOException e)
                {
                    throw new TypeConversionException("Conversion from stream to string failed", e);
                }
                finally
                {
                    if (source != null)
                    {
                        try { source.close(); } catch(IOException e) {};
                    }
                }
            }
        });

        INSTANCE.addDynamicTwoStageConverter(InputStream.class, String.class, Date.class);
        
        INSTANCE.addDynamicTwoStageConverter(InputStream.class, String.class, Double.class);
        
        INSTANCE.addDynamicTwoStageConverter(InputStream.class, String.class, Long.class);

        INSTANCE.addDynamicTwoStageConverter(InputStream.class, String.class, Boolean.class);

        INSTANCE.addDynamicTwoStageConverter(InputStream.class, String.class, QName.class);

        INSTANCE.addDynamicTwoStageConverter(InputStream.class, String.class, Path.class);

        INSTANCE.addDynamicTwoStageConverter(InputStream.class, String.class, NodeRef.class);
        
    }

}
