package org.segin.ttleditor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class ThemeUtils {
    public static final String PREF_THEME = "theme_preference";
    public static final String THEME_HOLO_LIGHT = "holo_light";
    public static final String THEME_HOLO_DARK = "holo_dark";
    public static final String THEME_MATERIAL_LIGHT = "material_light";
    public static final String THEME_MATERIAL_DARK = "material_dark";
    public static final String THEME_DEVICE_DEFAULT = "device_default";

    public static void applyTheme(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String theme = prefs.getString(PREF_THEME, THEME_HOLO_LIGHT);

        int themeId = android.R.style.Theme_Holo_Light_DarkActionBar; // Default fallback

        if (THEME_HOLO_DARK.equals(theme)) {
            themeId = android.R.style.Theme_Holo;
        } else if (THEME_HOLO_LIGHT.equals(theme)) {
            themeId = android.R.style.Theme_Holo_Light_DarkActionBar;
        } else if (THEME_MATERIAL_DARK.equals(theme)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                themeId = android.R.style.Theme_Material;
            } else {
                themeId = android.R.style.Theme_Holo;
            }
        } else if (THEME_MATERIAL_LIGHT.equals(theme)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                themeId = android.R.style.Theme_Material_Light_DarkActionBar;
            } else {
                themeId = android.R.style.Theme_Holo_Light_DarkActionBar;
            }
        } else if (THEME_DEVICE_DEFAULT.equals(theme)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                themeId = android.R.style.Theme_DeviceDefault;
            } else {
                 themeId = android.R.style.Theme_Holo;
            }
        }

        activity.setTheme(themeId);
    }
}
