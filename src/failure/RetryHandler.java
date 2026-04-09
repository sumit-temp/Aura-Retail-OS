package failure;

public class RetryHandler extends FailureHandler {
    private int retryCount = 0;
    private final int MAX_RETRIES = 3;

    @Override
    public void handleFailure(String failureContext) {
        retryCount++;
        System.out.println("[RetryHandler] Attempt " + retryCount + " to resolve failure: " + failureContext);
        
        if (retryCount >= MAX_RETRIES && nextHandler != null) {
            System.out.println("[RetryHandler] Max retries reached. Passing to next handler.");
            nextHandler.handleFailure(failureContext);
            retryCount = 0; // reset for next use
        }
    }
}
