package org.lndroid.framework;

import android.content.Context;

public interface IDaoConfig {
    Context getContext();
    String getFilesPath();
    String getDataPath();
    String getDatabaseName();
    String getDatabasePath();
    String getPasswordFileName();
    String getLndDirName();
    IKeyStore getKeyStore();
}
