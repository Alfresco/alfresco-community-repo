package org.alfresco.rest.framework.resource.parameters.where;

import org.alfresco.rest.antlr.WhereClauseLexer;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes a String expression using Antlr, it uses the Where grammar for the public api.
 */
public class WhereCompiler {
	
    private static Log logger = LogFactory.getLog(WhereCompiler.class);
    
	public static CommonTree compileWhereClause(String expression) throws RecognitionException {
		//lexer splits input into tokens
		ANTLRStringStream input = new ANTLRStringStream(expression);
		TokenStream tokens = new CommonTokenStream(new WhereClauseLexer(input));
		
		//parser generates abstract syntax tree
		WhereClauseParser parser = new WhereClauseParser(tokens);
	    WhereClauseParser.whereclause_return ret = parser.whereclause();
		
		//acquire parse result
		CommonTree ast = (CommonTree) ret.getTree();
		if (logger.isDebugEnabled()) print(ast, 0);
		return ast;
	}

	public static CommonTree compileSelectClause(String selectParam)  throws RecognitionException {
		//lexer splits input into tokens
		ANTLRStringStream input = new ANTLRStringStream(selectParam);
		TokenStream tokens = new CommonTokenStream(new WhereClauseLexer(input));
		
		//parser generates abstract syntax tree
		WhereClauseParser parser = new WhereClauseParser(tokens);
		WhereClauseParser.selectClause_return ret = parser.selectClause();
		
		//acquire parse result
		CommonTree ast = (CommonTree) ret.getTree();
		if (logger.isDebugEnabled()) print(ast, 0);
		return ast;
	}
	
	private static void print(CommonTree tree, int level) {
		//indent level
		for (int i = 0; i < level; i++)
			logger.debug("--");

		//print node description: type code followed by token text
		logger.debug(" " + tree.getType() + " " + tree.getText());
		
		//print all children
		if (tree.getChildren() != null)
			for (Object ie : tree.getChildren()) {
				print((CommonTree) ie, level + 1);
			}
	}
	
	/**
	 * Returns a message based on the information in the RecognitionException
	 * @param exception RecognitionException
	 * @return String explaining the error
	 */
	public static String resolveMessage(RecognitionException exception)
	{
		if (exception != null)
		{
			return "Error at char position "+exception.charPositionInLine;
		}
		
		return ""; //No message
	}


}
