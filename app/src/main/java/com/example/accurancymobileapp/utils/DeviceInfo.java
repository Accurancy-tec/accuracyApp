package com.example.accurancymobileapp.utils;

import android.os.Build;

import java.util.Locale;

/** Name of this phone, sent on login so it shows up in the connected devices list. */
public final class DeviceInfo {

    private DeviceInfo() {}

    /** "Samsung SM-A546E · Android 14". Models that already start with the brand do not repeat it. */
    public static String name() {
        String brand = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.trim();
        String model = Build.MODEL == null ? "" : Build.MODEL.trim();

        String name;
        if (brand.isEmpty() || model.toLowerCase(Locale.ROOT).startsWith(brand.toLowerCase(Locale.ROOT))) {
            name = model;
        } else {
            name = brand.substring(0, 1).toUpperCase(Locale.ROOT) + brand.substring(1) + " " + model;
        }
        name = name.trim();
        if (name.isEmpty()) name = "Celular Android";
        return name + " · Android " + Build.VERSION.RELEASE;
    }
}
