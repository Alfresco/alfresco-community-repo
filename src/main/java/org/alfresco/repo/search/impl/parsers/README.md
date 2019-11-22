# Antlr Grammars

The grammar files in this package (e.g. `FTS.g`) are written using the Antlr 3 syntax.
The java lexer and parser files are generated from the grammar files, along with the files
containing the tokens.

The easiest way to make changes to the grammar files is by using
[AntlrWorks 1.5](https://www.antlr3.org/download.html). This can be used to step through the
evaluations made while parsing troublesome input, and can help visualise the resulting 
decision tree. AntlrWorks includes a menu option to compile the grammar, and will generate
replacements for the existing files.

AntlrWorks will fail to parse the grammar file if it contains references to unknown classes
(for example FTS.g contains references to MismatchedTokenException and FTSQueryException).
To work around this issue then temporarily replace these exceptions with RuntimeExceptions.
Once changes to the grammar have been completed then these can either be manually fixed in
the generated output or compilation can be done via the command line. This requires the
[antlr jar](http://www.antlr3.org/download/antlr-3.5.2-complete-no-st3.jar) to be downloaded:
```bash
java -jar ~/Downloads/antlr-3.5.2-complete-no-st3.jar -o . src/main/java/org/alfresco/repo/search/impl/parsers/FTS.g
```

For some grammars then Antlr generates Java files that cannot be compiled due to the
64KB limit on methods. Attempting to compile this code will result in the error "Code too
large". In particular this is true of [FTS.g](FTS.g).  To work around this issue then once
the code has been generated using Antlr it must be refactored to ensure no single method is
too large.  For example, in [FTSParser.java](FTSParser.java) several larger blocks of code
have been manually extracted to smaller methods that look like:
```java
        private int specialStateTransition7(int LA17_9)
        {
            int s;
            s = -1;
            if ( (LA17_9==STAR) ) {s = 25;}
            else if ( (LA17_9==DOTDOT) && (synpred3_FTS())) {s = 27;}
            else if ( (LA17_9==COMMA||LA17_9==DOT) && (synpred5_FTS())) {s = 28;}
            ...
            else if ( (LA17_9==MINUS) && (synpred5_FTS())) {s = 57;}
            return s;
        }
```

To verify the grammar behaves how we expect then there are unit tests in
`org.alfresco.repo.search.impl.parsers`. These use gunit files to specify expected input and
output pairs for different entry points in the grammar.  More details about guint file can
be found on the [Antlr 3 wiki](https://theantlrguy.atlassian.net/wiki/spaces/ANTLR3/pages/2687338/gUnit+-+Grammar+Unit+Testing).
