// $ANTLR 3.4 org/alfresco/rest/antlr/WhereClause.g 2013-05-24 09:01:14

package org.alfresco.rest.antlr;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class WhereClauseParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "BETWEEN", "COMMA", "EQUALS", "EXISTS", "GREATERTHAN", "GREATERTHANOREQUALS", "IDENTIFIER", "IDENTIFIERDIGIT", "IDENTIFIERLETTER", "IN", "LEFTPAREN", "LESSTHAN", "LESSTHANOREQUALS", "MATCHES", "NEGATION", "OR", "PROPERTYNAME", "PROPERTYVALUE", "RIGHTPAREN", "SINGLEQUOTE", "WS"
    };

    public static final int EOF=-1;
    public static final int AND=4;
    public static final int BETWEEN=5;
    public static final int COMMA=6;
    public static final int EQUALS=7;
    public static final int EXISTS=8;
    public static final int GREATERTHAN=9;
    public static final int GREATERTHANOREQUALS=10;
    public static final int IDENTIFIER=11;
    public static final int IDENTIFIERDIGIT=12;
    public static final int IDENTIFIERLETTER=13;
    public static final int IN=14;
    public static final int LEFTPAREN=15;
    public static final int LESSTHAN=16;
    public static final int LESSTHANOREQUALS=17;
    public static final int MATCHES=18;
    public static final int NEGATION=19;
    public static final int OR=20;
    public static final int PROPERTYNAME=21;
    public static final int PROPERTYVALUE=22;
    public static final int RIGHTPAREN=23;
    public static final int SINGLEQUOTE=24;
    public static final int WS=25;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public WhereClauseParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public WhereClauseParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return WhereClauseParser.tokenNames; }
    public String getGrammarFileName() { return "org/alfresco/rest/antlr/WhereClause.g"; }


       
      // These methods are here to force the parser to error instead of suppressing problems.
    //	  @Override
    //	  public void reportError(RecognitionException e) {
    //      System.out.println("CUSTOM ERROR...\n" + e);
    //      throw new InvalidQueryException(e.getMessage());
    //	  }

        @Override
        protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException
        {
            throw new MismatchedTokenException(ttype, input);
        }
            
        @Override
        public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException
        {
            throw e;
        }
    //    
    //    @Override
    //    public String getErrorMessage(RecognitionException e, String[] tokenNames) 
    //    {
    //      System.out.println("THROW ME...\n" + e);
    //      throw new InvalidQueryException(e.getMessage());
    //    }
    // End of methods here to force the parser to error instead of supressing problems.


    public static class whereclause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "whereclause"
    // org/alfresco/rest/antlr/WhereClause.g:125:1: whereclause : ( WS )? LEFTPAREN ! ( WS )? predicate RIGHTPAREN ! ( WS )? ;
    public final WhereClauseParser.whereclause_return whereclause() throws RecognitionException {
        WhereClauseParser.whereclause_return retval = new WhereClauseParser.whereclause_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token WS1=null;
        Token LEFTPAREN2=null;
        Token WS3=null;
        Token RIGHTPAREN5=null;
        Token WS6=null;
        WhereClauseParser.predicate_return predicate4 =null;


        Object WS1_tree=null;
        Object LEFTPAREN2_tree=null;
        Object WS3_tree=null;
        Object RIGHTPAREN5_tree=null;
        Object WS6_tree=null;

        try {
            // org/alfresco/rest/antlr/WhereClause.g:125:13: ( ( WS )? LEFTPAREN ! ( WS )? predicate RIGHTPAREN ! ( WS )? )
            // org/alfresco/rest/antlr/WhereClause.g:125:15: ( WS )? LEFTPAREN ! ( WS )? predicate RIGHTPAREN ! ( WS )?
            {
            root_0 = (Object)adaptor.nil();


            // org/alfresco/rest/antlr/WhereClause.g:125:15: ( WS )?
            int alt1=2;
            switch ( input.LA(1) ) {
                case WS:
                    {
                    alt1=1;
                    }
                    break;
            }

            switch (alt1) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:125:15: WS
                    {
                    WS1=(Token)match(input,WS,FOLLOW_WS_in_whereclause750); 
                    WS1_tree = 
                    (Object)adaptor.create(WS1)
                    ;
                    adaptor.addChild(root_0, WS1_tree);


                    }
                    break;

            }


            LEFTPAREN2=(Token)match(input,LEFTPAREN,FOLLOW_LEFTPAREN_in_whereclause753); 

            // org/alfresco/rest/antlr/WhereClause.g:125:30: ( WS )?
            int alt2=2;
            switch ( input.LA(1) ) {
                case WS:
                    {
                    alt2=1;
                    }
                    break;
            }

            switch (alt2) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:125:30: WS
                    {
                    WS3=(Token)match(input,WS,FOLLOW_WS_in_whereclause756); 
                    WS3_tree = 
                    (Object)adaptor.create(WS3)
                    ;
                    adaptor.addChild(root_0, WS3_tree);


                    }
                    break;

            }


            pushFollow(FOLLOW_predicate_in_whereclause759);
            predicate4=predicate();

            state._fsp--;

            adaptor.addChild(root_0, predicate4.getTree());

            RIGHTPAREN5=(Token)match(input,RIGHTPAREN,FOLLOW_RIGHTPAREN_in_whereclause761); 

            // org/alfresco/rest/antlr/WhereClause.g:125:56: ( WS )?
            int alt3=2;
            switch ( input.LA(1) ) {
                case WS:
                    {
                    alt3=1;
                    }
                    break;
            }

            switch (alt3) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:125:56: WS
                    {
                    WS6=(Token)match(input,WS,FOLLOW_WS_in_whereclause764); 
                    WS6_tree = 
                    (Object)adaptor.create(WS6)
                    ;
                    adaptor.addChild(root_0, WS6_tree);


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "whereclause"


    public static class predicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "predicate"
    // org/alfresco/rest/antlr/WhereClause.g:126:1: predicate : ( simplepredicate | simplepredicate ( AND simplepredicate )+ -> ^( AND ( simplepredicate )+ ) | simplepredicate ( OR simplepredicate )+ -> ^( OR ( simplepredicate )+ ) );
    public final WhereClauseParser.predicate_return predicate() throws RecognitionException {
        WhereClauseParser.predicate_return retval = new WhereClauseParser.predicate_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token AND9=null;
        Token OR12=null;
        WhereClauseParser.simplepredicate_return simplepredicate7 =null;

        WhereClauseParser.simplepredicate_return simplepredicate8 =null;

        WhereClauseParser.simplepredicate_return simplepredicate10 =null;

        WhereClauseParser.simplepredicate_return simplepredicate11 =null;

        WhereClauseParser.simplepredicate_return simplepredicate13 =null;


        Object AND9_tree=null;
        Object OR12_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_simplepredicate=new RewriteRuleSubtreeStream(adaptor,"rule simplepredicate");
        try {
            // org/alfresco/rest/antlr/WhereClause.g:126:11: ( simplepredicate | simplepredicate ( AND simplepredicate )+ -> ^( AND ( simplepredicate )+ ) | simplepredicate ( OR simplepredicate )+ -> ^( OR ( simplepredicate )+ ) )
            int alt6=3;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:126:13: simplepredicate
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_simplepredicate_in_predicate772);
                    simplepredicate7=simplepredicate();

                    state._fsp--;

                    adaptor.addChild(root_0, simplepredicate7.getTree());

                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:127:13: simplepredicate ( AND simplepredicate )+
                    {
                    pushFollow(FOLLOW_simplepredicate_in_predicate786);
                    simplepredicate8=simplepredicate();

                    state._fsp--;

                    stream_simplepredicate.add(simplepredicate8.getTree());

                    // org/alfresco/rest/antlr/WhereClause.g:127:29: ( AND simplepredicate )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        switch ( input.LA(1) ) {
                        case AND:
                            {
                            alt4=1;
                            }
                            break;

                        }

                        switch (alt4) {
                    	case 1 :
                    	    // org/alfresco/rest/antlr/WhereClause.g:127:30: AND simplepredicate
                    	    {
                    	    AND9=(Token)match(input,AND,FOLLOW_AND_in_predicate789);  
                    	    stream_AND.add(AND9);


                    	    pushFollow(FOLLOW_simplepredicate_in_predicate791);
                    	    simplepredicate10=simplepredicate();

                    	    state._fsp--;

                    	    stream_simplepredicate.add(simplepredicate10.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt4 >= 1 ) break loop4;
                                EarlyExitException eee =
                                    new EarlyExitException(4, input);
                                throw eee;
                        }
                        cnt4++;
                    } while (true);


                    // AST REWRITE
                    // elements: simplepredicate, AND
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 127:52: -> ^( AND ( simplepredicate )+ )
                    {
                        // org/alfresco/rest/antlr/WhereClause.g:127:55: ^( AND ( simplepredicate )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_AND.nextNode()
                        , root_1);

                        if ( !(stream_simplepredicate.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_simplepredicate.hasNext() ) {
                            adaptor.addChild(root_1, stream_simplepredicate.nextTree());

                        }
                        stream_simplepredicate.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }
                    break;
                case 3 :
                    // org/alfresco/rest/antlr/WhereClause.g:128:13: simplepredicate ( OR simplepredicate )+
                    {
                    pushFollow(FOLLOW_simplepredicate_in_predicate816);
                    simplepredicate11=simplepredicate();

                    state._fsp--;

                    stream_simplepredicate.add(simplepredicate11.getTree());

                    // org/alfresco/rest/antlr/WhereClause.g:128:29: ( OR simplepredicate )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        switch ( input.LA(1) ) {
                        case OR:
                            {
                            alt5=1;
                            }
                            break;

                        }

                        switch (alt5) {
                    	case 1 :
                    	    // org/alfresco/rest/antlr/WhereClause.g:128:30: OR simplepredicate
                    	    {
                    	    OR12=(Token)match(input,OR,FOLLOW_OR_in_predicate819);  
                    	    stream_OR.add(OR12);


                    	    pushFollow(FOLLOW_simplepredicate_in_predicate821);
                    	    simplepredicate13=simplepredicate();

                    	    state._fsp--;

                    	    stream_simplepredicate.add(simplepredicate13.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt5 >= 1 ) break loop5;
                                EarlyExitException eee =
                                    new EarlyExitException(5, input);
                                throw eee;
                        }
                        cnt5++;
                    } while (true);


                    // AST REWRITE
                    // elements: OR, simplepredicate
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 128:51: -> ^( OR ( simplepredicate )+ )
                    {
                        // org/alfresco/rest/antlr/WhereClause.g:128:54: ^( OR ( simplepredicate )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_OR.nextNode()
                        , root_1);

                        if ( !(stream_simplepredicate.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_simplepredicate.hasNext() ) {
                            adaptor.addChild(root_1, stream_simplepredicate.nextTree());

                        }
                        stream_simplepredicate.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "predicate"


    public static class simplepredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "simplepredicate"
    // org/alfresco/rest/antlr/WhereClause.g:129:1: simplepredicate : ( allowedpredicates -> allowedpredicates | NEGATION allowedpredicates -> ^( NEGATION allowedpredicates ) );
    public final WhereClauseParser.simplepredicate_return simplepredicate() throws RecognitionException {
        WhereClauseParser.simplepredicate_return retval = new WhereClauseParser.simplepredicate_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NEGATION15=null;
        WhereClauseParser.allowedpredicates_return allowedpredicates14 =null;

        WhereClauseParser.allowedpredicates_return allowedpredicates16 =null;


        Object NEGATION15_tree=null;
        RewriteRuleTokenStream stream_NEGATION=new RewriteRuleTokenStream(adaptor,"token NEGATION");
        RewriteRuleSubtreeStream stream_allowedpredicates=new RewriteRuleSubtreeStream(adaptor,"rule allowedpredicates");
        try {
            // org/alfresco/rest/antlr/WhereClause.g:129:17: ( allowedpredicates -> allowedpredicates | NEGATION allowedpredicates -> ^( NEGATION allowedpredicates ) )
            int alt7=2;
            switch ( input.LA(1) ) {
            case EXISTS:
            case PROPERTYNAME:
                {
                alt7=1;
                }
                break;
            case NEGATION:
                {
                alt7=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }

            switch (alt7) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:129:19: allowedpredicates
                    {
                    pushFollow(FOLLOW_allowedpredicates_in_simplepredicate839);
                    allowedpredicates14=allowedpredicates();

                    state._fsp--;

                    stream_allowedpredicates.add(allowedpredicates14.getTree());

                    // AST REWRITE
                    // elements: allowedpredicates
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 129:37: -> allowedpredicates
                    {
                        adaptor.addChild(root_0, stream_allowedpredicates.nextTree());

                    }


                    retval.tree = root_0;

                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:130:19: NEGATION allowedpredicates
                    {
                    NEGATION15=(Token)match(input,NEGATION,FOLLOW_NEGATION_in_simplepredicate863);  
                    stream_NEGATION.add(NEGATION15);


                    pushFollow(FOLLOW_allowedpredicates_in_simplepredicate865);
                    allowedpredicates16=allowedpredicates();

                    state._fsp--;

                    stream_allowedpredicates.add(allowedpredicates16.getTree());

                    // AST REWRITE
                    // elements: NEGATION, allowedpredicates
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 130:46: -> ^( NEGATION allowedpredicates )
                    {
                        // org/alfresco/rest/antlr/WhereClause.g:130:49: ^( NEGATION allowedpredicates )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_NEGATION.nextNode()
                        , root_1);

                        adaptor.addChild(root_1, stream_allowedpredicates.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "simplepredicate"


    public static class allowedpredicates_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "allowedpredicates"
    // org/alfresco/rest/antlr/WhereClause.g:131:1: allowedpredicates : ( comparisonpredicate | existspredicate | betweenpredicate | inpredicate | matchespredicate );
    public final WhereClauseParser.allowedpredicates_return allowedpredicates() throws RecognitionException {
        WhereClauseParser.allowedpredicates_return retval = new WhereClauseParser.allowedpredicates_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        WhereClauseParser.comparisonpredicate_return comparisonpredicate17 =null;

        WhereClauseParser.existspredicate_return existspredicate18 =null;

        WhereClauseParser.betweenpredicate_return betweenpredicate19 =null;

        WhereClauseParser.inpredicate_return inpredicate20 =null;

        WhereClauseParser.matchespredicate_return matchespredicate21 =null;



        try {
            // org/alfresco/rest/antlr/WhereClause.g:131:19: ( comparisonpredicate | existspredicate | betweenpredicate | inpredicate | matchespredicate )
            int alt8=5;
            switch ( input.LA(1) ) {
            case PROPERTYNAME:
                {
                switch ( input.LA(2) ) {
                case BETWEEN:
                    {
                    alt8=3;
                    }
                    break;
                case IN:
                    {
                    alt8=4;
                    }
                    break;
                case MATCHES:
                    {
                    alt8=5;
                    }
                    break;
                case EQUALS:
                case GREATERTHAN:
                case GREATERTHANOREQUALS:
                case LESSTHAN:
                case LESSTHANOREQUALS:
                    {
                    alt8=1;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;

                }

                }
                break;
            case EXISTS:
                {
                alt8=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }

            switch (alt8) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:131:21: comparisonpredicate
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_comparisonpredicate_in_allowedpredicates880);
                    comparisonpredicate17=comparisonpredicate();

                    state._fsp--;

                    adaptor.addChild(root_0, comparisonpredicate17.getTree());

                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:131:43: existspredicate
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_existspredicate_in_allowedpredicates884);
                    existspredicate18=existspredicate();

                    state._fsp--;

                    adaptor.addChild(root_0, existspredicate18.getTree());

                    }
                    break;
                case 3 :
                    // org/alfresco/rest/antlr/WhereClause.g:131:61: betweenpredicate
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_betweenpredicate_in_allowedpredicates888);
                    betweenpredicate19=betweenpredicate();

                    state._fsp--;

                    adaptor.addChild(root_0, betweenpredicate19.getTree());

                    }
                    break;
                case 4 :
                    // org/alfresco/rest/antlr/WhereClause.g:131:80: inpredicate
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_inpredicate_in_allowedpredicates892);
                    inpredicate20=inpredicate();

                    state._fsp--;

                    adaptor.addChild(root_0, inpredicate20.getTree());

                    }
                    break;
                case 5 :
                    // org/alfresco/rest/antlr/WhereClause.g:131:94: matchespredicate
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_matchespredicate_in_allowedpredicates896);
                    matchespredicate21=matchespredicate();

                    state._fsp--;

                    adaptor.addChild(root_0, matchespredicate21.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "allowedpredicates"


    public static class comparisonpredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "comparisonpredicate"
    // org/alfresco/rest/antlr/WhereClause.g:132:1: comparisonpredicate : PROPERTYNAME comparisonoperator value -> ^( comparisonoperator PROPERTYNAME value ) ;
    public final WhereClauseParser.comparisonpredicate_return comparisonpredicate() throws RecognitionException {
        WhereClauseParser.comparisonpredicate_return retval = new WhereClauseParser.comparisonpredicate_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token PROPERTYNAME22=null;
        WhereClauseParser.comparisonoperator_return comparisonoperator23 =null;

        WhereClauseParser.value_return value24 =null;


        Object PROPERTYNAME22_tree=null;
        RewriteRuleTokenStream stream_PROPERTYNAME=new RewriteRuleTokenStream(adaptor,"token PROPERTYNAME");
        RewriteRuleSubtreeStream stream_comparisonoperator=new RewriteRuleSubtreeStream(adaptor,"rule comparisonoperator");
        RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
        try {
            // org/alfresco/rest/antlr/WhereClause.g:132:20: ( PROPERTYNAME comparisonoperator value -> ^( comparisonoperator PROPERTYNAME value ) )
            // org/alfresco/rest/antlr/WhereClause.g:132:22: PROPERTYNAME comparisonoperator value
            {
            PROPERTYNAME22=(Token)match(input,PROPERTYNAME,FOLLOW_PROPERTYNAME_in_comparisonpredicate902);  
            stream_PROPERTYNAME.add(PROPERTYNAME22);


            pushFollow(FOLLOW_comparisonoperator_in_comparisonpredicate904);
            comparisonoperator23=comparisonoperator();

            state._fsp--;

            stream_comparisonoperator.add(comparisonoperator23.getTree());

            pushFollow(FOLLOW_value_in_comparisonpredicate906);
            value24=value();

            state._fsp--;

            stream_value.add(value24.getTree());

            // AST REWRITE
            // elements: value, PROPERTYNAME, comparisonoperator
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 132:60: -> ^( comparisonoperator PROPERTYNAME value )
            {
                // org/alfresco/rest/antlr/WhereClause.g:132:63: ^( comparisonoperator PROPERTYNAME value )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_comparisonoperator.nextNode(), root_1);

                adaptor.addChild(root_1, 
                stream_PROPERTYNAME.nextNode()
                );

                adaptor.addChild(root_1, stream_value.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "comparisonpredicate"


    public static class comparisonoperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "comparisonoperator"
    // org/alfresco/rest/antlr/WhereClause.g:133:1: comparisonoperator : ( EQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS );
    public final WhereClauseParser.comparisonoperator_return comparisonoperator() throws RecognitionException {
        WhereClauseParser.comparisonoperator_return retval = new WhereClauseParser.comparisonoperator_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set25=null;

        Object set25_tree=null;

        try {
            // org/alfresco/rest/antlr/WhereClause.g:133:19: ( EQUALS | LESSTHAN | GREATERTHAN | LESSTHANOREQUALS | GREATERTHANOREQUALS )
            // org/alfresco/rest/antlr/WhereClause.g:
            {
            root_0 = (Object)adaptor.nil();


            set25=(Token)input.LT(1);

            if ( input.LA(1)==EQUALS||(input.LA(1) >= GREATERTHAN && input.LA(1) <= GREATERTHANOREQUALS)||(input.LA(1) >= LESSTHAN && input.LA(1) <= LESSTHANOREQUALS) ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set25)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "comparisonoperator"


    public static class existspredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "existspredicate"
    // org/alfresco/rest/antlr/WhereClause.g:134:1: existspredicate : EXISTS LEFTPAREN ( WS )? PROPERTYNAME RIGHTPAREN -> ^( EXISTS PROPERTYNAME ) ;
    public final WhereClauseParser.existspredicate_return existspredicate() throws RecognitionException {
        WhereClauseParser.existspredicate_return retval = new WhereClauseParser.existspredicate_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token EXISTS26=null;
        Token LEFTPAREN27=null;
        Token WS28=null;
        Token PROPERTYNAME29=null;
        Token RIGHTPAREN30=null;

        Object EXISTS26_tree=null;
        Object LEFTPAREN27_tree=null;
        Object WS28_tree=null;
        Object PROPERTYNAME29_tree=null;
        Object RIGHTPAREN30_tree=null;
        RewriteRuleTokenStream stream_PROPERTYNAME=new RewriteRuleTokenStream(adaptor,"token PROPERTYNAME");
        RewriteRuleTokenStream stream_LEFTPAREN=new RewriteRuleTokenStream(adaptor,"token LEFTPAREN");
        RewriteRuleTokenStream stream_WS=new RewriteRuleTokenStream(adaptor,"token WS");
        RewriteRuleTokenStream stream_EXISTS=new RewriteRuleTokenStream(adaptor,"token EXISTS");
        RewriteRuleTokenStream stream_RIGHTPAREN=new RewriteRuleTokenStream(adaptor,"token RIGHTPAREN");

        try {
            // org/alfresco/rest/antlr/WhereClause.g:134:16: ( EXISTS LEFTPAREN ( WS )? PROPERTYNAME RIGHTPAREN -> ^( EXISTS PROPERTYNAME ) )
            // org/alfresco/rest/antlr/WhereClause.g:134:18: EXISTS LEFTPAREN ( WS )? PROPERTYNAME RIGHTPAREN
            {
            EXISTS26=(Token)match(input,EXISTS,FOLLOW_EXISTS_in_existspredicate936);  
            stream_EXISTS.add(EXISTS26);


            LEFTPAREN27=(Token)match(input,LEFTPAREN,FOLLOW_LEFTPAREN_in_existspredicate938);  
            stream_LEFTPAREN.add(LEFTPAREN27);


            // org/alfresco/rest/antlr/WhereClause.g:134:35: ( WS )?
            int alt9=2;
            switch ( input.LA(1) ) {
                case WS:
                    {
                    alt9=1;
                    }
                    break;
            }

            switch (alt9) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:134:35: WS
                    {
                    WS28=(Token)match(input,WS,FOLLOW_WS_in_existspredicate940);  
                    stream_WS.add(WS28);


                    }
                    break;

            }


            PROPERTYNAME29=(Token)match(input,PROPERTYNAME,FOLLOW_PROPERTYNAME_in_existspredicate943);  
            stream_PROPERTYNAME.add(PROPERTYNAME29);


            RIGHTPAREN30=(Token)match(input,RIGHTPAREN,FOLLOW_RIGHTPAREN_in_existspredicate945);  
            stream_RIGHTPAREN.add(RIGHTPAREN30);


            // AST REWRITE
            // elements: EXISTS, PROPERTYNAME
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 134:63: -> ^( EXISTS PROPERTYNAME )
            {
                // org/alfresco/rest/antlr/WhereClause.g:134:66: ^( EXISTS PROPERTYNAME )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_EXISTS.nextNode()
                , root_1);

                adaptor.addChild(root_1, 
                stream_PROPERTYNAME.nextNode()
                );

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "existspredicate"


    public static class betweenpredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "betweenpredicate"
    // org/alfresco/rest/antlr/WhereClause.g:135:1: betweenpredicate : PROPERTYNAME BETWEEN LEFTPAREN ( WS )? propertyvaluepair RIGHTPAREN -> ^( BETWEEN PROPERTYNAME propertyvaluepair ) ;
    public final WhereClauseParser.betweenpredicate_return betweenpredicate() throws RecognitionException {
        WhereClauseParser.betweenpredicate_return retval = new WhereClauseParser.betweenpredicate_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token PROPERTYNAME31=null;
        Token BETWEEN32=null;
        Token LEFTPAREN33=null;
        Token WS34=null;
        Token RIGHTPAREN36=null;
        WhereClauseParser.propertyvaluepair_return propertyvaluepair35 =null;


        Object PROPERTYNAME31_tree=null;
        Object BETWEEN32_tree=null;
        Object LEFTPAREN33_tree=null;
        Object WS34_tree=null;
        Object RIGHTPAREN36_tree=null;
        RewriteRuleTokenStream stream_PROPERTYNAME=new RewriteRuleTokenStream(adaptor,"token PROPERTYNAME");
        RewriteRuleTokenStream stream_LEFTPAREN=new RewriteRuleTokenStream(adaptor,"token LEFTPAREN");
        RewriteRuleTokenStream stream_WS=new RewriteRuleTokenStream(adaptor,"token WS");
        RewriteRuleTokenStream stream_RIGHTPAREN=new RewriteRuleTokenStream(adaptor,"token RIGHTPAREN");
        RewriteRuleTokenStream stream_BETWEEN=new RewriteRuleTokenStream(adaptor,"token BETWEEN");
        RewriteRuleSubtreeStream stream_propertyvaluepair=new RewriteRuleSubtreeStream(adaptor,"rule propertyvaluepair");
        try {
            // org/alfresco/rest/antlr/WhereClause.g:135:17: ( PROPERTYNAME BETWEEN LEFTPAREN ( WS )? propertyvaluepair RIGHTPAREN -> ^( BETWEEN PROPERTYNAME propertyvaluepair ) )
            // org/alfresco/rest/antlr/WhereClause.g:135:19: PROPERTYNAME BETWEEN LEFTPAREN ( WS )? propertyvaluepair RIGHTPAREN
            {
            PROPERTYNAME31=(Token)match(input,PROPERTYNAME,FOLLOW_PROPERTYNAME_in_betweenpredicate959);  
            stream_PROPERTYNAME.add(PROPERTYNAME31);


            BETWEEN32=(Token)match(input,BETWEEN,FOLLOW_BETWEEN_in_betweenpredicate961);  
            stream_BETWEEN.add(BETWEEN32);


            LEFTPAREN33=(Token)match(input,LEFTPAREN,FOLLOW_LEFTPAREN_in_betweenpredicate963);  
            stream_LEFTPAREN.add(LEFTPAREN33);


            // org/alfresco/rest/antlr/WhereClause.g:135:50: ( WS )?
            int alt10=2;
            switch ( input.LA(1) ) {
                case WS:
                    {
                    alt10=1;
                    }
                    break;
            }

            switch (alt10) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:135:50: WS
                    {
                    WS34=(Token)match(input,WS,FOLLOW_WS_in_betweenpredicate965);  
                    stream_WS.add(WS34);


                    }
                    break;

            }


            pushFollow(FOLLOW_propertyvaluepair_in_betweenpredicate968);
            propertyvaluepair35=propertyvaluepair();

            state._fsp--;

            stream_propertyvaluepair.add(propertyvaluepair35.getTree());

            RIGHTPAREN36=(Token)match(input,RIGHTPAREN,FOLLOW_RIGHTPAREN_in_betweenpredicate970);  
            stream_RIGHTPAREN.add(RIGHTPAREN36);


            // AST REWRITE
            // elements: propertyvaluepair, PROPERTYNAME, BETWEEN
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 135:83: -> ^( BETWEEN PROPERTYNAME propertyvaluepair )
            {
                // org/alfresco/rest/antlr/WhereClause.g:135:86: ^( BETWEEN PROPERTYNAME propertyvaluepair )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_BETWEEN.nextNode()
                , root_1);

                adaptor.addChild(root_1, 
                stream_PROPERTYNAME.nextNode()
                );

                adaptor.addChild(root_1, stream_propertyvaluepair.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "betweenpredicate"


    public static class inpredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "inpredicate"
    // org/alfresco/rest/antlr/WhereClause.g:136:1: inpredicate : PROPERTYNAME IN LEFTPAREN ( WS )? propertyvaluelist RIGHTPAREN -> ^( IN PROPERTYNAME propertyvaluelist ) ;
    public final WhereClauseParser.inpredicate_return inpredicate() throws RecognitionException {
        WhereClauseParser.inpredicate_return retval = new WhereClauseParser.inpredicate_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token PROPERTYNAME37=null;
        Token IN38=null;
        Token LEFTPAREN39=null;
        Token WS40=null;
        Token RIGHTPAREN42=null;
        WhereClauseParser.propertyvaluelist_return propertyvaluelist41 =null;


        Object PROPERTYNAME37_tree=null;
        Object IN38_tree=null;
        Object LEFTPAREN39_tree=null;
        Object WS40_tree=null;
        Object RIGHTPAREN42_tree=null;
        RewriteRuleTokenStream stream_PROPERTYNAME=new RewriteRuleTokenStream(adaptor,"token PROPERTYNAME");
        RewriteRuleTokenStream stream_LEFTPAREN=new RewriteRuleTokenStream(adaptor,"token LEFTPAREN");
        RewriteRuleTokenStream stream_WS=new RewriteRuleTokenStream(adaptor,"token WS");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_RIGHTPAREN=new RewriteRuleTokenStream(adaptor,"token RIGHTPAREN");
        RewriteRuleSubtreeStream stream_propertyvaluelist=new RewriteRuleSubtreeStream(adaptor,"rule propertyvaluelist");
        try {
            // org/alfresco/rest/antlr/WhereClause.g:136:12: ( PROPERTYNAME IN LEFTPAREN ( WS )? propertyvaluelist RIGHTPAREN -> ^( IN PROPERTYNAME propertyvaluelist ) )
            // org/alfresco/rest/antlr/WhereClause.g:136:14: PROPERTYNAME IN LEFTPAREN ( WS )? propertyvaluelist RIGHTPAREN
            {
            PROPERTYNAME37=(Token)match(input,PROPERTYNAME,FOLLOW_PROPERTYNAME_in_inpredicate986);  
            stream_PROPERTYNAME.add(PROPERTYNAME37);


            IN38=(Token)match(input,IN,FOLLOW_IN_in_inpredicate988);  
            stream_IN.add(IN38);


            LEFTPAREN39=(Token)match(input,LEFTPAREN,FOLLOW_LEFTPAREN_in_inpredicate990);  
            stream_LEFTPAREN.add(LEFTPAREN39);


            // org/alfresco/rest/antlr/WhereClause.g:136:40: ( WS )?
            int alt11=2;
            switch ( input.LA(1) ) {
                case WS:
                    {
                    alt11=1;
                    }
                    break;
            }

            switch (alt11) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:136:40: WS
                    {
                    WS40=(Token)match(input,WS,FOLLOW_WS_in_inpredicate992);  
                    stream_WS.add(WS40);


                    }
                    break;

            }


            pushFollow(FOLLOW_propertyvaluelist_in_inpredicate995);
            propertyvaluelist41=propertyvaluelist();

            state._fsp--;

            stream_propertyvaluelist.add(propertyvaluelist41.getTree());

            RIGHTPAREN42=(Token)match(input,RIGHTPAREN,FOLLOW_RIGHTPAREN_in_inpredicate997);  
            stream_RIGHTPAREN.add(RIGHTPAREN42);


            // AST REWRITE
            // elements: propertyvaluelist, IN, PROPERTYNAME
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 136:73: -> ^( IN PROPERTYNAME propertyvaluelist )
            {
                // org/alfresco/rest/antlr/WhereClause.g:136:76: ^( IN PROPERTYNAME propertyvaluelist )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_IN.nextNode()
                , root_1);

                adaptor.addChild(root_1, 
                stream_PROPERTYNAME.nextNode()
                );

                adaptor.addChild(root_1, stream_propertyvaluelist.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "inpredicate"


    public static class matchespredicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "matchespredicate"
    // org/alfresco/rest/antlr/WhereClause.g:137:1: matchespredicate : PROPERTYNAME MATCHES LEFTPAREN ( WS )? value RIGHTPAREN -> ^( MATCHES PROPERTYNAME value ) ;
    public final WhereClauseParser.matchespredicate_return matchespredicate() throws RecognitionException {
        WhereClauseParser.matchespredicate_return retval = new WhereClauseParser.matchespredicate_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token PROPERTYNAME43=null;
        Token MATCHES44=null;
        Token LEFTPAREN45=null;
        Token WS46=null;
        Token RIGHTPAREN48=null;
        WhereClauseParser.value_return value47 =null;


        Object PROPERTYNAME43_tree=null;
        Object MATCHES44_tree=null;
        Object LEFTPAREN45_tree=null;
        Object WS46_tree=null;
        Object RIGHTPAREN48_tree=null;
        RewriteRuleTokenStream stream_PROPERTYNAME=new RewriteRuleTokenStream(adaptor,"token PROPERTYNAME");
        RewriteRuleTokenStream stream_LEFTPAREN=new RewriteRuleTokenStream(adaptor,"token LEFTPAREN");
        RewriteRuleTokenStream stream_WS=new RewriteRuleTokenStream(adaptor,"token WS");
        RewriteRuleTokenStream stream_MATCHES=new RewriteRuleTokenStream(adaptor,"token MATCHES");
        RewriteRuleTokenStream stream_RIGHTPAREN=new RewriteRuleTokenStream(adaptor,"token RIGHTPAREN");
        RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
        try {
            // org/alfresco/rest/antlr/WhereClause.g:137:17: ( PROPERTYNAME MATCHES LEFTPAREN ( WS )? value RIGHTPAREN -> ^( MATCHES PROPERTYNAME value ) )
            // org/alfresco/rest/antlr/WhereClause.g:137:19: PROPERTYNAME MATCHES LEFTPAREN ( WS )? value RIGHTPAREN
            {
            PROPERTYNAME43=(Token)match(input,PROPERTYNAME,FOLLOW_PROPERTYNAME_in_matchespredicate1013);  
            stream_PROPERTYNAME.add(PROPERTYNAME43);


            MATCHES44=(Token)match(input,MATCHES,FOLLOW_MATCHES_in_matchespredicate1015);  
            stream_MATCHES.add(MATCHES44);


            LEFTPAREN45=(Token)match(input,LEFTPAREN,FOLLOW_LEFTPAREN_in_matchespredicate1017);  
            stream_LEFTPAREN.add(LEFTPAREN45);


            // org/alfresco/rest/antlr/WhereClause.g:137:50: ( WS )?
            int alt12=2;
            switch ( input.LA(1) ) {
                case WS:
                    {
                    alt12=1;
                    }
                    break;
            }

            switch (alt12) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:137:50: WS
                    {
                    WS46=(Token)match(input,WS,FOLLOW_WS_in_matchespredicate1019);  
                    stream_WS.add(WS46);


                    }
                    break;

            }


            pushFollow(FOLLOW_value_in_matchespredicate1022);
            value47=value();

            state._fsp--;

            stream_value.add(value47.getTree());

            RIGHTPAREN48=(Token)match(input,RIGHTPAREN,FOLLOW_RIGHTPAREN_in_matchespredicate1024);  
            stream_RIGHTPAREN.add(RIGHTPAREN48);


            // AST REWRITE
            // elements: PROPERTYNAME, MATCHES, value
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 137:71: -> ^( MATCHES PROPERTYNAME value )
            {
                // org/alfresco/rest/antlr/WhereClause.g:137:74: ^( MATCHES PROPERTYNAME value )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_MATCHES.nextNode()
                , root_1);

                adaptor.addChild(root_1, 
                stream_PROPERTYNAME.nextNode()
                );

                adaptor.addChild(root_1, stream_value.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "matchespredicate"


    public static class propertyvaluepair_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "propertyvaluepair"
    // org/alfresco/rest/antlr/WhereClause.g:138:1: propertyvaluepair : value COMMA value -> ( value )+ ;
    public final WhereClauseParser.propertyvaluepair_return propertyvaluepair() throws RecognitionException {
        WhereClauseParser.propertyvaluepair_return retval = new WhereClauseParser.propertyvaluepair_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token COMMA50=null;
        WhereClauseParser.value_return value49 =null;

        WhereClauseParser.value_return value51 =null;


        Object COMMA50_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
        try {
            // org/alfresco/rest/antlr/WhereClause.g:138:18: ( value COMMA value -> ( value )+ )
            // org/alfresco/rest/antlr/WhereClause.g:138:20: value COMMA value
            {
            pushFollow(FOLLOW_value_in_propertyvaluepair1040);
            value49=value();

            state._fsp--;

            stream_value.add(value49.getTree());

            COMMA50=(Token)match(input,COMMA,FOLLOW_COMMA_in_propertyvaluepair1042);  
            stream_COMMA.add(COMMA50);


            pushFollow(FOLLOW_value_in_propertyvaluepair1044);
            value51=value();

            state._fsp--;

            stream_value.add(value51.getTree());

            // AST REWRITE
            // elements: value
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 138:38: -> ( value )+
            {
                if ( !(stream_value.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_value.hasNext() ) {
                    adaptor.addChild(root_0, stream_value.nextTree());

                }
                stream_value.reset();

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "propertyvaluepair"


    public static class propertyvaluelist_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "propertyvaluelist"
    // org/alfresco/rest/antlr/WhereClause.g:139:1: propertyvaluelist : value ( COMMA value )* -> ( value )+ ;
    public final WhereClauseParser.propertyvaluelist_return propertyvaluelist() throws RecognitionException {
        WhereClauseParser.propertyvaluelist_return retval = new WhereClauseParser.propertyvaluelist_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token COMMA53=null;
        WhereClauseParser.value_return value52 =null;

        WhereClauseParser.value_return value54 =null;


        Object COMMA53_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
        try {
            // org/alfresco/rest/antlr/WhereClause.g:139:18: ( value ( COMMA value )* -> ( value )+ )
            // org/alfresco/rest/antlr/WhereClause.g:139:20: value ( COMMA value )*
            {
            pushFollow(FOLLOW_value_in_propertyvaluelist1055);
            value52=value();

            state._fsp--;

            stream_value.add(value52.getTree());

            // org/alfresco/rest/antlr/WhereClause.g:139:26: ( COMMA value )*
            loop13:
            do {
                int alt13=2;
                switch ( input.LA(1) ) {
                case COMMA:
                    {
                    alt13=1;
                    }
                    break;

                }

                switch (alt13) {
            	case 1 :
            	    // org/alfresco/rest/antlr/WhereClause.g:139:27: COMMA value
            	    {
            	    COMMA53=(Token)match(input,COMMA,FOLLOW_COMMA_in_propertyvaluelist1058);  
            	    stream_COMMA.add(COMMA53);


            	    pushFollow(FOLLOW_value_in_propertyvaluelist1060);
            	    value54=value();

            	    state._fsp--;

            	    stream_value.add(value54.getTree());

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            // AST REWRITE
            // elements: value
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 139:41: -> ( value )+
            {
                if ( !(stream_value.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_value.hasNext() ) {
                    adaptor.addChild(root_0, stream_value.nextTree());

                }
                stream_value.reset();

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "propertyvaluelist"


    public static class value_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "value"
    // org/alfresco/rest/antlr/WhereClause.g:140:1: value : (a= PROPERTYVALUE -> ^( PROPERTYVALUE[$a] ) |b= PROPERTYNAME -> ^( PROPERTYVALUE[$b] ) );
    public final WhereClauseParser.value_return value() throws RecognitionException {
        WhereClauseParser.value_return retval = new WhereClauseParser.value_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token a=null;
        Token b=null;

        Object a_tree=null;
        Object b_tree=null;
        RewriteRuleTokenStream stream_PROPERTYNAME=new RewriteRuleTokenStream(adaptor,"token PROPERTYNAME");
        RewriteRuleTokenStream stream_PROPERTYVALUE=new RewriteRuleTokenStream(adaptor,"token PROPERTYVALUE");

        try {
            // org/alfresco/rest/antlr/WhereClause.g:140:6: (a= PROPERTYVALUE -> ^( PROPERTYVALUE[$a] ) |b= PROPERTYNAME -> ^( PROPERTYVALUE[$b] ) )
            int alt14=2;
            switch ( input.LA(1) ) {
            case PROPERTYVALUE:
                {
                alt14=1;
                }
                break;
            case PROPERTYNAME:
                {
                alt14=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }

            switch (alt14) {
                case 1 :
                    // org/alfresco/rest/antlr/WhereClause.g:140:8: a= PROPERTYVALUE
                    {
                    a=(Token)match(input,PROPERTYVALUE,FOLLOW_PROPERTYVALUE_in_value1075);  
                    stream_PROPERTYVALUE.add(a);


                    // AST REWRITE
                    // elements: PROPERTYVALUE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 140:24: -> ^( PROPERTYVALUE[$a] )
                    {
                        // org/alfresco/rest/antlr/WhereClause.g:140:27: ^( PROPERTYVALUE[$a] )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(PROPERTYVALUE, a)
                        , root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }
                    break;
                case 2 :
                    // org/alfresco/rest/antlr/WhereClause.g:141:9: b= PROPERTYNAME
                    {
                    b=(Token)match(input,PROPERTYNAME,FOLLOW_PROPERTYNAME_in_value1095);  
                    stream_PROPERTYNAME.add(b);


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 141:24: -> ^( PROPERTYVALUE[$b] )
                    {
                        // org/alfresco/rest/antlr/WhereClause.g:141:27: ^( PROPERTYVALUE[$b] )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(PROPERTYVALUE, b)
                        , root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "value"


    public static class selectClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "selectClause"
    // org/alfresco/rest/antlr/WhereClause.g:142:1: selectClause : PROPERTYNAME ( COMMA PROPERTYNAME )* -> ( PROPERTYNAME )+ ;
    public final WhereClauseParser.selectClause_return selectClause() throws RecognitionException {
        WhereClauseParser.selectClause_return retval = new WhereClauseParser.selectClause_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token PROPERTYNAME55=null;
        Token COMMA56=null;
        Token PROPERTYNAME57=null;

        Object PROPERTYNAME55_tree=null;
        Object COMMA56_tree=null;
        Object PROPERTYNAME57_tree=null;
        RewriteRuleTokenStream stream_PROPERTYNAME=new RewriteRuleTokenStream(adaptor,"token PROPERTYNAME");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // org/alfresco/rest/antlr/WhereClause.g:142:13: ( PROPERTYNAME ( COMMA PROPERTYNAME )* -> ( PROPERTYNAME )+ )
            // org/alfresco/rest/antlr/WhereClause.g:142:16: PROPERTYNAME ( COMMA PROPERTYNAME )*
            {
            PROPERTYNAME55=(Token)match(input,PROPERTYNAME,FOLLOW_PROPERTYNAME_in_selectClause1111);  
            stream_PROPERTYNAME.add(PROPERTYNAME55);


            // org/alfresco/rest/antlr/WhereClause.g:142:29: ( COMMA PROPERTYNAME )*
            loop15:
            do {
                int alt15=2;
                switch ( input.LA(1) ) {
                case COMMA:
                    {
                    alt15=1;
                    }
                    break;

                }

                switch (alt15) {
            	case 1 :
            	    // org/alfresco/rest/antlr/WhereClause.g:142:30: COMMA PROPERTYNAME
            	    {
            	    COMMA56=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectClause1114);  
            	    stream_COMMA.add(COMMA56);


            	    PROPERTYNAME57=(Token)match(input,PROPERTYNAME,FOLLOW_PROPERTYNAME_in_selectClause1116);  
            	    stream_PROPERTYNAME.add(PROPERTYNAME57);


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            // AST REWRITE
            // elements: PROPERTYNAME
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 142:51: -> ( PROPERTYNAME )+
            {
                if ( !(stream_PROPERTYNAME.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_PROPERTYNAME.hasNext() ) {
                    adaptor.addChild(root_0, 
                    stream_PROPERTYNAME.nextNode()
                    );

                }
                stream_PROPERTYNAME.reset();

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "selectClause"

    // Delegated rules


    protected DFA6 dfa6 = new DFA6(this);
    static final String DFA6_eotS =
        "\107\uffff";
    static final String DFA6_eofS =
        "\107\uffff";
    static final String DFA6_minS =
        "\1\10\1\5\1\17\1\10\3\17\2\25\1\5\1\17\3\25\2\4\1\25\1\27\3\17\3"+
        "\25\2\6\1\25\2\6\1\25\2\27\3\uffff\1\4\3\25\2\4\1\25\1\27\2\25\2"+
        "\4\1\25\2\6\1\25\2\6\1\25\2\27\1\4\2\27\2\6\2\25\3\4\2\27\2\6\1"+
        "\4";
    static final String DFA6_maxS =
        "\1\25\1\22\1\17\1\25\3\17\1\26\1\31\1\22\1\17\3\31\2\27\1\25\1\27"+
        "\3\17\1\26\1\31\1\26\2\6\1\26\2\27\1\26\2\27\3\uffff\1\27\3\31\2"+
        "\27\1\25\1\27\2\26\2\27\1\26\2\6\1\26\2\27\1\26\7\27\2\26\10\27";
    static final String DFA6_acceptS =
        "\40\uffff\1\1\1\2\1\3\44\uffff";
    static final String DFA6_specialS =
        "\107\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\2\12\uffff\1\3\1\uffff\1\1",
            "\1\4\1\uffff\1\7\1\uffff\2\7\3\uffff\1\5\1\uffff\2\7\1\6",
            "\1\10",
            "\1\12\14\uffff\1\11",
            "\1\13",
            "\1\14",
            "\1\15",
            "\1\17\1\16",
            "\1\21\3\uffff\1\20",
            "\1\22\1\uffff\1\25\1\uffff\2\25\3\uffff\1\23\1\uffff\2\25\1"+
            "\24",
            "\1\26",
            "\1\31\1\30\2\uffff\1\27",
            "\1\34\1\33\2\uffff\1\32",
            "\1\37\1\36\2\uffff\1\35",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\21",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\50\1\47",
            "\1\52\3\uffff\1\51",
            "\1\31\1\30",
            "\1\53",
            "\1\53",
            "\1\34\1\33",
            "\1\54\20\uffff\1\55",
            "\1\54\20\uffff\1\55",
            "\1\37\1\36",
            "\1\56",
            "\1\56",
            "",
            "",
            "",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\61\1\60\2\uffff\1\57",
            "\1\64\1\63\2\uffff\1\62",
            "\1\67\1\66\2\uffff\1\65",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\52",
            "\1\70",
            "\1\72\1\71",
            "\1\74\1\73",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\61\1\60",
            "\1\75",
            "\1\75",
            "\1\64\1\63",
            "\1\76\20\uffff\1\77",
            "\1\76\20\uffff\1\77",
            "\1\67\1\66",
            "\1\100",
            "\1\100",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\101",
            "\1\101",
            "\1\54\20\uffff\1\55",
            "\1\54\20\uffff\1\55",
            "\1\103\1\102",
            "\1\105\1\104",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\41\17\uffff\1\42\2\uffff\1\40",
            "\1\106",
            "\1\106",
            "\1\76\20\uffff\1\77",
            "\1\76\20\uffff\1\77",
            "\1\41\17\uffff\1\42\2\uffff\1\40"
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "126:1: predicate : ( simplepredicate | simplepredicate ( AND simplepredicate )+ -> ^( AND ( simplepredicate )+ ) | simplepredicate ( OR simplepredicate )+ -> ^( OR ( simplepredicate )+ ) );";
        }
    }
 

    public static final BitSet FOLLOW_WS_in_whereclause750 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LEFTPAREN_in_whereclause753 = new BitSet(new long[]{0x0000000002280100L});
    public static final BitSet FOLLOW_WS_in_whereclause756 = new BitSet(new long[]{0x0000000000280100L});
    public static final BitSet FOLLOW_predicate_in_whereclause759 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_RIGHTPAREN_in_whereclause761 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_WS_in_whereclause764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simplepredicate_in_predicate772 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simplepredicate_in_predicate786 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_AND_in_predicate789 = new BitSet(new long[]{0x0000000000280100L});
    public static final BitSet FOLLOW_simplepredicate_in_predicate791 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_simplepredicate_in_predicate816 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_OR_in_predicate819 = new BitSet(new long[]{0x0000000000280100L});
    public static final BitSet FOLLOW_simplepredicate_in_predicate821 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_allowedpredicates_in_simplepredicate839 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEGATION_in_simplepredicate863 = new BitSet(new long[]{0x0000000000200100L});
    public static final BitSet FOLLOW_allowedpredicates_in_simplepredicate865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparisonpredicate_in_allowedpredicates880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_existspredicate_in_allowedpredicates884 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_betweenpredicate_in_allowedpredicates888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inpredicate_in_allowedpredicates892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_matchespredicate_in_allowedpredicates896 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROPERTYNAME_in_comparisonpredicate902 = new BitSet(new long[]{0x0000000000030680L});
    public static final BitSet FOLLOW_comparisonoperator_in_comparisonpredicate904 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_value_in_comparisonpredicate906 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXISTS_in_existspredicate936 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LEFTPAREN_in_existspredicate938 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_WS_in_existspredicate940 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_PROPERTYNAME_in_existspredicate943 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_RIGHTPAREN_in_existspredicate945 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROPERTYNAME_in_betweenpredicate959 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_BETWEEN_in_betweenpredicate961 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LEFTPAREN_in_betweenpredicate963 = new BitSet(new long[]{0x0000000002600000L});
    public static final BitSet FOLLOW_WS_in_betweenpredicate965 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_propertyvaluepair_in_betweenpredicate968 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_RIGHTPAREN_in_betweenpredicate970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROPERTYNAME_in_inpredicate986 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_IN_in_inpredicate988 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LEFTPAREN_in_inpredicate990 = new BitSet(new long[]{0x0000000002600000L});
    public static final BitSet FOLLOW_WS_in_inpredicate992 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_propertyvaluelist_in_inpredicate995 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_RIGHTPAREN_in_inpredicate997 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROPERTYNAME_in_matchespredicate1013 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_MATCHES_in_matchespredicate1015 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LEFTPAREN_in_matchespredicate1017 = new BitSet(new long[]{0x0000000002600000L});
    public static final BitSet FOLLOW_WS_in_matchespredicate1019 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_value_in_matchespredicate1022 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_RIGHTPAREN_in_matchespredicate1024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_propertyvaluepair1040 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_propertyvaluepair1042 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_value_in_propertyvaluepair1044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_propertyvaluelist1055 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_COMMA_in_propertyvaluelist1058 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_value_in_propertyvaluelist1060 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_PROPERTYVALUE_in_value1075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROPERTYNAME_in_value1095 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROPERTYNAME_in_selectClause1111 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_COMMA_in_selectClause1114 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_PROPERTYNAME_in_selectClause1116 = new BitSet(new long[]{0x0000000000000042L});

}