package utils;

import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit {@link Rule} for running tests on the JavaFX thread and performing
 * JavaFX initialisation.  To include in your test case, add the following code:
 * 
 * <pre>
 * {@literal @}Rule
 * public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
 * </pre>
 * 
 * @author Andy Till
 * 
 */
public class JavaFXThreadingRule implements TestRule {
    
    /**
     * Flag for setting up the JavaFX, we only need to do this once for all tests.
     */
    private static boolean jfxIsSetup;

    @Override
    public Statement apply(Statement statement, Description description) {
        
        return new OnJFXThreadStatement(statement);
    }

    private static class OnJFXThreadStatement extends Statement {
        
        private final Statement statement;

        public OnJFXThreadStatement(Statement aStatement) {
            statement = aStatement;
        }

        private Throwable rethrownException = null;
        
        @Override
        public void evaluate() throws Throwable {
            
            if(!jfxIsSetup) {
                setupJavaFX();
                
                jfxIsSetup = true;
            }
            
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        statement.evaluate();
                    } catch (Throwable e) {
                        rethrownException = e;
                    }
                    countDownLatch.countDown();
                }});
            
            countDownLatch.await();
            
            // if an exception was thrown by the statement during evaluation,
            // then re-throw it to fail the test
            if(rethrownException != null) {
                throw rethrownException;
            }
        }

        protected void setupJavaFX() throws InterruptedException {
            
            long timeMillis = System.currentTimeMillis();
            
            final CountDownLatch latch = new CountDownLatch(1);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // initializes JavaFX environment
                    new JFXPanel(); 
                    
                    latch.countDown();
                }
            });
            
            System.out.println("javafx initialising...");
            latch.await();
            System.out.println("javafx is initialised in " + (System.currentTimeMillis() - timeMillis) + "ms");
        }
        
    }
}