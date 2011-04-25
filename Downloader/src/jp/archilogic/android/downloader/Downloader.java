package jp.archilogic.android.downloader;

import java.io.Serializable;
import java.util.List;

import jp.archilogic.android.downloader.internal.DownloadService;
import android.content.Context;
import android.content.Intent;

import com.google.common.collect.Lists;

/**
 * Download facade class. Do download files in Serial, and received list order.
 */
public class Downloader {
    private static final String PREFIX = Downloader.class.getName();

    /**
     * Dispatch when any single download finished. Dispatched with EXTRA_EXTRA
     */
    public static final String ACTION_DOWNLOADED = PREFIX + ".action.downloaded";
    /**
     * Dispatch when all download completed
     */
    public static final String ACTION_COMPLETED = PREFIX + ".action.completed";
    /**
     * Dispatch when download stopped (and waiting for resume). Dispatched with EXTRA_EXTRA, EXTRA_ERROR
     */
    public static final String ACTION_STOPPED = PREFIX + ".action.stopped";
    /**
     * Dispatch when download resumed
     */
    public static final String ACTION_RESUMED = PREFIX + ".action.resumed";
    /**
     * Dispatch when error ocuured. Dispatched with EXTRA_EXTRA, EXTRA_ERROR
     */
    public static final String ACTION_ERROR = PREFIX + ".action.error";

    /**
     * Key to DownloadInfo's extra (as Bundle)
     */
    public static final String EXTRA_EXTRA = PREFIX + ".extra.extra";
    /**
     * Key to DownloadError (as Serializable)
     */
    public static final String EXTRA_ERROR = PREFIX + ".extra.error";

    /**
     * Start download
     * 
     * @param context
     *            Context to start service
     * @param info
     *            single DownloadInfo
     */
    public static void start( final Context context , final DownloadInfo info ) {
        start( context , info , new DownloadOption() );
    }

    /**
     * Start download
     * 
     * @param context
     *            Context to start service
     * @param info
     *            single DownloadInfo
     * @param option
     *            Option
     */
    public static void start( final Context context , final DownloadInfo info , final DownloadOption option ) {
        start( context , Lists.newArrayList( info ) , option );
    }

    /**
     * Start download
     * 
     * @param context
     *            Context to start service
     * @param infos
     *            multiple DownloadInfo
     */
    public static void start( final Context context , final List< DownloadInfo > infos ) {
        start( context , infos , new DownloadOption() );
    }

    /**
     * Start download
     * 
     * @param context
     *            Context to start service
     * @param infos
     *            multiple DownloadInfo
     * @param option
     *            Option
     */
    public static void start( final Context context , final List< DownloadInfo > infos , final DownloadOption option ) {
        if ( !( infos instanceof Serializable ) ) {
            throw new IllegalArgumentException( "Infos should be Serializable" );
        }

        if ( infos == null || infos.isEmpty() ) {
            throw new IllegalArgumentException( "Illegal infos" );
        }

        context.startService( new Intent( context , DownloadService.class ) //
                .putExtra( DownloadService.EXTRA_INFOS , ( Serializable ) infos ) //
                .putExtra( DownloadService.EXTRA_OPTION , option ) );
    }
}
