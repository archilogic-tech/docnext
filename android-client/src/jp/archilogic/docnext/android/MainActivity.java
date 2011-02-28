package jp.archilogic.docnext.android;

import java.io.File;

import jp.archilogic.docnext.android.task.GetFontTask;
import jp.archilogic.docnext.android.task.GetImagePageTask;
import jp.archilogic.docnext.android.task.GetImagePageThumbnailTask;
import jp.archilogic.docnext.android.task.Receiver;
import jp.archilogic.docnext.android.util.StorageUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {
    private static final long DUMMY_ID = 123;
    private static final int N_PAGE = 227;

    private void fetchFont() {
        final ProgressDialog progress = ProgressDialog.show( this , "" , "Loading..." );

        new GetFontTask( this , new Receiver< Void , Void >() {
            @Override
            public void error( final Void error ) {
                progress.dismiss();
            }

            @Override
            public void receive( final Void result ) {
                progress.dismiss();

                startActivity( new Intent( MainActivity.this , TextViewerActivity.class ) );
            }
        } , "default" ).execute();
    }

    private void fetchImagePage() {
        final ProgressDialog progress = new ProgressDialog( this );
        progress.setMessage( "Loading..." );
        progress.setCancelable( false );
        progress.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
        progress.setMax( N_PAGE * 2 );

        progress.show();

        fetchImagePage( 0 , progress );
    }

    private void fetchImagePage( final int page , final ProgressDialog progress ) {
        if ( page < N_PAGE ) {
            progress.setProgress( page );

            new GetImagePageTask( this , new Receiver< Void , Void >() {
                @Override
                public void error( final Void error ) {
                    progress.dismiss();
                }

                @Override
                public void receive( final Void result ) {
                    fetchImagePage( page + 1 , progress );
                }
            } , DUMMY_ID , page ).execute();
        } else {
            fetchImagePageThumbnail( 0 , progress );
        }
    }

    private void fetchImagePageThumbnail( final int page , final ProgressDialog progress ) {
        if ( page < N_PAGE ) {
            progress.setProgress( page + N_PAGE );

            new GetImagePageThumbnailTask( this , new Receiver< Void , Void >() {
                @Override
                public void error( final Void error ) {
                    progress.dismiss();
                }

                @Override
                public void receive( final Void result ) {
                    fetchImagePageThumbnail( page + 1 , progress );
                }
            } , DUMMY_ID , page ).execute();
        } else {
            progress.dismiss();

            startActivity( new Intent( this , ImageViewerActivity.class ) );
        }
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        findViewById( R.id.image ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                if ( new File( StorageUtil.getImageThumbnailPath( DUMMY_ID , N_PAGE - 1 ) ).exists() ) {
                    startActivity( new Intent( MainActivity.this , ImageViewerActivity.class ) );

                    return;
                }

                StorageUtil.ensureImageDir( DUMMY_ID );

                fetchImagePage();
            }
        } );
        findViewById( R.id.text ).setOnClickListener( new OnClickListener() {
            @Override
            public void onClick( final View v ) {
                if ( new File( StorageUtil.getFontPath( "default" ) ).exists() ) {
                    startActivity( new Intent( MainActivity.this , TextViewerActivity.class ) );

                    return;
                }

                StorageUtil.ensureFontDir();

                fetchFont();
            }
        } );
    }
}