package jp.archilogic.docnext.android;

import java.io.File;

import jp.archilogic.docnext.android.info.MetaInfo;
import jp.archilogic.docnext.android.manager.ConstManager;
import jp.archilogic.docnext.android.task.DownloadTask;
import jp.archilogic.docnext.android.task.Receiver;
import jp.archilogic.docnext.android.type.ExtraType;
import jp.archilogic.docnext.android.util.StorageUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView _webView;

    private void checkMetaInfo( final long id ) {
        final MetaInfo meta = StorageUtil.getMetaInfo( id );

        switch ( meta.type ) {
        case IMAGE:
            fetchImage( id , meta );
            break;
        case TEXT:
            fetchText( id , meta );
            break;
        default:
            throw new RuntimeException();
        }
    }

    private void fetchFont( final String name , final long id ) {
        if ( new File( StorageUtil.getFontPath( name ) ).exists() ) {
            showText( id );

            return;
        }

        StorageUtil.ensureFontDir();

        final ProgressDialog progress = ProgressDialog.show( this , "" , "Loading font..." );

        new DownloadTask( this , new Receiver< Void , Void >() {
            @Override
            public void error( final Void error ) {
                progress.dismiss();
            }

            @Override
            public void receive( final Void result ) {
                progress.dismiss();

                showText( id );
            }
        } , ConstManager.getInstance( MainActivity.this ).getEndpoint() + "font/" + name ,
                StorageUtil.getFontPath( name ) ).execute();
    }

    private void fetchImage( final long id , final MetaInfo meta ) {
        if ( new File( StorageUtil.getImagePath( id , meta.pages - 1 ) ).exists() ) {
            fetchImageThumbnail( id , meta );

            return;
        }

        StorageUtil.ensureImageDir( id );

        final ProgressDialog progress = new ProgressDialog( this );
        progress.setMessage( "Loading image..." );
        progress.setCancelable( false );
        progress.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
        progress.setMax( meta.pages );

        progress.show();

        fetchImageHelper( id , meta , 0 , progress );
    }

    private void fetchImageHelper( final long id , final MetaInfo meta , final int page , final ProgressDialog progress ) {
        if ( page < meta.pages ) {
            progress.setProgress( page );

            new DownloadTask( this , new Receiver< Void , Void >() {
                @Override
                public void error( final Void error ) {
                    progress.dismiss();
                }

                @Override
                public void receive( final Void result ) {
                    fetchImageHelper( id , meta , page + 1 , progress );
                }
            } , ConstManager.getInstance( MainActivity.this ).getEndpoint() + "image/" + id + "/" + page ,
                    StorageUtil.getImagePath( id , page ) ).execute();
        } else {
            progress.dismiss();

            fetchImageThumbnail( id , meta );
        }
    }

    private void fetchImageThumbnail( final long id , final MetaInfo meta ) {
        if ( new File( StorageUtil.getImageThumbnailPath( id , meta.pages - 1 ) ).exists() ) {
            showImage( id );

            return;
        }

        StorageUtil.ensureImageDir( id );

        final ProgressDialog progress = new ProgressDialog( this );
        progress.setMessage( "Loading thumbnail..." );
        progress.setCancelable( false );
        progress.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
        progress.setMax( meta.pages );

        progress.show();

        fetchImageThumbnailHelper( id , meta , 0 , progress );
    }

    private void fetchImageThumbnailHelper( final long id , final MetaInfo meta , final int page ,
            final ProgressDialog progress ) {
        if ( page < meta.pages ) {
            progress.setProgress( page );

            new DownloadTask( this , new Receiver< Void , Void >() {
                @Override
                public void error( final Void error ) {
                    progress.dismiss();
                }

                @Override
                public void receive( final Void result ) {
                    fetchImageThumbnailHelper( id , meta , page + 1 , progress );
                }
            } , ConstManager.getInstance( MainActivity.this ).getEndpoint() + "imageThumbnail/" + id + "/" + page ,
                    StorageUtil.getImageThumbnailPath( id , page ) ).execute();
        } else {
            progress.dismiss();

            showImage( id );
        }
    }

    private void fetchMetaInfo( final long id ) {
        if ( new File( StorageUtil.getMetaInfoPath( id ) ).exists() ) {
            checkMetaInfo( id );

            return;
        }

        StorageUtil.ensureMetaInfoDir();

        final ProgressDialog progress = ProgressDialog.show( this , "" , "Loading meta..." );

        new DownloadTask( this , new Receiver< Void , Void >() {
            @Override
            public void error( final Void error ) {
                progress.dismiss();
            }

            @Override
            public void receive( final Void result ) {
                progress.dismiss();

                checkMetaInfo( id );
            }
        } , ConstManager.getInstance( MainActivity.this ).getEndpoint() + "meta/" + id + ".json" ,
                StorageUtil.getMetaInfoPath( id ) ).execute();
    }

    private void fetchText( final long id , final MetaInfo meta ) {
        if ( new File( StorageUtil.getTextPath( id , meta.pages - 1 ) ).exists() ) {
            fetchFont( "default" , id );

            return;
        }

        StorageUtil.ensureTextDir( id );

        final ProgressDialog progress = new ProgressDialog( this );
        progress.setMessage( "Loading text..." );
        progress.setCancelable( false );
        progress.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
        progress.setMax( meta.pages );

        progress.show();

        fetchTextHelper( id , meta , 0 , progress );
    }

    private void fetchTextHelper( final long id , final MetaInfo meta , final int page , final ProgressDialog progress ) {
        if ( page < meta.pages ) {
            progress.setProgress( page );

            new DownloadTask( this , new Receiver< Void , Void >() {
                @Override
                public void error( final Void error ) {
                    progress.dismiss();
                }

                @Override
                public void receive( final Void result ) {
                    fetchTextHelper( id , meta , page + 1 , progress );
                }
            } , ConstManager.getInstance( MainActivity.this ).getEndpoint() + "text/" + id + "/" + page + ".json" ,
                    StorageUtil.getTextPath( id , page ) ).execute();
        } else {
            progress.dismiss();

            fetchFont( "default" , id );
        }
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        _webView = ( WebView ) findViewById( R.id.webView );

        _webView.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE );
        _webView.setWebViewClient( new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading( final WebView view , final String url ) {
                final Uri uri = Uri.parse( url );

                if ( uri.getScheme().equals( "docnext" ) ) {
                    fetchMetaInfo( Long.parseLong( uri.getHost() ) );

                    return true;
                }

                return false;
            }
        } );

        _webView.loadUrl( ConstManager.getInstance( this ).getEndpoint() + "library.html" );
    }

    private void showImage( final long id ) {
        startActivity( new Intent( this , ImageViewerActivity.class ).putExtra( ExtraType.ID.toString() , id ) );
    }

    private void showText( final long id ) {
        startActivity( new Intent( this , TextViewerActivity.class ).putExtra( ExtraType.ID.toString() , id ) );
    }
}