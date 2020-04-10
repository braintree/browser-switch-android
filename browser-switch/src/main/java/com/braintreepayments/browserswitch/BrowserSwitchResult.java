package com.braintreepayments.browserswitch;

public abstract class BrowserSwitchResult {
    private BrowserSwitchResult() {}

    public static BrowserSwitchResult ok() {
        return new OkBrowserSwitchResult();
    }

    public static BrowserSwitchResult cancelled() {
        return new CanceledBrowserSwitchResult();
    }

    public static BrowserSwitchResult error(String message) {
        return new ErrorBrowserSwitchResult(message);
    }

    @Override
    public String toString() {
        return getStatus().name() + " " + getErrorMessage();
    }

    public abstract String getErrorMessage();
    public abstract Status getStatus();

    public enum Status {
        OK,
        CANCELED,
        ERROR;
    }

    private static class ErrorBrowserSwitchResult extends BrowserSwitchResult {
        private String mErrorMessage;

        private ErrorBrowserSwitchResult(String message) {
            mErrorMessage = message;
        }

        public String getErrorMessage() {
            return mErrorMessage;
        }

        public Status getStatus() {
            return Status.ERROR;
        }
    }

    private static class OkBrowserSwitchResult extends BrowserSwitchResult {
        private OkBrowserSwitchResult() {}

        public String getErrorMessage() {
            return null;
        }

        public Status getStatus() {
            return Status.OK;
        }
    }

    private static class CanceledBrowserSwitchResult extends BrowserSwitchResult {
        private CanceledBrowserSwitchResult() {}

        public String getErrorMessage() {
            return null;
        }

        public Status getStatus() {
            return Status.CANCELED;
        }
    }
}