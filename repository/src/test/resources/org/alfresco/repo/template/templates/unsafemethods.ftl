Freemarker Unsafe Methods Testing
=================================
java.lang.Thread.getId()<#if (thread.getId())??>${allowedText}<#else>${blockedText}</#if>
java.lang.Thread.interrupt()<#if (thread.interrupt())??>${allowedText}<#else>${blockedText}</#if>
java.lang.Thread.currentThread()<#if (thread.currentThread())??>${allowedText}<#else>${blockedText}</#if>
