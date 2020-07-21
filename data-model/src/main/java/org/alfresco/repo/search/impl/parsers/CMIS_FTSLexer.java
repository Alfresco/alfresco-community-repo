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
// $ANTLR 3.5.2 W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g 2015-06-18 19:37:59

package org.alfresco.repo.search.impl.parsers;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class CMIS_FTSLexer extends Lexer {
	public static final int EOF=-1;
	public static final int CONJUNCTION=4;
	public static final int DEFAULT=5;
	public static final int DISJUNCTION=6;
	public static final int EXCLUDE=7;
	public static final int FTSPHRASE=8;
	public static final int FTSWORD=9;
	public static final int F_ESC=10;
	public static final int IN_WORD=11;
	public static final int MINUS=12;
	public static final int OR=13;
	public static final int PHRASE=14;
	public static final int START_WORD=15;
	public static final int TERM=16;
	public static final int WS=17;

	List tokens = new ArrayList();
	public void emit(Token token) {
	        state.token = token;
	        tokens.add(token);
	}
	public Token nextToken() {
	        nextTokenImpl();
	        if ( tokens.size()==0 ) {
	            return getEOFToken();
	        }
	        return (Token)tokens.remove(0);
	}

	public Token nextTokenImpl() {
	        while (true) 
	        {
	            state.token = null;
	            state.channel = Token.DEFAULT_CHANNEL;
	            state.tokenStartCharIndex = input.index();
	            state.tokenStartCharPositionInLine = input.getCharPositionInLine();
	            state.tokenStartLine = input.getLine();
	            state.text = null;
	            if ( input.LA(1)==CharStream.EOF ) 
	            {
	                return getEOFToken();
	            }
	            try 
	            {
	                mTokens();
	                if ( state.token==null ) 
	                {
	                    emit();
	                }
	                else if ( state.token==Token.SKIP_TOKEN ) 
	                {
	                    continue;
	                }
	                return state.token;
	            }
	            catch (RecognitionException re) 
	            {
	                throw new CmisInvalidArgumentException(getErrorString(re), re);
	            }
	        }
	    }
	    
	    public String getErrorString(RecognitionException e)
	    {
	        String hdr = getErrorHeader(e);
	        String msg = getErrorMessage(e, this.getTokenNames());
	        return hdr+" "+msg;
	    }


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public CMIS_FTSLexer() {} 
	public CMIS_FTSLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public CMIS_FTSLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g"; }

	// $ANTLR start "FTSPHRASE"
	public final void mFTSPHRASE() throws RecognitionException {
		try {
			int _type = FTSPHRASE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:346:9: ( '\\'' ( F_ESC |~ ( '\\\\' | '\\'' ) )* '\\'' )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:347:9: '\\'' ( F_ESC |~ ( '\\\\' | '\\'' ) )* '\\''
			{
			match('\''); 
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:348:9: ( F_ESC |~ ( '\\\\' | '\\'' ) )*
			loop1:
			while (true) {
				int alt1=3;
				int LA1_0 = input.LA(1);
				if ( (LA1_0=='\\') ) {
					alt1=1;
				}
				else if ( ((LA1_0 >= '\u0000' && LA1_0 <= '&')||(LA1_0 >= '(' && LA1_0 <= '[')||(LA1_0 >= ']' && LA1_0 <= '\uFFFF')) ) {
					alt1=2;
				}

				switch (alt1) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:349:17: F_ESC
					{
					mF_ESC(); 

					}
					break;
				case 2 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:351:17: ~ ( '\\\\' | '\\'' )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop1;
				}
			}

			match('\''); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FTSPHRASE"

	// $ANTLR start "F_ESC"
	public final void mF_ESC() throws RecognitionException {
		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:363:9: ( '\\\\' ( '\\\\' | '\\'' ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:364:9: '\\\\' ( '\\\\' | '\\'' )
			{
			match('\\'); 
			if ( input.LA(1)=='\''||input.LA(1)=='\\' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "F_ESC"

	// $ANTLR start "OR"
	public final void mOR() throws RecognitionException {
		try {
			int _type = OR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:372:9: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:373:9: ( 'O' | 'o' ) ( 'R' | 'r' )
			{
			if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OR"

	// $ANTLR start "MINUS"
	public final void mMINUS() throws RecognitionException {
		try {
			int _type = MINUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:384:9: ( '-' )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:385:9: '-'
			{
			match('-'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MINUS"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:395:9: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:396:9: ( ' ' | '\\t' | '\\r' | '\\n' )+
			{
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:396:9: ( ' ' | '\\t' | '\\r' | '\\n' )+
			int cnt2=0;
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( ((LA2_0 >= '\t' && LA2_0 <= '\n')||LA2_0=='\r'||LA2_0==' ') ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt2 >= 1 ) break loop2;
					EarlyExitException eee = new EarlyExitException(2, input);
					throw eee;
				}
				cnt2++;
			}

			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	// $ANTLR start "FTSWORD"
	public final void mFTSWORD() throws RecognitionException {
		try {
			int _type = FTSWORD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:407:9: ( START_WORD ( IN_WORD )* )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:408:9: START_WORD ( IN_WORD )*
			{
			mSTART_WORD(); 

			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:408:20: ( IN_WORD )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '\u0000' && LA3_0 <= '\b')||(LA3_0 >= '\u000B' && LA3_0 <= '\f')||(LA3_0 >= '\u000E' && LA3_0 <= '\u001F')||(LA3_0 >= '!' && LA3_0 <= '\uFFFF')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\b')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\u001F')||(input.LA(1) >= '!' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop3;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FTSWORD"

	// $ANTLR start "START_WORD"
	public final void mSTART_WORD() throws RecognitionException {
		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:414:9: (~ ( ' ' | '\\t' | '\\r' | '\\n' | '-' ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:
			{
			if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\b')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\u001F')||(input.LA(1) >= '!' && input.LA(1) <= ',')||(input.LA(1) >= '.' && input.LA(1) <= '\uFFFF') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "START_WORD"

	// $ANTLR start "IN_WORD"
	public final void mIN_WORD() throws RecognitionException {
		try {
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:426:9: (~ ( ' ' | '\\t' | '\\r' | '\\n' ) )
			// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:
			{
			if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\b')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\u001F')||(input.LA(1) >= '!' && input.LA(1) <= '\uFFFF') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IN_WORD"

	@Override
	public void mTokens() throws RecognitionException {
		// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:1:8: ( FTSPHRASE | OR | MINUS | WS | FTSWORD )
		int alt4=5;
		alt4 = dfa4.predict(input);
		switch (alt4) {
			case 1 :
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:1:10: FTSPHRASE
				{
				mFTSPHRASE(); 

				}
				break;
			case 2 :
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:1:20: OR
				{
				mOR(); 

				}
				break;
			case 3 :
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:1:23: MINUS
				{
				mMINUS(); 

				}
				break;
			case 4 :
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:1:29: WS
				{
				mWS(); 

				}
				break;
			case 5 :
				// W:\\alfresco\\HEAD-BUG-FIX\\root\\projects\\data-model\\source\\java\\org\\alfresco\\repo\\search\\impl\\parsers\\CMIS_FTS.g:1:32: FTSWORD
				{
				mFTSWORD(); 

				}
				break;

		}
	}


	protected DFA4 dfa4 = new DFA4(this);
	static final String DFA4_eotS =
		"\1\uffff\2\5\3\uffff\2\5\1\11\1\uffff\1\14\1\5\1\uffff";
	static final String DFA4_eofS =
		"\15\uffff";
	static final String DFA4_minS =
		"\2\0\1\122\3\uffff\1\47\2\0\1\uffff\2\0\1\uffff";
	static final String DFA4_maxS =
		"\2\uffff\1\162\3\uffff\1\134\2\uffff\1\uffff\2\uffff\1\uffff";
	static final String DFA4_acceptS =
		"\3\uffff\1\3\1\4\1\5\3\uffff\1\1\2\uffff\1\2";
	static final String DFA4_specialS =
		"\1\0\1\1\5\uffff\1\5\1\3\1\uffff\1\4\1\2\1\uffff}>";
	static final String[] DFA4_transitionS = {
			"\11\5\2\4\2\5\1\4\22\5\1\4\6\5\1\1\5\5\1\3\41\5\1\2\37\5\1\2\uff90\5",
			"\11\7\2\11\2\7\1\11\22\7\1\11\6\7\1\10\64\7\1\6\uffa3\7",
			"\1\12\37\uffff\1\12",
			"",
			"",
			"",
			"\1\13\64\uffff\1\13",
			"\11\7\2\11\2\7\1\11\22\7\1\11\6\7\1\10\64\7\1\6\uffa3\7",
			"\11\5\2\uffff\2\5\1\uffff\22\5\1\uffff\uffdf\5",
			"",
			"\11\5\2\uffff\2\5\1\uffff\22\5\1\uffff\uffdf\5",
			"\11\7\2\11\2\7\1\11\22\7\1\11\6\7\1\10\64\7\1\6\uffa3\7",
			""
	};

	static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
	static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
	static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
	static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
	static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
	static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
	static final short[][] DFA4_transition;

	static {
		int numStates = DFA4_transitionS.length;
		DFA4_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
		}
	}

	protected class DFA4 extends DFA {

		public DFA4(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 4;
			this.eot = DFA4_eot;
			this.eof = DFA4_eof;
			this.min = DFA4_min;
			this.max = DFA4_max;
			this.accept = DFA4_accept;
			this.special = DFA4_special;
			this.transition = DFA4_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( FTSPHRASE | OR | MINUS | WS | FTSWORD );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA4_0 = input.LA(1);
						s = -1;
						if ( (LA4_0=='\'') ) {s = 1;}
						else if ( (LA4_0=='O'||LA4_0=='o') ) {s = 2;}
						else if ( (LA4_0=='-') ) {s = 3;}
						else if ( ((LA4_0 >= '\t' && LA4_0 <= '\n')||LA4_0=='\r'||LA4_0==' ') ) {s = 4;}
						else if ( ((LA4_0 >= '\u0000' && LA4_0 <= '\b')||(LA4_0 >= '\u000B' && LA4_0 <= '\f')||(LA4_0 >= '\u000E' && LA4_0 <= '\u001F')||(LA4_0 >= '!' && LA4_0 <= '&')||(LA4_0 >= '(' && LA4_0 <= ',')||(LA4_0 >= '.' && LA4_0 <= 'N')||(LA4_0 >= 'P' && LA4_0 <= 'n')||(LA4_0 >= 'p' && LA4_0 <= '\uFFFF')) ) {s = 5;}
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA4_1 = input.LA(1);
						s = -1;
						if ( (LA4_1=='\\') ) {s = 6;}
						else if ( ((LA4_1 >= '\u0000' && LA4_1 <= '\b')||(LA4_1 >= '\u000B' && LA4_1 <= '\f')||(LA4_1 >= '\u000E' && LA4_1 <= '\u001F')||(LA4_1 >= '!' && LA4_1 <= '&')||(LA4_1 >= '(' && LA4_1 <= '[')||(LA4_1 >= ']' && LA4_1 <= '\uFFFF')) ) {s = 7;}
						else if ( (LA4_1=='\'') ) {s = 8;}
						else if ( ((LA4_1 >= '\t' && LA4_1 <= '\n')||LA4_1=='\r'||LA4_1==' ') ) {s = 9;}
						else s = 5;
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA4_11 = input.LA(1);
						s = -1;
						if ( (LA4_11=='\'') ) {s = 8;}
						else if ( (LA4_11=='\\') ) {s = 6;}
						else if ( ((LA4_11 >= '\u0000' && LA4_11 <= '\b')||(LA4_11 >= '\u000B' && LA4_11 <= '\f')||(LA4_11 >= '\u000E' && LA4_11 <= '\u001F')||(LA4_11 >= '!' && LA4_11 <= '&')||(LA4_11 >= '(' && LA4_11 <= '[')||(LA4_11 >= ']' && LA4_11 <= '\uFFFF')) ) {s = 7;}
						else if ( ((LA4_11 >= '\t' && LA4_11 <= '\n')||LA4_11=='\r'||LA4_11==' ') ) {s = 9;}
						else s = 5;
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA4_8 = input.LA(1);
						s = -1;
						if ( ((LA4_8 >= '\u0000' && LA4_8 <= '\b')||(LA4_8 >= '\u000B' && LA4_8 <= '\f')||(LA4_8 >= '\u000E' && LA4_8 <= '\u001F')||(LA4_8 >= '!' && LA4_8 <= '\uFFFF')) ) {s = 5;}
						else s = 9;
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA4_10 = input.LA(1);
						s = -1;
						if ( ((LA4_10 >= '\u0000' && LA4_10 <= '\b')||(LA4_10 >= '\u000B' && LA4_10 <= '\f')||(LA4_10 >= '\u000E' && LA4_10 <= '\u001F')||(LA4_10 >= '!' && LA4_10 <= '\uFFFF')) ) {s = 5;}
						else s = 12;
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA4_7 = input.LA(1);
						s = -1;
						if ( (LA4_7=='\'') ) {s = 8;}
						else if ( (LA4_7=='\\') ) {s = 6;}
						else if ( ((LA4_7 >= '\u0000' && LA4_7 <= '\b')||(LA4_7 >= '\u000B' && LA4_7 <= '\f')||(LA4_7 >= '\u000E' && LA4_7 <= '\u001F')||(LA4_7 >= '!' && LA4_7 <= '&')||(LA4_7 >= '(' && LA4_7 <= '[')||(LA4_7 >= ']' && LA4_7 <= '\uFFFF')) ) {s = 7;}
						else if ( ((LA4_7 >= '\t' && LA4_7 <= '\n')||LA4_7=='\r'||LA4_7==' ') ) {s = 9;}
						else s = 5;
						if ( s>=0 ) return s;
						break;
			}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 4, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
