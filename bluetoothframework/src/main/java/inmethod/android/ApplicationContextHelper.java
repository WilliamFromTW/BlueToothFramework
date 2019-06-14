package inmethod.android;

import android.app.Application;
import android.content.Context;

/**
 * <pre>
 * {@code
How to use:
edit AndroidManifest.xml
put this class in
application. xml tag
ex:
<application
android:name="inmethod.android.ApplicationContextHelper"
android:icon="@drawable/ic_launcher"
android:label="@string/app_name"
android:theme="@style/AppTheme" >
}
 * @author william chen
 *
 *</pre>
 */
public class ApplicationContextHelper extends Application {
    private static Context mContext;

    public void onCreate(){
        super.onCreate();
        this.mContext = this;
    }
    public static Context getContext(){
        return mContext;
    }
}
