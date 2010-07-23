package jp.archilogic.documentmanager.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

public class FileUtil {
    public static void safeDelete( String path ) {
        File f = new File( path );
        if ( f.exists() ) {
            f.delete();
        }
    }

    public static byte[] toBytes( String filePath ) {
        List< Byte > temp = Lists.newArrayList();
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream( new FileInputStream( filePath ) );

            int t;
            while ( ( t = bufferedInputStream.read() ) != -1 ) {
                temp.add( ( byte ) t );
            }

            bufferedInputStream.close();
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        byte[] ret = new byte[ temp.size() ];
        for ( int i = 0 , length = ret.length ; i < length ; i++ ) {
            ret[ i ] = temp.get( i );
        }

        return ret;
    }

    public static void toFile( byte[] data , String path ) {
        try {
            FileOutputStream out = new FileOutputStream( path );
            out.write( data );
            out.flush();
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        } catch ( IOException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }
}
