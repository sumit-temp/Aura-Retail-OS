package failure;

public abstract class FailureHandler {
    protected FailureHandler nextHandler;

    public void setNextHandler(FailureHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public abstract void handleFailure(String failureContext);
}
