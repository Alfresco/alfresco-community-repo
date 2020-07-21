package org.alfresco.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This empty marker annotation is added to make sure .class files are actually generated
 * for package-info.java files. This allow to speed up incremental compilation time,
 * so that each build tool will be able to properly detect differences between sources and
 * .class compiled files.
 *
 * See https://jira.codehaus.org/browse/MCOMPILER-205?focusedCommentId=326795&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-326795
 * for more details
 *
 * NOTE: This annotation should be added in each package-info.java file
 * @author Gabriele Columbro
 *
 */
@Retention(RetentionPolicy.SOURCE)
public @interface PackageMarker {

}