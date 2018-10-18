package mdg.com.imagecolorizer;

import android.content.Context;
import android.content.SharedPreferences;

public class PermissionUtil {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PermissionUtil(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.permission_preference), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void updatePermissionPreference(String permission){

        switch (permission){
            case "storage" :
                editor.putBoolean(context.getString(R.string.permission_storage), true);
                editor.commit();
                break;
        }
    }

    public boolean checkPermissionPreference(String permission){

        boolean isShown = false;

        switch (permission){
            case "storage":
                isShown = sharedPreferences.getBoolean(context.getString(R.string.permission_storage),false);
                break;
        }

        return  isShown;
    }
}
