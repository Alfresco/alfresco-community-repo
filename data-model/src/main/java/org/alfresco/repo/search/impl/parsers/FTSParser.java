/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
// $ANTLR 3.5.2 W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g 2015-06-18 19:37:48

package org.alfresco.repo.search.impl.parsers;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class FTSParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "AMP", "AND", "AT", "BAR", "BOOST", 
		"CARAT", "COLON", "COMMA", "CONJUNCTION", "DATETIME", "DAY", "DECIMAL_INTEGER_LITERAL", 
		"DECIMAL_NUMERAL", "DEFAULT", "DIGIT", "DISJUNCTION", "DOLLAR", "DOT", 
		"DOTDOT", "E", "EQUALS", "EXACT_PHRASE", "EXACT_TERM", "EXCLAMATION", 
		"EXCLUDE", "EXCLUSIVE", "EXPONENT", "FG_EXACT_PHRASE", "FG_EXACT_TERM", 
		"FG_PHRASE", "FG_PROXIMITY", "FG_RANGE", "FG_SYNONYM", "FG_TERM", "FIELD_CONJUNCTION", 
		"FIELD_DEFAULT", "FIELD_DISJUNCTION", "FIELD_EXCLUDE", "FIELD_GROUP", 
		"FIELD_MANDATORY", "FIELD_NEGATION", "FIELD_OPTIONAL", "FIELD_REF", "FLOATING_POINT_LITERAL", 
		"FS", "FTS", "FTSPHRASE", "FTSPRE", "FTSWILD", "FTSWORD", "FUZZY", "F_ESC", 
		"F_HEX", "F_URI_ALPHA", "F_URI_DIGIT", "F_URI_ESC", "F_URI_OTHER", "GT", 
		"HOUR", "ID", "INCLUSIVE", "IN_WORD", "LCURL", "LPAREN", "LSQUARE", "LT", 
		"MANDATORY", "MILLIS", "MINUS", "MINUTE", "MONTH", "NAME_SPACE", "NEGATION", 
		"NON_ZERO_DIGIT", "NOT", "NOW", "OPTIONAL", "OR", "PERCENT", "PHRASE", 
		"PLUS", "PREFIX", "PROXIMITY", "QUALIFIER", "QUESTION_MARK", "RANGE", 
		"RCURL", "RPAREN", "RSQUARE", "SECOND", "SIGNED_INTEGER", "SPECIFICDATETIME", 
		"STAR", "START_WORD", "SYNONYM", "TEMPLATE", "TERM", "TILDA", "TO", "UNIT", 
		"URI", "WS", "YEAR", "ZERO_DIGIT"
	};
	public static final int EOF=-1;
	public static final int AMP=4;
	public static final int AND=5;
	public static final int AT=6;
	public static final int BAR=7;
	public static final int BOOST=8;
	public static final int CARAT=9;
	public static final int COLON=10;
	public static final int COMMA=11;
	public static final int CONJUNCTION=12;
	public static final int DATETIME=13;
	public static final int DAY=14;
	public static final int DECIMAL_INTEGER_LITERAL=15;
	public static final int DECIMAL_NUMERAL=16;
	public static final int DEFAULT=17;
	public static final int DIGIT=18;
	public static final int DISJUNCTION=19;
	public static final int DOLLAR=20;
	public static final int DOT=21;
	public static final int DOTDOT=22;
	public static final int E=23;
	public static final int EQUALS=24;
	public static final int EXACT_PHRASE=25;
	public static final int EXACT_TERM=26;
	public static final int EXCLAMATION=27;
	public static final int EXCLUDE=28;
	public static final int EXCLUSIVE=29;
	public static final int EXPONENT=30;
	public static final int FG_EXACT_PHRASE=31;
	public static final int FG_EXACT_TERM=32;
	public static final int FG_PHRASE=33;
	public static final int FG_PROXIMITY=34;
	public static final int FG_RANGE=35;
	public static final int FG_SYNONYM=36;
	public static final int FG_TERM=37;
	public static final int FIELD_CONJUNCTION=38;
	public static final int FIELD_DEFAULT=39;
	public static final int FIELD_DISJUNCTION=40;
	public static final int FIELD_EXCLUDE=41;
	public static final int FIELD_GROUP=42;
	public static final int FIELD_MANDATORY=43;
	public static final int FIELD_NEGATION=44;
	public static final int FIELD_OPTIONAL=45;
	public static final int FIELD_REF=46;
	public static final int FLOATING_POINT_LITERAL=47;
	public static final int FS=48;
	public static final int FTS=49;
	public static final int FTSPHRASE=50;
	public static final int FTSPRE=51;
	public static final int FTSWILD=52;
	public static final int FTSWORD=53;
	public static final int FUZZY=54;
	public static final int F_ESC=55;
	public static final int F_HEX=56;
	public static final int F_URI_ALPHA=57;
	public static final int F_URI_DIGIT=58;
	public static final int F_URI_ESC=59;
	public static final int F_URI_OTHER=60;
	public static final int GT=61;
	public static final int HOUR=62;
	public static final int ID=63;
	public static final int INCLUSIVE=64;
	public static final int IN_WORD=65;
	public static final int LCURL=66;
	public static final int LPAREN=67;
	public static final int LSQUARE=68;
	public static final int LT=69;
	public static final int MANDATORY=70;
	public static final int MILLIS=71;
	public static final int MINUS=72;
	public static final int MINUTE=73;
	public static final int MONTH=74;
	public static final int NAME_SPACE=75;
	public static final int NEGATION=76;
	public static final int NON_ZERO_DIGIT=77;
	public static final int NOT=78;
	public static final int NOW=79;
	public static final int OPTIONAL=80;
	public static final int OR=81;
	public static final int PERCENT=82;
	public static final int PHRASE=83;
	public static final int PLUS=84;
	public static final int PREFIX=85;
	public static final int PROXIMITY=86;
	public static final int QUALIFIER=87;
	public static final int QUESTION_MARK=88;
	public static final int RANGE=89;
	public static final int RCURL=90;
	public static final int RPAREN=91;
	public static final int RSQUARE=92;
	public static final int SECOND=93;
	public static final int SIGNED_INTEGER=94;
	public static final int SPECIFICDATETIME=95;
	public static final int STAR=96;
	public static final int START_WORD=97;
	public static final int SYNONYM=98;
	public static final int TEMPLATE=99;
	public static final int TERM=100;
	public static final int TILDA=101;
	public static final int TO=102;
	public static final int UNIT=103;
	public static final int URI=104;
	public static final int WS=105;
	public static final int YEAR=106;
	public static final int ZERO_DIGIT=107;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public FTSParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public FTSParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return FTSParser.tokenNames; }
	@Override public String getGrammarFileName() { return "W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g"; }


	    public enum Mode
	    {
	        CMIS, DEFAULT_CONJUNCTION, DEFAULT_DISJUNCTION
	    }
	    
	    private Stack<String> paraphrases = new Stack<String>();
	    
	    private boolean defaultFieldConjunction = true;
	    
	    private Mode mode = Mode.DEFAULT_CONJUNCTION;
	    
	    public Mode getMode()
	    {
	       return mode;
	    }
	    
	    public void setMode(Mode mode)
	    {
	       this.mode = mode;
	    }
	    
	    public boolean defaultFieldConjunction()
	    {
	       return defaultFieldConjunction;
	    }
	    
	    public void setDefaultFieldConjunction(boolean defaultFieldConjunction)
	    {
	       this.defaultFieldConjunction = defaultFieldConjunction;
	    }
	    
	    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException
	    {
	        throw new MismatchedTokenException(ttype, input);
	    }
	        
	    public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException
	    {
	        throw e;
	    }
	    
	   public String getErrorMessage(RecognitionException e, String[] tokenNames) 
	    {
	        List stack = getRuleInvocationStack(e, this.getClass().getName());
	        String msg = e.getMessage();
	        if ( e instanceof UnwantedTokenException ) 
	            {
	            UnwantedTokenException ute = (UnwantedTokenException)e;
	            String tokenName="<unknown>";
	            if ( ute.expecting== Token.EOF ) 
	            {
	                tokenName = "EOF";
	            }
	            else 
	            {
	                tokenName = tokenNames[ute.expecting];
	            }
	            msg = "extraneous input " + getTokenErrorDisplay(ute.getUnexpectedToken())
	                + " expecting "+tokenName;
	        }
	        else if ( e instanceof MissingTokenException ) 
	        {
	            MissingTokenException mte = (MissingTokenException)e;
	            String tokenName="<unknown>";
	            if ( mte.expecting== Token.EOF ) 
	            {
	                tokenName = "EOF";
	            }
	            else 
	            {
	                tokenName = tokenNames[mte.expecting];
	            }
	            msg = "missing " + tokenName+" at " + getTokenErrorDisplay(e.token)
	                + "  (" + getLongTokenErrorDisplay(e.token) +")";
	        }
	        else if ( e instanceof MismatchedTokenException ) 
	        {
	            MismatchedTokenException mte = (MismatchedTokenException)e;
	            String tokenName="<unknown>";
	            if ( mte.expecting== Token.EOF ) 
	            {
	                tokenName = "EOF";
	            }
	            else
	            {
	                tokenName = tokenNames[mte.expecting];
	            }
	            msg = "mismatched input " + getTokenErrorDisplay(e.token)
	                + " expecting " + tokenName +"  (" + getLongTokenErrorDisplay(e.token) + ")";
	        }
	        else if ( e instanceof MismatchedTreeNodeException ) 
	        {
	            MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
	            String tokenName="<unknown>";
	            if ( mtne.expecting==Token.EOF )  
	            {
	                tokenName = "EOF";
	            }
	            else 
	            {
	                tokenName = tokenNames[mtne.expecting];
	            }
	            msg = "mismatched tree node: " + mtne.node + " expecting " + tokenName;
	        }
	        else if ( e instanceof NoViableAltException ) 
	        {
	            NoViableAltException nvae = (NoViableAltException)e;
	            msg = "no viable alternative at input " + getTokenErrorDisplay(e.token)
	                + "\n\t (decision=" + nvae.decisionNumber
	                + " state " + nvae.stateNumber + ")" 
	                + " decision=<<" + nvae.grammarDecisionDescription + ">>";
	        }
	        else if ( e instanceof EarlyExitException ) 
	        {
	            //EarlyExitException eee = (EarlyExitException)e;
	            // for development, can add "(decision="+eee.decisionNumber+")"
	            msg = "required (...)+ loop did not match anything at input " + getTokenErrorDisplay(e.token);
	        }
	            else if ( e instanceof MismatchedSetException ) 
	            {
	                MismatchedSetException mse = (MismatchedSetException)e;
	                msg = "mismatched input " + getTokenErrorDisplay(e.token)
	                + " expecting set " + mse.expecting;
	        }
	        else if ( e instanceof MismatchedNotSetException ) 
	        {
	            MismatchedNotSetException mse = (MismatchedNotSetException)e;
	            msg = "mismatched input " + getTokenErrorDisplay(e.token)
	                + " expecting set " + mse.expecting;
	        }
	        else if ( e instanceof FailedPredicateException ) 
	        {
	            FailedPredicateException fpe = (FailedPredicateException)e;
	            msg = "rule " + fpe.ruleName + " failed predicate: {" + fpe.predicateText + "}?";
	        }
	                
	        if(paraphrases.size() > 0)
	        {
	            String paraphrase = (String)paraphrases.peek();
	            msg = msg+" "+paraphrase;
	        }
	        return msg +"\n\t"+stack;
	    }
	        
	    public String getLongTokenErrorDisplay(Token t)
	    {
	        return t.toString();
	    }
	    

	    public String getErrorString(RecognitionException e)
	    {
	        String hdr = getErrorHeader(e);
	        String msg = getErrorMessage(e, this.getTokenNames());
	        return hdr+" "+msg;
	    } 


	public static class ftsQuery_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsQuery"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:342:1: ftsQuery : ftsDisjunction EOF -> ftsDisjunction ;
	public final FTSParser.ftsQuery_return ftsQuery() throws RecognitionException {
		FTSParser.ftsQuery_return retval = new FTSParser.ftsQuery_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token EOF2=null;
		ParserRuleReturnScope ftsDisjunction1 =null;

		Object EOF2_tree=null;
		RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
		RewriteRuleSubtreeStream stream_ftsDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsDisjunction");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:343:9: ( ftsDisjunction EOF -> ftsDisjunction )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:344:9: ftsDisjunction EOF
			{
			pushFollow(FOLLOW_ftsDisjunction_in_ftsQuery577);
			ftsDisjunction1=ftsDisjunction();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsDisjunction.add(ftsDisjunction1.getTree());
			EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_ftsQuery579); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_EOF.add(EOF2);

			// AST REWRITE
			// elements: ftsDisjunction
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 345:17: -> ftsDisjunction
			{
				adaptor.addChild(root_0, stream_ftsDisjunction.nextTree());
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsQuery"


	public static class ftsDisjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsDisjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:353:1: ftsDisjunction : ({...}? cmisExplicitDisjunction |{...}? ftsExplicitDisjunction |{...}? ftsImplicitDisjunction );
	public final FTSParser.ftsDisjunction_return ftsDisjunction() throws RecognitionException {
		FTSParser.ftsDisjunction_return retval = new FTSParser.ftsDisjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope cmisExplicitDisjunction3 =null;
		ParserRuleReturnScope ftsExplicitDisjunction4 =null;
		ParserRuleReturnScope ftsImplicitDisjunction5 =null;


		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:354:9: ({...}? cmisExplicitDisjunction |{...}? ftsExplicitDisjunction |{...}? ftsImplicitDisjunction )
			int alt1=3;
			switch ( input.LA(1) ) {
			case COMMA:
			case DOT:
				{
				int LA1_1 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ID:
				{
				int LA1_2 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FTSWORD:
				{
				int LA1_3 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FTSPRE:
				{
				int LA1_4 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FTSWILD:
				{
				int LA1_5 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NOT:
				{
				int LA1_6 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case TO:
				{
				int LA1_7 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 7, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case DECIMAL_INTEGER_LITERAL:
				{
				int LA1_8 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 8, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FLOATING_POINT_LITERAL:
				{
				int LA1_9 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 9, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case STAR:
				{
				int LA1_10 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 10, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case QUESTION_MARK:
				{
				int LA1_11 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 11, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case DATETIME:
				{
				int LA1_12 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 12, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case URI:
				{
				int LA1_13 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 13, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FTSPHRASE:
				{
				int LA1_14 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 14, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case MINUS:
				{
				int LA1_15 = input.LA(2);
				if ( ((getMode() == Mode.CMIS)) ) {
					alt1=1;
				}
				else if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 15, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AND:
				{
				int LA1_16 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 16, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AMP:
				{
				alt1=2;
				}
				break;
			case EXCLAMATION:
				{
				int LA1_18 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 18, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AT:
				{
				int LA1_19 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 19, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case OR:
				{
				int LA1_20 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 20, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LSQUARE:
				{
				int LA1_21 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 21, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LT:
				{
				int LA1_22 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 22, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case EQUALS:
				{
				int LA1_23 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 23, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case TILDA:
				{
				int LA1_24 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 24, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LPAREN:
				{
				int LA1_25 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 25, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PERCENT:
				{
				int LA1_26 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 26, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PLUS:
				{
				int LA1_27 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 27, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case BAR:
				{
				int LA1_28 = input.LA(2);
				if ( ((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
					alt1=2;
				}
				else if ( ((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
					alt1=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 1, 28, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}
			switch (alt1) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:355:11: {...}? cmisExplicitDisjunction
					{
					root_0 = (Object)adaptor.nil();


					if ( !((getMode() == Mode.CMIS)) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "ftsDisjunction", "getMode() == Mode.CMIS");
					}
					pushFollow(FOLLOW_cmisExplicitDisjunction_in_ftsDisjunction639);
					cmisExplicitDisjunction3=cmisExplicitDisjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, cmisExplicitDisjunction3.getTree());

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:356:11: {...}? ftsExplicitDisjunction
					{
					root_0 = (Object)adaptor.nil();


					if ( !((getMode() == Mode.DEFAULT_CONJUNCTION)) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "ftsDisjunction", "getMode() == Mode.DEFAULT_CONJUNCTION");
					}
					pushFollow(FOLLOW_ftsExplicitDisjunction_in_ftsDisjunction653);
					ftsExplicitDisjunction4=ftsExplicitDisjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsExplicitDisjunction4.getTree());

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:357:11: {...}? ftsImplicitDisjunction
					{
					root_0 = (Object)adaptor.nil();


					if ( !((getMode() == Mode.DEFAULT_DISJUNCTION)) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "ftsDisjunction", "getMode() == Mode.DEFAULT_DISJUNCTION");
					}
					pushFollow(FOLLOW_ftsImplicitDisjunction_in_ftsDisjunction667);
					ftsImplicitDisjunction5=ftsImplicitDisjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsImplicitDisjunction5.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsDisjunction"


	public static class ftsExplicitDisjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsExplicitDisjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:360:1: ftsExplicitDisjunction : ftsImplicitConjunction ( or ftsImplicitConjunction )* -> ^( DISJUNCTION ( ftsImplicitConjunction )+ ) ;
	public final FTSParser.ftsExplicitDisjunction_return ftsExplicitDisjunction() throws RecognitionException {
		FTSParser.ftsExplicitDisjunction_return retval = new FTSParser.ftsExplicitDisjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope ftsImplicitConjunction6 =null;
		ParserRuleReturnScope or7 =null;
		ParserRuleReturnScope ftsImplicitConjunction8 =null;

		RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
		RewriteRuleSubtreeStream stream_ftsImplicitConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsImplicitConjunction");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:361:9: ( ftsImplicitConjunction ( or ftsImplicitConjunction )* -> ^( DISJUNCTION ( ftsImplicitConjunction )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:362:9: ftsImplicitConjunction ( or ftsImplicitConjunction )*
			{
			pushFollow(FOLLOW_ftsImplicitConjunction_in_ftsExplicitDisjunction700);
			ftsImplicitConjunction6=ftsImplicitConjunction();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsImplicitConjunction.add(ftsImplicitConjunction6.getTree());
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:362:32: ( or ftsImplicitConjunction )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==BAR||LA2_0==OR) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:362:33: or ftsImplicitConjunction
					{
					pushFollow(FOLLOW_or_in_ftsExplicitDisjunction703);
					or7=or();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_or.add(or7.getTree());
					pushFollow(FOLLOW_ftsImplicitConjunction_in_ftsExplicitDisjunction705);
					ftsImplicitConjunction8=ftsImplicitConjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsImplicitConjunction.add(ftsImplicitConjunction8.getTree());
					}
					break;

				default :
					break loop2;
				}
			}

			// AST REWRITE
			// elements: ftsImplicitConjunction
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 363:17: -> ^( DISJUNCTION ( ftsImplicitConjunction )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:364:25: ^( DISJUNCTION ( ftsImplicitConjunction )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);
				if ( !(stream_ftsImplicitConjunction.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsImplicitConjunction.hasNext() ) {
					adaptor.addChild(root_1, stream_ftsImplicitConjunction.nextTree());
				}
				stream_ftsImplicitConjunction.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsExplicitDisjunction"


	public static class cmisExplicitDisjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "cmisExplicitDisjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:367:1: cmisExplicitDisjunction : cmisConjunction ( or cmisConjunction )* -> ^( DISJUNCTION ( cmisConjunction )+ ) ;
	public final FTSParser.cmisExplicitDisjunction_return cmisExplicitDisjunction() throws RecognitionException {
		FTSParser.cmisExplicitDisjunction_return retval = new FTSParser.cmisExplicitDisjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope cmisConjunction9 =null;
		ParserRuleReturnScope or10 =null;
		ParserRuleReturnScope cmisConjunction11 =null;

		RewriteRuleSubtreeStream stream_cmisConjunction=new RewriteRuleSubtreeStream(adaptor,"rule cmisConjunction");
		RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:368:9: ( cmisConjunction ( or cmisConjunction )* -> ^( DISJUNCTION ( cmisConjunction )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:369:9: cmisConjunction ( or cmisConjunction )*
			{
			pushFollow(FOLLOW_cmisConjunction_in_cmisExplicitDisjunction789);
			cmisConjunction9=cmisConjunction();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_cmisConjunction.add(cmisConjunction9.getTree());
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:369:25: ( or cmisConjunction )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0==BAR||LA3_0==OR) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:369:26: or cmisConjunction
					{
					pushFollow(FOLLOW_or_in_cmisExplicitDisjunction792);
					or10=or();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_or.add(or10.getTree());
					pushFollow(FOLLOW_cmisConjunction_in_cmisExplicitDisjunction794);
					cmisConjunction11=cmisConjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_cmisConjunction.add(cmisConjunction11.getTree());
					}
					break;

				default :
					break loop3;
				}
			}

			// AST REWRITE
			// elements: cmisConjunction
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 370:17: -> ^( DISJUNCTION ( cmisConjunction )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:371:25: ^( DISJUNCTION ( cmisConjunction )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);
				if ( !(stream_cmisConjunction.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_cmisConjunction.hasNext() ) {
					adaptor.addChild(root_1, stream_cmisConjunction.nextTree());
				}
				stream_cmisConjunction.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cmisExplicitDisjunction"


	public static class ftsImplicitDisjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsImplicitDisjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:374:1: ftsImplicitDisjunction : ( ( or )? ftsExplicitConjunction )+ -> ^( DISJUNCTION ( ftsExplicitConjunction )+ ) ;
	public final FTSParser.ftsImplicitDisjunction_return ftsImplicitDisjunction() throws RecognitionException {
		FTSParser.ftsImplicitDisjunction_return retval = new FTSParser.ftsImplicitDisjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope or12 =null;
		ParserRuleReturnScope ftsExplicitConjunction13 =null;

		RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
		RewriteRuleSubtreeStream stream_ftsExplicitConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsExplicitConjunction");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:375:9: ( ( ( or )? ftsExplicitConjunction )+ -> ^( DISJUNCTION ( ftsExplicitConjunction )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:9: ( ( or )? ftsExplicitConjunction )+
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:9: ( ( or )? ftsExplicitConjunction )+
			int cnt5=0;
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( ((LA5_0 >= AND && LA5_0 <= BAR)||LA5_0==COMMA||LA5_0==DATETIME||LA5_0==DECIMAL_INTEGER_LITERAL||LA5_0==DOT||LA5_0==EQUALS||LA5_0==EXCLAMATION||LA5_0==FLOATING_POINT_LITERAL||(LA5_0 >= FTSPHRASE && LA5_0 <= FTSWORD)||LA5_0==ID||(LA5_0 >= LPAREN && LA5_0 <= LT)||LA5_0==MINUS||LA5_0==NOT||(LA5_0 >= OR && LA5_0 <= PERCENT)||LA5_0==PLUS||LA5_0==QUESTION_MARK||LA5_0==STAR||(LA5_0 >= TILDA && LA5_0 <= TO)||LA5_0==URI) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:10: ( or )? ftsExplicitConjunction
					{
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:10: ( or )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0==OR) ) {
						int LA4_1 = input.LA(2);
						if ( ((LA4_1 >= AND && LA4_1 <= BAR)||LA4_1==COMMA||LA4_1==DATETIME||LA4_1==DECIMAL_INTEGER_LITERAL||LA4_1==DOT||LA4_1==EQUALS||LA4_1==EXCLAMATION||LA4_1==FLOATING_POINT_LITERAL||(LA4_1 >= FTSPHRASE && LA4_1 <= FTSWORD)||LA4_1==ID||(LA4_1 >= LPAREN && LA4_1 <= LT)||LA4_1==MINUS||LA4_1==NOT||(LA4_1 >= OR && LA4_1 <= PERCENT)||LA4_1==PLUS||LA4_1==QUESTION_MARK||LA4_1==STAR||(LA4_1 >= TILDA && LA4_1 <= TO)||LA4_1==URI) ) {
							alt4=1;
						}
					}
					else if ( (LA4_0==BAR) ) {
						int LA4_2 = input.LA(2);
						if ( (LA4_2==BAR) ) {
							alt4=1;
						}
					}
					switch (alt4) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:376:10: or
							{
							pushFollow(FOLLOW_or_in_ftsImplicitDisjunction879);
							or12=or();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_or.add(or12.getTree());
							}
							break;

					}

					pushFollow(FOLLOW_ftsExplicitConjunction_in_ftsImplicitDisjunction882);
					ftsExplicitConjunction13=ftsExplicitConjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsExplicitConjunction.add(ftsExplicitConjunction13.getTree());
					}
					break;

				default :
					if ( cnt5 >= 1 ) break loop5;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(5, input);
					throw eee;
				}
				cnt5++;
			}

			// AST REWRITE
			// elements: ftsExplicitConjunction
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 377:17: -> ^( DISJUNCTION ( ftsExplicitConjunction )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:378:25: ^( DISJUNCTION ( ftsExplicitConjunction )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DISJUNCTION, "DISJUNCTION"), root_1);
				if ( !(stream_ftsExplicitConjunction.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsExplicitConjunction.hasNext() ) {
					adaptor.addChild(root_1, stream_ftsExplicitConjunction.nextTree());
				}
				stream_ftsExplicitConjunction.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsImplicitDisjunction"


	public static class ftsExplicitConjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsExplicitConjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:385:1: ftsExplicitConjunction : ftsPrefixed ( and ftsPrefixed )* -> ^( CONJUNCTION ( ftsPrefixed )+ ) ;
	public final FTSParser.ftsExplicitConjunction_return ftsExplicitConjunction() throws RecognitionException {
		FTSParser.ftsExplicitConjunction_return retval = new FTSParser.ftsExplicitConjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope ftsPrefixed14 =null;
		ParserRuleReturnScope and15 =null;
		ParserRuleReturnScope ftsPrefixed16 =null;

		RewriteRuleSubtreeStream stream_ftsPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsPrefixed");
		RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:386:9: ( ftsPrefixed ( and ftsPrefixed )* -> ^( CONJUNCTION ( ftsPrefixed )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:387:9: ftsPrefixed ( and ftsPrefixed )*
			{
			pushFollow(FOLLOW_ftsPrefixed_in_ftsExplicitConjunction969);
			ftsPrefixed14=ftsPrefixed();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed14.getTree());
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:387:21: ( and ftsPrefixed )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0==AND) ) {
					int LA6_2 = input.LA(2);
					if ( ((LA6_2 >= AND && LA6_2 <= BAR)||LA6_2==COMMA||LA6_2==DATETIME||LA6_2==DECIMAL_INTEGER_LITERAL||LA6_2==DOT||LA6_2==EQUALS||LA6_2==EXCLAMATION||LA6_2==FLOATING_POINT_LITERAL||(LA6_2 >= FTSPHRASE && LA6_2 <= FTSWORD)||LA6_2==ID||(LA6_2 >= LPAREN && LA6_2 <= LT)||LA6_2==MINUS||LA6_2==NOT||(LA6_2 >= OR && LA6_2 <= PERCENT)||LA6_2==PLUS||LA6_2==QUESTION_MARK||LA6_2==STAR||(LA6_2 >= TILDA && LA6_2 <= TO)||LA6_2==URI) ) {
						alt6=1;
					}

				}
				else if ( (LA6_0==AMP) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:387:22: and ftsPrefixed
					{
					pushFollow(FOLLOW_and_in_ftsExplicitConjunction972);
					and15=and();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_and.add(and15.getTree());
					pushFollow(FOLLOW_ftsPrefixed_in_ftsExplicitConjunction974);
					ftsPrefixed16=ftsPrefixed();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed16.getTree());
					}
					break;

				default :
					break loop6;
				}
			}

			// AST REWRITE
			// elements: ftsPrefixed
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 388:17: -> ^( CONJUNCTION ( ftsPrefixed )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:389:25: ^( CONJUNCTION ( ftsPrefixed )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);
				if ( !(stream_ftsPrefixed.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsPrefixed.hasNext() ) {
					adaptor.addChild(root_1, stream_ftsPrefixed.nextTree());
				}
				stream_ftsPrefixed.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsExplicitConjunction"


	public static class ftsImplicitConjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsImplicitConjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:392:1: ftsImplicitConjunction : ( ( and )? ftsPrefixed )+ -> ^( CONJUNCTION ( ftsPrefixed )+ ) ;
	public final FTSParser.ftsImplicitConjunction_return ftsImplicitConjunction() throws RecognitionException {
		FTSParser.ftsImplicitConjunction_return retval = new FTSParser.ftsImplicitConjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope and17 =null;
		ParserRuleReturnScope ftsPrefixed18 =null;

		RewriteRuleSubtreeStream stream_ftsPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsPrefixed");
		RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:393:9: ( ( ( and )? ftsPrefixed )+ -> ^( CONJUNCTION ( ftsPrefixed )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:9: ( ( and )? ftsPrefixed )+
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:9: ( ( and )? ftsPrefixed )+
			int cnt8=0;
			loop8:
			while (true) {
				int alt8=2;
				switch ( input.LA(1) ) {
				case OR:
					{
					int LA8_1 = input.LA(2);
					if ( (LA8_1==COLON) ) {
						alt8=1;
					}

					}
					break;
				case BAR:
					{
					int LA8_2 = input.LA(2);
					if ( ((LA8_2 >= AND && LA8_2 <= AT)||LA8_2==COMMA||LA8_2==DATETIME||LA8_2==DECIMAL_INTEGER_LITERAL||LA8_2==DOT||LA8_2==EQUALS||LA8_2==FLOATING_POINT_LITERAL||(LA8_2 >= FTSPHRASE && LA8_2 <= FTSWORD)||LA8_2==ID||(LA8_2 >= LPAREN && LA8_2 <= LT)||LA8_2==NOT||(LA8_2 >= OR && LA8_2 <= PERCENT)||LA8_2==QUESTION_MARK||LA8_2==STAR||(LA8_2 >= TILDA && LA8_2 <= TO)||LA8_2==URI) ) {
						alt8=1;
					}

					}
					break;
				case AMP:
				case AND:
				case AT:
				case COMMA:
				case DATETIME:
				case DECIMAL_INTEGER_LITERAL:
				case DOT:
				case EQUALS:
				case EXCLAMATION:
				case FLOATING_POINT_LITERAL:
				case FTSPHRASE:
				case FTSPRE:
				case FTSWILD:
				case FTSWORD:
				case ID:
				case LPAREN:
				case LSQUARE:
				case LT:
				case MINUS:
				case NOT:
				case PERCENT:
				case PLUS:
				case QUESTION_MARK:
				case STAR:
				case TILDA:
				case TO:
				case URI:
					{
					alt8=1;
					}
					break;
				}
				switch (alt8) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:10: ( and )? ftsPrefixed
					{
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:10: ( and )?
					int alt7=2;
					int LA7_0 = input.LA(1);
					if ( (LA7_0==AND) ) {
						int LA7_1 = input.LA(2);
						if ( ((LA7_1 >= AND && LA7_1 <= BAR)||LA7_1==COMMA||LA7_1==DATETIME||LA7_1==DECIMAL_INTEGER_LITERAL||LA7_1==DOT||LA7_1==EQUALS||LA7_1==EXCLAMATION||LA7_1==FLOATING_POINT_LITERAL||(LA7_1 >= FTSPHRASE && LA7_1 <= FTSWORD)||LA7_1==ID||(LA7_1 >= LPAREN && LA7_1 <= LT)||LA7_1==MINUS||LA7_1==NOT||(LA7_1 >= OR && LA7_1 <= PERCENT)||LA7_1==PLUS||LA7_1==QUESTION_MARK||LA7_1==STAR||(LA7_1 >= TILDA && LA7_1 <= TO)||LA7_1==URI) ) {
							alt7=1;
						}
					}
					else if ( (LA7_0==AMP) ) {
						alt7=1;
					}
					switch (alt7) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:394:10: and
							{
							pushFollow(FOLLOW_and_in_ftsImplicitConjunction1059);
							and17=and();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_and.add(and17.getTree());
							}
							break;

					}

					pushFollow(FOLLOW_ftsPrefixed_in_ftsImplicitConjunction1062);
					ftsPrefixed18=ftsPrefixed();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsPrefixed.add(ftsPrefixed18.getTree());
					}
					break;

				default :
					if ( cnt8 >= 1 ) break loop8;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(8, input);
					throw eee;
				}
				cnt8++;
			}

			// AST REWRITE
			// elements: ftsPrefixed
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 395:17: -> ^( CONJUNCTION ( ftsPrefixed )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:396:25: ^( CONJUNCTION ( ftsPrefixed )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);
				if ( !(stream_ftsPrefixed.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsPrefixed.hasNext() ) {
					adaptor.addChild(root_1, stream_ftsPrefixed.nextTree());
				}
				stream_ftsPrefixed.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsImplicitConjunction"


	public static class cmisConjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "cmisConjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:399:1: cmisConjunction : ( cmisPrefixed )+ -> ^( CONJUNCTION ( cmisPrefixed )+ ) ;
	public final FTSParser.cmisConjunction_return cmisConjunction() throws RecognitionException {
		FTSParser.cmisConjunction_return retval = new FTSParser.cmisConjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope cmisPrefixed19 =null;

		RewriteRuleSubtreeStream stream_cmisPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule cmisPrefixed");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:400:9: ( ( cmisPrefixed )+ -> ^( CONJUNCTION ( cmisPrefixed )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:401:9: ( cmisPrefixed )+
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:401:9: ( cmisPrefixed )+
			int cnt9=0;
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==COMMA||LA9_0==DATETIME||LA9_0==DECIMAL_INTEGER_LITERAL||LA9_0==DOT||LA9_0==FLOATING_POINT_LITERAL||(LA9_0 >= FTSPHRASE && LA9_0 <= FTSWORD)||LA9_0==ID||LA9_0==MINUS||LA9_0==NOT||LA9_0==QUESTION_MARK||LA9_0==STAR||LA9_0==TO||LA9_0==URI) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:401:9: cmisPrefixed
					{
					pushFollow(FOLLOW_cmisPrefixed_in_cmisConjunction1146);
					cmisPrefixed19=cmisPrefixed();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_cmisPrefixed.add(cmisPrefixed19.getTree());
					}
					break;

				default :
					if ( cnt9 >= 1 ) break loop9;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(9, input);
					throw eee;
				}
				cnt9++;
			}

			// AST REWRITE
			// elements: cmisPrefixed
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 402:17: -> ^( CONJUNCTION ( cmisPrefixed )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:403:25: ^( CONJUNCTION ( cmisPrefixed )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONJUNCTION, "CONJUNCTION"), root_1);
				if ( !(stream_cmisPrefixed.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_cmisPrefixed.hasNext() ) {
					adaptor.addChild(root_1, stream_cmisPrefixed.nextTree());
				}
				stream_cmisPrefixed.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cmisConjunction"


	public static class ftsPrefixed_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsPrefixed"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:413:1: ftsPrefixed : ( ( not )=> not ftsTest ( boost )? -> ^( NEGATION ftsTest ( boost )? ) | ftsTest ( boost )? -> ^( DEFAULT ftsTest ( boost )? ) | PLUS ftsTest ( boost )? -> ^( MANDATORY ftsTest ( boost )? ) | BAR ftsTest ( boost )? -> ^( OPTIONAL ftsTest ( boost )? ) | MINUS ftsTest ( boost )? -> ^( EXCLUDE ftsTest ( boost )? ) );
	public final FTSParser.ftsPrefixed_return ftsPrefixed() throws RecognitionException {
		FTSParser.ftsPrefixed_return retval = new FTSParser.ftsPrefixed_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token PLUS25=null;
		Token BAR28=null;
		Token MINUS31=null;
		ParserRuleReturnScope not20 =null;
		ParserRuleReturnScope ftsTest21 =null;
		ParserRuleReturnScope boost22 =null;
		ParserRuleReturnScope ftsTest23 =null;
		ParserRuleReturnScope boost24 =null;
		ParserRuleReturnScope ftsTest26 =null;
		ParserRuleReturnScope boost27 =null;
		ParserRuleReturnScope ftsTest29 =null;
		ParserRuleReturnScope boost30 =null;
		ParserRuleReturnScope ftsTest32 =null;
		ParserRuleReturnScope boost33 =null;

		Object PLUS25_tree=null;
		Object BAR28_tree=null;
		Object MINUS31_tree=null;
		RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
		RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
		RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
		RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
		RewriteRuleSubtreeStream stream_ftsTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsTest");
		RewriteRuleSubtreeStream stream_boost=new RewriteRuleSubtreeStream(adaptor,"rule boost");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:414:9: ( ( not )=> not ftsTest ( boost )? -> ^( NEGATION ftsTest ( boost )? ) | ftsTest ( boost )? -> ^( DEFAULT ftsTest ( boost )? ) | PLUS ftsTest ( boost )? -> ^( MANDATORY ftsTest ( boost )? ) | BAR ftsTest ( boost )? -> ^( OPTIONAL ftsTest ( boost )? ) | MINUS ftsTest ( boost )? -> ^( EXCLUDE ftsTest ( boost )? ) )
			int alt15=5;
			int LA15_0 = input.LA(1);
			if ( (LA15_0==NOT) ) {
				int LA15_1 = input.LA(2);
				if ( (synpred1_FTS()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

			}
			else if ( ((LA15_0 >= AND && LA15_0 <= AT)||LA15_0==COMMA||LA15_0==DATETIME||LA15_0==DECIMAL_INTEGER_LITERAL||LA15_0==DOT||LA15_0==EQUALS||LA15_0==FLOATING_POINT_LITERAL||(LA15_0 >= FTSPHRASE && LA15_0 <= FTSWORD)||LA15_0==ID||(LA15_0 >= LPAREN && LA15_0 <= LT)||(LA15_0 >= OR && LA15_0 <= PERCENT)||LA15_0==QUESTION_MARK||LA15_0==STAR||(LA15_0 >= TILDA && LA15_0 <= TO)||LA15_0==URI) ) {
				alt15=2;
			}
			else if ( (LA15_0==EXCLAMATION) && (synpred1_FTS())) {
				alt15=1;
			}
			else if ( (LA15_0==PLUS) ) {
				alt15=3;
			}
			else if ( (LA15_0==BAR) ) {
				alt15=4;
			}
			else if ( (LA15_0==MINUS) ) {
				alt15=5;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}

			switch (alt15) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:415:9: ( not )=> not ftsTest ( boost )?
					{
					pushFollow(FOLLOW_not_in_ftsPrefixed1238);
					not20=not();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_not.add(not20.getTree());
					pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1240);
					ftsTest21=ftsTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest21.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:415:30: ( boost )?
					int alt10=2;
					int LA10_0 = input.LA(1);
					if ( (LA10_0==CARAT) ) {
						alt10=1;
					}
					switch (alt10) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:415:30: boost
							{
							pushFollow(FOLLOW_boost_in_ftsPrefixed1242);
							boost22=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost22.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: boost, ftsTest
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 416:17: -> ^( NEGATION ftsTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:417:25: ^( NEGATION ftsTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NEGATION, "NEGATION"), root_1);
						adaptor.addChild(root_1, stream_ftsTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:417:44: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:418:11: ftsTest ( boost )?
					{
					pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1306);
					ftsTest23=ftsTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest23.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:418:19: ( boost )?
					int alt11=2;
					int LA11_0 = input.LA(1);
					if ( (LA11_0==CARAT) ) {
						alt11=1;
					}
					switch (alt11) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:418:19: boost
							{
							pushFollow(FOLLOW_boost_in_ftsPrefixed1308);
							boost24=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost24.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: boost, ftsTest
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 419:17: -> ^( DEFAULT ftsTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:420:25: ^( DEFAULT ftsTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DEFAULT, "DEFAULT"), root_1);
						adaptor.addChild(root_1, stream_ftsTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:420:43: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:421:11: PLUS ftsTest ( boost )?
					{
					PLUS25=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsPrefixed1372); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_PLUS.add(PLUS25);

					pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1374);
					ftsTest26=ftsTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest26.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:421:24: ( boost )?
					int alt12=2;
					int LA12_0 = input.LA(1);
					if ( (LA12_0==CARAT) ) {
						alt12=1;
					}
					switch (alt12) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:421:24: boost
							{
							pushFollow(FOLLOW_boost_in_ftsPrefixed1376);
							boost27=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost27.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsTest, boost
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 422:17: -> ^( MANDATORY ftsTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:423:25: ^( MANDATORY ftsTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MANDATORY, "MANDATORY"), root_1);
						adaptor.addChild(root_1, stream_ftsTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:423:45: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:424:11: BAR ftsTest ( boost )?
					{
					BAR28=(Token)match(input,BAR,FOLLOW_BAR_in_ftsPrefixed1440); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_BAR.add(BAR28);

					pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1442);
					ftsTest29=ftsTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest29.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:424:23: ( boost )?
					int alt13=2;
					int LA13_0 = input.LA(1);
					if ( (LA13_0==CARAT) ) {
						alt13=1;
					}
					switch (alt13) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:424:23: boost
							{
							pushFollow(FOLLOW_boost_in_ftsPrefixed1444);
							boost30=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost30.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: boost, ftsTest
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 425:17: -> ^( OPTIONAL ftsTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:426:25: ^( OPTIONAL ftsTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(OPTIONAL, "OPTIONAL"), root_1);
						adaptor.addChild(root_1, stream_ftsTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:426:44: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:427:11: MINUS ftsTest ( boost )?
					{
					MINUS31=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsPrefixed1508); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_MINUS.add(MINUS31);

					pushFollow(FOLLOW_ftsTest_in_ftsPrefixed1510);
					ftsTest32=ftsTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsTest.add(ftsTest32.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:427:25: ( boost )?
					int alt14=2;
					int LA14_0 = input.LA(1);
					if ( (LA14_0==CARAT) ) {
						alt14=1;
					}
					switch (alt14) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:427:25: boost
							{
							pushFollow(FOLLOW_boost_in_ftsPrefixed1512);
							boost33=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost33.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsTest, boost
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 428:17: -> ^( EXCLUDE ftsTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:429:25: ^( EXCLUDE ftsTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXCLUDE, "EXCLUDE"), root_1);
						adaptor.addChild(root_1, stream_ftsTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:429:43: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsPrefixed"


	public static class cmisPrefixed_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "cmisPrefixed"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:432:1: cmisPrefixed : ( cmisTest -> ^( DEFAULT cmisTest ) | MINUS cmisTest -> ^( EXCLUDE cmisTest ) );
	public final FTSParser.cmisPrefixed_return cmisPrefixed() throws RecognitionException {
		FTSParser.cmisPrefixed_return retval = new FTSParser.cmisPrefixed_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token MINUS35=null;
		ParserRuleReturnScope cmisTest34 =null;
		ParserRuleReturnScope cmisTest36 =null;

		Object MINUS35_tree=null;
		RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
		RewriteRuleSubtreeStream stream_cmisTest=new RewriteRuleSubtreeStream(adaptor,"rule cmisTest");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:433:9: ( cmisTest -> ^( DEFAULT cmisTest ) | MINUS cmisTest -> ^( EXCLUDE cmisTest ) )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==COMMA||LA16_0==DATETIME||LA16_0==DECIMAL_INTEGER_LITERAL||LA16_0==DOT||LA16_0==FLOATING_POINT_LITERAL||(LA16_0 >= FTSPHRASE && LA16_0 <= FTSWORD)||LA16_0==ID||LA16_0==NOT||LA16_0==QUESTION_MARK||LA16_0==STAR||LA16_0==TO||LA16_0==URI) ) {
				alt16=1;
			}
			else if ( (LA16_0==MINUS) ) {
				alt16=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:434:9: cmisTest
					{
					pushFollow(FOLLOW_cmisTest_in_cmisPrefixed1597);
					cmisTest34=cmisTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_cmisTest.add(cmisTest34.getTree());
					// AST REWRITE
					// elements: cmisTest
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 435:17: -> ^( DEFAULT cmisTest )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:436:25: ^( DEFAULT cmisTest )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DEFAULT, "DEFAULT"), root_1);
						adaptor.addChild(root_1, stream_cmisTest.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:437:11: MINUS cmisTest
					{
					MINUS35=(Token)match(input,MINUS,FOLLOW_MINUS_in_cmisPrefixed1657); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_MINUS.add(MINUS35);

					pushFollow(FOLLOW_cmisTest_in_cmisPrefixed1659);
					cmisTest36=cmisTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_cmisTest.add(cmisTest36.getTree());
					// AST REWRITE
					// elements: cmisTest
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 438:17: -> ^( EXCLUDE cmisTest )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:439:25: ^( EXCLUDE cmisTest )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXCLUDE, "EXCLUDE"), root_1);
						adaptor.addChild(root_1, stream_cmisTest.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cmisPrefixed"


	public static class ftsTest_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsTest"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:445:1: ftsTest : ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ( ftsRange )=> ftsRange -> ^( RANGE ftsRange ) | ( ftsFieldGroup )=> ftsFieldGroup -> ftsFieldGroup | ( ftsTermOrPhrase )=> ftsTermOrPhrase | ( ftsExactTermOrPhrase )=> ftsExactTermOrPhrase | ( ftsTokenisedTermOrPhrase )=> ftsTokenisedTermOrPhrase | LPAREN ftsDisjunction RPAREN -> ftsDisjunction | template -> template );
	public final FTSParser.ftsTest_return ftsTest() throws RecognitionException {
		FTSParser.ftsTest_return retval = new FTSParser.ftsTest_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token LPAREN43=null;
		Token RPAREN45=null;
		ParserRuleReturnScope ftsFieldGroupProximity37 =null;
		ParserRuleReturnScope ftsRange38 =null;
		ParserRuleReturnScope ftsFieldGroup39 =null;
		ParserRuleReturnScope ftsTermOrPhrase40 =null;
		ParserRuleReturnScope ftsExactTermOrPhrase41 =null;
		ParserRuleReturnScope ftsTokenisedTermOrPhrase42 =null;
		ParserRuleReturnScope ftsDisjunction44 =null;
		ParserRuleReturnScope template46 =null;

		Object LPAREN43_tree=null;
		Object RPAREN45_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_template=new RewriteRuleSubtreeStream(adaptor,"rule template");
		RewriteRuleSubtreeStream stream_ftsFieldGroup=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroup");
		RewriteRuleSubtreeStream stream_ftsDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsDisjunction");
		RewriteRuleSubtreeStream stream_ftsRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsRange");
		RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:446:9: ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ( ftsRange )=> ftsRange -> ^( RANGE ftsRange ) | ( ftsFieldGroup )=> ftsFieldGroup -> ftsFieldGroup | ( ftsTermOrPhrase )=> ftsTermOrPhrase | ( ftsExactTermOrPhrase )=> ftsExactTermOrPhrase | ( ftsTokenisedTermOrPhrase )=> ftsTokenisedTermOrPhrase | LPAREN ftsDisjunction RPAREN -> ftsDisjunction | template -> template )
			int alt17=8;
			alt17 = dfa17.predict(input);
			switch (alt17) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:447:12: ( ftsFieldGroupProximity )=> ftsFieldGroupProximity
					{
					pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsTest1751);
					ftsFieldGroupProximity37=ftsFieldGroupProximity();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity37.getTree());
					// AST REWRITE
					// elements: ftsFieldGroupProximity
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 448:17: -> ^( PROXIMITY ftsFieldGroupProximity )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:449:25: ^( PROXIMITY ftsFieldGroupProximity )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PROXIMITY, "PROXIMITY"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupProximity.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:451:12: ( ftsRange )=> ftsRange
					{
					pushFollow(FOLLOW_ftsRange_in_ftsTest1828);
					ftsRange38=ftsRange();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsRange.add(ftsRange38.getTree());
					// AST REWRITE
					// elements: ftsRange
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 452:17: -> ^( RANGE ftsRange )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:453:25: ^( RANGE ftsRange )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(RANGE, "RANGE"), root_1);
						adaptor.addChild(root_1, stream_ftsRange.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:455:12: ( ftsFieldGroup )=> ftsFieldGroup
					{
					pushFollow(FOLLOW_ftsFieldGroup_in_ftsTest1907);
					ftsFieldGroup39=ftsFieldGroup();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroup.add(ftsFieldGroup39.getTree());
					// AST REWRITE
					// elements: ftsFieldGroup
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 456:17: -> ftsFieldGroup
					{
						adaptor.addChild(root_0, stream_ftsFieldGroup.nextTree());
					}


					retval.tree = root_0;
					}

					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:458:12: ( ftsTermOrPhrase )=> ftsTermOrPhrase
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsTermOrPhrase_in_ftsTest1956);
					ftsTermOrPhrase40=ftsTermOrPhrase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsTermOrPhrase40.getTree());

					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:460:12: ( ftsExactTermOrPhrase )=> ftsExactTermOrPhrase
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsExactTermOrPhrase_in_ftsTest1985);
					ftsExactTermOrPhrase41=ftsExactTermOrPhrase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsExactTermOrPhrase41.getTree());

					}
					break;
				case 6 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:462:12: ( ftsTokenisedTermOrPhrase )=> ftsTokenisedTermOrPhrase
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsTokenisedTermOrPhrase_in_ftsTest2015);
					ftsTokenisedTermOrPhrase42=ftsTokenisedTermOrPhrase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsTokenisedTermOrPhrase42.getTree());

					}
					break;
				case 7 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:465:12: LPAREN ftsDisjunction RPAREN
					{
					LPAREN43=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsTest2046); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN43);

					pushFollow(FOLLOW_ftsDisjunction_in_ftsTest2048);
					ftsDisjunction44=ftsDisjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsDisjunction.add(ftsDisjunction44.getTree());
					RPAREN45=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsTest2050); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN45);

					// AST REWRITE
					// elements: ftsDisjunction
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 466:17: -> ftsDisjunction
					{
						adaptor.addChild(root_0, stream_ftsDisjunction.nextTree());
					}


					retval.tree = root_0;
					}

					}
					break;
				case 8 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:467:12: template
					{
					pushFollow(FOLLOW_template_in_ftsTest2083);
					template46=template();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_template.add(template46.getTree());
					// AST REWRITE
					// elements: template
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 468:17: -> template
					{
						adaptor.addChild(root_0, stream_template.nextTree());
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsTest"


	public static class cmisTest_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "cmisTest"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:471:1: cmisTest : ( cmisTerm -> ^( TERM cmisTerm ) | cmisPhrase -> ^( PHRASE cmisPhrase ) );
	public final FTSParser.cmisTest_return cmisTest() throws RecognitionException {
		FTSParser.cmisTest_return retval = new FTSParser.cmisTest_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope cmisTerm47 =null;
		ParserRuleReturnScope cmisPhrase48 =null;

		RewriteRuleSubtreeStream stream_cmisPhrase=new RewriteRuleSubtreeStream(adaptor,"rule cmisPhrase");
		RewriteRuleSubtreeStream stream_cmisTerm=new RewriteRuleSubtreeStream(adaptor,"rule cmisTerm");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:472:9: ( cmisTerm -> ^( TERM cmisTerm ) | cmisPhrase -> ^( PHRASE cmisPhrase ) )
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0==COMMA||LA18_0==DATETIME||LA18_0==DECIMAL_INTEGER_LITERAL||LA18_0==DOT||LA18_0==FLOATING_POINT_LITERAL||(LA18_0 >= FTSPRE && LA18_0 <= FTSWORD)||LA18_0==ID||LA18_0==NOT||LA18_0==QUESTION_MARK||LA18_0==STAR||LA18_0==TO||LA18_0==URI) ) {
				alt18=1;
			}
			else if ( (LA18_0==FTSPHRASE) ) {
				alt18=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}

			switch (alt18) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:473:9: cmisTerm
					{
					pushFollow(FOLLOW_cmisTerm_in_cmisTest2136);
					cmisTerm47=cmisTerm();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_cmisTerm.add(cmisTerm47.getTree());
					// AST REWRITE
					// elements: cmisTerm
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 474:17: -> ^( TERM cmisTerm )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:475:25: ^( TERM cmisTerm )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);
						adaptor.addChild(root_1, stream_cmisTerm.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:476:11: cmisPhrase
					{
					pushFollow(FOLLOW_cmisPhrase_in_cmisTest2196);
					cmisPhrase48=cmisPhrase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_cmisPhrase.add(cmisPhrase48.getTree());
					// AST REWRITE
					// elements: cmisPhrase
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 477:17: -> ^( PHRASE cmisPhrase )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:478:25: ^( PHRASE cmisPhrase )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);
						adaptor.addChild(root_1, stream_cmisPhrase.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cmisTest"


	public static class template_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "template"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:481:1: template : ( PERCENT tempReference -> ^( TEMPLATE tempReference ) | PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN -> ^( TEMPLATE ( tempReference )+ ) );
	public final FTSParser.template_return template() throws RecognitionException {
		FTSParser.template_return retval = new FTSParser.template_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token PERCENT49=null;
		Token PERCENT51=null;
		Token LPAREN52=null;
		Token COMMA54=null;
		Token RPAREN55=null;
		ParserRuleReturnScope tempReference50 =null;
		ParserRuleReturnScope tempReference53 =null;

		Object PERCENT49_tree=null;
		Object PERCENT51_tree=null;
		Object LPAREN52_tree=null;
		Object COMMA54_tree=null;
		Object RPAREN55_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_tempReference=new RewriteRuleSubtreeStream(adaptor,"rule tempReference");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:482:9: ( PERCENT tempReference -> ^( TEMPLATE tempReference ) | PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN -> ^( TEMPLATE ( tempReference )+ ) )
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0==PERCENT) ) {
				int LA21_1 = input.LA(2);
				if ( (LA21_1==LPAREN) ) {
					alt21=2;
				}
				else if ( ((LA21_1 >= AND && LA21_1 <= AT)||LA21_1==ID||LA21_1==NOT||LA21_1==OR||LA21_1==TO||LA21_1==URI) ) {
					alt21=1;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}

			switch (alt21) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:483:9: PERCENT tempReference
					{
					PERCENT49=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_template2277); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT49);

					pushFollow(FOLLOW_tempReference_in_template2279);
					tempReference50=tempReference();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_tempReference.add(tempReference50.getTree());
					// AST REWRITE
					// elements: tempReference
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 484:17: -> ^( TEMPLATE tempReference )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:485:25: ^( TEMPLATE tempReference )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TEMPLATE, "TEMPLATE"), root_1);
						adaptor.addChild(root_1, stream_tempReference.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:486:11: PERCENT LPAREN ( tempReference ( COMMA )? )+ RPAREN
					{
					PERCENT51=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_template2339); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT51);

					LPAREN52=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_template2341); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN52);

					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:486:26: ( tempReference ( COMMA )? )+
					int cnt20=0;
					loop20:
					while (true) {
						int alt20=2;
						int LA20_0 = input.LA(1);
						if ( ((LA20_0 >= AND && LA20_0 <= AT)||LA20_0==ID||LA20_0==NOT||LA20_0==OR||LA20_0==TO||LA20_0==URI) ) {
							alt20=1;
						}

						switch (alt20) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:486:27: tempReference ( COMMA )?
							{
							pushFollow(FOLLOW_tempReference_in_template2344);
							tempReference53=tempReference();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_tempReference.add(tempReference53.getTree());
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:486:41: ( COMMA )?
							int alt19=2;
							int LA19_0 = input.LA(1);
							if ( (LA19_0==COMMA) ) {
								alt19=1;
							}
							switch (alt19) {
								case 1 :
									// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:486:41: COMMA
									{
									COMMA54=(Token)match(input,COMMA,FOLLOW_COMMA_in_template2346); if (state.failed) return retval; 
									if ( state.backtracking==0 ) stream_COMMA.add(COMMA54);

									}
									break;

							}

							}
							break;

						default :
							if ( cnt20 >= 1 ) break loop20;
							if (state.backtracking>0) {state.failed=true; return retval;}
							EarlyExitException eee = new EarlyExitException(20, input);
							throw eee;
						}
						cnt20++;
					}

					RPAREN55=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_template2351); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN55);

					// AST REWRITE
					// elements: tempReference
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 487:17: -> ^( TEMPLATE ( tempReference )+ )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:488:25: ^( TEMPLATE ( tempReference )+ )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TEMPLATE, "TEMPLATE"), root_1);
						if ( !(stream_tempReference.hasNext()) ) {
							throw new RewriteEarlyExitException();
						}
						while ( stream_tempReference.hasNext() ) {
							adaptor.addChild(root_1, stream_tempReference.nextTree());
						}
						stream_tempReference.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "template"


	public static class fuzzy_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "fuzzy"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:491:1: fuzzy : TILDA number -> ^( FUZZY number ) ;
	public final FTSParser.fuzzy_return fuzzy() throws RecognitionException {
		FTSParser.fuzzy_return retval = new FTSParser.fuzzy_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token TILDA56=null;
		ParserRuleReturnScope number57 =null;

		Object TILDA56_tree=null;
		RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
		RewriteRuleSubtreeStream stream_number=new RewriteRuleSubtreeStream(adaptor,"rule number");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:492:9: ( TILDA number -> ^( FUZZY number ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:493:9: TILDA number
			{
			TILDA56=(Token)match(input,TILDA,FOLLOW_TILDA_in_fuzzy2433); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TILDA.add(TILDA56);

			pushFollow(FOLLOW_number_in_fuzzy2435);
			number57=number();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_number.add(number57.getTree());
			// AST REWRITE
			// elements: number
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 494:17: -> ^( FUZZY number )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:495:25: ^( FUZZY number )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUZZY, "FUZZY"), root_1);
				adaptor.addChild(root_1, stream_number.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "fuzzy"


	public static class slop_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "slop"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:498:1: slop : TILDA DECIMAL_INTEGER_LITERAL -> ^( FUZZY DECIMAL_INTEGER_LITERAL ) ;
	public final FTSParser.slop_return slop() throws RecognitionException {
		FTSParser.slop_return retval = new FTSParser.slop_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token TILDA58=null;
		Token DECIMAL_INTEGER_LITERAL59=null;

		Object TILDA58_tree=null;
		Object DECIMAL_INTEGER_LITERAL59_tree=null;
		RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
		RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:499:9: ( TILDA DECIMAL_INTEGER_LITERAL -> ^( FUZZY DECIMAL_INTEGER_LITERAL ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:500:9: TILDA DECIMAL_INTEGER_LITERAL
			{
			TILDA58=(Token)match(input,TILDA,FOLLOW_TILDA_in_slop2516); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TILDA.add(TILDA58);

			DECIMAL_INTEGER_LITERAL59=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_slop2518); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL59);

			// AST REWRITE
			// elements: DECIMAL_INTEGER_LITERAL
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 501:17: -> ^( FUZZY DECIMAL_INTEGER_LITERAL )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:502:25: ^( FUZZY DECIMAL_INTEGER_LITERAL )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FUZZY, "FUZZY"), root_1);
				adaptor.addChild(root_1, stream_DECIMAL_INTEGER_LITERAL.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "slop"


	public static class boost_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "boost"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:505:1: boost : CARAT number -> ^( BOOST number ) ;
	public final FTSParser.boost_return boost() throws RecognitionException {
		FTSParser.boost_return retval = new FTSParser.boost_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token CARAT60=null;
		ParserRuleReturnScope number61 =null;

		Object CARAT60_tree=null;
		RewriteRuleTokenStream stream_CARAT=new RewriteRuleTokenStream(adaptor,"token CARAT");
		RewriteRuleSubtreeStream stream_number=new RewriteRuleSubtreeStream(adaptor,"rule number");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:506:9: ( CARAT number -> ^( BOOST number ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:507:9: CARAT number
			{
			CARAT60=(Token)match(input,CARAT,FOLLOW_CARAT_in_boost2599); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_CARAT.add(CARAT60);

			pushFollow(FOLLOW_number_in_boost2601);
			number61=number();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_number.add(number61.getTree());
			// AST REWRITE
			// elements: number
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 508:17: -> ^( BOOST number )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:509:25: ^( BOOST number )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BOOST, "BOOST"), root_1);
				adaptor.addChild(root_1, stream_number.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "boost"


	public static class ftsTermOrPhrase_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsTermOrPhrase"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:512:1: ftsTermOrPhrase : ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord ( fuzzy )? ) );
	public final FTSParser.ftsTermOrPhrase_return ftsTermOrPhrase() throws RecognitionException {
		FTSParser.ftsTermOrPhrase_return retval = new FTSParser.ftsTermOrPhrase_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token COLON63=null;
		Token FTSPHRASE64=null;
		Token FTSPHRASE68=null;
		ParserRuleReturnScope fieldReference62 =null;
		ParserRuleReturnScope slop65 =null;
		ParserRuleReturnScope ftsWord66 =null;
		ParserRuleReturnScope fuzzy67 =null;
		ParserRuleReturnScope slop69 =null;
		ParserRuleReturnScope ftsWord70 =null;
		ParserRuleReturnScope fuzzy71 =null;

		Object COLON63_tree=null;
		Object FTSPHRASE64_tree=null;
		Object FTSPHRASE68_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
		RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
		RewriteRuleSubtreeStream stream_slop=new RewriteRuleSubtreeStream(adaptor,"rule slop");
		RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
		RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:513:9: ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord ( fuzzy )? ) )
			int alt27=3;
			int LA27_0 = input.LA(1);
			if ( (LA27_0==AT) && (synpred8_FTS())) {
				alt27=1;
			}
			else if ( (LA27_0==ID) ) {
				int LA27_2 = input.LA(2);
				if ( (LA27_2==DOT) ) {
					int LA27_10 = input.LA(3);
					if ( (LA27_10==ID) ) {
						int LA27_17 = input.LA(4);
						if ( (synpred8_FTS()) ) {
							alt27=1;
						}
						else if ( (true) ) {
							alt27=3;
						}

					}
					else if ( (LA27_10==EOF||(LA27_10 >= AMP && LA27_10 <= BAR)||LA27_10==CARAT||LA27_10==COMMA||LA27_10==DATETIME||LA27_10==DECIMAL_INTEGER_LITERAL||LA27_10==DOT||LA27_10==EQUALS||LA27_10==EXCLAMATION||LA27_10==FLOATING_POINT_LITERAL||(LA27_10 >= FTSPHRASE && LA27_10 <= FTSWORD)||(LA27_10 >= LPAREN && LA27_10 <= LT)||LA27_10==MINUS||LA27_10==NOT||(LA27_10 >= OR && LA27_10 <= PERCENT)||LA27_10==PLUS||LA27_10==QUESTION_MARK||LA27_10==RPAREN||LA27_10==STAR||(LA27_10 >= TILDA && LA27_10 <= TO)||LA27_10==URI) ) {
						alt27=3;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 27, 10, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA27_2==COLON) && (synpred8_FTS())) {
					alt27=1;
				}
				else if ( (LA27_2==EOF||(LA27_2 >= AMP && LA27_2 <= BAR)||LA27_2==CARAT||LA27_2==COMMA||LA27_2==DATETIME||LA27_2==DECIMAL_INTEGER_LITERAL||LA27_2==EQUALS||LA27_2==EXCLAMATION||LA27_2==FLOATING_POINT_LITERAL||(LA27_2 >= FTSPHRASE && LA27_2 <= FTSWORD)||LA27_2==ID||(LA27_2 >= LPAREN && LA27_2 <= LT)||LA27_2==MINUS||LA27_2==NOT||(LA27_2 >= OR && LA27_2 <= PERCENT)||LA27_2==PLUS||LA27_2==QUESTION_MARK||LA27_2==RPAREN||LA27_2==STAR||(LA27_2 >= TILDA && LA27_2 <= TO)||LA27_2==URI) ) {
					alt27=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 27, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA27_0==TO) ) {
				int LA27_3 = input.LA(2);
				if ( (LA27_3==COLON) && (synpred8_FTS())) {
					alt27=1;
				}
				else if ( (LA27_3==EOF||(LA27_3 >= AMP && LA27_3 <= BAR)||LA27_3==CARAT||LA27_3==COMMA||LA27_3==DATETIME||LA27_3==DECIMAL_INTEGER_LITERAL||LA27_3==DOT||LA27_3==EQUALS||LA27_3==EXCLAMATION||LA27_3==FLOATING_POINT_LITERAL||(LA27_3 >= FTSPHRASE && LA27_3 <= FTSWORD)||LA27_3==ID||(LA27_3 >= LPAREN && LA27_3 <= LT)||LA27_3==MINUS||LA27_3==NOT||(LA27_3 >= OR && LA27_3 <= PERCENT)||LA27_3==PLUS||LA27_3==QUESTION_MARK||LA27_3==RPAREN||LA27_3==STAR||(LA27_3 >= TILDA && LA27_3 <= TO)||LA27_3==URI) ) {
					alt27=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 27, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA27_0==OR) && (synpred8_FTS())) {
				alt27=1;
			}
			else if ( (LA27_0==AND) && (synpred8_FTS())) {
				alt27=1;
			}
			else if ( (LA27_0==NOT) ) {
				int LA27_6 = input.LA(2);
				if ( (LA27_6==COLON) && (synpred8_FTS())) {
					alt27=1;
				}
				else if ( (LA27_6==EOF||(LA27_6 >= AMP && LA27_6 <= BAR)||LA27_6==CARAT||LA27_6==COMMA||LA27_6==DATETIME||LA27_6==DECIMAL_INTEGER_LITERAL||LA27_6==DOT||LA27_6==EQUALS||LA27_6==EXCLAMATION||LA27_6==FLOATING_POINT_LITERAL||(LA27_6 >= FTSPHRASE && LA27_6 <= FTSWORD)||LA27_6==ID||(LA27_6 >= LPAREN && LA27_6 <= LT)||LA27_6==MINUS||LA27_6==NOT||(LA27_6 >= OR && LA27_6 <= PERCENT)||LA27_6==PLUS||LA27_6==QUESTION_MARK||LA27_6==RPAREN||LA27_6==STAR||(LA27_6 >= TILDA && LA27_6 <= TO)||LA27_6==URI) ) {
					alt27=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 27, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA27_0==URI) ) {
				switch ( input.LA(2) ) {
				case ID:
					{
					int LA27_12 = input.LA(3);
					if ( (LA27_12==DOT) ) {
						int LA27_18 = input.LA(4);
						if ( (LA27_18==ID) ) {
							int LA27_20 = input.LA(5);
							if ( (synpred8_FTS()) ) {
								alt27=1;
							}
							else if ( (true) ) {
								alt27=3;
							}

						}
						else if ( (LA27_18==EOF||(LA27_18 >= AMP && LA27_18 <= BAR)||LA27_18==CARAT||LA27_18==COMMA||LA27_18==DATETIME||LA27_18==DECIMAL_INTEGER_LITERAL||LA27_18==DOT||LA27_18==EQUALS||LA27_18==EXCLAMATION||LA27_18==FLOATING_POINT_LITERAL||(LA27_18 >= FTSPHRASE && LA27_18 <= FTSWORD)||(LA27_18 >= LPAREN && LA27_18 <= LT)||LA27_18==MINUS||LA27_18==NOT||(LA27_18 >= OR && LA27_18 <= PERCENT)||LA27_18==PLUS||LA27_18==QUESTION_MARK||LA27_18==RPAREN||LA27_18==STAR||(LA27_18 >= TILDA && LA27_18 <= TO)||LA27_18==URI) ) {
							alt27=3;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return retval;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 27, 18, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA27_12==COLON) && (synpred8_FTS())) {
						alt27=1;
					}
					else if ( (LA27_12==EOF||(LA27_12 >= AMP && LA27_12 <= BAR)||LA27_12==CARAT||LA27_12==COMMA||LA27_12==DATETIME||LA27_12==DECIMAL_INTEGER_LITERAL||LA27_12==EQUALS||LA27_12==EXCLAMATION||LA27_12==FLOATING_POINT_LITERAL||(LA27_12 >= FTSPHRASE && LA27_12 <= FTSWORD)||LA27_12==ID||(LA27_12 >= LPAREN && LA27_12 <= LT)||LA27_12==MINUS||LA27_12==NOT||(LA27_12 >= OR && LA27_12 <= PERCENT)||LA27_12==PLUS||LA27_12==QUESTION_MARK||LA27_12==RPAREN||LA27_12==STAR||(LA27_12 >= TILDA && LA27_12 <= TO)||LA27_12==URI) ) {
						alt27=3;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 27, 12, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case TO:
					{
					int LA27_13 = input.LA(3);
					if ( (LA27_13==COLON) && (synpred8_FTS())) {
						alt27=1;
					}
					else if ( (LA27_13==EOF||(LA27_13 >= AMP && LA27_13 <= BAR)||LA27_13==CARAT||LA27_13==COMMA||LA27_13==DATETIME||LA27_13==DECIMAL_INTEGER_LITERAL||LA27_13==DOT||LA27_13==EQUALS||LA27_13==EXCLAMATION||LA27_13==FLOATING_POINT_LITERAL||(LA27_13 >= FTSPHRASE && LA27_13 <= FTSWORD)||LA27_13==ID||(LA27_13 >= LPAREN && LA27_13 <= LT)||LA27_13==MINUS||LA27_13==NOT||(LA27_13 >= OR && LA27_13 <= PERCENT)||LA27_13==PLUS||LA27_13==QUESTION_MARK||LA27_13==RPAREN||LA27_13==STAR||(LA27_13 >= TILDA && LA27_13 <= TO)||LA27_13==URI) ) {
						alt27=3;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 27, 13, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case OR:
					{
					int LA27_14 = input.LA(3);
					if ( (LA27_14==COLON) && (synpred8_FTS())) {
						alt27=1;
					}
					else if ( (LA27_14==EOF||(LA27_14 >= AMP && LA27_14 <= BAR)||LA27_14==CARAT||LA27_14==COMMA||LA27_14==DATETIME||LA27_14==DECIMAL_INTEGER_LITERAL||LA27_14==DOT||LA27_14==EQUALS||LA27_14==EXCLAMATION||LA27_14==FLOATING_POINT_LITERAL||(LA27_14 >= FTSPHRASE && LA27_14 <= FTSWORD)||LA27_14==ID||(LA27_14 >= LPAREN && LA27_14 <= LT)||LA27_14==MINUS||LA27_14==NOT||(LA27_14 >= OR && LA27_14 <= PERCENT)||LA27_14==PLUS||LA27_14==QUESTION_MARK||LA27_14==RPAREN||LA27_14==STAR||(LA27_14 >= TILDA && LA27_14 <= TO)||LA27_14==URI) ) {
						alt27=3;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 27, 14, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case AND:
					{
					int LA27_15 = input.LA(3);
					if ( (LA27_15==COLON) && (synpred8_FTS())) {
						alt27=1;
					}
					else if ( (LA27_15==EOF||(LA27_15 >= AMP && LA27_15 <= BAR)||LA27_15==CARAT||LA27_15==COMMA||LA27_15==DATETIME||LA27_15==DECIMAL_INTEGER_LITERAL||LA27_15==DOT||LA27_15==EQUALS||LA27_15==EXCLAMATION||LA27_15==FLOATING_POINT_LITERAL||(LA27_15 >= FTSPHRASE && LA27_15 <= FTSWORD)||LA27_15==ID||(LA27_15 >= LPAREN && LA27_15 <= LT)||LA27_15==MINUS||LA27_15==NOT||(LA27_15 >= OR && LA27_15 <= PERCENT)||LA27_15==PLUS||LA27_15==QUESTION_MARK||LA27_15==RPAREN||LA27_15==STAR||(LA27_15 >= TILDA && LA27_15 <= TO)||LA27_15==URI) ) {
						alt27=3;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 27, 15, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case NOT:
					{
					int LA27_16 = input.LA(3);
					if ( (LA27_16==COLON) && (synpred8_FTS())) {
						alt27=1;
					}
					else if ( (LA27_16==EOF||(LA27_16 >= AMP && LA27_16 <= BAR)||LA27_16==CARAT||LA27_16==COMMA||LA27_16==DATETIME||LA27_16==DECIMAL_INTEGER_LITERAL||LA27_16==DOT||LA27_16==EQUALS||LA27_16==EXCLAMATION||LA27_16==FLOATING_POINT_LITERAL||(LA27_16 >= FTSPHRASE && LA27_16 <= FTSWORD)||LA27_16==ID||(LA27_16 >= LPAREN && LA27_16 <= LT)||LA27_16==MINUS||LA27_16==NOT||(LA27_16 >= OR && LA27_16 <= PERCENT)||LA27_16==PLUS||LA27_16==QUESTION_MARK||LA27_16==RPAREN||LA27_16==STAR||(LA27_16 >= TILDA && LA27_16 <= TO)||LA27_16==URI) ) {
						alt27=3;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 27, 16, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 27, 7, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
			}
			else if ( (LA27_0==FTSPHRASE) ) {
				alt27=2;
			}
			else if ( (LA27_0==COMMA||LA27_0==DATETIME||LA27_0==DECIMAL_INTEGER_LITERAL||LA27_0==DOT||LA27_0==FLOATING_POINT_LITERAL||(LA27_0 >= FTSPRE && LA27_0 <= FTSWORD)||LA27_0==QUESTION_MARK||LA27_0==STAR) ) {
				alt27=3;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 27, 0, input);
				throw nvae;
			}

			switch (alt27) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:514:9: ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) )
					{
					pushFollow(FOLLOW_fieldReference_in_ftsTermOrPhrase2690);
					fieldReference62=fieldReference();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference62.getTree());
					COLON63=(Token)match(input,COLON,FOLLOW_COLON_in_ftsTermOrPhrase2692); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COLON.add(COLON63);

					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:515:9: ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) )
					int alt24=2;
					int LA24_0 = input.LA(1);
					if ( (LA24_0==FTSPHRASE) ) {
						alt24=1;
					}
					else if ( (LA24_0==COMMA||LA24_0==DATETIME||LA24_0==DECIMAL_INTEGER_LITERAL||LA24_0==DOT||LA24_0==FLOATING_POINT_LITERAL||(LA24_0 >= FTSPRE && LA24_0 <= FTSWORD)||LA24_0==ID||LA24_0==NOT||LA24_0==QUESTION_MARK||LA24_0==STAR||LA24_0==TO||LA24_0==URI) ) {
						alt24=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 24, 0, input);
						throw nvae;
					}

					switch (alt24) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:516:17: FTSPHRASE ( ( slop )=> slop )?
							{
							FTSPHRASE64=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsTermOrPhrase2720); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE64);

							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:516:27: ( ( slop )=> slop )?
							int alt22=2;
							int LA22_0 = input.LA(1);
							if ( (LA22_0==TILDA) ) {
								int LA22_1 = input.LA(2);
								if ( (LA22_1==DECIMAL_INTEGER_LITERAL) ) {
									int LA22_3 = input.LA(3);
									if ( (synpred9_FTS()) ) {
										alt22=1;
									}
								}
							}
							switch (alt22) {
								case 1 :
									// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:516:28: ( slop )=> slop
									{
									pushFollow(FOLLOW_slop_in_ftsTermOrPhrase2728);
									slop65=slop();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_slop.add(slop65.getTree());
									}
									break;

							}

							// AST REWRITE
							// elements: FTSPHRASE, slop, fieldReference
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (Object)adaptor.nil();
							// 517:17: -> ^( PHRASE FTSPHRASE fieldReference ( slop )? )
							{
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:517:20: ^( PHRASE FTSPHRASE fieldReference ( slop )? )
								{
								Object root_1 = (Object)adaptor.nil();
								root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);
								adaptor.addChild(root_1, stream_FTSPHRASE.nextNode());
								adaptor.addChild(root_1, stream_fieldReference.nextTree());
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:517:54: ( slop )?
								if ( stream_slop.hasNext() ) {
									adaptor.addChild(root_1, stream_slop.nextTree());
								}
								stream_slop.reset();

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;
						case 2 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:17: ftsWord ( ( fuzzy )=> fuzzy )?
							{
							pushFollow(FOLLOW_ftsWord_in_ftsTermOrPhrase2795);
							ftsWord66=ftsWord();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord66.getTree());
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:25: ( ( fuzzy )=> fuzzy )?
							int alt23=2;
							int LA23_0 = input.LA(1);
							if ( (LA23_0==TILDA) ) {
								int LA23_1 = input.LA(2);
								if ( (LA23_1==DECIMAL_INTEGER_LITERAL) ) {
									int LA23_3 = input.LA(3);
									if ( (synpred10_FTS()) ) {
										alt23=1;
									}
								}
								else if ( (LA23_1==FLOATING_POINT_LITERAL) ) {
									int LA23_4 = input.LA(3);
									if ( (synpred10_FTS()) ) {
										alt23=1;
									}
								}
							}
							switch (alt23) {
								case 1 :
									// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:26: ( fuzzy )=> fuzzy
									{
									pushFollow(FOLLOW_fuzzy_in_ftsTermOrPhrase2804);
									fuzzy67=fuzzy();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy67.getTree());
									}
									break;

							}

							// AST REWRITE
							// elements: fuzzy, ftsWord, fieldReference
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (Object)adaptor.nil();
							// 520:17: -> ^( TERM ftsWord fieldReference ( fuzzy )? )
							{
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:520:20: ^( TERM ftsWord fieldReference ( fuzzy )? )
								{
								Object root_1 = (Object)adaptor.nil();
								root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);
								adaptor.addChild(root_1, stream_ftsWord.nextTree());
								adaptor.addChild(root_1, stream_fieldReference.nextTree());
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:520:50: ( fuzzy )?
								if ( stream_fuzzy.hasNext() ) {
									adaptor.addChild(root_1, stream_fuzzy.nextTree());
								}
								stream_fuzzy.reset();

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;

					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:9: FTSPHRASE ( ( slop )=> slop )?
					{
					FTSPHRASE68=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsTermOrPhrase2865); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE68);

					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:19: ( ( slop )=> slop )?
					int alt25=2;
					int LA25_0 = input.LA(1);
					if ( (LA25_0==TILDA) ) {
						int LA25_1 = input.LA(2);
						if ( (LA25_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA25_3 = input.LA(3);
							if ( (synpred11_FTS()) ) {
								alt25=1;
							}
						}
					}
					switch (alt25) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:20: ( slop )=> slop
							{
							pushFollow(FOLLOW_slop_in_ftsTermOrPhrase2873);
							slop69=slop();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_slop.add(slop69.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: slop, FTSPHRASE
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 524:17: -> ^( PHRASE FTSPHRASE ( slop )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:524:20: ^( PHRASE FTSPHRASE ( slop )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);
						adaptor.addChild(root_1, stream_FTSPHRASE.nextNode());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:524:39: ( slop )?
						if ( stream_slop.hasNext() ) {
							adaptor.addChild(root_1, stream_slop.nextTree());
						}
						stream_slop.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:526:9: ftsWord ( ( fuzzy )=> fuzzy )?
					{
					pushFollow(FOLLOW_ftsWord_in_ftsTermOrPhrase2923);
					ftsWord70=ftsWord();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord70.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:526:17: ( ( fuzzy )=> fuzzy )?
					int alt26=2;
					int LA26_0 = input.LA(1);
					if ( (LA26_0==TILDA) ) {
						int LA26_1 = input.LA(2);
						if ( (LA26_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA26_3 = input.LA(3);
							if ( (synpred12_FTS()) ) {
								alt26=1;
							}
						}
						else if ( (LA26_1==FLOATING_POINT_LITERAL) ) {
							int LA26_4 = input.LA(3);
							if ( (synpred12_FTS()) ) {
								alt26=1;
							}
						}
					}
					switch (alt26) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:526:18: ( fuzzy )=> fuzzy
							{
							pushFollow(FOLLOW_fuzzy_in_ftsTermOrPhrase2932);
							fuzzy71=fuzzy();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy71.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsWord, fuzzy
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 527:17: -> ^( TERM ftsWord ( fuzzy )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:527:20: ^( TERM ftsWord ( fuzzy )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);
						adaptor.addChild(root_1, stream_ftsWord.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:527:35: ( fuzzy )?
						if ( stream_fuzzy.hasNext() ) {
							adaptor.addChild(root_1, stream_fuzzy.nextTree());
						}
						stream_fuzzy.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsTermOrPhrase"


	public static class ftsExactTermOrPhrase_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsExactTermOrPhrase"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:531:1: ftsExactTermOrPhrase : EQUALS ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord ( fuzzy )? ) ) ;
	public final FTSParser.ftsExactTermOrPhrase_return ftsExactTermOrPhrase() throws RecognitionException {
		FTSParser.ftsExactTermOrPhrase_return retval = new FTSParser.ftsExactTermOrPhrase_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token EQUALS72=null;
		Token COLON74=null;
		Token FTSPHRASE75=null;
		Token FTSPHRASE79=null;
		ParserRuleReturnScope fieldReference73 =null;
		ParserRuleReturnScope slop76 =null;
		ParserRuleReturnScope ftsWord77 =null;
		ParserRuleReturnScope fuzzy78 =null;
		ParserRuleReturnScope slop80 =null;
		ParserRuleReturnScope ftsWord81 =null;
		ParserRuleReturnScope fuzzy82 =null;

		Object EQUALS72_tree=null;
		Object COLON74_tree=null;
		Object FTSPHRASE75_tree=null;
		Object FTSPHRASE79_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
		RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
		RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
		RewriteRuleSubtreeStream stream_slop=new RewriteRuleSubtreeStream(adaptor,"rule slop");
		RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
		RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:532:9: ( EQUALS ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord ( fuzzy )? ) ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:533:9: EQUALS ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord ( fuzzy )? ) )
			{
			EQUALS72=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsExactTermOrPhrase3011); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS72);

			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:534:9: ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord ( fuzzy )? ) )
			int alt33=3;
			alt33 = dfa33.predict(input);
			switch (alt33) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:535:9: ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? ) )
					{
					pushFollow(FOLLOW_fieldReference_in_ftsExactTermOrPhrase3039);
					fieldReference73=fieldReference();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference73.getTree());
					COLON74=(Token)match(input,COLON,FOLLOW_COLON_in_ftsExactTermOrPhrase3041); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COLON.add(COLON74);

					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:536:9: ( FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? ) )
					int alt30=2;
					int LA30_0 = input.LA(1);
					if ( (LA30_0==FTSPHRASE) ) {
						alt30=1;
					}
					else if ( (LA30_0==COMMA||LA30_0==DATETIME||LA30_0==DECIMAL_INTEGER_LITERAL||LA30_0==DOT||LA30_0==FLOATING_POINT_LITERAL||(LA30_0 >= FTSPRE && LA30_0 <= FTSWORD)||LA30_0==ID||LA30_0==NOT||LA30_0==QUESTION_MARK||LA30_0==STAR||LA30_0==TO||LA30_0==URI) ) {
						alt30=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 30, 0, input);
						throw nvae;
					}

					switch (alt30) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:537:17: FTSPHRASE ( ( slop )=> slop )?
							{
							FTSPHRASE75=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsExactTermOrPhrase3069); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE75);

							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:537:27: ( ( slop )=> slop )?
							int alt28=2;
							int LA28_0 = input.LA(1);
							if ( (LA28_0==TILDA) ) {
								int LA28_1 = input.LA(2);
								if ( (LA28_1==DECIMAL_INTEGER_LITERAL) ) {
									int LA28_3 = input.LA(3);
									if ( (synpred14_FTS()) ) {
										alt28=1;
									}
								}
							}
							switch (alt28) {
								case 1 :
									// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:537:28: ( slop )=> slop
									{
									pushFollow(FOLLOW_slop_in_ftsExactTermOrPhrase3077);
									slop76=slop();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_slop.add(slop76.getTree());
									}
									break;

							}

							// AST REWRITE
							// elements: slop, fieldReference, FTSPHRASE
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (Object)adaptor.nil();
							// 538:17: -> ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? )
							{
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:538:20: ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? )
								{
								Object root_1 = (Object)adaptor.nil();
								root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_PHRASE, "EXACT_PHRASE"), root_1);
								adaptor.addChild(root_1, stream_FTSPHRASE.nextNode());
								adaptor.addChild(root_1, stream_fieldReference.nextTree());
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:538:60: ( slop )?
								if ( stream_slop.hasNext() ) {
									adaptor.addChild(root_1, stream_slop.nextTree());
								}
								stream_slop.reset();

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;
						case 2 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:540:17: ftsWord ( ( fuzzy )=> fuzzy )?
							{
							pushFollow(FOLLOW_ftsWord_in_ftsExactTermOrPhrase3144);
							ftsWord77=ftsWord();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord77.getTree());
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:540:25: ( ( fuzzy )=> fuzzy )?
							int alt29=2;
							int LA29_0 = input.LA(1);
							if ( (LA29_0==TILDA) ) {
								int LA29_1 = input.LA(2);
								if ( (LA29_1==DECIMAL_INTEGER_LITERAL) ) {
									int LA29_3 = input.LA(3);
									if ( (synpred15_FTS()) ) {
										alt29=1;
									}
								}
								else if ( (LA29_1==FLOATING_POINT_LITERAL) ) {
									int LA29_4 = input.LA(3);
									if ( (synpred15_FTS()) ) {
										alt29=1;
									}
								}
							}
							switch (alt29) {
								case 1 :
									// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:540:26: ( fuzzy )=> fuzzy
									{
									pushFollow(FOLLOW_fuzzy_in_ftsExactTermOrPhrase3153);
									fuzzy78=fuzzy();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy78.getTree());
									}
									break;

							}

							// AST REWRITE
							// elements: fieldReference, ftsWord, fuzzy
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (Object)adaptor.nil();
							// 541:17: -> ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? )
							{
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:541:20: ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? )
								{
								Object root_1 = (Object)adaptor.nil();
								root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_TERM, "EXACT_TERM"), root_1);
								adaptor.addChild(root_1, stream_ftsWord.nextTree());
								adaptor.addChild(root_1, stream_fieldReference.nextTree());
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:541:56: ( fuzzy )?
								if ( stream_fuzzy.hasNext() ) {
									adaptor.addChild(root_1, stream_fuzzy.nextTree());
								}
								stream_fuzzy.reset();

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;

					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:544:9: FTSPHRASE ( ( slop )=> slop )?
					{
					FTSPHRASE79=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsExactTermOrPhrase3214); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE79);

					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:544:19: ( ( slop )=> slop )?
					int alt31=2;
					int LA31_0 = input.LA(1);
					if ( (LA31_0==TILDA) ) {
						int LA31_1 = input.LA(2);
						if ( (LA31_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA31_3 = input.LA(3);
							if ( (synpred16_FTS()) ) {
								alt31=1;
							}
						}
					}
					switch (alt31) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:544:20: ( slop )=> slop
							{
							pushFollow(FOLLOW_slop_in_ftsExactTermOrPhrase3222);
							slop80=slop();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_slop.add(slop80.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: FTSPHRASE, slop
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 545:17: -> ^( EXACT_PHRASE FTSPHRASE ( slop )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:545:20: ^( EXACT_PHRASE FTSPHRASE ( slop )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_PHRASE, "EXACT_PHRASE"), root_1);
						adaptor.addChild(root_1, stream_FTSPHRASE.nextNode());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:545:45: ( slop )?
						if ( stream_slop.hasNext() ) {
							adaptor.addChild(root_1, stream_slop.nextTree());
						}
						stream_slop.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:547:9: ftsWord ( ( fuzzy )=> fuzzy )?
					{
					pushFollow(FOLLOW_ftsWord_in_ftsExactTermOrPhrase3272);
					ftsWord81=ftsWord();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord81.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:547:17: ( ( fuzzy )=> fuzzy )?
					int alt32=2;
					int LA32_0 = input.LA(1);
					if ( (LA32_0==TILDA) ) {
						int LA32_1 = input.LA(2);
						if ( (LA32_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA32_3 = input.LA(3);
							if ( (synpred17_FTS()) ) {
								alt32=1;
							}
						}
						else if ( (LA32_1==FLOATING_POINT_LITERAL) ) {
							int LA32_4 = input.LA(3);
							if ( (synpred17_FTS()) ) {
								alt32=1;
							}
						}
					}
					switch (alt32) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:547:18: ( fuzzy )=> fuzzy
							{
							pushFollow(FOLLOW_fuzzy_in_ftsExactTermOrPhrase3281);
							fuzzy82=fuzzy();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy82.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsWord, fuzzy
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 548:17: -> ^( EXACT_TERM ftsWord ( fuzzy )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:548:20: ^( EXACT_TERM ftsWord ( fuzzy )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACT_TERM, "EXACT_TERM"), root_1);
						adaptor.addChild(root_1, stream_ftsWord.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:548:41: ( fuzzy )?
						if ( stream_fuzzy.hasNext() ) {
							adaptor.addChild(root_1, stream_fuzzy.nextTree());
						}
						stream_fuzzy.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsExactTermOrPhrase"


	public static class ftsTokenisedTermOrPhrase_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsTokenisedTermOrPhrase"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:553:1: ftsTokenisedTermOrPhrase : TILDA ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord ( fuzzy )? ) ) ;
	public final FTSParser.ftsTokenisedTermOrPhrase_return ftsTokenisedTermOrPhrase() throws RecognitionException {
		FTSParser.ftsTokenisedTermOrPhrase_return retval = new FTSParser.ftsTokenisedTermOrPhrase_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token TILDA83=null;
		Token COLON85=null;
		Token FTSPHRASE86=null;
		Token FTSPHRASE90=null;
		ParserRuleReturnScope fieldReference84 =null;
		ParserRuleReturnScope slop87 =null;
		ParserRuleReturnScope ftsWord88 =null;
		ParserRuleReturnScope fuzzy89 =null;
		ParserRuleReturnScope slop91 =null;
		ParserRuleReturnScope ftsWord92 =null;
		ParserRuleReturnScope fuzzy93 =null;

		Object TILDA83_tree=null;
		Object COLON85_tree=null;
		Object FTSPHRASE86_tree=null;
		Object FTSPHRASE90_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
		RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");
		RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
		RewriteRuleSubtreeStream stream_slop=new RewriteRuleSubtreeStream(adaptor,"rule slop");
		RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");
		RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:554:9: ( TILDA ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord ( fuzzy )? ) ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:555:9: TILDA ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord ( fuzzy )? ) )
			{
			TILDA83=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsTokenisedTermOrPhrase3362); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TILDA.add(TILDA83);

			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:556:9: ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord ( fuzzy )? ) )
			int alt39=3;
			alt39 = dfa39.predict(input);
			switch (alt39) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:557:9: ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) )
					{
					pushFollow(FOLLOW_fieldReference_in_ftsTokenisedTermOrPhrase3390);
					fieldReference84=fieldReference();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference84.getTree());
					COLON85=(Token)match(input,COLON,FOLLOW_COLON_in_ftsTokenisedTermOrPhrase3392); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COLON.add(COLON85);

					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:558:9: ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) )
					int alt36=2;
					int LA36_0 = input.LA(1);
					if ( (LA36_0==FTSPHRASE) ) {
						alt36=1;
					}
					else if ( (LA36_0==COMMA||LA36_0==DATETIME||LA36_0==DECIMAL_INTEGER_LITERAL||LA36_0==DOT||LA36_0==FLOATING_POINT_LITERAL||(LA36_0 >= FTSPRE && LA36_0 <= FTSWORD)||LA36_0==ID||LA36_0==NOT||LA36_0==QUESTION_MARK||LA36_0==STAR||LA36_0==TO||LA36_0==URI) ) {
						alt36=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 36, 0, input);
						throw nvae;
					}

					switch (alt36) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:559:17: FTSPHRASE ( ( slop )=> slop )?
							{
							FTSPHRASE86=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsTokenisedTermOrPhrase3420); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE86);

							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:559:27: ( ( slop )=> slop )?
							int alt34=2;
							int LA34_0 = input.LA(1);
							if ( (LA34_0==TILDA) ) {
								int LA34_1 = input.LA(2);
								if ( (LA34_1==DECIMAL_INTEGER_LITERAL) ) {
									int LA34_3 = input.LA(3);
									if ( (synpred19_FTS()) ) {
										alt34=1;
									}
								}
							}
							switch (alt34) {
								case 1 :
									// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:559:28: ( slop )=> slop
									{
									pushFollow(FOLLOW_slop_in_ftsTokenisedTermOrPhrase3428);
									slop87=slop();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_slop.add(slop87.getTree());
									}
									break;

							}

							// AST REWRITE
							// elements: fieldReference, slop, FTSPHRASE
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (Object)adaptor.nil();
							// 560:17: -> ^( PHRASE FTSPHRASE fieldReference ( slop )? )
							{
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:560:20: ^( PHRASE FTSPHRASE fieldReference ( slop )? )
								{
								Object root_1 = (Object)adaptor.nil();
								root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);
								adaptor.addChild(root_1, stream_FTSPHRASE.nextNode());
								adaptor.addChild(root_1, stream_fieldReference.nextTree());
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:560:54: ( slop )?
								if ( stream_slop.hasNext() ) {
									adaptor.addChild(root_1, stream_slop.nextTree());
								}
								stream_slop.reset();

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;
						case 2 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:562:17: ftsWord ( ( fuzzy )=> fuzzy )?
							{
							pushFollow(FOLLOW_ftsWord_in_ftsTokenisedTermOrPhrase3495);
							ftsWord88=ftsWord();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord88.getTree());
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:562:25: ( ( fuzzy )=> fuzzy )?
							int alt35=2;
							int LA35_0 = input.LA(1);
							if ( (LA35_0==TILDA) ) {
								int LA35_1 = input.LA(2);
								if ( (LA35_1==DECIMAL_INTEGER_LITERAL) ) {
									int LA35_3 = input.LA(3);
									if ( (synpred20_FTS()) ) {
										alt35=1;
									}
								}
								else if ( (LA35_1==FLOATING_POINT_LITERAL) ) {
									int LA35_4 = input.LA(3);
									if ( (synpred20_FTS()) ) {
										alt35=1;
									}
								}
							}
							switch (alt35) {
								case 1 :
									// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:562:26: ( fuzzy )=> fuzzy
									{
									pushFollow(FOLLOW_fuzzy_in_ftsTokenisedTermOrPhrase3504);
									fuzzy89=fuzzy();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy89.getTree());
									}
									break;

							}

							// AST REWRITE
							// elements: ftsWord, fuzzy, fieldReference
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (Object)adaptor.nil();
							// 563:17: -> ^( TERM ftsWord fieldReference ( fuzzy )? )
							{
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:563:20: ^( TERM ftsWord fieldReference ( fuzzy )? )
								{
								Object root_1 = (Object)adaptor.nil();
								root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);
								adaptor.addChild(root_1, stream_ftsWord.nextTree());
								adaptor.addChild(root_1, stream_fieldReference.nextTree());
								// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:563:50: ( fuzzy )?
								if ( stream_fuzzy.hasNext() ) {
									adaptor.addChild(root_1, stream_fuzzy.nextTree());
								}
								stream_fuzzy.reset();

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;

					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:566:9: FTSPHRASE ( ( slop )=> slop )?
					{
					FTSPHRASE90=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsTokenisedTermOrPhrase3565); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE90);

					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:566:19: ( ( slop )=> slop )?
					int alt37=2;
					int LA37_0 = input.LA(1);
					if ( (LA37_0==TILDA) ) {
						int LA37_1 = input.LA(2);
						if ( (LA37_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA37_3 = input.LA(3);
							if ( (synpred21_FTS()) ) {
								alt37=1;
							}
						}
					}
					switch (alt37) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:566:20: ( slop )=> slop
							{
							pushFollow(FOLLOW_slop_in_ftsTokenisedTermOrPhrase3573);
							slop91=slop();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_slop.add(slop91.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: FTSPHRASE, slop
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 567:17: -> ^( PHRASE FTSPHRASE ( slop )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:567:20: ^( PHRASE FTSPHRASE ( slop )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PHRASE, "PHRASE"), root_1);
						adaptor.addChild(root_1, stream_FTSPHRASE.nextNode());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:567:39: ( slop )?
						if ( stream_slop.hasNext() ) {
							adaptor.addChild(root_1, stream_slop.nextTree());
						}
						stream_slop.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:569:9: ftsWord ( ( fuzzy )=> fuzzy )?
					{
					pushFollow(FOLLOW_ftsWord_in_ftsTokenisedTermOrPhrase3623);
					ftsWord92=ftsWord();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord92.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:569:17: ( ( fuzzy )=> fuzzy )?
					int alt38=2;
					int LA38_0 = input.LA(1);
					if ( (LA38_0==TILDA) ) {
						int LA38_1 = input.LA(2);
						if ( (LA38_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA38_3 = input.LA(3);
							if ( (synpred22_FTS()) ) {
								alt38=1;
							}
						}
						else if ( (LA38_1==FLOATING_POINT_LITERAL) ) {
							int LA38_4 = input.LA(3);
							if ( (synpred22_FTS()) ) {
								alt38=1;
							}
						}
					}
					switch (alt38) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:569:18: ( fuzzy )=> fuzzy
							{
							pushFollow(FOLLOW_fuzzy_in_ftsTokenisedTermOrPhrase3632);
							fuzzy93=fuzzy();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy93.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsWord, fuzzy
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 570:17: -> ^( TERM ftsWord ( fuzzy )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:570:20: ^( TERM ftsWord ( fuzzy )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(TERM, "TERM"), root_1);
						adaptor.addChild(root_1, stream_ftsWord.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:570:35: ( fuzzy )?
						if ( stream_fuzzy.hasNext() ) {
							adaptor.addChild(root_1, stream_fuzzy.nextTree());
						}
						stream_fuzzy.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsTokenisedTermOrPhrase"


	public static class cmisTerm_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "cmisTerm"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:575:1: cmisTerm : ftsWord -> ftsWord ;
	public final FTSParser.cmisTerm_return cmisTerm() throws RecognitionException {
		FTSParser.cmisTerm_return retval = new FTSParser.cmisTerm_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope ftsWord94 =null;

		RewriteRuleSubtreeStream stream_ftsWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsWord");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:576:9: ( ftsWord -> ftsWord )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:577:9: ftsWord
			{
			pushFollow(FOLLOW_ftsWord_in_cmisTerm3705);
			ftsWord94=ftsWord();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsWord.add(ftsWord94.getTree());
			// AST REWRITE
			// elements: ftsWord
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 578:17: -> ftsWord
			{
				adaptor.addChild(root_0, stream_ftsWord.nextTree());
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cmisTerm"


	public static class cmisPhrase_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "cmisPhrase"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:582:1: cmisPhrase : FTSPHRASE -> FTSPHRASE ;
	public final FTSParser.cmisPhrase_return cmisPhrase() throws RecognitionException {
		FTSParser.cmisPhrase_return retval = new FTSParser.cmisPhrase_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token FTSPHRASE95=null;

		Object FTSPHRASE95_tree=null;
		RewriteRuleTokenStream stream_FTSPHRASE=new RewriteRuleTokenStream(adaptor,"token FTSPHRASE");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:583:9: ( FTSPHRASE -> FTSPHRASE )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:584:9: FTSPHRASE
			{
			FTSPHRASE95=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_cmisPhrase3759); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_FTSPHRASE.add(FTSPHRASE95);

			// AST REWRITE
			// elements: FTSPHRASE
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 585:17: -> FTSPHRASE
			{
				adaptor.addChild(root_0, stream_FTSPHRASE.nextNode());
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cmisPhrase"


	public static class ftsRange_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsRange"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:589:1: ftsRange : ( fieldReference COLON )? ftsFieldGroupRange -> ftsFieldGroupRange ( fieldReference )? ;
	public final FTSParser.ftsRange_return ftsRange() throws RecognitionException {
		FTSParser.ftsRange_return retval = new FTSParser.ftsRange_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token COLON97=null;
		ParserRuleReturnScope fieldReference96 =null;
		ParserRuleReturnScope ftsFieldGroupRange98 =null;

		Object COLON97_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
		RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:590:9: ( ( fieldReference COLON )? ftsFieldGroupRange -> ftsFieldGroupRange ( fieldReference )? )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:591:9: ( fieldReference COLON )? ftsFieldGroupRange
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:591:9: ( fieldReference COLON )?
			int alt40=2;
			switch ( input.LA(1) ) {
				case AND:
				case AT:
				case NOT:
				case OR:
				case TO:
					{
					alt40=1;
					}
					break;
				case ID:
					{
					int LA40_2 = input.LA(2);
					if ( (LA40_2==COLON||LA40_2==DOT) ) {
						alt40=1;
					}
					}
					break;
				case URI:
					{
					switch ( input.LA(2) ) {
						case ID:
							{
							int LA40_5 = input.LA(3);
							if ( (LA40_5==DOT) ) {
								int LA40_10 = input.LA(4);
								if ( (LA40_10==ID) ) {
									int LA40_11 = input.LA(5);
									if ( (LA40_11==COLON) ) {
										alt40=1;
									}
								}
							}
							else if ( (LA40_5==COLON) ) {
								alt40=1;
							}
							}
							break;
						case TO:
							{
							int LA40_6 = input.LA(3);
							if ( (LA40_6==COLON) ) {
								alt40=1;
							}
							}
							break;
						case OR:
							{
							int LA40_7 = input.LA(3);
							if ( (LA40_7==COLON) ) {
								alt40=1;
							}
							}
							break;
						case AND:
							{
							int LA40_8 = input.LA(3);
							if ( (LA40_8==COLON) ) {
								alt40=1;
							}
							}
							break;
						case NOT:
							{
							int LA40_9 = input.LA(3);
							if ( (LA40_9==COLON) ) {
								alt40=1;
							}
							}
							break;
					}
					}
					break;
			}
			switch (alt40) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:591:10: fieldReference COLON
					{
					pushFollow(FOLLOW_fieldReference_in_ftsRange3814);
					fieldReference96=fieldReference();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference96.getTree());
					COLON97=(Token)match(input,COLON,FOLLOW_COLON_in_ftsRange3816); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COLON.add(COLON97);

					}
					break;

			}

			pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsRange3820);
			ftsFieldGroupRange98=ftsFieldGroupRange();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange98.getTree());
			// AST REWRITE
			// elements: ftsFieldGroupRange, fieldReference
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 592:17: -> ftsFieldGroupRange ( fieldReference )?
			{
				adaptor.addChild(root_0, stream_ftsFieldGroupRange.nextTree());
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:592:39: ( fieldReference )?
				if ( stream_fieldReference.hasNext() ) {
					adaptor.addChild(root_0, stream_fieldReference.nextTree());
				}
				stream_fieldReference.reset();

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsRange"


	public static class ftsFieldGroup_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroup"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:595:1: ftsFieldGroup : fieldReference COLON LPAREN ftsFieldGroupDisjunction RPAREN -> ^( FIELD_GROUP fieldReference ftsFieldGroupDisjunction ) ;
	public final FTSParser.ftsFieldGroup_return ftsFieldGroup() throws RecognitionException {
		FTSParser.ftsFieldGroup_return retval = new FTSParser.ftsFieldGroup_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token COLON100=null;
		Token LPAREN101=null;
		Token RPAREN103=null;
		ParserRuleReturnScope fieldReference99 =null;
		ParserRuleReturnScope ftsFieldGroupDisjunction102 =null;

		Object COLON100_tree=null;
		Object LPAREN101_tree=null;
		Object RPAREN103_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_fieldReference=new RewriteRuleSubtreeStream(adaptor,"rule fieldReference");
		RewriteRuleSubtreeStream stream_ftsFieldGroupDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupDisjunction");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:596:9: ( fieldReference COLON LPAREN ftsFieldGroupDisjunction RPAREN -> ^( FIELD_GROUP fieldReference ftsFieldGroupDisjunction ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:597:9: fieldReference COLON LPAREN ftsFieldGroupDisjunction RPAREN
			{
			pushFollow(FOLLOW_fieldReference_in_ftsFieldGroup3876);
			fieldReference99=fieldReference();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_fieldReference.add(fieldReference99.getTree());
			COLON100=(Token)match(input,COLON,FOLLOW_COLON_in_ftsFieldGroup3878); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_COLON.add(COLON100);

			LPAREN101=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroup3880); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN101);

			pushFollow(FOLLOW_ftsFieldGroupDisjunction_in_ftsFieldGroup3882);
			ftsFieldGroupDisjunction102=ftsFieldGroupDisjunction();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupDisjunction.add(ftsFieldGroupDisjunction102.getTree());
			RPAREN103=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroup3884); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN103);

			// AST REWRITE
			// elements: fieldReference, ftsFieldGroupDisjunction
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 598:17: -> ^( FIELD_GROUP fieldReference ftsFieldGroupDisjunction )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:599:25: ^( FIELD_GROUP fieldReference ftsFieldGroupDisjunction )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_GROUP, "FIELD_GROUP"), root_1);
				adaptor.addChild(root_1, stream_fieldReference.nextTree());
				adaptor.addChild(root_1, stream_ftsFieldGroupDisjunction.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroup"


	public static class ftsFieldGroupDisjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupDisjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:602:1: ftsFieldGroupDisjunction : ({...}? ftsFieldGroupExplicitDisjunction |{...}? ftsFieldGroupImplicitDisjunction );
	public final FTSParser.ftsFieldGroupDisjunction_return ftsFieldGroupDisjunction() throws RecognitionException {
		FTSParser.ftsFieldGroupDisjunction_return retval = new FTSParser.ftsFieldGroupDisjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope ftsFieldGroupExplicitDisjunction104 =null;
		ParserRuleReturnScope ftsFieldGroupImplicitDisjunction105 =null;


		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:603:9: ({...}? ftsFieldGroupExplicitDisjunction |{...}? ftsFieldGroupImplicitDisjunction )
			int alt41=2;
			switch ( input.LA(1) ) {
			case AMP:
			case AND:
				{
				alt41=1;
				}
				break;
			case NOT:
				{
				int LA41_3 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ID:
				{
				int LA41_4 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FTSWORD:
				{
				int LA41_5 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FTSPRE:
				{
				int LA41_6 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FTSWILD:
				{
				int LA41_7 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 7, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case EXCLAMATION:
				{
				int LA41_8 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 8, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case TO:
				{
				int LA41_9 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 9, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case DECIMAL_INTEGER_LITERAL:
				{
				int LA41_10 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 10, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FLOATING_POINT_LITERAL:
				{
				int LA41_11 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 11, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case DATETIME:
				{
				int LA41_12 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 12, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case STAR:
				{
				int LA41_13 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 13, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case URI:
				{
				int LA41_14 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 14, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case COMMA:
			case DOT:
				{
				int LA41_15 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 15, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case QUESTION_MARK:
				{
				int LA41_16 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 16, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case EQUALS:
				{
				int LA41_17 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 17, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FTSPHRASE:
				{
				int LA41_18 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 18, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case TILDA:
				{
				int LA41_19 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 19, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LSQUARE:
				{
				int LA41_20 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 20, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LT:
				{
				int LA41_21 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 21, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LPAREN:
				{
				int LA41_22 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 22, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PLUS:
				{
				int LA41_23 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 23, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case BAR:
				{
				int LA41_24 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 24, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case MINUS:
				{
				int LA41_25 = input.LA(2);
				if ( ((defaultFieldConjunction() == true)) ) {
					alt41=1;
				}
				else if ( ((defaultFieldConjunction() == false)) ) {
					alt41=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 41, 25, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case OR:
				{
				alt41=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 41, 0, input);
				throw nvae;
			}
			switch (alt41) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:604:9: {...}? ftsFieldGroupExplicitDisjunction
					{
					root_0 = (Object)adaptor.nil();


					if ( !((defaultFieldConjunction() == true)) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "ftsFieldGroupDisjunction", "defaultFieldConjunction() == true");
					}
					pushFollow(FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupDisjunction3969);
					ftsFieldGroupExplicitDisjunction104=ftsFieldGroupExplicitDisjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsFieldGroupExplicitDisjunction104.getTree());

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:605:11: {...}? ftsFieldGroupImplicitDisjunction
					{
					root_0 = (Object)adaptor.nil();


					if ( !((defaultFieldConjunction() == false)) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "ftsFieldGroupDisjunction", "defaultFieldConjunction() == false");
					}
					pushFollow(FOLLOW_ftsFieldGroupImplicitDisjunction_in_ftsFieldGroupDisjunction3983);
					ftsFieldGroupImplicitDisjunction105=ftsFieldGroupImplicitDisjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsFieldGroupImplicitDisjunction105.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupDisjunction"


	public static class ftsFieldGroupExplicitDisjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupExplicitDisjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:608:1: ftsFieldGroupExplicitDisjunction : ftsFieldGroupImplicitConjunction ( or ftsFieldGroupImplicitConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupImplicitConjunction )+ ) ;
	public final FTSParser.ftsFieldGroupExplicitDisjunction_return ftsFieldGroupExplicitDisjunction() throws RecognitionException {
		FTSParser.ftsFieldGroupExplicitDisjunction_return retval = new FTSParser.ftsFieldGroupExplicitDisjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope ftsFieldGroupImplicitConjunction106 =null;
		ParserRuleReturnScope or107 =null;
		ParserRuleReturnScope ftsFieldGroupImplicitConjunction108 =null;

		RewriteRuleSubtreeStream stream_ftsFieldGroupImplicitConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupImplicitConjunction");
		RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:609:9: ( ftsFieldGroupImplicitConjunction ( or ftsFieldGroupImplicitConjunction )* -> ^( FIELD_DISJUNCTION ( ftsFieldGroupImplicitConjunction )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:610:9: ftsFieldGroupImplicitConjunction ( or ftsFieldGroupImplicitConjunction )*
			{
			pushFollow(FOLLOW_ftsFieldGroupImplicitConjunction_in_ftsFieldGroupExplicitDisjunction4016);
			ftsFieldGroupImplicitConjunction106=ftsFieldGroupImplicitConjunction();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunction.add(ftsFieldGroupImplicitConjunction106.getTree());
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:610:42: ( or ftsFieldGroupImplicitConjunction )*
			loop42:
			while (true) {
				int alt42=2;
				int LA42_0 = input.LA(1);
				if ( (LA42_0==BAR||LA42_0==OR) ) {
					alt42=1;
				}

				switch (alt42) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:610:43: or ftsFieldGroupImplicitConjunction
					{
					pushFollow(FOLLOW_or_in_ftsFieldGroupExplicitDisjunction4019);
					or107=or();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_or.add(or107.getTree());
					pushFollow(FOLLOW_ftsFieldGroupImplicitConjunction_in_ftsFieldGroupExplicitDisjunction4021);
					ftsFieldGroupImplicitConjunction108=ftsFieldGroupImplicitConjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupImplicitConjunction.add(ftsFieldGroupImplicitConjunction108.getTree());
					}
					break;

				default :
					break loop42;
				}
			}

			// AST REWRITE
			// elements: ftsFieldGroupImplicitConjunction
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 611:17: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupImplicitConjunction )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:612:25: ^( FIELD_DISJUNCTION ( ftsFieldGroupImplicitConjunction )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DISJUNCTION, "FIELD_DISJUNCTION"), root_1);
				if ( !(stream_ftsFieldGroupImplicitConjunction.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsFieldGroupImplicitConjunction.hasNext() ) {
					adaptor.addChild(root_1, stream_ftsFieldGroupImplicitConjunction.nextTree());
				}
				stream_ftsFieldGroupImplicitConjunction.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupExplicitDisjunction"


	public static class ftsFieldGroupImplicitDisjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupImplicitDisjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:615:1: ftsFieldGroupImplicitDisjunction : ( ( or )? ftsFieldGroupExplicitConjunction )+ -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitConjunction )+ ) ;
	public final FTSParser.ftsFieldGroupImplicitDisjunction_return ftsFieldGroupImplicitDisjunction() throws RecognitionException {
		FTSParser.ftsFieldGroupImplicitDisjunction_return retval = new FTSParser.ftsFieldGroupImplicitDisjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope or109 =null;
		ParserRuleReturnScope ftsFieldGroupExplicitConjunction110 =null;

		RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
		RewriteRuleSubtreeStream stream_ftsFieldGroupExplicitConjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExplicitConjunction");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:616:9: ( ( ( or )? ftsFieldGroupExplicitConjunction )+ -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitConjunction )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:617:9: ( ( or )? ftsFieldGroupExplicitConjunction )+
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:617:9: ( ( or )? ftsFieldGroupExplicitConjunction )+
			int cnt44=0;
			loop44:
			while (true) {
				int alt44=2;
				int LA44_0 = input.LA(1);
				if ( (LA44_0==BAR||LA44_0==COMMA||LA44_0==DATETIME||LA44_0==DECIMAL_INTEGER_LITERAL||LA44_0==DOT||LA44_0==EQUALS||LA44_0==EXCLAMATION||LA44_0==FLOATING_POINT_LITERAL||(LA44_0 >= FTSPHRASE && LA44_0 <= FTSWORD)||LA44_0==ID||(LA44_0 >= LPAREN && LA44_0 <= LT)||LA44_0==MINUS||LA44_0==NOT||LA44_0==OR||LA44_0==PLUS||LA44_0==QUESTION_MARK||LA44_0==STAR||(LA44_0 >= TILDA && LA44_0 <= TO)||LA44_0==URI) ) {
					alt44=1;
				}

				switch (alt44) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:617:10: ( or )? ftsFieldGroupExplicitConjunction
					{
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:617:10: ( or )?
					int alt43=2;
					int LA43_0 = input.LA(1);
					if ( (LA43_0==OR) ) {
						alt43=1;
					}
					else if ( (LA43_0==BAR) ) {
						int LA43_2 = input.LA(2);
						if ( (LA43_2==BAR) ) {
							alt43=1;
						}
					}
					switch (alt43) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:617:10: or
							{
							pushFollow(FOLLOW_or_in_ftsFieldGroupImplicitDisjunction4106);
							or109=or();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_or.add(or109.getTree());
							}
							break;

					}

					pushFollow(FOLLOW_ftsFieldGroupExplicitConjunction_in_ftsFieldGroupImplicitDisjunction4109);
					ftsFieldGroupExplicitConjunction110=ftsFieldGroupExplicitConjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupExplicitConjunction.add(ftsFieldGroupExplicitConjunction110.getTree());
					}
					break;

				default :
					if ( cnt44 >= 1 ) break loop44;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(44, input);
					throw eee;
				}
				cnt44++;
			}

			// AST REWRITE
			// elements: ftsFieldGroupExplicitConjunction
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 618:17: -> ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitConjunction )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:619:25: ^( FIELD_DISJUNCTION ( ftsFieldGroupExplicitConjunction )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DISJUNCTION, "FIELD_DISJUNCTION"), root_1);
				if ( !(stream_ftsFieldGroupExplicitConjunction.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsFieldGroupExplicitConjunction.hasNext() ) {
					adaptor.addChild(root_1, stream_ftsFieldGroupExplicitConjunction.nextTree());
				}
				stream_ftsFieldGroupExplicitConjunction.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupImplicitDisjunction"


	public static class ftsFieldGroupExplicitConjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupExplicitConjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:626:1: ftsFieldGroupExplicitConjunction : ftsFieldGroupPrefixed ( and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) ;
	public final FTSParser.ftsFieldGroupExplicitConjunction_return ftsFieldGroupExplicitConjunction() throws RecognitionException {
		FTSParser.ftsFieldGroupExplicitConjunction_return retval = new FTSParser.ftsFieldGroupExplicitConjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope ftsFieldGroupPrefixed111 =null;
		ParserRuleReturnScope and112 =null;
		ParserRuleReturnScope ftsFieldGroupPrefixed113 =null;

		RewriteRuleSubtreeStream stream_ftsFieldGroupPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPrefixed");
		RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:627:9: ( ftsFieldGroupPrefixed ( and ftsFieldGroupPrefixed )* -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:628:9: ftsFieldGroupPrefixed ( and ftsFieldGroupPrefixed )*
			{
			pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplicitConjunction4196);
			ftsFieldGroupPrefixed111=ftsFieldGroupPrefixed();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed111.getTree());
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:628:31: ( and ftsFieldGroupPrefixed )*
			loop45:
			while (true) {
				int alt45=2;
				int LA45_0 = input.LA(1);
				if ( ((LA45_0 >= AMP && LA45_0 <= AND)) ) {
					alt45=1;
				}

				switch (alt45) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:628:32: and ftsFieldGroupPrefixed
					{
					pushFollow(FOLLOW_and_in_ftsFieldGroupExplicitConjunction4199);
					and112=and();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_and.add(and112.getTree());
					pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplicitConjunction4201);
					ftsFieldGroupPrefixed113=ftsFieldGroupPrefixed();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed113.getTree());
					}
					break;

				default :
					break loop45;
				}
			}

			// AST REWRITE
			// elements: ftsFieldGroupPrefixed
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 629:17: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:630:25: ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_CONJUNCTION, "FIELD_CONJUNCTION"), root_1);
				if ( !(stream_ftsFieldGroupPrefixed.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsFieldGroupPrefixed.hasNext() ) {
					adaptor.addChild(root_1, stream_ftsFieldGroupPrefixed.nextTree());
				}
				stream_ftsFieldGroupPrefixed.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupExplicitConjunction"


	public static class ftsFieldGroupImplicitConjunction_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupImplicitConjunction"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:633:1: ftsFieldGroupImplicitConjunction : ( ( and )? ftsFieldGroupPrefixed )+ -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) ;
	public final FTSParser.ftsFieldGroupImplicitConjunction_return ftsFieldGroupImplicitConjunction() throws RecognitionException {
		FTSParser.ftsFieldGroupImplicitConjunction_return retval = new FTSParser.ftsFieldGroupImplicitConjunction_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope and114 =null;
		ParserRuleReturnScope ftsFieldGroupPrefixed115 =null;

		RewriteRuleSubtreeStream stream_ftsFieldGroupPrefixed=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPrefixed");
		RewriteRuleSubtreeStream stream_and=new RewriteRuleSubtreeStream(adaptor,"rule and");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:634:9: ( ( ( and )? ftsFieldGroupPrefixed )+ -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:635:9: ( ( and )? ftsFieldGroupPrefixed )+
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:635:9: ( ( and )? ftsFieldGroupPrefixed )+
			int cnt47=0;
			loop47:
			while (true) {
				int alt47=2;
				int LA47_0 = input.LA(1);
				if ( (LA47_0==BAR) ) {
					int LA47_2 = input.LA(2);
					if ( (LA47_2==COMMA||LA47_2==DATETIME||LA47_2==DECIMAL_INTEGER_LITERAL||LA47_2==DOT||LA47_2==EQUALS||LA47_2==FLOATING_POINT_LITERAL||(LA47_2 >= FTSPHRASE && LA47_2 <= FTSWORD)||LA47_2==ID||(LA47_2 >= LPAREN && LA47_2 <= LT)||LA47_2==NOT||LA47_2==QUESTION_MARK||LA47_2==STAR||(LA47_2 >= TILDA && LA47_2 <= TO)||LA47_2==URI) ) {
						alt47=1;
					}

				}
				else if ( ((LA47_0 >= AMP && LA47_0 <= AND)||LA47_0==COMMA||LA47_0==DATETIME||LA47_0==DECIMAL_INTEGER_LITERAL||LA47_0==DOT||LA47_0==EQUALS||LA47_0==EXCLAMATION||LA47_0==FLOATING_POINT_LITERAL||(LA47_0 >= FTSPHRASE && LA47_0 <= FTSWORD)||LA47_0==ID||(LA47_0 >= LPAREN && LA47_0 <= LT)||LA47_0==MINUS||LA47_0==NOT||LA47_0==PLUS||LA47_0==QUESTION_MARK||LA47_0==STAR||(LA47_0 >= TILDA && LA47_0 <= TO)||LA47_0==URI) ) {
					alt47=1;
				}

				switch (alt47) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:635:10: ( and )? ftsFieldGroupPrefixed
					{
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:635:10: ( and )?
					int alt46=2;
					int LA46_0 = input.LA(1);
					if ( ((LA46_0 >= AMP && LA46_0 <= AND)) ) {
						alt46=1;
					}
					switch (alt46) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:635:10: and
							{
							pushFollow(FOLLOW_and_in_ftsFieldGroupImplicitConjunction4286);
							and114=and();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_and.add(and114.getTree());
							}
							break;

					}

					pushFollow(FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupImplicitConjunction4289);
					ftsFieldGroupPrefixed115=ftsFieldGroupPrefixed();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupPrefixed.add(ftsFieldGroupPrefixed115.getTree());
					}
					break;

				default :
					if ( cnt47 >= 1 ) break loop47;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(47, input);
					throw eee;
				}
				cnt47++;
			}

			// AST REWRITE
			// elements: ftsFieldGroupPrefixed
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 636:17: -> ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:637:25: ^( FIELD_CONJUNCTION ( ftsFieldGroupPrefixed )+ )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_CONJUNCTION, "FIELD_CONJUNCTION"), root_1);
				if ( !(stream_ftsFieldGroupPrefixed.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsFieldGroupPrefixed.hasNext() ) {
					adaptor.addChild(root_1, stream_ftsFieldGroupPrefixed.nextTree());
				}
				stream_ftsFieldGroupPrefixed.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupImplicitConjunction"


	public static class ftsFieldGroupPrefixed_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupPrefixed"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:640:1: ftsFieldGroupPrefixed : ( ( not )=> not ftsFieldGroupTest ( boost )? -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? ) | ftsFieldGroupTest ( boost )? -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? ) | PLUS ftsFieldGroupTest ( boost )? -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? ) | BAR ftsFieldGroupTest ( boost )? -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? ) | MINUS ftsFieldGroupTest ( boost )? -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? ) );
	public final FTSParser.ftsFieldGroupPrefixed_return ftsFieldGroupPrefixed() throws RecognitionException {
		FTSParser.ftsFieldGroupPrefixed_return retval = new FTSParser.ftsFieldGroupPrefixed_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token PLUS121=null;
		Token BAR124=null;
		Token MINUS127=null;
		ParserRuleReturnScope not116 =null;
		ParserRuleReturnScope ftsFieldGroupTest117 =null;
		ParserRuleReturnScope boost118 =null;
		ParserRuleReturnScope ftsFieldGroupTest119 =null;
		ParserRuleReturnScope boost120 =null;
		ParserRuleReturnScope ftsFieldGroupTest122 =null;
		ParserRuleReturnScope boost123 =null;
		ParserRuleReturnScope ftsFieldGroupTest125 =null;
		ParserRuleReturnScope boost126 =null;
		ParserRuleReturnScope ftsFieldGroupTest128 =null;
		ParserRuleReturnScope boost129 =null;

		Object PLUS121_tree=null;
		Object BAR124_tree=null;
		Object MINUS127_tree=null;
		RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
		RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
		RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
		RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
		RewriteRuleSubtreeStream stream_boost=new RewriteRuleSubtreeStream(adaptor,"rule boost");
		RewriteRuleSubtreeStream stream_ftsFieldGroupTest=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTest");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:641:9: ( ( not )=> not ftsFieldGroupTest ( boost )? -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? ) | ftsFieldGroupTest ( boost )? -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? ) | PLUS ftsFieldGroupTest ( boost )? -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? ) | BAR ftsFieldGroupTest ( boost )? -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? ) | MINUS ftsFieldGroupTest ( boost )? -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? ) )
			int alt53=5;
			int LA53_0 = input.LA(1);
			if ( (LA53_0==NOT) ) {
				int LA53_1 = input.LA(2);
				if ( (synpred23_FTS()) ) {
					alt53=1;
				}
				else if ( (true) ) {
					alt53=2;
				}

			}
			else if ( (LA53_0==COMMA||LA53_0==DATETIME||LA53_0==DECIMAL_INTEGER_LITERAL||LA53_0==DOT||LA53_0==EQUALS||LA53_0==FLOATING_POINT_LITERAL||(LA53_0 >= FTSPHRASE && LA53_0 <= FTSWORD)||LA53_0==ID||(LA53_0 >= LPAREN && LA53_0 <= LT)||LA53_0==QUESTION_MARK||LA53_0==STAR||(LA53_0 >= TILDA && LA53_0 <= TO)||LA53_0==URI) ) {
				alt53=2;
			}
			else if ( (LA53_0==EXCLAMATION) && (synpred23_FTS())) {
				alt53=1;
			}
			else if ( (LA53_0==PLUS) ) {
				alt53=3;
			}
			else if ( (LA53_0==BAR) ) {
				alt53=4;
			}
			else if ( (LA53_0==MINUS) ) {
				alt53=5;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 53, 0, input);
				throw nvae;
			}

			switch (alt53) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:642:9: ( not )=> not ftsFieldGroupTest ( boost )?
					{
					pushFollow(FOLLOW_not_in_ftsFieldGroupPrefixed4379);
					not116=not();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_not.add(not116.getTree());
					pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4381);
					ftsFieldGroupTest117=ftsFieldGroupTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest117.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:642:40: ( boost )?
					int alt48=2;
					int LA48_0 = input.LA(1);
					if ( (LA48_0==CARAT) ) {
						alt48=1;
					}
					switch (alt48) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:642:40: boost
							{
							pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed4383);
							boost118=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost118.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: boost, ftsFieldGroupTest
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 643:17: -> ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:644:25: ^( FIELD_NEGATION ftsFieldGroupTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_NEGATION, "FIELD_NEGATION"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:644:60: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:645:11: ftsFieldGroupTest ( boost )?
					{
					pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4447);
					ftsFieldGroupTest119=ftsFieldGroupTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest119.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:645:29: ( boost )?
					int alt49=2;
					int LA49_0 = input.LA(1);
					if ( (LA49_0==CARAT) ) {
						alt49=1;
					}
					switch (alt49) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:645:29: boost
							{
							pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed4449);
							boost120=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost120.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsFieldGroupTest, boost
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 646:17: -> ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:647:25: ^( FIELD_DEFAULT ftsFieldGroupTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_DEFAULT, "FIELD_DEFAULT"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:647:59: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:648:11: PLUS ftsFieldGroupTest ( boost )?
					{
					PLUS121=(Token)match(input,PLUS,FOLLOW_PLUS_in_ftsFieldGroupPrefixed4513); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_PLUS.add(PLUS121);

					pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4515);
					ftsFieldGroupTest122=ftsFieldGroupTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest122.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:648:34: ( boost )?
					int alt50=2;
					int LA50_0 = input.LA(1);
					if ( (LA50_0==CARAT) ) {
						alt50=1;
					}
					switch (alt50) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:648:34: boost
							{
							pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed4517);
							boost123=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost123.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: boost, ftsFieldGroupTest
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 649:17: -> ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:650:25: ^( FIELD_MANDATORY ftsFieldGroupTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_MANDATORY, "FIELD_MANDATORY"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:650:61: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:651:11: BAR ftsFieldGroupTest ( boost )?
					{
					BAR124=(Token)match(input,BAR,FOLLOW_BAR_in_ftsFieldGroupPrefixed4581); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_BAR.add(BAR124);

					pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4583);
					ftsFieldGroupTest125=ftsFieldGroupTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest125.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:651:33: ( boost )?
					int alt51=2;
					int LA51_0 = input.LA(1);
					if ( (LA51_0==CARAT) ) {
						alt51=1;
					}
					switch (alt51) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:651:33: boost
							{
							pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed4585);
							boost126=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost126.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsFieldGroupTest, boost
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 652:17: -> ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:653:25: ^( FIELD_OPTIONAL ftsFieldGroupTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_OPTIONAL, "FIELD_OPTIONAL"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:653:60: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:654:11: MINUS ftsFieldGroupTest ( boost )?
					{
					MINUS127=(Token)match(input,MINUS,FOLLOW_MINUS_in_ftsFieldGroupPrefixed4649); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_MINUS.add(MINUS127);

					pushFollow(FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4651);
					ftsFieldGroupTest128=ftsFieldGroupTest();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupTest.add(ftsFieldGroupTest128.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:654:35: ( boost )?
					int alt52=2;
					int LA52_0 = input.LA(1);
					if ( (LA52_0==CARAT) ) {
						alt52=1;
					}
					switch (alt52) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:654:35: boost
							{
							pushFollow(FOLLOW_boost_in_ftsFieldGroupPrefixed4653);
							boost129=boost();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_boost.add(boost129.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: boost, ftsFieldGroupTest
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 655:17: -> ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:656:25: ^( FIELD_EXCLUDE ftsFieldGroupTest ( boost )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_EXCLUDE, "FIELD_EXCLUDE"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupTest.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:656:59: ( boost )?
						if ( stream_boost.hasNext() ) {
							adaptor.addChild(root_1, stream_boost.nextTree());
						}
						stream_boost.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupPrefixed"


	public static class ftsFieldGroupTest_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupTest"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:659:1: ftsFieldGroupTest : ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ( ftsFieldGroupTerm )=> ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ( ftsFieldGroupExactTerm )=> ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ( ftsFieldGroupPhrase )=> ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ( ftsFieldGroupExactPhrase )=> ftsFieldGroupExactPhrase ( ( slop )=> slop )? -> ^( FG_EXACT_PHRASE ftsFieldGroupExactPhrase ( slop )? ) | ( ftsFieldGroupTokenisedPhrase )=> ftsFieldGroupTokenisedPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupTokenisedPhrase ( slop )? ) | ( ftsFieldGroupSynonym )=> ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ( ftsFieldGroupRange )=> ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupDisjunction RPAREN -> ftsFieldGroupDisjunction );
	public final FTSParser.ftsFieldGroupTest_return ftsFieldGroupTest() throws RecognitionException {
		FTSParser.ftsFieldGroupTest_return retval = new FTSParser.ftsFieldGroupTest_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token LPAREN144=null;
		Token RPAREN146=null;
		ParserRuleReturnScope ftsFieldGroupProximity130 =null;
		ParserRuleReturnScope ftsFieldGroupTerm131 =null;
		ParserRuleReturnScope fuzzy132 =null;
		ParserRuleReturnScope ftsFieldGroupExactTerm133 =null;
		ParserRuleReturnScope fuzzy134 =null;
		ParserRuleReturnScope ftsFieldGroupPhrase135 =null;
		ParserRuleReturnScope slop136 =null;
		ParserRuleReturnScope ftsFieldGroupExactPhrase137 =null;
		ParserRuleReturnScope slop138 =null;
		ParserRuleReturnScope ftsFieldGroupTokenisedPhrase139 =null;
		ParserRuleReturnScope slop140 =null;
		ParserRuleReturnScope ftsFieldGroupSynonym141 =null;
		ParserRuleReturnScope fuzzy142 =null;
		ParserRuleReturnScope ftsFieldGroupRange143 =null;
		ParserRuleReturnScope ftsFieldGroupDisjunction145 =null;

		Object LPAREN144_tree=null;
		Object RPAREN146_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_ftsFieldGroupRange=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupRange");
		RewriteRuleSubtreeStream stream_ftsFieldGroupPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupPhrase");
		RewriteRuleSubtreeStream stream_ftsFieldGroupExactPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExactPhrase");
		RewriteRuleSubtreeStream stream_ftsFieldGroupTokenisedPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTokenisedPhrase");
		RewriteRuleSubtreeStream stream_fuzzy=new RewriteRuleSubtreeStream(adaptor,"rule fuzzy");
		RewriteRuleSubtreeStream stream_slop=new RewriteRuleSubtreeStream(adaptor,"rule slop");
		RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");
		RewriteRuleSubtreeStream stream_ftsFieldGroupSynonym=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupSynonym");
		RewriteRuleSubtreeStream stream_ftsFieldGroupExactTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExactTerm");
		RewriteRuleSubtreeStream stream_ftsFieldGroupDisjunction=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupDisjunction");
		RewriteRuleSubtreeStream stream_ftsFieldGroupProximity=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximity");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:660:9: ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ( ftsFieldGroupTerm )=> ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ( ftsFieldGroupExactTerm )=> ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ( ftsFieldGroupPhrase )=> ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ( ftsFieldGroupExactPhrase )=> ftsFieldGroupExactPhrase ( ( slop )=> slop )? -> ^( FG_EXACT_PHRASE ftsFieldGroupExactPhrase ( slop )? ) | ( ftsFieldGroupTokenisedPhrase )=> ftsFieldGroupTokenisedPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupTokenisedPhrase ( slop )? ) | ( ftsFieldGroupSynonym )=> ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ( ftsFieldGroupRange )=> ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupDisjunction RPAREN -> ftsFieldGroupDisjunction )
			int alt60=9;
			alt60 = dfa60.predict(input);
			switch (alt60) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:661:9: ( ftsFieldGroupProximity )=> ftsFieldGroupProximity
					{
					pushFollow(FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest4744);
					ftsFieldGroupProximity130=ftsFieldGroupProximity();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupProximity.add(ftsFieldGroupProximity130.getTree());
					// AST REWRITE
					// elements: ftsFieldGroupProximity
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 662:17: -> ^( FG_PROXIMITY ftsFieldGroupProximity )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:663:25: ^( FG_PROXIMITY ftsFieldGroupProximity )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PROXIMITY, "FG_PROXIMITY"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupProximity.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:664:11: ( ftsFieldGroupTerm )=> ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )?
					{
					pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest4810);
					ftsFieldGroupTerm131=ftsFieldGroupTerm();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm131.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:664:52: ( ( fuzzy )=> fuzzy )?
					int alt54=2;
					int LA54_0 = input.LA(1);
					if ( (LA54_0==TILDA) ) {
						int LA54_1 = input.LA(2);
						if ( (LA54_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA54_3 = input.LA(3);
							if ( (synpred26_FTS()) ) {
								alt54=1;
							}
						}
						else if ( (LA54_1==FLOATING_POINT_LITERAL) ) {
							int LA54_4 = input.LA(3);
							if ( (synpred26_FTS()) ) {
								alt54=1;
							}
						}
					}
					switch (alt54) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:664:54: ( fuzzy )=> fuzzy
							{
							pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest4820);
							fuzzy132=fuzzy();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy132.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: fuzzy, ftsFieldGroupTerm
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 665:17: -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:666:25: ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_TERM, "FG_TERM"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupTerm.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:666:53: ( fuzzy )?
						if ( stream_fuzzy.hasNext() ) {
							adaptor.addChild(root_1, stream_fuzzy.nextTree());
						}
						stream_fuzzy.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:667:11: ( ftsFieldGroupExactTerm )=> ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )?
					{
					pushFollow(FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest4891);
					ftsFieldGroupExactTerm133=ftsFieldGroupExactTerm();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupExactTerm.add(ftsFieldGroupExactTerm133.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:667:62: ( ( fuzzy )=> fuzzy )?
					int alt55=2;
					int LA55_0 = input.LA(1);
					if ( (LA55_0==TILDA) ) {
						int LA55_1 = input.LA(2);
						if ( (LA55_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA55_3 = input.LA(3);
							if ( (synpred28_FTS()) ) {
								alt55=1;
							}
						}
						else if ( (LA55_1==FLOATING_POINT_LITERAL) ) {
							int LA55_4 = input.LA(3);
							if ( (synpred28_FTS()) ) {
								alt55=1;
							}
						}
					}
					switch (alt55) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:667:64: ( fuzzy )=> fuzzy
							{
							pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest4901);
							fuzzy134=fuzzy();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy134.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: fuzzy, ftsFieldGroupExactTerm
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 668:17: -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:669:25: ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_EXACT_TERM, "FG_EXACT_TERM"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupExactTerm.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:669:64: ( fuzzy )?
						if ( stream_fuzzy.hasNext() ) {
							adaptor.addChild(root_1, stream_fuzzy.nextTree());
						}
						stream_fuzzy.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:670:11: ( ftsFieldGroupPhrase )=> ftsFieldGroupPhrase ( ( slop )=> slop )?
					{
					pushFollow(FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest4972);
					ftsFieldGroupPhrase135=ftsFieldGroupPhrase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupPhrase.add(ftsFieldGroupPhrase135.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:670:56: ( ( slop )=> slop )?
					int alt56=2;
					int LA56_0 = input.LA(1);
					if ( (LA56_0==TILDA) ) {
						int LA56_1 = input.LA(2);
						if ( (LA56_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA56_3 = input.LA(3);
							if ( (synpred30_FTS()) ) {
								alt56=1;
							}
						}
					}
					switch (alt56) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:670:58: ( slop )=> slop
							{
							pushFollow(FOLLOW_slop_in_ftsFieldGroupTest4982);
							slop136=slop();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_slop.add(slop136.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsFieldGroupPhrase, slop
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 671:17: -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:672:25: ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PHRASE, "FG_PHRASE"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupPhrase.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:672:57: ( slop )?
						if ( stream_slop.hasNext() ) {
							adaptor.addChild(root_1, stream_slop.nextTree());
						}
						stream_slop.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:673:11: ( ftsFieldGroupExactPhrase )=> ftsFieldGroupExactPhrase ( ( slop )=> slop )?
					{
					pushFollow(FOLLOW_ftsFieldGroupExactPhrase_in_ftsFieldGroupTest5053);
					ftsFieldGroupExactPhrase137=ftsFieldGroupExactPhrase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupExactPhrase.add(ftsFieldGroupExactPhrase137.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:673:66: ( ( slop )=> slop )?
					int alt57=2;
					int LA57_0 = input.LA(1);
					if ( (LA57_0==TILDA) ) {
						int LA57_1 = input.LA(2);
						if ( (LA57_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA57_3 = input.LA(3);
							if ( (synpred32_FTS()) ) {
								alt57=1;
							}
						}
					}
					switch (alt57) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:673:68: ( slop )=> slop
							{
							pushFollow(FOLLOW_slop_in_ftsFieldGroupTest5063);
							slop138=slop();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_slop.add(slop138.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: slop, ftsFieldGroupExactPhrase
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 674:17: -> ^( FG_EXACT_PHRASE ftsFieldGroupExactPhrase ( slop )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:675:25: ^( FG_EXACT_PHRASE ftsFieldGroupExactPhrase ( slop )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_EXACT_PHRASE, "FG_EXACT_PHRASE"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupExactPhrase.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:675:68: ( slop )?
						if ( stream_slop.hasNext() ) {
							adaptor.addChild(root_1, stream_slop.nextTree());
						}
						stream_slop.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 6 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:676:11: ( ftsFieldGroupTokenisedPhrase )=> ftsFieldGroupTokenisedPhrase ( ( slop )=> slop )?
					{
					pushFollow(FOLLOW_ftsFieldGroupTokenisedPhrase_in_ftsFieldGroupTest5134);
					ftsFieldGroupTokenisedPhrase139=ftsFieldGroupTokenisedPhrase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupTokenisedPhrase.add(ftsFieldGroupTokenisedPhrase139.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:676:74: ( ( slop )=> slop )?
					int alt58=2;
					int LA58_0 = input.LA(1);
					if ( (LA58_0==TILDA) ) {
						int LA58_1 = input.LA(2);
						if ( (LA58_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA58_3 = input.LA(3);
							if ( (synpred34_FTS()) ) {
								alt58=1;
							}
						}
					}
					switch (alt58) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:676:76: ( slop )=> slop
							{
							pushFollow(FOLLOW_slop_in_ftsFieldGroupTest5144);
							slop140=slop();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_slop.add(slop140.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ftsFieldGroupTokenisedPhrase, slop
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 677:17: -> ^( FG_PHRASE ftsFieldGroupTokenisedPhrase ( slop )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:678:25: ^( FG_PHRASE ftsFieldGroupTokenisedPhrase ( slop )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_PHRASE, "FG_PHRASE"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupTokenisedPhrase.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:678:66: ( slop )?
						if ( stream_slop.hasNext() ) {
							adaptor.addChild(root_1, stream_slop.nextTree());
						}
						stream_slop.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 7 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:11: ( ftsFieldGroupSynonym )=> ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )?
					{
					pushFollow(FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest5215);
					ftsFieldGroupSynonym141=ftsFieldGroupSynonym();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupSynonym.add(ftsFieldGroupSynonym141.getTree());
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:58: ( ( fuzzy )=> fuzzy )?
					int alt59=2;
					int LA59_0 = input.LA(1);
					if ( (LA59_0==TILDA) ) {
						int LA59_1 = input.LA(2);
						if ( (LA59_1==DECIMAL_INTEGER_LITERAL) ) {
							int LA59_3 = input.LA(3);
							if ( (synpred36_FTS()) ) {
								alt59=1;
							}
						}
						else if ( (LA59_1==FLOATING_POINT_LITERAL) ) {
							int LA59_4 = input.LA(3);
							if ( (synpred36_FTS()) ) {
								alt59=1;
							}
						}
					}
					switch (alt59) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:60: ( fuzzy )=> fuzzy
							{
							pushFollow(FOLLOW_fuzzy_in_ftsFieldGroupTest5225);
							fuzzy142=fuzzy();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_fuzzy.add(fuzzy142.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: fuzzy, ftsFieldGroupSynonym
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 680:17: -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:681:25: ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_SYNONYM, "FG_SYNONYM"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupSynonym.nextTree());
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:681:59: ( fuzzy )?
						if ( stream_fuzzy.hasNext() ) {
							adaptor.addChild(root_1, stream_fuzzy.nextTree());
						}
						stream_fuzzy.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 8 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:682:11: ( ftsFieldGroupRange )=> ftsFieldGroupRange
					{
					pushFollow(FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest5296);
					ftsFieldGroupRange143=ftsFieldGroupRange();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupRange.add(ftsFieldGroupRange143.getTree());
					// AST REWRITE
					// elements: ftsFieldGroupRange
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 683:17: -> ^( FG_RANGE ftsFieldGroupRange )
					{
						// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:684:25: ^( FG_RANGE ftsFieldGroupRange )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FG_RANGE, "FG_RANGE"), root_1);
						adaptor.addChild(root_1, stream_ftsFieldGroupRange.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 9 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:685:11: LPAREN ftsFieldGroupDisjunction RPAREN
					{
					LPAREN144=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ftsFieldGroupTest5356); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN144);

					pushFollow(FOLLOW_ftsFieldGroupDisjunction_in_ftsFieldGroupTest5358);
					ftsFieldGroupDisjunction145=ftsFieldGroupDisjunction();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupDisjunction.add(ftsFieldGroupDisjunction145.getTree());
					RPAREN146=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ftsFieldGroupTest5360); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN146);

					// AST REWRITE
					// elements: ftsFieldGroupDisjunction
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 686:17: -> ftsFieldGroupDisjunction
					{
						adaptor.addChild(root_0, stream_ftsFieldGroupDisjunction.nextTree());
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupTest"


	public static class ftsFieldGroupTerm_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupTerm"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:689:1: ftsFieldGroupTerm : ftsWord ;
	public final FTSParser.ftsFieldGroupTerm_return ftsFieldGroupTerm() throws RecognitionException {
		FTSParser.ftsFieldGroupTerm_return retval = new FTSParser.ftsFieldGroupTerm_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope ftsWord147 =null;


		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:690:9: ( ftsWord )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:691:9: ftsWord
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_ftsWord_in_ftsFieldGroupTerm5413);
			ftsWord147=ftsWord();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWord147.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupTerm"


	public static class ftsFieldGroupExactTerm_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupExactTerm"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:694:1: ftsFieldGroupExactTerm : EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm ;
	public final FTSParser.ftsFieldGroupExactTerm_return ftsFieldGroupExactTerm() throws RecognitionException {
		FTSParser.ftsFieldGroupExactTerm_return retval = new FTSParser.ftsFieldGroupExactTerm_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token EQUALS148=null;
		ParserRuleReturnScope ftsFieldGroupTerm149 =null;

		Object EQUALS148_tree=null;
		RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
		RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:695:9: ( EQUALS ftsFieldGroupTerm -> ftsFieldGroupTerm )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:696:9: EQUALS ftsFieldGroupTerm
			{
			EQUALS148=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsFieldGroupExactTerm5446); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS148);

			pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm5448);
			ftsFieldGroupTerm149=ftsFieldGroupTerm();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm149.getTree());
			// AST REWRITE
			// elements: ftsFieldGroupTerm
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 697:17: -> ftsFieldGroupTerm
			{
				adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupExactTerm"


	public static class ftsFieldGroupPhrase_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupPhrase"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:700:1: ftsFieldGroupPhrase : FTSPHRASE ;
	public final FTSParser.ftsFieldGroupPhrase_return ftsFieldGroupPhrase() throws RecognitionException {
		FTSParser.ftsFieldGroupPhrase_return retval = new FTSParser.ftsFieldGroupPhrase_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token FTSPHRASE150=null;

		Object FTSPHRASE150_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:701:9: ( FTSPHRASE )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:702:9: FTSPHRASE
			{
			root_0 = (Object)adaptor.nil();


			FTSPHRASE150=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase5501); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			FTSPHRASE150_tree = (Object)adaptor.create(FTSPHRASE150);
			adaptor.addChild(root_0, FTSPHRASE150_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupPhrase"


	public static class ftsFieldGroupExactPhrase_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupExactPhrase"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:705:1: ftsFieldGroupExactPhrase : EQUALS ftsFieldGroupExactPhrase -> ftsFieldGroupExactPhrase ;
	public final FTSParser.ftsFieldGroupExactPhrase_return ftsFieldGroupExactPhrase() throws RecognitionException {
		FTSParser.ftsFieldGroupExactPhrase_return retval = new FTSParser.ftsFieldGroupExactPhrase_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token EQUALS151=null;
		ParserRuleReturnScope ftsFieldGroupExactPhrase152 =null;

		Object EQUALS151_tree=null;
		RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
		RewriteRuleSubtreeStream stream_ftsFieldGroupExactPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExactPhrase");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:706:9: ( EQUALS ftsFieldGroupExactPhrase -> ftsFieldGroupExactPhrase )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:707:9: EQUALS ftsFieldGroupExactPhrase
			{
			EQUALS151=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_ftsFieldGroupExactPhrase5542); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS151);

			pushFollow(FOLLOW_ftsFieldGroupExactPhrase_in_ftsFieldGroupExactPhrase5544);
			ftsFieldGroupExactPhrase152=ftsFieldGroupExactPhrase();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupExactPhrase.add(ftsFieldGroupExactPhrase152.getTree());
			// AST REWRITE
			// elements: ftsFieldGroupExactPhrase
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 708:17: -> ftsFieldGroupExactPhrase
			{
				adaptor.addChild(root_0, stream_ftsFieldGroupExactPhrase.nextTree());
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupExactPhrase"


	public static class ftsFieldGroupTokenisedPhrase_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupTokenisedPhrase"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:711:1: ftsFieldGroupTokenisedPhrase : TILDA ftsFieldGroupExactPhrase -> ftsFieldGroupExactPhrase ;
	public final FTSParser.ftsFieldGroupTokenisedPhrase_return ftsFieldGroupTokenisedPhrase() throws RecognitionException {
		FTSParser.ftsFieldGroupTokenisedPhrase_return retval = new FTSParser.ftsFieldGroupTokenisedPhrase_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token TILDA153=null;
		ParserRuleReturnScope ftsFieldGroupExactPhrase154 =null;

		Object TILDA153_tree=null;
		RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
		RewriteRuleSubtreeStream stream_ftsFieldGroupExactPhrase=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupExactPhrase");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:712:9: ( TILDA ftsFieldGroupExactPhrase -> ftsFieldGroupExactPhrase )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:713:9: TILDA ftsFieldGroupExactPhrase
			{
			TILDA153=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsFieldGroupTokenisedPhrase5605); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TILDA.add(TILDA153);

			pushFollow(FOLLOW_ftsFieldGroupExactPhrase_in_ftsFieldGroupTokenisedPhrase5607);
			ftsFieldGroupExactPhrase154=ftsFieldGroupExactPhrase();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupExactPhrase.add(ftsFieldGroupExactPhrase154.getTree());
			// AST REWRITE
			// elements: ftsFieldGroupExactPhrase
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 714:17: -> ftsFieldGroupExactPhrase
			{
				adaptor.addChild(root_0, stream_ftsFieldGroupExactPhrase.nextTree());
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupTokenisedPhrase"


	public static class ftsFieldGroupSynonym_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupSynonym"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:717:1: ftsFieldGroupSynonym : TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm ;
	public final FTSParser.ftsFieldGroupSynonym_return ftsFieldGroupSynonym() throws RecognitionException {
		FTSParser.ftsFieldGroupSynonym_return retval = new FTSParser.ftsFieldGroupSynonym_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token TILDA155=null;
		ParserRuleReturnScope ftsFieldGroupTerm156 =null;

		Object TILDA155_tree=null;
		RewriteRuleTokenStream stream_TILDA=new RewriteRuleTokenStream(adaptor,"token TILDA");
		RewriteRuleSubtreeStream stream_ftsFieldGroupTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupTerm");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:718:9: ( TILDA ftsFieldGroupTerm -> ftsFieldGroupTerm )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:719:9: TILDA ftsFieldGroupTerm
			{
			TILDA155=(Token)match(input,TILDA,FOLLOW_TILDA_in_ftsFieldGroupSynonym5660); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TILDA.add(TILDA155);

			pushFollow(FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym5662);
			ftsFieldGroupTerm156=ftsFieldGroupTerm();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupTerm.add(ftsFieldGroupTerm156.getTree());
			// AST REWRITE
			// elements: ftsFieldGroupTerm
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 720:17: -> ftsFieldGroupTerm
			{
				adaptor.addChild(root_0, stream_ftsFieldGroupTerm.nextTree());
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupSynonym"


	public static class ftsFieldGroupProximity_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupProximity"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:723:1: ftsFieldGroupProximity : ftsFieldGroupProximityTerm ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+ -> ftsFieldGroupProximityTerm ( proximityGroup ftsFieldGroupProximityTerm )+ ;
	public final FTSParser.ftsFieldGroupProximity_return ftsFieldGroupProximity() throws RecognitionException {
		FTSParser.ftsFieldGroupProximity_return retval = new FTSParser.ftsFieldGroupProximity_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope ftsFieldGroupProximityTerm157 =null;
		ParserRuleReturnScope proximityGroup158 =null;
		ParserRuleReturnScope ftsFieldGroupProximityTerm159 =null;

		RewriteRuleSubtreeStream stream_proximityGroup=new RewriteRuleSubtreeStream(adaptor,"rule proximityGroup");
		RewriteRuleSubtreeStream stream_ftsFieldGroupProximityTerm=new RewriteRuleSubtreeStream(adaptor,"rule ftsFieldGroupProximityTerm");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:724:9: ( ftsFieldGroupProximityTerm ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+ -> ftsFieldGroupProximityTerm ( proximityGroup ftsFieldGroupProximityTerm )+ )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:725:9: ftsFieldGroupProximityTerm ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+
			{
			pushFollow(FOLLOW_ftsFieldGroupProximityTerm_in_ftsFieldGroupProximity5715);
			ftsFieldGroupProximityTerm157=ftsFieldGroupProximityTerm();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ftsFieldGroupProximityTerm.add(ftsFieldGroupProximityTerm157.getTree());
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:725:36: ( ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm )+
			int cnt61=0;
			loop61:
			while (true) {
				int alt61=2;
				int LA61_0 = input.LA(1);
				if ( (LA61_0==STAR) ) {
					switch ( input.LA(2) ) {
					case STAR:
						{
						int LA61_3 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case NOT:
						{
						int LA61_4 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case ID:
						{
						int LA61_5 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case FTSWORD:
						{
						int LA61_6 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case FTSPRE:
						{
						int LA61_7 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case FTSWILD:
						{
						int LA61_8 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case TO:
						{
						int LA61_9 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case DECIMAL_INTEGER_LITERAL:
						{
						int LA61_10 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case FLOATING_POINT_LITERAL:
						{
						int LA61_11 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case DATETIME:
						{
						int LA61_12 = input.LA(3);
						if ( (synpred38_FTS()) ) {
							alt61=1;
						}

						}
						break;
					case URI:
						{
						switch ( input.LA(3) ) {
						case ID:
							{
							int LA61_16 = input.LA(4);
							if ( (synpred38_FTS()) ) {
								alt61=1;
							}

							}
							break;
						case TO:
							{
							int LA61_17 = input.LA(4);
							if ( (synpred38_FTS()) ) {
								alt61=1;
							}

							}
							break;
						case OR:
							{
							int LA61_18 = input.LA(4);
							if ( (synpred38_FTS()) ) {
								alt61=1;
							}

							}
							break;
						case AND:
							{
							int LA61_19 = input.LA(4);
							if ( (synpred38_FTS()) ) {
								alt61=1;
							}

							}
							break;
						case NOT:
							{
							int LA61_20 = input.LA(4);
							if ( (synpred38_FTS()) ) {
								alt61=1;
							}

							}
							break;
						}
						}
						break;
					case LPAREN:
						{
						int LA61_14 = input.LA(3);
						if ( (LA61_14==DECIMAL_INTEGER_LITERAL) ) {
							int LA61_21 = input.LA(4);
							if ( (LA61_21==RPAREN) ) {
								switch ( input.LA(5) ) {
								case NOT:
									{
									int LA61_24 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case ID:
									{
									int LA61_25 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case FTSWORD:
									{
									int LA61_26 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case FTSPRE:
									{
									int LA61_27 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case FTSWILD:
									{
									int LA61_28 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case TO:
									{
									int LA61_29 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case DECIMAL_INTEGER_LITERAL:
									{
									int LA61_30 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case FLOATING_POINT_LITERAL:
									{
									int LA61_31 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case DATETIME:
									{
									int LA61_32 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case STAR:
									{
									int LA61_33 = input.LA(6);
									if ( (synpred38_FTS()) ) {
										alt61=1;
									}

									}
									break;
								case URI:
									{
									switch ( input.LA(6) ) {
									case ID:
										{
										int LA61_16 = input.LA(7);
										if ( (synpred38_FTS()) ) {
											alt61=1;
										}

										}
										break;
									case TO:
										{
										int LA61_17 = input.LA(7);
										if ( (synpred38_FTS()) ) {
											alt61=1;
										}

										}
										break;
									case OR:
										{
										int LA61_18 = input.LA(7);
										if ( (synpred38_FTS()) ) {
											alt61=1;
										}

										}
										break;
									case AND:
										{
										int LA61_19 = input.LA(7);
										if ( (synpred38_FTS()) ) {
											alt61=1;
										}

										}
										break;
									case NOT:
										{
										int LA61_20 = input.LA(7);
										if ( (synpred38_FTS()) ) {
											alt61=1;
										}

										}
										break;
									}
									}
									break;
								}
							}

						}
						else if ( (LA61_14==RPAREN) && (synpred38_FTS())) {
							alt61=1;
						}

						}
						break;
					}
				}

				switch (alt61) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:725:38: ( proximityGroup )=> proximityGroup ftsFieldGroupProximityTerm
					{
					pushFollow(FOLLOW_proximityGroup_in_ftsFieldGroupProximity5725);
					proximityGroup158=proximityGroup();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_proximityGroup.add(proximityGroup158.getTree());
					pushFollow(FOLLOW_ftsFieldGroupProximityTerm_in_ftsFieldGroupProximity5727);
					ftsFieldGroupProximityTerm159=ftsFieldGroupProximityTerm();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsFieldGroupProximityTerm.add(ftsFieldGroupProximityTerm159.getTree());
					}
					break;

				default :
					if ( cnt61 >= 1 ) break loop61;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(61, input);
					throw eee;
				}
				cnt61++;
			}

			// AST REWRITE
			// elements: ftsFieldGroupProximityTerm, proximityGroup, ftsFieldGroupProximityTerm
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 726:17: -> ftsFieldGroupProximityTerm ( proximityGroup ftsFieldGroupProximityTerm )+
			{
				adaptor.addChild(root_0, stream_ftsFieldGroupProximityTerm.nextTree());
				if ( !(stream_ftsFieldGroupProximityTerm.hasNext()||stream_proximityGroup.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_ftsFieldGroupProximityTerm.hasNext()||stream_proximityGroup.hasNext() ) {
					adaptor.addChild(root_0, stream_proximityGroup.nextTree());
					adaptor.addChild(root_0, stream_ftsFieldGroupProximityTerm.nextTree());
				}
				stream_ftsFieldGroupProximityTerm.reset();
				stream_proximityGroup.reset();

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupProximity"


	public static class ftsFieldGroupProximityTerm_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupProximityTerm"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:729:1: ftsFieldGroupProximityTerm : ( ID | FTSWORD | FTSPRE | FTSWILD | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | DATETIME | STAR | URI identifier );
	public final FTSParser.ftsFieldGroupProximityTerm_return ftsFieldGroupProximityTerm() throws RecognitionException {
		FTSParser.ftsFieldGroupProximityTerm_return retval = new FTSParser.ftsFieldGroupProximityTerm_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token ID160=null;
		Token FTSWORD161=null;
		Token FTSPRE162=null;
		Token FTSWILD163=null;
		Token NOT164=null;
		Token TO165=null;
		Token DECIMAL_INTEGER_LITERAL166=null;
		Token FLOATING_POINT_LITERAL167=null;
		Token DATETIME168=null;
		Token STAR169=null;
		Token URI170=null;
		ParserRuleReturnScope identifier171 =null;

		Object ID160_tree=null;
		Object FTSWORD161_tree=null;
		Object FTSPRE162_tree=null;
		Object FTSWILD163_tree=null;
		Object NOT164_tree=null;
		Object TO165_tree=null;
		Object DECIMAL_INTEGER_LITERAL166_tree=null;
		Object FLOATING_POINT_LITERAL167_tree=null;
		Object DATETIME168_tree=null;
		Object STAR169_tree=null;
		Object URI170_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:730:9: ( ID | FTSWORD | FTSPRE | FTSWILD | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | DATETIME | STAR | URI identifier )
			int alt62=11;
			switch ( input.LA(1) ) {
			case ID:
				{
				alt62=1;
				}
				break;
			case FTSWORD:
				{
				alt62=2;
				}
				break;
			case FTSPRE:
				{
				alt62=3;
				}
				break;
			case FTSWILD:
				{
				alt62=4;
				}
				break;
			case NOT:
				{
				alt62=5;
				}
				break;
			case TO:
				{
				alt62=6;
				}
				break;
			case DECIMAL_INTEGER_LITERAL:
				{
				alt62=7;
				}
				break;
			case FLOATING_POINT_LITERAL:
				{
				alt62=8;
				}
				break;
			case DATETIME:
				{
				alt62=9;
				}
				break;
			case STAR:
				{
				alt62=10;
				}
				break;
			case URI:
				{
				alt62=11;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 62, 0, input);
				throw nvae;
			}
			switch (alt62) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:731:11: ID
					{
					root_0 = (Object)adaptor.nil();


					ID160=(Token)match(input,ID,FOLLOW_ID_in_ftsFieldGroupProximityTerm5791); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					ID160_tree = (Object)adaptor.create(ID160);
					adaptor.addChild(root_0, ID160_tree);
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:732:11: FTSWORD
					{
					root_0 = (Object)adaptor.nil();


					FTSWORD161=(Token)match(input,FTSWORD,FOLLOW_FTSWORD_in_ftsFieldGroupProximityTerm5803); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSWORD161_tree = (Object)adaptor.create(FTSWORD161);
					adaptor.addChild(root_0, FTSWORD161_tree);
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:733:11: FTSPRE
					{
					root_0 = (Object)adaptor.nil();


					FTSPRE162=(Token)match(input,FTSPRE,FOLLOW_FTSPRE_in_ftsFieldGroupProximityTerm5815); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSPRE162_tree = (Object)adaptor.create(FTSPRE162);
					adaptor.addChild(root_0, FTSPRE162_tree);
					}

					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:734:11: FTSWILD
					{
					root_0 = (Object)adaptor.nil();


					FTSWILD163=(Token)match(input,FTSWILD,FOLLOW_FTSWILD_in_ftsFieldGroupProximityTerm5827); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSWILD163_tree = (Object)adaptor.create(FTSWILD163);
					adaptor.addChild(root_0, FTSWILD163_tree);
					}

					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:735:11: NOT
					{
					root_0 = (Object)adaptor.nil();


					NOT164=(Token)match(input,NOT,FOLLOW_NOT_in_ftsFieldGroupProximityTerm5839); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					NOT164_tree = (Object)adaptor.create(NOT164);
					adaptor.addChild(root_0, NOT164_tree);
					}

					}
					break;
				case 6 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:736:11: TO
					{
					root_0 = (Object)adaptor.nil();


					TO165=(Token)match(input,TO,FOLLOW_TO_in_ftsFieldGroupProximityTerm5851); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					TO165_tree = (Object)adaptor.create(TO165);
					adaptor.addChild(root_0, TO165_tree);
					}

					}
					break;
				case 7 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:737:11: DECIMAL_INTEGER_LITERAL
					{
					root_0 = (Object)adaptor.nil();


					DECIMAL_INTEGER_LITERAL166=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_ftsFieldGroupProximityTerm5863); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					DECIMAL_INTEGER_LITERAL166_tree = (Object)adaptor.create(DECIMAL_INTEGER_LITERAL166);
					adaptor.addChild(root_0, DECIMAL_INTEGER_LITERAL166_tree);
					}

					}
					break;
				case 8 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:738:11: FLOATING_POINT_LITERAL
					{
					root_0 = (Object)adaptor.nil();


					FLOATING_POINT_LITERAL167=(Token)match(input,FLOATING_POINT_LITERAL,FOLLOW_FLOATING_POINT_LITERAL_in_ftsFieldGroupProximityTerm5875); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FLOATING_POINT_LITERAL167_tree = (Object)adaptor.create(FLOATING_POINT_LITERAL167);
					adaptor.addChild(root_0, FLOATING_POINT_LITERAL167_tree);
					}

					}
					break;
				case 9 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:739:11: DATETIME
					{
					root_0 = (Object)adaptor.nil();


					DATETIME168=(Token)match(input,DATETIME,FOLLOW_DATETIME_in_ftsFieldGroupProximityTerm5887); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					DATETIME168_tree = (Object)adaptor.create(DATETIME168);
					adaptor.addChild(root_0, DATETIME168_tree);
					}

					}
					break;
				case 10 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:740:11: STAR
					{
					root_0 = (Object)adaptor.nil();


					STAR169=(Token)match(input,STAR,FOLLOW_STAR_in_ftsFieldGroupProximityTerm5899); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					STAR169_tree = (Object)adaptor.create(STAR169);
					adaptor.addChild(root_0, STAR169_tree);
					}

					}
					break;
				case 11 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:741:11: URI identifier
					{
					root_0 = (Object)adaptor.nil();


					URI170=(Token)match(input,URI,FOLLOW_URI_in_ftsFieldGroupProximityTerm5911); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					URI170_tree = (Object)adaptor.create(URI170);
					adaptor.addChild(root_0, URI170_tree);
					}

					pushFollow(FOLLOW_identifier_in_ftsFieldGroupProximityTerm5913);
					identifier171=identifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier171.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupProximityTerm"


	public static class proximityGroup_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "proximityGroup"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:744:1: proximityGroup : STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )? -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? ) ;
	public final FTSParser.proximityGroup_return proximityGroup() throws RecognitionException {
		FTSParser.proximityGroup_return retval = new FTSParser.proximityGroup_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token STAR172=null;
		Token LPAREN173=null;
		Token DECIMAL_INTEGER_LITERAL174=null;
		Token RPAREN175=null;

		Object STAR172_tree=null;
		Object LPAREN173_tree=null;
		Object DECIMAL_INTEGER_LITERAL174_tree=null;
		Object RPAREN175_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
		RewriteRuleTokenStream stream_DECIMAL_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_INTEGER_LITERAL");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:745:9: ( STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )? -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:746:9: STAR ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )?
			{
			STAR172=(Token)match(input,STAR,FOLLOW_STAR_in_proximityGroup5946); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_STAR.add(STAR172);

			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:746:14: ( LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN )?
			int alt64=2;
			int LA64_0 = input.LA(1);
			if ( (LA64_0==LPAREN) ) {
				alt64=1;
			}
			switch (alt64) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:746:15: LPAREN ( DECIMAL_INTEGER_LITERAL )? RPAREN
					{
					LPAREN173=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proximityGroup5949); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN173);

					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:746:22: ( DECIMAL_INTEGER_LITERAL )?
					int alt63=2;
					int LA63_0 = input.LA(1);
					if ( (LA63_0==DECIMAL_INTEGER_LITERAL) ) {
						alt63=1;
					}
					switch (alt63) {
						case 1 :
							// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:746:22: DECIMAL_INTEGER_LITERAL
							{
							DECIMAL_INTEGER_LITERAL174=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_proximityGroup5951); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_DECIMAL_INTEGER_LITERAL.add(DECIMAL_INTEGER_LITERAL174);

							}
							break;

					}

					RPAREN175=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proximityGroup5954); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN175);

					}
					break;

			}

			// AST REWRITE
			// elements: DECIMAL_INTEGER_LITERAL
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 747:17: -> ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:748:25: ^( PROXIMITY ( DECIMAL_INTEGER_LITERAL )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PROXIMITY, "PROXIMITY"), root_1);
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:748:37: ( DECIMAL_INTEGER_LITERAL )?
				if ( stream_DECIMAL_INTEGER_LITERAL.hasNext() ) {
					adaptor.addChild(root_1, stream_DECIMAL_INTEGER_LITERAL.nextNode());
				}
				stream_DECIMAL_INTEGER_LITERAL.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "proximityGroup"


	public static class ftsFieldGroupRange_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsFieldGroupRange"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:751:1: ftsFieldGroupRange : ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right );
	public final FTSParser.ftsFieldGroupRange_return ftsFieldGroupRange() throws RecognitionException {
		FTSParser.ftsFieldGroupRange_return retval = new FTSParser.ftsFieldGroupRange_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token DOTDOT177=null;
		Token TO181=null;
		ParserRuleReturnScope ftsRangeWord176 =null;
		ParserRuleReturnScope ftsRangeWord178 =null;
		ParserRuleReturnScope range_left179 =null;
		ParserRuleReturnScope ftsRangeWord180 =null;
		ParserRuleReturnScope ftsRangeWord182 =null;
		ParserRuleReturnScope range_right183 =null;

		Object DOTDOT177_tree=null;
		Object TO181_tree=null;
		RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
		RewriteRuleTokenStream stream_TO=new RewriteRuleTokenStream(adaptor,"token TO");
		RewriteRuleSubtreeStream stream_range_left=new RewriteRuleSubtreeStream(adaptor,"rule range_left");
		RewriteRuleSubtreeStream stream_range_right=new RewriteRuleSubtreeStream(adaptor,"rule range_right");
		RewriteRuleSubtreeStream stream_ftsRangeWord=new RewriteRuleSubtreeStream(adaptor,"rule ftsRangeWord");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:752:9: ( ftsRangeWord DOTDOT ftsRangeWord -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE | range_left ftsRangeWord TO ftsRangeWord range_right -> range_left ftsRangeWord ftsRangeWord range_right )
			int alt65=2;
			int LA65_0 = input.LA(1);
			if ( (LA65_0==DATETIME||LA65_0==DECIMAL_INTEGER_LITERAL||LA65_0==FLOATING_POINT_LITERAL||(LA65_0 >= FTSPHRASE && LA65_0 <= FTSWORD)||LA65_0==ID||LA65_0==STAR||LA65_0==URI) ) {
				alt65=1;
			}
			else if ( ((LA65_0 >= LSQUARE && LA65_0 <= LT)) ) {
				alt65=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 65, 0, input);
				throw nvae;
			}

			switch (alt65) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:753:9: ftsRangeWord DOTDOT ftsRangeWord
					{
					pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange6038);
					ftsRangeWord176=ftsRangeWord();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord176.getTree());
					DOTDOT177=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_ftsFieldGroupRange6040); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOTDOT.add(DOTDOT177);

					pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange6042);
					ftsRangeWord178=ftsRangeWord();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord178.getTree());
					// AST REWRITE
					// elements: ftsRangeWord, ftsRangeWord
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 754:17: -> INCLUSIVE ftsRangeWord ftsRangeWord INCLUSIVE
					{
						adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));
						adaptor.addChild(root_0, stream_ftsRangeWord.nextTree());
						adaptor.addChild(root_0, stream_ftsRangeWord.nextTree());
						adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:755:11: range_left ftsRangeWord TO ftsRangeWord range_right
					{
					pushFollow(FOLLOW_range_left_in_ftsFieldGroupRange6080);
					range_left179=range_left();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_range_left.add(range_left179.getTree());
					pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange6082);
					ftsRangeWord180=ftsRangeWord();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord180.getTree());
					TO181=(Token)match(input,TO,FOLLOW_TO_in_ftsFieldGroupRange6084); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_TO.add(TO181);

					pushFollow(FOLLOW_ftsRangeWord_in_ftsFieldGroupRange6086);
					ftsRangeWord182=ftsRangeWord();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ftsRangeWord.add(ftsRangeWord182.getTree());
					pushFollow(FOLLOW_range_right_in_ftsFieldGroupRange6088);
					range_right183=range_right();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_range_right.add(range_right183.getTree());
					// AST REWRITE
					// elements: range_right, ftsRangeWord, ftsRangeWord, range_left
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 756:17: -> range_left ftsRangeWord ftsRangeWord range_right
					{
						adaptor.addChild(root_0, stream_range_left.nextTree());
						adaptor.addChild(root_0, stream_ftsRangeWord.nextTree());
						adaptor.addChild(root_0, stream_ftsRangeWord.nextTree());
						adaptor.addChild(root_0, stream_range_right.nextTree());
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsFieldGroupRange"


	public static class range_left_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "range_left"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:759:1: range_left : ( LSQUARE -> INCLUSIVE | LT -> EXCLUSIVE );
	public final FTSParser.range_left_return range_left() throws RecognitionException {
		FTSParser.range_left_return retval = new FTSParser.range_left_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token LSQUARE184=null;
		Token LT185=null;

		Object LSQUARE184_tree=null;
		Object LT185_tree=null;
		RewriteRuleTokenStream stream_LT=new RewriteRuleTokenStream(adaptor,"token LT");
		RewriteRuleTokenStream stream_LSQUARE=new RewriteRuleTokenStream(adaptor,"token LSQUARE");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:760:9: ( LSQUARE -> INCLUSIVE | LT -> EXCLUSIVE )
			int alt66=2;
			int LA66_0 = input.LA(1);
			if ( (LA66_0==LSQUARE) ) {
				alt66=1;
			}
			else if ( (LA66_0==LT) ) {
				alt66=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 66, 0, input);
				throw nvae;
			}

			switch (alt66) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:761:9: LSQUARE
					{
					LSQUARE184=(Token)match(input,LSQUARE,FOLLOW_LSQUARE_in_range_left6147); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LSQUARE.add(LSQUARE184);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 762:17: -> INCLUSIVE
					{
						adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:763:11: LT
					{
					LT185=(Token)match(input,LT,FOLLOW_LT_in_range_left6179); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LT.add(LT185);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 764:17: -> EXCLUSIVE
					{
						adaptor.addChild(root_0, (Object)adaptor.create(EXCLUSIVE, "EXCLUSIVE"));
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "range_left"


	public static class range_right_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "range_right"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:767:1: range_right : ( RSQUARE -> INCLUSIVE | GT -> EXCLUSIVE );
	public final FTSParser.range_right_return range_right() throws RecognitionException {
		FTSParser.range_right_return retval = new FTSParser.range_right_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token RSQUARE186=null;
		Token GT187=null;

		Object RSQUARE186_tree=null;
		Object GT187_tree=null;
		RewriteRuleTokenStream stream_GT=new RewriteRuleTokenStream(adaptor,"token GT");
		RewriteRuleTokenStream stream_RSQUARE=new RewriteRuleTokenStream(adaptor,"token RSQUARE");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:768:9: ( RSQUARE -> INCLUSIVE | GT -> EXCLUSIVE )
			int alt67=2;
			int LA67_0 = input.LA(1);
			if ( (LA67_0==RSQUARE) ) {
				alt67=1;
			}
			else if ( (LA67_0==GT) ) {
				alt67=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 67, 0, input);
				throw nvae;
			}

			switch (alt67) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:769:9: RSQUARE
					{
					RSQUARE186=(Token)match(input,RSQUARE,FOLLOW_RSQUARE_in_range_right6232); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RSQUARE.add(RSQUARE186);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 770:17: -> INCLUSIVE
					{
						adaptor.addChild(root_0, (Object)adaptor.create(INCLUSIVE, "INCLUSIVE"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:771:11: GT
					{
					GT187=(Token)match(input,GT,FOLLOW_GT_in_range_right6264); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_GT.add(GT187);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 772:17: -> EXCLUSIVE
					{
						adaptor.addChild(root_0, (Object)adaptor.create(EXCLUSIVE, "EXCLUSIVE"));
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "range_right"


	public static class fieldReference_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "fieldReference"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:777:1: fieldReference : ( AT )? ( ( prefix )=> prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) ;
	public final FTSParser.fieldReference_return fieldReference() throws RecognitionException {
		FTSParser.fieldReference_return retval = new FTSParser.fieldReference_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token AT188=null;
		ParserRuleReturnScope prefix189 =null;
		ParserRuleReturnScope uri190 =null;
		ParserRuleReturnScope identifier191 =null;

		Object AT188_tree=null;
		RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
		RewriteRuleSubtreeStream stream_prefix=new RewriteRuleSubtreeStream(adaptor,"rule prefix");
		RewriteRuleSubtreeStream stream_uri=new RewriteRuleSubtreeStream(adaptor,"rule uri");
		RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:778:9: ( ( AT )? ( ( prefix )=> prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:779:9: ( AT )? ( ( prefix )=> prefix | uri )? identifier
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:779:9: ( AT )?
			int alt68=2;
			int LA68_0 = input.LA(1);
			if ( (LA68_0==AT) ) {
				alt68=1;
			}
			switch (alt68) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:779:9: AT
					{
					AT188=(Token)match(input,AT,FOLLOW_AT_in_fieldReference6320); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_AT.add(AT188);

					}
					break;

			}

			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:780:9: ( ( prefix )=> prefix | uri )?
			int alt69=3;
			switch ( input.LA(1) ) {
				case ID:
					{
					int LA69_1 = input.LA(2);
					if ( (LA69_1==DOT) ) {
						int LA69_7 = input.LA(3);
						if ( (LA69_7==ID) ) {
							int LA69_9 = input.LA(4);
							if ( (LA69_9==COLON) ) {
								int LA69_8 = input.LA(5);
								if ( (LA69_8==ID) ) {
									int LA69_11 = input.LA(6);
									if ( (LA69_11==DOT) ) {
										int LA69_16 = input.LA(7);
										if ( (LA69_16==ID) ) {
											int LA69_18 = input.LA(8);
											if ( (synpred39_FTS()) ) {
												alt69=1;
											}
										}
									}
									else if ( (LA69_11==COLON) && (synpred39_FTS())) {
										alt69=1;
									}
								}
								else if ( (LA69_8==TO) ) {
									int LA69_12 = input.LA(6);
									if ( (LA69_12==COLON) && (synpred39_FTS())) {
										alt69=1;
									}
								}
								else if ( (LA69_8==OR) && (synpred39_FTS())) {
									alt69=1;
								}
								else if ( (LA69_8==AND) && (synpred39_FTS())) {
									alt69=1;
								}
								else if ( (LA69_8==NOT) ) {
									int LA69_15 = input.LA(6);
									if ( (LA69_15==COLON) && (synpred39_FTS())) {
										alt69=1;
									}
								}
							}
						}
					}
					else if ( (LA69_1==COLON) ) {
						int LA69_8 = input.LA(3);
						if ( (LA69_8==ID) ) {
							int LA69_11 = input.LA(4);
							if ( (LA69_11==DOT) ) {
								int LA69_16 = input.LA(5);
								if ( (LA69_16==ID) ) {
									int LA69_18 = input.LA(6);
									if ( (synpred39_FTS()) ) {
										alt69=1;
									}
								}
							}
							else if ( (LA69_11==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==TO) ) {
							int LA69_12 = input.LA(4);
							if ( (LA69_12==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==OR) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==AND) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==NOT) ) {
							int LA69_15 = input.LA(4);
							if ( (LA69_15==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
					}
					}
					break;
				case TO:
					{
					int LA69_2 = input.LA(2);
					if ( (LA69_2==COLON) ) {
						int LA69_8 = input.LA(3);
						if ( (LA69_8==ID) ) {
							int LA69_11 = input.LA(4);
							if ( (LA69_11==DOT) ) {
								int LA69_16 = input.LA(5);
								if ( (LA69_16==ID) ) {
									int LA69_18 = input.LA(6);
									if ( (synpred39_FTS()) ) {
										alt69=1;
									}
								}
							}
							else if ( (LA69_11==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==TO) ) {
							int LA69_12 = input.LA(4);
							if ( (LA69_12==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==OR) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==AND) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==NOT) ) {
							int LA69_15 = input.LA(4);
							if ( (LA69_15==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
					}
					}
					break;
				case OR:
					{
					int LA69_3 = input.LA(2);
					if ( (LA69_3==COLON) ) {
						int LA69_8 = input.LA(3);
						if ( (LA69_8==ID) ) {
							int LA69_11 = input.LA(4);
							if ( (LA69_11==DOT) ) {
								int LA69_16 = input.LA(5);
								if ( (LA69_16==ID) ) {
									int LA69_18 = input.LA(6);
									if ( (synpred39_FTS()) ) {
										alt69=1;
									}
								}
							}
							else if ( (LA69_11==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==TO) ) {
							int LA69_12 = input.LA(4);
							if ( (LA69_12==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==OR) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==AND) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==NOT) ) {
							int LA69_15 = input.LA(4);
							if ( (LA69_15==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
					}
					}
					break;
				case AND:
					{
					int LA69_4 = input.LA(2);
					if ( (LA69_4==COLON) ) {
						int LA69_8 = input.LA(3);
						if ( (LA69_8==ID) ) {
							int LA69_11 = input.LA(4);
							if ( (LA69_11==DOT) ) {
								int LA69_16 = input.LA(5);
								if ( (LA69_16==ID) ) {
									int LA69_18 = input.LA(6);
									if ( (synpred39_FTS()) ) {
										alt69=1;
									}
								}
							}
							else if ( (LA69_11==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==TO) ) {
							int LA69_12 = input.LA(4);
							if ( (LA69_12==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==OR) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==AND) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==NOT) ) {
							int LA69_15 = input.LA(4);
							if ( (LA69_15==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
					}
					}
					break;
				case NOT:
					{
					int LA69_5 = input.LA(2);
					if ( (LA69_5==COLON) ) {
						int LA69_8 = input.LA(3);
						if ( (LA69_8==ID) ) {
							int LA69_11 = input.LA(4);
							if ( (LA69_11==DOT) ) {
								int LA69_16 = input.LA(5);
								if ( (LA69_16==ID) ) {
									int LA69_18 = input.LA(6);
									if ( (synpred39_FTS()) ) {
										alt69=1;
									}
								}
							}
							else if ( (LA69_11==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==TO) ) {
							int LA69_12 = input.LA(4);
							if ( (LA69_12==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
						else if ( (LA69_8==OR) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==AND) && (synpred39_FTS())) {
							alt69=1;
						}
						else if ( (LA69_8==NOT) ) {
							int LA69_15 = input.LA(4);
							if ( (LA69_15==COLON) && (synpred39_FTS())) {
								alt69=1;
							}
						}
					}
					}
					break;
				case URI:
					{
					alt69=2;
					}
					break;
			}
			switch (alt69) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:781:19: ( prefix )=> prefix
					{
					pushFollow(FOLLOW_prefix_in_fieldReference6357);
					prefix189=prefix();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_prefix.add(prefix189.getTree());
					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:782:19: uri
					{
					pushFollow(FOLLOW_uri_in_fieldReference6377);
					uri190=uri();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_uri.add(uri190.getTree());
					}
					break;

			}

			pushFollow(FOLLOW_identifier_in_fieldReference6398);
			identifier191=identifier();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identifier.add(identifier191.getTree());
			// AST REWRITE
			// elements: uri, prefix, identifier
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 785:17: -> ^( FIELD_REF identifier ( prefix )? ( uri )? )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:786:25: ^( FIELD_REF identifier ( prefix )? ( uri )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_REF, "FIELD_REF"), root_1);
				adaptor.addChild(root_1, stream_identifier.nextTree());
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:786:48: ( prefix )?
				if ( stream_prefix.hasNext() ) {
					adaptor.addChild(root_1, stream_prefix.nextTree());
				}
				stream_prefix.reset();

				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:786:56: ( uri )?
				if ( stream_uri.hasNext() ) {
					adaptor.addChild(root_1, stream_uri.nextTree());
				}
				stream_uri.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "fieldReference"


	public static class tempReference_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "tempReference"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:789:1: tempReference : ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) ;
	public final FTSParser.tempReference_return tempReference() throws RecognitionException {
		FTSParser.tempReference_return retval = new FTSParser.tempReference_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token AT192=null;
		ParserRuleReturnScope prefix193 =null;
		ParserRuleReturnScope uri194 =null;
		ParserRuleReturnScope identifier195 =null;

		Object AT192_tree=null;
		RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
		RewriteRuleSubtreeStream stream_prefix=new RewriteRuleSubtreeStream(adaptor,"rule prefix");
		RewriteRuleSubtreeStream stream_uri=new RewriteRuleSubtreeStream(adaptor,"rule uri");
		RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:790:9: ( ( AT )? ( prefix | uri )? identifier -> ^( FIELD_REF identifier ( prefix )? ( uri )? ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:791:9: ( AT )? ( prefix | uri )? identifier
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:791:9: ( AT )?
			int alt70=2;
			int LA70_0 = input.LA(1);
			if ( (LA70_0==AT) ) {
				alt70=1;
			}
			switch (alt70) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:791:9: AT
					{
					AT192=(Token)match(input,AT,FOLLOW_AT_in_tempReference6485); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_AT.add(AT192);

					}
					break;

			}

			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:792:9: ( prefix | uri )?
			int alt71=3;
			switch ( input.LA(1) ) {
				case ID:
					{
					int LA71_1 = input.LA(2);
					if ( (LA71_1==DOT) ) {
						int LA71_7 = input.LA(3);
						if ( (LA71_7==ID) ) {
							int LA71_10 = input.LA(4);
							if ( (LA71_10==COLON) ) {
								alt71=1;
							}
						}
					}
					else if ( (LA71_1==COLON) ) {
						alt71=1;
					}
					}
					break;
				case TO:
					{
					int LA71_2 = input.LA(2);
					if ( (LA71_2==COLON) ) {
						alt71=1;
					}
					}
					break;
				case OR:
					{
					int LA71_3 = input.LA(2);
					if ( (LA71_3==COLON) ) {
						alt71=1;
					}
					}
					break;
				case AND:
					{
					int LA71_4 = input.LA(2);
					if ( (LA71_4==COLON) ) {
						alt71=1;
					}
					}
					break;
				case NOT:
					{
					int LA71_5 = input.LA(2);
					if ( (LA71_5==COLON) ) {
						alt71=1;
					}
					}
					break;
				case URI:
					{
					alt71=2;
					}
					break;
			}
			switch (alt71) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:793:17: prefix
					{
					pushFollow(FOLLOW_prefix_in_tempReference6514);
					prefix193=prefix();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_prefix.add(prefix193.getTree());
					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:794:19: uri
					{
					pushFollow(FOLLOW_uri_in_tempReference6534);
					uri194=uri();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_uri.add(uri194.getTree());
					}
					break;

			}

			pushFollow(FOLLOW_identifier_in_tempReference6555);
			identifier195=identifier();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identifier.add(identifier195.getTree());
			// AST REWRITE
			// elements: prefix, identifier, uri
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 797:17: -> ^( FIELD_REF identifier ( prefix )? ( uri )? )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:798:25: ^( FIELD_REF identifier ( prefix )? ( uri )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD_REF, "FIELD_REF"), root_1);
				adaptor.addChild(root_1, stream_identifier.nextTree());
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:798:48: ( prefix )?
				if ( stream_prefix.hasNext() ) {
					adaptor.addChild(root_1, stream_prefix.nextTree());
				}
				stream_prefix.reset();

				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:798:56: ( uri )?
				if ( stream_uri.hasNext() ) {
					adaptor.addChild(root_1, stream_uri.nextTree());
				}
				stream_uri.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "tempReference"


	public static class prefix_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "prefix"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:801:1: prefix : identifier COLON -> ^( PREFIX identifier ) ;
	public final FTSParser.prefix_return prefix() throws RecognitionException {
		FTSParser.prefix_return retval = new FTSParser.prefix_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token COLON197=null;
		ParserRuleReturnScope identifier196 =null;

		Object COLON197_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:802:9: ( identifier COLON -> ^( PREFIX identifier ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:803:9: identifier COLON
			{
			pushFollow(FOLLOW_identifier_in_prefix6642);
			identifier196=identifier();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identifier.add(identifier196.getTree());
			COLON197=(Token)match(input,COLON,FOLLOW_COLON_in_prefix6644); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_COLON.add(COLON197);

			// AST REWRITE
			// elements: identifier
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 804:17: -> ^( PREFIX identifier )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:805:25: ^( PREFIX identifier )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PREFIX, "PREFIX"), root_1);
				adaptor.addChild(root_1, stream_identifier.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "prefix"


	public static class uri_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "uri"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:808:1: uri : URI -> ^( NAME_SPACE URI ) ;
	public final FTSParser.uri_return uri() throws RecognitionException {
		FTSParser.uri_return retval = new FTSParser.uri_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token URI198=null;

		Object URI198_tree=null;
		RewriteRuleTokenStream stream_URI=new RewriteRuleTokenStream(adaptor,"token URI");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:809:9: ( URI -> ^( NAME_SPACE URI ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:810:9: URI
			{
			URI198=(Token)match(input,URI,FOLLOW_URI_in_uri6725); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_URI.add(URI198);

			// AST REWRITE
			// elements: URI
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 811:17: -> ^( NAME_SPACE URI )
			{
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:812:25: ^( NAME_SPACE URI )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NAME_SPACE, "NAME_SPACE"), root_1);
				adaptor.addChild(root_1, stream_URI.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "uri"


	public static class identifier_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "identifier"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:815:1: identifier : ( ( ID DOT ID )=>id1= ID DOT id2= ID ->| ID -> ID | TO -> TO | OR -> OR | AND -> AND | NOT -> NOT );
	public final FTSParser.identifier_return identifier() throws RecognitionException {
		FTSParser.identifier_return retval = new FTSParser.identifier_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token id1=null;
		Token id2=null;
		Token DOT199=null;
		Token ID200=null;
		Token TO201=null;
		Token OR202=null;
		Token AND203=null;
		Token NOT204=null;

		Object id1_tree=null;
		Object id2_tree=null;
		Object DOT199_tree=null;
		Object ID200_tree=null;
		Object TO201_tree=null;
		Object OR202_tree=null;
		Object AND203_tree=null;
		Object NOT204_tree=null;
		RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
		RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
		RewriteRuleTokenStream stream_TO=new RewriteRuleTokenStream(adaptor,"token TO");
		RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:816:9: ( ( ID DOT ID )=>id1= ID DOT id2= ID ->| ID -> ID | TO -> TO | OR -> OR | AND -> AND | NOT -> NOT )
			int alt72=6;
			switch ( input.LA(1) ) {
			case ID:
				{
				int LA72_1 = input.LA(2);
				if ( (LA72_1==DOT) ) {
					int LA72_6 = input.LA(3);
					if ( (LA72_6==ID) ) {
						int LA72_8 = input.LA(4);
						if ( (synpred40_FTS()) ) {
							alt72=1;
						}
						else if ( (true) ) {
							alt72=2;
						}

					}
					else if ( (LA72_6==EOF||(LA72_6 >= AMP && LA72_6 <= BAR)||LA72_6==CARAT||LA72_6==COMMA||LA72_6==DATETIME||LA72_6==DECIMAL_INTEGER_LITERAL||LA72_6==DOT||LA72_6==EQUALS||LA72_6==EXCLAMATION||LA72_6==FLOATING_POINT_LITERAL||(LA72_6 >= FTSPHRASE && LA72_6 <= FTSWORD)||(LA72_6 >= LPAREN && LA72_6 <= LT)||LA72_6==MINUS||LA72_6==NOT||(LA72_6 >= OR && LA72_6 <= PERCENT)||LA72_6==PLUS||LA72_6==QUESTION_MARK||LA72_6==RPAREN||LA72_6==STAR||(LA72_6 >= TILDA && LA72_6 <= TO)||LA72_6==URI) ) {
						alt72=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 72, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA72_1==EOF||(LA72_1 >= AMP && LA72_1 <= BAR)||(LA72_1 >= CARAT && LA72_1 <= COMMA)||LA72_1==DATETIME||LA72_1==DECIMAL_INTEGER_LITERAL||LA72_1==DOTDOT||LA72_1==EQUALS||LA72_1==EXCLAMATION||LA72_1==FLOATING_POINT_LITERAL||(LA72_1 >= FTSPHRASE && LA72_1 <= FTSWORD)||LA72_1==GT||LA72_1==ID||(LA72_1 >= LPAREN && LA72_1 <= LT)||LA72_1==MINUS||LA72_1==NOT||(LA72_1 >= OR && LA72_1 <= PERCENT)||LA72_1==PLUS||LA72_1==QUESTION_MARK||(LA72_1 >= RPAREN && LA72_1 <= RSQUARE)||LA72_1==STAR||(LA72_1 >= TILDA && LA72_1 <= TO)||LA72_1==URI) ) {
					alt72=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 72, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case TO:
				{
				alt72=3;
				}
				break;
			case OR:
				{
				alt72=4;
				}
				break;
			case AND:
				{
				alt72=5;
				}
				break;
			case NOT:
				{
				alt72=6;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 72, 0, input);
				throw nvae;
			}
			switch (alt72) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:817:9: ( ID DOT ID )=>id1= ID DOT id2= ID
					{
					id1=(Token)match(input,ID,FOLLOW_ID_in_identifier6827); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_ID.add(id1);

					DOT199=(Token)match(input,DOT,FOLLOW_DOT_in_identifier6829); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DOT.add(DOT199);

					id2=(Token)match(input,ID,FOLLOW_ID_in_identifier6833); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_ID.add(id2);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 819:17: ->
					{
						adaptor.addChild(root_0, new CommonTree(new CommonToken(FTSLexer.ID, (id1!=null?id1.getText():null)+(DOT199!=null?DOT199.getText():null)+(id2!=null?id2.getText():null))));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:821:12: ID
					{
					ID200=(Token)match(input,ID,FOLLOW_ID_in_identifier6882); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_ID.add(ID200);

					// AST REWRITE
					// elements: ID
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 822:17: -> ID
					{
						adaptor.addChild(root_0, stream_ID.nextNode());
					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:825:12: TO
					{
					TO201=(Token)match(input,TO,FOLLOW_TO_in_identifier6949); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_TO.add(TO201);

					// AST REWRITE
					// elements: TO
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 826:17: -> TO
					{
						adaptor.addChild(root_0, stream_TO.nextNode());
					}


					retval.tree = root_0;
					}

					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:827:12: OR
					{
					OR202=(Token)match(input,OR,FOLLOW_OR_in_identifier6987); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_OR.add(OR202);

					// AST REWRITE
					// elements: OR
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 828:17: -> OR
					{
						adaptor.addChild(root_0, stream_OR.nextNode());
					}


					retval.tree = root_0;
					}

					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:829:12: AND
					{
					AND203=(Token)match(input,AND,FOLLOW_AND_in_identifier7025); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_AND.add(AND203);

					// AST REWRITE
					// elements: AND
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 830:17: -> AND
					{
						adaptor.addChild(root_0, stream_AND.nextNode());
					}


					retval.tree = root_0;
					}

					}
					break;
				case 6 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:831:12: NOT
					{
					NOT204=(Token)match(input,NOT,FOLLOW_NOT_in_identifier7064); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_NOT.add(NOT204);

					// AST REWRITE
					// elements: NOT
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 832:17: -> NOT
					{
						adaptor.addChild(root_0, stream_NOT.nextNode());
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "identifier"


	public static class ftsWord_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsWord"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:835:1: ftsWord : ( ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase DOT | COMMA ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) | ( DOT | COMMA ) ftsWordBase | ftsWordBase );
	public final FTSParser.ftsWord_return ftsWord() throws RecognitionException {
		FTSParser.ftsWord_return retval = new FTSParser.ftsWord_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set205=null;
		Token set207=null;
		Token set209=null;
		Token set211=null;
		Token set213=null;
		Token set216=null;
		Token set218=null;
		Token set220=null;
		Token set222=null;
		Token set224=null;
		Token set226=null;
		Token set228=null;
		Token set230=null;
		Token set232=null;
		Token set234=null;
		Token set236=null;
		Token set238=null;
		Token set240=null;
		Token set241=null;
		Token set243=null;
		Token set245=null;
		Token set247=null;
		Token set250=null;
		Token set252=null;
		Token set254=null;
		Token set256=null;
		Token set258=null;
		Token set260=null;
		Token set262=null;
		Token set264=null;
		Token set266=null;
		Token set268=null;
		Token set269=null;
		Token set271=null;
		Token set273=null;
		Token set276=null;
		Token set278=null;
		Token set280=null;
		Token set282=null;
		Token set284=null;
		Token set286=null;
		Token set288=null;
		Token set289=null;
		Token set291=null;
		Token set294=null;
		Token set296=null;
		Token set298=null;
		Token set300=null;
		Token set301=null;
		ParserRuleReturnScope ftsWordBase206 =null;
		ParserRuleReturnScope ftsWordBase208 =null;
		ParserRuleReturnScope ftsWordBase210 =null;
		ParserRuleReturnScope ftsWordBase212 =null;
		ParserRuleReturnScope ftsWordBase214 =null;
		ParserRuleReturnScope ftsWordBase215 =null;
		ParserRuleReturnScope ftsWordBase217 =null;
		ParserRuleReturnScope ftsWordBase219 =null;
		ParserRuleReturnScope ftsWordBase221 =null;
		ParserRuleReturnScope ftsWordBase223 =null;
		ParserRuleReturnScope ftsWordBase225 =null;
		ParserRuleReturnScope ftsWordBase227 =null;
		ParserRuleReturnScope ftsWordBase229 =null;
		ParserRuleReturnScope ftsWordBase231 =null;
		ParserRuleReturnScope ftsWordBase233 =null;
		ParserRuleReturnScope ftsWordBase235 =null;
		ParserRuleReturnScope ftsWordBase237 =null;
		ParserRuleReturnScope ftsWordBase239 =null;
		ParserRuleReturnScope ftsWordBase242 =null;
		ParserRuleReturnScope ftsWordBase244 =null;
		ParserRuleReturnScope ftsWordBase246 =null;
		ParserRuleReturnScope ftsWordBase248 =null;
		ParserRuleReturnScope ftsWordBase249 =null;
		ParserRuleReturnScope ftsWordBase251 =null;
		ParserRuleReturnScope ftsWordBase253 =null;
		ParserRuleReturnScope ftsWordBase255 =null;
		ParserRuleReturnScope ftsWordBase257 =null;
		ParserRuleReturnScope ftsWordBase259 =null;
		ParserRuleReturnScope ftsWordBase261 =null;
		ParserRuleReturnScope ftsWordBase263 =null;
		ParserRuleReturnScope ftsWordBase265 =null;
		ParserRuleReturnScope ftsWordBase267 =null;
		ParserRuleReturnScope ftsWordBase270 =null;
		ParserRuleReturnScope ftsWordBase272 =null;
		ParserRuleReturnScope ftsWordBase274 =null;
		ParserRuleReturnScope ftsWordBase275 =null;
		ParserRuleReturnScope ftsWordBase277 =null;
		ParserRuleReturnScope ftsWordBase279 =null;
		ParserRuleReturnScope ftsWordBase281 =null;
		ParserRuleReturnScope ftsWordBase283 =null;
		ParserRuleReturnScope ftsWordBase285 =null;
		ParserRuleReturnScope ftsWordBase287 =null;
		ParserRuleReturnScope ftsWordBase290 =null;
		ParserRuleReturnScope ftsWordBase292 =null;
		ParserRuleReturnScope ftsWordBase293 =null;
		ParserRuleReturnScope ftsWordBase295 =null;
		ParserRuleReturnScope ftsWordBase297 =null;
		ParserRuleReturnScope ftsWordBase299 =null;
		ParserRuleReturnScope ftsWordBase302 =null;
		ParserRuleReturnScope ftsWordBase303 =null;

		Object set205_tree=null;
		Object set207_tree=null;
		Object set209_tree=null;
		Object set211_tree=null;
		Object set213_tree=null;
		Object set216_tree=null;
		Object set218_tree=null;
		Object set220_tree=null;
		Object set222_tree=null;
		Object set224_tree=null;
		Object set226_tree=null;
		Object set228_tree=null;
		Object set230_tree=null;
		Object set232_tree=null;
		Object set234_tree=null;
		Object set236_tree=null;
		Object set238_tree=null;
		Object set240_tree=null;
		Object set241_tree=null;
		Object set243_tree=null;
		Object set245_tree=null;
		Object set247_tree=null;
		Object set250_tree=null;
		Object set252_tree=null;
		Object set254_tree=null;
		Object set256_tree=null;
		Object set258_tree=null;
		Object set260_tree=null;
		Object set262_tree=null;
		Object set264_tree=null;
		Object set266_tree=null;
		Object set268_tree=null;
		Object set269_tree=null;
		Object set271_tree=null;
		Object set273_tree=null;
		Object set276_tree=null;
		Object set278_tree=null;
		Object set280_tree=null;
		Object set282_tree=null;
		Object set284_tree=null;
		Object set286_tree=null;
		Object set288_tree=null;
		Object set289_tree=null;
		Object set291_tree=null;
		Object set294_tree=null;
		Object set296_tree=null;
		Object set298_tree=null;
		Object set300_tree=null;
		Object set301_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:836:9: ( ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase DOT | COMMA ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) | ( DOT | COMMA ) ftsWordBase | ftsWordBase )
			int alt73=18;
			alt73 = dfa73.predict(input);
			switch (alt73) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:837:12: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					set205=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set205));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7188);
					ftsWordBase206=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase206.getTree());

					set207=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set207));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7196);
					ftsWordBase208=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase208.getTree());

					set209=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set209));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7204);
					ftsWordBase210=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase210.getTree());

					set211=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set211));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7212);
					ftsWordBase212=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase212.getTree());

					set213=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set213));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7220);
					ftsWordBase214=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase214.getTree());

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:839:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase DOT | COMMA ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7280);
					ftsWordBase215=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase215.getTree());

					set216=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set216));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7288);
					ftsWordBase217=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase217.getTree());

					set218=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set218));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7296);
					ftsWordBase219=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase219.getTree());

					set220=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set220));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7304);
					ftsWordBase221=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase221.getTree());

					set222=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set222));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7312);
					ftsWordBase223=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase223.getTree());

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:841:12: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
					{
					root_0 = (Object)adaptor.nil();


					set224=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set224));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7385);
					ftsWordBase225=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase225.getTree());

					set226=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set226));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7393);
					ftsWordBase227=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase227.getTree());

					set228=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set228));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7401);
					ftsWordBase229=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase229.getTree());

					set230=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set230));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7409);
					ftsWordBase231=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase231.getTree());

					set232=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set232));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:843:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7475);
					ftsWordBase233=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase233.getTree());

					set234=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set234));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7483);
					ftsWordBase235=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase235.getTree());

					set236=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set236));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7491);
					ftsWordBase237=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase237.getTree());

					set238=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set238));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7499);
					ftsWordBase239=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase239.getTree());

					set240=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set240));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:845:12: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					set241=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set241));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7572);
					ftsWordBase242=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase242.getTree());

					set243=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set243));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7580);
					ftsWordBase244=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase244.getTree());

					set245=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set245));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7588);
					ftsWordBase246=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase246.getTree());

					set247=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set247));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7596);
					ftsWordBase248=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase248.getTree());

					}
					break;
				case 6 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:847:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7650);
					ftsWordBase249=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase249.getTree());

					set250=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set250));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7658);
					ftsWordBase251=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase251.getTree());

					set252=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set252));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7666);
					ftsWordBase253=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase253.getTree());

					set254=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set254));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7674);
					ftsWordBase255=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase255.getTree());

					}
					break;
				case 7 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:849:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
					{
					root_0 = (Object)adaptor.nil();


					set256=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set256));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7738);
					ftsWordBase257=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase257.getTree());

					set258=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set258));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7746);
					ftsWordBase259=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase259.getTree());

					set260=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set260));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7754);
					ftsWordBase261=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase261.getTree());

					set262=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set262));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 8 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:851:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7812);
					ftsWordBase263=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase263.getTree());

					set264=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set264));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7820);
					ftsWordBase265=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase265.getTree());

					set266=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set266));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7828);
					ftsWordBase267=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase267.getTree());

					set268=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set268));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 9 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:853:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					set269=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set269));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7892);
					ftsWordBase270=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase270.getTree());

					set271=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set271));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7900);
					ftsWordBase272=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase272.getTree());

					set273=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set273));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7908);
					ftsWordBase274=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase274.getTree());

					}
					break;
				case 10 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:855:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7954);
					ftsWordBase275=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase275.getTree());

					set276=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set276));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7962);
					ftsWordBase277=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase277.getTree());

					set278=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set278));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord7970);
					ftsWordBase279=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase279.getTree());

					}
					break;
				case 11 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:857:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
					{
					root_0 = (Object)adaptor.nil();


					set280=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set280));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8027);
					ftsWordBase281=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase281.getTree());

					set282=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set282));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8035);
					ftsWordBase283=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase283.getTree());

					set284=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set284));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 12 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:859:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8086);
					ftsWordBase285=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase285.getTree());

					set286=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set286));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8094);
					ftsWordBase287=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase287.getTree());

					set288=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set288));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 13 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:861:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					set289=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set289));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8151);
					ftsWordBase290=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase290.getTree());

					set291=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set291));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8159);
					ftsWordBase292=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase292.getTree());

					}
					break;
				case 14 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:863:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8197);
					ftsWordBase293=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase293.getTree());

					set294=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set294));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8205);
					ftsWordBase295=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase295.getTree());

					}
					break;
				case 15 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:865:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
					{
					root_0 = (Object)adaptor.nil();


					set296=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set296));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8253);
					ftsWordBase297=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase297.getTree());

					set298=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set298));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 16 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:867:11: ( ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8295);
					ftsWordBase299=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase299.getTree());

					set300=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set300));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;
				case 17 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:869:11: ( DOT | COMMA ) ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					set301=input.LT(1);
					if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set301));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8319);
					ftsWordBase302=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase302.getTree());

					}
					break;
				case 18 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:870:11: ftsWordBase
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ftsWordBase_in_ftsWord8332);
					ftsWordBase303=ftsWordBase();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ftsWordBase303.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsWord"


	public static class ftsWordBase_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsWordBase"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:874:1: ftsWordBase : ( ID | FTSWORD | FTSPRE | FTSWILD | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | STAR | QUESTION_MARK | DATETIME | URI identifier );
	public final FTSParser.ftsWordBase_return ftsWordBase() throws RecognitionException {
		FTSParser.ftsWordBase_return retval = new FTSParser.ftsWordBase_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token ID304=null;
		Token FTSWORD305=null;
		Token FTSPRE306=null;
		Token FTSWILD307=null;
		Token NOT308=null;
		Token TO309=null;
		Token DECIMAL_INTEGER_LITERAL310=null;
		Token FLOATING_POINT_LITERAL311=null;
		Token STAR312=null;
		Token QUESTION_MARK313=null;
		Token DATETIME314=null;
		Token URI315=null;
		ParserRuleReturnScope identifier316 =null;

		Object ID304_tree=null;
		Object FTSWORD305_tree=null;
		Object FTSPRE306_tree=null;
		Object FTSWILD307_tree=null;
		Object NOT308_tree=null;
		Object TO309_tree=null;
		Object DECIMAL_INTEGER_LITERAL310_tree=null;
		Object FLOATING_POINT_LITERAL311_tree=null;
		Object STAR312_tree=null;
		Object QUESTION_MARK313_tree=null;
		Object DATETIME314_tree=null;
		Object URI315_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:875:9: ( ID | FTSWORD | FTSPRE | FTSWILD | NOT | TO | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | STAR | QUESTION_MARK | DATETIME | URI identifier )
			int alt74=12;
			switch ( input.LA(1) ) {
			case ID:
				{
				alt74=1;
				}
				break;
			case FTSWORD:
				{
				alt74=2;
				}
				break;
			case FTSPRE:
				{
				alt74=3;
				}
				break;
			case FTSWILD:
				{
				alt74=4;
				}
				break;
			case NOT:
				{
				alt74=5;
				}
				break;
			case TO:
				{
				alt74=6;
				}
				break;
			case DECIMAL_INTEGER_LITERAL:
				{
				alt74=7;
				}
				break;
			case FLOATING_POINT_LITERAL:
				{
				alt74=8;
				}
				break;
			case STAR:
				{
				alt74=9;
				}
				break;
			case QUESTION_MARK:
				{
				alt74=10;
				}
				break;
			case DATETIME:
				{
				alt74=11;
				}
				break;
			case URI:
				{
				alt74=12;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 74, 0, input);
				throw nvae;
			}
			switch (alt74) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:876:11: ID
					{
					root_0 = (Object)adaptor.nil();


					ID304=(Token)match(input,ID,FOLLOW_ID_in_ftsWordBase8377); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					ID304_tree = (Object)adaptor.create(ID304);
					adaptor.addChild(root_0, ID304_tree);
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:877:11: FTSWORD
					{
					root_0 = (Object)adaptor.nil();


					FTSWORD305=(Token)match(input,FTSWORD,FOLLOW_FTSWORD_in_ftsWordBase8389); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSWORD305_tree = (Object)adaptor.create(FTSWORD305);
					adaptor.addChild(root_0, FTSWORD305_tree);
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:878:11: FTSPRE
					{
					root_0 = (Object)adaptor.nil();


					FTSPRE306=(Token)match(input,FTSPRE,FOLLOW_FTSPRE_in_ftsWordBase8401); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSPRE306_tree = (Object)adaptor.create(FTSPRE306);
					adaptor.addChild(root_0, FTSPRE306_tree);
					}

					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:879:11: FTSWILD
					{
					root_0 = (Object)adaptor.nil();


					FTSWILD307=(Token)match(input,FTSWILD,FOLLOW_FTSWILD_in_ftsWordBase8414); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSWILD307_tree = (Object)adaptor.create(FTSWILD307);
					adaptor.addChild(root_0, FTSWILD307_tree);
					}

					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:880:11: NOT
					{
					root_0 = (Object)adaptor.nil();


					NOT308=(Token)match(input,NOT,FOLLOW_NOT_in_ftsWordBase8427); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					NOT308_tree = (Object)adaptor.create(NOT308);
					adaptor.addChild(root_0, NOT308_tree);
					}

					}
					break;
				case 6 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:881:11: TO
					{
					root_0 = (Object)adaptor.nil();


					TO309=(Token)match(input,TO,FOLLOW_TO_in_ftsWordBase8439); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					TO309_tree = (Object)adaptor.create(TO309);
					adaptor.addChild(root_0, TO309_tree);
					}

					}
					break;
				case 7 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:882:11: DECIMAL_INTEGER_LITERAL
					{
					root_0 = (Object)adaptor.nil();


					DECIMAL_INTEGER_LITERAL310=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_ftsWordBase8451); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					DECIMAL_INTEGER_LITERAL310_tree = (Object)adaptor.create(DECIMAL_INTEGER_LITERAL310);
					adaptor.addChild(root_0, DECIMAL_INTEGER_LITERAL310_tree);
					}

					}
					break;
				case 8 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:883:11: FLOATING_POINT_LITERAL
					{
					root_0 = (Object)adaptor.nil();


					FLOATING_POINT_LITERAL311=(Token)match(input,FLOATING_POINT_LITERAL,FOLLOW_FLOATING_POINT_LITERAL_in_ftsWordBase8463); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FLOATING_POINT_LITERAL311_tree = (Object)adaptor.create(FLOATING_POINT_LITERAL311);
					adaptor.addChild(root_0, FLOATING_POINT_LITERAL311_tree);
					}

					}
					break;
				case 9 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:884:11: STAR
					{
					root_0 = (Object)adaptor.nil();


					STAR312=(Token)match(input,STAR,FOLLOW_STAR_in_ftsWordBase8475); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					STAR312_tree = (Object)adaptor.create(STAR312);
					adaptor.addChild(root_0, STAR312_tree);
					}

					}
					break;
				case 10 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:885:11: QUESTION_MARK
					{
					root_0 = (Object)adaptor.nil();


					QUESTION_MARK313=(Token)match(input,QUESTION_MARK,FOLLOW_QUESTION_MARK_in_ftsWordBase8487); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					QUESTION_MARK313_tree = (Object)adaptor.create(QUESTION_MARK313);
					adaptor.addChild(root_0, QUESTION_MARK313_tree);
					}

					}
					break;
				case 11 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:886:11: DATETIME
					{
					root_0 = (Object)adaptor.nil();


					DATETIME314=(Token)match(input,DATETIME,FOLLOW_DATETIME_in_ftsWordBase8499); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					DATETIME314_tree = (Object)adaptor.create(DATETIME314);
					adaptor.addChild(root_0, DATETIME314_tree);
					}

					}
					break;
				case 12 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:887:11: URI identifier
					{
					root_0 = (Object)adaptor.nil();


					URI315=(Token)match(input,URI,FOLLOW_URI_in_ftsWordBase8511); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					URI315_tree = (Object)adaptor.create(URI315);
					adaptor.addChild(root_0, URI315_tree);
					}

					pushFollow(FOLLOW_identifier_in_ftsWordBase8513);
					identifier316=identifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier316.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsWordBase"


	public static class number_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "number"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:890:1: number : ( DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL );
	public final FTSParser.number_return number() throws RecognitionException {
		FTSParser.number_return retval = new FTSParser.number_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set317=null;

		Object set317_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:891:9: ( DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
			{
			root_0 = (Object)adaptor.nil();


			set317=input.LT(1);
			if ( input.LA(1)==DECIMAL_INTEGER_LITERAL||input.LA(1)==FLOATING_POINT_LITERAL ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set317));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "number"


	public static class ftsRangeWord_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ftsRangeWord"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:896:1: ftsRangeWord : ( ID | FTSWORD | FTSPRE | FTSWILD | FTSPHRASE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | DATETIME | STAR | URI identifier );
	public final FTSParser.ftsRangeWord_return ftsRangeWord() throws RecognitionException {
		FTSParser.ftsRangeWord_return retval = new FTSParser.ftsRangeWord_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token ID318=null;
		Token FTSWORD319=null;
		Token FTSPRE320=null;
		Token FTSWILD321=null;
		Token FTSPHRASE322=null;
		Token DECIMAL_INTEGER_LITERAL323=null;
		Token FLOATING_POINT_LITERAL324=null;
		Token DATETIME325=null;
		Token STAR326=null;
		Token URI327=null;
		ParserRuleReturnScope identifier328 =null;

		Object ID318_tree=null;
		Object FTSWORD319_tree=null;
		Object FTSPRE320_tree=null;
		Object FTSWILD321_tree=null;
		Object FTSPHRASE322_tree=null;
		Object DECIMAL_INTEGER_LITERAL323_tree=null;
		Object FLOATING_POINT_LITERAL324_tree=null;
		Object DATETIME325_tree=null;
		Object STAR326_tree=null;
		Object URI327_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:897:9: ( ID | FTSWORD | FTSPRE | FTSWILD | FTSPHRASE | DECIMAL_INTEGER_LITERAL | FLOATING_POINT_LITERAL | DATETIME | STAR | URI identifier )
			int alt75=10;
			switch ( input.LA(1) ) {
			case ID:
				{
				alt75=1;
				}
				break;
			case FTSWORD:
				{
				alt75=2;
				}
				break;
			case FTSPRE:
				{
				alt75=3;
				}
				break;
			case FTSWILD:
				{
				alt75=4;
				}
				break;
			case FTSPHRASE:
				{
				alt75=5;
				}
				break;
			case DECIMAL_INTEGER_LITERAL:
				{
				alt75=6;
				}
				break;
			case FLOATING_POINT_LITERAL:
				{
				alt75=7;
				}
				break;
			case DATETIME:
				{
				alt75=8;
				}
				break;
			case STAR:
				{
				alt75=9;
				}
				break;
			case URI:
				{
				alt75=10;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 75, 0, input);
				throw nvae;
			}
			switch (alt75) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:898:11: ID
					{
					root_0 = (Object)adaptor.nil();


					ID318=(Token)match(input,ID,FOLLOW_ID_in_ftsRangeWord8593); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					ID318_tree = (Object)adaptor.create(ID318);
					adaptor.addChild(root_0, ID318_tree);
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:899:11: FTSWORD
					{
					root_0 = (Object)adaptor.nil();


					FTSWORD319=(Token)match(input,FTSWORD,FOLLOW_FTSWORD_in_ftsRangeWord8605); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSWORD319_tree = (Object)adaptor.create(FTSWORD319);
					adaptor.addChild(root_0, FTSWORD319_tree);
					}

					}
					break;
				case 3 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:900:11: FTSPRE
					{
					root_0 = (Object)adaptor.nil();


					FTSPRE320=(Token)match(input,FTSPRE,FOLLOW_FTSPRE_in_ftsRangeWord8617); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSPRE320_tree = (Object)adaptor.create(FTSPRE320);
					adaptor.addChild(root_0, FTSPRE320_tree);
					}

					}
					break;
				case 4 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:901:11: FTSWILD
					{
					root_0 = (Object)adaptor.nil();


					FTSWILD321=(Token)match(input,FTSWILD,FOLLOW_FTSWILD_in_ftsRangeWord8629); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSWILD321_tree = (Object)adaptor.create(FTSWILD321);
					adaptor.addChild(root_0, FTSWILD321_tree);
					}

					}
					break;
				case 5 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:902:11: FTSPHRASE
					{
					root_0 = (Object)adaptor.nil();


					FTSPHRASE322=(Token)match(input,FTSPHRASE,FOLLOW_FTSPHRASE_in_ftsRangeWord8641); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FTSPHRASE322_tree = (Object)adaptor.create(FTSPHRASE322);
					adaptor.addChild(root_0, FTSPHRASE322_tree);
					}

					}
					break;
				case 6 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:903:11: DECIMAL_INTEGER_LITERAL
					{
					root_0 = (Object)adaptor.nil();


					DECIMAL_INTEGER_LITERAL323=(Token)match(input,DECIMAL_INTEGER_LITERAL,FOLLOW_DECIMAL_INTEGER_LITERAL_in_ftsRangeWord8653); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					DECIMAL_INTEGER_LITERAL323_tree = (Object)adaptor.create(DECIMAL_INTEGER_LITERAL323);
					adaptor.addChild(root_0, DECIMAL_INTEGER_LITERAL323_tree);
					}

					}
					break;
				case 7 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:904:11: FLOATING_POINT_LITERAL
					{
					root_0 = (Object)adaptor.nil();


					FLOATING_POINT_LITERAL324=(Token)match(input,FLOATING_POINT_LITERAL,FOLLOW_FLOATING_POINT_LITERAL_in_ftsRangeWord8665); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FLOATING_POINT_LITERAL324_tree = (Object)adaptor.create(FLOATING_POINT_LITERAL324);
					adaptor.addChild(root_0, FLOATING_POINT_LITERAL324_tree);
					}

					}
					break;
				case 8 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:905:11: DATETIME
					{
					root_0 = (Object)adaptor.nil();


					DATETIME325=(Token)match(input,DATETIME,FOLLOW_DATETIME_in_ftsRangeWord8677); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					DATETIME325_tree = (Object)adaptor.create(DATETIME325);
					adaptor.addChild(root_0, DATETIME325_tree);
					}

					}
					break;
				case 9 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:906:11: STAR
					{
					root_0 = (Object)adaptor.nil();


					STAR326=(Token)match(input,STAR,FOLLOW_STAR_in_ftsRangeWord8689); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					STAR326_tree = (Object)adaptor.create(STAR326);
					adaptor.addChild(root_0, STAR326_tree);
					}

					}
					break;
				case 10 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:907:11: URI identifier
					{
					root_0 = (Object)adaptor.nil();


					URI327=(Token)match(input,URI,FOLLOW_URI_in_ftsRangeWord8701); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					URI327_tree = (Object)adaptor.create(URI327);
					adaptor.addChild(root_0, URI327_tree);
					}

					pushFollow(FOLLOW_identifier_in_ftsRangeWord8703);
					identifier328=identifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier328.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ftsRangeWord"


	public static class or_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "or"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:912:1: or : ( OR | BAR BAR );
	public final FTSParser.or_return or() throws RecognitionException {
		FTSParser.or_return retval = new FTSParser.or_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token OR329=null;
		Token BAR330=null;
		Token BAR331=null;

		Object OR329_tree=null;
		Object BAR330_tree=null;
		Object BAR331_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:913:9: ( OR | BAR BAR )
			int alt76=2;
			int LA76_0 = input.LA(1);
			if ( (LA76_0==OR) ) {
				alt76=1;
			}
			else if ( (LA76_0==BAR) ) {
				alt76=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 76, 0, input);
				throw nvae;
			}

			switch (alt76) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:914:9: OR
					{
					root_0 = (Object)adaptor.nil();


					OR329=(Token)match(input,OR,FOLLOW_OR_in_or8738); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					OR329_tree = (Object)adaptor.create(OR329);
					adaptor.addChild(root_0, OR329_tree);
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:915:11: BAR BAR
					{
					root_0 = (Object)adaptor.nil();


					BAR330=(Token)match(input,BAR,FOLLOW_BAR_in_or8750); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					BAR330_tree = (Object)adaptor.create(BAR330);
					adaptor.addChild(root_0, BAR330_tree);
					}

					BAR331=(Token)match(input,BAR,FOLLOW_BAR_in_or8752); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					BAR331_tree = (Object)adaptor.create(BAR331);
					adaptor.addChild(root_0, BAR331_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "or"


	public static class and_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "and"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:918:1: and : ( AND | AMP AMP );
	public final FTSParser.and_return and() throws RecognitionException {
		FTSParser.and_return retval = new FTSParser.and_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token AND332=null;
		Token AMP333=null;
		Token AMP334=null;

		Object AND332_tree=null;
		Object AMP333_tree=null;
		Object AMP334_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:919:9: ( AND | AMP AMP )
			int alt77=2;
			int LA77_0 = input.LA(1);
			if ( (LA77_0==AND) ) {
				alt77=1;
			}
			else if ( (LA77_0==AMP) ) {
				alt77=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 77, 0, input);
				throw nvae;
			}

			switch (alt77) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:920:9: AND
					{
					root_0 = (Object)adaptor.nil();


					AND332=(Token)match(input,AND,FOLLOW_AND_in_and8785); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					AND332_tree = (Object)adaptor.create(AND332);
					adaptor.addChild(root_0, AND332_tree);
					}

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:921:11: AMP AMP
					{
					root_0 = (Object)adaptor.nil();


					AMP333=(Token)match(input,AMP,FOLLOW_AMP_in_and8797); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					AMP333_tree = (Object)adaptor.create(AMP333);
					adaptor.addChild(root_0, AMP333_tree);
					}

					AMP334=(Token)match(input,AMP,FOLLOW_AMP_in_and8799); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					AMP334_tree = (Object)adaptor.create(AMP334);
					adaptor.addChild(root_0, AMP334_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "and"


	public static class not_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "not"
	// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:924:1: not : ( NOT | EXCLAMATION );
	public final FTSParser.not_return not() throws RecognitionException {
		FTSParser.not_return retval = new FTSParser.not_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set335=null;

		Object set335_tree=null;

		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:925:9: ( NOT | EXCLAMATION )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:
			{
			root_0 = (Object)adaptor.nil();


			set335=input.LT(1);
			if ( input.LA(1)==EXCLAMATION||input.LA(1)==NOT ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set335));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		catch(RecognitionException e)
		{
		   throw e;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "not"

	// $ANTLR start synpred1_FTS
	public final void synpred1_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:415:9: ( not )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:415:10: not
		{
		pushFollow(FOLLOW_not_in_synpred1_FTS1233);
		not();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred1_FTS

	// $ANTLR start synpred2_FTS
	public final void synpred2_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:447:12: ( ftsFieldGroupProximity )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:447:13: ftsFieldGroupProximity
		{
		pushFollow(FOLLOW_ftsFieldGroupProximity_in_synpred2_FTS1746);
		ftsFieldGroupProximity();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred2_FTS

	// $ANTLR start synpred3_FTS
	public final void synpred3_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:451:12: ( ftsRange )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:451:13: ftsRange
		{
		pushFollow(FOLLOW_ftsRange_in_synpred3_FTS1823);
		ftsRange();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred3_FTS

	// $ANTLR start synpred4_FTS
	public final void synpred4_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:455:12: ( ftsFieldGroup )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:455:13: ftsFieldGroup
		{
		pushFollow(FOLLOW_ftsFieldGroup_in_synpred4_FTS1902);
		ftsFieldGroup();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred4_FTS

	// $ANTLR start synpred5_FTS
	public final void synpred5_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:458:12: ( ftsTermOrPhrase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:458:13: ftsTermOrPhrase
		{
		pushFollow(FOLLOW_ftsTermOrPhrase_in_synpred5_FTS1951);
		ftsTermOrPhrase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred5_FTS

	// $ANTLR start synpred6_FTS
	public final void synpred6_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:460:12: ( ftsExactTermOrPhrase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:460:13: ftsExactTermOrPhrase
		{
		pushFollow(FOLLOW_ftsExactTermOrPhrase_in_synpred6_FTS1980);
		ftsExactTermOrPhrase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred6_FTS

	// $ANTLR start synpred7_FTS
	public final void synpred7_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:462:12: ( ftsTokenisedTermOrPhrase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:462:13: ftsTokenisedTermOrPhrase
		{
		pushFollow(FOLLOW_ftsTokenisedTermOrPhrase_in_synpred7_FTS2010);
		ftsTokenisedTermOrPhrase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred7_FTS

	// $ANTLR start synpred8_FTS
	public final void synpred8_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:514:9: ( fieldReference COLON )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:514:10: fieldReference COLON
		{
		pushFollow(FOLLOW_fieldReference_in_synpred8_FTS2683);
		fieldReference();
		state._fsp--;
		if (state.failed) return;

		match(input,COLON,FOLLOW_COLON_in_synpred8_FTS2685); if (state.failed) return;

		}

	}
	// $ANTLR end synpred8_FTS

	// $ANTLR start synpred9_FTS
	public final void synpred9_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:516:28: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:516:29: slop
		{
		pushFollow(FOLLOW_slop_in_synpred9_FTS2724);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred9_FTS

	// $ANTLR start synpred10_FTS
	public final void synpred10_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:26: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:519:27: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred10_FTS2799);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred10_FTS

	// $ANTLR start synpred11_FTS
	public final void synpred11_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:20: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:523:21: slop
		{
		pushFollow(FOLLOW_slop_in_synpred11_FTS2869);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred11_FTS

	// $ANTLR start synpred12_FTS
	public final void synpred12_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:526:18: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:526:19: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred12_FTS2927);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred12_FTS

	// $ANTLR start synpred13_FTS
	public final void synpred13_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:535:9: ( fieldReference COLON )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:535:10: fieldReference COLON
		{
		pushFollow(FOLLOW_fieldReference_in_synpred13_FTS3032);
		fieldReference();
		state._fsp--;
		if (state.failed) return;

		match(input,COLON,FOLLOW_COLON_in_synpred13_FTS3034); if (state.failed) return;

		}

	}
	// $ANTLR end synpred13_FTS

	// $ANTLR start synpred14_FTS
	public final void synpred14_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:537:28: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:537:29: slop
		{
		pushFollow(FOLLOW_slop_in_synpred14_FTS3073);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred14_FTS

	// $ANTLR start synpred15_FTS
	public final void synpred15_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:540:26: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:540:27: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred15_FTS3148);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred15_FTS

	// $ANTLR start synpred16_FTS
	public final void synpred16_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:544:20: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:544:21: slop
		{
		pushFollow(FOLLOW_slop_in_synpred16_FTS3218);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred16_FTS

	// $ANTLR start synpred17_FTS
	public final void synpred17_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:547:18: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:547:19: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred17_FTS3276);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred17_FTS

	// $ANTLR start synpred18_FTS
	public final void synpred18_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:557:9: ( fieldReference COLON )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:557:10: fieldReference COLON
		{
		pushFollow(FOLLOW_fieldReference_in_synpred18_FTS3383);
		fieldReference();
		state._fsp--;
		if (state.failed) return;

		match(input,COLON,FOLLOW_COLON_in_synpred18_FTS3385); if (state.failed) return;

		}

	}
	// $ANTLR end synpred18_FTS

	// $ANTLR start synpred19_FTS
	public final void synpred19_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:559:28: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:559:29: slop
		{
		pushFollow(FOLLOW_slop_in_synpred19_FTS3424);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred19_FTS

	// $ANTLR start synpred20_FTS
	public final void synpred20_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:562:26: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:562:27: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred20_FTS3499);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred20_FTS

	// $ANTLR start synpred21_FTS
	public final void synpred21_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:566:20: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:566:21: slop
		{
		pushFollow(FOLLOW_slop_in_synpred21_FTS3569);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred21_FTS

	// $ANTLR start synpred22_FTS
	public final void synpred22_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:569:18: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:569:19: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred22_FTS3627);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred22_FTS

	// $ANTLR start synpred23_FTS
	public final void synpred23_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:642:9: ( not )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:642:10: not
		{
		pushFollow(FOLLOW_not_in_synpred23_FTS4374);
		not();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred23_FTS

	// $ANTLR start synpred24_FTS
	public final void synpred24_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:661:9: ( ftsFieldGroupProximity )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:661:10: ftsFieldGroupProximity
		{
		pushFollow(FOLLOW_ftsFieldGroupProximity_in_synpred24_FTS4739);
		ftsFieldGroupProximity();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred24_FTS

	// $ANTLR start synpred25_FTS
	public final void synpred25_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:664:11: ( ftsFieldGroupTerm )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:664:12: ftsFieldGroupTerm
		{
		pushFollow(FOLLOW_ftsFieldGroupTerm_in_synpred25_FTS4805);
		ftsFieldGroupTerm();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred25_FTS

	// $ANTLR start synpred26_FTS
	public final void synpred26_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:664:54: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:664:55: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred26_FTS4815);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred26_FTS

	// $ANTLR start synpred27_FTS
	public final void synpred27_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:667:11: ( ftsFieldGroupExactTerm )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:667:12: ftsFieldGroupExactTerm
		{
		pushFollow(FOLLOW_ftsFieldGroupExactTerm_in_synpred27_FTS4886);
		ftsFieldGroupExactTerm();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred27_FTS

	// $ANTLR start synpred28_FTS
	public final void synpred28_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:667:64: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:667:65: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred28_FTS4896);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred28_FTS

	// $ANTLR start synpred29_FTS
	public final void synpred29_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:670:11: ( ftsFieldGroupPhrase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:670:12: ftsFieldGroupPhrase
		{
		pushFollow(FOLLOW_ftsFieldGroupPhrase_in_synpred29_FTS4967);
		ftsFieldGroupPhrase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred29_FTS

	// $ANTLR start synpred30_FTS
	public final void synpred30_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:670:58: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:670:59: slop
		{
		pushFollow(FOLLOW_slop_in_synpred30_FTS4977);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred30_FTS

	// $ANTLR start synpred31_FTS
	public final void synpred31_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:673:11: ( ftsFieldGroupExactPhrase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:673:12: ftsFieldGroupExactPhrase
		{
		pushFollow(FOLLOW_ftsFieldGroupExactPhrase_in_synpred31_FTS5048);
		ftsFieldGroupExactPhrase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred31_FTS

	// $ANTLR start synpred32_FTS
	public final void synpred32_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:673:68: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:673:69: slop
		{
		pushFollow(FOLLOW_slop_in_synpred32_FTS5058);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred32_FTS

	// $ANTLR start synpred33_FTS
	public final void synpred33_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:676:11: ( ftsFieldGroupTokenisedPhrase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:676:12: ftsFieldGroupTokenisedPhrase
		{
		pushFollow(FOLLOW_ftsFieldGroupTokenisedPhrase_in_synpred33_FTS5129);
		ftsFieldGroupTokenisedPhrase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred33_FTS

	// $ANTLR start synpred34_FTS
	public final void synpred34_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:676:76: ( slop )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:676:77: slop
		{
		pushFollow(FOLLOW_slop_in_synpred34_FTS5139);
		slop();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred34_FTS

	// $ANTLR start synpred35_FTS
	public final void synpred35_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:11: ( ftsFieldGroupSynonym )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:12: ftsFieldGroupSynonym
		{
		pushFollow(FOLLOW_ftsFieldGroupSynonym_in_synpred35_FTS5210);
		ftsFieldGroupSynonym();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred35_FTS

	// $ANTLR start synpred36_FTS
	public final void synpred36_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:60: ( fuzzy )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:679:61: fuzzy
		{
		pushFollow(FOLLOW_fuzzy_in_synpred36_FTS5220);
		fuzzy();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred36_FTS

	// $ANTLR start synpred37_FTS
	public final void synpred37_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:682:11: ( ftsFieldGroupRange )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:682:12: ftsFieldGroupRange
		{
		pushFollow(FOLLOW_ftsFieldGroupRange_in_synpred37_FTS5291);
		ftsFieldGroupRange();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred37_FTS

	// $ANTLR start synpred38_FTS
	public final void synpred38_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:725:38: ( proximityGroup )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:725:39: proximityGroup
		{
		pushFollow(FOLLOW_proximityGroup_in_synpred38_FTS5720);
		proximityGroup();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred38_FTS

	// $ANTLR start synpred39_FTS
	public final void synpred39_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:781:19: ( prefix )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:781:20: prefix
		{
		pushFollow(FOLLOW_prefix_in_synpred39_FTS6352);
		prefix();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred39_FTS

	// $ANTLR start synpred40_FTS
	public final void synpred40_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:817:9: ( ID DOT ID )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:817:10: ID DOT ID
		{
		match(input,ID,FOLLOW_ID_in_synpred40_FTS6807); if (state.failed) return;

		match(input,DOT,FOLLOW_DOT_in_synpred40_FTS6809); if (state.failed) return;

		match(input,ID,FOLLOW_ID_in_synpred40_FTS6811); if (state.failed) return;

		}

	}
	// $ANTLR end synpred40_FTS

	// $ANTLR start synpred41_FTS
	public final void synpred41_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:837:12: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:837:13: ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
		{
		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred41_FTS7133);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred41_FTS7141);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred41_FTS7149);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred41_FTS7157);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred41_FTS7165);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred41_FTS

	// $ANTLR start synpred42_FTS
	public final void synpred42_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:839:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase DOT | COMMA ftsWordBase )
		int alt78=2;
		int LA78_0 = input.LA(1);
		if ( (LA78_0==DATETIME||LA78_0==DECIMAL_INTEGER_LITERAL||LA78_0==FLOATING_POINT_LITERAL||(LA78_0 >= FTSPRE && LA78_0 <= FTSWORD)||LA78_0==ID||LA78_0==NOT||LA78_0==QUESTION_MARK||LA78_0==STAR||LA78_0==TO||LA78_0==URI) ) {
			alt78=1;
		}
		else if ( (LA78_0==COMMA) ) {
			alt78=2;
		}

		else {
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 78, 0, input);
			throw nvae;
		}

		switch (alt78) {
			case 1 :
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:839:12: ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase DOT
				{
				pushFollow(FOLLOW_ftsWordBase_in_synpred42_FTS7233);
				ftsWordBase();
				state._fsp--;
				if (state.failed) return;

				if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
					input.consume();
					state.errorRecovery=false;
					state.failed=false;
				}
				else {
					if (state.backtracking>0) {state.failed=true; return;}
					MismatchedSetException mse = new MismatchedSetException(null,input);
					throw mse;
				}
				pushFollow(FOLLOW_ftsWordBase_in_synpred42_FTS7241);
				ftsWordBase();
				state._fsp--;
				if (state.failed) return;

				if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
					input.consume();
					state.errorRecovery=false;
					state.failed=false;
				}
				else {
					if (state.backtracking>0) {state.failed=true; return;}
					MismatchedSetException mse = new MismatchedSetException(null,input);
					throw mse;
				}
				pushFollow(FOLLOW_ftsWordBase_in_synpred42_FTS7249);
				ftsWordBase();
				state._fsp--;
				if (state.failed) return;

				if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
					input.consume();
					state.errorRecovery=false;
					state.failed=false;
				}
				else {
					if (state.backtracking>0) {state.failed=true; return;}
					MismatchedSetException mse = new MismatchedSetException(null,input);
					throw mse;
				}
				pushFollow(FOLLOW_ftsWordBase_in_synpred42_FTS7257);
				ftsWordBase();
				state._fsp--;
				if (state.failed) return;

				match(input,DOT,FOLLOW_DOT_in_synpred42_FTS7259); if (state.failed) return;

				}
				break;
			case 2 :
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:839:100: COMMA ftsWordBase
				{
				match(input,COMMA,FOLLOW_COMMA_in_synpred42_FTS7261); if (state.failed) return;

				pushFollow(FOLLOW_ftsWordBase_in_synpred42_FTS7263);
				ftsWordBase();
				state._fsp--;
				if (state.failed) return;

				}
				break;

		}
	}
	// $ANTLR end synpred42_FTS

	// $ANTLR start synpred43_FTS
	public final void synpred43_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:841:12: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:841:13: ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
		{
		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred43_FTS7332);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred43_FTS7340);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred43_FTS7348);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred43_FTS7356);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred43_FTS

	// $ANTLR start synpred44_FTS
	public final void synpred44_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:843:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:843:12: ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
		{
		pushFollow(FOLLOW_ftsWordBase_in_synpred44_FTS7428);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred44_FTS7436);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred44_FTS7444);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred44_FTS7452);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred44_FTS

	// $ANTLR start synpred45_FTS
	public final void synpred45_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:845:12: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:845:13: ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
		{
		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred45_FTS7525);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred45_FTS7533);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred45_FTS7541);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred45_FTS7549);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred45_FTS

	// $ANTLR start synpred46_FTS
	public final void synpred46_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:847:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:847:12: ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
		{
		pushFollow(FOLLOW_ftsWordBase_in_synpred46_FTS7609);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred46_FTS7617);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred46_FTS7625);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred46_FTS7633);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred46_FTS

	// $ANTLR start synpred47_FTS
	public final void synpred47_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:849:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:849:12: ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
		{
		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred47_FTS7693);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred47_FTS7701);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred47_FTS7709);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred47_FTS

	// $ANTLR start synpred48_FTS
	public final void synpred48_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:851:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:851:12: ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
		{
		pushFollow(FOLLOW_ftsWordBase_in_synpred48_FTS7773);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred48_FTS7781);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred48_FTS7789);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred48_FTS

	// $ANTLR start synpred49_FTS
	public final void synpred49_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:853:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:853:12: ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
		{
		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred49_FTS7853);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred49_FTS7861);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred49_FTS7869);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred49_FTS

	// $ANTLR start synpred50_FTS
	public final void synpred50_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:855:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:855:12: ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
		{
		pushFollow(FOLLOW_ftsWordBase_in_synpred50_FTS7921);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred50_FTS7929);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred50_FTS7937);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred50_FTS

	// $ANTLR start synpred51_FTS
	public final void synpred51_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:857:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:857:12: ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
		{
		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred51_FTS7990);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred51_FTS7998);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred51_FTS

	// $ANTLR start synpred52_FTS
	public final void synpred52_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:859:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:859:12: ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
		{
		pushFollow(FOLLOW_ftsWordBase_in_synpred52_FTS8054);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred52_FTS8062);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred52_FTS

	// $ANTLR start synpred53_FTS
	public final void synpred53_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:861:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:861:12: ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase
		{
		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred53_FTS8120);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred53_FTS8128);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred53_FTS

	// $ANTLR start synpred54_FTS
	public final void synpred54_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:863:11: ( ftsWordBase ( DOT | COMMA ) ftsWordBase )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:863:12: ftsWordBase ( DOT | COMMA ) ftsWordBase
		{
		pushFollow(FOLLOW_ftsWordBase_in_synpred54_FTS8172);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred54_FTS8180);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred54_FTS

	// $ANTLR start synpred55_FTS
	public final void synpred55_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:865:11: ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:865:12: ( DOT | COMMA ) ftsWordBase ( DOT | COMMA )
		{
		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_ftsWordBase_in_synpred55_FTS8224);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred55_FTS

	// $ANTLR start synpred56_FTS
	public final void synpred56_FTS_fragment() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:867:11: ( ftsWordBase ( DOT | COMMA ) )
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\FTS.g:867:12: ftsWordBase ( DOT | COMMA )
		{
		pushFollow(FOLLOW_ftsWordBase_in_synpred56_FTS8272);
		ftsWordBase();
		state._fsp--;
		if (state.failed) return;

		if ( input.LA(1)==COMMA||input.LA(1)==DOT ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred56_FTS

	// Delegated rules

	public final boolean synpred22_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred22_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred1_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred27_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred27_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred43_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred43_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred34_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred34_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred7_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred7_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred31_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred31_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred45_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred45_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred8_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred8_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred49_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred49_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred4_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred4_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred24_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred24_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred20_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred20_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred48_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred48_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred52_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred52_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred10_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred10_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred41_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred41_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred35_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred35_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred26_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred26_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred40_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred40_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred56_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred56_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred36_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred36_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred6_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred6_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred30_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred30_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred21_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred21_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred23_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred23_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred17_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred17_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred14_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred14_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred12_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred12_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred50_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred50_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred51_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred51_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred53_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred53_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred19_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred19_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred9_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred9_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred32_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred32_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred16_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred16_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred29_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred29_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred11_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred11_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred3_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred3_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred44_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred44_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred54_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred54_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred46_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred46_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred18_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred18_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred25_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred25_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred55_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred55_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred47_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred47_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred38_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred38_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred42_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred42_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred13_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred13_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred5_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred5_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred28_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred28_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred15_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred15_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred39_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred39_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred37_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred37_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred33_FTS() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred33_FTS_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}


	protected DFA17 dfa17 = new DFA17(this);
	protected DFA33 dfa33 = new DFA33(this);
	protected DFA39 dfa39 = new DFA39(this);
	protected DFA60 dfa60 = new DFA60(this);
	protected DFA73 dfa73 = new DFA73(this);
	static final String DFA17_eotS =
		"\u00c8\uffff";
	static final String DFA17_eofS =
		"\1\uffff\12\41\4\uffff\1\41\10\uffff\2\41\40\uffff\5\41\46\uffff\2\41"+
		"\2\uffff\11\41\3\uffff\1\41\47\uffff\1\41\3\uffff\5\41\1\uffff\1\41\4"+
		"\uffff\1\41\26\uffff\1\41\4\uffff";
	static final String DFA17_minS =
		"\1\5\12\4\2\5\2\12\1\4\10\uffff\2\4\1\5\37\uffff\5\4\3\12\1\5\3\uffff"+
		"\1\0\13\uffff\1\4\12\0\1\5\7\uffff\2\4\2\12\11\4\1\5\2\uffff\1\4\1\13"+
		"\1\77\3\12\1\4\34\uffff\5\0\1\4\3\uffff\5\4\1\0\1\4\2\uffff\1\12\1\77"+
		"\1\4\12\uffff\1\0\13\uffff\1\4\1\12\1\0\1\uffff\1\0";
	static final String DFA17_maxS =
		"\13\150\1\146\1\150\2\12\1\150\10\uffff\3\150\37\uffff\5\150\1\25\2\12"+
		"\1\146\3\uffff\1\0\13\uffff\1\150\12\0\1\146\7\uffff\2\150\2\12\11\150"+
		"\1\146\2\uffff\2\150\1\77\1\25\2\12\1\150\34\uffff\5\0\1\150\3\uffff\5"+
		"\150\1\0\1\150\2\uffff\1\12\1\77\1\150\12\uffff\1\0\13\uffff\1\150\1\12"+
		"\1\0\1\uffff\1\0";
	static final String DFA17_acceptS =
		"\20\uffff\2\2\2\4\1\5\1\6\1\7\1\10\3\uffff\1\2\36\4\11\uffff\3\4\1\uffff"+
		"\13\4\14\uffff\6\4\1\3\16\uffff\2\4\7\uffff\1\1\33\4\6\uffff\3\4\7\uffff"+
		"\2\4\3\uffff\12\4\1\uffff\13\4\3\uffff\1\4\1\uffff";
	static final String DFA17_specialS =
		"\1\45\1\73\1\40\1\43\1\44\1\66\1\77\1\54\1\55\1\71\1\63\4\uffff\1\62\10"+
		"\uffff\1\53\1\20\1\56\37\uffff\1\47\1\4\1\2\1\5\1\27\7\uffff\1\41\13\uffff"+
		"\1\17\1\60\1\57\1\65\1\11\1\75\1\12\1\25\1\35\1\31\1\24\10\uffff\1\3\1"+
		"\0\2\uffff\1\1\1\67\1\72\1\42\1\10\1\51\1\52\1\26\1\46\3\uffff\1\37\1"+
		"\34\4\uffff\1\6\34\uffff\1\32\1\33\1\22\1\14\1\7\1\76\3\uffff\1\50\1\15"+
		"\1\13\1\64\1\70\1\16\1\74\4\uffff\1\36\12\uffff\1\23\13\uffff\1\21\1\uffff"+
		"\1\30\1\uffff\1\61}>";
	static final String[] DFA17_transitionS = {
			"\1\16\1\14\4\uffff\1\22\1\uffff\1\11\1\uffff\1\7\5\uffff\1\22\2\uffff"+
			"\1\24\26\uffff\1\10\2\uffff\1\17\1\3\1\4\1\2\11\uffff\1\1\3\uffff\1\26"+
			"\1\20\1\21\10\uffff\1\5\2\uffff\1\15\1\27\5\uffff\1\23\7\uffff\1\12\4"+
			"\uffff\1\25\1\6\1\uffff\1\13",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\32\1\34\1\uffff\1\56\1\uffff\1\54"+
			"\5\uffff\1\30\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff\1"+
			"\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\34\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\34\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\34\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\32\1\34\1\uffff\1\56\1\uffff\1\54"+
			"\5\uffff\1\34\2\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff\1\61\1"+
			"\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71\5\uffff"+
			"\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42\4\uffff"+
			"\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\32\1\34\1\uffff\1\56\1\uffff\1\54"+
			"\5\uffff\1\34\2\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff\1\61\1"+
			"\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71\5\uffff"+
			"\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42\4\uffff"+
			"\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\34\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\34\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\34\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\34\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\75\71\uffff\1\72\16\uffff\1\76\2\uffff\1\74\24\uffff\1\73",
			"\1\16\71\uffff\1\77\16\uffff\1\101\2\uffff\1\15\24\uffff\1\100\1\uffff"+
			"\1\102",
			"\1\32",
			"\1\32",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\105\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\105\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\104\4\uffff\1\103\1\53\1\uffff\1\57",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\105\1\uffff\1\120\1\uffff"+
			"\1\114\5\uffff\1\105\2\uffff\1\65\2\uffff\1\52\23\uffff\1\115\2\uffff"+
			"\1\61\1\110\1\111\1\107\11\uffff\1\106\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\112\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\117\2\uffff"+
			"\1\42\4\uffff\1\116\4\uffff\1\35\1\113\1\uffff\1\121",
			"\1\143\1\142\1\60\1\44\1\uffff\1\141\1\uffff\1\137\1\uffff\1\133\1\uffff"+
			"\1\131\5\uffff\1\137\1\136\1\uffff\1\65\2\uffff\1\52\23\uffff\1\132\2"+
			"\uffff\1\61\1\125\1\126\1\124\11\uffff\1\123\3\uffff\1\122\1\62\1\63"+
			"\2\uffff\1\71\5\uffff\1\127\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1"+
			"\64\2\uffff\1\42\4\uffff\1\134\4\uffff\1\140\1\130\1\uffff\1\135",
			"\1\150\5\uffff\1\163\1\uffff\1\160\1\uffff\1\156\5\uffff\1\163\31\uffff"+
			"\1\157\2\uffff\1\155\1\153\1\154\1\152\11\uffff\1\145\3\uffff\1\144\1"+
			"\20\1\21\10\uffff\1\151\2\uffff\1\147\6\uffff\1\164\7\uffff\1\161\5\uffff"+
			"\1\146\1\uffff\1\162",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\166\1\34\1\uffff\1\56\1\uffff\1"+
			"\54\5\uffff\1\165\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\166\1\34\1\uffff\1\56\1\uffff\1"+
			"\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\166\1\34\1\uffff\1\56\1\uffff\1"+
			"\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\166\1\34\1\uffff\1\56\1\uffff\1"+
			"\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\166\1\34\1\uffff\1\56\1\uffff\1"+
			"\54\5\uffff\1\34\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\31\4\uffff\1\35\1\53\1\uffff\1\57",
			"\1\32\12\uffff\1\167",
			"\1\32",
			"\1\32",
			"\1\150\71\uffff\1\170\16\uffff\1\172\2\uffff\1\147\24\uffff\1\171",
			"",
			"",
			"",
			"\1\uffff",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u008c\1\u008b\1\u008e\1\u0097\3\uffff\1\175\1\uffff\1\u0087\1\uffff"+
			"\1\173\5\uffff\1\175\2\uffff\1\u0092\2\uffff\1\u008d\23\uffff\1\u0084"+
			"\2\uffff\1\u0089\1\u0080\1\u0081\1\177\11\uffff\1\176\3\uffff\1\u0094"+
			"\1\u0090\1\u0091\2\uffff\1\u008a\5\uffff\1\u0082\2\uffff\1\u008f\1\u0095"+
			"\1\uffff\1\u0096\3\uffff\1\u0086\2\uffff\1\174\4\uffff\1\u0085\4\uffff"+
			"\1\u0093\1\u0083\1\uffff\1\u0088",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u009b\71\uffff\1\u0098\16\uffff\1\u009c\2\uffff\1\u009a\24\uffff"+
			"\1\u0099",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\166\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009d\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\166\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\2\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\166",
			"\1\166",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\166\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\2\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\105\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\105\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2\uffff"+
			"\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff\1\71"+
			"\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff\1\42"+
			"\4\uffff\1\104\4\uffff\1\u00a0\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\u00a4\71\uffff\1\u00a1\16\uffff\1\u00a5\2\uffff\1\u00a3\24\uffff"+
			"\1\u00a2",
			"",
			"",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\105\1\uffff\1\120\1\uffff"+
			"\1\114\5\uffff\1\105\2\uffff\1\65\2\uffff\1\52\23\uffff\1\115\2\uffff"+
			"\1\61\1\110\1\111\1\107\11\uffff\1\u00a6\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\112\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\117\2\uffff"+
			"\1\42\4\uffff\1\116\4\uffff\1\35\1\113\1\uffff\1\121",
			"\1\163\1\uffff\1\160\1\uffff\1\156\5\uffff\1\163\31\uffff\1\157\2\uffff"+
			"\1\155\1\153\1\154\1\152\11\uffff\1\u00a7\3\uffff\1\144\1\20\1\21\10"+
			"\uffff\1\u00a8\11\uffff\1\164\7\uffff\1\161\5\uffff\1\u00a9\1\uffff\1"+
			"\162",
			"\1\u00aa",
			"\1\166\12\uffff\1\u00ab",
			"\1\166",
			"\1\166",
			"\1\u00b6\1\u00b5\1\u008e\1\u00af\1\uffff\1\u00b4\1\uffff\1\u00ad\1\uffff"+
			"\1\u0087\1\uffff\1\u00b0\5\uffff\1\u00ad\1\u00b2\1\uffff\1\u0092\2\uffff"+
			"\1\u008d\23\uffff\1\u0084\2\uffff\1\u0089\1\u0080\1\u0081\1\177\11\uffff"+
			"\1\176\3\uffff\1\u0094\1\u0090\1\u0091\2\uffff\1\u008a\5\uffff\1\u0082"+
			"\2\uffff\1\u00ae\1\u0095\1\uffff\1\u0096\3\uffff\1\u0086\2\uffff\1\u00ac"+
			"\4\uffff\1\u00b1\4\uffff\1\u00b3\1\u0083\1\uffff\1\u0088",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\105\1\uffff\1\u00c1\1\uffff"+
			"\1\u00bd\5\uffff\1\105\2\uffff\1\65\2\uffff\1\52\23\uffff\1\u00be\2\uffff"+
			"\1\61\1\u00b9\1\u00ba\1\u00b8\11\uffff\1\u00b7\3\uffff\1\66\1\62\1\63"+
			"\2\uffff\1\71\5\uffff\1\u00bb\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff"+
			"\1\u00c0\2\uffff\1\42\4\uffff\1\u00bf\4\uffff\1\u009f\1\u00bc\1\uffff"+
			"\1\u00c2",
			"",
			"",
			"",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u00c3\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"\1\uffff",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\u009e\1\uffff\1\56\1\uffff"+
			"\1\54\5\uffff\1\u009e\1\33\1\uffff\1\65\2\uffff\1\52\23\uffff\1\55\2"+
			"\uffff\1\61\1\50\1\51\1\47\11\uffff\1\46\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\45\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\104\4\uffff\1\u009f\1\53\1\uffff\1\57",
			"",
			"",
			"\1\32",
			"\1\u00c4",
			"\1\143\1\142\1\60\1\44\1\uffff\1\141\1\uffff\1\105\1\uffff\1\133\1\uffff"+
			"\1\131\5\uffff\1\105\2\uffff\1\65\2\uffff\1\52\23\uffff\1\132\2\uffff"+
			"\1\61\1\125\1\126\1\124\11\uffff\1\123\3\uffff\1\66\1\62\1\63\2\uffff"+
			"\1\71\5\uffff\1\127\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff\1\64\2\uffff"+
			"\1\42\4\uffff\1\u00c5\4\uffff\1\u00c6\1\130\1\uffff\1\135",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\uffff",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\40\1\37\1\60\1\44\1\uffff\1\36\1\uffff\1\105\1\uffff\1\u00c1\1\uffff"+
			"\1\u00bd\5\uffff\1\105\2\uffff\1\65\2\uffff\1\52\23\uffff\1\u00be\2\uffff"+
			"\1\61\1\u00b9\1\u00ba\1\u00b8\11\uffff\1\u00c7\3\uffff\1\66\1\62\1\63"+
			"\2\uffff\1\71\5\uffff\1\u00bb\2\uffff\1\43\1\67\1\uffff\1\70\3\uffff"+
			"\1\u00c0\2\uffff\1\42\4\uffff\1\u00bf\4\uffff\1\u009f\1\u00bc\1\uffff"+
			"\1\u00c2",
			"\1\166",
			"\1\uffff",
			"",
			"\1\uffff"
	};

	static final short[] DFA17_eot = DFA.unpackEncodedString(DFA17_eotS);
	static final short[] DFA17_eof = DFA.unpackEncodedString(DFA17_eofS);
	static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars(DFA17_minS);
	static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars(DFA17_maxS);
	static final short[] DFA17_accept = DFA.unpackEncodedString(DFA17_acceptS);
	static final short[] DFA17_special = DFA.unpackEncodedString(DFA17_specialS);
	static final short[][] DFA17_transition;

	static {
		int numStates = DFA17_transitionS.length;
		DFA17_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA17_transition[i] = DFA.unpackEncodedString(DFA17_transitionS[i]);
		}
	}

	protected class DFA17 extends DFA {

		public DFA17(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 17;
			this.eot = DFA17_eot;
			this.eof = DFA17_eof;
			this.min = DFA17_min;
			this.max = DFA17_max;
			this.accept = DFA17_accept;
			this.special = DFA17_special;
			this.transition = DFA17_transition;
		}
		@Override
		public String getDescription() {
			return "445:1: ftsTest : ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( PROXIMITY ftsFieldGroupProximity ) | ( ftsRange )=> ftsRange -> ^( RANGE ftsRange ) | ( ftsFieldGroup )=> ftsFieldGroup -> ftsFieldGroup | ( ftsTermOrPhrase )=> ftsTermOrPhrase | ( ftsExactTermOrPhrase )=> ftsExactTermOrPhrase | ( ftsTokenisedTermOrPhrase )=> ftsTokenisedTermOrPhrase | LPAREN ftsDisjunction RPAREN -> ftsDisjunction | template -> template );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA17_102 = input.LA(1);
						 
						int index17_102 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_102==COLON) ) {s = 118;}
						else if ( (LA17_102==COMMA||LA17_102==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_102==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_102==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_102==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_102==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_102==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_102==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_102==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_102==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_102==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_102==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_102==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_102==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_102==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_102==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_102==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_102==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_102==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_102==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_102==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_102==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_102==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_102==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_102==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_102==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_102==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_102==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_102==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_102==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_102==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_102==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_102);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA17_105 = input.LA(1);
						 
						int index17_105 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_105==COLON) ) {s = 118;}
						else if ( (LA17_105==COMMA||LA17_105==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_105==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_105==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_105==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_105==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_105==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_105==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_105==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_105==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_105==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_105==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_105==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_105==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_105==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_105==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_105==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_105==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_105==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_105==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_105==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_105==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_105==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_105==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_105==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_105==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_105==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_105==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_105==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_105==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_105==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_105==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_105);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA17_60 = input.LA(1);
						 
						int index17_60 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_60==STAR) ) {s = 25;}
						else if ( (LA17_60==COLON) ) {s = 118;}
						else if ( (LA17_60==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_60==COMMA||LA17_60==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_60==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_60==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_60==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_60==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_60==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_60==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_60==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_60==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_60==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_60==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_60==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_60==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_60==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_60==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_60==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_60==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_60==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_60==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_60==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_60==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_60==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_60==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_60==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_60==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_60==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_60==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_60==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_60==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_60==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_60);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA17_101 = input.LA(1);
						 
						int index17_101 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_101==DOT) ) {s = 157;}
						else if ( (LA17_101==COLON) ) {s = 118;}
						else if ( (LA17_101==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_101==COMMA) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_101==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_101==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_101==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_101==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_101==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_101==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_101==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_101==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_101==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_101==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_101==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_101==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_101==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_101==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_101==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_101==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_101==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_101==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_101==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_101==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_101==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_101==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_101==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_101==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_101==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_101==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_101==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_101==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_101==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_101==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_101);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA17_59 = input.LA(1);
						 
						int index17_59 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_59==STAR) ) {s = 25;}
						else if ( (LA17_59==COLON) ) {s = 118;}
						else if ( (LA17_59==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_59==COMMA||LA17_59==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_59==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_59==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_59==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_59==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_59==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_59==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_59==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_59==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_59==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_59==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_59==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_59==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_59==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_59==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_59==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_59==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_59==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_59==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_59==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_59==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_59==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_59==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_59==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_59==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_59==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_59==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_59==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_59==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_59==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_59);
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA17_61 = input.LA(1);
						 
						int index17_61 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_61==STAR) ) {s = 25;}
						else if ( (LA17_61==COLON) ) {s = 118;}
						else if ( (LA17_61==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_61==COMMA||LA17_61==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_61==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_61==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_61==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_61==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_61==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_61==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_61==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_61==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_61==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_61==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_61==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_61==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_61==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_61==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_61==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_61==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_61==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_61==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_61==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_61==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_61==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_61==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_61==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_61==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_61==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_61==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_61==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_61==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_61==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_61);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA17_123 = input.LA(1);
						 
						int index17_123 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_123==RPAREN) ) {s = 172;}
						else if ( (LA17_123==COMMA||LA17_123==DOT) && (synpred5_FTS())) {s = 173;}
						else if ( (LA17_123==OR) && (synpred5_FTS())) {s = 174;}
						else if ( (LA17_123==BAR) && (synpred5_FTS())) {s = 175;}
						else if ( (LA17_123==ID) && (synpred5_FTS())) {s = 126;}
						else if ( (LA17_123==FTSWORD) && (synpred5_FTS())) {s = 127;}
						else if ( (LA17_123==FTSPRE) && (synpred5_FTS())) {s = 128;}
						else if ( (LA17_123==FTSWILD) && (synpred5_FTS())) {s = 129;}
						else if ( (LA17_123==NOT) && (synpred5_FTS())) {s = 130;}
						else if ( (LA17_123==TO) && (synpred5_FTS())) {s = 131;}
						else if ( (LA17_123==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 176;}
						else if ( (LA17_123==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 132;}
						else if ( (LA17_123==STAR) && (synpred5_FTS())) {s = 177;}
						else if ( (LA17_123==QUESTION_MARK) && (synpred5_FTS())) {s = 134;}
						else if ( (LA17_123==DATETIME) && (synpred5_FTS())) {s = 135;}
						else if ( (LA17_123==URI) && (synpred5_FTS())) {s = 136;}
						else if ( (LA17_123==FTSPHRASE) && (synpred5_FTS())) {s = 137;}
						else if ( (LA17_123==MINUS) && (synpred5_FTS())) {s = 138;}
						else if ( (LA17_123==DOTDOT) && (synpred5_FTS())) {s = 178;}
						else if ( (LA17_123==TILDA) && (synpred5_FTS())) {s = 179;}
						else if ( (LA17_123==CARAT) && (synpred5_FTS())) {s = 180;}
						else if ( (LA17_123==AND) && (synpred5_FTS())) {s = 181;}
						else if ( (LA17_123==AMP) && (synpred5_FTS())) {s = 182;}
						else if ( (LA17_123==EXCLAMATION) && (synpred5_FTS())) {s = 141;}
						else if ( (LA17_123==AT) && (synpred5_FTS())) {s = 142;}
						else if ( (LA17_123==LSQUARE) && (synpred5_FTS())) {s = 144;}
						else if ( (LA17_123==LT) && (synpred5_FTS())) {s = 145;}
						else if ( (LA17_123==EQUALS) && (synpred5_FTS())) {s = 146;}
						else if ( (LA17_123==LPAREN) && (synpred5_FTS())) {s = 148;}
						else if ( (LA17_123==PERCENT) && (synpred5_FTS())) {s = 149;}
						else if ( (LA17_123==PLUS) && (synpred5_FTS())) {s = 150;}
						 
						input.seek(index17_123);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA17_156 = input.LA(1);
						 
						int index17_156 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 182;}
						 
						input.seek(index17_156);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA17_109 = input.LA(1);
						 
						int index17_109 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_109==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_109==TILDA) && (synpred5_FTS())) {s = 160;}
						else if ( (LA17_109==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_109==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_109==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_109==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_109==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_109==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_109==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_109==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_109==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_109==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_109==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_109==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_109==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_109==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_109==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_109==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_109==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_109==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_109==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_109==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_109==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_109==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_109==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_109==COMMA||LA17_109==DOT) && (synpred5_FTS())) {s = 69;}
						else if ( (LA17_109==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_109==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_109==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_109==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_109==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_109==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_109);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA17_86 = input.LA(1);
						 
						int index17_86 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_86);
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA17_88 = input.LA(1);
						 
						int index17_88 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_88);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA17_163 = input.LA(1);
						 
						int index17_163 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_163==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_163==COMMA||LA17_163==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_163==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_163==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_163==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_163==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_163==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_163==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_163==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_163==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_163==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_163==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_163==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_163==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_163==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_163==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_163==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_163==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_163==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_163==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_163==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_163==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_163==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_163==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_163==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_163==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_163==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_163==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_163==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_163==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_163==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_163==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_163);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA17_155 = input.LA(1);
						 
						int index17_155 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 182;}
						 
						input.seek(index17_155);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA17_162 = input.LA(1);
						 
						int index17_162 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_162==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_162==COMMA||LA17_162==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_162==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_162==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_162==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_162==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_162==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_162==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_162==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_162==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_162==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_162==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_162==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_162==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_162==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_162==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_162==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_162==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_162==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_162==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_162==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_162==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_162==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_162==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_162==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_162==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_162==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_162==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_162==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_162==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_162==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_162==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_162);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA17_166 = input.LA(1);
						 
						int index17_166 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred3_FTS()) ) {s = 27;}
						else if ( (synpred4_FTS()) ) {s = 100;}
						else if ( (synpred5_FTS()) ) {s = 194;}
						 
						input.seek(index17_166);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA17_82 = input.LA(1);
						 
						int index17_82 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_82==DECIMAL_INTEGER_LITERAL) ) {s = 123;}
						else if ( (LA17_82==RPAREN) && (synpred2_FTS())) {s = 124;}
						else if ( (LA17_82==COMMA||LA17_82==DOT) && (synpred5_FTS())) {s = 125;}
						else if ( (LA17_82==ID) && (synpred5_FTS())) {s = 126;}
						else if ( (LA17_82==FTSWORD) && (synpred5_FTS())) {s = 127;}
						else if ( (LA17_82==FTSPRE) && (synpred5_FTS())) {s = 128;}
						else if ( (LA17_82==FTSWILD) && (synpred5_FTS())) {s = 129;}
						else if ( (LA17_82==NOT) && (synpred5_FTS())) {s = 130;}
						else if ( (LA17_82==TO) && (synpred5_FTS())) {s = 131;}
						else if ( (LA17_82==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 132;}
						else if ( (LA17_82==STAR) && (synpred5_FTS())) {s = 133;}
						else if ( (LA17_82==QUESTION_MARK) && (synpred5_FTS())) {s = 134;}
						else if ( (LA17_82==DATETIME) && (synpred5_FTS())) {s = 135;}
						else if ( (LA17_82==URI) && (synpred5_FTS())) {s = 136;}
						else if ( (LA17_82==FTSPHRASE) && (synpred5_FTS())) {s = 137;}
						else if ( (LA17_82==MINUS) && (synpred5_FTS())) {s = 138;}
						else if ( (LA17_82==AND) && (synpred5_FTS())) {s = 139;}
						else if ( (LA17_82==AMP) && (synpred5_FTS())) {s = 140;}
						else if ( (LA17_82==EXCLAMATION) && (synpred5_FTS())) {s = 141;}
						else if ( (LA17_82==AT) && (synpred5_FTS())) {s = 142;}
						else if ( (LA17_82==OR) && (synpred5_FTS())) {s = 143;}
						else if ( (LA17_82==LSQUARE) && (synpred5_FTS())) {s = 144;}
						else if ( (LA17_82==LT) && (synpred5_FTS())) {s = 145;}
						else if ( (LA17_82==EQUALS) && (synpred5_FTS())) {s = 146;}
						else if ( (LA17_82==TILDA) && (synpred5_FTS())) {s = 147;}
						else if ( (LA17_82==LPAREN) && (synpred5_FTS())) {s = 148;}
						else if ( (LA17_82==PERCENT) && (synpred5_FTS())) {s = 149;}
						else if ( (LA17_82==PLUS) && (synpred5_FTS())) {s = 150;}
						else if ( (LA17_82==BAR) && (synpred5_FTS())) {s = 151;}
						 
						input.seek(index17_82);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA17_25 = input.LA(1);
						 
						int index17_25 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_25==LPAREN) ) {s = 82;}
						else if ( (LA17_25==ID) ) {s = 83;}
						else if ( (LA17_25==FTSWORD) ) {s = 84;}
						else if ( (LA17_25==FTSPRE) ) {s = 85;}
						else if ( (LA17_25==FTSWILD) ) {s = 86;}
						else if ( (LA17_25==NOT) ) {s = 87;}
						else if ( (LA17_25==TO) ) {s = 88;}
						else if ( (LA17_25==DECIMAL_INTEGER_LITERAL) ) {s = 89;}
						else if ( (LA17_25==FLOATING_POINT_LITERAL) ) {s = 90;}
						else if ( (LA17_25==DATETIME) ) {s = 91;}
						else if ( (LA17_25==STAR) ) {s = 92;}
						else if ( (LA17_25==URI) ) {s = 93;}
						else if ( (LA17_25==DOTDOT) && (synpred5_FTS())) {s = 94;}
						else if ( (LA17_25==COMMA||LA17_25==DOT) && (synpred5_FTS())) {s = 95;}
						else if ( (LA17_25==TILDA) && (synpred5_FTS())) {s = 96;}
						else if ( (LA17_25==CARAT) && (synpred5_FTS())) {s = 97;}
						else if ( (LA17_25==AND) && (synpred5_FTS())) {s = 98;}
						else if ( (LA17_25==AMP) && (synpred5_FTS())) {s = 99;}
						else if ( (LA17_25==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_25==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_25==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_25==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_25==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_25==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_25==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_25==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_25==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_25==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_25==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_25==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_25==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_25==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_25);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA17_195 = input.LA(1);
						 
						int index17_195 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_195==ID) ) {s = 199;}
						else if ( (LA17_195==FTSWORD) && (synpred5_FTS())) {s = 184;}
						else if ( (LA17_195==FTSPRE) && (synpred5_FTS())) {s = 185;}
						else if ( (LA17_195==FTSWILD) && (synpred5_FTS())) {s = 186;}
						else if ( (LA17_195==NOT) && (synpred5_FTS())) {s = 187;}
						else if ( (LA17_195==TO) && (synpred5_FTS())) {s = 188;}
						else if ( (LA17_195==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 189;}
						else if ( (LA17_195==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 190;}
						else if ( (LA17_195==STAR) && (synpred5_FTS())) {s = 191;}
						else if ( (LA17_195==QUESTION_MARK) && (synpred5_FTS())) {s = 192;}
						else if ( (LA17_195==DATETIME) && (synpred5_FTS())) {s = 193;}
						else if ( (LA17_195==URI) && (synpred5_FTS())) {s = 194;}
						else if ( (LA17_195==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_195==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_195==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_195==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_195==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_195==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_195==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_195==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_195==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_195==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_195==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_195==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_195==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_195==COMMA||LA17_195==DOT) && (synpred5_FTS())) {s = 69;}
						else if ( (LA17_195==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_195==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_195==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_195==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_195==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_195);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA17_154 = input.LA(1);
						 
						int index17_154 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 182;}
						 
						input.seek(index17_154);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA17_183 = input.LA(1);
						 
						int index17_183 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_FTS()) ) {s = 27;}
						else if ( (synpred4_FTS()) ) {s = 100;}
						else if ( (synpred5_FTS()) ) {s = 198;}
						 
						input.seek(index17_183);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA17_92 = input.LA(1);
						 
						int index17_92 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_92);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA17_89 = input.LA(1);
						 
						int index17_89 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_89);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA17_112 = input.LA(1);
						 
						int index17_112 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_112==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_112==COMMA||LA17_112==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_112==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_112==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_112==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_112==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_112==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_112==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_112==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_112==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_112==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_112==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_112==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_112==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_112==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_112==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_112==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_112==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_112==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_112==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_112==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_112==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_112==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_112==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_112==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_112==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_112==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_112==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_112==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_112==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_112==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_112==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_112);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA17_62 = input.LA(1);
						 
						int index17_62 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_62==STAR) ) {s = 25;}
						else if ( (LA17_62==COLON) ) {s = 118;}
						else if ( (LA17_62==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_62==COMMA||LA17_62==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_62==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_62==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_62==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_62==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_62==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_62==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_62==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_62==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_62==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_62==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_62==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_62==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_62==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_62==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_62==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_62==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_62==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_62==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_62==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_62==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_62==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_62==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_62==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_62==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_62==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_62==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_62==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_62==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_62==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_62);
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA17_197 = input.LA(1);
						 
						int index17_197 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 198;}
						 
						input.seek(index17_197);
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA17_91 = input.LA(1);
						 
						int index17_91 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_91);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA17_152 = input.LA(1);
						 
						int index17_152 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 182;}
						 
						input.seek(index17_152);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA17_153 = input.LA(1);
						 
						int index17_153 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 182;}
						 
						input.seek(index17_153);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA17_118 = input.LA(1);
						 
						int index17_118 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_118==LPAREN) && (synpred4_FTS())) {s = 100;}
						else if ( (LA17_118==ID) ) {s = 167;}
						else if ( (LA17_118==FTSWORD) ) {s = 106;}
						else if ( (LA17_118==FTSPRE) ) {s = 107;}
						else if ( (LA17_118==FTSWILD) ) {s = 108;}
						else if ( (LA17_118==FTSPHRASE) ) {s = 109;}
						else if ( (LA17_118==DECIMAL_INTEGER_LITERAL) ) {s = 110;}
						else if ( (LA17_118==FLOATING_POINT_LITERAL) ) {s = 111;}
						else if ( (LA17_118==DATETIME) ) {s = 112;}
						else if ( (LA17_118==STAR) ) {s = 113;}
						else if ( (LA17_118==URI) ) {s = 114;}
						else if ( (LA17_118==LSQUARE) && (synpred3_FTS())) {s = 16;}
						else if ( (LA17_118==LT) && (synpred3_FTS())) {s = 17;}
						else if ( (LA17_118==COMMA||LA17_118==DOT) && (synpred5_FTS())) {s = 115;}
						else if ( (LA17_118==NOT) && (synpred5_FTS())) {s = 168;}
						else if ( (LA17_118==TO) && (synpred5_FTS())) {s = 169;}
						else if ( (LA17_118==QUESTION_MARK) && (synpred5_FTS())) {s = 116;}
						 
						input.seek(index17_118);
						if ( s>=0 ) return s;
						break;

					case 29 : 
						int LA17_90 = input.LA(1);
						 
						int index17_90 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_90);
						if ( s>=0 ) return s;
						break;

					case 30 : 
						int LA17_172 = input.LA(1);
						 
						int index17_172 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_172==ID) ) {s = 83;}
						else if ( (LA17_172==FTSWORD) ) {s = 84;}
						else if ( (LA17_172==FTSPRE) ) {s = 85;}
						else if ( (LA17_172==FTSWILD) ) {s = 86;}
						else if ( (LA17_172==NOT) ) {s = 87;}
						else if ( (LA17_172==TO) ) {s = 88;}
						else if ( (LA17_172==DECIMAL_INTEGER_LITERAL) ) {s = 89;}
						else if ( (LA17_172==FLOATING_POINT_LITERAL) ) {s = 90;}
						else if ( (LA17_172==DATETIME) ) {s = 91;}
						else if ( (LA17_172==STAR) ) {s = 197;}
						else if ( (LA17_172==URI) ) {s = 93;}
						else if ( (LA17_172==CARAT) && (synpred5_FTS())) {s = 97;}
						else if ( (LA17_172==AND) && (synpred5_FTS())) {s = 98;}
						else if ( (LA17_172==AMP) && (synpred5_FTS())) {s = 99;}
						else if ( (LA17_172==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_172==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_172==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_172==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_172==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_172==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_172==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_172==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_172==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_172==COMMA||LA17_172==DOT) && (synpred5_FTS())) {s = 69;}
						else if ( (LA17_172==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_172==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_172==TILDA) && (synpred5_FTS())) {s = 198;}
						else if ( (LA17_172==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_172==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_172==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_172==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_172);
						if ( s>=0 ) return s;
						break;

					case 31 : 
						int LA17_117 = input.LA(1);
						 
						int index17_117 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_117==ID) ) {s = 166;}
						else if ( (LA17_117==FTSWORD) && (synpred5_FTS())) {s = 71;}
						else if ( (LA17_117==FTSPRE) && (synpred5_FTS())) {s = 72;}
						else if ( (LA17_117==FTSWILD) && (synpred5_FTS())) {s = 73;}
						else if ( (LA17_117==NOT) && (synpred5_FTS())) {s = 74;}
						else if ( (LA17_117==TO) && (synpred5_FTS())) {s = 75;}
						else if ( (LA17_117==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 76;}
						else if ( (LA17_117==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 77;}
						else if ( (LA17_117==STAR) && (synpred5_FTS())) {s = 78;}
						else if ( (LA17_117==QUESTION_MARK) && (synpred5_FTS())) {s = 79;}
						else if ( (LA17_117==DATETIME) && (synpred5_FTS())) {s = 80;}
						else if ( (LA17_117==URI) && (synpred5_FTS())) {s = 81;}
						else if ( (LA17_117==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_117==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_117==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_117==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_117==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_117==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_117==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_117==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_117==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_117==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_117==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_117==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_117==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_117==COMMA||LA17_117==DOT) && (synpred5_FTS())) {s = 69;}
						else if ( (LA17_117==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_117==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_117==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_117==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_117==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_117);
						if ( s>=0 ) return s;
						break;

					case 32 : 
						int LA17_2 = input.LA(1);
						 
						int index17_2 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_2==STAR) ) {s = 25;}
						else if ( (LA17_2==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_2==COMMA||LA17_2==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_2==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_2==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_2==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_2==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_2==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_2==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_2==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_2==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_2==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_2==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_2==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_2==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_2==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_2==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_2==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_2==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_2==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_2==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_2==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_2==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_2==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_2==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_2==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_2==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_2==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_2==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_2==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_2==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_2==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_2);
						if ( s>=0 ) return s;
						break;

					case 33 : 
						int LA17_70 = input.LA(1);
						 
						int index17_70 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_FTS()) ) {s = 27;}
						else if ( (synpred4_FTS()) ) {s = 100;}
						else if ( (synpred5_FTS()) ) {s = 116;}
						 
						input.seek(index17_70);
						if ( s>=0 ) return s;
						break;

					case 34 : 
						int LA17_108 = input.LA(1);
						 
						int index17_108 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_108==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_108==COMMA||LA17_108==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_108==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_108==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_108==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_108==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_108==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_108==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_108==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_108==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_108==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_108==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_108==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_108==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_108==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_108==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_108==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_108==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_108==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_108==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_108==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_108==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_108==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_108==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_108==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_108==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_108==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_108==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_108==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_108==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_108==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_108==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_108);
						if ( s>=0 ) return s;
						break;

					case 35 : 
						int LA17_3 = input.LA(1);
						 
						int index17_3 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_3==STAR) ) {s = 25;}
						else if ( (LA17_3==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_3==COMMA||LA17_3==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_3==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_3==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_3==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_3==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_3==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_3==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_3==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_3==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_3==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_3==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_3==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_3==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_3==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_3==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_3==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_3==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_3==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_3==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_3==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_3==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_3==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_3==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_3==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_3==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_3==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_3==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_3==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_3==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_3==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_3);
						if ( s>=0 ) return s;
						break;

					case 36 : 
						int LA17_4 = input.LA(1);
						 
						int index17_4 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_4==STAR) ) {s = 25;}
						else if ( (LA17_4==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_4==COMMA||LA17_4==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_4==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_4==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_4==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_4==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_4==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_4==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_4==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_4==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_4==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_4==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_4==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_4==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_4==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_4==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_4==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_4==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_4==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_4==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_4==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_4==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_4==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_4==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_4==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_4==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_4==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_4==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_4==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_4==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_4==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_4);
						if ( s>=0 ) return s;
						break;

					case 37 : 
						int LA17_0 = input.LA(1);
						 
						int index17_0 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_0==ID) ) {s = 1;}
						else if ( (LA17_0==FTSWORD) ) {s = 2;}
						else if ( (LA17_0==FTSPRE) ) {s = 3;}
						else if ( (LA17_0==FTSWILD) ) {s = 4;}
						else if ( (LA17_0==NOT) ) {s = 5;}
						else if ( (LA17_0==TO) ) {s = 6;}
						else if ( (LA17_0==DECIMAL_INTEGER_LITERAL) ) {s = 7;}
						else if ( (LA17_0==FLOATING_POINT_LITERAL) ) {s = 8;}
						else if ( (LA17_0==DATETIME) ) {s = 9;}
						else if ( (LA17_0==STAR) ) {s = 10;}
						else if ( (LA17_0==URI) ) {s = 11;}
						else if ( (LA17_0==AT) ) {s = 12;}
						else if ( (LA17_0==OR) ) {s = 13;}
						else if ( (LA17_0==AND) ) {s = 14;}
						else if ( (LA17_0==FTSPHRASE) ) {s = 15;}
						else if ( (LA17_0==LSQUARE) && (synpred3_FTS())) {s = 16;}
						else if ( (LA17_0==LT) && (synpred3_FTS())) {s = 17;}
						else if ( (LA17_0==COMMA||LA17_0==DOT) && (synpred5_FTS())) {s = 18;}
						else if ( (LA17_0==QUESTION_MARK) && (synpred5_FTS())) {s = 19;}
						else if ( (LA17_0==EQUALS) && (synpred6_FTS())) {s = 20;}
						else if ( (LA17_0==TILDA) && (synpred7_FTS())) {s = 21;}
						else if ( (LA17_0==LPAREN) ) {s = 22;}
						else if ( (LA17_0==PERCENT) ) {s = 23;}
						 
						input.seek(index17_0);
						if ( s>=0 ) return s;
						break;

					case 38 : 
						int LA17_113 = input.LA(1);
						 
						int index17_113 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_113==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_113==COMMA||LA17_113==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_113==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_113==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_113==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_113==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_113==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_113==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_113==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_113==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_113==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_113==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_113==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_113==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_113==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_113==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_113==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_113==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_113==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_113==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_113==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_113==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_113==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_113==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_113==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_113==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_113==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_113==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_113==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_113==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_113==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_113==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_113);
						if ( s>=0 ) return s;
						break;

					case 39 : 
						int LA17_58 = input.LA(1);
						 
						int index17_58 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_58==DOT) ) {s = 117;}
						else if ( (LA17_58==STAR) ) {s = 25;}
						else if ( (LA17_58==COLON) ) {s = 118;}
						else if ( (LA17_58==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_58==COMMA) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_58==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_58==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_58==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_58==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_58==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_58==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_58==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_58==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_58==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_58==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_58==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_58==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_58==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_58==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_58==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_58==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_58==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_58==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_58==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_58==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_58==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_58==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_58==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_58==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_58==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_58==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_58==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_58==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_58==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_58);
						if ( s>=0 ) return s;
						break;

					case 40 : 
						int LA17_161 = input.LA(1);
						 
						int index17_161 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_161==DOT) ) {s = 195;}
						else if ( (LA17_161==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_161==COMMA) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_161==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_161==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_161==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_161==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_161==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_161==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_161==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_161==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_161==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_161==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_161==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_161==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_161==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_161==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_161==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_161==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_161==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_161==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_161==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_161==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_161==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_161==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_161==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_161==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_161==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_161==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_161==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_161==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_161==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_161==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_161);
						if ( s>=0 ) return s;
						break;

					case 41 : 
						int LA17_110 = input.LA(1);
						 
						int index17_110 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_110==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_110==COMMA||LA17_110==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_110==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_110==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_110==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_110==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_110==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_110==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_110==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_110==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_110==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_110==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_110==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_110==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_110==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_110==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_110==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_110==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_110==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_110==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_110==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_110==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_110==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_110==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_110==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_110==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_110==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_110==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_110==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_110==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_110==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_110==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_110);
						if ( s>=0 ) return s;
						break;

					case 42 : 
						int LA17_111 = input.LA(1);
						 
						int index17_111 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_111==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_111==COMMA||LA17_111==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_111==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_111==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_111==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_111==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_111==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_111==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_111==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_111==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_111==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_111==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_111==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_111==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_111==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_111==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_111==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_111==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_111==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_111==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_111==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_111==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_111==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_111==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_111==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_111==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_111==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_111==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_111==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_111==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_111==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_111==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_111);
						if ( s>=0 ) return s;
						break;

					case 43 : 
						int LA17_24 = input.LA(1);
						 
						int index17_24 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_24==ID) ) {s = 70;}
						else if ( (LA17_24==FTSWORD) && (synpred5_FTS())) {s = 71;}
						else if ( (LA17_24==FTSPRE) && (synpred5_FTS())) {s = 72;}
						else if ( (LA17_24==FTSWILD) && (synpred5_FTS())) {s = 73;}
						else if ( (LA17_24==NOT) && (synpred5_FTS())) {s = 74;}
						else if ( (LA17_24==TO) && (synpred5_FTS())) {s = 75;}
						else if ( (LA17_24==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 76;}
						else if ( (LA17_24==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 77;}
						else if ( (LA17_24==STAR) && (synpred5_FTS())) {s = 78;}
						else if ( (LA17_24==QUESTION_MARK) && (synpred5_FTS())) {s = 79;}
						else if ( (LA17_24==DATETIME) && (synpred5_FTS())) {s = 80;}
						else if ( (LA17_24==URI) && (synpred5_FTS())) {s = 81;}
						else if ( (LA17_24==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_24==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_24==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_24==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_24==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_24==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_24==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_24==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_24==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_24==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_24==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_24==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_24==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_24==COMMA||LA17_24==DOT) && (synpred5_FTS())) {s = 69;}
						else if ( (LA17_24==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_24==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_24==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_24==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_24==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_24);
						if ( s>=0 ) return s;
						break;

					case 44 : 
						int LA17_7 = input.LA(1);
						 
						int index17_7 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_7==STAR) ) {s = 25;}
						else if ( (LA17_7==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_7==COMMA||LA17_7==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_7==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_7==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_7==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_7==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_7==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_7==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_7==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_7==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_7==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_7==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_7==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_7==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_7==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_7==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_7==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_7==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_7==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_7==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_7==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_7==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_7==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_7==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_7==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_7==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_7==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_7==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_7==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_7==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_7==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_7);
						if ( s>=0 ) return s;
						break;

					case 45 : 
						int LA17_8 = input.LA(1);
						 
						int index17_8 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_8==STAR) ) {s = 25;}
						else if ( (LA17_8==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_8==COMMA||LA17_8==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_8==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_8==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_8==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_8==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_8==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_8==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_8==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_8==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_8==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_8==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_8==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_8==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_8==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_8==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_8==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_8==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_8==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_8==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_8==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_8==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_8==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_8==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_8==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_8==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_8==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_8==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_8==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_8==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_8==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_8);
						if ( s>=0 ) return s;
						break;

					case 46 : 
						int LA17_26 = input.LA(1);
						 
						int index17_26 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_26==LPAREN) && (synpred4_FTS())) {s = 100;}
						else if ( (LA17_26==ID) ) {s = 101;}
						else if ( (LA17_26==TO) ) {s = 102;}
						else if ( (LA17_26==OR) ) {s = 103;}
						else if ( (LA17_26==AND) ) {s = 104;}
						else if ( (LA17_26==NOT) ) {s = 105;}
						else if ( (LA17_26==FTSWORD) ) {s = 106;}
						else if ( (LA17_26==FTSPRE) ) {s = 107;}
						else if ( (LA17_26==FTSWILD) ) {s = 108;}
						else if ( (LA17_26==FTSPHRASE) ) {s = 109;}
						else if ( (LA17_26==DECIMAL_INTEGER_LITERAL) ) {s = 110;}
						else if ( (LA17_26==FLOATING_POINT_LITERAL) ) {s = 111;}
						else if ( (LA17_26==DATETIME) ) {s = 112;}
						else if ( (LA17_26==STAR) ) {s = 113;}
						else if ( (LA17_26==URI) ) {s = 114;}
						else if ( (LA17_26==LSQUARE) && (synpred3_FTS())) {s = 16;}
						else if ( (LA17_26==LT) && (synpred3_FTS())) {s = 17;}
						else if ( (LA17_26==COMMA||LA17_26==DOT) && (synpred5_FTS())) {s = 115;}
						else if ( (LA17_26==QUESTION_MARK) && (synpred5_FTS())) {s = 116;}
						 
						input.seek(index17_26);
						if ( s>=0 ) return s;
						break;

					case 47 : 
						int LA17_84 = input.LA(1);
						 
						int index17_84 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_84);
						if ( s>=0 ) return s;
						break;

					case 48 : 
						int LA17_83 = input.LA(1);
						 
						int index17_83 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_83);
						if ( s>=0 ) return s;
						break;

					case 49 : 
						int LA17_199 = input.LA(1);
						 
						int index17_199 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_FTS()) ) {s = 27;}
						else if ( (synpred5_FTS()) ) {s = 198;}
						 
						input.seek(index17_199);
						if ( s>=0 ) return s;
						break;

					case 50 : 
						int LA17_15 = input.LA(1);
						 
						int index17_15 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_15==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_15==TILDA) && (synpred5_FTS())) {s = 67;}
						else if ( (LA17_15==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_15==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_15==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_15==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_15==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_15==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_15==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_15==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_15==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_15==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_15==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_15==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_15==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_15==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_15==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_15==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_15==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_15==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_15==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_15==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_15==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_15==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_15==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_15==COMMA||LA17_15==DOT) && (synpred5_FTS())) {s = 69;}
						else if ( (LA17_15==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_15==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_15==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_15==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_15==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_15==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_15);
						if ( s>=0 ) return s;
						break;

					case 51 : 
						int LA17_10 = input.LA(1);
						 
						int index17_10 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_10==STAR) ) {s = 25;}
						else if ( (LA17_10==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_10==COMMA||LA17_10==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_10==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_10==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_10==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_10==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_10==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_10==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_10==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_10==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_10==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_10==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_10==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_10==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_10==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_10==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_10==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_10==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_10==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_10==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_10==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_10==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_10==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_10==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_10==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_10==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_10==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_10==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_10==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_10==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_10==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_10);
						if ( s>=0 ) return s;
						break;

					case 52 : 
						int LA17_164 = input.LA(1);
						 
						int index17_164 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_164==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_164==COMMA||LA17_164==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_164==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_164==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_164==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_164==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_164==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_164==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_164==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_164==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_164==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_164==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_164==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_164==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_164==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_164==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_164==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_164==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_164==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_164==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_164==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_164==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_164==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_164==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_164==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_164==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_164==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_164==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_164==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_164==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_164==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_164==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_164);
						if ( s>=0 ) return s;
						break;

					case 53 : 
						int LA17_85 = input.LA(1);
						 
						int index17_85 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_85);
						if ( s>=0 ) return s;
						break;

					case 54 : 
						int LA17_5 = input.LA(1);
						 
						int index17_5 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_5==STAR) ) {s = 25;}
						else if ( (LA17_5==COLON) ) {s = 26;}
						else if ( (LA17_5==COMMA||LA17_5==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_5==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_5==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_5==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_5==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_5==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_5==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_5==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_5==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_5==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_5==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_5==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_5==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_5==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_5==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_5==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_5==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_5==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_5==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_5==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_5==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_5==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_5==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_5==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_5==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_5==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_5==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_5==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_5==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_5==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_5);
						if ( s>=0 ) return s;
						break;

					case 55 : 
						int LA17_106 = input.LA(1);
						 
						int index17_106 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_106==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_106==COMMA||LA17_106==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_106==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_106==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_106==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_106==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_106==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_106==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_106==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_106==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_106==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_106==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_106==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_106==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_106==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_106==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_106==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_106==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_106==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_106==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_106==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_106==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_106==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_106==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_106==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_106==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_106==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_106==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_106==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_106==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_106==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_106==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_106);
						if ( s>=0 ) return s;
						break;

					case 56 : 
						int LA17_165 = input.LA(1);
						 
						int index17_165 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_165==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_165==COMMA||LA17_165==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_165==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_165==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_165==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_165==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_165==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_165==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_165==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_165==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_165==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_165==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_165==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_165==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_165==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_165==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_165==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_165==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_165==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_165==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_165==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_165==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_165==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_165==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_165==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_165==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_165==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_165==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_165==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_165==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_165==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_165==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_165);
						if ( s>=0 ) return s;
						break;

					case 57 : 
						int LA17_9 = input.LA(1);
						 
						int index17_9 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_9==STAR) ) {s = 25;}
						else if ( (LA17_9==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_9==COMMA||LA17_9==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_9==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_9==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_9==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_9==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_9==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_9==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_9==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_9==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_9==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_9==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_9==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_9==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_9==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_9==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_9==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_9==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_9==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_9==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_9==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_9==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_9==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_9==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_9==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_9==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_9==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_9==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_9==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_9==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_9==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_9);
						if ( s>=0 ) return s;
						break;

					case 58 : 
						int LA17_107 = input.LA(1);
						 
						int index17_107 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_107==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_107==COMMA||LA17_107==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_107==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_107==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_107==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_107==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_107==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_107==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_107==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_107==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_107==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_107==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_107==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_107==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_107==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_107==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_107==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_107==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_107==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_107==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_107==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_107==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_107==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_107==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_107==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_107==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_107==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_107==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_107==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_107==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_107==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_107==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_107);
						if ( s>=0 ) return s;
						break;

					case 59 : 
						int LA17_1 = input.LA(1);
						 
						int index17_1 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_1==DOT) ) {s = 24;}
						else if ( (LA17_1==STAR) ) {s = 25;}
						else if ( (LA17_1==COLON) ) {s = 26;}
						else if ( (LA17_1==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_1==COMMA) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_1==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_1==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_1==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_1==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_1==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_1==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_1==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_1==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_1==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_1==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_1==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_1==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_1==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_1==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_1==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_1==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_1==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_1==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_1==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_1==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_1==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_1==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_1==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_1==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_1==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_1==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_1==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_1==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_1==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_1);
						if ( s>=0 ) return s;
						break;

					case 60 : 
						int LA17_167 = input.LA(1);
						 
						int index17_167 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_167==DOTDOT) && (synpred3_FTS())) {s = 27;}
						else if ( (LA17_167==COMMA||LA17_167==DOT) && (synpred5_FTS())) {s = 158;}
						else if ( (LA17_167==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_167==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_167==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_167==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_167==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_167==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_167==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_167==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_167==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_167==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_167==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_167==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_167==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_167==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_167==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_167==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_167==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_167==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_167==STAR) && (synpred5_FTS())) {s = 68;}
						else if ( (LA17_167==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_167==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_167==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_167==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_167==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_167==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_167==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_167==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_167==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_167==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_167==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_167);
						if ( s>=0 ) return s;
						break;

					case 61 : 
						int LA17_87 = input.LA(1);
						 
						int index17_87 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_FTS()) ) {s = 124;}
						else if ( (synpred5_FTS()) ) {s = 151;}
						 
						input.seek(index17_87);
						if ( s>=0 ) return s;
						break;

					case 62 : 
						int LA17_157 = input.LA(1);
						 
						int index17_157 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_157==ID) ) {s = 183;}
						else if ( (LA17_157==FTSWORD) && (synpred5_FTS())) {s = 184;}
						else if ( (LA17_157==FTSPRE) && (synpred5_FTS())) {s = 185;}
						else if ( (LA17_157==FTSWILD) && (synpred5_FTS())) {s = 186;}
						else if ( (LA17_157==NOT) && (synpred5_FTS())) {s = 187;}
						else if ( (LA17_157==TO) && (synpred5_FTS())) {s = 188;}
						else if ( (LA17_157==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 189;}
						else if ( (LA17_157==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 190;}
						else if ( (LA17_157==STAR) && (synpred5_FTS())) {s = 191;}
						else if ( (LA17_157==QUESTION_MARK) && (synpred5_FTS())) {s = 192;}
						else if ( (LA17_157==DATETIME) && (synpred5_FTS())) {s = 193;}
						else if ( (LA17_157==URI) && (synpred5_FTS())) {s = 194;}
						else if ( (LA17_157==TILDA) && (synpred5_FTS())) {s = 159;}
						else if ( (LA17_157==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_157==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_157==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_157==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_157==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_157==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_157==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_157==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_157==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_157==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_157==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_157==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_157==COMMA||LA17_157==DOT) && (synpred5_FTS())) {s = 69;}
						else if ( (LA17_157==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_157==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_157==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_157==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_157==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_157);
						if ( s>=0 ) return s;
						break;

					case 63 : 
						int LA17_6 = input.LA(1);
						 
						int index17_6 = input.index();
						input.rewind();
						s = -1;
						if ( (LA17_6==STAR) ) {s = 25;}
						else if ( (LA17_6==COLON) ) {s = 26;}
						else if ( (LA17_6==COMMA||LA17_6==DOT) && (synpred5_FTS())) {s = 28;}
						else if ( (LA17_6==TILDA) && (synpred5_FTS())) {s = 29;}
						else if ( (LA17_6==CARAT) && (synpred5_FTS())) {s = 30;}
						else if ( (LA17_6==AND) && (synpred5_FTS())) {s = 31;}
						else if ( (LA17_6==AMP) && (synpred5_FTS())) {s = 32;}
						else if ( (LA17_6==EOF) && (synpred5_FTS())) {s = 33;}
						else if ( (LA17_6==RPAREN) && (synpred5_FTS())) {s = 34;}
						else if ( (LA17_6==OR) && (synpred5_FTS())) {s = 35;}
						else if ( (LA17_6==BAR) && (synpred5_FTS())) {s = 36;}
						else if ( (LA17_6==NOT) && (synpred5_FTS())) {s = 37;}
						else if ( (LA17_6==ID) && (synpred5_FTS())) {s = 38;}
						else if ( (LA17_6==FTSWORD) && (synpred5_FTS())) {s = 39;}
						else if ( (LA17_6==FTSPRE) && (synpred5_FTS())) {s = 40;}
						else if ( (LA17_6==FTSWILD) && (synpred5_FTS())) {s = 41;}
						else if ( (LA17_6==EXCLAMATION) && (synpred5_FTS())) {s = 42;}
						else if ( (LA17_6==TO) && (synpred5_FTS())) {s = 43;}
						else if ( (LA17_6==DECIMAL_INTEGER_LITERAL) && (synpred5_FTS())) {s = 44;}
						else if ( (LA17_6==FLOATING_POINT_LITERAL) && (synpred5_FTS())) {s = 45;}
						else if ( (LA17_6==DATETIME) && (synpred5_FTS())) {s = 46;}
						else if ( (LA17_6==URI) && (synpred5_FTS())) {s = 47;}
						else if ( (LA17_6==AT) && (synpred5_FTS())) {s = 48;}
						else if ( (LA17_6==FTSPHRASE) && (synpred5_FTS())) {s = 49;}
						else if ( (LA17_6==LSQUARE) && (synpred5_FTS())) {s = 50;}
						else if ( (LA17_6==LT) && (synpred5_FTS())) {s = 51;}
						else if ( (LA17_6==QUESTION_MARK) && (synpred5_FTS())) {s = 52;}
						else if ( (LA17_6==EQUALS) && (synpred5_FTS())) {s = 53;}
						else if ( (LA17_6==LPAREN) && (synpred5_FTS())) {s = 54;}
						else if ( (LA17_6==PERCENT) && (synpred5_FTS())) {s = 55;}
						else if ( (LA17_6==PLUS) && (synpred5_FTS())) {s = 56;}
						else if ( (LA17_6==MINUS) && (synpred5_FTS())) {s = 57;}
						 
						input.seek(index17_6);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 17, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	static final String DFA33_eotS =
		"\126\uffff";
	static final String DFA33_eofS =
		"\2\uffff\2\11\2\uffff\1\11\3\uffff\1\11\1\uffff\7\11\2\uffff\1\11\100"+
		"\uffff";
	static final String DFA33_minS =
		"\1\5\1\uffff\2\4\2\uffff\1\4\1\5\2\uffff\1\4\1\uffff\7\4\1\uffff\1\5\1"+
		"\4\2\0\2\12\2\0\1\15\10\0\2\5\1\13\13\0\1\5\42\0";
	static final String DFA33_maxS =
		"\1\150\1\uffff\2\150\2\uffff\1\150\1\146\2\uffff\1\150\1\uffff\7\150\1"+
		"\uffff\2\150\2\0\2\12\2\0\1\150\10\0\1\146\2\150\13\0\1\146\42\0";
	static final String DFA33_acceptS =
		"\1\uffff\1\1\2\uffff\2\1\2\uffff\1\2\1\3\1\uffff\1\1\7\uffff\1\1\102\uffff";
	static final String DFA33_specialS =
		"\1\46\1\uffff\1\61\1\74\2\uffff\1\75\5\uffff\1\24\1\7\1\60\1\77\1\42\5"+
		"\uffff\1\22\1\15\2\uffff\1\55\1\33\1\uffff\1\62\1\21\1\70\1\51\1\12\1"+
		"\54\1\3\1\37\3\uffff\1\100\1\23\1\40\1\4\1\27\1\0\1\43\1\13\1\71\1\25"+
		"\1\64\1\uffff\1\30\1\73\1\26\1\44\1\5\1\32\1\72\1\57\1\20\1\67\1\14\1"+
		"\45\1\50\1\11\1\53\1\2\1\36\1\31\1\101\1\56\1\17\1\66\1\16\1\63\1\47\1"+
		"\10\1\52\1\1\1\35\1\65\1\34\1\6\1\41\1\76}>";
	static final String[] DFA33_transitionS = {
			"\1\5\1\1\4\uffff\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11\31\uffff"+
			"\1\11\2\uffff\1\10\3\11\11\uffff\1\2\16\uffff\1\6\2\uffff\1\4\6\uffff"+
			"\1\11\7\uffff\1\11\5\uffff\1\3\1\uffff\1\7",
			"",
			"\4\11\1\uffff\1\11\1\13\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\12"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\13\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"",
			"",
			"\4\11\1\uffff\1\11\1\13\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\1\17\71\uffff\1\14\16\uffff\1\20\2\uffff\1\16\24\uffff\1\15",
			"",
			"",
			"\4\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1"+
			"\11\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\21"+
			"\3\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\22"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\24\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\2\11"+
			"\1\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1"+
			"\11\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\25"+
			"\3\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"",
			"\1\31\5\uffff\1\34\1\uffff\1\44\1\uffff\1\40\5\uffff\1\34\31\uffff\1"+
			"\41\2\uffff\1\33\1\36\1\37\1\35\11\uffff\1\26\3\uffff\3\11\10\uffff\1"+
			"\32\2\uffff\1\30\6\uffff\1\43\7\uffff\1\42\5\uffff\1\27\1\uffff\1\45",
			"\4\11\1\uffff\1\11\1\46\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\2\11"+
			"\1\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\1\uffff",
			"\1\uffff",
			"\1\47",
			"\1\47",
			"\1\uffff",
			"\1\uffff",
			"\1\62\1\uffff\1\56\37\uffff\1\57\3\uffff\1\52\1\53\1\51\11\uffff\1\50"+
			"\16\uffff\1\54\11\uffff\1\61\7\uffff\1\60\5\uffff\1\55\1\uffff\1\63",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\67\71\uffff\1\64\16\uffff\1\70\2\uffff\1\66\24\uffff\1\65",
			"\1\11\5\uffff\1\34\1\uffff\1\104\1\uffff\1\100\5\uffff\1\34\31\uffff"+
			"\1\101\2\uffff\1\71\1\74\1\75\1\73\11\uffff\1\72\3\uffff\3\11\10\uffff"+
			"\1\76\2\uffff\1\11\6\uffff\1\103\7\uffff\1\102\5\uffff\1\77\1\uffff\1"+
			"\45",
			"\1\34\1\uffff\1\120\1\uffff\1\114\5\uffff\1\34\31\uffff\1\115\2\uffff"+
			"\1\105\1\110\1\111\1\107\11\uffff\1\106\3\uffff\3\11\10\uffff\1\112\11"+
			"\uffff\1\117\7\uffff\1\116\5\uffff\1\113\1\uffff\1\45",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\124\71\uffff\1\121\16\uffff\1\125\2\uffff\1\123\24\uffff\1\122",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff"
	};

	static final short[] DFA33_eot = DFA.unpackEncodedString(DFA33_eotS);
	static final short[] DFA33_eof = DFA.unpackEncodedString(DFA33_eofS);
	static final char[] DFA33_min = DFA.unpackEncodedStringToUnsignedChars(DFA33_minS);
	static final char[] DFA33_max = DFA.unpackEncodedStringToUnsignedChars(DFA33_maxS);
	static final short[] DFA33_accept = DFA.unpackEncodedString(DFA33_acceptS);
	static final short[] DFA33_special = DFA.unpackEncodedString(DFA33_specialS);
	static final short[][] DFA33_transition;

	static {
		int numStates = DFA33_transitionS.length;
		DFA33_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA33_transition[i] = DFA.unpackEncodedString(DFA33_transitionS[i]);
		}
	}

	protected class DFA33 extends DFA {

		public DFA33(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 33;
			this.eot = DFA33_eot;
			this.eof = DFA33_eof;
			this.min = DFA33_min;
			this.max = DFA33_max;
			this.accept = DFA33_accept;
			this.special = DFA33_special;
			this.transition = DFA33_transition;
		}
		@Override
		public String getDescription() {
			return "534:9: ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( EXACT_PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( EXACT_TERM ftsWord ( fuzzy )? ) )";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA33_45 = input.LA(1);
						 
						int index33_45 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_45);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA33_79 = input.LA(1);
						 
						int index33_79 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_79);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA33_67 = input.LA(1);
						 
						int index33_67 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_67);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA33_35 = input.LA(1);
						 
						int index33_35 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_35);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA33_43 = input.LA(1);
						 
						int index33_43 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_43);
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA33_56 = input.LA(1);
						 
						int index33_56 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_56);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA33_83 = input.LA(1);
						 
						int index33_83 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_83);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA33_13 = input.LA(1);
						 
						int index33_13 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_13==COLON) && (synpred13_FTS())) {s = 19;}
						else if ( (LA33_13==EOF||(LA33_13 >= AMP && LA33_13 <= BAR)||LA33_13==CARAT||LA33_13==COMMA||LA33_13==DATETIME||LA33_13==DECIMAL_INTEGER_LITERAL||LA33_13==DOT||LA33_13==EQUALS||LA33_13==EXCLAMATION||LA33_13==FLOATING_POINT_LITERAL||(LA33_13 >= FTSPHRASE && LA33_13 <= FTSWORD)||LA33_13==ID||(LA33_13 >= LPAREN && LA33_13 <= LT)||LA33_13==MINUS||LA33_13==NOT||(LA33_13 >= OR && LA33_13 <= PERCENT)||LA33_13==PLUS||LA33_13==QUESTION_MARK||LA33_13==RPAREN||LA33_13==STAR||(LA33_13 >= TILDA && LA33_13 <= TO)||LA33_13==URI) ) {s = 9;}
						 
						input.seek(index33_13);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA33_77 = input.LA(1);
						 
						int index33_77 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_77);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA33_65 = input.LA(1);
						 
						int index33_65 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_65);
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA33_33 = input.LA(1);
						 
						int index33_33 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_33);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA33_47 = input.LA(1);
						 
						int index33_47 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_47);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA33_62 = input.LA(1);
						 
						int index33_62 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_62);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA33_23 = input.LA(1);
						 
						int index33_23 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_23);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA33_74 = input.LA(1);
						 
						int index33_74 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_74);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA33_72 = input.LA(1);
						 
						int index33_72 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_72);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA33_60 = input.LA(1);
						 
						int index33_60 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_60);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA33_30 = input.LA(1);
						 
						int index33_30 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_30);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA33_22 = input.LA(1);
						 
						int index33_22 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_22);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA33_41 = input.LA(1);
						 
						int index33_41 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_41);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA33_12 = input.LA(1);
						 
						int index33_12 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_12==DOT) ) {s = 18;}
						else if ( (LA33_12==COLON) && (synpred13_FTS())) {s = 19;}
						else if ( (LA33_12==EOF||(LA33_12 >= AMP && LA33_12 <= BAR)||LA33_12==CARAT||LA33_12==COMMA||LA33_12==DATETIME||LA33_12==DECIMAL_INTEGER_LITERAL||LA33_12==EQUALS||LA33_12==EXCLAMATION||LA33_12==FLOATING_POINT_LITERAL||(LA33_12 >= FTSPHRASE && LA33_12 <= FTSWORD)||LA33_12==ID||(LA33_12 >= LPAREN && LA33_12 <= LT)||LA33_12==MINUS||LA33_12==NOT||(LA33_12 >= OR && LA33_12 <= PERCENT)||LA33_12==PLUS||LA33_12==QUESTION_MARK||LA33_12==RPAREN||LA33_12==STAR||(LA33_12 >= TILDA && LA33_12 <= TO)||LA33_12==URI) ) {s = 9;}
						 
						input.seek(index33_12);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA33_49 = input.LA(1);
						 
						int index33_49 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_49);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA33_54 = input.LA(1);
						 
						int index33_54 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_54);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA33_44 = input.LA(1);
						 
						int index33_44 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_44);
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA33_52 = input.LA(1);
						 
						int index33_52 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_52);
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA33_69 = input.LA(1);
						 
						int index33_69 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_69);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA33_57 = input.LA(1);
						 
						int index33_57 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_57);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA33_27 = input.LA(1);
						 
						int index33_27 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_27);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA33_82 = input.LA(1);
						 
						int index33_82 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_82);
						if ( s>=0 ) return s;
						break;

					case 29 : 
						int LA33_80 = input.LA(1);
						 
						int index33_80 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_80);
						if ( s>=0 ) return s;
						break;

					case 30 : 
						int LA33_68 = input.LA(1);
						 
						int index33_68 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_68);
						if ( s>=0 ) return s;
						break;

					case 31 : 
						int LA33_36 = input.LA(1);
						 
						int index33_36 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_36);
						if ( s>=0 ) return s;
						break;

					case 32 : 
						int LA33_42 = input.LA(1);
						 
						int index33_42 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_42);
						if ( s>=0 ) return s;
						break;

					case 33 : 
						int LA33_84 = input.LA(1);
						 
						int index33_84 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_84);
						if ( s>=0 ) return s;
						break;

					case 34 : 
						int LA33_16 = input.LA(1);
						 
						int index33_16 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_16==COLON) && (synpred13_FTS())) {s = 19;}
						else if ( (LA33_16==EOF||(LA33_16 >= AMP && LA33_16 <= BAR)||LA33_16==CARAT||LA33_16==COMMA||LA33_16==DATETIME||LA33_16==DECIMAL_INTEGER_LITERAL||LA33_16==DOT||LA33_16==EQUALS||LA33_16==EXCLAMATION||LA33_16==FLOATING_POINT_LITERAL||(LA33_16 >= FTSPHRASE && LA33_16 <= FTSWORD)||LA33_16==ID||(LA33_16 >= LPAREN && LA33_16 <= LT)||LA33_16==MINUS||LA33_16==NOT||(LA33_16 >= OR && LA33_16 <= PERCENT)||LA33_16==PLUS||LA33_16==QUESTION_MARK||LA33_16==RPAREN||LA33_16==STAR||(LA33_16 >= TILDA && LA33_16 <= TO)||LA33_16==URI) ) {s = 9;}
						 
						input.seek(index33_16);
						if ( s>=0 ) return s;
						break;

					case 35 : 
						int LA33_46 = input.LA(1);
						 
						int index33_46 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_46);
						if ( s>=0 ) return s;
						break;

					case 36 : 
						int LA33_55 = input.LA(1);
						 
						int index33_55 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_55);
						if ( s>=0 ) return s;
						break;

					case 37 : 
						int LA33_63 = input.LA(1);
						 
						int index33_63 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_63);
						if ( s>=0 ) return s;
						break;

					case 38 : 
						int LA33_0 = input.LA(1);
						 
						int index33_0 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_0==AT) && (synpred13_FTS())) {s = 1;}
						else if ( (LA33_0==ID) ) {s = 2;}
						else if ( (LA33_0==TO) ) {s = 3;}
						else if ( (LA33_0==OR) && (synpred13_FTS())) {s = 4;}
						else if ( (LA33_0==AND) && (synpred13_FTS())) {s = 5;}
						else if ( (LA33_0==NOT) ) {s = 6;}
						else if ( (LA33_0==URI) ) {s = 7;}
						else if ( (LA33_0==FTSPHRASE) ) {s = 8;}
						else if ( (LA33_0==COMMA||LA33_0==DATETIME||LA33_0==DECIMAL_INTEGER_LITERAL||LA33_0==DOT||LA33_0==FLOATING_POINT_LITERAL||(LA33_0 >= FTSPRE && LA33_0 <= FTSWORD)||LA33_0==QUESTION_MARK||LA33_0==STAR) ) {s = 9;}
						 
						input.seek(index33_0);
						if ( s>=0 ) return s;
						break;

					case 39 : 
						int LA33_76 = input.LA(1);
						 
						int index33_76 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_76);
						if ( s>=0 ) return s;
						break;

					case 40 : 
						int LA33_64 = input.LA(1);
						 
						int index33_64 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_64);
						if ( s>=0 ) return s;
						break;

					case 41 : 
						int LA33_32 = input.LA(1);
						 
						int index33_32 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_32);
						if ( s>=0 ) return s;
						break;

					case 42 : 
						int LA33_78 = input.LA(1);
						 
						int index33_78 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_78);
						if ( s>=0 ) return s;
						break;

					case 43 : 
						int LA33_66 = input.LA(1);
						 
						int index33_66 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_66);
						if ( s>=0 ) return s;
						break;

					case 44 : 
						int LA33_34 = input.LA(1);
						 
						int index33_34 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_34);
						if ( s>=0 ) return s;
						break;

					case 45 : 
						int LA33_26 = input.LA(1);
						 
						int index33_26 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_26);
						if ( s>=0 ) return s;
						break;

					case 46 : 
						int LA33_71 = input.LA(1);
						 
						int index33_71 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_71);
						if ( s>=0 ) return s;
						break;

					case 47 : 
						int LA33_59 = input.LA(1);
						 
						int index33_59 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_59);
						if ( s>=0 ) return s;
						break;

					case 48 : 
						int LA33_14 = input.LA(1);
						 
						int index33_14 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_14==COLON) && (synpred13_FTS())) {s = 19;}
						else if ( (LA33_14==EOF||(LA33_14 >= AMP && LA33_14 <= BAR)||LA33_14==CARAT||LA33_14==COMMA||LA33_14==DATETIME||LA33_14==DECIMAL_INTEGER_LITERAL||LA33_14==DOT||LA33_14==EQUALS||LA33_14==EXCLAMATION||LA33_14==FLOATING_POINT_LITERAL||(LA33_14 >= FTSPHRASE && LA33_14 <= FTSWORD)||LA33_14==ID||(LA33_14 >= LPAREN && LA33_14 <= LT)||LA33_14==MINUS||LA33_14==NOT||(LA33_14 >= OR && LA33_14 <= PERCENT)||LA33_14==PLUS||LA33_14==QUESTION_MARK||LA33_14==RPAREN||LA33_14==STAR||(LA33_14 >= TILDA && LA33_14 <= TO)||LA33_14==URI) ) {s = 9;}
						 
						input.seek(index33_14);
						if ( s>=0 ) return s;
						break;

					case 49 : 
						int LA33_2 = input.LA(1);
						 
						int index33_2 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_2==DOT) ) {s = 10;}
						else if ( (LA33_2==COLON) && (synpred13_FTS())) {s = 11;}
						else if ( (LA33_2==EOF||(LA33_2 >= AMP && LA33_2 <= BAR)||LA33_2==CARAT||LA33_2==COMMA||LA33_2==DATETIME||LA33_2==DECIMAL_INTEGER_LITERAL||LA33_2==EQUALS||LA33_2==EXCLAMATION||LA33_2==FLOATING_POINT_LITERAL||(LA33_2 >= FTSPHRASE && LA33_2 <= FTSWORD)||LA33_2==ID||(LA33_2 >= LPAREN && LA33_2 <= LT)||LA33_2==MINUS||LA33_2==NOT||(LA33_2 >= OR && LA33_2 <= PERCENT)||LA33_2==PLUS||LA33_2==QUESTION_MARK||LA33_2==RPAREN||LA33_2==STAR||(LA33_2 >= TILDA && LA33_2 <= TO)||LA33_2==URI) ) {s = 9;}
						 
						input.seek(index33_2);
						if ( s>=0 ) return s;
						break;

					case 50 : 
						int LA33_29 = input.LA(1);
						 
						int index33_29 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_29);
						if ( s>=0 ) return s;
						break;

					case 51 : 
						int LA33_75 = input.LA(1);
						 
						int index33_75 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_75);
						if ( s>=0 ) return s;
						break;

					case 52 : 
						int LA33_50 = input.LA(1);
						 
						int index33_50 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_50);
						if ( s>=0 ) return s;
						break;

					case 53 : 
						int LA33_81 = input.LA(1);
						 
						int index33_81 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_81);
						if ( s>=0 ) return s;
						break;

					case 54 : 
						int LA33_73 = input.LA(1);
						 
						int index33_73 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_73);
						if ( s>=0 ) return s;
						break;

					case 55 : 
						int LA33_61 = input.LA(1);
						 
						int index33_61 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_61);
						if ( s>=0 ) return s;
						break;

					case 56 : 
						int LA33_31 = input.LA(1);
						 
						int index33_31 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_31);
						if ( s>=0 ) return s;
						break;

					case 57 : 
						int LA33_48 = input.LA(1);
						 
						int index33_48 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_48);
						if ( s>=0 ) return s;
						break;

					case 58 : 
						int LA33_58 = input.LA(1);
						 
						int index33_58 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_58);
						if ( s>=0 ) return s;
						break;

					case 59 : 
						int LA33_53 = input.LA(1);
						 
						int index33_53 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_53);
						if ( s>=0 ) return s;
						break;

					case 60 : 
						int LA33_3 = input.LA(1);
						 
						int index33_3 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_3==COLON) && (synpred13_FTS())) {s = 11;}
						else if ( (LA33_3==EOF||(LA33_3 >= AMP && LA33_3 <= BAR)||LA33_3==CARAT||LA33_3==COMMA||LA33_3==DATETIME||LA33_3==DECIMAL_INTEGER_LITERAL||LA33_3==DOT||LA33_3==EQUALS||LA33_3==EXCLAMATION||LA33_3==FLOATING_POINT_LITERAL||(LA33_3 >= FTSPHRASE && LA33_3 <= FTSWORD)||LA33_3==ID||(LA33_3 >= LPAREN && LA33_3 <= LT)||LA33_3==MINUS||LA33_3==NOT||(LA33_3 >= OR && LA33_3 <= PERCENT)||LA33_3==PLUS||LA33_3==QUESTION_MARK||LA33_3==RPAREN||LA33_3==STAR||(LA33_3 >= TILDA && LA33_3 <= TO)||LA33_3==URI) ) {s = 9;}
						 
						input.seek(index33_3);
						if ( s>=0 ) return s;
						break;

					case 61 : 
						int LA33_6 = input.LA(1);
						 
						int index33_6 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_6==COLON) && (synpred13_FTS())) {s = 11;}
						else if ( (LA33_6==EOF||(LA33_6 >= AMP && LA33_6 <= BAR)||LA33_6==CARAT||LA33_6==COMMA||LA33_6==DATETIME||LA33_6==DECIMAL_INTEGER_LITERAL||LA33_6==DOT||LA33_6==EQUALS||LA33_6==EXCLAMATION||LA33_6==FLOATING_POINT_LITERAL||(LA33_6 >= FTSPHRASE && LA33_6 <= FTSWORD)||LA33_6==ID||(LA33_6 >= LPAREN && LA33_6 <= LT)||LA33_6==MINUS||LA33_6==NOT||(LA33_6 >= OR && LA33_6 <= PERCENT)||LA33_6==PLUS||LA33_6==QUESTION_MARK||LA33_6==RPAREN||LA33_6==STAR||(LA33_6 >= TILDA && LA33_6 <= TO)||LA33_6==URI) ) {s = 9;}
						 
						input.seek(index33_6);
						if ( s>=0 ) return s;
						break;

					case 62 : 
						int LA33_85 = input.LA(1);
						 
						int index33_85 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_85);
						if ( s>=0 ) return s;
						break;

					case 63 : 
						int LA33_15 = input.LA(1);
						 
						int index33_15 = input.index();
						input.rewind();
						s = -1;
						if ( (LA33_15==COLON) && (synpred13_FTS())) {s = 19;}
						else if ( (LA33_15==EOF||(LA33_15 >= AMP && LA33_15 <= BAR)||LA33_15==CARAT||LA33_15==COMMA||LA33_15==DATETIME||LA33_15==DECIMAL_INTEGER_LITERAL||LA33_15==DOT||LA33_15==EQUALS||LA33_15==EXCLAMATION||LA33_15==FLOATING_POINT_LITERAL||(LA33_15 >= FTSPHRASE && LA33_15 <= FTSWORD)||LA33_15==ID||(LA33_15 >= LPAREN && LA33_15 <= LT)||LA33_15==MINUS||LA33_15==NOT||(LA33_15 >= OR && LA33_15 <= PERCENT)||LA33_15==PLUS||LA33_15==QUESTION_MARK||LA33_15==RPAREN||LA33_15==STAR||(LA33_15 >= TILDA && LA33_15 <= TO)||LA33_15==URI) ) {s = 9;}
						 
						input.seek(index33_15);
						if ( s>=0 ) return s;
						break;

					case 64 : 
						int LA33_40 = input.LA(1);
						 
						int index33_40 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_40);
						if ( s>=0 ) return s;
						break;

					case 65 : 
						int LA33_70 = input.LA(1);
						 
						int index33_70 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred13_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index33_70);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 33, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	static final String DFA39_eotS =
		"\126\uffff";
	static final String DFA39_eofS =
		"\2\uffff\2\11\2\uffff\1\11\3\uffff\1\11\1\uffff\7\11\2\uffff\1\11\100"+
		"\uffff";
	static final String DFA39_minS =
		"\1\5\1\uffff\2\4\2\uffff\1\4\1\5\2\uffff\1\4\1\uffff\7\4\1\uffff\1\5\1"+
		"\4\2\0\2\12\2\0\1\15\10\0\2\5\1\13\13\0\1\5\42\0";
	static final String DFA39_maxS =
		"\1\150\1\uffff\2\150\2\uffff\1\150\1\146\2\uffff\1\150\1\uffff\7\150\1"+
		"\uffff\2\150\2\0\2\12\2\0\1\150\10\0\1\146\2\150\13\0\1\146\42\0";
	static final String DFA39_acceptS =
		"\1\uffff\1\1\2\uffff\2\1\2\uffff\1\2\1\3\1\uffff\1\1\7\uffff\1\1\102\uffff";
	static final String DFA39_specialS =
		"\1\44\1\uffff\1\14\1\6\2\uffff\1\24\5\uffff\1\42\1\43\1\53\1\1\1\11\5"+
		"\uffff\1\10\1\101\2\uffff\1\71\1\66\1\uffff\1\52\1\56\1\20\1\63\1\5\1"+
		"\47\1\77\1\27\3\uffff\1\32\1\57\1\21\1\12\1\40\1\33\1\35\1\0\1\41\1\60"+
		"\1\31\1\uffff\1\67\1\13\1\100\1\30\1\73\1\65\1\2\1\51\1\55\1\17\1\70\1"+
		"\34\1\62\1\4\1\46\1\76\1\26\1\64\1\23\1\50\1\54\1\16\1\72\1\22\1\61\1"+
		"\3\1\45\1\75\1\25\1\15\1\74\1\37\1\7\1\36}>";
	static final String[] DFA39_transitionS = {
			"\1\5\1\1\4\uffff\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11\31\uffff"+
			"\1\11\2\uffff\1\10\3\11\11\uffff\1\2\16\uffff\1\6\2\uffff\1\4\6\uffff"+
			"\1\11\7\uffff\1\11\5\uffff\1\3\1\uffff\1\7",
			"",
			"\4\11\1\uffff\1\11\1\13\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\12"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\13\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"",
			"",
			"\4\11\1\uffff\1\11\1\13\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\1\17\71\uffff\1\14\16\uffff\1\20\2\uffff\1\16\24\uffff\1\15",
			"",
			"",
			"\4\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1"+
			"\11\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\21"+
			"\3\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\22"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\23\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1\11"+
			"\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\24\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\2\11"+
			"\1\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\4\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\1"+
			"\11\2\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\25"+
			"\3\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"",
			"\1\31\5\uffff\1\34\1\uffff\1\44\1\uffff\1\40\5\uffff\1\34\31\uffff\1"+
			"\41\2\uffff\1\33\1\36\1\37\1\35\11\uffff\1\26\3\uffff\3\11\10\uffff\1"+
			"\32\2\uffff\1\30\6\uffff\1\43\7\uffff\1\42\5\uffff\1\27\1\uffff\1\45",
			"\4\11\1\uffff\1\11\1\46\1\11\1\uffff\1\11\1\uffff\1\11\5\uffff\2\11"+
			"\1\uffff\1\11\2\uffff\1\11\23\uffff\1\11\2\uffff\4\11\11\uffff\1\11\3"+
			"\uffff\3\11\2\uffff\1\11\5\uffff\1\11\2\uffff\2\11\1\uffff\1\11\3\uffff"+
			"\1\11\2\uffff\1\11\4\uffff\1\11\4\uffff\2\11\1\uffff\1\11",
			"\1\uffff",
			"\1\uffff",
			"\1\47",
			"\1\47",
			"\1\uffff",
			"\1\uffff",
			"\1\62\1\uffff\1\56\37\uffff\1\57\3\uffff\1\52\1\53\1\51\11\uffff\1\50"+
			"\16\uffff\1\54\11\uffff\1\61\7\uffff\1\60\5\uffff\1\55\1\uffff\1\63",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\67\71\uffff\1\64\16\uffff\1\70\2\uffff\1\66\24\uffff\1\65",
			"\1\11\5\uffff\1\34\1\uffff\1\104\1\uffff\1\100\5\uffff\1\34\31\uffff"+
			"\1\101\2\uffff\1\71\1\74\1\75\1\73\11\uffff\1\72\3\uffff\3\11\10\uffff"+
			"\1\76\2\uffff\1\11\6\uffff\1\103\7\uffff\1\102\5\uffff\1\77\1\uffff\1"+
			"\45",
			"\1\34\1\uffff\1\120\1\uffff\1\114\5\uffff\1\34\31\uffff\1\115\2\uffff"+
			"\1\105\1\110\1\111\1\107\11\uffff\1\106\3\uffff\3\11\10\uffff\1\112\11"+
			"\uffff\1\117\7\uffff\1\116\5\uffff\1\113\1\uffff\1\45",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\124\71\uffff\1\121\16\uffff\1\125\2\uffff\1\123\24\uffff\1\122",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff"
	};

	static final short[] DFA39_eot = DFA.unpackEncodedString(DFA39_eotS);
	static final short[] DFA39_eof = DFA.unpackEncodedString(DFA39_eofS);
	static final char[] DFA39_min = DFA.unpackEncodedStringToUnsignedChars(DFA39_minS);
	static final char[] DFA39_max = DFA.unpackEncodedStringToUnsignedChars(DFA39_maxS);
	static final short[] DFA39_accept = DFA.unpackEncodedString(DFA39_acceptS);
	static final short[] DFA39_special = DFA.unpackEncodedString(DFA39_specialS);
	static final short[][] DFA39_transition;

	static {
		int numStates = DFA39_transitionS.length;
		DFA39_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA39_transition[i] = DFA.unpackEncodedString(DFA39_transitionS[i]);
		}
	}

	protected class DFA39 extends DFA {

		public DFA39(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 39;
			this.eot = DFA39_eot;
			this.eof = DFA39_eof;
			this.min = DFA39_min;
			this.max = DFA39_max;
			this.accept = DFA39_accept;
			this.special = DFA39_special;
			this.transition = DFA39_transition;
		}
		@Override
		public String getDescription() {
			return "556:9: ( ( fieldReference COLON )=> fieldReference COLON ( FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE fieldReference ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord fieldReference ( fuzzy )? ) ) | FTSPHRASE ( ( slop )=> slop )? -> ^( PHRASE FTSPHRASE ( slop )? ) | ftsWord ( ( fuzzy )=> fuzzy )? -> ^( TERM ftsWord ( fuzzy )? ) )";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA39_47 = input.LA(1);
						 
						int index39_47 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_47);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA39_15 = input.LA(1);
						 
						int index39_15 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_15==COLON) && (synpred18_FTS())) {s = 19;}
						else if ( (LA39_15==EOF||(LA39_15 >= AMP && LA39_15 <= BAR)||LA39_15==CARAT||LA39_15==COMMA||LA39_15==DATETIME||LA39_15==DECIMAL_INTEGER_LITERAL||LA39_15==DOT||LA39_15==EQUALS||LA39_15==EXCLAMATION||LA39_15==FLOATING_POINT_LITERAL||(LA39_15 >= FTSPHRASE && LA39_15 <= FTSWORD)||LA39_15==ID||(LA39_15 >= LPAREN && LA39_15 <= LT)||LA39_15==MINUS||LA39_15==NOT||(LA39_15 >= OR && LA39_15 <= PERCENT)||LA39_15==PLUS||LA39_15==QUESTION_MARK||LA39_15==RPAREN||LA39_15==STAR||(LA39_15 >= TILDA && LA39_15 <= TO)||LA39_15==URI) ) {s = 9;}
						 
						input.seek(index39_15);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA39_58 = input.LA(1);
						 
						int index39_58 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_58);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA39_77 = input.LA(1);
						 
						int index39_77 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_77);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA39_65 = input.LA(1);
						 
						int index39_65 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_65);
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA39_33 = input.LA(1);
						 
						int index39_33 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_33);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA39_3 = input.LA(1);
						 
						int index39_3 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_3==COLON) && (synpred18_FTS())) {s = 11;}
						else if ( (LA39_3==EOF||(LA39_3 >= AMP && LA39_3 <= BAR)||LA39_3==CARAT||LA39_3==COMMA||LA39_3==DATETIME||LA39_3==DECIMAL_INTEGER_LITERAL||LA39_3==DOT||LA39_3==EQUALS||LA39_3==EXCLAMATION||LA39_3==FLOATING_POINT_LITERAL||(LA39_3 >= FTSPHRASE && LA39_3 <= FTSWORD)||LA39_3==ID||(LA39_3 >= LPAREN && LA39_3 <= LT)||LA39_3==MINUS||LA39_3==NOT||(LA39_3 >= OR && LA39_3 <= PERCENT)||LA39_3==PLUS||LA39_3==QUESTION_MARK||LA39_3==RPAREN||LA39_3==STAR||(LA39_3 >= TILDA && LA39_3 <= TO)||LA39_3==URI) ) {s = 9;}
						 
						input.seek(index39_3);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA39_84 = input.LA(1);
						 
						int index39_84 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_84);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA39_22 = input.LA(1);
						 
						int index39_22 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_22);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA39_16 = input.LA(1);
						 
						int index39_16 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_16==COLON) && (synpred18_FTS())) {s = 19;}
						else if ( (LA39_16==EOF||(LA39_16 >= AMP && LA39_16 <= BAR)||LA39_16==CARAT||LA39_16==COMMA||LA39_16==DATETIME||LA39_16==DECIMAL_INTEGER_LITERAL||LA39_16==DOT||LA39_16==EQUALS||LA39_16==EXCLAMATION||LA39_16==FLOATING_POINT_LITERAL||(LA39_16 >= FTSPHRASE && LA39_16 <= FTSWORD)||LA39_16==ID||(LA39_16 >= LPAREN && LA39_16 <= LT)||LA39_16==MINUS||LA39_16==NOT||(LA39_16 >= OR && LA39_16 <= PERCENT)||LA39_16==PLUS||LA39_16==QUESTION_MARK||LA39_16==RPAREN||LA39_16==STAR||(LA39_16 >= TILDA && LA39_16 <= TO)||LA39_16==URI) ) {s = 9;}
						 
						input.seek(index39_16);
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA39_43 = input.LA(1);
						 
						int index39_43 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_43);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA39_53 = input.LA(1);
						 
						int index39_53 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_53);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA39_2 = input.LA(1);
						 
						int index39_2 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_2==DOT) ) {s = 10;}
						else if ( (LA39_2==COLON) && (synpred18_FTS())) {s = 11;}
						else if ( (LA39_2==EOF||(LA39_2 >= AMP && LA39_2 <= BAR)||LA39_2==CARAT||LA39_2==COMMA||LA39_2==DATETIME||LA39_2==DECIMAL_INTEGER_LITERAL||LA39_2==EQUALS||LA39_2==EXCLAMATION||LA39_2==FLOATING_POINT_LITERAL||(LA39_2 >= FTSPHRASE && LA39_2 <= FTSWORD)||LA39_2==ID||(LA39_2 >= LPAREN && LA39_2 <= LT)||LA39_2==MINUS||LA39_2==NOT||(LA39_2 >= OR && LA39_2 <= PERCENT)||LA39_2==PLUS||LA39_2==QUESTION_MARK||LA39_2==RPAREN||LA39_2==STAR||(LA39_2 >= TILDA && LA39_2 <= TO)||LA39_2==URI) ) {s = 9;}
						 
						input.seek(index39_2);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA39_81 = input.LA(1);
						 
						int index39_81 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_81);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA39_73 = input.LA(1);
						 
						int index39_73 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_73);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA39_61 = input.LA(1);
						 
						int index39_61 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_61);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA39_31 = input.LA(1);
						 
						int index39_31 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_31);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA39_42 = input.LA(1);
						 
						int index39_42 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_42);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA39_75 = input.LA(1);
						 
						int index39_75 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_75);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA39_70 = input.LA(1);
						 
						int index39_70 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_70);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA39_6 = input.LA(1);
						 
						int index39_6 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_6==COLON) && (synpred18_FTS())) {s = 11;}
						else if ( (LA39_6==EOF||(LA39_6 >= AMP && LA39_6 <= BAR)||LA39_6==CARAT||LA39_6==COMMA||LA39_6==DATETIME||LA39_6==DECIMAL_INTEGER_LITERAL||LA39_6==DOT||LA39_6==EQUALS||LA39_6==EXCLAMATION||LA39_6==FLOATING_POINT_LITERAL||(LA39_6 >= FTSPHRASE && LA39_6 <= FTSWORD)||LA39_6==ID||(LA39_6 >= LPAREN && LA39_6 <= LT)||LA39_6==MINUS||LA39_6==NOT||(LA39_6 >= OR && LA39_6 <= PERCENT)||LA39_6==PLUS||LA39_6==QUESTION_MARK||LA39_6==RPAREN||LA39_6==STAR||(LA39_6 >= TILDA && LA39_6 <= TO)||LA39_6==URI) ) {s = 9;}
						 
						input.seek(index39_6);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA39_80 = input.LA(1);
						 
						int index39_80 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_80);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA39_68 = input.LA(1);
						 
						int index39_68 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_68);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA39_36 = input.LA(1);
						 
						int index39_36 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_36);
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA39_55 = input.LA(1);
						 
						int index39_55 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_55);
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA39_50 = input.LA(1);
						 
						int index39_50 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_50);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA39_40 = input.LA(1);
						 
						int index39_40 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_40);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA39_45 = input.LA(1);
						 
						int index39_45 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_45);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA39_63 = input.LA(1);
						 
						int index39_63 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_63);
						if ( s>=0 ) return s;
						break;

					case 29 : 
						int LA39_46 = input.LA(1);
						 
						int index39_46 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_46);
						if ( s>=0 ) return s;
						break;

					case 30 : 
						int LA39_85 = input.LA(1);
						 
						int index39_85 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_85);
						if ( s>=0 ) return s;
						break;

					case 31 : 
						int LA39_83 = input.LA(1);
						 
						int index39_83 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_83);
						if ( s>=0 ) return s;
						break;

					case 32 : 
						int LA39_44 = input.LA(1);
						 
						int index39_44 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_44);
						if ( s>=0 ) return s;
						break;

					case 33 : 
						int LA39_48 = input.LA(1);
						 
						int index39_48 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_48);
						if ( s>=0 ) return s;
						break;

					case 34 : 
						int LA39_12 = input.LA(1);
						 
						int index39_12 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_12==DOT) ) {s = 18;}
						else if ( (LA39_12==COLON) && (synpred18_FTS())) {s = 19;}
						else if ( (LA39_12==EOF||(LA39_12 >= AMP && LA39_12 <= BAR)||LA39_12==CARAT||LA39_12==COMMA||LA39_12==DATETIME||LA39_12==DECIMAL_INTEGER_LITERAL||LA39_12==EQUALS||LA39_12==EXCLAMATION||LA39_12==FLOATING_POINT_LITERAL||(LA39_12 >= FTSPHRASE && LA39_12 <= FTSWORD)||LA39_12==ID||(LA39_12 >= LPAREN && LA39_12 <= LT)||LA39_12==MINUS||LA39_12==NOT||(LA39_12 >= OR && LA39_12 <= PERCENT)||LA39_12==PLUS||LA39_12==QUESTION_MARK||LA39_12==RPAREN||LA39_12==STAR||(LA39_12 >= TILDA && LA39_12 <= TO)||LA39_12==URI) ) {s = 9;}
						 
						input.seek(index39_12);
						if ( s>=0 ) return s;
						break;

					case 35 : 
						int LA39_13 = input.LA(1);
						 
						int index39_13 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_13==COLON) && (synpred18_FTS())) {s = 19;}
						else if ( (LA39_13==EOF||(LA39_13 >= AMP && LA39_13 <= BAR)||LA39_13==CARAT||LA39_13==COMMA||LA39_13==DATETIME||LA39_13==DECIMAL_INTEGER_LITERAL||LA39_13==DOT||LA39_13==EQUALS||LA39_13==EXCLAMATION||LA39_13==FLOATING_POINT_LITERAL||(LA39_13 >= FTSPHRASE && LA39_13 <= FTSWORD)||LA39_13==ID||(LA39_13 >= LPAREN && LA39_13 <= LT)||LA39_13==MINUS||LA39_13==NOT||(LA39_13 >= OR && LA39_13 <= PERCENT)||LA39_13==PLUS||LA39_13==QUESTION_MARK||LA39_13==RPAREN||LA39_13==STAR||(LA39_13 >= TILDA && LA39_13 <= TO)||LA39_13==URI) ) {s = 9;}
						 
						input.seek(index39_13);
						if ( s>=0 ) return s;
						break;

					case 36 : 
						int LA39_0 = input.LA(1);
						 
						int index39_0 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_0==AT) && (synpred18_FTS())) {s = 1;}
						else if ( (LA39_0==ID) ) {s = 2;}
						else if ( (LA39_0==TO) ) {s = 3;}
						else if ( (LA39_0==OR) && (synpred18_FTS())) {s = 4;}
						else if ( (LA39_0==AND) && (synpred18_FTS())) {s = 5;}
						else if ( (LA39_0==NOT) ) {s = 6;}
						else if ( (LA39_0==URI) ) {s = 7;}
						else if ( (LA39_0==FTSPHRASE) ) {s = 8;}
						else if ( (LA39_0==COMMA||LA39_0==DATETIME||LA39_0==DECIMAL_INTEGER_LITERAL||LA39_0==DOT||LA39_0==FLOATING_POINT_LITERAL||(LA39_0 >= FTSPRE && LA39_0 <= FTSWORD)||LA39_0==QUESTION_MARK||LA39_0==STAR) ) {s = 9;}
						 
						input.seek(index39_0);
						if ( s>=0 ) return s;
						break;

					case 37 : 
						int LA39_78 = input.LA(1);
						 
						int index39_78 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_78);
						if ( s>=0 ) return s;
						break;

					case 38 : 
						int LA39_66 = input.LA(1);
						 
						int index39_66 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_66);
						if ( s>=0 ) return s;
						break;

					case 39 : 
						int LA39_34 = input.LA(1);
						 
						int index39_34 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_34);
						if ( s>=0 ) return s;
						break;

					case 40 : 
						int LA39_71 = input.LA(1);
						 
						int index39_71 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_71);
						if ( s>=0 ) return s;
						break;

					case 41 : 
						int LA39_59 = input.LA(1);
						 
						int index39_59 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_59);
						if ( s>=0 ) return s;
						break;

					case 42 : 
						int LA39_29 = input.LA(1);
						 
						int index39_29 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_29);
						if ( s>=0 ) return s;
						break;

					case 43 : 
						int LA39_14 = input.LA(1);
						 
						int index39_14 = input.index();
						input.rewind();
						s = -1;
						if ( (LA39_14==COLON) && (synpred18_FTS())) {s = 19;}
						else if ( (LA39_14==EOF||(LA39_14 >= AMP && LA39_14 <= BAR)||LA39_14==CARAT||LA39_14==COMMA||LA39_14==DATETIME||LA39_14==DECIMAL_INTEGER_LITERAL||LA39_14==DOT||LA39_14==EQUALS||LA39_14==EXCLAMATION||LA39_14==FLOATING_POINT_LITERAL||(LA39_14 >= FTSPHRASE && LA39_14 <= FTSWORD)||LA39_14==ID||(LA39_14 >= LPAREN && LA39_14 <= LT)||LA39_14==MINUS||LA39_14==NOT||(LA39_14 >= OR && LA39_14 <= PERCENT)||LA39_14==PLUS||LA39_14==QUESTION_MARK||LA39_14==RPAREN||LA39_14==STAR||(LA39_14 >= TILDA && LA39_14 <= TO)||LA39_14==URI) ) {s = 9;}
						 
						input.seek(index39_14);
						if ( s>=0 ) return s;
						break;

					case 44 : 
						int LA39_72 = input.LA(1);
						 
						int index39_72 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_72);
						if ( s>=0 ) return s;
						break;

					case 45 : 
						int LA39_60 = input.LA(1);
						 
						int index39_60 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_60);
						if ( s>=0 ) return s;
						break;

					case 46 : 
						int LA39_30 = input.LA(1);
						 
						int index39_30 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_30);
						if ( s>=0 ) return s;
						break;

					case 47 : 
						int LA39_41 = input.LA(1);
						 
						int index39_41 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_41);
						if ( s>=0 ) return s;
						break;

					case 48 : 
						int LA39_49 = input.LA(1);
						 
						int index39_49 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_49);
						if ( s>=0 ) return s;
						break;

					case 49 : 
						int LA39_76 = input.LA(1);
						 
						int index39_76 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_76);
						if ( s>=0 ) return s;
						break;

					case 50 : 
						int LA39_64 = input.LA(1);
						 
						int index39_64 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_64);
						if ( s>=0 ) return s;
						break;

					case 51 : 
						int LA39_32 = input.LA(1);
						 
						int index39_32 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_32);
						if ( s>=0 ) return s;
						break;

					case 52 : 
						int LA39_69 = input.LA(1);
						 
						int index39_69 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_69);
						if ( s>=0 ) return s;
						break;

					case 53 : 
						int LA39_57 = input.LA(1);
						 
						int index39_57 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_57);
						if ( s>=0 ) return s;
						break;

					case 54 : 
						int LA39_27 = input.LA(1);
						 
						int index39_27 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_27);
						if ( s>=0 ) return s;
						break;

					case 55 : 
						int LA39_52 = input.LA(1);
						 
						int index39_52 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_52);
						if ( s>=0 ) return s;
						break;

					case 56 : 
						int LA39_62 = input.LA(1);
						 
						int index39_62 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_62);
						if ( s>=0 ) return s;
						break;

					case 57 : 
						int LA39_26 = input.LA(1);
						 
						int index39_26 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_26);
						if ( s>=0 ) return s;
						break;

					case 58 : 
						int LA39_74 = input.LA(1);
						 
						int index39_74 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_74);
						if ( s>=0 ) return s;
						break;

					case 59 : 
						int LA39_56 = input.LA(1);
						 
						int index39_56 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_56);
						if ( s>=0 ) return s;
						break;

					case 60 : 
						int LA39_82 = input.LA(1);
						 
						int index39_82 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_82);
						if ( s>=0 ) return s;
						break;

					case 61 : 
						int LA39_79 = input.LA(1);
						 
						int index39_79 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_79);
						if ( s>=0 ) return s;
						break;

					case 62 : 
						int LA39_67 = input.LA(1);
						 
						int index39_67 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_67);
						if ( s>=0 ) return s;
						break;

					case 63 : 
						int LA39_35 = input.LA(1);
						 
						int index39_35 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_35);
						if ( s>=0 ) return s;
						break;

					case 64 : 
						int LA39_54 = input.LA(1);
						 
						int index39_54 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_54);
						if ( s>=0 ) return s;
						break;

					case 65 : 
						int LA39_23 = input.LA(1);
						 
						int index39_23 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred18_FTS()) ) {s = 19;}
						else if ( (true) ) {s = 9;}
						 
						input.seek(index39_23);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 39, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	static final String DFA60_eotS =
		"\u00bb\uffff";
	static final String DFA60_eofS =
		"\u00bb\uffff";
	static final String DFA60_minS =
		"\1\13\12\4\1\5\2\uffff\1\13\1\4\1\13\3\uffff\1\4\34\uffff\5\4\70\uffff"+
		"\1\4\12\0\1\5\6\uffff\2\4\32\uffff\6\0\14\uffff\1\4\12\uffff\1\0\1\uffff";
	static final String DFA60_maxS =
		"\13\150\1\146\2\uffff\3\150\3\uffff\1\150\34\uffff\5\150\70\uffff\1\150"+
		"\12\0\1\146\6\uffff\2\150\32\uffff\6\0\14\uffff\1\150\12\uffff\1\0\1\uffff";
	static final String DFA60_acceptS =
		"\14\uffff\2\2\3\uffff\2\10\1\11\1\uffff\33\2\1\10\5\uffff\15\3\1\5\34"+
		"\4\1\6\15\7\14\uffff\6\2\2\uffff\1\1\31\2\6\uffff\14\2\1\uffff\12\2\1"+
		"\uffff\1\2";
	static final String DFA60_specialS =
		"\1\20\1\31\1\34\1\43\1\50\1\33\1\21\1\10\1\12\1\32\1\17\3\uffff\1\35\1"+
		"\4\1\30\3\uffff\1\37\34\uffff\1\6\1\45\1\42\1\36\1\27\70\uffff\1\0\1\14"+
		"\1\3\1\44\1\26\1\47\1\13\1\7\1\2\1\25\1\46\7\uffff\1\23\1\16\32\uffff"+
		"\1\11\1\15\1\1\1\22\1\41\1\5\14\uffff\1\24\12\uffff\1\40\1\uffff}>";
	static final String[] DFA60_transitionS = {
			"\1\14\1\uffff\1\11\1\uffff\1\7\5\uffff\1\14\2\uffff\1\16\26\uffff\1\10"+
			"\2\uffff\1\17\1\3\1\4\1\2\11\uffff\1\1\3\uffff\1\23\1\21\1\22\10\uffff"+
			"\1\5\11\uffff\1\15\7\uffff\1\12\4\uffff\1\20\1\6\1\uffff\1\13",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\2\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff\1"+
			"\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\2\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff\1"+
			"\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\64\71\uffff\1\61\16\uffff\1\65\2\uffff\1\63\24\uffff\1\62",
			"",
			"",
			"\1\66\1\uffff\1\101\1\uffff\1\75\5\uffff\1\66\2\uffff\1\103\26\uffff"+
			"\1\76\3\uffff\1\71\1\72\1\70\11\uffff\1\67\16\uffff\1\73\11\uffff\1\100"+
			"\7\uffff\1\77\5\uffff\1\74\1\uffff\1\102",
			"\1\107\1\106\1\uffff\1\112\1\uffff\1\105\1\uffff\1\127\1\uffff\1\124"+
			"\1\uffff\1\122\5\uffff\1\127\1\60\1\uffff\1\131\2\uffff\1\120\23\uffff"+
			"\1\123\2\uffff\1\132\1\116\1\117\1\115\11\uffff\1\114\3\uffff\1\135\1"+
			"\133\1\134\2\uffff\1\137\5\uffff\1\113\2\uffff\1\111\2\uffff\1\136\3"+
			"\uffff\1\130\2\uffff\1\110\4\uffff\1\125\4\uffff\1\104\1\121\1\uffff"+
			"\1\126",
			"\1\141\1\uffff\1\154\1\uffff\1\150\5\uffff\1\141\2\uffff\1\140\26\uffff"+
			"\1\151\3\uffff\1\144\1\145\1\143\11\uffff\1\142\16\uffff\1\146\11\uffff"+
			"\1\153\7\uffff\1\152\5\uffff\1\147\1\uffff\1\155",
			"",
			"",
			"",
			"\1\176\1\175\1\uffff\1\34\1\uffff\1\174\1\uffff\1\172\1\uffff\1\167"+
			"\1\uffff\1\165\5\uffff\1\172\1\177\1\uffff\1\51\2\uffff\1\42\23\uffff"+
			"\1\166\2\uffff\1\52\1\161\1\162\1\160\11\uffff\1\157\3\uffff\1\156\1"+
			"\53\1\54\2\uffff\1\57\5\uffff\1\163\2\uffff\1\33\2\uffff\1\56\3\uffff"+
			"\1\50\2\uffff\1\32\4\uffff\1\170\4\uffff\1\173\1\164\1\uffff\1\171",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\u0080\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2"+
			"\uffff\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff"+
			"\1\57\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32"+
			"\4\uffff\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\25\1\uffff\1\46\1\uffff"+
			"\1\44\5\uffff\1\25\1\60\1\uffff\1\51\2\uffff\1\42\23\uffff\1\45\2\uffff"+
			"\1\52\1\40\1\41\1\37\11\uffff\1\36\3\uffff\1\55\1\53\1\54\2\uffff\1\57"+
			"\5\uffff\1\35\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2\uffff\1\32\4\uffff"+
			"\1\24\4\uffff\1\26\1\43\1\uffff\1\47",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u0084\1\u0083\1\uffff\1\u0099\3\uffff\1\u0090\1\uffff\1\u008d\1\uffff"+
			"\1\u0081\5\uffff\1\u0090\2\uffff\1\u0092\2\uffff\1\u008a\23\uffff\1\u008c"+
			"\2\uffff\1\u0093\1\u0088\1\u0089\1\u0087\11\uffff\1\u0086\3\uffff\1\u0097"+
			"\1\u0095\1\u0096\2\uffff\1\u009a\5\uffff\1\u0085\2\uffff\1\u009b\2\uffff"+
			"\1\u0098\3\uffff\1\u0091\2\uffff\1\u0082\4\uffff\1\u008e\4\uffff\1\u0094"+
			"\1\u008b\1\uffff\1\u008f",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u009f\71\uffff\1\u009c\16\uffff\1\u00a0\2\uffff\1\u009e\24\uffff"+
			"\1\u009d",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\31\1\30\1\uffff\1\34\1\uffff\1\27\1\uffff\1\u00ad\1\uffff\1\u00ab"+
			"\1\uffff\1\u00a7\5\uffff\1\u00ad\2\uffff\1\51\2\uffff\1\42\23\uffff\1"+
			"\u00a8\2\uffff\1\52\1\u00a3\1\u00a4\1\u00a2\11\uffff\1\u00a1\3\uffff"+
			"\1\55\1\53\1\54\2\uffff\1\57\5\uffff\1\u00a5\2\uffff\1\33\2\uffff\1\56"+
			"\3\uffff\1\u00aa\2\uffff\1\32\4\uffff\1\u00a9\4\uffff\1\26\1\u00a6\1"+
			"\uffff\1\u00ac",
			"\1\u00b6\1\u00b5\1\uffff\1\u00b4\1\uffff\1\u00b2\1\uffff\1\u00b0\1\uffff"+
			"\1\u008d\1\uffff\1\u00b7\5\uffff\1\u00b0\1\u00b8\1\uffff\1\u0092\2\uffff"+
			"\1\u008a\23\uffff\1\u008c\2\uffff\1\u0093\1\u0088\1\u0089\1\u0087\11"+
			"\uffff\1\u0086\3\uffff\1\u0097\1\u0095\1\u0096\2\uffff\1\u009a\5\uffff"+
			"\1\u0085\2\uffff\1\u00b3\2\uffff\1\u0098\3\uffff\1\u0091\2\uffff\1\u00ae"+
			"\4\uffff\1\u00af\4\uffff\1\u00b1\1\u008b\1\uffff\1\u008f",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\176\1\175\1\uffff\1\34\1\uffff\1\174\1\uffff\1\u00ad\1\uffff\1\167"+
			"\1\uffff\1\165\5\uffff\1\u00ad\2\uffff\1\51\2\uffff\1\42\23\uffff\1\166"+
			"\2\uffff\1\52\1\161\1\162\1\160\11\uffff\1\157\3\uffff\1\55\1\53\1\54"+
			"\2\uffff\1\57\5\uffff\1\163\2\uffff\1\33\2\uffff\1\56\3\uffff\1\50\2"+
			"\uffff\1\32\4\uffff\1\u00b9\4\uffff\1\u00ba\1\164\1\uffff\1\171",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\uffff",
			""
	};

	static final short[] DFA60_eot = DFA.unpackEncodedString(DFA60_eotS);
	static final short[] DFA60_eof = DFA.unpackEncodedString(DFA60_eofS);
	static final char[] DFA60_min = DFA.unpackEncodedStringToUnsignedChars(DFA60_minS);
	static final char[] DFA60_max = DFA.unpackEncodedStringToUnsignedChars(DFA60_maxS);
	static final short[] DFA60_accept = DFA.unpackEncodedString(DFA60_acceptS);
	static final short[] DFA60_special = DFA.unpackEncodedString(DFA60_specialS);
	static final short[][] DFA60_transition;

	static {
		int numStates = DFA60_transitionS.length;
		DFA60_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA60_transition[i] = DFA.unpackEncodedString(DFA60_transitionS[i]);
		}
	}

	protected class DFA60 extends DFA {

		public DFA60(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 60;
			this.eot = DFA60_eot;
			this.eof = DFA60_eof;
			this.min = DFA60_min;
			this.max = DFA60_max;
			this.accept = DFA60_accept;
			this.special = DFA60_special;
			this.transition = DFA60_transition;
		}
		@Override
		public String getDescription() {
			return "659:1: ftsFieldGroupTest : ( ( ftsFieldGroupProximity )=> ftsFieldGroupProximity -> ^( FG_PROXIMITY ftsFieldGroupProximity ) | ( ftsFieldGroupTerm )=> ftsFieldGroupTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_TERM ftsFieldGroupTerm ( fuzzy )? ) | ( ftsFieldGroupExactTerm )=> ftsFieldGroupExactTerm ( ( fuzzy )=> fuzzy )? -> ^( FG_EXACT_TERM ftsFieldGroupExactTerm ( fuzzy )? ) | ( ftsFieldGroupPhrase )=> ftsFieldGroupPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupPhrase ( slop )? ) | ( ftsFieldGroupExactPhrase )=> ftsFieldGroupExactPhrase ( ( slop )=> slop )? -> ^( FG_EXACT_PHRASE ftsFieldGroupExactPhrase ( slop )? ) | ( ftsFieldGroupTokenisedPhrase )=> ftsFieldGroupTokenisedPhrase ( ( slop )=> slop )? -> ^( FG_PHRASE ftsFieldGroupTokenisedPhrase ( slop )? ) | ( ftsFieldGroupSynonym )=> ftsFieldGroupSynonym ( ( fuzzy )=> fuzzy )? -> ^( FG_SYNONYM ftsFieldGroupSynonym ( fuzzy )? ) | ( ftsFieldGroupRange )=> ftsFieldGroupRange -> ^( FG_RANGE ftsFieldGroupRange ) | LPAREN ftsFieldGroupDisjunction RPAREN -> ftsFieldGroupDisjunction );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA60_110 = input.LA(1);
						 
						int index60_110 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_110==DECIMAL_INTEGER_LITERAL) ) {s = 129;}
						else if ( (LA60_110==RPAREN) && (synpred24_FTS())) {s = 130;}
						else if ( (LA60_110==AND) && (synpred25_FTS())) {s = 131;}
						else if ( (LA60_110==AMP) && (synpred25_FTS())) {s = 132;}
						else if ( (LA60_110==NOT) && (synpred25_FTS())) {s = 133;}
						else if ( (LA60_110==ID) && (synpred25_FTS())) {s = 134;}
						else if ( (LA60_110==FTSWORD) && (synpred25_FTS())) {s = 135;}
						else if ( (LA60_110==FTSPRE) && (synpred25_FTS())) {s = 136;}
						else if ( (LA60_110==FTSWILD) && (synpred25_FTS())) {s = 137;}
						else if ( (LA60_110==EXCLAMATION) && (synpred25_FTS())) {s = 138;}
						else if ( (LA60_110==TO) && (synpred25_FTS())) {s = 139;}
						else if ( (LA60_110==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 140;}
						else if ( (LA60_110==DATETIME) && (synpred25_FTS())) {s = 141;}
						else if ( (LA60_110==STAR) && (synpred25_FTS())) {s = 142;}
						else if ( (LA60_110==URI) && (synpred25_FTS())) {s = 143;}
						else if ( (LA60_110==COMMA||LA60_110==DOT) && (synpred25_FTS())) {s = 144;}
						else if ( (LA60_110==QUESTION_MARK) && (synpred25_FTS())) {s = 145;}
						else if ( (LA60_110==EQUALS) && (synpred25_FTS())) {s = 146;}
						else if ( (LA60_110==FTSPHRASE) && (synpred25_FTS())) {s = 147;}
						else if ( (LA60_110==TILDA) && (synpred25_FTS())) {s = 148;}
						else if ( (LA60_110==LSQUARE) && (synpred25_FTS())) {s = 149;}
						else if ( (LA60_110==LT) && (synpred25_FTS())) {s = 150;}
						else if ( (LA60_110==LPAREN) && (synpred25_FTS())) {s = 151;}
						else if ( (LA60_110==PLUS) && (synpred25_FTS())) {s = 152;}
						else if ( (LA60_110==BAR) && (synpred25_FTS())) {s = 153;}
						else if ( (LA60_110==MINUS) && (synpred25_FTS())) {s = 154;}
						else if ( (LA60_110==OR) && (synpred25_FTS())) {s = 155;}
						 
						input.seek(index60_110);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA60_158 = input.LA(1);
						 
						int index60_158 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 184;}
						 
						input.seek(index60_158);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA60_118 = input.LA(1);
						 
						int index60_118 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_118);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA60_112 = input.LA(1);
						 
						int index60_112 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_112);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA60_15 = input.LA(1);
						 
						int index60_15 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_15==TILDA) && (synpred29_FTS())) {s = 68;}
						else if ( (LA60_15==CARAT) && (synpred29_FTS())) {s = 69;}
						else if ( (LA60_15==AND) && (synpred29_FTS())) {s = 70;}
						else if ( (LA60_15==AMP) && (synpred29_FTS())) {s = 71;}
						else if ( (LA60_15==RPAREN) && (synpred29_FTS())) {s = 72;}
						else if ( (LA60_15==OR) && (synpred29_FTS())) {s = 73;}
						else if ( (LA60_15==BAR) && (synpred29_FTS())) {s = 74;}
						else if ( (LA60_15==NOT) && (synpred29_FTS())) {s = 75;}
						else if ( (LA60_15==ID) && (synpred29_FTS())) {s = 76;}
						else if ( (LA60_15==FTSWORD) && (synpred29_FTS())) {s = 77;}
						else if ( (LA60_15==FTSPRE) && (synpred29_FTS())) {s = 78;}
						else if ( (LA60_15==FTSWILD) && (synpred29_FTS())) {s = 79;}
						else if ( (LA60_15==EXCLAMATION) && (synpred29_FTS())) {s = 80;}
						else if ( (LA60_15==TO) && (synpred29_FTS())) {s = 81;}
						else if ( (LA60_15==DECIMAL_INTEGER_LITERAL) && (synpred29_FTS())) {s = 82;}
						else if ( (LA60_15==FLOATING_POINT_LITERAL) && (synpred29_FTS())) {s = 83;}
						else if ( (LA60_15==DATETIME) && (synpred29_FTS())) {s = 84;}
						else if ( (LA60_15==STAR) && (synpred29_FTS())) {s = 85;}
						else if ( (LA60_15==URI) && (synpred29_FTS())) {s = 86;}
						else if ( (LA60_15==COMMA||LA60_15==DOT) && (synpred29_FTS())) {s = 87;}
						else if ( (LA60_15==QUESTION_MARK) && (synpred29_FTS())) {s = 88;}
						else if ( (LA60_15==EQUALS) && (synpred29_FTS())) {s = 89;}
						else if ( (LA60_15==FTSPHRASE) && (synpred29_FTS())) {s = 90;}
						else if ( (LA60_15==LSQUARE) && (synpred29_FTS())) {s = 91;}
						else if ( (LA60_15==LT) && (synpred29_FTS())) {s = 92;}
						else if ( (LA60_15==LPAREN) && (synpred29_FTS())) {s = 93;}
						else if ( (LA60_15==PLUS) && (synpred29_FTS())) {s = 94;}
						else if ( (LA60_15==MINUS) && (synpred29_FTS())) {s = 95;}
						else if ( (LA60_15==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_15);
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA60_161 = input.LA(1);
						 
						int index60_161 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 184;}
						else if ( (synpred37_FTS()) ) {s = 48;}
						 
						input.seek(index60_161);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA60_49 = input.LA(1);
						 
						int index60_49 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_49==DOT) ) {s = 128;}
						else if ( (LA60_49==STAR) ) {s = 20;}
						else if ( (LA60_49==COMMA) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_49==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_49==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_49==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_49==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_49==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_49==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_49==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_49==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_49==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_49==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_49==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_49==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_49==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_49==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_49==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_49==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_49==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_49==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_49==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_49==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_49==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_49==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_49==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_49==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_49==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_49==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_49==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_49);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA60_117 = input.LA(1);
						 
						int index60_117 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_117);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA60_7 = input.LA(1);
						 
						int index60_7 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_7==STAR) ) {s = 20;}
						else if ( (LA60_7==COMMA||LA60_7==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_7==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_7==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_7==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_7==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_7==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_7==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_7==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_7==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_7==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_7==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_7==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_7==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_7==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_7==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_7==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_7==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_7==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_7==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_7==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_7==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_7==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_7==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_7==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_7==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_7==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_7==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_7==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_7);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA60_156 = input.LA(1);
						 
						int index60_156 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 184;}
						 
						input.seek(index60_156);
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA60_8 = input.LA(1);
						 
						int index60_8 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_8==STAR) ) {s = 20;}
						else if ( (LA60_8==COMMA||LA60_8==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_8==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_8==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_8==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_8==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_8==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_8==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_8==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_8==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_8==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_8==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_8==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_8==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_8==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_8==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_8==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_8==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_8==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_8==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_8==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_8==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_8==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_8==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_8==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_8==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_8==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_8==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_8==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_8);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA60_116 = input.LA(1);
						 
						int index60_116 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_116);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA60_111 = input.LA(1);
						 
						int index60_111 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_111);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA60_157 = input.LA(1);
						 
						int index60_157 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 184;}
						 
						input.seek(index60_157);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA60_129 = input.LA(1);
						 
						int index60_129 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_129==RPAREN) ) {s = 174;}
						else if ( (LA60_129==STAR) && (synpred25_FTS())) {s = 175;}
						else if ( (LA60_129==COMMA||LA60_129==DOT) && (synpred25_FTS())) {s = 176;}
						else if ( (LA60_129==TILDA) && (synpred25_FTS())) {s = 177;}
						else if ( (LA60_129==CARAT) && (synpred25_FTS())) {s = 178;}
						else if ( (LA60_129==OR) && (synpred25_FTS())) {s = 179;}
						else if ( (LA60_129==BAR) && (synpred25_FTS())) {s = 180;}
						else if ( (LA60_129==AND) && (synpred25_FTS())) {s = 181;}
						else if ( (LA60_129==AMP) && (synpred25_FTS())) {s = 182;}
						else if ( (LA60_129==NOT) && (synpred25_FTS())) {s = 133;}
						else if ( (LA60_129==ID) && (synpred25_FTS())) {s = 134;}
						else if ( (LA60_129==FTSWORD) && (synpred25_FTS())) {s = 135;}
						else if ( (LA60_129==FTSPRE) && (synpred25_FTS())) {s = 136;}
						else if ( (LA60_129==FTSWILD) && (synpred25_FTS())) {s = 137;}
						else if ( (LA60_129==EXCLAMATION) && (synpred25_FTS())) {s = 138;}
						else if ( (LA60_129==TO) && (synpred25_FTS())) {s = 139;}
						else if ( (LA60_129==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 183;}
						else if ( (LA60_129==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 140;}
						else if ( (LA60_129==DATETIME) && (synpred25_FTS())) {s = 141;}
						else if ( (LA60_129==URI) && (synpred25_FTS())) {s = 143;}
						else if ( (LA60_129==QUESTION_MARK) && (synpred25_FTS())) {s = 145;}
						else if ( (LA60_129==EQUALS) && (synpred25_FTS())) {s = 146;}
						else if ( (LA60_129==FTSPHRASE) && (synpred25_FTS())) {s = 147;}
						else if ( (LA60_129==LSQUARE) && (synpred25_FTS())) {s = 149;}
						else if ( (LA60_129==LT) && (synpred25_FTS())) {s = 150;}
						else if ( (LA60_129==LPAREN) && (synpred25_FTS())) {s = 151;}
						else if ( (LA60_129==PLUS) && (synpred25_FTS())) {s = 152;}
						else if ( (LA60_129==MINUS) && (synpred25_FTS())) {s = 154;}
						else if ( (LA60_129==DOTDOT) && (synpred25_FTS())) {s = 184;}
						 
						input.seek(index60_129);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA60_10 = input.LA(1);
						 
						int index60_10 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_10==STAR) ) {s = 20;}
						else if ( (LA60_10==COMMA||LA60_10==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_10==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_10==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_10==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_10==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_10==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_10==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_10==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_10==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_10==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_10==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_10==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_10==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_10==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_10==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_10==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_10==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_10==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_10==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_10==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_10==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_10==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_10==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_10==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_10==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_10==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_10==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_10==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_10);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA60_0 = input.LA(1);
						 
						int index60_0 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_0==ID) ) {s = 1;}
						else if ( (LA60_0==FTSWORD) ) {s = 2;}
						else if ( (LA60_0==FTSPRE) ) {s = 3;}
						else if ( (LA60_0==FTSWILD) ) {s = 4;}
						else if ( (LA60_0==NOT) ) {s = 5;}
						else if ( (LA60_0==TO) ) {s = 6;}
						else if ( (LA60_0==DECIMAL_INTEGER_LITERAL) ) {s = 7;}
						else if ( (LA60_0==FLOATING_POINT_LITERAL) ) {s = 8;}
						else if ( (LA60_0==DATETIME) ) {s = 9;}
						else if ( (LA60_0==STAR) ) {s = 10;}
						else if ( (LA60_0==URI) ) {s = 11;}
						else if ( (LA60_0==COMMA||LA60_0==DOT) && (synpred25_FTS())) {s = 12;}
						else if ( (LA60_0==QUESTION_MARK) && (synpred25_FTS())) {s = 13;}
						else if ( (LA60_0==EQUALS) ) {s = 14;}
						else if ( (LA60_0==FTSPHRASE) ) {s = 15;}
						else if ( (LA60_0==TILDA) ) {s = 16;}
						else if ( (LA60_0==LSQUARE) && (synpred37_FTS())) {s = 17;}
						else if ( (LA60_0==LT) && (synpred37_FTS())) {s = 18;}
						else if ( (LA60_0==LPAREN) ) {s = 19;}
						 
						input.seek(index60_0);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA60_6 = input.LA(1);
						 
						int index60_6 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_6==STAR) ) {s = 20;}
						else if ( (LA60_6==COMMA||LA60_6==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_6==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_6==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_6==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_6==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_6==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_6==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_6==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_6==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_6==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_6==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_6==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_6==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_6==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_6==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_6==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_6==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_6==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_6==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_6==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_6==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_6==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_6==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_6==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_6==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_6==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_6==MINUS) && (synpred25_FTS())) {s = 47;}
						 
						input.seek(index60_6);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA60_159 = input.LA(1);
						 
						int index60_159 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 184;}
						 
						input.seek(index60_159);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA60_128 = input.LA(1);
						 
						int index60_128 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_128==ID) ) {s = 161;}
						else if ( (LA60_128==FTSWORD) && (synpred25_FTS())) {s = 162;}
						else if ( (LA60_128==FTSPRE) && (synpred25_FTS())) {s = 163;}
						else if ( (LA60_128==FTSWILD) && (synpred25_FTS())) {s = 164;}
						else if ( (LA60_128==NOT) && (synpred25_FTS())) {s = 165;}
						else if ( (LA60_128==TO) && (synpred25_FTS())) {s = 166;}
						else if ( (LA60_128==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 167;}
						else if ( (LA60_128==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 168;}
						else if ( (LA60_128==STAR) && (synpred25_FTS())) {s = 169;}
						else if ( (LA60_128==QUESTION_MARK) && (synpred25_FTS())) {s = 170;}
						else if ( (LA60_128==DATETIME) && (synpred25_FTS())) {s = 171;}
						else if ( (LA60_128==URI) && (synpred25_FTS())) {s = 172;}
						else if ( (LA60_128==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_128==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_128==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_128==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_128==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_128==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_128==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_128==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_128==COMMA||LA60_128==DOT) && (synpred25_FTS())) {s = 173;}
						else if ( (LA60_128==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_128==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_128==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_128==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_128==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_128==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_128==MINUS) && (synpred25_FTS())) {s = 47;}
						 
						input.seek(index60_128);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA60_174 = input.LA(1);
						 
						int index60_174 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_174==ID) ) {s = 111;}
						else if ( (LA60_174==FTSWORD) ) {s = 112;}
						else if ( (LA60_174==FTSPRE) ) {s = 113;}
						else if ( (LA60_174==FTSWILD) ) {s = 114;}
						else if ( (LA60_174==NOT) ) {s = 115;}
						else if ( (LA60_174==TO) ) {s = 116;}
						else if ( (LA60_174==DECIMAL_INTEGER_LITERAL) ) {s = 117;}
						else if ( (LA60_174==FLOATING_POINT_LITERAL) ) {s = 118;}
						else if ( (LA60_174==DATETIME) ) {s = 119;}
						else if ( (LA60_174==STAR) ) {s = 185;}
						else if ( (LA60_174==URI) ) {s = 121;}
						else if ( (LA60_174==CARAT) && (synpred25_FTS())) {s = 124;}
						else if ( (LA60_174==AND) && (synpred25_FTS())) {s = 125;}
						else if ( (LA60_174==AMP) && (synpred25_FTS())) {s = 126;}
						else if ( (LA60_174==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_174==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_174==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_174==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_174==COMMA||LA60_174==DOT) && (synpred25_FTS())) {s = 173;}
						else if ( (LA60_174==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_174==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_174==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_174==TILDA) && (synpred25_FTS())) {s = 186;}
						else if ( (LA60_174==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_174==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_174==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_174==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_174==MINUS) && (synpred25_FTS())) {s = 47;}
						 
						input.seek(index60_174);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA60_119 = input.LA(1);
						 
						int index60_119 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_119);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA60_114 = input.LA(1);
						 
						int index60_114 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_114);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA60_53 = input.LA(1);
						 
						int index60_53 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_53==STAR) ) {s = 20;}
						else if ( (LA60_53==COMMA||LA60_53==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_53==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_53==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_53==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_53==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_53==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_53==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_53==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_53==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_53==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_53==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_53==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_53==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_53==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_53==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_53==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_53==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_53==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_53==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_53==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_53==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_53==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_53==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_53==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_53==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_53==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_53==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_53==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_53);
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA60_16 = input.LA(1);
						 
						int index60_16 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_16==EQUALS) && (synpred33_FTS())) {s = 96;}
						else if ( (LA60_16==COMMA||LA60_16==DOT) && (synpred35_FTS())) {s = 97;}
						else if ( (LA60_16==ID) && (synpred35_FTS())) {s = 98;}
						else if ( (LA60_16==FTSWORD) && (synpred35_FTS())) {s = 99;}
						else if ( (LA60_16==FTSPRE) && (synpred35_FTS())) {s = 100;}
						else if ( (LA60_16==FTSWILD) && (synpred35_FTS())) {s = 101;}
						else if ( (LA60_16==NOT) && (synpred35_FTS())) {s = 102;}
						else if ( (LA60_16==TO) && (synpred35_FTS())) {s = 103;}
						else if ( (LA60_16==DECIMAL_INTEGER_LITERAL) && (synpred35_FTS())) {s = 104;}
						else if ( (LA60_16==FLOATING_POINT_LITERAL) && (synpred35_FTS())) {s = 105;}
						else if ( (LA60_16==STAR) && (synpred35_FTS())) {s = 106;}
						else if ( (LA60_16==QUESTION_MARK) && (synpred35_FTS())) {s = 107;}
						else if ( (LA60_16==DATETIME) && (synpred35_FTS())) {s = 108;}
						else if ( (LA60_16==URI) && (synpred35_FTS())) {s = 109;}
						 
						input.seek(index60_16);
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA60_1 = input.LA(1);
						 
						int index60_1 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_1==STAR) ) {s = 20;}
						else if ( (LA60_1==COMMA||LA60_1==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_1==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_1==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_1==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_1==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_1==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_1==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_1==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_1==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_1==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_1==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_1==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_1==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_1==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_1==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_1==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_1==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_1==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_1==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_1==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_1==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_1==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_1==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_1==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_1==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_1==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_1==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_1==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_1);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA60_9 = input.LA(1);
						 
						int index60_9 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_9==STAR) ) {s = 20;}
						else if ( (LA60_9==COMMA||LA60_9==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_9==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_9==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_9==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_9==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_9==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_9==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_9==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_9==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_9==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_9==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_9==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_9==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_9==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_9==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_9==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_9==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_9==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_9==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_9==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_9==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_9==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_9==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_9==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_9==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_9==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_9==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_9==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_9);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA60_5 = input.LA(1);
						 
						int index60_5 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_5==STAR) ) {s = 20;}
						else if ( (LA60_5==COMMA||LA60_5==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_5==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_5==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_5==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_5==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_5==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_5==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_5==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_5==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_5==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_5==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_5==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_5==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_5==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_5==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_5==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_5==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_5==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_5==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_5==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_5==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_5==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_5==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_5==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_5==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_5==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_5==MINUS) && (synpred25_FTS())) {s = 47;}
						 
						input.seek(index60_5);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA60_2 = input.LA(1);
						 
						int index60_2 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_2==STAR) ) {s = 20;}
						else if ( (LA60_2==COMMA||LA60_2==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_2==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_2==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_2==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_2==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_2==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_2==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_2==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_2==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_2==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_2==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_2==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_2==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_2==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_2==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_2==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_2==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_2==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_2==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_2==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_2==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_2==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_2==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_2==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_2==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_2==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_2==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_2==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_2);
						if ( s>=0 ) return s;
						break;

					case 29 : 
						int LA60_14 = input.LA(1);
						 
						int index60_14 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_14==COMMA||LA60_14==DOT) && (synpred27_FTS())) {s = 54;}
						else if ( (LA60_14==ID) && (synpred27_FTS())) {s = 55;}
						else if ( (LA60_14==FTSWORD) && (synpred27_FTS())) {s = 56;}
						else if ( (LA60_14==FTSPRE) && (synpred27_FTS())) {s = 57;}
						else if ( (LA60_14==FTSWILD) && (synpred27_FTS())) {s = 58;}
						else if ( (LA60_14==NOT) && (synpred27_FTS())) {s = 59;}
						else if ( (LA60_14==TO) && (synpred27_FTS())) {s = 60;}
						else if ( (LA60_14==DECIMAL_INTEGER_LITERAL) && (synpred27_FTS())) {s = 61;}
						else if ( (LA60_14==FLOATING_POINT_LITERAL) && (synpred27_FTS())) {s = 62;}
						else if ( (LA60_14==STAR) && (synpred27_FTS())) {s = 63;}
						else if ( (LA60_14==QUESTION_MARK) && (synpred27_FTS())) {s = 64;}
						else if ( (LA60_14==DATETIME) && (synpred27_FTS())) {s = 65;}
						else if ( (LA60_14==URI) && (synpred27_FTS())) {s = 66;}
						else if ( (LA60_14==EQUALS) && (synpred31_FTS())) {s = 67;}
						 
						input.seek(index60_14);
						if ( s>=0 ) return s;
						break;

					case 30 : 
						int LA60_52 = input.LA(1);
						 
						int index60_52 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_52==STAR) ) {s = 20;}
						else if ( (LA60_52==COMMA||LA60_52==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_52==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_52==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_52==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_52==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_52==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_52==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_52==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_52==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_52==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_52==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_52==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_52==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_52==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_52==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_52==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_52==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_52==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_52==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_52==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_52==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_52==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_52==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_52==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_52==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_52==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_52==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_52==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_52);
						if ( s>=0 ) return s;
						break;

					case 31 : 
						int LA60_20 = input.LA(1);
						 
						int index60_20 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_20==LPAREN) ) {s = 110;}
						else if ( (LA60_20==ID) ) {s = 111;}
						else if ( (LA60_20==FTSWORD) ) {s = 112;}
						else if ( (LA60_20==FTSPRE) ) {s = 113;}
						else if ( (LA60_20==FTSWILD) ) {s = 114;}
						else if ( (LA60_20==NOT) ) {s = 115;}
						else if ( (LA60_20==TO) ) {s = 116;}
						else if ( (LA60_20==DECIMAL_INTEGER_LITERAL) ) {s = 117;}
						else if ( (LA60_20==FLOATING_POINT_LITERAL) ) {s = 118;}
						else if ( (LA60_20==DATETIME) ) {s = 119;}
						else if ( (LA60_20==STAR) ) {s = 120;}
						else if ( (LA60_20==URI) ) {s = 121;}
						else if ( (LA60_20==COMMA||LA60_20==DOT) && (synpred25_FTS())) {s = 122;}
						else if ( (LA60_20==TILDA) && (synpred25_FTS())) {s = 123;}
						else if ( (LA60_20==CARAT) && (synpred25_FTS())) {s = 124;}
						else if ( (LA60_20==AND) && (synpred25_FTS())) {s = 125;}
						else if ( (LA60_20==AMP) && (synpred25_FTS())) {s = 126;}
						else if ( (LA60_20==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_20==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_20==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_20==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_20==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_20==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_20==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_20==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_20==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_20==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_20==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_20==DOTDOT) && (synpred25_FTS())) {s = 127;}
						 
						input.seek(index60_20);
						if ( s>=0 ) return s;
						break;

					case 32 : 
						int LA60_185 = input.LA(1);
						 
						int index60_185 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 186;}
						 
						input.seek(index60_185);
						if ( s>=0 ) return s;
						break;

					case 33 : 
						int LA60_160 = input.LA(1);
						 
						int index60_160 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 184;}
						 
						input.seek(index60_160);
						if ( s>=0 ) return s;
						break;

					case 34 : 
						int LA60_51 = input.LA(1);
						 
						int index60_51 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_51==STAR) ) {s = 20;}
						else if ( (LA60_51==COMMA||LA60_51==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_51==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_51==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_51==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_51==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_51==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_51==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_51==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_51==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_51==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_51==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_51==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_51==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_51==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_51==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_51==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_51==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_51==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_51==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_51==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_51==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_51==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_51==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_51==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_51==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_51==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_51==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_51==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_51);
						if ( s>=0 ) return s;
						break;

					case 35 : 
						int LA60_3 = input.LA(1);
						 
						int index60_3 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_3==STAR) ) {s = 20;}
						else if ( (LA60_3==COMMA||LA60_3==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_3==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_3==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_3==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_3==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_3==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_3==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_3==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_3==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_3==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_3==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_3==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_3==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_3==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_3==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_3==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_3==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_3==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_3==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_3==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_3==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_3==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_3==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_3==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_3==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_3==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_3==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_3==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_3);
						if ( s>=0 ) return s;
						break;

					case 36 : 
						int LA60_113 = input.LA(1);
						 
						int index60_113 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_113);
						if ( s>=0 ) return s;
						break;

					case 37 : 
						int LA60_50 = input.LA(1);
						 
						int index60_50 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_50==STAR) ) {s = 20;}
						else if ( (LA60_50==COMMA||LA60_50==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_50==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_50==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_50==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_50==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_50==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_50==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_50==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_50==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_50==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_50==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_50==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_50==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_50==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_50==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_50==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_50==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_50==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_50==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_50==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_50==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_50==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_50==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_50==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_50==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_50==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_50==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_50==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_50);
						if ( s>=0 ) return s;
						break;

					case 38 : 
						int LA60_120 = input.LA(1);
						 
						int index60_120 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_120);
						if ( s>=0 ) return s;
						break;

					case 39 : 
						int LA60_115 = input.LA(1);
						 
						int index60_115 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred24_FTS()) ) {s = 130;}
						else if ( (synpred25_FTS()) ) {s = 155;}
						 
						input.seek(index60_115);
						if ( s>=0 ) return s;
						break;

					case 40 : 
						int LA60_4 = input.LA(1);
						 
						int index60_4 = input.index();
						input.rewind();
						s = -1;
						if ( (LA60_4==STAR) ) {s = 20;}
						else if ( (LA60_4==COMMA||LA60_4==DOT) && (synpred25_FTS())) {s = 21;}
						else if ( (LA60_4==TILDA) && (synpred25_FTS())) {s = 22;}
						else if ( (LA60_4==CARAT) && (synpred25_FTS())) {s = 23;}
						else if ( (LA60_4==AND) && (synpred25_FTS())) {s = 24;}
						else if ( (LA60_4==AMP) && (synpred25_FTS())) {s = 25;}
						else if ( (LA60_4==RPAREN) && (synpred25_FTS())) {s = 26;}
						else if ( (LA60_4==OR) && (synpred25_FTS())) {s = 27;}
						else if ( (LA60_4==BAR) && (synpred25_FTS())) {s = 28;}
						else if ( (LA60_4==NOT) && (synpred25_FTS())) {s = 29;}
						else if ( (LA60_4==ID) && (synpred25_FTS())) {s = 30;}
						else if ( (LA60_4==FTSWORD) && (synpred25_FTS())) {s = 31;}
						else if ( (LA60_4==FTSPRE) && (synpred25_FTS())) {s = 32;}
						else if ( (LA60_4==FTSWILD) && (synpred25_FTS())) {s = 33;}
						else if ( (LA60_4==EXCLAMATION) && (synpred25_FTS())) {s = 34;}
						else if ( (LA60_4==TO) && (synpred25_FTS())) {s = 35;}
						else if ( (LA60_4==DECIMAL_INTEGER_LITERAL) && (synpred25_FTS())) {s = 36;}
						else if ( (LA60_4==FLOATING_POINT_LITERAL) && (synpred25_FTS())) {s = 37;}
						else if ( (LA60_4==DATETIME) && (synpred25_FTS())) {s = 38;}
						else if ( (LA60_4==URI) && (synpred25_FTS())) {s = 39;}
						else if ( (LA60_4==QUESTION_MARK) && (synpred25_FTS())) {s = 40;}
						else if ( (LA60_4==EQUALS) && (synpred25_FTS())) {s = 41;}
						else if ( (LA60_4==FTSPHRASE) && (synpred25_FTS())) {s = 42;}
						else if ( (LA60_4==LSQUARE) && (synpred25_FTS())) {s = 43;}
						else if ( (LA60_4==LT) && (synpred25_FTS())) {s = 44;}
						else if ( (LA60_4==LPAREN) && (synpred25_FTS())) {s = 45;}
						else if ( (LA60_4==PLUS) && (synpred25_FTS())) {s = 46;}
						else if ( (LA60_4==MINUS) && (synpred25_FTS())) {s = 47;}
						else if ( (LA60_4==DOTDOT) && (synpred37_FTS())) {s = 48;}
						 
						input.seek(index60_4);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 60, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	static final String DFA73_eotS =
		"\u01ca\uffff";
	static final String DFA73_eofS =
		"\2\uffff\13\33\1\uffff\13\42\1\uffff\1\70\1\uffff\5\33\1\130\1\uffff\5"+
		"\42\37\uffff\1\70\37\uffff\1\130\1\u009c\21\uffff\1\u00c1\60\uffff\1\u009c"+
		"\44\uffff\1\u00c1\5\uffff\1\u00f4\6\uffff\1\u0115\45\uffff\2\u00f4\37"+
		"\uffff\2\u0115\1\u015c\22\uffff\1\u0182\61\uffff\2\u015c\44\uffff\2\u0182"+
		"\27\uffff\1\u015c\22\uffff\1\u0182\14\uffff";
	static final String DFA73_minS =
		"\1\13\1\15\13\4\1\5\13\4\1\5\1\4\1\uffff\6\4\1\uffff\5\4\13\13\1\5\23"+
		"\uffff\1\4\13\13\1\5\23\uffff\2\4\1\uffff\5\13\1\0\12\13\1\4\1\uffff\5"+
		"\13\1\0\25\13\1\5\23\uffff\1\4\5\uffff\13\13\1\5\23\uffff\1\4\5\uffff"+
		"\1\4\6\13\1\4\21\13\1\5\23\uffff\2\4\13\13\1\5\23\uffff\3\4\21\13\1\5"+
		"\1\4\21\13\1\5\13\0\1\5\23\uffff\2\4\5\13\13\0\1\5\23\uffff\2\4\5\13\21"+
		"\0\1\5\1\4\21\0\1\5\1\4\14\0";
	static final String DFA73_maxS =
		"\15\150\1\146\13\150\1\146\1\150\1\uffff\6\150\1\uffff\5\150\13\25\1\146"+
		"\23\uffff\1\150\13\25\1\146\23\uffff\2\150\1\uffff\5\25\1\0\12\25\1\150"+
		"\1\uffff\5\25\1\0\25\25\1\146\23\uffff\1\150\5\uffff\13\25\1\146\23\uffff"+
		"\1\150\5\uffff\1\150\6\25\1\150\21\25\1\146\23\uffff\2\150\13\25\1\146"+
		"\23\uffff\3\150\21\25\1\146\1\150\21\25\1\146\13\0\1\146\23\uffff\2\150"+
		"\5\25\13\0\1\146\23\uffff\2\150\5\25\21\0\1\146\1\150\21\0\1\146\1\150"+
		"\14\0";
	static final String DFA73_acceptS =
		"\33\uffff\1\22\6\uffff\1\21\21\uffff\23\20\15\uffff\23\17\2\uffff\1\16"+
		"\21\uffff\1\15\34\uffff\23\14\1\uffff\1\2\1\4\1\6\1\10\1\12\14\uffff\23"+
		"\13\1\uffff\1\1\1\3\1\5\1\7\1\11\32\uffff\23\10\16\uffff\23\7\64\uffff"+
		"\23\4\23\uffff\23\3\71\uffff";
	static final String DFA73_specialS =
		"\32\uffff\1\u00a8\6\uffff\1\u00f0\6\uffff\1\75\1\26\1\33\1\136\1\u00cb"+
		"\1\62\1\1\1\111\1\144\1\71\1\u00c5\24\uffff\1\u00ae\1\u00a2\1\u00a4\1"+
		"\u0091\1\103\1\u00c0\1\56\1\u00b0\1\165\1\74\1\174\1\154\24\uffff\1\17"+
		"\1\u00ce\1\uffff\1\40\1\50\1\110\1\u008f\1\u00b8\1\u00db\1\25\1\32\1\135"+
		"\1\u00ca\1\61\1\0\1\107\1\143\1\70\1\u00c4\1\u00b6\1\uffff\1\167\1\161"+
		"\1\u00b1\1\u00e7\1\54\1\u00c8\1\u00a3\1\u0090\1\102\1\u00bf\1\55\1\u00af"+
		"\1\164\1\73\1\173\1\153\1\125\1\137\1\134\1\14\1\u00b3\1\u00d2\1\u00e6"+
		"\1\u0084\1\u00a7\1\152\1\115\24\uffff\1\65\5\uffff\1\145\1\4\1\u00f2\1"+
		"\u00c3\1\53\1\u0099\1\172\1\130\1\100\1\u00c2\1\27\24\uffff\1\u00ab\5"+
		"\uffff\1\11\1\30\1\u00f1\1\2\1\35\1\101\1\162\1\u008c\1\106\1\15\1\u00f3"+
		"\1\u00cf\1\u00b2\1\66\1\51\1\u009e\1\175\1\u0085\1\104\1\116\1\u00ee\1"+
		"\u00b9\1\u0097\1\23\1\u0089\24\uffff\1\146\1\u008d\1\u0087\1\42\1\5\1"+
		"\u00d4\1\u00e2\1\u00d0\1\u008e\1\133\1\105\1\u00b4\1\41\24\uffff\1\166"+
		"\1\13\1\u0094\1\126\1\u00de\1\u00c1\1\u00a5\1\176\1\157\1\151\1\u009b"+
		"\1\u00be\1\u00e0\1\10\1\u0096\1\u00ac\1\u00cc\1\u00e4\1\127\1\47\1\uffff"+
		"\1\34\1\u00ed\1\u0088\1\u00e8\1\u00c6\1\u00aa\1\22\1\141\1\64\1\u00c7"+
		"\1\u00e9\1\u00ef\1\171\1\u009c\1\117\1\147\1\u00eb\1\37\1\uffff\1\u00ad"+
		"\1\6\1\46\1\113\1\31\1\u00dc\1\u0080\1\u009d\1\u00b5\1\u00d5\1\123\24"+
		"\uffff\1\132\1\160\1\76\1\140\1\114\1\57\1\u00d9\1\163\1\52\1\u00d7\1"+
		"\u00d1\1\u008b\1\u00d6\1\170\1\122\1\72\1\u009a\1\24\24\uffff\1\45\1\u0083"+
		"\1\36\1\u00a1\1\u00bc\1\u00dd\1\20\1\131\1\u008a\1\u00a9\1\120\1\155\1"+
		"\63\1\u00e3\1\u0093\1\u0098\1\u00bb\1\u00a0\1\77\1\21\1\60\1\112\1\142"+
		"\1\u00ea\1\uffff\1\150\1\u0086\1\u00df\1\u00bd\1\u009f\1\177\1\7\1\156"+
		"\1\u0095\1\67\1\121\1\16\1\124\1\43\1\u00b7\1\u00d3\1\12\1\u0092\1\uffff"+
		"\1\u00e1\1\u00da\1\u00cd\1\u00ec\1\u00c9\1\u00e5\1\3\1\u0081\1\44\1\u00ba"+
		"\1\u00d8\1\u0082\1\u00a6}>";
	static final String[] DFA73_transitionS = {
			"\1\1\1\uffff\1\14\1\uffff\1\10\5\uffff\1\1\31\uffff\1\11\3\uffff\1\4"+
			"\1\5\1\3\11\uffff\1\2\16\uffff\1\6\11\uffff\1\13\7\uffff\1\12\5\uffff"+
			"\1\7\1\uffff\1\15",
			"\1\30\1\uffff\1\24\37\uffff\1\25\3\uffff\1\20\1\21\1\17\11\uffff\1\16"+
			"\16\uffff\1\22\11\uffff\1\27\7\uffff\1\26\5\uffff\1\23\1\uffff\1\31",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\1\37\71\uffff\1\34\16\uffff\1\40\2\uffff\1\36\24\uffff\1\35",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\1\46\71\uffff\1\43\16\uffff\1\47\2\uffff\1\45\24\uffff\1\44",
			"\1\67\1\66\1\75\1\73\1\uffff\1\65\1\uffff\1\101\1\uffff\1\62\1\uffff"+
			"\1\56\5\uffff\1\101\2\uffff\1\102\2\uffff\1\74\23\uffff\1\57\2\uffff"+
			"\1\76\1\52\1\53\1\51\11\uffff\1\50\3\uffff\1\103\1\77\1\100\2\uffff\1"+
			"\106\5\uffff\1\54\2\uffff\1\72\1\104\1\uffff\1\105\3\uffff\1\61\2\uffff"+
			"\1\71\4\uffff\1\60\4\uffff\1\64\1\55\1\uffff\1\63",
			"",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\107\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1"+
			"\33\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33"+
			"\3\uffff\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\4\33\1\uffff\1\33\1\uffff\1\32\1\uffff\1\33\1\uffff\1\33\5\uffff\1"+
			"\32\2\uffff\1\33\2\uffff\1\33\23\uffff\1\33\2\uffff\4\33\11\uffff\1\33"+
			"\3\uffff\3\33\2\uffff\1\33\5\uffff\1\33\2\uffff\2\33\1\uffff\1\33\3\uffff"+
			"\1\33\2\uffff\1\33\4\uffff\1\33\4\uffff\2\33\1\uffff\1\33",
			"\1\127\1\126\1\135\1\133\1\uffff\1\125\1\uffff\1\141\1\uffff\1\122\1"+
			"\uffff\1\116\5\uffff\1\141\2\uffff\1\142\2\uffff\1\134\23\uffff\1\117"+
			"\2\uffff\1\136\1\112\1\113\1\111\11\uffff\1\110\3\uffff\1\143\1\137\1"+
			"\140\2\uffff\1\146\5\uffff\1\114\2\uffff\1\132\1\144\1\uffff\1\145\3"+
			"\uffff\1\121\2\uffff\1\131\4\uffff\1\120\4\uffff\1\124\1\115\1\uffff"+
			"\1\123",
			"",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\147\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1"+
			"\42\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42"+
			"\3\uffff\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\4\42\1\uffff\1\42\1\uffff\1\41\1\uffff\1\42\1\uffff\1\42\5\uffff\1"+
			"\41\2\uffff\1\42\2\uffff\1\42\23\uffff\1\42\2\uffff\4\42\11\uffff\1\42"+
			"\3\uffff\3\42\2\uffff\1\42\5\uffff\1\42\2\uffff\2\42\1\uffff\1\42\3\uffff"+
			"\1\42\2\uffff\1\42\4\uffff\1\42\4\uffff\2\42\1\uffff\1\42",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\155\71\uffff\1\152\16\uffff\1\156\2\uffff\1\154\24\uffff\1\153",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\67\1\66\1\75\1\73\1\uffff\1\65\1\uffff\1\101\1\uffff\1\171\1\uffff"+
			"\1\165\5\uffff\1\101\2\uffff\1\102\2\uffff\1\74\23\uffff\1\166\2\uffff"+
			"\1\76\1\161\1\162\1\160\11\uffff\1\157\3\uffff\1\103\1\77\1\100\2\uffff"+
			"\1\106\5\uffff\1\163\2\uffff\1\72\1\104\1\uffff\1\105\3\uffff\1\170\2"+
			"\uffff\1\71\4\uffff\1\167\4\uffff\1\64\1\164\1\uffff\1\63",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\177\71\uffff\1\174\16\uffff\1\u0080\2\uffff\1\176\24\uffff\1\175",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\127\1\126\1\135\1\133\1\uffff\1\125\1\uffff\1\141\1\uffff\1\u008b"+
			"\1\uffff\1\u0087\5\uffff\1\141\2\uffff\1\142\2\uffff\1\134\23\uffff\1"+
			"\u0088\2\uffff\1\136\1\u0083\1\u0084\1\u0082\11\uffff\1\u0081\3\uffff"+
			"\1\143\1\137\1\140\2\uffff\1\146\5\uffff\1\u0085\2\uffff\1\132\1\144"+
			"\1\uffff\1\145\3\uffff\1\u008a\2\uffff\1\131\4\uffff\1\u0089\4\uffff"+
			"\1\124\1\u0086\1\uffff\1\123",
			"\1\u009b\1\u009a\1\u00a1\1\u009f\1\uffff\1\u0099\1\uffff\1\u00a5\1\uffff"+
			"\1\u0096\1\uffff\1\u0092\5\uffff\1\u00a5\2\uffff\1\u00a6\2\uffff\1\u00a0"+
			"\23\uffff\1\u0093\2\uffff\1\u00a2\1\u008e\1\u008f\1\u008d\11\uffff\1"+
			"\u008c\3\uffff\1\u00a7\1\u00a3\1\u00a4\2\uffff\1\u00aa\5\uffff\1\u0090"+
			"\2\uffff\1\u009e\1\u00a8\1\uffff\1\u00a9\3\uffff\1\u0095\2\uffff\1\u009d"+
			"\4\uffff\1\u0094\4\uffff\1\u0098\1\u0091\1\uffff\1\u0097",
			"",
			"\1\150\11\uffff\1\u00ab",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\uffff",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\150\11\uffff\1\150",
			"\1\u00c0\1\u00bf\1\u00c6\1\u00c4\1\uffff\1\u00be\1\uffff\1\u00ca\1\uffff"+
			"\1\u00bb\1\uffff\1\u00b7\5\uffff\1\u00ca\2\uffff\1\u00cb\2\uffff\1\u00c5"+
			"\23\uffff\1\u00b8\2\uffff\1\u00c7\1\u00b3\1\u00b4\1\u00b2\11\uffff\1"+
			"\u00b1\3\uffff\1\u00cc\1\u00c8\1\u00c9\2\uffff\1\u00cf\5\uffff\1\u00b5"+
			"\2\uffff\1\u00c3\1\u00cd\1\uffff\1\u00ce\3\uffff\1\u00ba\2\uffff\1\u00c2"+
			"\4\uffff\1\u00b9\4\uffff\1\u00bd\1\u00b6\1\uffff\1\u00bc",
			"",
			"\1\172\11\uffff\1\u00d0",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\uffff",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\172\11\uffff\1\172",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00da\71\uffff\1\u00d7\16\uffff\1\u00db\2\uffff\1\u00d9\24\uffff"+
			"\1\u00d8",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u009b\1\u009a\1\u00a1\1\u009f\1\uffff\1\u0099\1\uffff\1\u00a5\1\uffff"+
			"\1\u0096\1\uffff\1\u0092\5\uffff\1\u00a5\2\uffff\1\u00a6\2\uffff\1\u00a0"+
			"\23\uffff\1\u0093\2\uffff\1\u00a2\1\u008e\1\u008f\1\u008d\11\uffff\1"+
			"\u00dc\3\uffff\1\u00a7\1\u00a3\1\u00a4\2\uffff\1\u00aa\5\uffff\1\u0090"+
			"\2\uffff\1\u009e\1\u00a8\1\uffff\1\u00a9\3\uffff\1\u0095\2\uffff\1\u009d"+
			"\4\uffff\1\u0094\4\uffff\1\u0098\1\u0091\1\uffff\1\u0097",
			"",
			"",
			"",
			"",
			"",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00e1\71\uffff\1\u00de\16\uffff\1\u00e2\2\uffff\1\u00e0\24\uffff"+
			"\1\u00df",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u00c0\1\u00bf\1\u00c6\1\u00c4\1\uffff\1\u00be\1\uffff\1\u00ca\1\uffff"+
			"\1\u00bb\1\uffff\1\u00b7\5\uffff\1\u00ca\2\uffff\1\u00cb\2\uffff\1\u00c5"+
			"\23\uffff\1\u00b8\2\uffff\1\u00c7\1\u00b3\1\u00b4\1\u00b2\11\uffff\1"+
			"\u00e3\3\uffff\1\u00cc\1\u00c8\1\u00c9\2\uffff\1\u00cf\5\uffff\1\u00b5"+
			"\2\uffff\1\u00c3\1\u00cd\1\uffff\1\u00ce\3\uffff\1\u00ba\2\uffff\1\u00c2"+
			"\4\uffff\1\u00b9\4\uffff\1\u00bd\1\u00b6\1\uffff\1\u00bc",
			"",
			"",
			"",
			"",
			"",
			"\1\u00f3\1\u00f2\1\u00f9\1\u00f7\1\uffff\1\u00f1\1\uffff\1\u00fd\1\uffff"+
			"\1\u00ee\1\uffff\1\u00ea\5\uffff\1\u00fd\2\uffff\1\u00fe\2\uffff\1\u00f8"+
			"\23\uffff\1\u00eb\2\uffff\1\u00fa\1\u00e6\1\u00e7\1\u00e5\11\uffff\1"+
			"\u00e4\3\uffff\1\u00ff\1\u00fb\1\u00fc\2\uffff\1\u0102\5\uffff\1\u00e8"+
			"\2\uffff\1\u00f6\1\u0100\1\uffff\1\u0101\3\uffff\1\u00ed\2\uffff\1\u00f5"+
			"\4\uffff\1\u00ec\4\uffff\1\u00f0\1\u00e9\1\uffff\1\u00ef",
			"\1\u00d6\11\uffff\1\u0103",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u00d6\11\uffff\1\u00d6",
			"\1\u0104\11\uffff\1\u0104",
			"\1\u0114\1\u0113\1\u011a\1\u0118\1\uffff\1\u0112\1\uffff\1\u011e\1\uffff"+
			"\1\u010f\1\uffff\1\u010b\5\uffff\1\u011e\2\uffff\1\u011f\2\uffff\1\u0119"+
			"\23\uffff\1\u010c\2\uffff\1\u011b\1\u0107\1\u0108\1\u0106\11\uffff\1"+
			"\u0105\3\uffff\1\u0120\1\u011c\1\u011d\2\uffff\1\u0123\5\uffff\1\u0109"+
			"\2\uffff\1\u0117\1\u0121\1\uffff\1\u0122\3\uffff\1\u010e\2\uffff\1\u0116"+
			"\4\uffff\1\u010d\4\uffff\1\u0111\1\u010a\1\uffff\1\u0110",
			"\1\u00dd\11\uffff\1\u0124",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u00dd\11\uffff\1\u00dd",
			"\1\u0125\11\uffff\1\u0125",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u012a\71\uffff\1\u0127\16\uffff\1\u012b\2\uffff\1\u0129\24\uffff"+
			"\1\u0128",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u00f3\1\u00f2\1\u00f9\1\u00f7\1\uffff\1\u00f1\1\uffff\1\u00fd\1\uffff"+
			"\1\u00ee\1\uffff\1\u00ea\5\uffff\1\u00fd\2\uffff\1\u00fe\2\uffff\1\u00f8"+
			"\23\uffff\1\u00eb\2\uffff\1\u00fa\1\u00e6\1\u00e7\1\u00e5\11\uffff\1"+
			"\u012c\3\uffff\1\u00ff\1\u00fb\1\u00fc\2\uffff\1\u0102\5\uffff\1\u00e8"+
			"\2\uffff\1\u00f6\1\u0100\1\uffff\1\u0101\3\uffff\1\u00ed\2\uffff\1\u00f5"+
			"\4\uffff\1\u00ec\4\uffff\1\u00f0\1\u00e9\1\uffff\1\u00ef",
			"\1\u00f3\1\u00f2\1\u00f9\1\u00f7\1\uffff\1\u00f1\1\uffff\1\u00fd\1\uffff"+
			"\1\u0137\1\uffff\1\u0133\5\uffff\1\u00fd\2\uffff\1\u00fe\2\uffff\1\u00f8"+
			"\23\uffff\1\u0134\2\uffff\1\u00fa\1\u012f\1\u0130\1\u012e\11\uffff\1"+
			"\u012d\3\uffff\1\u00ff\1\u00fb\1\u00fc\2\uffff\1\u0102\5\uffff\1\u0131"+
			"\2\uffff\1\u00f6\1\u0100\1\uffff\1\u0101\3\uffff\1\u0136\2\uffff\1\u00f5"+
			"\4\uffff\1\u0135\4\uffff\1\u00f0\1\u0132\1\uffff\1\u0138",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u013d\71\uffff\1\u013a\16\uffff\1\u013e\2\uffff\1\u013c\24\uffff"+
			"\1\u013b",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u0114\1\u0113\1\u011a\1\u0118\1\uffff\1\u0112\1\uffff\1\u011e\1\uffff"+
			"\1\u010f\1\uffff\1\u010b\5\uffff\1\u011e\2\uffff\1\u011f\2\uffff\1\u0119"+
			"\23\uffff\1\u010c\2\uffff\1\u011b\1\u0107\1\u0108\1\u0106\11\uffff\1"+
			"\u013f\3\uffff\1\u0120\1\u011c\1\u011d\2\uffff\1\u0123\5\uffff\1\u0109"+
			"\2\uffff\1\u0117\1\u0121\1\uffff\1\u0122\3\uffff\1\u010e\2\uffff\1\u0116"+
			"\4\uffff\1\u010d\4\uffff\1\u0111\1\u010a\1\uffff\1\u0110",
			"\1\u0114\1\u0113\1\u011a\1\u0118\1\uffff\1\u0112\1\uffff\1\u011e\1\uffff"+
			"\1\u014a\1\uffff\1\u0146\5\uffff\1\u011e\2\uffff\1\u011f\2\uffff\1\u0119"+
			"\23\uffff\1\u0147\2\uffff\1\u011b\1\u0142\1\u0143\1\u0141\11\uffff\1"+
			"\u0140\3\uffff\1\u0120\1\u011c\1\u011d\2\uffff\1\u0123\5\uffff\1\u0144"+
			"\2\uffff\1\u0117\1\u0121\1\uffff\1\u0122\3\uffff\1\u0149\2\uffff\1\u0116"+
			"\4\uffff\1\u0148\4\uffff\1\u0111\1\u0145\1\uffff\1\u014b",
			"\1\u015b\1\u015a\1\u0161\1\u015f\1\uffff\1\u0159\1\uffff\1\u0165\1\uffff"+
			"\1\u0156\1\uffff\1\u0152\5\uffff\1\u0165\2\uffff\1\u0166\2\uffff\1\u0160"+
			"\23\uffff\1\u0153\2\uffff\1\u0162\1\u014e\1\u014f\1\u014d\11\uffff\1"+
			"\u014c\3\uffff\1\u0167\1\u0163\1\u0164\2\uffff\1\u016a\5\uffff\1\u0150"+
			"\2\uffff\1\u015e\1\u0168\1\uffff\1\u0169\3\uffff\1\u0155\2\uffff\1\u015d"+
			"\4\uffff\1\u0154\4\uffff\1\u0158\1\u0151\1\uffff\1\u0157",
			"\1\u0126\11\uffff\1\u016b",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u0126\11\uffff\1\u0126",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u0170\71\uffff\1\u016d\16\uffff\1\u0171\2\uffff\1\u016f\24\uffff"+
			"\1\u016e",
			"\1\u0181\1\u0180\1\u0187\1\u0185\1\uffff\1\u017f\1\uffff\1\u018b\1\uffff"+
			"\1\u017c\1\uffff\1\u0178\5\uffff\1\u018b\2\uffff\1\u018c\2\uffff\1\u0186"+
			"\23\uffff\1\u0179\2\uffff\1\u0188\1\u0174\1\u0175\1\u0173\11\uffff\1"+
			"\u0172\3\uffff\1\u018d\1\u0189\1\u018a\2\uffff\1\u0190\5\uffff\1\u0176"+
			"\2\uffff\1\u0184\1\u018e\1\uffff\1\u018f\3\uffff\1\u017b\2\uffff\1\u0183"+
			"\4\uffff\1\u017a\4\uffff\1\u017e\1\u0177\1\uffff\1\u017d",
			"\1\u0139\11\uffff\1\u0191",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0139\11\uffff\1\u0139",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0196\71\uffff\1\u0193\16\uffff\1\u0197\2\uffff\1\u0195\24\uffff"+
			"\1\u0194",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u019b\71\uffff\1\u0198\16\uffff\1\u019c\2\uffff\1\u019a\24\uffff"+
			"\1\u0199",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u015b\1\u015a\1\u0161\1\u015f\1\uffff\1\u0159\1\uffff\1\u0165\1\uffff"+
			"\1\u0156\1\uffff\1\u0152\5\uffff\1\u0165\2\uffff\1\u0166\2\uffff\1\u0160"+
			"\23\uffff\1\u0153\2\uffff\1\u0162\1\u014e\1\u014f\1\u014d\11\uffff\1"+
			"\u019d\3\uffff\1\u0167\1\u0163\1\u0164\2\uffff\1\u016a\5\uffff\1\u0150"+
			"\2\uffff\1\u015e\1\u0168\1\uffff\1\u0169\3\uffff\1\u0155\2\uffff\1\u015d"+
			"\4\uffff\1\u0154\4\uffff\1\u0158\1\u0151\1\uffff\1\u0157",
			"\1\u015b\1\u015a\1\u0161\1\u015f\1\uffff\1\u0159\1\uffff\1\u0165\1\uffff"+
			"\1\u01a8\1\uffff\1\u01a4\5\uffff\1\u0165\2\uffff\1\u0166\2\uffff\1\u0160"+
			"\23\uffff\1\u01a5\2\uffff\1\u0162\1\u01a0\1\u01a1\1\u019f\11\uffff\1"+
			"\u019e\3\uffff\1\u0167\1\u0163\1\u0164\2\uffff\1\u016a\5\uffff\1\u01a2"+
			"\2\uffff\1\u015e\1\u0168\1\uffff\1\u0169\3\uffff\1\u01a7\2\uffff\1\u015d"+
			"\4\uffff\1\u01a6\4\uffff\1\u0158\1\u01a3\1\uffff\1\u01a9",
			"\1\u016c\11\uffff\1\u01aa",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\u016c\11\uffff\1\u016c",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01ae\71\uffff\1\u01ab\16\uffff\1\u01af\2\uffff\1\u01ad\24\uffff"+
			"\1\u01ac",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u0181\1\u0180\1\u0187\1\u0185\1\uffff\1\u017f\1\uffff\1\u018b\1\uffff"+
			"\1\u017c\1\uffff\1\u0178\5\uffff\1\u018b\2\uffff\1\u018c\2\uffff\1\u0186"+
			"\23\uffff\1\u0179\2\uffff\1\u0188\1\u0174\1\u0175\1\u0173\11\uffff\1"+
			"\u01b0\3\uffff\1\u018d\1\u0189\1\u018a\2\uffff\1\u0190\5\uffff\1\u0176"+
			"\2\uffff\1\u0184\1\u018e\1\uffff\1\u018f\3\uffff\1\u017b\2\uffff\1\u0183"+
			"\4\uffff\1\u017a\4\uffff\1\u017e\1\u0177\1\uffff\1\u017d",
			"\1\u0181\1\u0180\1\u0187\1\u0185\1\uffff\1\u017f\1\uffff\1\u018b\1\uffff"+
			"\1\u01bb\1\uffff\1\u01b7\5\uffff\1\u018b\2\uffff\1\u018c\2\uffff\1\u0186"+
			"\23\uffff\1\u01b8\2\uffff\1\u0188\1\u01b3\1\u01b4\1\u01b2\11\uffff\1"+
			"\u01b1\3\uffff\1\u018d\1\u0189\1\u018a\2\uffff\1\u0190\5\uffff\1\u01b5"+
			"\2\uffff\1\u0184\1\u018e\1\uffff\1\u018f\3\uffff\1\u01ba\2\uffff\1\u0183"+
			"\4\uffff\1\u01b9\4\uffff\1\u017e\1\u01b6\1\uffff\1\u01bc",
			"\1\u0192\11\uffff\1\u01bd",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\u0192\11\uffff\1\u0192",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01c1\71\uffff\1\u01be\16\uffff\1\u01c2\2\uffff\1\u01c0\24\uffff"+
			"\1\u01bf",
			"\1\u015b\1\u015a\1\u0161\1\u015f\1\uffff\1\u0159\1\uffff\1\u0165\1\uffff"+
			"\1\u01a8\1\uffff\1\u01a4\5\uffff\1\u0165\2\uffff\1\u0166\2\uffff\1\u0160"+
			"\23\uffff\1\u01a5\2\uffff\1\u0162\1\u01a0\1\u01a1\1\u019f\11\uffff\1"+
			"\u01c3\3\uffff\1\u0167\1\u0163\1\u0164\2\uffff\1\u016a\5\uffff\1\u01a2"+
			"\2\uffff\1\u015e\1\u0168\1\uffff\1\u0169\3\uffff\1\u01a7\2\uffff\1\u015d"+
			"\4\uffff\1\u01a6\4\uffff\1\u0158\1\u01a3\1\uffff\1\u01a9",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01c7\71\uffff\1\u01c4\16\uffff\1\u01c8\2\uffff\1\u01c6\24\uffff"+
			"\1\u01c5",
			"\1\u0181\1\u0180\1\u0187\1\u0185\1\uffff\1\u017f\1\uffff\1\u018b\1\uffff"+
			"\1\u01bb\1\uffff\1\u01b7\5\uffff\1\u018b\2\uffff\1\u018c\2\uffff\1\u0186"+
			"\23\uffff\1\u01b8\2\uffff\1\u0188\1\u01b3\1\u01b4\1\u01b2\11\uffff\1"+
			"\u01c9\3\uffff\1\u018d\1\u0189\1\u018a\2\uffff\1\u0190\5\uffff\1\u01b5"+
			"\2\uffff\1\u0184\1\u018e\1\uffff\1\u018f\3\uffff\1\u01ba\2\uffff\1\u0183"+
			"\4\uffff\1\u01b9\4\uffff\1\u017e\1\u01b6\1\uffff\1\u01bc",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff"
	};

	static final short[] DFA73_eot = DFA.unpackEncodedString(DFA73_eotS);
	static final short[] DFA73_eof = DFA.unpackEncodedString(DFA73_eofS);
	static final char[] DFA73_min = DFA.unpackEncodedStringToUnsignedChars(DFA73_minS);
	static final char[] DFA73_max = DFA.unpackEncodedStringToUnsignedChars(DFA73_maxS);
	static final short[] DFA73_accept = DFA.unpackEncodedString(DFA73_acceptS);
	static final short[] DFA73_special = DFA.unpackEncodedString(DFA73_specialS);
	static final short[][] DFA73_transition;

	static {
		int numStates = DFA73_transitionS.length;
		DFA73_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA73_transition[i] = DFA.unpackEncodedString(DFA73_transitionS[i]);
		}
	}

	protected class DFA73 extends DFA {

		public DFA73(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 73;
			this.eot = DFA73_eot;
			this.eof = DFA73_eof;
			this.min = DFA73_min;
			this.max = DFA73_max;
			this.accept = DFA73_accept;
			this.special = DFA73_special;
			this.transition = DFA73_transition;
		}
		@Override
		public String getDescription() {
			return "835:1: ftsWord : ( ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase DOT | COMMA ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ftsWordBase ( DOT | COMMA ) ftsWordBase )=> ftsWordBase ( DOT | COMMA ) ftsWordBase | ( ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) )=> ( DOT | COMMA ) ftsWordBase ( DOT | COMMA ) | ( ftsWordBase ( DOT | COMMA ) )=> ftsWordBase ( DOT | COMMA ) | ( DOT | COMMA ) ftsWordBase | ftsWordBase );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA73_117 = input.LA(1);
						 
						int index73_117 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_117==COMMA||LA73_117==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_117);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA73_46 = input.LA(1);
						 
						int index73_46 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_46==COMMA||LA73_46==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_46);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA73_217 = input.LA(1);
						 
						int index73_217 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_217==COMMA||LA73_217==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_217);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA73_451 = input.LA(1);
						 
						int index73_451 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_451);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA73_178 = input.LA(1);
						 
						int index73_178 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_178==COMMA||LA73_178==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_178);
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA73_263 = input.LA(1);
						 
						int index73_263 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_263==COMMA||LA73_263==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_263);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA73_333 = input.LA(1);
						 
						int index73_333 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_333);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA73_432 = input.LA(1);
						 
						int index73_432 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_432);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA73_305 = input.LA(1);
						 
						int index73_305 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_305==COMMA||LA73_305==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_305);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA73_214 = input.LA(1);
						 
						int index73_214 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_214==ID) ) {s = 228;}
						else if ( (LA73_214==FTSWORD) ) {s = 229;}
						else if ( (LA73_214==FTSPRE) ) {s = 230;}
						else if ( (LA73_214==FTSWILD) ) {s = 231;}
						else if ( (LA73_214==NOT) ) {s = 232;}
						else if ( (LA73_214==TO) ) {s = 233;}
						else if ( (LA73_214==DECIMAL_INTEGER_LITERAL) ) {s = 234;}
						else if ( (LA73_214==FLOATING_POINT_LITERAL) ) {s = 235;}
						else if ( (LA73_214==STAR) ) {s = 236;}
						else if ( (LA73_214==QUESTION_MARK) ) {s = 237;}
						else if ( (LA73_214==DATETIME) ) {s = 238;}
						else if ( (LA73_214==URI) ) {s = 239;}
						else if ( (LA73_214==TILDA) && (synpred48_FTS())) {s = 240;}
						else if ( (LA73_214==CARAT) && (synpred48_FTS())) {s = 241;}
						else if ( (LA73_214==AND) && (synpred48_FTS())) {s = 242;}
						else if ( (LA73_214==AMP) && (synpred48_FTS())) {s = 243;}
						else if ( (LA73_214==EOF) && (synpred48_FTS())) {s = 244;}
						else if ( (LA73_214==RPAREN) && (synpred48_FTS())) {s = 245;}
						else if ( (LA73_214==OR) && (synpred48_FTS())) {s = 246;}
						else if ( (LA73_214==BAR) && (synpred48_FTS())) {s = 247;}
						else if ( (LA73_214==EXCLAMATION) && (synpred48_FTS())) {s = 248;}
						else if ( (LA73_214==AT) && (synpred48_FTS())) {s = 249;}
						else if ( (LA73_214==FTSPHRASE) && (synpred48_FTS())) {s = 250;}
						else if ( (LA73_214==LSQUARE) && (synpred48_FTS())) {s = 251;}
						else if ( (LA73_214==LT) && (synpred48_FTS())) {s = 252;}
						else if ( (LA73_214==COMMA||LA73_214==DOT) && (synpred48_FTS())) {s = 253;}
						else if ( (LA73_214==EQUALS) && (synpred48_FTS())) {s = 254;}
						else if ( (LA73_214==LPAREN) && (synpred48_FTS())) {s = 255;}
						else if ( (LA73_214==PERCENT) && (synpred48_FTS())) {s = 256;}
						else if ( (LA73_214==PLUS) && (synpred48_FTS())) {s = 257;}
						else if ( (LA73_214==MINUS) && (synpred48_FTS())) {s = 258;}
						 
						input.seek(index73_214);
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA73_442 = input.LA(1);
						 
						int index73_442 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_442);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA73_293 = input.LA(1);
						 
						int index73_293 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_293==ID) ) {s = 320;}
						else if ( (LA73_293==FTSWORD) ) {s = 321;}
						else if ( (LA73_293==FTSPRE) ) {s = 322;}
						else if ( (LA73_293==FTSWILD) ) {s = 323;}
						else if ( (LA73_293==NOT) ) {s = 324;}
						else if ( (LA73_293==TO) ) {s = 325;}
						else if ( (LA73_293==DECIMAL_INTEGER_LITERAL) ) {s = 326;}
						else if ( (LA73_293==FLOATING_POINT_LITERAL) ) {s = 327;}
						else if ( (LA73_293==STAR) ) {s = 328;}
						else if ( (LA73_293==QUESTION_MARK) ) {s = 329;}
						else if ( (LA73_293==DATETIME) ) {s = 330;}
						else if ( (LA73_293==URI) ) {s = 331;}
						else if ( (LA73_293==TILDA) && (synpred47_FTS())) {s = 273;}
						else if ( (LA73_293==CARAT) && (synpred47_FTS())) {s = 274;}
						else if ( (LA73_293==AND) && (synpred47_FTS())) {s = 275;}
						else if ( (LA73_293==AMP) && (synpred47_FTS())) {s = 276;}
						else if ( (LA73_293==EOF) && (synpred47_FTS())) {s = 277;}
						else if ( (LA73_293==RPAREN) && (synpred47_FTS())) {s = 278;}
						else if ( (LA73_293==OR) && (synpred47_FTS())) {s = 279;}
						else if ( (LA73_293==BAR) && (synpred47_FTS())) {s = 280;}
						else if ( (LA73_293==EXCLAMATION) && (synpred47_FTS())) {s = 281;}
						else if ( (LA73_293==AT) && (synpred47_FTS())) {s = 282;}
						else if ( (LA73_293==FTSPHRASE) && (synpred47_FTS())) {s = 283;}
						else if ( (LA73_293==LSQUARE) && (synpred47_FTS())) {s = 284;}
						else if ( (LA73_293==LT) && (synpred47_FTS())) {s = 285;}
						else if ( (LA73_293==COMMA||LA73_293==DOT) && (synpred47_FTS())) {s = 286;}
						else if ( (LA73_293==EQUALS) && (synpred47_FTS())) {s = 287;}
						else if ( (LA73_293==LPAREN) && (synpred47_FTS())) {s = 288;}
						else if ( (LA73_293==PERCENT) && (synpred47_FTS())) {s = 289;}
						else if ( (LA73_293==PLUS) && (synpred47_FTS())) {s = 290;}
						else if ( (LA73_293==MINUS) && (synpred47_FTS())) {s = 291;}
						 
						input.seek(index73_293);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA73_143 = input.LA(1);
						 
						int index73_143 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_143==COMMA||LA73_143==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_143);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA73_223 = input.LA(1);
						 
						int index73_223 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_223==COMMA||LA73_223==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_223);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA73_437 = input.LA(1);
						 
						int index73_437 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_437);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA73_103 = input.LA(1);
						 
						int index73_103 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_103==ID) ) {s = 129;}
						else if ( (LA73_103==FTSWORD) ) {s = 130;}
						else if ( (LA73_103==FTSPRE) ) {s = 131;}
						else if ( (LA73_103==FTSWILD) ) {s = 132;}
						else if ( (LA73_103==NOT) ) {s = 133;}
						else if ( (LA73_103==TO) ) {s = 134;}
						else if ( (LA73_103==DECIMAL_INTEGER_LITERAL) ) {s = 135;}
						else if ( (LA73_103==FLOATING_POINT_LITERAL) ) {s = 136;}
						else if ( (LA73_103==STAR) ) {s = 137;}
						else if ( (LA73_103==QUESTION_MARK) ) {s = 138;}
						else if ( (LA73_103==DATETIME) ) {s = 139;}
						else if ( (LA73_103==URI) ) {s = 83;}
						else if ( (LA73_103==TILDA) && (synpred55_FTS())) {s = 84;}
						else if ( (LA73_103==CARAT) && (synpred55_FTS())) {s = 85;}
						else if ( (LA73_103==AND) && (synpred55_FTS())) {s = 86;}
						else if ( (LA73_103==AMP) && (synpred55_FTS())) {s = 87;}
						else if ( (LA73_103==EOF) && (synpred55_FTS())) {s = 88;}
						else if ( (LA73_103==RPAREN) && (synpred55_FTS())) {s = 89;}
						else if ( (LA73_103==OR) && (synpred55_FTS())) {s = 90;}
						else if ( (LA73_103==BAR) && (synpred55_FTS())) {s = 91;}
						else if ( (LA73_103==EXCLAMATION) && (synpred55_FTS())) {s = 92;}
						else if ( (LA73_103==AT) && (synpred55_FTS())) {s = 93;}
						else if ( (LA73_103==FTSPHRASE) && (synpred55_FTS())) {s = 94;}
						else if ( (LA73_103==LSQUARE) && (synpred55_FTS())) {s = 95;}
						else if ( (LA73_103==LT) && (synpred55_FTS())) {s = 96;}
						else if ( (LA73_103==COMMA||LA73_103==DOT) && (synpred55_FTS())) {s = 97;}
						else if ( (LA73_103==EQUALS) && (synpred55_FTS())) {s = 98;}
						else if ( (LA73_103==LPAREN) && (synpred55_FTS())) {s = 99;}
						else if ( (LA73_103==PERCENT) && (synpred55_FTS())) {s = 100;}
						else if ( (LA73_103==PLUS) && (synpred55_FTS())) {s = 101;}
						else if ( (LA73_103==MINUS) && (synpred55_FTS())) {s = 102;}
						 
						input.seek(index73_103);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA73_407 = input.LA(1);
						 
						int index73_407 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_407==COMMA||LA73_407==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_407);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA73_420 = input.LA(1);
						 
						int index73_420 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_420);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA73_319 = input.LA(1);
						 
						int index73_319 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_319==COMMA||LA73_319==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_319);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA73_237 = input.LA(1);
						 
						int index73_237 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_237==COMMA||LA73_237==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_237);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA73_380 = input.LA(1);
						 
						int index73_380 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_380);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA73_112 = input.LA(1);
						 
						int index73_112 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_112==COMMA||LA73_112==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_112);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA73_41 = input.LA(1);
						 
						int index73_41 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_41==COMMA||LA73_41==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_41);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA73_187 = input.LA(1);
						 
						int index73_187 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_187==COMMA||LA73_187==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_187);
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA73_215 = input.LA(1);
						 
						int index73_215 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_215==DOT) ) {s = 259;}
						else if ( (LA73_215==COMMA) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_215);
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA73_336 = input.LA(1);
						 
						int index73_336 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_336);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA73_113 = input.LA(1);
						 
						int index73_113 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_113==COMMA||LA73_113==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_113);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA73_42 = input.LA(1);
						 
						int index73_42 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_42==COMMA||LA73_42==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_42);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA73_313 = input.LA(1);
						 
						int index73_313 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_313==ID) ) {s = 370;}
						else if ( (LA73_313==FTSWORD) ) {s = 371;}
						else if ( (LA73_313==FTSPRE) ) {s = 372;}
						else if ( (LA73_313==FTSWILD) ) {s = 373;}
						else if ( (LA73_313==NOT) ) {s = 374;}
						else if ( (LA73_313==TO) ) {s = 375;}
						else if ( (LA73_313==DECIMAL_INTEGER_LITERAL) ) {s = 376;}
						else if ( (LA73_313==FLOATING_POINT_LITERAL) ) {s = 377;}
						else if ( (LA73_313==STAR) ) {s = 378;}
						else if ( (LA73_313==QUESTION_MARK) ) {s = 379;}
						else if ( (LA73_313==DATETIME) ) {s = 380;}
						else if ( (LA73_313==URI) ) {s = 381;}
						else if ( (LA73_313==TILDA) && (synpred43_FTS())) {s = 382;}
						else if ( (LA73_313==CARAT) && (synpred43_FTS())) {s = 383;}
						else if ( (LA73_313==AND) && (synpred43_FTS())) {s = 384;}
						else if ( (LA73_313==AMP) && (synpred43_FTS())) {s = 385;}
						else if ( (LA73_313==EOF) && (synpred43_FTS())) {s = 386;}
						else if ( (LA73_313==RPAREN) && (synpred43_FTS())) {s = 387;}
						else if ( (LA73_313==OR) && (synpred43_FTS())) {s = 388;}
						else if ( (LA73_313==BAR) && (synpred43_FTS())) {s = 389;}
						else if ( (LA73_313==EXCLAMATION) && (synpred43_FTS())) {s = 390;}
						else if ( (LA73_313==AT) && (synpred43_FTS())) {s = 391;}
						else if ( (LA73_313==FTSPHRASE) && (synpred43_FTS())) {s = 392;}
						else if ( (LA73_313==LSQUARE) && (synpred43_FTS())) {s = 393;}
						else if ( (LA73_313==LT) && (synpred43_FTS())) {s = 394;}
						else if ( (LA73_313==COMMA||LA73_313==DOT) && (synpred43_FTS())) {s = 395;}
						else if ( (LA73_313==EQUALS) && (synpred43_FTS())) {s = 396;}
						else if ( (LA73_313==LPAREN) && (synpred43_FTS())) {s = 397;}
						else if ( (LA73_313==PERCENT) && (synpred43_FTS())) {s = 398;}
						else if ( (LA73_313==PLUS) && (synpred43_FTS())) {s = 399;}
						else if ( (LA73_313==MINUS) && (synpred43_FTS())) {s = 400;}
						 
						input.seek(index73_313);
						if ( s>=0 ) return s;
						break;

					case 29 : 
						int LA73_218 = input.LA(1);
						 
						int index73_218 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_218==COMMA||LA73_218==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_218);
						if ( s>=0 ) return s;
						break;

					case 30 : 
						int LA73_403 = input.LA(1);
						 
						int index73_403 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_403==DOT) ) {s = 445;}
						else if ( (LA73_403==COMMA) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_403);
						if ( s>=0 ) return s;
						break;

					case 31 : 
						int LA73_330 = input.LA(1);
						 
						int index73_330 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_330==COMMA||LA73_330==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_330);
						if ( s>=0 ) return s;
						break;

					case 32 : 
						int LA73_106 = input.LA(1);
						 
						int index73_106 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_106==DOT) ) {s = 171;}
						else if ( (LA73_106==COMMA) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_106);
						if ( s>=0 ) return s;
						break;

					case 33 : 
						int LA73_271 = input.LA(1);
						 
						int index73_271 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_271==COMMA||LA73_271==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_271);
						if ( s>=0 ) return s;
						break;

					case 34 : 
						int LA73_262 = input.LA(1);
						 
						int index73_262 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_262==COMMA||LA73_262==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_262);
						if ( s>=0 ) return s;
						break;

					case 35 : 
						int LA73_439 = input.LA(1);
						 
						int index73_439 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_439);
						if ( s>=0 ) return s;
						break;

					case 36 : 
						int LA73_453 = input.LA(1);
						 
						int index73_453 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_453);
						if ( s>=0 ) return s;
						break;

					case 37 : 
						int LA73_401 = input.LA(1);
						 
						int index73_401 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_401==ID) ) {s = 432;}
						else if ( (LA73_401==FTSWORD) ) {s = 371;}
						else if ( (LA73_401==FTSPRE) ) {s = 372;}
						else if ( (LA73_401==FTSWILD) ) {s = 373;}
						else if ( (LA73_401==NOT) ) {s = 374;}
						else if ( (LA73_401==TO) ) {s = 375;}
						else if ( (LA73_401==DECIMAL_INTEGER_LITERAL) ) {s = 376;}
						else if ( (LA73_401==FLOATING_POINT_LITERAL) ) {s = 377;}
						else if ( (LA73_401==STAR) ) {s = 378;}
						else if ( (LA73_401==QUESTION_MARK) ) {s = 379;}
						else if ( (LA73_401==DATETIME) ) {s = 380;}
						else if ( (LA73_401==URI) ) {s = 381;}
						else if ( (LA73_401==TILDA) && (synpred43_FTS())) {s = 382;}
						else if ( (LA73_401==CARAT) && (synpred43_FTS())) {s = 383;}
						else if ( (LA73_401==AND) && (synpred43_FTS())) {s = 384;}
						else if ( (LA73_401==AMP) && (synpred43_FTS())) {s = 385;}
						else if ( (LA73_401==EOF) && (synpred43_FTS())) {s = 386;}
						else if ( (LA73_401==RPAREN) && (synpred43_FTS())) {s = 387;}
						else if ( (LA73_401==OR) && (synpred43_FTS())) {s = 388;}
						else if ( (LA73_401==BAR) && (synpred43_FTS())) {s = 389;}
						else if ( (LA73_401==EXCLAMATION) && (synpred43_FTS())) {s = 390;}
						else if ( (LA73_401==AT) && (synpred43_FTS())) {s = 391;}
						else if ( (LA73_401==FTSPHRASE) && (synpred43_FTS())) {s = 392;}
						else if ( (LA73_401==LSQUARE) && (synpred43_FTS())) {s = 393;}
						else if ( (LA73_401==LT) && (synpred43_FTS())) {s = 394;}
						else if ( (LA73_401==COMMA||LA73_401==DOT) && (synpred43_FTS())) {s = 395;}
						else if ( (LA73_401==EQUALS) && (synpred43_FTS())) {s = 396;}
						else if ( (LA73_401==LPAREN) && (synpred43_FTS())) {s = 397;}
						else if ( (LA73_401==PERCENT) && (synpred43_FTS())) {s = 398;}
						else if ( (LA73_401==PLUS) && (synpred43_FTS())) {s = 399;}
						else if ( (LA73_401==MINUS) && (synpred43_FTS())) {s = 400;}
						 
						input.seek(index73_401);
						if ( s>=0 ) return s;
						break;

					case 38 : 
						int LA73_334 = input.LA(1);
						 
						int index73_334 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_334);
						if ( s>=0 ) return s;
						break;

					case 39 : 
						int LA73_311 = input.LA(1);
						 
						int index73_311 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_311==COMMA||LA73_311==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_311);
						if ( s>=0 ) return s;
						break;

					case 40 : 
						int LA73_107 = input.LA(1);
						 
						int index73_107 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_107==COMMA||LA73_107==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_107);
						if ( s>=0 ) return s;
						break;

					case 41 : 
						int LA73_228 = input.LA(1);
						 
						int index73_228 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_228==COMMA||LA73_228==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_228);
						if ( s>=0 ) return s;
						break;

					case 42 : 
						int LA73_371 = input.LA(1);
						 
						int index73_371 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_371);
						if ( s>=0 ) return s;
						break;

					case 43 : 
						int LA73_181 = input.LA(1);
						 
						int index73_181 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_181==COMMA||LA73_181==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_181);
						if ( s>=0 ) return s;
						break;

					case 44 : 
						int LA73_128 = input.LA(1);
						 
						int index73_128 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_128==COMMA||LA73_128==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_128);
						if ( s>=0 ) return s;
						break;

					case 45 : 
						int LA73_134 = input.LA(1);
						 
						int index73_134 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_134==COMMA||LA73_134==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_134);
						if ( s>=0 ) return s;
						break;

					case 46 : 
						int LA73_77 = input.LA(1);
						 
						int index73_77 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_77==COMMA||LA73_77==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_77);
						if ( s>=0 ) return s;
						break;

					case 47 : 
						int LA73_368 = input.LA(1);
						 
						int index73_368 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_368==COMMA||LA73_368==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_368);
						if ( s>=0 ) return s;
						break;

					case 48 : 
						int LA73_421 = input.LA(1);
						 
						int index73_421 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_421);
						if ( s>=0 ) return s;
						break;

					case 49 : 
						int LA73_116 = input.LA(1);
						 
						int index73_116 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_116==COMMA||LA73_116==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_116);
						if ( s>=0 ) return s;
						break;

					case 50 : 
						int LA73_45 = input.LA(1);
						 
						int index73_45 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_45==COMMA||LA73_45==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_45);
						if ( s>=0 ) return s;
						break;

					case 51 : 
						int LA73_413 = input.LA(1);
						 
						int index73_413 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_413);
						if ( s>=0 ) return s;
						break;

					case 52 : 
						int LA73_321 = input.LA(1);
						 
						int index73_321 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_321==COMMA||LA73_321==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_321);
						if ( s>=0 ) return s;
						break;

					case 53 : 
						int LA73_171 = input.LA(1);
						 
						int index73_171 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_171==ID) ) {s = 220;}
						else if ( (LA73_171==FTSWORD) ) {s = 141;}
						else if ( (LA73_171==FTSPRE) ) {s = 142;}
						else if ( (LA73_171==FTSWILD) ) {s = 143;}
						else if ( (LA73_171==NOT) ) {s = 144;}
						else if ( (LA73_171==TO) ) {s = 145;}
						else if ( (LA73_171==DECIMAL_INTEGER_LITERAL) ) {s = 146;}
						else if ( (LA73_171==FLOATING_POINT_LITERAL) ) {s = 147;}
						else if ( (LA73_171==STAR) ) {s = 148;}
						else if ( (LA73_171==QUESTION_MARK) ) {s = 149;}
						else if ( (LA73_171==DATETIME) ) {s = 150;}
						else if ( (LA73_171==URI) ) {s = 151;}
						else if ( (LA73_171==TILDA) && (synpred52_FTS())) {s = 152;}
						else if ( (LA73_171==CARAT) && (synpred52_FTS())) {s = 153;}
						else if ( (LA73_171==AND) && (synpred52_FTS())) {s = 154;}
						else if ( (LA73_171==AMP) && (synpred52_FTS())) {s = 155;}
						else if ( (LA73_171==EOF) && (synpred52_FTS())) {s = 156;}
						else if ( (LA73_171==RPAREN) && (synpred52_FTS())) {s = 157;}
						else if ( (LA73_171==OR) && (synpred52_FTS())) {s = 158;}
						else if ( (LA73_171==BAR) && (synpred52_FTS())) {s = 159;}
						else if ( (LA73_171==EXCLAMATION) && (synpred52_FTS())) {s = 160;}
						else if ( (LA73_171==AT) && (synpred52_FTS())) {s = 161;}
						else if ( (LA73_171==FTSPHRASE) && (synpred52_FTS())) {s = 162;}
						else if ( (LA73_171==LSQUARE) && (synpred52_FTS())) {s = 163;}
						else if ( (LA73_171==LT) && (synpred52_FTS())) {s = 164;}
						else if ( (LA73_171==COMMA||LA73_171==DOT) && (synpred52_FTS())) {s = 165;}
						else if ( (LA73_171==EQUALS) && (synpred52_FTS())) {s = 166;}
						else if ( (LA73_171==LPAREN) && (synpred52_FTS())) {s = 167;}
						else if ( (LA73_171==PERCENT) && (synpred52_FTS())) {s = 168;}
						else if ( (LA73_171==PLUS) && (synpred52_FTS())) {s = 169;}
						else if ( (LA73_171==MINUS) && (synpred52_FTS())) {s = 170;}
						 
						input.seek(index73_171);
						if ( s>=0 ) return s;
						break;

					case 54 : 
						int LA73_227 = input.LA(1);
						 
						int index73_227 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_227==COMMA||LA73_227==DOT) ) {s = 293;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_227);
						if ( s>=0 ) return s;
						break;

					case 55 : 
						int LA73_435 = input.LA(1);
						 
						int index73_435 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_435);
						if ( s>=0 ) return s;
						break;

					case 56 : 
						int LA73_120 = input.LA(1);
						 
						int index73_120 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_120==COMMA||LA73_120==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_120);
						if ( s>=0 ) return s;
						break;

					case 57 : 
						int LA73_49 = input.LA(1);
						 
						int index73_49 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_49==COMMA||LA73_49==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_49);
						if ( s>=0 ) return s;
						break;

					case 58 : 
						int LA73_378 = input.LA(1);
						 
						int index73_378 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_378);
						if ( s>=0 ) return s;
						break;

					case 59 : 
						int LA73_137 = input.LA(1);
						 
						int index73_137 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_137==COMMA||LA73_137==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_137);
						if ( s>=0 ) return s;
						break;

					case 60 : 
						int LA73_80 = input.LA(1);
						 
						int index73_80 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_80==COMMA||LA73_80==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_80);
						if ( s>=0 ) return s;
						break;

					case 61 : 
						int LA73_40 = input.LA(1);
						 
						int index73_40 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_40==COMMA||LA73_40==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_40);
						if ( s>=0 ) return s;
						break;

					case 62 : 
						int LA73_365 = input.LA(1);
						 
						int index73_365 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_365==DOT) ) {s = 426;}
						else if ( (LA73_365==COMMA) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_365);
						if ( s>=0 ) return s;
						break;

					case 63 : 
						int LA73_419 = input.LA(1);
						 
						int index73_419 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_419);
						if ( s>=0 ) return s;
						break;

					case 64 : 
						int LA73_185 = input.LA(1);
						 
						int index73_185 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_185==COMMA||LA73_185==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_185);
						if ( s>=0 ) return s;
						break;

					case 65 : 
						int LA73_219 = input.LA(1);
						 
						int index73_219 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_219==COMMA||LA73_219==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_219);
						if ( s>=0 ) return s;
						break;

					case 66 : 
						int LA73_132 = input.LA(1);
						 
						int index73_132 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_132==COMMA||LA73_132==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_132);
						if ( s>=0 ) return s;
						break;

					case 67 : 
						int LA73_75 = input.LA(1);
						 
						int index73_75 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_75==COMMA||LA73_75==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_75);
						if ( s>=0 ) return s;
						break;

					case 68 : 
						int LA73_232 = input.LA(1);
						 
						int index73_232 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_232==COMMA||LA73_232==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_232);
						if ( s>=0 ) return s;
						break;

					case 69 : 
						int LA73_269 = input.LA(1);
						 
						int index73_269 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_269==COMMA||LA73_269==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_269);
						if ( s>=0 ) return s;
						break;

					case 70 : 
						int LA73_222 = input.LA(1);
						 
						int index73_222 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_222==DOT) ) {s = 292;}
						else if ( (LA73_222==COMMA) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_222);
						if ( s>=0 ) return s;
						break;

					case 71 : 
						int LA73_118 = input.LA(1);
						 
						int index73_118 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_118==COMMA||LA73_118==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_118);
						if ( s>=0 ) return s;
						break;

					case 72 : 
						int LA73_108 = input.LA(1);
						 
						int index73_108 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_108==COMMA||LA73_108==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_108);
						if ( s>=0 ) return s;
						break;

					case 73 : 
						int LA73_47 = input.LA(1);
						 
						int index73_47 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_47==COMMA||LA73_47==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_47);
						if ( s>=0 ) return s;
						break;

					case 74 : 
						int LA73_422 = input.LA(1);
						 
						int index73_422 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_422);
						if ( s>=0 ) return s;
						break;

					case 75 : 
						int LA73_335 = input.LA(1);
						 
						int index73_335 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_335);
						if ( s>=0 ) return s;
						break;

					case 76 : 
						int LA73_367 = input.LA(1);
						 
						int index73_367 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_367==COMMA||LA73_367==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_367);
						if ( s>=0 ) return s;
						break;

					case 77 : 
						int LA73_150 = input.LA(1);
						 
						int index73_150 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_150==COMMA||LA73_150==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_150);
						if ( s>=0 ) return s;
						break;

					case 78 : 
						int LA73_233 = input.LA(1);
						 
						int index73_233 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_233==COMMA||LA73_233==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_233);
						if ( s>=0 ) return s;
						break;

					case 79 : 
						int LA73_327 = input.LA(1);
						 
						int index73_327 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_327==COMMA||LA73_327==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_327);
						if ( s>=0 ) return s;
						break;

					case 80 : 
						int LA73_411 = input.LA(1);
						 
						int index73_411 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_411);
						if ( s>=0 ) return s;
						break;

					case 81 : 
						int LA73_436 = input.LA(1);
						 
						int index73_436 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_436);
						if ( s>=0 ) return s;
						break;

					case 82 : 
						int LA73_377 = input.LA(1);
						 
						int index73_377 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_377);
						if ( s>=0 ) return s;
						break;

					case 83 : 
						int LA73_342 = input.LA(1);
						 
						int index73_342 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_342);
						if ( s>=0 ) return s;
						break;

					case 84 : 
						int LA73_438 = input.LA(1);
						 
						int index73_438 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_438);
						if ( s>=0 ) return s;
						break;

					case 85 : 
						int LA73_140 = input.LA(1);
						 
						int index73_140 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_140==COMMA||LA73_140==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_140);
						if ( s>=0 ) return s;
						break;

					case 86 : 
						int LA73_295 = input.LA(1);
						 
						int index73_295 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_295==DOT) ) {s = 363;}
						else if ( (LA73_295==COMMA) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_295);
						if ( s>=0 ) return s;
						break;

					case 87 : 
						int LA73_310 = input.LA(1);
						 
						int index73_310 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_310==COMMA||LA73_310==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_310);
						if ( s>=0 ) return s;
						break;

					case 88 : 
						int LA73_184 = input.LA(1);
						 
						int index73_184 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_184==COMMA||LA73_184==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_184);
						if ( s>=0 ) return s;
						break;

					case 89 : 
						int LA73_408 = input.LA(1);
						 
						int index73_408 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_408);
						if ( s>=0 ) return s;
						break;

					case 90 : 
						int LA73_363 = input.LA(1);
						 
						int index73_363 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_363==ID) ) {s = 413;}
						else if ( (LA73_363==FTSWORD) ) {s = 333;}
						else if ( (LA73_363==FTSPRE) ) {s = 334;}
						else if ( (LA73_363==FTSWILD) ) {s = 335;}
						else if ( (LA73_363==NOT) ) {s = 336;}
						else if ( (LA73_363==TO) ) {s = 337;}
						else if ( (LA73_363==DECIMAL_INTEGER_LITERAL) ) {s = 338;}
						else if ( (LA73_363==FLOATING_POINT_LITERAL) ) {s = 339;}
						else if ( (LA73_363==STAR) ) {s = 340;}
						else if ( (LA73_363==QUESTION_MARK) ) {s = 341;}
						else if ( (LA73_363==DATETIME) ) {s = 342;}
						else if ( (LA73_363==URI) ) {s = 343;}
						else if ( (LA73_363==TILDA) && (synpred44_FTS())) {s = 344;}
						else if ( (LA73_363==CARAT) && (synpred44_FTS())) {s = 345;}
						else if ( (LA73_363==AND) && (synpred44_FTS())) {s = 346;}
						else if ( (LA73_363==AMP) && (synpred44_FTS())) {s = 347;}
						else if ( (LA73_363==EOF) && (synpred44_FTS())) {s = 348;}
						else if ( (LA73_363==RPAREN) && (synpred44_FTS())) {s = 349;}
						else if ( (LA73_363==OR) && (synpred44_FTS())) {s = 350;}
						else if ( (LA73_363==BAR) && (synpred44_FTS())) {s = 351;}
						else if ( (LA73_363==EXCLAMATION) && (synpred44_FTS())) {s = 352;}
						else if ( (LA73_363==AT) && (synpred44_FTS())) {s = 353;}
						else if ( (LA73_363==FTSPHRASE) && (synpred44_FTS())) {s = 354;}
						else if ( (LA73_363==LSQUARE) && (synpred44_FTS())) {s = 355;}
						else if ( (LA73_363==LT) && (synpred44_FTS())) {s = 356;}
						else if ( (LA73_363==COMMA||LA73_363==DOT) && (synpred44_FTS())) {s = 357;}
						else if ( (LA73_363==EQUALS) && (synpred44_FTS())) {s = 358;}
						else if ( (LA73_363==LPAREN) && (synpred44_FTS())) {s = 359;}
						else if ( (LA73_363==PERCENT) && (synpred44_FTS())) {s = 360;}
						else if ( (LA73_363==PLUS) && (synpred44_FTS())) {s = 361;}
						else if ( (LA73_363==MINUS) && (synpred44_FTS())) {s = 362;}
						 
						input.seek(index73_363);
						if ( s>=0 ) return s;
						break;

					case 91 : 
						int LA73_268 = input.LA(1);
						 
						int index73_268 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_268==COMMA||LA73_268==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_268);
						if ( s>=0 ) return s;
						break;

					case 92 : 
						int LA73_142 = input.LA(1);
						 
						int index73_142 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_142==COMMA||LA73_142==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_142);
						if ( s>=0 ) return s;
						break;

					case 93 : 
						int LA73_114 = input.LA(1);
						 
						int index73_114 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_114==COMMA||LA73_114==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_114);
						if ( s>=0 ) return s;
						break;

					case 94 : 
						int LA73_43 = input.LA(1);
						 
						int index73_43 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_43==COMMA||LA73_43==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_43);
						if ( s>=0 ) return s;
						break;

					case 95 : 
						int LA73_141 = input.LA(1);
						 
						int index73_141 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_141==COMMA||LA73_141==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_141);
						if ( s>=0 ) return s;
						break;

					case 96 : 
						int LA73_366 = input.LA(1);
						 
						int index73_366 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_366==COMMA||LA73_366==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_366);
						if ( s>=0 ) return s;
						break;

					case 97 : 
						int LA73_320 = input.LA(1);
						 
						int index73_320 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_320==COMMA||LA73_320==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_320);
						if ( s>=0 ) return s;
						break;

					case 98 : 
						int LA73_423 = input.LA(1);
						 
						int index73_423 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_423);
						if ( s>=0 ) return s;
						break;

					case 99 : 
						int LA73_119 = input.LA(1);
						 
						int index73_119 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_119==COMMA||LA73_119==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_119);
						if ( s>=0 ) return s;
						break;

					case 100 : 
						int LA73_48 = input.LA(1);
						 
						int index73_48 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_48==COMMA||LA73_48==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_48);
						if ( s>=0 ) return s;
						break;

					case 101 : 
						int LA73_177 = input.LA(1);
						 
						int index73_177 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_177==COMMA||LA73_177==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_177);
						if ( s>=0 ) return s;
						break;

					case 102 : 
						int LA73_259 = input.LA(1);
						 
						int index73_259 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_259==ID) ) {s = 300;}
						else if ( (LA73_259==FTSWORD) ) {s = 229;}
						else if ( (LA73_259==FTSPRE) ) {s = 230;}
						else if ( (LA73_259==FTSWILD) ) {s = 231;}
						else if ( (LA73_259==NOT) ) {s = 232;}
						else if ( (LA73_259==TO) ) {s = 233;}
						else if ( (LA73_259==DECIMAL_INTEGER_LITERAL) ) {s = 234;}
						else if ( (LA73_259==FLOATING_POINT_LITERAL) ) {s = 235;}
						else if ( (LA73_259==STAR) ) {s = 236;}
						else if ( (LA73_259==QUESTION_MARK) ) {s = 237;}
						else if ( (LA73_259==DATETIME) ) {s = 238;}
						else if ( (LA73_259==URI) ) {s = 239;}
						else if ( (LA73_259==TILDA) && (synpred48_FTS())) {s = 240;}
						else if ( (LA73_259==CARAT) && (synpred48_FTS())) {s = 241;}
						else if ( (LA73_259==AND) && (synpred48_FTS())) {s = 242;}
						else if ( (LA73_259==AMP) && (synpred48_FTS())) {s = 243;}
						else if ( (LA73_259==EOF) && (synpred48_FTS())) {s = 244;}
						else if ( (LA73_259==RPAREN) && (synpred48_FTS())) {s = 245;}
						else if ( (LA73_259==OR) && (synpred48_FTS())) {s = 246;}
						else if ( (LA73_259==BAR) && (synpred48_FTS())) {s = 247;}
						else if ( (LA73_259==EXCLAMATION) && (synpred48_FTS())) {s = 248;}
						else if ( (LA73_259==AT) && (synpred48_FTS())) {s = 249;}
						else if ( (LA73_259==FTSPHRASE) && (synpred48_FTS())) {s = 250;}
						else if ( (LA73_259==LSQUARE) && (synpred48_FTS())) {s = 251;}
						else if ( (LA73_259==LT) && (synpred48_FTS())) {s = 252;}
						else if ( (LA73_259==COMMA||LA73_259==DOT) && (synpred48_FTS())) {s = 253;}
						else if ( (LA73_259==EQUALS) && (synpred48_FTS())) {s = 254;}
						else if ( (LA73_259==LPAREN) && (synpred48_FTS())) {s = 255;}
						else if ( (LA73_259==PERCENT) && (synpred48_FTS())) {s = 256;}
						else if ( (LA73_259==PLUS) && (synpred48_FTS())) {s = 257;}
						else if ( (LA73_259==MINUS) && (synpred48_FTS())) {s = 258;}
						 
						input.seek(index73_259);
						if ( s>=0 ) return s;
						break;

					case 103 : 
						int LA73_328 = input.LA(1);
						 
						int index73_328 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_328==COMMA||LA73_328==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_328);
						if ( s>=0 ) return s;
						break;

					case 104 : 
						int LA73_426 = input.LA(1);
						 
						int index73_426 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_426==ID) ) {s = 451;}
						else if ( (LA73_426==FTSWORD) ) {s = 415;}
						else if ( (LA73_426==FTSPRE) ) {s = 416;}
						else if ( (LA73_426==FTSWILD) ) {s = 417;}
						else if ( (LA73_426==NOT) ) {s = 418;}
						else if ( (LA73_426==TO) ) {s = 419;}
						else if ( (LA73_426==DECIMAL_INTEGER_LITERAL) ) {s = 420;}
						else if ( (LA73_426==FLOATING_POINT_LITERAL) ) {s = 421;}
						else if ( (LA73_426==STAR) ) {s = 422;}
						else if ( (LA73_426==QUESTION_MARK) ) {s = 423;}
						else if ( (LA73_426==DATETIME) ) {s = 424;}
						else if ( (LA73_426==URI) ) {s = 425;}
						else if ( (LA73_426==TILDA) && (synpred44_FTS())) {s = 344;}
						else if ( (LA73_426==CARAT) && (synpred44_FTS())) {s = 345;}
						else if ( (LA73_426==AND) && (synpred44_FTS())) {s = 346;}
						else if ( (LA73_426==AMP) && (synpred44_FTS())) {s = 347;}
						else if ( (LA73_426==EOF) && (synpred44_FTS())) {s = 348;}
						else if ( (LA73_426==RPAREN) && (synpred44_FTS())) {s = 349;}
						else if ( (LA73_426==OR) && (synpred44_FTS())) {s = 350;}
						else if ( (LA73_426==BAR) && (synpred44_FTS())) {s = 351;}
						else if ( (LA73_426==EXCLAMATION) && (synpred44_FTS())) {s = 352;}
						else if ( (LA73_426==AT) && (synpred44_FTS())) {s = 353;}
						else if ( (LA73_426==FTSPHRASE) && (synpred44_FTS())) {s = 354;}
						else if ( (LA73_426==LSQUARE) && (synpred44_FTS())) {s = 355;}
						else if ( (LA73_426==LT) && (synpred44_FTS())) {s = 356;}
						else if ( (LA73_426==COMMA||LA73_426==DOT) && (synpred44_FTS())) {s = 357;}
						else if ( (LA73_426==EQUALS) && (synpred44_FTS())) {s = 358;}
						else if ( (LA73_426==LPAREN) && (synpred44_FTS())) {s = 359;}
						else if ( (LA73_426==PERCENT) && (synpred44_FTS())) {s = 360;}
						else if ( (LA73_426==PLUS) && (synpred44_FTS())) {s = 361;}
						else if ( (LA73_426==MINUS) && (synpred44_FTS())) {s = 362;}
						 
						input.seek(index73_426);
						if ( s>=0 ) return s;
						break;

					case 105 : 
						int LA73_301 = input.LA(1);
						 
						int index73_301 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_301==COMMA||LA73_301==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_301);
						if ( s>=0 ) return s;
						break;

					case 106 : 
						int LA73_149 = input.LA(1);
						 
						int index73_149 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_149==COMMA||LA73_149==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_149);
						if ( s>=0 ) return s;
						break;

					case 107 : 
						int LA73_139 = input.LA(1);
						 
						int index73_139 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_139==COMMA||LA73_139==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_139);
						if ( s>=0 ) return s;
						break;

					case 108 : 
						int LA73_82 = input.LA(1);
						 
						int index73_82 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_82==COMMA||LA73_82==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_82);
						if ( s>=0 ) return s;
						break;

					case 109 : 
						int LA73_412 = input.LA(1);
						 
						int index73_412 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_412);
						if ( s>=0 ) return s;
						break;

					case 110 : 
						int LA73_433 = input.LA(1);
						 
						int index73_433 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_433);
						if ( s>=0 ) return s;
						break;

					case 111 : 
						int LA73_300 = input.LA(1);
						 
						int index73_300 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_300==COMMA||LA73_300==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_300);
						if ( s>=0 ) return s;
						break;

					case 112 : 
						int LA73_364 = input.LA(1);
						 
						int index73_364 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_364==ID) ) {s = 414;}
						else if ( (LA73_364==FTSWORD) ) {s = 415;}
						else if ( (LA73_364==FTSPRE) ) {s = 416;}
						else if ( (LA73_364==FTSWILD) ) {s = 417;}
						else if ( (LA73_364==NOT) ) {s = 418;}
						else if ( (LA73_364==TO) ) {s = 419;}
						else if ( (LA73_364==DECIMAL_INTEGER_LITERAL) ) {s = 420;}
						else if ( (LA73_364==FLOATING_POINT_LITERAL) ) {s = 421;}
						else if ( (LA73_364==STAR) ) {s = 422;}
						else if ( (LA73_364==QUESTION_MARK) ) {s = 423;}
						else if ( (LA73_364==DATETIME) ) {s = 424;}
						else if ( (LA73_364==URI) ) {s = 425;}
						else if ( (LA73_364==TILDA) && (synpred44_FTS())) {s = 344;}
						else if ( (LA73_364==CARAT) && (synpred44_FTS())) {s = 345;}
						else if ( (LA73_364==AND) && (synpred44_FTS())) {s = 346;}
						else if ( (LA73_364==AMP) && (synpred44_FTS())) {s = 347;}
						else if ( (LA73_364==EOF) && (synpred44_FTS())) {s = 348;}
						else if ( (LA73_364==RPAREN) && (synpred44_FTS())) {s = 349;}
						else if ( (LA73_364==OR) && (synpred44_FTS())) {s = 350;}
						else if ( (LA73_364==BAR) && (synpred44_FTS())) {s = 351;}
						else if ( (LA73_364==EXCLAMATION) && (synpred44_FTS())) {s = 352;}
						else if ( (LA73_364==AT) && (synpred44_FTS())) {s = 353;}
						else if ( (LA73_364==FTSPHRASE) && (synpred44_FTS())) {s = 354;}
						else if ( (LA73_364==LSQUARE) && (synpred44_FTS())) {s = 355;}
						else if ( (LA73_364==LT) && (synpred44_FTS())) {s = 356;}
						else if ( (LA73_364==COMMA||LA73_364==DOT) && (synpred44_FTS())) {s = 357;}
						else if ( (LA73_364==EQUALS) && (synpred44_FTS())) {s = 358;}
						else if ( (LA73_364==LPAREN) && (synpred44_FTS())) {s = 359;}
						else if ( (LA73_364==PERCENT) && (synpred44_FTS())) {s = 360;}
						else if ( (LA73_364==PLUS) && (synpred44_FTS())) {s = 361;}
						else if ( (LA73_364==MINUS) && (synpred44_FTS())) {s = 362;}
						 
						input.seek(index73_364);
						if ( s>=0 ) return s;
						break;

					case 113 : 
						int LA73_125 = input.LA(1);
						 
						int index73_125 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_125==COMMA||LA73_125==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_125);
						if ( s>=0 ) return s;
						break;

					case 114 : 
						int LA73_220 = input.LA(1);
						 
						int index73_220 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_220==COMMA||LA73_220==DOT) ) {s = 260;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_220);
						if ( s>=0 ) return s;
						break;

					case 115 : 
						int LA73_370 = input.LA(1);
						 
						int index73_370 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_370);
						if ( s>=0 ) return s;
						break;

					case 116 : 
						int LA73_136 = input.LA(1);
						 
						int index73_136 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_136==COMMA||LA73_136==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_136);
						if ( s>=0 ) return s;
						break;

					case 117 : 
						int LA73_79 = input.LA(1);
						 
						int index73_79 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_79==COMMA||LA73_79==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_79);
						if ( s>=0 ) return s;
						break;

					case 118 : 
						int LA73_292 = input.LA(1);
						 
						int index73_292 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_292==ID) ) {s = 319;}
						else if ( (LA73_292==FTSWORD) ) {s = 262;}
						else if ( (LA73_292==FTSPRE) ) {s = 263;}
						else if ( (LA73_292==FTSWILD) ) {s = 264;}
						else if ( (LA73_292==NOT) ) {s = 265;}
						else if ( (LA73_292==TO) ) {s = 266;}
						else if ( (LA73_292==DECIMAL_INTEGER_LITERAL) ) {s = 267;}
						else if ( (LA73_292==FLOATING_POINT_LITERAL) ) {s = 268;}
						else if ( (LA73_292==STAR) ) {s = 269;}
						else if ( (LA73_292==QUESTION_MARK) ) {s = 270;}
						else if ( (LA73_292==DATETIME) ) {s = 271;}
						else if ( (LA73_292==URI) ) {s = 272;}
						else if ( (LA73_292==TILDA) && (synpred47_FTS())) {s = 273;}
						else if ( (LA73_292==CARAT) && (synpred47_FTS())) {s = 274;}
						else if ( (LA73_292==AND) && (synpred47_FTS())) {s = 275;}
						else if ( (LA73_292==AMP) && (synpred47_FTS())) {s = 276;}
						else if ( (LA73_292==EOF) && (synpred47_FTS())) {s = 277;}
						else if ( (LA73_292==RPAREN) && (synpred47_FTS())) {s = 278;}
						else if ( (LA73_292==OR) && (synpred47_FTS())) {s = 279;}
						else if ( (LA73_292==BAR) && (synpred47_FTS())) {s = 280;}
						else if ( (LA73_292==EXCLAMATION) && (synpred47_FTS())) {s = 281;}
						else if ( (LA73_292==AT) && (synpred47_FTS())) {s = 282;}
						else if ( (LA73_292==FTSPHRASE) && (synpred47_FTS())) {s = 283;}
						else if ( (LA73_292==LSQUARE) && (synpred47_FTS())) {s = 284;}
						else if ( (LA73_292==LT) && (synpred47_FTS())) {s = 285;}
						else if ( (LA73_292==COMMA||LA73_292==DOT) && (synpred47_FTS())) {s = 286;}
						else if ( (LA73_292==EQUALS) && (synpred47_FTS())) {s = 287;}
						else if ( (LA73_292==LPAREN) && (synpred47_FTS())) {s = 288;}
						else if ( (LA73_292==PERCENT) && (synpred47_FTS())) {s = 289;}
						else if ( (LA73_292==PLUS) && (synpred47_FTS())) {s = 290;}
						else if ( (LA73_292==MINUS) && (synpred47_FTS())) {s = 291;}
						 
						input.seek(index73_292);
						if ( s>=0 ) return s;
						break;

					case 119 : 
						int LA73_124 = input.LA(1);
						 
						int index73_124 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_124==DOT) ) {s = 208;}
						else if ( (LA73_124==COMMA) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_124);
						if ( s>=0 ) return s;
						break;

					case 120 : 
						int LA73_376 = input.LA(1);
						 
						int index73_376 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_376);
						if ( s>=0 ) return s;
						break;

					case 121 : 
						int LA73_325 = input.LA(1);
						 
						int index73_325 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_325==COMMA||LA73_325==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_325);
						if ( s>=0 ) return s;
						break;

					case 122 : 
						int LA73_183 = input.LA(1);
						 
						int index73_183 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_183==COMMA||LA73_183==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_183);
						if ( s>=0 ) return s;
						break;

					case 123 : 
						int LA73_138 = input.LA(1);
						 
						int index73_138 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_138==COMMA||LA73_138==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_138);
						if ( s>=0 ) return s;
						break;

					case 124 : 
						int LA73_81 = input.LA(1);
						 
						int index73_81 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_81==COMMA||LA73_81==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_81);
						if ( s>=0 ) return s;
						break;

					case 125 : 
						int LA73_230 = input.LA(1);
						 
						int index73_230 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_230==COMMA||LA73_230==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_230);
						if ( s>=0 ) return s;
						break;

					case 126 : 
						int LA73_299 = input.LA(1);
						 
						int index73_299 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_299==COMMA||LA73_299==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_299);
						if ( s>=0 ) return s;
						break;

					case 127 : 
						int LA73_431 = input.LA(1);
						 
						int index73_431 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_431);
						if ( s>=0 ) return s;
						break;

					case 128 : 
						int LA73_338 = input.LA(1);
						 
						int index73_338 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_338);
						if ( s>=0 ) return s;
						break;

					case 129 : 
						int LA73_452 = input.LA(1);
						 
						int index73_452 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_452);
						if ( s>=0 ) return s;
						break;

					case 130 : 
						int LA73_456 = input.LA(1);
						 
						int index73_456 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_456);
						if ( s>=0 ) return s;
						break;

					case 131 : 
						int LA73_402 = input.LA(1);
						 
						int index73_402 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_402==ID) ) {s = 433;}
						else if ( (LA73_402==FTSWORD) ) {s = 434;}
						else if ( (LA73_402==FTSPRE) ) {s = 435;}
						else if ( (LA73_402==FTSWILD) ) {s = 436;}
						else if ( (LA73_402==NOT) ) {s = 437;}
						else if ( (LA73_402==TO) ) {s = 438;}
						else if ( (LA73_402==DECIMAL_INTEGER_LITERAL) ) {s = 439;}
						else if ( (LA73_402==FLOATING_POINT_LITERAL) ) {s = 440;}
						else if ( (LA73_402==STAR) ) {s = 441;}
						else if ( (LA73_402==QUESTION_MARK) ) {s = 442;}
						else if ( (LA73_402==DATETIME) ) {s = 443;}
						else if ( (LA73_402==URI) ) {s = 444;}
						else if ( (LA73_402==TILDA) && (synpred43_FTS())) {s = 382;}
						else if ( (LA73_402==CARAT) && (synpred43_FTS())) {s = 383;}
						else if ( (LA73_402==AND) && (synpred43_FTS())) {s = 384;}
						else if ( (LA73_402==AMP) && (synpred43_FTS())) {s = 385;}
						else if ( (LA73_402==EOF) && (synpred43_FTS())) {s = 386;}
						else if ( (LA73_402==RPAREN) && (synpred43_FTS())) {s = 387;}
						else if ( (LA73_402==OR) && (synpred43_FTS())) {s = 388;}
						else if ( (LA73_402==BAR) && (synpred43_FTS())) {s = 389;}
						else if ( (LA73_402==EXCLAMATION) && (synpred43_FTS())) {s = 390;}
						else if ( (LA73_402==AT) && (synpred43_FTS())) {s = 391;}
						else if ( (LA73_402==FTSPHRASE) && (synpred43_FTS())) {s = 392;}
						else if ( (LA73_402==LSQUARE) && (synpred43_FTS())) {s = 393;}
						else if ( (LA73_402==LT) && (synpred43_FTS())) {s = 394;}
						else if ( (LA73_402==COMMA||LA73_402==DOT) && (synpred43_FTS())) {s = 395;}
						else if ( (LA73_402==EQUALS) && (synpred43_FTS())) {s = 396;}
						else if ( (LA73_402==LPAREN) && (synpred43_FTS())) {s = 397;}
						else if ( (LA73_402==PERCENT) && (synpred43_FTS())) {s = 398;}
						else if ( (LA73_402==PLUS) && (synpred43_FTS())) {s = 399;}
						else if ( (LA73_402==MINUS) && (synpred43_FTS())) {s = 400;}
						 
						input.seek(index73_402);
						if ( s>=0 ) return s;
						break;

					case 132 : 
						int LA73_147 = input.LA(1);
						 
						int index73_147 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_147==COMMA||LA73_147==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_147);
						if ( s>=0 ) return s;
						break;

					case 133 : 
						int LA73_231 = input.LA(1);
						 
						int index73_231 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_231==COMMA||LA73_231==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_231);
						if ( s>=0 ) return s;
						break;

					case 134 : 
						int LA73_427 = input.LA(1);
						 
						int index73_427 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_427);
						if ( s>=0 ) return s;
						break;

					case 135 : 
						int LA73_261 = input.LA(1);
						 
						int index73_261 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_261==COMMA||LA73_261==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_261);
						if ( s>=0 ) return s;
						break;

					case 136 : 
						int LA73_315 = input.LA(1);
						 
						int index73_315 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_315==COMMA||LA73_315==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_315);
						if ( s>=0 ) return s;
						break;

					case 137 : 
						int LA73_238 = input.LA(1);
						 
						int index73_238 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_238==COMMA||LA73_238==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_238);
						if ( s>=0 ) return s;
						break;

					case 138 : 
						int LA73_409 = input.LA(1);
						 
						int index73_409 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_409);
						if ( s>=0 ) return s;
						break;

					case 139 : 
						int LA73_374 = input.LA(1);
						 
						int index73_374 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_374);
						if ( s>=0 ) return s;
						break;

					case 140 : 
						int LA73_221 = input.LA(1);
						 
						int index73_221 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_221==ID) ) {s = 261;}
						else if ( (LA73_221==FTSWORD) ) {s = 262;}
						else if ( (LA73_221==FTSPRE) ) {s = 263;}
						else if ( (LA73_221==FTSWILD) ) {s = 264;}
						else if ( (LA73_221==NOT) ) {s = 265;}
						else if ( (LA73_221==TO) ) {s = 266;}
						else if ( (LA73_221==DECIMAL_INTEGER_LITERAL) ) {s = 267;}
						else if ( (LA73_221==FLOATING_POINT_LITERAL) ) {s = 268;}
						else if ( (LA73_221==STAR) ) {s = 269;}
						else if ( (LA73_221==QUESTION_MARK) ) {s = 270;}
						else if ( (LA73_221==DATETIME) ) {s = 271;}
						else if ( (LA73_221==URI) ) {s = 272;}
						else if ( (LA73_221==TILDA) && (synpred47_FTS())) {s = 273;}
						else if ( (LA73_221==CARAT) && (synpred47_FTS())) {s = 274;}
						else if ( (LA73_221==AND) && (synpred47_FTS())) {s = 275;}
						else if ( (LA73_221==AMP) && (synpred47_FTS())) {s = 276;}
						else if ( (LA73_221==EOF) && (synpred47_FTS())) {s = 277;}
						else if ( (LA73_221==RPAREN) && (synpred47_FTS())) {s = 278;}
						else if ( (LA73_221==OR) && (synpred47_FTS())) {s = 279;}
						else if ( (LA73_221==BAR) && (synpred47_FTS())) {s = 280;}
						else if ( (LA73_221==EXCLAMATION) && (synpred47_FTS())) {s = 281;}
						else if ( (LA73_221==AT) && (synpred47_FTS())) {s = 282;}
						else if ( (LA73_221==FTSPHRASE) && (synpred47_FTS())) {s = 283;}
						else if ( (LA73_221==LSQUARE) && (synpred47_FTS())) {s = 284;}
						else if ( (LA73_221==LT) && (synpred47_FTS())) {s = 285;}
						else if ( (LA73_221==COMMA||LA73_221==DOT) && (synpred47_FTS())) {s = 286;}
						else if ( (LA73_221==EQUALS) && (synpred47_FTS())) {s = 287;}
						else if ( (LA73_221==LPAREN) && (synpred47_FTS())) {s = 288;}
						else if ( (LA73_221==PERCENT) && (synpred47_FTS())) {s = 289;}
						else if ( (LA73_221==PLUS) && (synpred47_FTS())) {s = 290;}
						else if ( (LA73_221==MINUS) && (synpred47_FTS())) {s = 291;}
						 
						input.seek(index73_221);
						if ( s>=0 ) return s;
						break;

					case 141 : 
						int LA73_260 = input.LA(1);
						 
						int index73_260 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_260==ID) ) {s = 301;}
						else if ( (LA73_260==FTSWORD) ) {s = 302;}
						else if ( (LA73_260==FTSPRE) ) {s = 303;}
						else if ( (LA73_260==FTSWILD) ) {s = 304;}
						else if ( (LA73_260==NOT) ) {s = 305;}
						else if ( (LA73_260==TO) ) {s = 306;}
						else if ( (LA73_260==DECIMAL_INTEGER_LITERAL) ) {s = 307;}
						else if ( (LA73_260==FLOATING_POINT_LITERAL) ) {s = 308;}
						else if ( (LA73_260==STAR) ) {s = 309;}
						else if ( (LA73_260==QUESTION_MARK) ) {s = 310;}
						else if ( (LA73_260==DATETIME) ) {s = 311;}
						else if ( (LA73_260==URI) ) {s = 312;}
						else if ( (LA73_260==TILDA) && (synpred48_FTS())) {s = 240;}
						else if ( (LA73_260==CARAT) && (synpred48_FTS())) {s = 241;}
						else if ( (LA73_260==AND) && (synpred48_FTS())) {s = 242;}
						else if ( (LA73_260==AMP) && (synpred48_FTS())) {s = 243;}
						else if ( (LA73_260==EOF) && (synpred48_FTS())) {s = 244;}
						else if ( (LA73_260==RPAREN) && (synpred48_FTS())) {s = 245;}
						else if ( (LA73_260==OR) && (synpred48_FTS())) {s = 246;}
						else if ( (LA73_260==BAR) && (synpred48_FTS())) {s = 247;}
						else if ( (LA73_260==EXCLAMATION) && (synpred48_FTS())) {s = 248;}
						else if ( (LA73_260==AT) && (synpred48_FTS())) {s = 249;}
						else if ( (LA73_260==FTSPHRASE) && (synpred48_FTS())) {s = 250;}
						else if ( (LA73_260==LSQUARE) && (synpred48_FTS())) {s = 251;}
						else if ( (LA73_260==LT) && (synpred48_FTS())) {s = 252;}
						else if ( (LA73_260==COMMA||LA73_260==DOT) && (synpred48_FTS())) {s = 253;}
						else if ( (LA73_260==EQUALS) && (synpred48_FTS())) {s = 254;}
						else if ( (LA73_260==LPAREN) && (synpred48_FTS())) {s = 255;}
						else if ( (LA73_260==PERCENT) && (synpred48_FTS())) {s = 256;}
						else if ( (LA73_260==PLUS) && (synpred48_FTS())) {s = 257;}
						else if ( (LA73_260==MINUS) && (synpred48_FTS())) {s = 258;}
						 
						input.seek(index73_260);
						if ( s>=0 ) return s;
						break;

					case 142 : 
						int LA73_267 = input.LA(1);
						 
						int index73_267 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_267==COMMA||LA73_267==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_267);
						if ( s>=0 ) return s;
						break;

					case 143 : 
						int LA73_109 = input.LA(1);
						 
						int index73_109 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_109==COMMA||LA73_109==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_109);
						if ( s>=0 ) return s;
						break;

					case 144 : 
						int LA73_131 = input.LA(1);
						 
						int index73_131 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_131==COMMA||LA73_131==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_131);
						if ( s>=0 ) return s;
						break;

					case 145 : 
						int LA73_74 = input.LA(1);
						 
						int index73_74 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_74==COMMA||LA73_74==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_74);
						if ( s>=0 ) return s;
						break;

					case 146 : 
						int LA73_443 = input.LA(1);
						 
						int index73_443 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_443);
						if ( s>=0 ) return s;
						break;

					case 147 : 
						int LA73_415 = input.LA(1);
						 
						int index73_415 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_415);
						if ( s>=0 ) return s;
						break;

					case 148 : 
						int LA73_294 = input.LA(1);
						 
						int index73_294 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_294==ID) ) {s = 332;}
						else if ( (LA73_294==FTSWORD) ) {s = 333;}
						else if ( (LA73_294==FTSPRE) ) {s = 334;}
						else if ( (LA73_294==FTSWILD) ) {s = 335;}
						else if ( (LA73_294==NOT) ) {s = 336;}
						else if ( (LA73_294==TO) ) {s = 337;}
						else if ( (LA73_294==DECIMAL_INTEGER_LITERAL) ) {s = 338;}
						else if ( (LA73_294==FLOATING_POINT_LITERAL) ) {s = 339;}
						else if ( (LA73_294==STAR) ) {s = 340;}
						else if ( (LA73_294==QUESTION_MARK) ) {s = 341;}
						else if ( (LA73_294==DATETIME) ) {s = 342;}
						else if ( (LA73_294==URI) ) {s = 343;}
						else if ( (LA73_294==TILDA) && (synpred44_FTS())) {s = 344;}
						else if ( (LA73_294==CARAT) && (synpred44_FTS())) {s = 345;}
						else if ( (LA73_294==AND) && (synpred44_FTS())) {s = 346;}
						else if ( (LA73_294==AMP) && (synpred44_FTS())) {s = 347;}
						else if ( (LA73_294==EOF) && (synpred44_FTS())) {s = 348;}
						else if ( (LA73_294==RPAREN) && (synpred44_FTS())) {s = 349;}
						else if ( (LA73_294==OR) && (synpred44_FTS())) {s = 350;}
						else if ( (LA73_294==BAR) && (synpred44_FTS())) {s = 351;}
						else if ( (LA73_294==EXCLAMATION) && (synpred44_FTS())) {s = 352;}
						else if ( (LA73_294==AT) && (synpred44_FTS())) {s = 353;}
						else if ( (LA73_294==FTSPHRASE) && (synpred44_FTS())) {s = 354;}
						else if ( (LA73_294==LSQUARE) && (synpred44_FTS())) {s = 355;}
						else if ( (LA73_294==LT) && (synpred44_FTS())) {s = 356;}
						else if ( (LA73_294==COMMA||LA73_294==DOT) && (synpred44_FTS())) {s = 357;}
						else if ( (LA73_294==EQUALS) && (synpred44_FTS())) {s = 358;}
						else if ( (LA73_294==LPAREN) && (synpred44_FTS())) {s = 359;}
						else if ( (LA73_294==PERCENT) && (synpred44_FTS())) {s = 360;}
						else if ( (LA73_294==PLUS) && (synpred44_FTS())) {s = 361;}
						else if ( (LA73_294==MINUS) && (synpred44_FTS())) {s = 362;}
						 
						input.seek(index73_294);
						if ( s>=0 ) return s;
						break;

					case 149 : 
						int LA73_434 = input.LA(1);
						 
						int index73_434 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_434);
						if ( s>=0 ) return s;
						break;

					case 150 : 
						int LA73_306 = input.LA(1);
						 
						int index73_306 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_306==COMMA||LA73_306==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_306);
						if ( s>=0 ) return s;
						break;

					case 151 : 
						int LA73_236 = input.LA(1);
						 
						int index73_236 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_236==COMMA||LA73_236==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_236);
						if ( s>=0 ) return s;
						break;

					case 152 : 
						int LA73_416 = input.LA(1);
						 
						int index73_416 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_416);
						if ( s>=0 ) return s;
						break;

					case 153 : 
						int LA73_182 = input.LA(1);
						 
						int index73_182 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_182==COMMA||LA73_182==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_182);
						if ( s>=0 ) return s;
						break;

					case 154 : 
						int LA73_379 = input.LA(1);
						 
						int index73_379 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_379);
						if ( s>=0 ) return s;
						break;

					case 155 : 
						int LA73_302 = input.LA(1);
						 
						int index73_302 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_302==COMMA||LA73_302==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_302);
						if ( s>=0 ) return s;
						break;

					case 156 : 
						int LA73_326 = input.LA(1);
						 
						int index73_326 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_326==COMMA||LA73_326==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_326);
						if ( s>=0 ) return s;
						break;

					case 157 : 
						int LA73_339 = input.LA(1);
						 
						int index73_339 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_339);
						if ( s>=0 ) return s;
						break;

					case 158 : 
						int LA73_229 = input.LA(1);
						 
						int index73_229 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_229==COMMA||LA73_229==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_229);
						if ( s>=0 ) return s;
						break;

					case 159 : 
						int LA73_430 = input.LA(1);
						 
						int index73_430 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_430);
						if ( s>=0 ) return s;
						break;

					case 160 : 
						int LA73_418 = input.LA(1);
						 
						int index73_418 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_418);
						if ( s>=0 ) return s;
						break;

					case 161 : 
						int LA73_404 = input.LA(1);
						 
						int index73_404 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_404==COMMA||LA73_404==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_404);
						if ( s>=0 ) return s;
						break;

					case 162 : 
						int LA73_72 = input.LA(1);
						 
						int index73_72 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_72==COMMA||LA73_72==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_72);
						if ( s>=0 ) return s;
						break;

					case 163 : 
						int LA73_130 = input.LA(1);
						 
						int index73_130 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_130==COMMA||LA73_130==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_130);
						if ( s>=0 ) return s;
						break;

					case 164 : 
						int LA73_73 = input.LA(1);
						 
						int index73_73 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_73==COMMA||LA73_73==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_73);
						if ( s>=0 ) return s;
						break;

					case 165 : 
						int LA73_298 = input.LA(1);
						 
						int index73_298 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_298==COMMA||LA73_298==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_298);
						if ( s>=0 ) return s;
						break;

					case 166 : 
						int LA73_457 = input.LA(1);
						 
						int index73_457 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_457);
						if ( s>=0 ) return s;
						break;

					case 167 : 
						int LA73_148 = input.LA(1);
						 
						int index73_148 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_148==COMMA||LA73_148==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_148);
						if ( s>=0 ) return s;
						break;

					case 168 : 
						int LA73_26 = input.LA(1);
						 
						int index73_26 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_26==ID) ) {s = 40;}
						else if ( (LA73_26==FTSWORD) ) {s = 41;}
						else if ( (LA73_26==FTSPRE) ) {s = 42;}
						else if ( (LA73_26==FTSWILD) ) {s = 43;}
						else if ( (LA73_26==NOT) ) {s = 44;}
						else if ( (LA73_26==TO) ) {s = 45;}
						else if ( (LA73_26==DECIMAL_INTEGER_LITERAL) ) {s = 46;}
						else if ( (LA73_26==FLOATING_POINT_LITERAL) ) {s = 47;}
						else if ( (LA73_26==STAR) ) {s = 48;}
						else if ( (LA73_26==QUESTION_MARK) ) {s = 49;}
						else if ( (LA73_26==DATETIME) ) {s = 50;}
						else if ( (LA73_26==URI) ) {s = 51;}
						else if ( (LA73_26==TILDA) && (synpred56_FTS())) {s = 52;}
						else if ( (LA73_26==CARAT) && (synpred56_FTS())) {s = 53;}
						else if ( (LA73_26==AND) && (synpred56_FTS())) {s = 54;}
						else if ( (LA73_26==AMP) && (synpred56_FTS())) {s = 55;}
						else if ( (LA73_26==EOF) && (synpred56_FTS())) {s = 56;}
						else if ( (LA73_26==RPAREN) && (synpred56_FTS())) {s = 57;}
						else if ( (LA73_26==OR) && (synpred56_FTS())) {s = 58;}
						else if ( (LA73_26==BAR) && (synpred56_FTS())) {s = 59;}
						else if ( (LA73_26==EXCLAMATION) && (synpred56_FTS())) {s = 60;}
						else if ( (LA73_26==AT) && (synpred56_FTS())) {s = 61;}
						else if ( (LA73_26==FTSPHRASE) && (synpred56_FTS())) {s = 62;}
						else if ( (LA73_26==LSQUARE) && (synpred56_FTS())) {s = 63;}
						else if ( (LA73_26==LT) && (synpred56_FTS())) {s = 64;}
						else if ( (LA73_26==COMMA||LA73_26==DOT) && (synpred56_FTS())) {s = 65;}
						else if ( (LA73_26==EQUALS) && (synpred56_FTS())) {s = 66;}
						else if ( (LA73_26==LPAREN) && (synpred56_FTS())) {s = 67;}
						else if ( (LA73_26==PERCENT) && (synpred56_FTS())) {s = 68;}
						else if ( (LA73_26==PLUS) && (synpred56_FTS())) {s = 69;}
						else if ( (LA73_26==MINUS) && (synpred56_FTS())) {s = 70;}
						 
						input.seek(index73_26);
						if ( s>=0 ) return s;
						break;

					case 169 : 
						int LA73_410 = input.LA(1);
						 
						int index73_410 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_410);
						if ( s>=0 ) return s;
						break;

					case 170 : 
						int LA73_318 = input.LA(1);
						 
						int index73_318 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_318==COMMA||LA73_318==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_318);
						if ( s>=0 ) return s;
						break;

					case 171 : 
						int LA73_208 = input.LA(1);
						 
						int index73_208 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_208==ID) ) {s = 227;}
						else if ( (LA73_208==FTSWORD) ) {s = 178;}
						else if ( (LA73_208==FTSPRE) ) {s = 179;}
						else if ( (LA73_208==FTSWILD) ) {s = 180;}
						else if ( (LA73_208==NOT) ) {s = 181;}
						else if ( (LA73_208==TO) ) {s = 182;}
						else if ( (LA73_208==DECIMAL_INTEGER_LITERAL) ) {s = 183;}
						else if ( (LA73_208==FLOATING_POINT_LITERAL) ) {s = 184;}
						else if ( (LA73_208==STAR) ) {s = 185;}
						else if ( (LA73_208==QUESTION_MARK) ) {s = 186;}
						else if ( (LA73_208==DATETIME) ) {s = 187;}
						else if ( (LA73_208==URI) ) {s = 188;}
						else if ( (LA73_208==TILDA) && (synpred51_FTS())) {s = 189;}
						else if ( (LA73_208==CARAT) && (synpred51_FTS())) {s = 190;}
						else if ( (LA73_208==AND) && (synpred51_FTS())) {s = 191;}
						else if ( (LA73_208==AMP) && (synpred51_FTS())) {s = 192;}
						else if ( (LA73_208==EOF) && (synpred51_FTS())) {s = 193;}
						else if ( (LA73_208==RPAREN) && (synpred51_FTS())) {s = 194;}
						else if ( (LA73_208==OR) && (synpred51_FTS())) {s = 195;}
						else if ( (LA73_208==BAR) && (synpred51_FTS())) {s = 196;}
						else if ( (LA73_208==EXCLAMATION) && (synpred51_FTS())) {s = 197;}
						else if ( (LA73_208==AT) && (synpred51_FTS())) {s = 198;}
						else if ( (LA73_208==FTSPHRASE) && (synpred51_FTS())) {s = 199;}
						else if ( (LA73_208==LSQUARE) && (synpred51_FTS())) {s = 200;}
						else if ( (LA73_208==LT) && (synpred51_FTS())) {s = 201;}
						else if ( (LA73_208==COMMA||LA73_208==DOT) && (synpred51_FTS())) {s = 202;}
						else if ( (LA73_208==EQUALS) && (synpred51_FTS())) {s = 203;}
						else if ( (LA73_208==LPAREN) && (synpred51_FTS())) {s = 204;}
						else if ( (LA73_208==PERCENT) && (synpred51_FTS())) {s = 205;}
						else if ( (LA73_208==PLUS) && (synpred51_FTS())) {s = 206;}
						else if ( (LA73_208==MINUS) && (synpred51_FTS())) {s = 207;}
						 
						input.seek(index73_208);
						if ( s>=0 ) return s;
						break;

					case 172 : 
						int LA73_307 = input.LA(1);
						 
						int index73_307 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_307==COMMA||LA73_307==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_307);
						if ( s>=0 ) return s;
						break;

					case 173 : 
						int LA73_332 = input.LA(1);
						 
						int index73_332 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_332);
						if ( s>=0 ) return s;
						break;

					case 174 : 
						int LA73_71 = input.LA(1);
						 
						int index73_71 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_71==ID) ) {s = 111;}
						else if ( (LA73_71==FTSWORD) ) {s = 112;}
						else if ( (LA73_71==FTSPRE) ) {s = 113;}
						else if ( (LA73_71==FTSWILD) ) {s = 114;}
						else if ( (LA73_71==NOT) ) {s = 115;}
						else if ( (LA73_71==TO) ) {s = 116;}
						else if ( (LA73_71==DECIMAL_INTEGER_LITERAL) ) {s = 117;}
						else if ( (LA73_71==FLOATING_POINT_LITERAL) ) {s = 118;}
						else if ( (LA73_71==STAR) ) {s = 119;}
						else if ( (LA73_71==QUESTION_MARK) ) {s = 120;}
						else if ( (LA73_71==DATETIME) ) {s = 121;}
						else if ( (LA73_71==URI) ) {s = 51;}
						else if ( (LA73_71==TILDA) && (synpred56_FTS())) {s = 52;}
						else if ( (LA73_71==CARAT) && (synpred56_FTS())) {s = 53;}
						else if ( (LA73_71==AND) && (synpred56_FTS())) {s = 54;}
						else if ( (LA73_71==AMP) && (synpred56_FTS())) {s = 55;}
						else if ( (LA73_71==EOF) && (synpred56_FTS())) {s = 56;}
						else if ( (LA73_71==RPAREN) && (synpred56_FTS())) {s = 57;}
						else if ( (LA73_71==OR) && (synpred56_FTS())) {s = 58;}
						else if ( (LA73_71==BAR) && (synpred56_FTS())) {s = 59;}
						else if ( (LA73_71==EXCLAMATION) && (synpred56_FTS())) {s = 60;}
						else if ( (LA73_71==AT) && (synpred56_FTS())) {s = 61;}
						else if ( (LA73_71==FTSPHRASE) && (synpred56_FTS())) {s = 62;}
						else if ( (LA73_71==LSQUARE) && (synpred56_FTS())) {s = 63;}
						else if ( (LA73_71==LT) && (synpred56_FTS())) {s = 64;}
						else if ( (LA73_71==COMMA||LA73_71==DOT) && (synpred56_FTS())) {s = 65;}
						else if ( (LA73_71==EQUALS) && (synpred56_FTS())) {s = 66;}
						else if ( (LA73_71==LPAREN) && (synpred56_FTS())) {s = 67;}
						else if ( (LA73_71==PERCENT) && (synpred56_FTS())) {s = 68;}
						else if ( (LA73_71==PLUS) && (synpred56_FTS())) {s = 69;}
						else if ( (LA73_71==MINUS) && (synpred56_FTS())) {s = 70;}
						 
						input.seek(index73_71);
						if ( s>=0 ) return s;
						break;

					case 175 : 
						int LA73_135 = input.LA(1);
						 
						int index73_135 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_135==COMMA||LA73_135==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_135);
						if ( s>=0 ) return s;
						break;

					case 176 : 
						int LA73_78 = input.LA(1);
						 
						int index73_78 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_78==COMMA||LA73_78==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_78);
						if ( s>=0 ) return s;
						break;

					case 177 : 
						int LA73_126 = input.LA(1);
						 
						int index73_126 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_126==COMMA||LA73_126==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_126);
						if ( s>=0 ) return s;
						break;

					case 178 : 
						int LA73_226 = input.LA(1);
						 
						int index73_226 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_226==COMMA||LA73_226==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_226);
						if ( s>=0 ) return s;
						break;

					case 179 : 
						int LA73_144 = input.LA(1);
						 
						int index73_144 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_144==COMMA||LA73_144==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_144);
						if ( s>=0 ) return s;
						break;

					case 180 : 
						int LA73_270 = input.LA(1);
						 
						int index73_270 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_270==COMMA||LA73_270==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_270);
						if ( s>=0 ) return s;
						break;

					case 181 : 
						int LA73_340 = input.LA(1);
						 
						int index73_340 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_340);
						if ( s>=0 ) return s;
						break;

					case 182 : 
						int LA73_122 = input.LA(1);
						 
						int index73_122 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_122==ID) ) {s = 177;}
						else if ( (LA73_122==FTSWORD) ) {s = 178;}
						else if ( (LA73_122==FTSPRE) ) {s = 179;}
						else if ( (LA73_122==FTSWILD) ) {s = 180;}
						else if ( (LA73_122==NOT) ) {s = 181;}
						else if ( (LA73_122==TO) ) {s = 182;}
						else if ( (LA73_122==DECIMAL_INTEGER_LITERAL) ) {s = 183;}
						else if ( (LA73_122==FLOATING_POINT_LITERAL) ) {s = 184;}
						else if ( (LA73_122==STAR) ) {s = 185;}
						else if ( (LA73_122==QUESTION_MARK) ) {s = 186;}
						else if ( (LA73_122==DATETIME) ) {s = 187;}
						else if ( (LA73_122==URI) ) {s = 188;}
						else if ( (LA73_122==TILDA) && (synpred51_FTS())) {s = 189;}
						else if ( (LA73_122==CARAT) && (synpred51_FTS())) {s = 190;}
						else if ( (LA73_122==AND) && (synpred51_FTS())) {s = 191;}
						else if ( (LA73_122==AMP) && (synpred51_FTS())) {s = 192;}
						else if ( (LA73_122==EOF) && (synpred51_FTS())) {s = 193;}
						else if ( (LA73_122==RPAREN) && (synpred51_FTS())) {s = 194;}
						else if ( (LA73_122==OR) && (synpred51_FTS())) {s = 195;}
						else if ( (LA73_122==BAR) && (synpred51_FTS())) {s = 196;}
						else if ( (LA73_122==EXCLAMATION) && (synpred51_FTS())) {s = 197;}
						else if ( (LA73_122==AT) && (synpred51_FTS())) {s = 198;}
						else if ( (LA73_122==FTSPHRASE) && (synpred51_FTS())) {s = 199;}
						else if ( (LA73_122==LSQUARE) && (synpred51_FTS())) {s = 200;}
						else if ( (LA73_122==LT) && (synpred51_FTS())) {s = 201;}
						else if ( (LA73_122==COMMA||LA73_122==DOT) && (synpred51_FTS())) {s = 202;}
						else if ( (LA73_122==EQUALS) && (synpred51_FTS())) {s = 203;}
						else if ( (LA73_122==LPAREN) && (synpred51_FTS())) {s = 204;}
						else if ( (LA73_122==PERCENT) && (synpred51_FTS())) {s = 205;}
						else if ( (LA73_122==PLUS) && (synpred51_FTS())) {s = 206;}
						else if ( (LA73_122==MINUS) && (synpred51_FTS())) {s = 207;}
						 
						input.seek(index73_122);
						if ( s>=0 ) return s;
						break;

					case 183 : 
						int LA73_440 = input.LA(1);
						 
						int index73_440 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_440);
						if ( s>=0 ) return s;
						break;

					case 184 : 
						int LA73_110 = input.LA(1);
						 
						int index73_110 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_110==COMMA||LA73_110==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_110);
						if ( s>=0 ) return s;
						break;

					case 185 : 
						int LA73_235 = input.LA(1);
						 
						int index73_235 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_235==COMMA||LA73_235==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_235);
						if ( s>=0 ) return s;
						break;

					case 186 : 
						int LA73_454 = input.LA(1);
						 
						int index73_454 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_454);
						if ( s>=0 ) return s;
						break;

					case 187 : 
						int LA73_417 = input.LA(1);
						 
						int index73_417 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_417);
						if ( s>=0 ) return s;
						break;

					case 188 : 
						int LA73_405 = input.LA(1);
						 
						int index73_405 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_405==COMMA||LA73_405==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_405);
						if ( s>=0 ) return s;
						break;

					case 189 : 
						int LA73_429 = input.LA(1);
						 
						int index73_429 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_429);
						if ( s>=0 ) return s;
						break;

					case 190 : 
						int LA73_303 = input.LA(1);
						 
						int index73_303 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_303==COMMA||LA73_303==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_303);
						if ( s>=0 ) return s;
						break;

					case 191 : 
						int LA73_133 = input.LA(1);
						 
						int index73_133 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_133==COMMA||LA73_133==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_133);
						if ( s>=0 ) return s;
						break;

					case 192 : 
						int LA73_76 = input.LA(1);
						 
						int index73_76 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_76==COMMA||LA73_76==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_76);
						if ( s>=0 ) return s;
						break;

					case 193 : 
						int LA73_297 = input.LA(1);
						 
						int index73_297 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_297==COMMA||LA73_297==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_297);
						if ( s>=0 ) return s;
						break;

					case 194 : 
						int LA73_186 = input.LA(1);
						 
						int index73_186 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_186==COMMA||LA73_186==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_186);
						if ( s>=0 ) return s;
						break;

					case 195 : 
						int LA73_180 = input.LA(1);
						 
						int index73_180 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_180==COMMA||LA73_180==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_180);
						if ( s>=0 ) return s;
						break;

					case 196 : 
						int LA73_121 = input.LA(1);
						 
						int index73_121 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_121==COMMA||LA73_121==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_121);
						if ( s>=0 ) return s;
						break;

					case 197 : 
						int LA73_50 = input.LA(1);
						 
						int index73_50 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_50==COMMA||LA73_50==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_50);
						if ( s>=0 ) return s;
						break;

					case 198 : 
						int LA73_317 = input.LA(1);
						 
						int index73_317 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_317==COMMA||LA73_317==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_317);
						if ( s>=0 ) return s;
						break;

					case 199 : 
						int LA73_322 = input.LA(1);
						 
						int index73_322 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_322==COMMA||LA73_322==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_322);
						if ( s>=0 ) return s;
						break;

					case 200 : 
						int LA73_129 = input.LA(1);
						 
						int index73_129 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 210;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 212;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_129);
						if ( s>=0 ) return s;
						break;

					case 201 : 
						int LA73_449 = input.LA(1);
						 
						int index73_449 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_449);
						if ( s>=0 ) return s;
						break;

					case 202 : 
						int LA73_115 = input.LA(1);
						 
						int index73_115 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_115==COMMA||LA73_115==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_115);
						if ( s>=0 ) return s;
						break;

					case 203 : 
						int LA73_44 = input.LA(1);
						 
						int index73_44 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_44==COMMA||LA73_44==DOT) ) {s = 104;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_44);
						if ( s>=0 ) return s;
						break;

					case 204 : 
						int LA73_308 = input.LA(1);
						 
						int index73_308 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_308==COMMA||LA73_308==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_308);
						if ( s>=0 ) return s;
						break;

					case 205 : 
						int LA73_447 = input.LA(1);
						 
						int index73_447 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_447);
						if ( s>=0 ) return s;
						break;

					case 206 : 
						int LA73_104 = input.LA(1);
						 
						int index73_104 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_104==ID) ) {s = 140;}
						else if ( (LA73_104==FTSWORD) ) {s = 141;}
						else if ( (LA73_104==FTSPRE) ) {s = 142;}
						else if ( (LA73_104==FTSWILD) ) {s = 143;}
						else if ( (LA73_104==NOT) ) {s = 144;}
						else if ( (LA73_104==TO) ) {s = 145;}
						else if ( (LA73_104==DECIMAL_INTEGER_LITERAL) ) {s = 146;}
						else if ( (LA73_104==FLOATING_POINT_LITERAL) ) {s = 147;}
						else if ( (LA73_104==STAR) ) {s = 148;}
						else if ( (LA73_104==QUESTION_MARK) ) {s = 149;}
						else if ( (LA73_104==DATETIME) ) {s = 150;}
						else if ( (LA73_104==URI) ) {s = 151;}
						else if ( (LA73_104==TILDA) && (synpred52_FTS())) {s = 152;}
						else if ( (LA73_104==CARAT) && (synpred52_FTS())) {s = 153;}
						else if ( (LA73_104==AND) && (synpred52_FTS())) {s = 154;}
						else if ( (LA73_104==AMP) && (synpred52_FTS())) {s = 155;}
						else if ( (LA73_104==EOF) && (synpred52_FTS())) {s = 156;}
						else if ( (LA73_104==RPAREN) && (synpred52_FTS())) {s = 157;}
						else if ( (LA73_104==OR) && (synpred52_FTS())) {s = 158;}
						else if ( (LA73_104==BAR) && (synpred52_FTS())) {s = 159;}
						else if ( (LA73_104==EXCLAMATION) && (synpred52_FTS())) {s = 160;}
						else if ( (LA73_104==AT) && (synpred52_FTS())) {s = 161;}
						else if ( (LA73_104==FTSPHRASE) && (synpred52_FTS())) {s = 162;}
						else if ( (LA73_104==LSQUARE) && (synpred52_FTS())) {s = 163;}
						else if ( (LA73_104==LT) && (synpred52_FTS())) {s = 164;}
						else if ( (LA73_104==COMMA||LA73_104==DOT) && (synpred52_FTS())) {s = 165;}
						else if ( (LA73_104==EQUALS) && (synpred52_FTS())) {s = 166;}
						else if ( (LA73_104==LPAREN) && (synpred52_FTS())) {s = 167;}
						else if ( (LA73_104==PERCENT) && (synpred52_FTS())) {s = 168;}
						else if ( (LA73_104==PLUS) && (synpred52_FTS())) {s = 169;}
						else if ( (LA73_104==MINUS) && (synpred52_FTS())) {s = 170;}
						 
						input.seek(index73_104);
						if ( s>=0 ) return s;
						break;

					case 207 : 
						int LA73_225 = input.LA(1);
						 
						int index73_225 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_225==COMMA||LA73_225==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_225);
						if ( s>=0 ) return s;
						break;

					case 208 : 
						int LA73_266 = input.LA(1);
						 
						int index73_266 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_266==COMMA||LA73_266==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_266);
						if ( s>=0 ) return s;
						break;

					case 209 : 
						int LA73_373 = input.LA(1);
						 
						int index73_373 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_373);
						if ( s>=0 ) return s;
						break;

					case 210 : 
						int LA73_145 = input.LA(1);
						 
						int index73_145 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_145==COMMA||LA73_145==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_145);
						if ( s>=0 ) return s;
						break;

					case 211 : 
						int LA73_441 = input.LA(1);
						 
						int index73_441 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_441);
						if ( s>=0 ) return s;
						break;

					case 212 : 
						int LA73_264 = input.LA(1);
						 
						int index73_264 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_264==COMMA||LA73_264==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_264);
						if ( s>=0 ) return s;
						break;

					case 213 : 
						int LA73_341 = input.LA(1);
						 
						int index73_341 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_341);
						if ( s>=0 ) return s;
						break;

					case 214 : 
						int LA73_375 = input.LA(1);
						 
						int index73_375 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_375);
						if ( s>=0 ) return s;
						break;

					case 215 : 
						int LA73_372 = input.LA(1);
						 
						int index73_372 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_372);
						if ( s>=0 ) return s;
						break;

					case 216 : 
						int LA73_455 = input.LA(1);
						 
						int index73_455 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_455);
						if ( s>=0 ) return s;
						break;

					case 217 : 
						int LA73_369 = input.LA(1);
						 
						int index73_369 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_369==COMMA||LA73_369==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_369);
						if ( s>=0 ) return s;
						break;

					case 218 : 
						int LA73_446 = input.LA(1);
						 
						int index73_446 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_446);
						if ( s>=0 ) return s;
						break;

					case 219 : 
						int LA73_111 = input.LA(1);
						 
						int index73_111 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 173;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 175;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						else if ( (synpred54_FTS()) ) {s = 105;}
						else if ( (synpred56_FTS()) ) {s = 70;}
						else if ( (true) ) {s = 27;}
						 
						input.seek(index73_111);
						if ( s>=0 ) return s;
						break;

					case 220 : 
						int LA73_337 = input.LA(1);
						 
						int index73_337 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_337);
						if ( s>=0 ) return s;
						break;

					case 221 : 
						int LA73_406 = input.LA(1);
						 
						int index73_406 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_406==COMMA||LA73_406==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_406);
						if ( s>=0 ) return s;
						break;

					case 222 : 
						int LA73_296 = input.LA(1);
						 
						int index73_296 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_296==COMMA||LA73_296==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_296);
						if ( s>=0 ) return s;
						break;

					case 223 : 
						int LA73_428 = input.LA(1);
						 
						int index73_428 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred41_FTS()) ) {s = 209;}
						else if ( (synpred43_FTS()) ) {s = 400;}
						 
						input.seek(index73_428);
						if ( s>=0 ) return s;
						break;

					case 224 : 
						int LA73_304 = input.LA(1);
						 
						int index73_304 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_304==COMMA||LA73_304==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_304);
						if ( s>=0 ) return s;
						break;

					case 225 : 
						int LA73_445 = input.LA(1);
						 
						int index73_445 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_445==ID) ) {s = 457;}
						else if ( (LA73_445==FTSWORD) ) {s = 434;}
						else if ( (LA73_445==FTSPRE) ) {s = 435;}
						else if ( (LA73_445==FTSWILD) ) {s = 436;}
						else if ( (LA73_445==NOT) ) {s = 437;}
						else if ( (LA73_445==TO) ) {s = 438;}
						else if ( (LA73_445==DECIMAL_INTEGER_LITERAL) ) {s = 439;}
						else if ( (LA73_445==FLOATING_POINT_LITERAL) ) {s = 440;}
						else if ( (LA73_445==STAR) ) {s = 441;}
						else if ( (LA73_445==QUESTION_MARK) ) {s = 442;}
						else if ( (LA73_445==DATETIME) ) {s = 443;}
						else if ( (LA73_445==URI) ) {s = 444;}
						else if ( (LA73_445==TILDA) && (synpred43_FTS())) {s = 382;}
						else if ( (LA73_445==CARAT) && (synpred43_FTS())) {s = 383;}
						else if ( (LA73_445==AND) && (synpred43_FTS())) {s = 384;}
						else if ( (LA73_445==AMP) && (synpred43_FTS())) {s = 385;}
						else if ( (LA73_445==EOF) && (synpred43_FTS())) {s = 386;}
						else if ( (LA73_445==RPAREN) && (synpred43_FTS())) {s = 387;}
						else if ( (LA73_445==OR) && (synpred43_FTS())) {s = 388;}
						else if ( (LA73_445==BAR) && (synpred43_FTS())) {s = 389;}
						else if ( (LA73_445==EXCLAMATION) && (synpred43_FTS())) {s = 390;}
						else if ( (LA73_445==AT) && (synpred43_FTS())) {s = 391;}
						else if ( (LA73_445==FTSPHRASE) && (synpred43_FTS())) {s = 392;}
						else if ( (LA73_445==LSQUARE) && (synpred43_FTS())) {s = 393;}
						else if ( (LA73_445==LT) && (synpred43_FTS())) {s = 394;}
						else if ( (LA73_445==COMMA||LA73_445==DOT) && (synpred43_FTS())) {s = 395;}
						else if ( (LA73_445==EQUALS) && (synpred43_FTS())) {s = 396;}
						else if ( (LA73_445==LPAREN) && (synpred43_FTS())) {s = 397;}
						else if ( (LA73_445==PERCENT) && (synpred43_FTS())) {s = 398;}
						else if ( (LA73_445==PLUS) && (synpred43_FTS())) {s = 399;}
						else if ( (LA73_445==MINUS) && (synpred43_FTS())) {s = 400;}
						 
						input.seek(index73_445);
						if ( s>=0 ) return s;
						break;

					case 226 : 
						int LA73_265 = input.LA(1);
						 
						int index73_265 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_265==COMMA||LA73_265==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_265);
						if ( s>=0 ) return s;
						break;

					case 227 : 
						int LA73_414 = input.LA(1);
						 
						int index73_414 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_414);
						if ( s>=0 ) return s;
						break;

					case 228 : 
						int LA73_309 = input.LA(1);
						 
						int index73_309 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_309==COMMA||LA73_309==DOT) ) {s = 364;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_309);
						if ( s>=0 ) return s;
						break;

					case 229 : 
						int LA73_450 = input.LA(1);
						 
						int index73_450 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_450);
						if ( s>=0 ) return s;
						break;

					case 230 : 
						int LA73_146 = input.LA(1);
						 
						int index73_146 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_146==COMMA||LA73_146==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_146);
						if ( s>=0 ) return s;
						break;

					case 231 : 
						int LA73_127 = input.LA(1);
						 
						int index73_127 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_127==COMMA||LA73_127==DOT) ) {s = 122;}
						else if ( (synpred53_FTS()) ) {s = 123;}
						else if ( (synpred55_FTS()) ) {s = 102;}
						else if ( (true) ) {s = 34;}
						 
						input.seek(index73_127);
						if ( s>=0 ) return s;
						break;

					case 232 : 
						int LA73_316 = input.LA(1);
						 
						int index73_316 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_316==COMMA||LA73_316==DOT) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_316);
						if ( s>=0 ) return s;
						break;

					case 233 : 
						int LA73_323 = input.LA(1);
						 
						int index73_323 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_323==COMMA||LA73_323==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_323);
						if ( s>=0 ) return s;
						break;

					case 234 : 
						int LA73_424 = input.LA(1);
						 
						int index73_424 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_424);
						if ( s>=0 ) return s;
						break;

					case 235 : 
						int LA73_329 = input.LA(1);
						 
						int index73_329 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_329==COMMA||LA73_329==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_329);
						if ( s>=0 ) return s;
						break;

					case 236 : 
						int LA73_448 = input.LA(1);
						 
						int index73_448 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred42_FTS()) ) {s = 172;}
						else if ( (synpred44_FTS()) ) {s = 362;}
						 
						input.seek(index73_448);
						if ( s>=0 ) return s;
						break;

					case 237 : 
						int LA73_314 = input.LA(1);
						 
						int index73_314 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_314==DOT) ) {s = 401;}
						else if ( (LA73_314==COMMA) ) {s = 313;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_314);
						if ( s>=0 ) return s;
						break;

					case 238 : 
						int LA73_234 = input.LA(1);
						 
						int index73_234 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_234==COMMA||LA73_234==DOT) ) {s = 294;}
						else if ( (synpred46_FTS()) ) {s = 174;}
						else if ( (synpred48_FTS()) ) {s = 258;}
						 
						input.seek(index73_234);
						if ( s>=0 ) return s;
						break;

					case 239 : 
						int LA73_324 = input.LA(1);
						 
						int index73_324 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_324==COMMA||LA73_324==DOT) ) {s = 402;}
						else if ( (synpred45_FTS()) ) {s = 211;}
						else if ( (synpred47_FTS()) ) {s = 291;}
						 
						input.seek(index73_324);
						if ( s>=0 ) return s;
						break;

					case 240 : 
						int LA73_33 = input.LA(1);
						 
						int index73_33 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_33==ID) ) {s = 72;}
						else if ( (LA73_33==FTSWORD) ) {s = 73;}
						else if ( (LA73_33==FTSPRE) ) {s = 74;}
						else if ( (LA73_33==FTSWILD) ) {s = 75;}
						else if ( (LA73_33==NOT) ) {s = 76;}
						else if ( (LA73_33==TO) ) {s = 77;}
						else if ( (LA73_33==DECIMAL_INTEGER_LITERAL) ) {s = 78;}
						else if ( (LA73_33==FLOATING_POINT_LITERAL) ) {s = 79;}
						else if ( (LA73_33==STAR) ) {s = 80;}
						else if ( (LA73_33==QUESTION_MARK) ) {s = 81;}
						else if ( (LA73_33==DATETIME) ) {s = 82;}
						else if ( (LA73_33==URI) ) {s = 83;}
						else if ( (LA73_33==TILDA) && (synpred55_FTS())) {s = 84;}
						else if ( (LA73_33==CARAT) && (synpred55_FTS())) {s = 85;}
						else if ( (LA73_33==AND) && (synpred55_FTS())) {s = 86;}
						else if ( (LA73_33==AMP) && (synpred55_FTS())) {s = 87;}
						else if ( (LA73_33==EOF) && (synpred55_FTS())) {s = 88;}
						else if ( (LA73_33==RPAREN) && (synpred55_FTS())) {s = 89;}
						else if ( (LA73_33==OR) && (synpred55_FTS())) {s = 90;}
						else if ( (LA73_33==BAR) && (synpred55_FTS())) {s = 91;}
						else if ( (LA73_33==EXCLAMATION) && (synpred55_FTS())) {s = 92;}
						else if ( (LA73_33==AT) && (synpred55_FTS())) {s = 93;}
						else if ( (LA73_33==FTSPHRASE) && (synpred55_FTS())) {s = 94;}
						else if ( (LA73_33==LSQUARE) && (synpred55_FTS())) {s = 95;}
						else if ( (LA73_33==LT) && (synpred55_FTS())) {s = 96;}
						else if ( (LA73_33==COMMA||LA73_33==DOT) && (synpred55_FTS())) {s = 97;}
						else if ( (LA73_33==EQUALS) && (synpred55_FTS())) {s = 98;}
						else if ( (LA73_33==LPAREN) && (synpred55_FTS())) {s = 99;}
						else if ( (LA73_33==PERCENT) && (synpred55_FTS())) {s = 100;}
						else if ( (LA73_33==PLUS) && (synpred55_FTS())) {s = 101;}
						else if ( (LA73_33==MINUS) && (synpred55_FTS())) {s = 102;}
						 
						input.seek(index73_33);
						if ( s>=0 ) return s;
						break;

					case 241 : 
						int LA73_216 = input.LA(1);
						 
						int index73_216 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_216==COMMA||LA73_216==DOT) ) {s = 214;}
						else if ( (synpred50_FTS()) ) {s = 176;}
						else if ( (synpred52_FTS()) ) {s = 170;}
						 
						input.seek(index73_216);
						if ( s>=0 ) return s;
						break;

					case 242 : 
						int LA73_179 = input.LA(1);
						 
						int index73_179 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_179==COMMA||LA73_179==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_179);
						if ( s>=0 ) return s;
						break;

					case 243 : 
						int LA73_224 = input.LA(1);
						 
						int index73_224 = input.index();
						input.rewind();
						s = -1;
						if ( (LA73_224==COMMA||LA73_224==DOT) ) {s = 221;}
						else if ( (synpred49_FTS()) ) {s = 213;}
						else if ( (synpred51_FTS()) ) {s = 207;}
						 
						input.seek(index73_224);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 73, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	public static final BitSet FOLLOW_ftsDisjunction_in_ftsQuery577 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_ftsQuery579 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cmisExplicitDisjunction_in_ftsDisjunction639 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsExplicitDisjunction_in_ftsDisjunction653 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsImplicitDisjunction_in_ftsDisjunction667 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsImplicitConjunction_in_ftsExplicitDisjunction700 = new BitSet(new long[]{0x0000000000000082L,0x0000000000020000L});
	public static final BitSet FOLLOW_or_in_ftsExplicitDisjunction703 = new BitSet(new long[]{0x803C80000920A8F0L,0x0000016101164138L});
	public static final BitSet FOLLOW_ftsImplicitConjunction_in_ftsExplicitDisjunction705 = new BitSet(new long[]{0x0000000000000082L,0x0000000000020000L});
	public static final BitSet FOLLOW_cmisConjunction_in_cmisExplicitDisjunction789 = new BitSet(new long[]{0x0000000000000082L,0x0000000000020000L});
	public static final BitSet FOLLOW_or_in_cmisExplicitDisjunction792 = new BitSet(new long[]{0x803C80000020A800L,0x0000014101004100L});
	public static final BitSet FOLLOW_cmisConjunction_in_cmisExplicitDisjunction794 = new BitSet(new long[]{0x0000000000000082L,0x0000000000020000L});
	public static final BitSet FOLLOW_or_in_ftsImplicitDisjunction879 = new BitSet(new long[]{0x803C80000920A8E0L,0x0000016101164138L});
	public static final BitSet FOLLOW_ftsExplicitConjunction_in_ftsImplicitDisjunction882 = new BitSet(new long[]{0x803C80000920A8E2L,0x0000016101164138L});
	public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplicitConjunction969 = new BitSet(new long[]{0x0000000000000032L});
	public static final BitSet FOLLOW_and_in_ftsExplicitConjunction972 = new BitSet(new long[]{0x803C80000920A8E0L,0x0000016101164138L});
	public static final BitSet FOLLOW_ftsPrefixed_in_ftsExplicitConjunction974 = new BitSet(new long[]{0x0000000000000032L});
	public static final BitSet FOLLOW_and_in_ftsImplicitConjunction1059 = new BitSet(new long[]{0x803C80000920A8E0L,0x0000016101164138L});
	public static final BitSet FOLLOW_ftsPrefixed_in_ftsImplicitConjunction1062 = new BitSet(new long[]{0x803C80000920A8F2L,0x0000016101164138L});
	public static final BitSet FOLLOW_cmisPrefixed_in_cmisConjunction1146 = new BitSet(new long[]{0x803C80000020A802L,0x0000014101004100L});
	public static final BitSet FOLLOW_not_in_ftsPrefixed1238 = new BitSet(new long[]{0x803C80000120A860L,0x0000016101064038L});
	public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1240 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsPrefixed1242 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1306 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsPrefixed1308 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PLUS_in_ftsPrefixed1372 = new BitSet(new long[]{0x803C80000120A860L,0x0000016101064038L});
	public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1374 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsPrefixed1376 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BAR_in_ftsPrefixed1440 = new BitSet(new long[]{0x803C80000120A860L,0x0000016101064038L});
	public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1442 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsPrefixed1444 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MINUS_in_ftsPrefixed1508 = new BitSet(new long[]{0x803C80000120A860L,0x0000016101064038L});
	public static final BitSet FOLLOW_ftsTest_in_ftsPrefixed1510 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsPrefixed1512 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cmisTest_in_cmisPrefixed1597 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MINUS_in_cmisPrefixed1657 = new BitSet(new long[]{0x803C80000020A800L,0x0000014101004000L});
	public static final BitSet FOLLOW_cmisTest_in_cmisPrefixed1659 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsTest1751 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsRange_in_ftsTest1828 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroup_in_ftsTest1907 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsTermOrPhrase_in_ftsTest1956 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsExactTermOrPhrase_in_ftsTest1985 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsTokenisedTermOrPhrase_in_ftsTest2015 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_ftsTest2046 = new BitSet(new long[]{0x803C80000920A8F0L,0x0000016101164138L});
	public static final BitSet FOLLOW_ftsDisjunction_in_ftsTest2048 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_ftsTest2050 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_template_in_ftsTest2083 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cmisTerm_in_cmisTest2136 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cmisPhrase_in_cmisTest2196 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PERCENT_in_template2277 = new BitSet(new long[]{0x8000000000000060L,0x0000014000024000L});
	public static final BitSet FOLLOW_tempReference_in_template2279 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PERCENT_in_template2339 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_LPAREN_in_template2341 = new BitSet(new long[]{0x8000000000000060L,0x0000014000024000L});
	public static final BitSet FOLLOW_tempReference_in_template2344 = new BitSet(new long[]{0x8000000000000860L,0x0000014008024000L});
	public static final BitSet FOLLOW_COMMA_in_template2346 = new BitSet(new long[]{0x8000000000000060L,0x0000014008024000L});
	public static final BitSet FOLLOW_RPAREN_in_template2351 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TILDA_in_fuzzy2433 = new BitSet(new long[]{0x0000800000008000L});
	public static final BitSet FOLLOW_number_in_fuzzy2435 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TILDA_in_slop2516 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_slop2518 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CARAT_in_boost2599 = new BitSet(new long[]{0x0000800000008000L});
	public static final BitSet FOLLOW_number_in_boost2601 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldReference_in_ftsTermOrPhrase2690 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_ftsTermOrPhrase2692 = new BitSet(new long[]{0x803C80000020A800L,0x0000014101004000L});
	public static final BitSet FOLLOW_FTSPHRASE_in_ftsTermOrPhrase2720 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsTermOrPhrase2728 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWord_in_ftsTermOrPhrase2795 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsTermOrPhrase2804 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPHRASE_in_ftsTermOrPhrase2865 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsTermOrPhrase2873 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWord_in_ftsTermOrPhrase2923 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsTermOrPhrase2932 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EQUALS_in_ftsExactTermOrPhrase3011 = new BitSet(new long[]{0x803C80000020A860L,0x0000014101024000L});
	public static final BitSet FOLLOW_fieldReference_in_ftsExactTermOrPhrase3039 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_ftsExactTermOrPhrase3041 = new BitSet(new long[]{0x803C80000020A800L,0x0000014101004000L});
	public static final BitSet FOLLOW_FTSPHRASE_in_ftsExactTermOrPhrase3069 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsExactTermOrPhrase3077 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWord_in_ftsExactTermOrPhrase3144 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsExactTermOrPhrase3153 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPHRASE_in_ftsExactTermOrPhrase3214 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsExactTermOrPhrase3222 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWord_in_ftsExactTermOrPhrase3272 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsExactTermOrPhrase3281 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TILDA_in_ftsTokenisedTermOrPhrase3362 = new BitSet(new long[]{0x803C80000020A860L,0x0000014101024000L});
	public static final BitSet FOLLOW_fieldReference_in_ftsTokenisedTermOrPhrase3390 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_ftsTokenisedTermOrPhrase3392 = new BitSet(new long[]{0x803C80000020A800L,0x0000014101004000L});
	public static final BitSet FOLLOW_FTSPHRASE_in_ftsTokenisedTermOrPhrase3420 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsTokenisedTermOrPhrase3428 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWord_in_ftsTokenisedTermOrPhrase3495 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsTokenisedTermOrPhrase3504 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPHRASE_in_ftsTokenisedTermOrPhrase3565 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsTokenisedTermOrPhrase3573 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWord_in_ftsTokenisedTermOrPhrase3623 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsTokenisedTermOrPhrase3632 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWord_in_cmisTerm3705 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPHRASE_in_cmisPhrase3759 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldReference_in_ftsRange3814 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_ftsRange3816 = new BitSet(new long[]{0x803C80000000A000L,0x0000010100000030L});
	public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsRange3820 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldReference_in_ftsFieldGroup3876 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_ftsFieldGroup3878 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroup3880 = new BitSet(new long[]{0x803C80000920A8B0L,0x0000016101124138L});
	public static final BitSet FOLLOW_ftsFieldGroupDisjunction_in_ftsFieldGroup3882 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroup3884 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupExplicitDisjunction_in_ftsFieldGroupDisjunction3969 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupImplicitDisjunction_in_ftsFieldGroupDisjunction3983 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunction_in_ftsFieldGroupExplicitDisjunction4016 = new BitSet(new long[]{0x0000000000000082L,0x0000000000020000L});
	public static final BitSet FOLLOW_or_in_ftsFieldGroupExplicitDisjunction4019 = new BitSet(new long[]{0x803C80000920A8B0L,0x0000016101104138L});
	public static final BitSet FOLLOW_ftsFieldGroupImplicitConjunction_in_ftsFieldGroupExplicitDisjunction4021 = new BitSet(new long[]{0x0000000000000082L,0x0000000000020000L});
	public static final BitSet FOLLOW_or_in_ftsFieldGroupImplicitDisjunction4106 = new BitSet(new long[]{0x803C80000920A880L,0x0000016101104138L});
	public static final BitSet FOLLOW_ftsFieldGroupExplicitConjunction_in_ftsFieldGroupImplicitDisjunction4109 = new BitSet(new long[]{0x803C80000920A882L,0x0000016101124138L});
	public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplicitConjunction4196 = new BitSet(new long[]{0x0000000000000032L});
	public static final BitSet FOLLOW_and_in_ftsFieldGroupExplicitConjunction4199 = new BitSet(new long[]{0x803C80000920A880L,0x0000016101104138L});
	public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupExplicitConjunction4201 = new BitSet(new long[]{0x0000000000000032L});
	public static final BitSet FOLLOW_and_in_ftsFieldGroupImplicitConjunction4286 = new BitSet(new long[]{0x803C80000920A880L,0x0000016101104138L});
	public static final BitSet FOLLOW_ftsFieldGroupPrefixed_in_ftsFieldGroupImplicitConjunction4289 = new BitSet(new long[]{0x803C80000920A8B2L,0x0000016101104138L});
	public static final BitSet FOLLOW_not_in_ftsFieldGroupPrefixed4379 = new BitSet(new long[]{0x803C80000120A800L,0x0000016101004038L});
	public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4381 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed4383 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4447 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed4449 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PLUS_in_ftsFieldGroupPrefixed4513 = new BitSet(new long[]{0x803C80000120A800L,0x0000016101004038L});
	public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4515 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed4517 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BAR_in_ftsFieldGroupPrefixed4581 = new BitSet(new long[]{0x803C80000120A800L,0x0000016101004038L});
	public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4583 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed4585 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MINUS_in_ftsFieldGroupPrefixed4649 = new BitSet(new long[]{0x803C80000120A800L,0x0000016101004038L});
	public static final BitSet FOLLOW_ftsFieldGroupTest_in_ftsFieldGroupPrefixed4651 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_boost_in_ftsFieldGroupPrefixed4653 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupProximity_in_ftsFieldGroupTest4744 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupTest4810 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest4820 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupExactTerm_in_ftsFieldGroupTest4891 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest4901 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupPhrase_in_ftsFieldGroupTest4972 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsFieldGroupTest4982 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupExactPhrase_in_ftsFieldGroupTest5053 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsFieldGroupTest5063 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupTokenisedPhrase_in_ftsFieldGroupTest5134 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_slop_in_ftsFieldGroupTest5144 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupSynonym_in_ftsFieldGroupTest5215 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_fuzzy_in_ftsFieldGroupTest5225 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupRange_in_ftsFieldGroupTest5296 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_ftsFieldGroupTest5356 = new BitSet(new long[]{0x803C80000920A8B0L,0x0000016101124138L});
	public static final BitSet FOLLOW_ftsFieldGroupDisjunction_in_ftsFieldGroupTest5358 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_ftsFieldGroupTest5360 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWord_in_ftsFieldGroupTerm5413 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EQUALS_in_ftsFieldGroupExactTerm5446 = new BitSet(new long[]{0x803880000020A800L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupExactTerm5448 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPHRASE_in_ftsFieldGroupPhrase5501 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EQUALS_in_ftsFieldGroupExactPhrase5542 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_ftsFieldGroupExactPhrase_in_ftsFieldGroupExactPhrase5544 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TILDA_in_ftsFieldGroupTokenisedPhrase5605 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_ftsFieldGroupExactPhrase_in_ftsFieldGroupTokenisedPhrase5607 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TILDA_in_ftsFieldGroupSynonym5660 = new BitSet(new long[]{0x803880000020A800L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsFieldGroupTerm_in_ftsFieldGroupSynonym5662 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupProximityTerm_in_ftsFieldGroupProximity5715 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_proximityGroup_in_ftsFieldGroupProximity5725 = new BitSet(new long[]{0x803880000000A000L,0x0000014100004000L});
	public static final BitSet FOLLOW_ftsFieldGroupProximityTerm_in_ftsFieldGroupProximity5727 = new BitSet(new long[]{0x0000000000000002L,0x0000000100000000L});
	public static final BitSet FOLLOW_ID_in_ftsFieldGroupProximityTerm5791 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSWORD_in_ftsFieldGroupProximityTerm5803 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPRE_in_ftsFieldGroupProximityTerm5815 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSWILD_in_ftsFieldGroupProximityTerm5827 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NOT_in_ftsFieldGroupProximityTerm5839 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TO_in_ftsFieldGroupProximityTerm5851 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_ftsFieldGroupProximityTerm5863 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_ftsFieldGroupProximityTerm5875 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DATETIME_in_ftsFieldGroupProximityTerm5887 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STAR_in_ftsFieldGroupProximityTerm5899 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_URI_in_ftsFieldGroupProximityTerm5911 = new BitSet(new long[]{0x8000000000000020L,0x0000004000024000L});
	public static final BitSet FOLLOW_identifier_in_ftsFieldGroupProximityTerm5913 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STAR_in_proximityGroup5946 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
	public static final BitSet FOLLOW_LPAREN_in_proximityGroup5949 = new BitSet(new long[]{0x0000000000008000L,0x0000000008000000L});
	public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_proximityGroup5951 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_proximityGroup5954 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange6038 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_DOTDOT_in_ftsFieldGroupRange6040 = new BitSet(new long[]{0x803C80000000A000L,0x0000010100000000L});
	public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange6042 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_range_left_in_ftsFieldGroupRange6080 = new BitSet(new long[]{0x803C80000000A000L,0x0000010100000000L});
	public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange6082 = new BitSet(new long[]{0x0000000000000000L,0x0000004000000000L});
	public static final BitSet FOLLOW_TO_in_ftsFieldGroupRange6084 = new BitSet(new long[]{0x803C80000000A000L,0x0000010100000000L});
	public static final BitSet FOLLOW_ftsRangeWord_in_ftsFieldGroupRange6086 = new BitSet(new long[]{0x2000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_range_right_in_ftsFieldGroupRange6088 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LSQUARE_in_range_left6147 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_range_left6179 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_RSQUARE_in_range_right6232 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_range_right6264 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AT_in_fieldReference6320 = new BitSet(new long[]{0x8000000000000020L,0x0000014000024000L});
	public static final BitSet FOLLOW_prefix_in_fieldReference6357 = new BitSet(new long[]{0x8000000000000020L,0x0000004000024000L});
	public static final BitSet FOLLOW_uri_in_fieldReference6377 = new BitSet(new long[]{0x8000000000000020L,0x0000004000024000L});
	public static final BitSet FOLLOW_identifier_in_fieldReference6398 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AT_in_tempReference6485 = new BitSet(new long[]{0x8000000000000020L,0x0000014000024000L});
	public static final BitSet FOLLOW_prefix_in_tempReference6514 = new BitSet(new long[]{0x8000000000000020L,0x0000004000024000L});
	public static final BitSet FOLLOW_uri_in_tempReference6534 = new BitSet(new long[]{0x8000000000000020L,0x0000004000024000L});
	public static final BitSet FOLLOW_identifier_in_tempReference6555 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifier_in_prefix6642 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_prefix6644 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_URI_in_uri6725 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_identifier6827 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_DOT_in_identifier6829 = new BitSet(new long[]{0x8000000000000000L});
	public static final BitSet FOLLOW_ID_in_identifier6833 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_identifier6882 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TO_in_identifier6949 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OR_in_identifier6987 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AND_in_identifier7025 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NOT_in_identifier7064 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord7182 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7188 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7190 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7196 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7198 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7204 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7206 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7212 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7214 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7220 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7280 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7282 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7288 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7290 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7296 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7298 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7304 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7306 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7312 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord7379 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7385 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7387 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7393 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7395 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7401 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7403 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7409 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7411 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7475 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7477 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7483 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7485 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7491 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7493 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7499 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7501 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord7566 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7572 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7574 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7580 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7582 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7588 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7590 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7596 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7650 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7652 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7658 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7660 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7666 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7668 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7674 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord7732 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7738 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7740 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7746 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7748 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7754 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7756 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7812 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7814 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7820 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7822 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7828 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7830 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord7886 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7892 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7894 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7900 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7902 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7908 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7954 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7956 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7962 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord7964 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord7970 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord8021 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8027 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord8029 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8035 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord8037 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8086 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord8088 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8094 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord8096 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord8145 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8151 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord8153 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8159 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8197 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord8199 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8205 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord8247 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8253 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord8255 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8295 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_ftsWord8297 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_ftsWord8313 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8319 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_ftsWord8332 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_ftsWordBase8377 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSWORD_in_ftsWordBase8389 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPRE_in_ftsWordBase8401 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSWILD_in_ftsWordBase8414 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NOT_in_ftsWordBase8427 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TO_in_ftsWordBase8439 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_ftsWordBase8451 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_ftsWordBase8463 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STAR_in_ftsWordBase8475 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUESTION_MARK_in_ftsWordBase8487 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DATETIME_in_ftsWordBase8499 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_URI_in_ftsWordBase8511 = new BitSet(new long[]{0x8000000000000020L,0x0000004000024000L});
	public static final BitSet FOLLOW_identifier_in_ftsWordBase8513 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_ftsRangeWord8593 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSWORD_in_ftsRangeWord8605 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPRE_in_ftsRangeWord8617 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSWILD_in_ftsRangeWord8629 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FTSPHRASE_in_ftsRangeWord8641 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_INTEGER_LITERAL_in_ftsRangeWord8653 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOATING_POINT_LITERAL_in_ftsRangeWord8665 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DATETIME_in_ftsRangeWord8677 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STAR_in_ftsRangeWord8689 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_URI_in_ftsRangeWord8701 = new BitSet(new long[]{0x8000000000000020L,0x0000004000024000L});
	public static final BitSet FOLLOW_identifier_in_ftsRangeWord8703 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OR_in_or8738 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BAR_in_or8750 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_BAR_in_or8752 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AND_in_and8785 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AMP_in_and8797 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_AMP_in_and8799 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_not_in_synpred1_FTS1233 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupProximity_in_synpred2_FTS1746 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsRange_in_synpred3_FTS1823 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroup_in_synpred4_FTS1902 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsTermOrPhrase_in_synpred5_FTS1951 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsExactTermOrPhrase_in_synpred6_FTS1980 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsTokenisedTermOrPhrase_in_synpred7_FTS2010 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldReference_in_synpred8_FTS2683 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_synpred8_FTS2685 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred9_FTS2724 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred10_FTS2799 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred11_FTS2869 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred12_FTS2927 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldReference_in_synpred13_FTS3032 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_synpred13_FTS3034 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred14_FTS3073 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred15_FTS3148 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred16_FTS3218 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred17_FTS3276 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldReference_in_synpred18_FTS3383 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_COLON_in_synpred18_FTS3385 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred19_FTS3424 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred20_FTS3499 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred21_FTS3569 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred22_FTS3627 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_not_in_synpred23_FTS4374 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupProximity_in_synpred24_FTS4739 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupTerm_in_synpred25_FTS4805 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred26_FTS4815 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupExactTerm_in_synpred27_FTS4886 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred28_FTS4896 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupPhrase_in_synpred29_FTS4967 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred30_FTS4977 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupExactPhrase_in_synpred31_FTS5048 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred32_FTS5058 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupTokenisedPhrase_in_synpred33_FTS5129 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_slop_in_synpred34_FTS5139 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupSynonym_in_synpred35_FTS5210 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fuzzy_in_synpred36_FTS5220 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsFieldGroupRange_in_synpred37_FTS5291 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_proximityGroup_in_synpred38_FTS5720 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_prefix_in_synpred39_FTS6352 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_synpred40_FTS6807 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_DOT_in_synpred40_FTS6809 = new BitSet(new long[]{0x8000000000000000L});
	public static final BitSet FOLLOW_ID_in_synpred40_FTS6811 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_synpred41_FTS7127 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred41_FTS7133 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred41_FTS7135 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred41_FTS7141 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred41_FTS7143 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred41_FTS7149 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred41_FTS7151 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred41_FTS7157 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred41_FTS7159 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred41_FTS7165 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred42_FTS7233 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred42_FTS7235 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred42_FTS7241 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred42_FTS7243 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred42_FTS7249 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred42_FTS7251 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred42_FTS7257 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_DOT_in_synpred42_FTS7259 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COMMA_in_synpred42_FTS7261 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred42_FTS7263 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_synpred43_FTS7326 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred43_FTS7332 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred43_FTS7334 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred43_FTS7340 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred43_FTS7342 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred43_FTS7348 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred43_FTS7350 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred43_FTS7356 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred43_FTS7358 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred44_FTS7428 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred44_FTS7430 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred44_FTS7436 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred44_FTS7438 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred44_FTS7444 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred44_FTS7446 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred44_FTS7452 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred44_FTS7454 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_synpred45_FTS7519 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred45_FTS7525 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred45_FTS7527 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred45_FTS7533 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred45_FTS7535 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred45_FTS7541 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred45_FTS7543 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred45_FTS7549 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred46_FTS7609 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred46_FTS7611 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred46_FTS7617 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred46_FTS7619 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred46_FTS7625 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred46_FTS7627 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred46_FTS7633 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_synpred47_FTS7687 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred47_FTS7693 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred47_FTS7695 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred47_FTS7701 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred47_FTS7703 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred47_FTS7709 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred47_FTS7711 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred48_FTS7773 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred48_FTS7775 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred48_FTS7781 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred48_FTS7783 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred48_FTS7789 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred48_FTS7791 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_synpred49_FTS7847 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred49_FTS7853 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred49_FTS7855 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred49_FTS7861 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred49_FTS7863 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred49_FTS7869 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred50_FTS7921 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred50_FTS7923 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred50_FTS7929 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred50_FTS7931 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred50_FTS7937 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_synpred51_FTS7984 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred51_FTS7990 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred51_FTS7992 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred51_FTS7998 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred51_FTS8000 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred52_FTS8054 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred52_FTS8056 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred52_FTS8062 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred52_FTS8064 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_synpred53_FTS8114 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred53_FTS8120 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred53_FTS8122 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred53_FTS8128 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred54_FTS8172 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred54_FTS8174 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred54_FTS8180 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_synpred55_FTS8218 = new BitSet(new long[]{0x803880000000A000L,0x0000014101004000L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred55_FTS8224 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred55_FTS8226 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ftsWordBase_in_synpred56_FTS8272 = new BitSet(new long[]{0x0000000000200800L});
	public static final BitSet FOLLOW_set_in_synpred56_FTS8274 = new BitSet(new long[]{0x0000000000000002L});
}
