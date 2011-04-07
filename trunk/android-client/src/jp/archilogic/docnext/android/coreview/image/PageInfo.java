package jp.archilogic.docnext.android.coreview.image;

import jp.archilogic.docnext.android.info.SizeInfo;

public class PageInfo {
    enum PageTextureStatus {
        UNBIND , BIND;
    }

    private static final int TEXTURE_SIZE = 512;

    int width;
    int height;
    PageTextureInfo[][][] textures;
    PageTextureStatus[][][] statuses;

    PageInfo( final int nLevel , final SizeInfo page ) {
        textures = new PageTextureInfo[ nLevel ][][];
        statuses = new PageTextureStatus[ nLevel ][][];
        for ( int level = 0 ; level < nLevel ; level++ ) {
            final int factor = ( int ) Math.pow( 2 , level );
            final int nx = ( page.width * factor - 1 ) / TEXTURE_SIZE + 1;
            final int ny = ( page.height * factor - 1 ) / TEXTURE_SIZE + 1;
            textures[ level ] = new PageTextureInfo[ ny ][ nx ];
            statuses[ level ] = new PageTextureStatus[ ny ][ nx ];

            for ( int py = 0 ; py < ny ; py++ ) {
                for ( int px = 0 ; px < nx ; px++ ) {
                    final int x = px * TEXTURE_SIZE;
                    final int y = py * TEXTURE_SIZE;

                    textures[ level ][ py ][ px ] =
                            PageTextureInfo.getInstance( Math.min( page.width * factor - x , TEXTURE_SIZE ) ,
                                    Math.min( page.height * factor - y , TEXTURE_SIZE ) , x , y );
                    statuses[ level ][ py ][ px ] = PageTextureStatus.UNBIND;
                }
            }
        }
    }
}
