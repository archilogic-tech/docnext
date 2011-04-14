package jp.archilogic.docnext.android.thumnail;

import jp.archilogic.docnext.android.Kernel;
import jp.archilogic.docnext.android.activity.CoreViewActivity;
import jp.archilogic.docnext.android.coreview.NavigationView;
import jp.archilogic.docnext.android.meta.DocumentType;
import jp.archilogic.docnext.android.thumnail.ThumbnailImageAdapter.Direction;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

public class ThumbnailView extends NavigationView {
    private CoverFlow _coverFlow;

    private final Direction _direction = Direction.LEFT;

    public ThumbnailView( final Context context ) {
        super( context );
    }
    
    protected void init() {
        initCoverFlow();
    }

    private void initCoverFlow() {
        _coverFlow = new CoverFlow( getContext() );

        _coverFlow.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT ,
                LayoutParams.FILL_PARENT ) );
        _coverFlow.setAdapter( new ThumbnailImageAdapter( getContext() , _id , Direction.LEFT ) );
        _coverFlow.setSelection( _coverFlow.getAdapter().getCount() - 1 );

        _coverFlow.setOnItemClickListener( new OnItemClickListener() {

            @Override
            public void onItemClick( final AdapterView< ? > parent , final View v ,
                    final int position , final long id ) {
                final Intent intent = new Intent();
                int page;
                if ( _direction == Direction.LEFT ) {
                    page = _coverFlow.getAdapter().getCount() - position - 1;
                } else {
                    page = position;
                }
                if ( !Kernel.getLocalProvider().isAllImageExists( _id , page ) ) {
                    return;
                }
                intent.putExtra( CoreViewActivity.EXTRA_PAGE , page );
                _delegate.changeCoreViewType( DocumentType.IMAGE , intent );
            }
        } );

        addView( _coverFlow );
    }
}
