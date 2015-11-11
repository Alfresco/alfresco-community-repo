package org.alfresco.repo.search.impl.lucene.fts;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.text.MessageFormat;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.surf.util.I18NUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FullTextSearchIndexerBootstrapBeanTest
{
    private FullTextSearchIndexerBootstrapBean bean;


    private final static Appender appender = mock(Appender.class);
    private final static Logger logger = Logger.getRootLogger();

    @BeforeClass
    public static void setupBeforeClass() {
        I18NUtil.registerResourceBundle("alfresco.messages.system-messages");
        I18NUtil.registerResourceBundle("alfresco.version");
        logger.addAppender(appender);
    }

    @Test
    public void test() {
        // when
        bean = new FullTextSearchIndexerBootstrapBean();
        try {
            bean.onBootstrap(null);
        }
        catch (NullPointerException n)
        {
            // expected for this test, since there is no nodeService
        }
        
        // then
        ArgumentCaptor<LoggingEvent> argument = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(appender).doAppend(argument.capture());
        assertThat(argument.getValue().getLevel(), is(Level.ERROR));

        String majorVersion = I18NUtil.getMessage("version.major");
        String minorVersion = I18NUtil.getMessage("version.minor");
        String pattern = "docs.alfresco.com/{0}";
        pattern = MessageFormat.format(pattern, majorVersion + "." + minorVersion);
        assertTrue(String.valueOf(argument.getValue().getMessage()).contains(pattern));
    }

    @AfterClass
    public static void cleanupAfterClass() {
        logger.removeAppender(appender);
    }
}
