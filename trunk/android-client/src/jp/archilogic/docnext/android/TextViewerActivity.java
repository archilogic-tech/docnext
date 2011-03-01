package jp.archilogic.docnext.android;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jp.archilogic.docnext.android.core.OnPageChangedListener;
import jp.archilogic.docnext.android.core.text.CoreTextConfig;
import jp.archilogic.docnext.android.core.text.CoreTextConfig.LineBreakingRule;
import jp.archilogic.docnext.android.core.text.CoreTextInfo;
import jp.archilogic.docnext.android.core.text.CoreTextView;
import jp.archilogic.docnext.android.core.text.TextDocDirection;
import jp.archilogic.docnext.android.info.MetaInfo;
import jp.archilogic.docnext.android.type.ExtraType;
import jp.archilogic.docnext.android.util.StorageUtil;
import net.arnx.jsonic.JSON;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.common.collect.Lists;

public class TextViewerActivity extends Activity {
    private CoreTextView _coreTextView;
    private TextView _currentPageTextView;
    private TextView _totalPageTextView;

    private CoreTextConfig _config;

    private final OnPageChangedListener _coreImageListener = new OnPageChangedListener() {
        @Override
        public void onPageChanged( final int index ) {
            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    _currentPageTextView.setText( String.valueOf( index + 1 ) );
                }
            } );
        }
    };

    private void assignWidget() {
        _coreTextView = ( CoreTextView ) findViewById( R.id.coreTextView );
        _currentPageTextView = ( TextView ) findViewById( R.id.CurrentPageTextView );
        _totalPageTextView = ( TextView ) findViewById( R.id.TotalPageTextView );
    }

    private void changeDirection() {
        _config.direction =
                _config.direction == TextDocDirection.HORIZONTAL ? TextDocDirection.VERTICAL
                        : TextDocDirection.HORIZONTAL;

        _coreTextView.setConfig( _config );
    }

    private void changeFontSizeBigger() {
        _config.fontSize++;
        _coreTextView.setConfig( _config );
    }

    private void changeFontSizeSmaller() {
        _config.fontSize--;
        _coreTextView.setConfig( _config );
    }

    private void changeLineBreakRule() {
        switch ( _config.lineBreakingRule ) {
        case NONE:
            _config.lineBreakingRule = LineBreakingRule.TO_NEXT;
            break;
        case TO_NEXT:
            _config.lineBreakingRule = LineBreakingRule.SQUEEZE;
            break;
        case SQUEEZE:
            _config.lineBreakingRule = LineBreakingRule.NONE;
            break;
        default:
            throw new RuntimeException();
        }

        _coreTextView.setConfig( _config );
    }

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.text_viewer );

        final long id = getIntent().getLongExtra( ExtraType.ID.toString() , -1 );
        if ( id == -1 ) {
            throw new RuntimeException();
        }

        assignWidget();

        final MetaInfo meta = StorageUtil.getMetaInfo( id );

        final List< CoreTextInfo > source = Lists.newArrayList();
        for ( int page = 0 ; page < meta.pages ; page++ ) {
            try {
                final InputStream in = new FileInputStream( StorageUtil.getTextPath( id , page ) );

                source.add( JSON.decode( in , CoreTextInfo.class ) );

                in.close();
            } catch ( final IOException e ) {
                throw new RuntimeException( e );
            }
        }
        _coreTextView.setSources( source );

        _coreTextView.setListener( _coreImageListener );
        _coreTextView.setConfig( _config = new CoreTextConfig() );

        _currentPageTextView.setText( String.valueOf( 1 ) );
        _totalPageTextView.setText( String.valueOf( source.size() ) );
    }

    @Override
    public boolean onCreateOptionsMenu( final Menu menu ) {
        menu.add( Menu.NONE , 0 , Menu.NONE , "Toggle Justify" );
        menu.add( Menu.NONE , 1 , Menu.NONE , "Change Line Break Rule" );
        menu.add( Menu.NONE , 2 , Menu.NONE , "Bigger" );
        menu.add( Menu.NONE , 3 , Menu.NONE , "Smaller" );
        menu.add( Menu.NONE , 4 , Menu.NONE , "Reverse color" );
        menu.add( Menu.NONE , 5 , Menu.NONE , "Direction" );

        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected( final MenuItem item ) {
        switch ( item.getItemId() ) {
        case 0:
            toggleJustify();

            return true;
        case 1:
            changeLineBreakRule();

            return true;
        case 2:
            changeFontSizeBigger();

            return true;
        case 3:
            changeFontSizeSmaller();

            return true;
        case 4:
            reverseColor();

            return true;
        case 5:
            changeDirection();

            return true;
        }

        return false;
    }

    private void reverseColor() {
        _config.backgroundColor ^= _config.defaultTextColor;
        _config.defaultTextColor ^= _config.backgroundColor;
        _config.backgroundColor ^= _config.defaultTextColor;

        _coreTextView.setConfig( _config );
    }

    private void toggleJustify() {
        _config.useJustification = !_config.useJustification;
        _coreTextView.setConfig( _config );
    }
}
