package jp.archilogic.docnext.android.task;

public interface Receiver< T , E > {
    void error( E error );

    void receive( T result );
}
