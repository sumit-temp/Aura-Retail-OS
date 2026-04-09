package failure;

public class TechnicianAlertHandler extends FailureHandler {
    @Override
    public void handleFailure(String failureContext) {
        System.out.println("[TechnicianAlertHandler] CRITICAL: Sending alert to city technicians for: " + failureContext);
    }
}
