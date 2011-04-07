package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.R;
import jp.archilogic.docnext.android.coreview.image.PageInfo.PageTextureStatus;
import jp.archilogic.docnext.android.info.SizeFInfo;
import jp.archilogic.docnext.android.info.SizeInfo;
import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLES11Ext;
import android.os.SystemClock;

public class CoreImageRenderEngine {
    private static final int TEXTURE_SIZE = 512;

    private TextureInfo _background;
    private TextureInfo _blank;
    private PageInfo[] _pages;

    // to avoid GC
    private final CoreImageMatrix _immutableMatrix = new CoreImageMatrix();
    private final SizeFInfo _immutablePadding = new SizeFInfo( 0 , 0 );

    int _fpsCounter = 0;
    long _fpsTime;
    long _frameSum;

    void bindPageImage( final LoadBitmapTask task ) {
        final PageTextureInfo texture = _pages[ task.page ].textures[ task.level ][ task.py ][ task.px ];

        texture.bindTexture( task.bitmap );

        task.bitmap.recycle();

        _pages[ task.page ].statuses[ task.level ][ task.py ][ task.px ] = PageTextureStatus.BIND;
    }

    private void checkAndDrawSingleImage( final int level , final PageTextureInfo[][] textures ,
            final PageTextureStatus[][] statuses , final int py , final int px , final float x , final float y ,
            final float w , final float h , final SizeInfo surface ) {
        final boolean isVisible = x + w >= 0 && x < surface.width && y + h >= 0 && y < surface.height;

        if ( isVisible ) {
            if ( statuses[ py ][ px ] == PageTextureStatus.BIND ) {
                drawSingleImage( textures[ py ][ px ].id , x , y , w , h );
            } else if ( level == 0 ) {
                drawSingleImage( _blank.id , x , y , w , h );
            }
        }
    }

    private void drawBackground() {
        drawSingleImage( _background.id , 0 , 0 , _background.width , _background.height );
    }

    private void drawImage( final CoreImageMatrix matrix , final SizeFInfo padding , final CoreImageState state ) {
        final int xSign = state.direction.toXSign();
        final int ySign = state.direction.toYSign();

        for ( int level = 0 ; level < state.nLevel ; level++ ) {
            if ( level > 0 && ( matrix.scale < Math.pow( 2 , level - 1 ) || state.isCleanup() ) ) {
                continue;
            }

            final float factor = ( float ) Math.pow( 2 , level );

            final float size = matrix.length( TEXTURE_SIZE ) / factor;

            for ( int delta = -1 ; delta <= 1 ; delta++ ) {
                final int page = state.page + delta;

                if ( page >= 0 && page < _pages.length ) {
                    final PageTextureInfo[][] textures = _pages[ page ].textures[ level ];
                    final PageTextureStatus[][] statuses = _pages[ page ].statuses[ level ];

                    float y =
                            state.surfaceSize.height
                                    - ( matrix.y( TEXTURE_SIZE / factor ) + padding.height + matrix
                                            .length( state.pageSize.height ) * delta * ySign );
                    float height = textures.length > 1 ? size : matrix.length( textures[ 0 ][ 0 ].height ) / factor;

                    for ( int py = 0 ; py < textures.length ; py++ ) {
                        float x = matrix.x( 0 ) + padding.width + matrix.length( state.pageSize.width ) * delta * xSign;

                        for ( int px = 0 ; px < textures[ py ].length ; px++ ) {
                            checkAndDrawSingleImage( level , textures , statuses , py , px , x , y , size , height ,
                                    state.surfaceSize );

                            x += size;
                        }

                        if ( py + 1 == textures.length - 1 ) {
                            height = matrix.length( textures[ py + 1 ][ 0 ].height ) / factor;
                        }

                        y -= height;
                    }
                }
            }
        }
    }

    private void drawSingleImage( final int id , final float x , final float y , final float w , final float h ) {
        GLES10.glBindTexture( GLES10.GL_TEXTURE_2D , id );
        GLES11Ext.glDrawTexfOES( x , y , 0 , w , h );
    }

    /**
     * @return [level][npx,npy]
     */
    int[][] getTextureDimension( final int page ) {
        final PageTextureInfo[][][] info = _pages[ page ].textures;

        final int[][] ret = new int[ _pages[ page ].textures.length ][];

        for ( int level = 0 ; level < info.length ; level++ ) {
            ret[ level ] = new int[] { info[ level ][ 0 ].length , info[ level ].length };
        }

        return ret;
    }

    void prepare( final Context context , final int pages , final int nLevel , final SizeInfo pageSize ,
            final SizeInfo surfaceSize ) {
        _background = TextureInfo.getTiledBitmapInstance( context.getResources() , R.drawable.background , surfaceSize );
        _blank = TextureInfo.getBitmapInstance( context.getResources() , R.drawable.blank );

        _pages = new PageInfo[ pages ];
        for ( int page = 0 ; page < _pages.length ; page++ ) {
            _pages[ page ] = new PageInfo( nLevel , pageSize );
        }
    }

    void render( final CoreImageState state ) {
        // copy for thread consistency
        _immutableMatrix.scale = state.matrix.scale;
        _immutableMatrix.tx = state.matrix.tx;
        _immutableMatrix.ty = state.matrix.ty;
        _immutablePadding.width = state.getHorizontalPadding();
        _immutablePadding.height = state.getVerticalPadding();

        drawBackground();

        final long t = SystemClock.elapsedRealtime();

        drawImage( _immutableMatrix , _immutablePadding , state );

        _fpsCounter++;
        _frameSum += SystemClock.elapsedRealtime() - t;
        if ( _fpsCounter == 120 ) {
            System.err.println( "drawImage FPS: " + 120.0 * 1000 / ( SystemClock.elapsedRealtime() - _fpsTime )
                    + ", avg: " + _frameSum / 120.0 );

            _fpsTime = SystemClock.elapsedRealtime();
            _fpsCounter = 0;
            _frameSum = 0;
        }
    }

    void unbindPageImage( final LoadBitmapTask task ) {
        _pages[ task.page ].statuses[ task.level ][ task.py ][ task.px ] = PageTextureStatus.UNBIND;

        GLES10.glDeleteTextures( 1 , new int[] { _pages[ task.page ].textures[ task.level ][ task.py ][ task.px ].id } ,
                0 );
    }
}
