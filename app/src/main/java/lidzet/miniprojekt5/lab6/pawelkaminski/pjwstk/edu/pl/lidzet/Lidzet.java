package lidzet.miniprojekt5.lab6.pawelkaminski.pjwstk.edu.pl.lidzet;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class Lidzet extends AppWidgetProvider {

    static MediaPlayer mp;
    static List<List<Object>> playlist;
    static Integer nowPlayingId;
    static Integer leftImgId;
    static Integer rightImgId;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lidzet);

        Log.e("URZA_MP", "Called UPDATE");
        Log.e("URZA_MP", "Img IDs L/R:" + leftImgId + "/" + rightImgId);

        // WWW
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setData(Uri.parse("http://www.przemo.org/phpBB2/forum/"));
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.btn_broadcast, pi);

        // MP3
        i = new Intent(context, getClass());
        i.setAction("mp_play_pause");
        pi = PendingIntent.getBroadcast(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.btn_go_play, pi);

        i = new Intent(context, getClass());
        i.setAction("mp_stop");
        pi = PendingIntent.getBroadcast(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.btn_go_stop, pi);

        i = new Intent(context, getClass());
        i.setAction("mp_next");
        pi = PendingIntent.getBroadcast(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.btn_go_next, pi);

        i = new Intent(context, getClass());
        i.setAction("mp_prev");
        pi = PendingIntent.getBroadcast(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.btn_go_prev, pi);

        i = new Intent(context, getClass());
        i.setAction("img_left");
        pi = PendingIntent.getBroadcast(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.btn_image1, pi);

        i = new Intent(context, getClass());
        i.setAction("img_right");
        pi = PendingIntent.getBroadcast(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.btn_image2, pi);

        // Update Now Playing
        views.setTextViewText(R.id.now_playing, (String)playlist.get(nowPlayingId).get(1));

        if(mp.isPlaying())
            views.setImageViewResource(R.id.btn_go_play, R.drawable.ic_pause);
        else
            views.setImageViewResource(R.id.btn_go_play, R.drawable.ic_play);

        if(leftImgId > 0)
        {
            views.setImageViewResource(R.id.view_image1, getImgRes(leftImgId));
        }

        if(rightImgId > 0)
        {
            views.setImageViewResource(R.id.view_image2, getImgRes(rightImgId));
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        playlist = new ArrayList<>();
        mp  = new MediaPlayer();
        nowPlayingId = 0;
        leftImgId = 0;
        rightImgId = 0;

        addTrack(R.raw.amiga, "X-Out");
        addTrack(R.raw.blondie, "Blondie");
        addTrack(R.raw.hammer, "Jan Hammer");

        resetTrack(context);

        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        Log.e("URZA_MP", "Action: " + intent.getAction());
        Boolean doPlay = mp.isPlaying();
        Random rn = new Random();

        switch (intent.getAction())
        {
            case "img_left":
                leftImgId = rn.nextInt(6) + 1;
                updateAll(context);
                break;

            case "img_right":
                rightImgId = rn.nextInt(6) + 1;
                updateAll(context);
                break;

            case "mp_play_pause":
                if(mp.isPlaying()) mp.pause(); else mp.start();
                updateAll(context);
                break;

            case "mp_pause":
                mp.pause();
                updateAll(context);
                break;

            case "mp_stop":
                resetTrack(context);
                updateAll(context);
                break;

            case "mp_prev":
                if(--nowPlayingId < 0) nowPlayingId = (playlist.size() - 1);
                resetTrack(context);
                if(doPlay) mp.start();
                updateAll(context);
                break;

            case "mp_next":
                if(++nowPlayingId >= playlist.size()) nowPlayingId = 0;
                resetTrack(context);
                if(doPlay) mp.start();
                updateAll(context);
                break;
        }
    }

    private void addTrack(Integer rawId, String name)
    {
        ArrayList k = new ArrayList<Object>();
        k.add(0, rawId);
        k.add(1, name);
        playlist.add(k);
        Log.e("URZA_MP", "addTrack ListSize: " + playlist.size());
    }

    private void updateAll(Context c)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(c);
        ComponentName widgetComponent = new ComponentName(c, getClass());
        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        Intent update = new Intent();
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        c.sendBroadcast(update);
    }

    private void resetTrack(Context c)
    {
        Log.e("URZA_MP", "resetTrack ListSize: " + playlist.size());

        try
        {
            AssetFileDescriptor afd = c.getResources().openRawResourceFd((int)playlist.get(nowPlayingId).get(0));
            mp.reset();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            afd.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Integer getImgRes(Integer i)
    {
        Integer r = -1;
        switch (i)
        {
            case 1:
                r = R.raw.pic1;
                break;
            case 2:
                r = R.raw.pic2;
                break;
            case 3:
                r = R.raw.pic3;
                break;
            case 4:
                r = R.raw.pic4;
                break;
            case 5:
                r = R.raw.pic5;
                break;
            case 6:
                r = R.raw.pic6;
                break;
        }

        return r;
    }


}

