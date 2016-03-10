package net.cosmoway.sesame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetIntentReceiver extends BroadcastReceiver {
    public static int clickCount = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("UPDATE_WIDGET")) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // テキストをクリック回数を元に更新
            remoteViews.setTextViewText(R.id.title, "クリック回数: " + WidgetIntentReceiver.clickCount);

            SesameWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
        }
    }
}
