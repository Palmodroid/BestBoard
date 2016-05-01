package org.lattilad.bestboard.fileselector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.lattilad.bestboard.R;


public class ChooseOne extends Activity 
	{
	private EditText ed1;
	private TextView tx1;
	private EditText ed2;
	private TextView tx2;
	private EditText ed3;
	private TextView tx3;
	private EditText ed4;
	private TextView tx4;
	private EditText ed5;
	private TextView tx5;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
		if (requestCode == 1) 
			{
			if(resultCode == RESULT_OK)
				{
				//String result=data.getStringExtra("RESULT");
				
				//visszateres data reszben
				String result = data.getData().getPath();
				Toast.makeText(this, "File Clicked: " + result, Toast.LENGTH_LONG).show();
				}
			}
		if (resultCode == RESULT_CANCELED) 
			{
	        Toast.makeText(this, "- C A N C E L -", Toast.LENGTH_SHORT).show();
			}
		}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
		{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_one);

		ed1 = (EditText) findViewById(R.id.ed1);
		tx1 =(TextView) findViewById(R.id.tx1);
		ed2 = (EditText) findViewById(R.id.ed2);
		tx2 =(TextView) findViewById(R.id.tx2);
		ed3 = (EditText) findViewById(R.id.ed3);
		tx3 =(TextView) findViewById(R.id.tx3);
		ed4 = (EditText) findViewById(R.id.ed4);
		tx4 =(TextView) findViewById(R.id.tx4);
		ed5 = (EditText) findViewById(R.id.ed5);
		tx5 =(TextView) findViewById(R.id.tx5);

		tx1.setText("Forráskönyvtár:");
		ed1.setText("external_sd/_Hordozo/Android/Blog/Hulyeseg"); //_alfa/Android/Blog/Hulyeseg

		tx2.setText("File-végződés:");
		ed2.setText(".bbcode");
		
		tx3.setVisibility(View.GONE);
		tx4.setVisibility(View.GONE);
		tx5.setVisibility(View.GONE);
		ed3.setVisibility(View.GONE);
		ed4.setVisibility(View.GONE);
		ed5.setVisibility(View.GONE);
		}

	public void DoIt(View view) 
		{
		switch (view.getId()) 
			{
			case R.id.button:

				try
					{
					/*
					 * Ide jön a kipróbálandó programrész. 
					 * Az ed1-ed5 mezőket (és a tx1-tx5 címkéket is) szabadon használhatjuk
					 */
					Intent i = new Intent();
					//i.setClass(this, FileChooser.class);
					i.putExtra(FileChooserActivity.DIRECTORY_SUB_PATH, ed1.getText().toString());
					i.putExtra(FileChooserActivity.FILE_ENDING, ed2.getText().toString());
					
					// implicit intent
					i.setAction(Intent.ACTION_GET_CONTENT);
					i.setType("file/*");
					
					startActivityForResult(i, 1);
					}
				catch (Exception e)
					{
					Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
					}

			break;
			}
		}
	}
