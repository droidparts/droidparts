package org.droidparts.backup;

import android.app.backup.FileBackupHelper;
import android.content.Context;

public class DbBackupHelper extends FileBackupHelper {

	public DbBackupHelper(Context ctx, String dbName) {
		super(ctx, ctx.getDatabasePath(dbName).getAbsolutePath());
	}
}
