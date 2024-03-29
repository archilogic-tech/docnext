package jp.archilogic.docnext.android.widget;

import java.util.ArrayList;
import java.util.Set;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.CoreView;
import jp.archilogic.docnext.android.coreview.HasPage;
import jp.archilogic.docnext.android.info.BookmarkInfo;
import jp.archilogic.docnext.android.info.DocInfo;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.setting.Setting;
import jp.archilogic.docnext.android.type.FragmentType;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CoreViewMenu extends LinearLayout {
    public interface CoreViewMenuDelegate {
        void changeCoreViewType( DocumentType type , Intent extra );

        void finish();

        CoreView getCoreView();
    }

    private final String TAG = CoreViewMenu.class.getName();
    private final long _id;
    private final CoreViewMenuDelegate _delegate;

    private View _bookmarkMenuItem;
    private TextView _titleView;
    private Context _context;
    
    public CoreViewMenu( final Context context , final DocumentType type , final long id ,
            final CoreViewMenuDelegate delegate ) {
        super( context );

        _id = id;
        _delegate = delegate;
        _context = context;

        initialize( type );
    }

    private void bindBookmarkMenuItemIcon() {
        Log.d( TAG , "bindBookmarkMenuItemIcon" );
        if ( !( _delegate.getCoreView() instanceof HasPage ) ) {
            return;
        }

        final int page = ( ( HasPage ) _delegate.getCoreView() ).getPage();

        if ( Kernel.getLocalProvider().getBookmarkInfo( _id ) == null ) {
            Kernel.getLocalProvider().setBookmarkInfo( _id ,
                    Lists.newArrayList( new ArrayList< BookmarkInfo >() ) );
        }

        final Set< BookmarkInfo > bookmark =
                Sets.newTreeSet( Kernel.getLocalProvider().getBookmarkInfo( _id ) );

        if ( _bookmarkMenuItem == null ) {
            return;
        }
        final ImageView image = ( ImageView ) _bookmarkMenuItem.findViewById( R.id.bookmark );

        image.setImageResource( bookmark.contains( new BookmarkInfo( page ) )
                ? R.drawable.button_bookmark_on : R.drawable.button_bookmark_off );
    }

    private OnClickListener buildMenuClickListener( final FragmentType type ) {
        final DocumentType doc = type.getDocumentType();

        if ( doc != null ) {
            return new OnClickListener() {
                @Override
                public void onClick( final View v ) {
                    final Intent intent = new Intent();
                    if ( _delegate.getCoreView() instanceof HasPage ) {
                        intent.putExtra( CoreViewActivity.EXTRA_PAGE ,
                                ( ( HasPage ) _delegate.getCoreView() ).getPage() );
                    }
                    _delegate.changeCoreViewType( doc , intent );
                }
            };
        } else {
            switch ( type ) {
            case HOME:
                return new OnClickListener() {
                    @Override
                    public void onClick( final View v ) {
                        _delegate.finish();
                    }
                };
            case BOOKMARK:
                return new OnClickListener() {
                    @Override
                    public void onClick( final View v ) {
                        toggleBookmark();
                    }
                };
            case SETTING:
                return new OnClickListener() {
                    @Override
                    public void onClick( final View v ) {
                        Intent intent = new Intent( _context , Setting.class );
                        _context.startActivity( intent );
                    }
                };
            }
            return null;
        }
    }

    private View buildMenuItem( final FragmentType type ) {
        final View view = type.buildSwithButton( getContext() );

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

    private void buildPageInfo() {
        final LinearLayout holder = new LinearLayout( getContext() );

        _titleView = new TextView( getContext() );
        updateTitle();
        
        holder.addView( _titleView );
        addView( holder );
    }
    
    private void buildPrimaryMenu( final DocumentType type ) {
        final FragmentType[] primary = type.getPrimarySwitchFragment();

        final LinearLayout holder = new LinearLayout( getContext() );

        for ( final FragmentType fragment : primary ) {
            holder.addView( buildMenuItem( fragment ) );
        }

        addView( holder );
    }
    
    private void buildSecondaryMenu( final DocumentType type ) {
        final FragmentType[] secondary = type.getSecondarySwitchFragment();
        final FragmentType[] subSecondary = type.getSubSecondarySwitchFragment();

        final LinearLayout holder = new LinearLayout( getContext() );

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

        addView( holder );
    }

    private View buildSpacer( final int width , final int height , final float weight ) {
        final View view = new View( getContext() );

        view.setLayoutParams( new LinearLayout.LayoutParams( width , height , weight ) );

        return view;
    }

    private int dp( final float value ) {
        final float density = getResources().getDisplayMetrics().density;

        return Math.round( value * density );
    }

    private void initialize( final DocumentType type ) {
        setLayoutParams( new FrameLayout.LayoutParams( FrameLayout.LayoutParams.FILL_PARENT ,
                FrameLayout.LayoutParams.WRAP_CONTENT ) );
        setOrientation( LinearLayout.VERTICAL );
        setBackgroundColor( 0x80000000 );
        setPadding( dp( 5 ) , dp( 10 ) , dp( 5 ) , dp( 10 ) );
        setVisibility( View.GONE );

        buildPageInfo();
        addView( buildSpacer( 0 , dp( 10 ) , 0 ) );
        buildPrimaryMenu( type );
        addView( buildSpacer( 0 , dp( 10 ) , 0 ) );
        buildSecondaryMenu( type );
    }

    public void onPageChanged() {
        bindBookmarkMenuItemIcon();
        updateTitle();
    }

    private void toggleBookmark() {
        if ( !( _delegate.getCoreView() instanceof HasPage ) ) {
            return;
        }

        final Set< BookmarkInfo > bookmark =
                Sets.newTreeSet( Kernel.getLocalProvider().getBookmarkInfo( _id ) );

        final int page = ( ( HasPage ) _delegate.getCoreView() ).getPage();

        if ( bookmark.contains( new BookmarkInfo( page ) ) ) {
            bookmark.remove( new BookmarkInfo( page ) );
        } else {
            bookmark.add( new BookmarkInfo( page ) );
        }

        Kernel.getLocalProvider().setBookmarkInfo( _id , Lists.newArrayList( bookmark ) );

        bindBookmarkMenuItemIcon();
    }
    
    private void updateTitle() {
        int page = ( ( HasPage ) _delegate.getCoreView() ).getPage();
        
        DocInfo doc = Kernel.getLocalProvider().getDocInfo( _id );
        String title = Kernel.getLocalProvider().getTOCText( _id , page ) 
            + " ( " + ( page + 1 ) + " / " + doc.pages + " page )";
        _titleView.setText( title );
    }
}
