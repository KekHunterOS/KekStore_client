package com.team420.kekstore.compat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.team420.kekstore.BuildConfig;
import com.team420.kekstore.Utils;
import com.team420.kekstore.installer.PrivilegedInstaller;

/**
 * Starting with 7.0 (API 24), we're using PackageInstaller APIs
 * to install and uninstall apps via the privileged extension.
 * That enforces the uninstaller being the same as the installer,
 * so set the package name to privileged extension if it is being used.
 * <p>
 * While setting the installer package name, many problems can occur,
 * such as:
 * <li> App wasn't installed due to incompatibility
 * <li> User canceled install
 * <li> Another app interfered in the process
 * <li> Another app already set the target's installer package,
 * which happens in the case where we fell back to {@link com.team420.kekstore.installer.DefaultInstaller}
 */
public class PackageManagerCompat {

    private static final String TAG = "PackageManagerCompat";

    public static void setInstaller(Context context, PackageManager mPm, String packageName) {
        try {
            if (Build.VERSION.SDK_INT >= 24 && PrivilegedInstaller.isDefault(context)) {
                mPm.setInstallerPackageName(packageName, PrivilegedInstaller.PRIVILEGED_EXTENSION_PACKAGE_NAME);
            } else {
                mPm.setInstallerPackageName(packageName, BuildConfig.APPLICATION_ID);
            }
            Utils.debugLog(TAG, "Installer package name for " + packageName + " set successfully");
        } catch (SecurityException | IllegalArgumentException e) {
            Log.e(TAG, "Could not set installer package name for " +
                    packageName, e);
        }
    }

}
