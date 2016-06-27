/**
 *   UbWins Lab
 *   University at Buffalo, The State University of New York.
 *
 */
package ubcomputerscience.ubwins.cellularnetworkmonitor;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;


public class CellularDataRecorder
{

    static final String TAG = "[CELNETMON-CDR]";

    public String getLocalTimeStamp()
    {
        Log.v(TAG, "inside getLocalTimeStamp");
        Long timeStamp = System.currentTimeMillis() / 1000;
        String ts = timeStamp.toString();
        return ts;
    }
    public String getCurrentDataState(TelephonyManager telephonyManager)
    {
        Log.v(TAG,"inside getDataState");
        int state = telephonyManager.getDataState();
        String dataState = "Unknown";
        if(state == 0)
        {
            dataState = "Disconnected";
        }
        else if (state == 1)
        {
            dataState = "Connecting";
        }
        else if(state == 2)
        {
            dataState = "Connected";
        }
        else if(state == 3)
        {
            dataState = "Suspended";
        }
        return dataState;
    }

    public String getCurrentDataActivity(TelephonyManager telephonyManager)
    {
        Log.v(TAG,"inside getCurrentDataActivity");
        int activity = telephonyManager.getDataActivity();
        String dataActivity = "Unknown";
        if(activity == 0)
        {
            dataActivity = "None";
        }
        else if (activity == 1)
        {
            dataActivity = "IN";
        }
        else if(activity == 2)
        {
            dataActivity = "OUT";
        }
        else if(activity == 3)
        {
            dataActivity = "IN_OUT";
        }
        else if(activity == 4)
        {
            dataActivity = "Dormant";
        }
        return dataActivity;
    }

    public String getCellularInfo(TelephonyManager telephonyManager)
    {
        Log.v(TAG, "inside getCellularInfo");
        String cellularInfo = "";
        String log = "";

        for (final CellInfo info : telephonyManager.getAllCellInfo()) {
            if (info instanceof CellInfoGsm) {
                log += "GSM@";
                CellIdentityGsm gsm_cell = ((CellInfoGsm) info).getCellIdentity();
                log += gsm_cell.getCid() + "#" + gsm_cell.getLac() + "#" + gsm_cell.getMcc() + "#" + gsm_cell.getMnc() + "_";

                final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                log += gsm.getDbm() + "#" + gsm.getLevel();
            } else if (info instanceof CellInfoCdma) {
                log += "CDMA@";
                CellIdentityCdma cdma_cell = ((CellInfoCdma) info).getCellIdentity();
                log += cdma_cell.getBasestationId() + "#" + cdma_cell.getNetworkId() + "#" + cdma_cell.getSystemId() + "#" + cdma_cell.getSystemId() + "_";

                final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                log += cdma.getDbm() + "#" + cdma.getLevel();
            } else if (info instanceof CellInfoLte) {
                log += "LTE@";
                CellIdentityLte lte_cell = ((CellInfoLte) info).getCellIdentity();
                log += lte_cell.getCi() + "#" + lte_cell.getPci() + "#" + lte_cell.getMcc() + "#" + lte_cell.getMnc() + "_";

                final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                log += lte.getDbm() + "#" + lte.getLevel();
            } else if (info instanceof CellInfoWcdma) {
                log += "WCDMA@";
                CellIdentityWcdma wcdma_cell = ((CellInfoWcdma) info).getCellIdentity();
                log += wcdma_cell.getCid() + "#" + wcdma_cell.getLac() + "#" + wcdma_cell.getMcc() + "#" + wcdma_cell.getMnc() + "_";

                final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                log += wcdma.getDbm() + "#" + wcdma.getLevel();
            } else {
                Log.v(TAG, "Unknown Network Type");
            }
        }
        cellularInfo = log;
        return cellularInfo;
    }
}
