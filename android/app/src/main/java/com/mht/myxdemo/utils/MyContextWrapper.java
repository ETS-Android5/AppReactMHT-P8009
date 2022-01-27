package com.mht.myxdemo.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class MyContextWrapper extends android.content.ContextWrapper {

    public MyContextWrapper(Context base) {
        super(base);
    }

    public static Locale newLocale = Locale.ENGLISH;

    public static ContextWrapper wrap(Context context) {

        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            configuration.setLocale(newLocale);
            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            context = context.createConfigurationContext(configuration);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            configuration.setLocale(newLocale);
            context = context.createConfigurationContext(configuration);

        }

        return new ContextWrapper(context);
    }
}
