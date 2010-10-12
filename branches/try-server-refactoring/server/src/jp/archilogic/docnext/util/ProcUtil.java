package jp.archilogic.docnext.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger( ProcUtil.class );

    public static String doProc( String command ) {
        return doProc( command , false );
    }

    public static String doProc( String command , boolean ignoreError ) {
        LOGGER.info( "ProcUtil.doProc: " + command );

        try {
            Process process = Runtime.getRuntime().exec( command );

            String ret = readStream( process.getInputStream() );
            String error = readStream( process.getErrorStream() );

            if ( !error.isEmpty() ) {
                if ( ignoreError ) {
                    LOGGER.error( "Error : " + error + "(for command: " + command + ")" );
                } else {
                    throw new RuntimeException( "Error : " + error + "(for command: " + command + ")" );
                }
            }

            process.waitFor();

            return ret.toString();
        } catch ( IOException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        } catch ( InterruptedException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }

    private static String readStream( InputStream stream ) {
        try {
            BufferedReader output = new BufferedReader( new InputStreamReader( stream ) );

            StringBuilder ret = new StringBuilder();
            String line;
            while ( ( line = output.readLine() ) != null ) {
                ret.append( line + "\n" );
            }

            return ret.toString();
        } catch ( IOException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }
}
