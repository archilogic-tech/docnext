package jp.archilogic.docnext.android.setting;

import java.io.File;

import jp.archilogic.docnext.android.R;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Setting extends Activity {
    private interface ProgressConverter {
        public int PrefToProgress( int value );
        public int progressToPref( int value );
    }
    private final static int[] _colorArray = {
            0xffffffff,
            0xffffffcc,
            0xfff5f5dc,
            0xffffe4c4,
            0xffd3ecf0,
            0xffd8bfd8,
            0xffa5a5a5,
            0xffff6347,
            0xff6a5acd,
            0xffFF8C00,
            0xff228B22,
            0xff660099,
            0xff003366,
            0xffC01920,
            0xff33363B,
            0xff070101
    };

    public static final String HORIZONTAL = "horizontal";

    private final static int MAX_FONT_SIZE = 36;

    private final static int MIN_FONT_PROGRESS = 2;

    private final static int MIN_FONT_SIZE = 8;
    
    public final static String PREF_BRIGHTNESS = "brightness";

    public final static String PREF_FONT_COLOR = "fontColor";
    
    public final static String PREF_FONT_SIZE = "fontSize";

    public final static String PREF_SCREENLOCK = "screenLock";
    public static final String VERTICAL = "vertical";
    private Editor _editor;
    private SharedPreferences _prefs; 
    
    private final float BRIGHTNESS_STEP = 0.1f;

    private final float MAX_BRIGHTNESS = 1.0f;
    
    private final float MIN_BRIGHTNESS = 0.1f;

    private final int BRIGHTNESS_BAR_MAX_PROGRESS = ( int ) ( MAX_BRIGHTNESS / BRIGHTNESS_STEP );
    
    public final static String PREF_BACKGROUND_COLOR = "backgroundColor";
    
    public static final String PREF_ROTATION = "rotation";  
    
    public final static String PREF_WRITING_MODE = "previewWritingMode";
    
    String melos = "メロスは激怒した。必ず、かの邪智暴虐の王を除かなければならぬと決意した。メロスには政治がわからぬ。メロスは、村の牧人である。笛を吹き、羊と遊んで暮らしてきた。けれども邪悪に対しては、人一倍に敏感であった。きょう未明メロスは村を出発し、野を超え山声、十里はなれた";
    
    public final static int DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    public final static int DEFAULT_FONT_COLOR = _colorArray[ _colorArray.length - 1 ];

    public final static int DEFAULT_FONT_SIZE = 16;

    public final static float DEFAULT_BRIGHTNESS = 1.0f;
    
    public final static String DEFAULT_WRITING_MODE = HORIZONTAL;
    
    public final static boolean DEFAULT_SCREENUNLOCK = true;

    public final static String PREFERENCE_NAME = "viewer";
    
    private static final String FONT_PATH = 
        Environment.getExternalStorageDirectory().getAbsolutePath()
        + File.separator + "ipaexm00103/ipaexm.ttf";
    

    public static void setBrightness( Window window , float value ) {
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = value;
        window.setAttributes( layoutParams );
    }
    
    public static void setOrientation( Activity activity , boolean lock ) {
        Display display = ( ( WindowManager ) activity.getSystemService( WINDOW_SERVICE ) ).getDefaultDisplay();
        int orientation = display.getOrientation();
        
        if ( lock ) {
            switch ( orientation ) {
            case Surface.ROTATION_0:
                activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
                break;
            case Surface.ROTATION_90:
                activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
                break;
            case Surface.ROTATION_180:
                final int reversePortrait = 9;
                activity.setRequestedOrientation( reversePortrait );
                break;
            case Surface.ROTATION_270:
                final int reverseLandscape = 8;
                activity.setRequestedOrientation( reverseLandscape );
                break;
            }
        } else {
            activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR );
        }
    }

    private OnSeekBarChangeListener fontSizeSeekBarChangeListenerBuilder( final String prefName ) {
        return new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged( SeekBar seekBar , int progress , boolean fromUser ) {
                int fontSize = progressToFontSize( progress );
                _editor.putInt( prefName , fontSize );
                _editor.commit();
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) {
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) {
            }

        };
    }
    
    private float getBrightness() {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        return layoutParams.screenBrightness;
    }
    
    private void initBackgroundColorSeekBar() {
        initColorSeekBar( R.id.bgColorSeekBar , PREF_BACKGROUND_COLOR , DEFAULT_BACKGROUND_COLOR );
    }

    private void initBrightnessSeekBar() {
        SeekBar brightnessSeekBar = ( SeekBar ) findViewById( R.id.brightnessSeekBar );
        
        brightnessSeekBar.setMax( BRIGHTNESS_BAR_MAX_PROGRESS );
        Log.d( "brightness" , "getBrightness(): " + getBrightness() );
        float brightness = _prefs.getFloat( PREF_BRIGHTNESS , DEFAULT_BRIGHTNESS );
        brightnessSeekBar.setProgress( ( int ) ( brightness / BRIGHTNESS_STEP ) );

        OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged( SeekBar seekBar , int progress , boolean fromUser ) {
                float brightness = progress * BRIGHTNESS_STEP + MIN_BRIGHTNESS; 
                Log.d( "onProgressChanged" , "brightness:" + brightness );
                _editor.putFloat( PREF_BRIGHTNESS , brightness );
                _editor.commit();
                setBrightness( brightness );
            }
            
            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) {
            }
            
            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) {
            }
        };
        
        brightnessSeekBar.setOnSeekBarChangeListener( listener );
    }
    
    private void initColorSeekBar( int viewId , final String prefName , int defaultValue ) {
        final int maxProgress = _colorArray.length - 1;
        final ProgressConverter converter = new ProgressConverter() {

            @Override
            public int PrefToProgress( int value ) {
                for ( int i = 0 ; i < _colorArray.length ; i++ ) {
                    if ( _colorArray[ i ] == value ) {
                        return i;
                    }
                }
                return 0;
            }

            @Override
            public int progressToPref( int value ) {
                return progressToColor( value );
            }
        };
        
        OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
            
            @Override
            public void onProgressChanged( SeekBar seekBar , int progress , boolean fromUser ) {
                int prefValue = converter.progressToPref( progress );
                _editor.putInt( prefName , prefValue );
                _editor.commit();
                
                updatePreview();
            }
            
            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) {
            }
            
            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) {
            }
        };
        initSeekBar( viewId , prefName , maxProgress , listener  , converter , defaultValue );
    }
    
    private void initFontColorSeekBar() {
        initColorSeekBar( R.id.fontColorSeekBar , PREF_FONT_COLOR , DEFAULT_FONT_COLOR );
    }
    
    private void initFontSizeSeekBar() {
        int maxProgress = ( MAX_FONT_SIZE - MIN_FONT_SIZE ) / MIN_FONT_PROGRESS;

        ProgressConverter converter = new ProgressConverter() {

            @Override
            public int PrefToProgress( int value ) {
                int point = value;
                return ( point - MIN_FONT_SIZE ) / MIN_FONT_PROGRESS;
            }

            @Override
            public int progressToPref( int value ) {
                return progressToFontSize( value );
            }
        };
        
        initSeekBar( R.id.fontSizeSeekBar , PREF_FONT_SIZE , maxProgress , 
                fontSizeSeekBarChangeListenerBuilder( PREF_FONT_SIZE ) , converter ,
                DEFAULT_FONT_SIZE );
    }
    private void initLockCheckBox() {
        CheckBox screenUnlockCheckBox = ( CheckBox ) findViewById( R.id.unlockCheckBox );
        
        screenUnlockCheckBox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
    
            @Override
            public void onCheckedChanged( CompoundButton buttonView , boolean isChecked ) {

                Display display = ( ( WindowManager ) getSystemService( WINDOW_SERVICE ) ).getDefaultDisplay();
                int orientation = display.getOrientation();
                
                boolean lock = !isChecked;
                setOrientation( lock );
                _editor.putBoolean( PREF_SCREENLOCK , lock );
                _editor.putInt( PREF_ROTATION , orientation );
                _editor.commit();
            }
        });
        
        boolean lock = _prefs.getBoolean( PREF_SCREENLOCK , DEFAULT_SCREENUNLOCK );
        screenUnlockCheckBox.setChecked( !lock );
        setOrientation( lock );
    }
    private void initResetButton() {
        CheckBox resetCheckBox = ( CheckBox ) findViewById( R.id.resetCheckBox );
        
        resetCheckBox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged( CompoundButton buttonView , boolean isChecked ) {
                reset();
            }
        });
    }
    private void initSeekBar( int viewId , String prefName , int maxProgress , OnSeekBarChangeListener listener , 
            ProgressConverter converter , int defaultValue ) {
        SeekBar seekBar = ( SeekBar ) findViewById( viewId );
        
        seekBar.setMax( maxProgress );
        seekBar.setOnSeekBarChangeListener( listener );
        int prefValue = _prefs.getInt( prefName  , defaultValue );
        seekBar.setProgress( converter.PrefToProgress( prefValue ) );
    }
    private void initViews() {
        
        initLockCheckBox();
        initFontSizeSeekBar();
        initFontColorSeekBar();
        initBackgroundColorSeekBar();
        initBrightnessSeekBar();
        initWritingModeCheckBox();
        initResetButton();
        
    }
    
    private void initWritingModeCheckBox() {
        final RadioButton horizontalRadioButton = ( RadioButton ) findViewById( R.id.horizontalRadioButton );
        final RadioButton verticalRadioButton = ( RadioButton ) findViewById( R.id.verticalRadioButton );
        
        String writingMode = _prefs.getString( PREF_WRITING_MODE , HORIZONTAL );
        if ( writingMode.equalsIgnoreCase( VERTICAL ) ) {
            verticalRadioButton.setChecked( true );
            horizontalRadioButton.setChecked( false );
        } else {
            verticalRadioButton.setChecked( false);
            horizontalRadioButton.setChecked( true );
        }
        
        setPreviewWritingMode( writingMode );
        
        OnCheckedChangeListener listener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( CompoundButton buttonView , boolean isChecked ) {
                int id = buttonView.getId();
                String writingMode = "";
                
                if ( id == R.id.verticalRadioButton && isChecked ) {
                    writingMode = VERTICAL;
                    
                    horizontalRadioButton.setChecked( !isChecked );
                    verticalRadioButton.setChecked( isChecked );
                } else if ( id == R.id.horizontalRadioButton && isChecked ) {
                    writingMode = HORIZONTAL;
                    
                    horizontalRadioButton.setChecked( isChecked );
                    verticalRadioButton.setChecked( !isChecked );
                }
                
                setPreviewWritingMode( writingMode );

                _editor.putString( PREF_WRITING_MODE , writingMode );
                _editor.commit();
            }
        };
        verticalRadioButton.setOnCheckedChangeListener( listener ); 
        horizontalRadioButton.setOnCheckedChangeListener( listener );
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.setting );
        
        //_prefs = getPreferences( MODE_PRIVATE );
        _prefs = getSharedPreferences( PREFERENCE_NAME , Context.MODE_PRIVATE );
        _editor = _prefs.edit();
        
        initViews();
        
        updatePreview();
        
        setDisplayPreferences();
    }

    private int progressToColor( int progress ) {
        if ( progress >= _colorArray.length ) {
            return 0x0000;
        }
        
        return _colorArray[ progress ];
    }
    private int progressToFontSize( int progress ) {
        return MIN_FONT_SIZE + progress * MIN_FONT_PROGRESS;
    }
    
    private void reset() {
        _editor.putInt( PREF_BACKGROUND_COLOR , DEFAULT_BACKGROUND_COLOR );
        _editor.putInt( PREF_FONT_COLOR , DEFAULT_FONT_COLOR );
        _editor.putInt( PREF_FONT_SIZE , DEFAULT_FONT_SIZE );
        _editor.putBoolean( PREF_SCREENLOCK , DEFAULT_SCREENUNLOCK );
        _editor.putFloat( PREF_BRIGHTNESS , DEFAULT_BRIGHTNESS );
        _editor.putString( PREF_WRITING_MODE , DEFAULT_WRITING_MODE );
        _editor.commit();
        
        initViews();
    }
    
    private void setBrightness( float value ) {
        setBrightness( getWindow() , value );
    }
    
    private void setOrientation( boolean lock ) {
        setOrientation( this , lock );
    }
    
    private void setPreviewWritingMode( String mode ) {
        RelativeLayout layout = ( RelativeLayout ) findViewById( R.id.layoutOrientation );

        View view = findViewById( R.id.previewText );
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layout.removeView( view );
        
        if ( mode.equals( VERTICAL ) ) {
            VTextView textView = new VTextView( getApplicationContext() , null );
            textView.setText( melos );
            textView.setId( R.id.previewText );
            textView.setLayoutParams( layoutParams );
            layout.addView( textView );
        } else {
            TextView textView = new TextView( getApplicationContext() );
            textView.setText( melos );
            textView.setId( R.id.previewText );
            textView.setLayoutParams( layoutParams );
            layout.addView( textView );
        }
        
        updatePreview();
    }

    public static void setDisplayPreferences( Activity activity ) {
        SharedPreferences preferences = activity.getSharedPreferences( PREFERENCE_NAME , MODE_PRIVATE );
        Setting.setBrightness( activity.getWindow() , preferences.getFloat( PREF_BRIGHTNESS , DEFAULT_BRIGHTNESS ) );
        
        Setting.setOrientation( activity , preferences.getBoolean( PREF_SCREENLOCK , DEFAULT_SCREENUNLOCK ) );
    }
    
    private void setDisplayPreferences() {
        setDisplayPreferences( this );
    }

    private void updatePreview() {
        View view = findViewById( R.id.previewText );
        
        if ( view instanceof TextView ) {
            TextView preview = ( TextView ) view;
            preview.setTextSize( TypedValue.COMPLEX_UNIT_PT , _prefs.getInt( PREF_FONT_SIZE , DEFAULT_FONT_SIZE ) ); 
            preview.setTextColor( _prefs.getInt( PREF_FONT_COLOR , DEFAULT_FONT_COLOR ) );
            preview.setBackgroundColor( _prefs.getInt( PREF_BACKGROUND_COLOR , DEFAULT_BACKGROUND_COLOR ) );
            preview.setLineSpacing( 3 , 2 );
            preview.setPadding( 18 , 18 , 18 , 18 );
            if ( ( new File ( FONT_PATH ) ).exists() ) {
                preview.setTypeface( Typeface.createFromFile( FONT_PATH ) );
            }
        } else if ( view instanceof VTextView ) {
            VTextView preview = ( VTextView ) view;
            preview.setTextSize( _prefs.getInt( PREF_FONT_SIZE , DEFAULT_FONT_SIZE ) );
            preview.setTextColor( _prefs.getInt( PREF_FONT_COLOR , DEFAULT_FONT_COLOR ) );
            preview.setBackgroundColor( _prefs.getInt( PREF_BACKGROUND_COLOR , DEFAULT_BACKGROUND_COLOR ) );
       }
    }
}