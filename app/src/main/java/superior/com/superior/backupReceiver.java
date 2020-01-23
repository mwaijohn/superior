package superior.com.superior;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class backupReceiver extends BroadcastReceiver {
    public backupReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context,UpdateDB.class);
        context.startService(intent1);
    }
}
