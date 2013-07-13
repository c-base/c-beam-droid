package org.c_base.c_beam.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.c_base.c_beam.domain.Notification;

import java.util.ArrayList;

public class NotificationsDataSource {
    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    private String[] allColumns = {SQLiteHelper.COLUMN_ID,
            SQLiteHelper.COLUMN_NOTIFICATION};

    public NotificationsDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Notification createNotification(String notification) {
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_NOTIFICATION, notification);
        long insertId = database.insert(SQLiteHelper.TABLE_NOTIFICATIONS, null,
                values);
        Cursor cursor = database.query(SQLiteHelper.TABLE_NOTIFICATIONS,
                allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Notification newNotification = cursorToNotification(cursor);
        cursor.close();
        return newNotification;
    }

    public void deleteNotification(Notification notification) {
        long id = notification.getId();
        database.delete(SQLiteHelper.TABLE_NOTIFICATIONS, SQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public ArrayList<Notification> getAllNotifications() {
        ArrayList<Notification> notifications = new ArrayList<Notification>();

        Cursor cursor = database.query(SQLiteHelper.TABLE_NOTIFICATIONS,
                allColumns, null, null, null, null, "_id DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Notification notification = cursorToNotification(cursor);
            notifications.add(notification);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return notifications;
    }

    public void deleteAllNotifications() {
        database.delete(SQLiteHelper.TABLE_NOTIFICATIONS, null, null);
    }

    private Notification cursorToNotification(Cursor cursor) {
        Notification notification = new Notification();
        notification.setId(cursor.getLong(0));
        notification.setNotification(cursor.getString(1));
        return notification;
    }
}
