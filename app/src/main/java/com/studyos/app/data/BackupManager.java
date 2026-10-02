package com.studyos.app.data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Backup / restore = copying the SQLite database file. Both calls must run off the main thread. */
public final class BackupManager {
    private static final String SQLITE_HEADER = "SQLite format 3";

    private BackupManager() {}

    public static void export(Context context, Uri target) throws IOException {
        AppDatabase db = AppDatabase.get(context);
        Cursor c = db.getOpenHelper().getWritableDatabase().query("PRAGMA wal_checkpoint(FULL)");
        c.close();
        File file = context.getDatabasePath(AppDatabase.NAME);
        try (InputStream in = new FileInputStream(file);
             OutputStream out = context.getContentResolver().openOutputStream(target)) {
            if (out == null) throw new IOException("Cannot open destination");
            copy(in, out);
        }
    }

    /** Replaces the current database with the backup at source, then restarts the app. */
    public static void restore(Context context, Uri source) throws IOException {
        File tmp = new File(context.getCacheDir(), "restore.db");
        try (InputStream in = context.getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(tmp)) {
            if (in == null) throw new IOException("Cannot open backup file");
            copy(in, out);
        }
        try (InputStream check = new FileInputStream(tmp)) {
            byte[] header = new byte[SQLITE_HEADER.length()];
            int read = check.read(header);
            if (read != header.length || !SQLITE_HEADER.equals(new String(header, StandardCharsets.US_ASCII))) {
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
                throw new IOException("This file is not a Study OS backup");
            }
        }
        AppDatabase.closeInstance();
        File db = context.getDatabasePath(AppDatabase.NAME);
        //noinspection ResultOfMethodCallIgnored
        new File(db.getPath() + "-wal").delete();
        //noinspection ResultOfMethodCallIgnored
        new File(db.getPath() + "-shm").delete();
        try (InputStream in = new FileInputStream(tmp); OutputStream out = new FileOutputStream(db)) {
            copy(in, out);
        }
        //noinspection ResultOfMethodCallIgnored
        tmp.delete();

        Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(launch);
        }
        Runtime.getRuntime().exit(0);
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[16 * 1024];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        out.flush();
    }
}
