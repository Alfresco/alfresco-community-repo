package org.alfresco.repo.activities;

public class SiteActivityTestCaseInsensitivity extends AbstractSiteActivityTest
{
    static
    {
        System.setProperty("user.name.caseSensitive", "false");
    }
}
