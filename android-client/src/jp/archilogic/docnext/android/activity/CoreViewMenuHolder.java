package jp.archilogic.docnext.android.activity;

import java.util.Set;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.coreview.image.CoreImageRenderer;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.type.FragmentType;
import jp.archilogic.docnext.android.util.AnimationUtils2;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CoreViewMenuHolder {
    private final CoreViewActivity _activity;

    private final CoreView _view;
    private final View _menuView;
    private View _bookmarkMenuItem;
    
    private final String TAG = "CoreViewMenuHolder";

    private final long[] _ids;

    private final BroadcastReceiver _pageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( final Context context , final Intent intent ) {
            Log.d( TAG , "onReceive" );
            bindBookmarkMenuItemIcon();
        }
    };
    

    CoreViewMenuHolder( final CoreViewActivity activity , final CoreView view , final long[] ids ,
            final DocumentType type ) {
        _activity = activity;
        _view = view;
        _ids = ids;

        _menuView = buildMenu( type );

        activity.registerReceiver( _pageChangeReceiver , new IntentFilter( CoreImageRenderer.BROADCAST_PAGE_CHANGED ) );
    }

    private void bindBookmarkMenuItemIcon() {
        if ( !( _view instanceof HasPage ) ) {
            return;
        }

        final int page = ( ( HasPage ) _view ).getPage();

        final Set< Integer > bookmark = Sets.newTreeSet( Kernel.getLocalProvider().getBookmarkInfo( _ids[ 0 ] ) );

        final ImageView image = ( ImageView ) _bookmarkMenuItem.findViewById( R.id.bookmark );

        image.setImageResource( bookmark.contains( page ) ? R.drawable.button_bookmark_on
                : R.drawable.button_bookmark_off );
    }
    
    private View buildMenu( final DocumentType type ) {
        final LinearLayout root = new LinearLayout( _activity );

        root.setLayoutParams( new FrameLayout.LayoutParams( FrameLayout.LayoutParams.FILL_PARENT ,
                FrameLayout.LayoutParams.WRAP_CONTENT ) );
        root.setOrientation( LinearLayout.VERTICAL );
        root.setBackgroundColor( 0x80000000 );
        root.setPadding( dp( 5 ) , dp( 10 ) , dp( 5 ) , dp( 10 ) );
        root.setVisibility( View.GONE );

        buildPrimaryMenu( type , root );
        root.addView( buildSpacer( 0 , dp( 10 ) , 0 ) );
        buildSecondaryMenu( type , root );

        return root;
    }

    private OnClickListener buildMenuClickListener( final FragmentType type ) {
        final DocumentType doc = type.getDocumentType();

        if ( doc != null ) {
            return new OnClickListener() {
                @Override
                public void onClick( final View v ) {
                    _activity.changeCoreViewType( doc , new Intent() );
                }
            };
        } else {
            switch ( type ) {
            case HOME:
                return new OnClickListener() {
                    @Override
                    public void onClick( final View v ) {
                        _activity.finish();
                    }
                };
            case BOOKMARK:
                return new OnClickListener() {
                    @Override
                    public void onClick( final View v ) {
                        toggleBookmark();
                    }
                };
            }
            return null;
        }
    }

    private View buildMenuItem( final FragmentType type ) {
        final View view = type.buildSwithButton( _activity );

        if ( type == FragmentType.BOOKMARK ) {
            _bookmarkMenuItem = view;
            _bookmarkMenuItem.post( new Runnable() {
                @Override
                public void run() {
                    bindBookmarkMenuItemIcon();
                }
            } );
        }

        view.setOnClickListener( buildMenuClickListener( type ) );

        return view;
    }

    private void buildPrimaryMenu( final DocumentType type , final LinearLayout root ) {
        final FragmentType[] primary = type.getPrimarySwitchFragment();

        final LinearLayout holder = new LinearLayout( _activity );

        for ( final FragmentType fragment : primary ) {
            holder.addView( buildMenuItem( fragment ) );
        }

        root.addView( holder );
    }

    private void buildSecondaryMenu( final DocumentType type , final LinearLayout root ) {
        final FragmentType[] secondary = type.getSecondarySwitchFragment();
        final FragmentType[] subSecondary = type.getSubSecondarySwitchFragment();

        final LinearLayout holder = new LinearLayout( _activity );

        for ( final FragmentType fragment : secondary ) {
            holder.addView( buildMenuItem( fragment ) );
        }

        // dead code
        if ( subSecondary.length > 0 && subSecondary.length == 0 ) {
            holder.addView( buildSpacer( 0 , 0 , 1 ) );
        }

        for ( final FragmentType fragment : subSecondary ) {
            holder.addView( buildMenuItem( fragment ) );
        }

        root.addView( holder );
    }

    private View buildSpacer( final int width , final int height , final float weight ) {
        final View view = new View( _activity );

        view.setLayoutParams( new LinearLayout.LayoutParams( width , height , weight ) );

        return view;
    }

    private int dp( final float value ) {
        final float density = _activity.getResources().getDisplayMetrics().density;

        return Math.round( value * density );
    }

    View getMenu() {
        return _menuView;
    }

    private void toggleBookmark() {
        if ( !( _view instanceof HasPage ) ) {
            return;
        }

        final Set< Integer > bookmark = Sets.newTreeSet( Kernel.getLocalProvider().getBookmarkInfo( _ids[ 0 ] ) );

        final int page = ( ( HasPage ) _view ).getPage();

        if ( bookmark.contains( page ) ) {
            bookmark.remove( page );
        } else {
            bookmark.add( page );
        }

        Kernel.getLocalProvider().setBookmarkInfo( _ids[ 0 ] , Lists.newArrayList( bookmark ) );

        bindBookmarkMenuItemIcon();
    }

    void toggleMenu() {
        AnimationUtils2.toggle( _activity , _menuView );
    }

}
