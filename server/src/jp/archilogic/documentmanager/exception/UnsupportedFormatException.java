package jp.archilogic.documentmanager.exception;

import flex.messaging.MessageException;

@SuppressWarnings( "serial" )
public class UnsupportedFormatException extends MessageException {
    public UnsupportedFormatException() {
        setCode( "UnsupportedFormatException" );
    }
}
