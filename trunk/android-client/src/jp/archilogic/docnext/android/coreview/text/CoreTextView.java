package jp.archilogic.docnext.android.coreview.text;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.CoreViewDelegate;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.coreview.text.CoreTextConfig.LineBreakingRule;
import jp.archilogic.docnext.android.util.AnimationUtils2;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

public class CoreTextView extends FrameLayout implements CoreView , HasPage {
    private SurfaceView _surfaceView;
    private View _menuView;
    private View _toggleJustifyButton;
    private View _changeLineBreakRuleButton;
    private View _biggerCharButton;
    private View _smallerCharButton;
    private View _reverseColorButton;
    private View _changeDirectionButton;

    private CoreTextCallback _callback;
    private CoreTextConfig _config;

    private final OnClickListener _toggleJustifyButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _config.useJustification = !_config.useJustification;

            _callback.setConfig( _config );
        }
    };

    private final OnClickListener _changeLineBreakRuleButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _config.lineBreakingRule =
                    LineBreakingRule.values()[ ( _config.lineBreakingRule.ordinal() + 1 )
                            % LineBreakingRule.values().length ];

            _callback.setConfig( _config );
        }
    };

    private final OnClickListener _biggerCharButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _config.fontSize++;

            _callback.setConfig( _config );
        }
    };

    private final OnClickListener _smallerCharButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _config.fontSize--;

            _callback.setConfig( _config );
        }
    };

    private final OnClickListener _reverseColorButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _config.backgroundColor ^= _config.defaultTextColor;
            _config.defaultTextColor ^= _config.backgroundColor;
            _config.backgroundColor ^= _config.defaultTextColor;

            _callback.setConfig( _config );
        }
    };

    private final OnClickListener _changeDirectionButtonClick = new OnClickListener() {
        @Override
        public void onClick( final View v ) {
            _config.direction =
                    TextDocDirection.values()[ ( _config.direction.ordinal() + 1 )
                            % TextDocDirection.values().length ];

            _callback.setConfig( _config );
        }
    };
    private Context _context;

    public CoreTextView( final Context context ) {
        super( context );

        LayoutInflater.from( context ).inflate( R.layout.core_text , this , true );

        assignWidget();

        final BitmapDrawable backgroundDrawable =
                ( BitmapDrawable ) getResources().getDrawable( R.drawable.background );
        final Bitmap background = backgroundDrawable.getBitmap();

        _surfaceView.getHolder().addCallback( _callback = new CoreTextCallback( background ) );

        _callback.setConfig( _config = new CoreTextConfig( context ) );
        
        _context = context;

        _toggleJustifyButton.setOnClickListener( _toggleJustifyButtonClick );
        _changeLineBreakRuleButton.setOnClickListener( _changeLineBreakRuleButtonClick );
        _biggerCharButton.setOnClickListener( _biggerCharButtonClick );
        _smallerCharButton.setOnClickListener( _smallerCharButtonClick );
        _reverseColorButton.setOnClickListener( _reverseColorButtonClick );
        _changeDirectionButton.setOnClickListener( _changeDirectionButtonClick );
    }

    private void assignWidget() {
        _surfaceView = ( SurfaceView ) findViewById( R.id.surfaceView );
        _menuView = findViewById( R.id.menuView );
        _toggleJustifyButton = findViewById( R.id.toggleJustifyButton );
        _changeLineBreakRuleButton = findViewById( R.id.changeLineBreakRuleButton );
        _biggerCharButton = findViewById( R.id.biggerCharButton );
        _smallerCharButton = findViewById( R.id.smallerCharButton );
        _reverseColorButton = findViewById( R.id.reverseColorButton );
        _changeDirectionButton = findViewById( R.id.changeDirectionButton );
    }

    @Override
    public int getPage() {
        return 0;
    }

    @Override
    public void onDoubleTapGesture( final PointF point ) {
    }

    @Override
    public void onDragGesture( final PointF delta ) {
        _callback.drag( delta );
    }

    @Override
    public void onFlingGesture( final PointF velocity ) {
    }

    @Override
    public void onGestureBegin() {
        _callback.beginInteraction();
    }

    @Override
    public void onGestureEnd() {
        _callback.endInteraction();
    }

    @Override
    public void onMenuVisibilityChange( final boolean isMenuVisible ) {
        final boolean willVisible = _menuView.getVisibility() == GONE;

        if ( isMenuVisible == willVisible ) {
            AnimationUtils2.toggle( getContext() , _menuView );
        }
    }

    @Override
    public void onPause() {
        Log.d( "CoreTextView" , "onPause" );
    }

    @Override
    public void onResume() {
        Log.d( "CoreTextView" , "onResume" );
        _callback.setConfig( _config = new CoreTextConfig( _context ) );
        
        // just for redrawing
        // there should be better way.
        // maybe coretextcallback's thread have problem with redrawing
//        PointF point = new PointF();
//        _callback.drag( point );
    }

    @Override
    public void onTapGesture( final PointF point ) {
    }

    @Override
    public void onZoomGesture( final float scaleDelta , final PointF center ) {
    }

    @Override
    public void restoreState( final Bundle state ) {
        Log.d( "CoreTextView" , "restoreState" );
    }

    @Override
    public void saveState( final Bundle state ) {
    }

    @Override
    public void setDelegate( final CoreViewDelegate delegate ) {
    }

    @Override
    public void setIds( final long[] ids ) {
        _callback.setId( ids[ 0 ] );
    }

    @Override
    public void setPage( final int page ) {
        _callback.setPage( page );
    }
}
