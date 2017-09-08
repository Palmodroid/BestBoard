package org.lattilad.bestboard.permission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;

import java.util.List;

/**
 *
 */

public class RequestPermissionActivity extends AppCompatActivity
    {
    private final int PERMISSION_REQUEST_CODE = 234;

    Button PermissionDataButton;
    Button PermissionDataSettingsButton;
    TextView PermissionDataOk;

    Button PermissionInputSettingsButton;
    TextView PermissionInputOk;


    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_permission_activity);
        //getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFinishOnTouchOutside(false);

        PermissionDataButton = (Button) (findViewById(R.id.permission_data_button));
        PermissionDataButton.setOnClickListener(new View.OnClickListener()
                {
                @Override
                public void onClick(View view)
                    {
                    ActivityCompat.requestPermissions( RequestPermissionActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);

                    // Set PERMISSION_REQUESTED flag
                    }
                });

        PermissionDataSettingsButton = (Button) (findViewById(R.id.permission_data_settings_button));
        PermissionDataSettingsButton.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                }
            });

        PermissionDataOk = (TextView) (findViewById(R.id.permission_data_ok));


        PermissionInputSettingsButton = (Button) (findViewById(R.id.permission_input_settings_button));
        PermissionInputSettingsButton.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                // per doc activity may not exist
                // intent.resolveActivity(packageManager) can be helpful
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                }
            });

        PermissionInputOk = (TextView) (findViewById(R.id.permission_input_ok));
        }

    @Override
    protected void onResume()
        {
        super.onResume();

        boolean dataEnabled = checkData();

        PermissionDataOk.setVisibility( dataEnabled ? View.VISIBLE : View.GONE );

        // read PERMISSION_REQUESTED flag

        boolean inputEnabled = checkInput();

        PermissionInputSettingsButton.setVisibility( inputEnabled ? View.GONE : View.VISIBLE );
        PermissionInputOk.setVisibility( inputEnabled ? View.VISIBLE : View.GONE );
        }

    boolean checkData()
        {
        return ContextCompat.checkSelfPermission( this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) ==
                PackageManager.PERMISSION_GRANTED;

        // if true, clear PERMISSION_REQUESTED flag
        }

    boolean checkInput()
        {
        Scribe.locus( Debug.PERMISSION );

        InputMethodManager inputMethodManager= (InputMethodManager)(getSystemService( INPUT_METHOD_SERVICE ));
        List<InputMethodInfo> list = inputMethodManager.getEnabledInputMethodList();

        Scribe.debug(Debug.PERMISSION, "Package name of Best Board: " + this.getPackageName() );
        Scribe.debug(Debug.PERMISSION, "Package name of enabbled keyboards: " + this.getPackageName() );

        for (InputMethodInfo info : list )
            {
            Scribe.debug(Debug.PERMISSION, info.getPackageName() );
            if ( info.getPackageName().equals( this.getPackageName()) )
                {
                Scribe.debug(Debug.PERMISSION, "BestBoard is enabled!" );
                return true;
                }
            }

        Scribe.debug(Debug.PERMISSION, "BestBoard is NOT enabled!" );
        return false;
        }
    }

