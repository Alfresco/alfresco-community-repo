/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.content.filestore;

import java.io.File;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.textgen.TextGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Provides access to text data that is generated when requested.
 * <p/>
 * The URL has the format: <b>spoofed://{locale=en_GB,seed=12345,length=1024,strings=["Alfresco", "Cloud"]}
 * <p/>
 * The lexicon for the given locale is found by taking the language part of the locale (<b>en</b> in <b>en_GB</b>)
 * and finding the resource <b>alfresco/textgen/lexicon-stem-en.txt</b>.
 * 
 * @see TextGenerator
 * 
 * @author Derek Hulley
 * @since 5.1
 */
public class SpoofedTextContentReader extends AbstractContentReader
{
    public static final String LEXICON_STEM_PATH = "alfresco/textgen/lexicon-stem-@@LOCALE@@.txt";
    public static final String KEY_LOCALE = "locale";
    public static final String KEY_SEED = "seed";
    public static final String KEY_SIZE = "size";
    public static final String KEY_WORDS = "words";
    
    private static Map<Locale, TextGenerator> textGeneratorsByLocale = new HashMap<Locale, TextGenerator>();
    private static ReentrantReadWriteLock textGeneratorsLock = new ReentrantReadWriteLock();
    
    private static final Log logger = LogFactory.getLog(SpoofedTextContentReader.class);
    
    private final TextGenerator textGenerator;
    private final long seed;
    private final long size;
    private final String[] words;
    
    /**
     * Get a text generator for the given locale
     * 
     * @throws RuntimeException         if the locale has no lexicon exists for the locale
     */
    public static TextGenerator getTextGenerator(Locale locale)
    {
        textGeneratorsLock.readLock().lock();
        try
        {
            TextGenerator tg = textGeneratorsByLocale.get(locale);
            if (tg != null)
            {
                return tg;
            }
        }
        finally
        {
            textGeneratorsLock.readLock().unlock();
        }
        // Create one
        textGeneratorsLock.writeLock().lock();
        try
        {
            // Double check
            TextGenerator tg = textGeneratorsByLocale.get(locale);
            if (tg != null)
            {
                return tg;
            }
            // Create it
            String lang = locale.getLanguage();
            String configPath = LEXICON_STEM_PATH.replace("@@LOCALE@@", lang);
            tg = new TextGenerator(configPath);
            // Store it
            textGeneratorsByLocale.put(locale, tg);
            // Done
            return tg;
        }
        finally
        {
            textGeneratorsLock.writeLock().unlock();
        }
    }
    
    /**
     * Helper to create a content URL that represents spoofed text
     * 
     * @param locale                    the text local (must be supported by an appropriate lexicon config resource)
     * @param seed                      numerical seed to ensure repeatable sequences of random text
     * @param size                      the size (bytes) of the text to generate
     * @param words                     additional words with decreasing frequency
     * @return                          the content URL
     * 
     * @throws IllegalArgumentException if the resulting URL exceeds 255 characters
     */
    @SuppressWarnings("unchecked")
    public static String createContentUrl(Locale locale, long seed, long size, String ... words)
    {
        if (locale == null || size < 0L)
        {
            throw new IllegalArgumentException("Locale must be supplied and size must be zero or greater.");
        }
        
        // Make sure that there is a text generator available
        SpoofedTextContentReader.getTextGenerator(locale);
        
        // Build map
        String url = null;
        try
        {
             JSONObject jsonObj = new JSONObject();
            jsonObj.put(KEY_LOCALE, locale.toString());
            jsonObj.put(KEY_SEED, Long.valueOf(seed).toString());
            jsonObj.put(KEY_SIZE, Long.valueOf(size).toString());
            JSONArray jsonWords = new JSONArray();
            for (String word : words)
            {
                if (word == null)
                {
                    throw new IllegalArgumentException("Words to inject into the document may not be null.");
                }
                jsonWords.add(word);
            }
            jsonObj.put(KEY_WORDS, jsonWords);
            
            url = FileContentStore.SPOOF_PROTOCOL + "://" + jsonObj.toString();
            if (url.length() > 255)
            {
                throw new IllegalArgumentException("Content URLs can be up to 255 characters.  Have " + url.length() + " characters: " + url);
            }
            return url;
        }
        catch (IllegalArgumentException e)
        {
            // Let these out as they are
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to create content URL using " + locale + ", " + seed + ", " + size + ", " + words, e);
        }
    }
    
