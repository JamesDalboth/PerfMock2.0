/*
 * Created on Dec 17, 2003
 */
package atest.jmock.builder;

import junit.framework.AssertionFailedError;

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.jmock.dynamic.DynamicMockError;

public class ErrorMessagesAcceptanceTest extends MockObjectTestCase {
    private static final String MOCK_NAME = "MOCK_NAME";
    
    public void testUnexpectedCallAlsoShowsExpectedCalls() {
        String arg1 = "arg1";
        String notArg1 = "not "+arg1;
        Object arg2 = new Object();
        Object notArg2 = new Object();
        Mock mock = new Mock(Types.WithTwoMethods.class, MOCK_NAME);

        mock.expect(once()).method("twoArgsReturnsInt").with(ANYTHING,ANYTHING)
            .will(returnValue(1));
        mock.expect(once()).method("twoArgsReturnsInt").with(eq(arg1),same(arg2))
            .will(returnValue(1));
        
        try {
            ((Types.WithTwoMethods)mock.proxy()).twoArgsReturnInt(notArg1, notArg2);
        } catch (DynamicMockError error) {
            String errorMessage = error.getMessage();
            
            String causeOfError = "no match found";
            String expectedMethod1 = 
                "expected once, Method = twoArgsReturnsInt, (<any value>, <any value>), returns <1>";
            String expectedMethod2 =  
                "expected once, Method = twoArgsReturnsInt, (<= "+arg1+">, <== <"+arg2+">>), returns <1>";
            
            assertStringContains( "should contain mock name", 
                                  errorMessage, MOCK_NAME );
            
            assertStringContains( "should report cause of error",
                                   errorMessage, causeOfError );
            
            assertSubstringOrder( "mock name should appear before cause of error",
                                  errorMessage, MOCK_NAME, causeOfError );
            
            assertStringContains( "should report method that caused error",
                errorMessage,
                "twoArgsReturnInt(<"+notArg1+">, <" + notArg2 + ">)" );
            
            assertStringContains( "should report acceptable methods (#1)",
                                  errorMessage, expectedMethod1 ); 
            
            assertStringContains( "should report acceptable methods (#2)",
                                  errorMessage, expectedMethod2 );
            
            assertSubstringOrder( "should report acceptable methods in search order",
                errorMessage, expectedMethod1, expectedMethod2 );
            
            return;
        }
        
        fail("expected DynamicMockError");
    }
    
    public void testShowsNoExpectationsStringWhenNoExpectationsSet() {
        Mock mock = new Mock(Types.WithTwoMethods.class);
        try {
            ((Types.WithTwoMethods)mock.proxy()).twoArgsReturnInt("arg1", "arg2");
        } catch (DynamicMockError error) {
            String errorMessage = error.getMessage();
            
            assertStringContains( "should report no expectations have been set",
                errorMessage, "No expectations set" );
            return;
        }
        
        fail("expected DynamicMockError");
    }
    
    //TODO: what should be displayed when verify fails?
    public void XtestShowPossibleMethodsWhenVerifyFails() {
        Mock mock = new Mock(Types.WithTwoMethods.class,MOCK_NAME);
        Object a2 = new Object();
        Object b2 = new Object();
        
        mock.expect(once()).method("twoArgsReturnInt").with(eq("a1"),same(a2))
            .will(returnValue(1));
        mock.expect(once()).method("twoArgsReturnInt").with(eq("b1"),same(b2))
            .will(returnValue(2));
        mock.expect(atLeastOnce()).method("noArgsReturnsNothing").noParams();
        
        ((Types.WithTwoMethods)mock.proxy()).twoArgsReturnInt("b1",b2);
        
        try {
            mock.verify();
        }
        catch( AssertionFailedError error ) {
            String errorMessage = error.getMessage();
            String causeOfError = "not all expected methods were invoked";
            String expectedMethod1 =
                "twoArgsReturnsInt, (<= a1>, <== <"+a2+">>), expected once, returns <1>";
            String expectedMethod2 = 
                "noArgsReturnsNothing, (no arguments), expected at least once, returns(<void>)";
            String calledMethod =
                "twoArgsReturnsInt, (<= b1>, <== <"+b2+">>), expected once and has been invoked, returns <2>";
            
            assertStringContains( "should contain mock name", 
                                  errorMessage, MOCK_NAME );
            
            assertStringContains( "should report cause of error",
                                   errorMessage, causeOfError );
            
            assertSubstringOrder( "mock name should appear before cause of error",
                                  errorMessage, MOCK_NAME, causeOfError );
            
            assertStringContains( "should report uncalled methods (#1)",
                errorMessage, expectedMethod1 );
            
            assertStringContains( "should report uncalled methods (#2)",
                errorMessage, expectedMethod2 );
            
            assertStringContains( "should list include methods in error message",
                errorMessage, calledMethod );
            
            return;
        }
        
        fail("expected AssertionFailedError");
    }
    
    public static void assertStringContains( String message, String string, String substring ) {
        assertTrue( message + ": expected \"" + string + "\" to contain \"" + substring + "\"",
                    string.indexOf(substring) >= 0 );
    }
    
    public static void assertSubstringOrder( String message, String string, 
											 String earlierSubstring, String laterSubstring )
    {
        assertStringContains( message, string, earlierSubstring );
        assertStringContains( message, string, laterSubstring );
        
        int earlierIndex = string.indexOf(earlierSubstring);
        int laterIndex = string.indexOf(laterSubstring);
        
        assertTrue( message+": expected \""+earlierSubstring+"\" "+
                        "to appear before \"" + laterSubstring+"\" in \""+string+"\"",
                    earlierIndex < laterIndex );
                    
    }
}
