package jp.archilogic.docnext.android;

import jp.archilogic.docnext.android.provider.local.AppStateManager;
import jp.archilogic.docnext.android.provider.local.LocalProvider;
import jp.archilogic.docnext.android.provider.local.LocalProviderImpl;
import jp.archilogic.docnext.android.provider.remote.RemoteProvider;
import jp.archilogic.docnext.android.provider.remote.RemoteProviderImpl;

public class Kernel {
    private static LocalProvider _localProvider;
    private static RemoteProvider _remoteProvider;
    private static AppStateManager _appStateManager;

    public static AppStateManager getAppStateManager() {
        if ( _appStateManager == null ) {
            _appStateManager = new AppStateManager();
        }

        return _appStateManager;
    }

    public static LocalProvider getLocalProvider() {
        if ( _localProvider == null ) {
            _localProvider = new LocalProviderImpl();
        }

        return _localProvider;
    }

    public static RemoteProvider getRemoteProvider() {
        if ( _remoteProvider == null ) {
            _remoteProvider = new RemoteProviderImpl();
        }

        return _remoteProvider;
    }
}