    /**
     * @param url           a URL describing the type of text to produce (see class comments)
     */
    public SpoofedTextContentReader(String url)
    {
        super(url);
        if (url.length() > 255)
        {
            throw new IllegalArgumentException("A content URL is limited to 255 characters: " + url);
        }
        // Split out data part
        int index = url.indexOf(ContentStore.PROTOCOL_DELIMITER);
        if (index <= 0 || !url.startsWith(FileContentStore.SPOOF_PROTOCOL))
        {
            throw new RuntimeException("URL not supported by this reader: " + url);
        }
        String urlData = url.substring(index + 3, url.length());
        // Parse URL
        try
        {
            JSONParser parser = new JSONParser();
            JSONObject mappedData = (JSONObject) parser.parse(urlData);

            String jsonLocale = mappedData.containsKey(KEY_LOCALE) ? (String) mappedData.get(KEY_LOCALE) : Locale.ENGLISH.toString();
            String jsonSeed = mappedData.containsKey(KEY_SEED) ? (String) mappedData.get(KEY_SEED) : "0";
            String jsonSize = mappedData.containsKey(KEY_SIZE) ? (String) mappedData.get(KEY_SIZE) : "1024";
            JSONArray jsonWords = mappedData.containsKey(KEY_WORDS) ? (JSONArray) mappedData.get(KEY_WORDS) : new JSONArray();
            // Get the text generator
            Locale locale = new Locale(jsonLocale);
            seed = Long.valueOf(jsonSeed);
            size = Long.valueOf(jsonSize);
            words = new String[jsonWords.size()];
            for (int i = 0; i < words.length; i++)
            {
                words[i] = (String) jsonWords.get(i);
            }
            this.textGenerator = SpoofedTextContentReader.getTextGenerator(locale);
            // Set the base class storage for external information
            super.setLocale(locale);
            super.setEncoding("UTF-8");
            super.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to interpret URL: " + url, e);
        }
        
    }
    
    /**
     * @return              <tt>true</tt> always
     */
    public boolean exists()
    {
        return true;
    }

    /**
     * @return          the text generator that will make the spoofed text
     */
    public TextGenerator getTextGenerator()
    {
        return textGenerator;
    }

    /**
     * @return          the random seed for the spoofed text
     */
    public long getSeed()
    {
        return seed;
    }

    /**
     * @return          the words to add to the spoofed text
     */
    public String[] getWords()
    {
        return words;
    }

    /**
     * @return          spoofed text size
     */
    public long getSize()
    {
        return size;
    }

    /**
     * @see File#lastModified()
     */
    public long getLastModified()
    {
        return 0L;
    }

    /**
     * The URL of the write is known from the start and this method contract states
     * that no consideration needs to be taken w.r.t. the stream state.
     */
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        SpoofedTextContentReader reader = new SpoofedTextContentReader(getContentUrl());
        return reader;
    }
    
    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        try
        {
            // Interpret the URL to generate the text
            InputStream textStream = textGenerator.getInputStream(seed, size, words);
            ReadableByteChannel textChannel = Channels.newChannel(textStream);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Opened read channel to random text for URL: " + getContentUrl());
            }
            return textChannel;
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to read channel: " + this, e);
        }
    }
    
    @Override
    protected final void setContentUrl(String contentUrl)
    {
        throw new UnsupportedOperationException("The URL is static and cannot be changed.");
    }
}
