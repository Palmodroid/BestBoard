package org.lattilad.bestboard.permission;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.lattilad.bestboard.R;

/**
 *
 */

public class RequestPermissionActivity extends AppCompatActivity
    {
    private static final int TEST_SELECTOR_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_permission_activity);
        //getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFinishOnTouchOutside(false);

        }

    }

