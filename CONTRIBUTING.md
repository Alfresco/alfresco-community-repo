### Contributing
Thanks for taking the time to contribute!

The following is a set of guidelines for contributing to this library. Most of them will make the life of the reviewer easier and therefore decrease the time required for the patch go to the next version of the library.

Please, take a look at the contribution information in the [Community Site](https://community.alfresco.com/docs/DOC-6385-project-overview-repository)

#### Reporting bugs
The bug can be submitted as an issue on GitHub. But the best way to report a bug is to create an issue in [JIRA tracker](https://issues.alfresco.com). Ideally supported by a good pull request.

#### Suggesting enhancements
The enhancements can be submitted as an issue on GitHub.

#### Pull requests
* Describe what is in the code and include the JIRA number of the reported bug if applicable.
* Follow the [Style guides](#style-guides)
* Add/modify the tests to check the new code. The test coverage should be enough to support all of the changes to prevent regressions in future.
Please, pay attention to the level of test being done. It is preferred to create unit tests as opposing to system/integration tests. Unit tests are simpler, easier to maintain and take less time to run.

#### Style guides

##### Code formatting
* Charset is UTF-8
* Line endings are CRLF
* All braces are on new lines
* The braces are enforced everywhere even if not explicitly required
* The multi statement constructions like *try-catch-finally* have each statement on new line 
* Wrap code at 255 characters
* Tabs are substituted with 4 spaces
* Empty lines do not have tabs/spaces
* All new public methods have JavaDoc
* The JavaDoc should be compliant with [Java8 DocLint](http://openjdk.java.net/jeps/172)
* If the code is not self explanatory, then comments/JavaDoc should be added as appropriate. Excessive comments should be avoided.
* The strings which are shown to the user (in the UI) should be put in localization property bundles. Our localization team will handle the translations if required.

##### Commit message
* Separate subject from body with a blank line
* Limit the subject line to 50 characters
* Capitalize the subject line
* Do not end the subject line with a period
* Use the imperative mood in the subject line
* Include JIRA numbers in the subject line
* Wrap the body at 72 characters
* Use the body to explain what and why vs. how
* Use a hyphen as a bullet point in the lists

Example:
~~~
ALF-12345 Summarize changes in around 50 characters or less

More detailed explanatory text, if necessary. Wrap it to about 72
characters or so. In some contexts, the first line is treated as the
subject of the commit and the rest of the text as the body. The
blank line separating the summary from the body is critical (unless
you omit the body entirely); various tools like `log`, `shortlog`
and `rebase` can get confused if you run the two together.

Explain the problem that this commit is solving. Focus on why you
are making this change as opposed to how (the code explains that).
Are there side effects or other unintuitive consequences of this
change? Here's the place to explain them.

Further paragraphs come after blank lines.

 - Bullet points are okay, too
 - Use a hyphen for the bullet, preceded
   by a single space
~~~
