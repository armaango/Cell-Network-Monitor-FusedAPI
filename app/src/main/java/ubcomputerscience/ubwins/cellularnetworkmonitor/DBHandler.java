

/**
 *   UbWins Lab
 *   University at Buffalo, The State University of New York.
 *
 */
package ubcomputerscience.ubwins.cellularnetworkmonitor;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class DBHandler extends SQLiteOpenHelper
{
    private static  String dbName="mainTuple";
    private static int version=1;
    static final String TAG = "[CELNETMON-DBHANDLER]";

    private static final String schema = "CREATE TABLE cellRecords (LAT DATA, LONG DATA, LOCALITY DATA, CITY DATA, STATE DATA, COUNTRY DATA, NETWORK_PROVIDER DATA, TIMESTAMP DATA, NETWORK_TYPE DATA, NETWORK_STATE DATA, NETWORK_RSSI DATA, DATA_STATE DATA, DATA_ACTIVITY DATA)";

    public DBHandler(Context context)

    {
        super(context, dbName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(schema);
        Log.v(TAG, "CREATING DB " + dbName + "WITH TABLE cellRecords");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int obsolete, int latest)
    {
        db.execSQL("DROP TABLE IF EXISTS cellRecords");
        onCreate(db);
    }

}
