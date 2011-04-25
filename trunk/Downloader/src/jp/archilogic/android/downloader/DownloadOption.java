package jp.archilogic.android.downloader;

import java.io.Serializable;

/**
 * Option class to specify Downloader behavior. Use {@link Builder} to construct.
 */
@SuppressWarnings( "serial" )
public class DownloadOption implements Serializable {
    /**
     * Builder for DownloadOption
     */
    public static class Builder {
        DownloadOption _instance = new DownloadOption();

        /**
         * Abort on error or not. If false, Downloader just ignore the error target, and try next.
         */
        public Builder abortOnError( final boolean abortOnError ) {
            _instance._abortOnError = abortOnError;

            return this;
        }

        /**
         * Return configured DownloadOption instance
         */
        public DownloadOption build() {
            return _instance;
        }

        /**
         * Enable resume or not. This is prior params than abortOnError
         */
        public Builder canResume( final boolean canResume ) {
            _instance._canResume = canResume;

            return this;
        }

        /**
         * Message for Notification on downloading
         */
        public Builder onGoingMessage( final CharSequence message ) {
            _instance._onGoingMessage = message;

            return this;
        }

        /**
         * Message for Notification on download suspended. This should have the message like 'Tap to Resume'
         */
        public Builder suspendMessage( final CharSequence message ) {
            _instance._suspendMessage = message;

            return this;
        }

        /**
         * Ticker for status-bar
         */
        public Builder ticker( final CharSequence ticker ) {
            _instance._ticker = ticker;

            return this;
        }
    }

    private boolean _canResume = false;
    private boolean _abortOnError = true;
    private CharSequence _ticker = "Download Started";
    private CharSequence _onGoingMessage = "Downloading...";
    private CharSequence _suspendMessage = "Suspended (Tap to Resume)";

    public boolean abortOnError() {
        return _abortOnError;
    }

    public boolean canResume() {
        return _canResume;
    }

    public CharSequence getOnGoingMessage() {
        return _onGoingMessage;
    }

    public CharSequence getSuspendMessage() {
        return _suspendMessage;
    }

    public CharSequence getTicker() {
        return _ticker;
    }
}
