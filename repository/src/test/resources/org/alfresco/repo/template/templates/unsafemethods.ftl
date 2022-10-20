Freemarker Unsafe Methods Testing
=================================
<#assign string = "foo">

java.lang.Thread.getName()<#if (thread.getName())??>${allowedText}<#else>${blockedText}</#if>
java.lang.Thread.setName(java.lang.String)<#if (thread.setName(string))??>${allowedText}<#else>${blockedText}</#if>
java.lang.Thread.onSpinWait()<#if (thread.onSpinWait())??>${allowedText}<#else>${blockedText}</#if>
