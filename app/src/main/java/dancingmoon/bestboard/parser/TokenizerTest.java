package dancingmoon.bestboard.parser;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.R;
import dancingmoon.bestboard.scribe.Scribe;

public class TokenizerTest extends Activity
	{
	private TextView text;
	private EditText edit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		
		// primary scribe is started as normal
		Debug.initScribe(this);
		// Ignition.start( this ); need not be called,
		// because TokenizerTest do not use any preferences.

        setContentView(R.layout.tokenizer_test);

		text = (TextView) findViewById( R.id.text );
		edit = (EditText) findViewById( R.id.edit );
		
		findViewById( R.id.button ).setOnClickListener( new View.OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
                // secondary is set for tokenizer temporarily
                // no parsing during TokenizerTest!
                Bundle scribeConfig = Scribe.getConfigSecondary();
                Scribe.enableFileLogSecondary( Debug.tokenLogFileName );
                // Delete previous tokens from log file
				Scribe.clear_secondary();
                Scribe.enableSysLogSecondary( Debug.LOG_TAG_TOKEN );
				Scribe.disableTimeStampSecondary();

				StringReader reader = new StringReader( edit.getText().toString() );
				
				text.setText("Tokens:\n");
				try
					{
					Tokenizer tokenizer = new Tokenizer( TokenizerTest.this, reader );
					int type;
					
					while (true)
						{
						type = tokenizer.nextToken();
						
						if ( type == Tokenizer.TYPE_EOF )
							{
							text.setText( text.getText() + "EOF");
							break;
							}
						else if ( type == Tokenizer.TYPE_EOL )
							{
							text.setText( text.getText() + "EOL\n");
							}
						else if ( type == Tokenizer.TYPE_START )
							{
							text.setText( text.getText() + "\nSTART ");
							}
						else if ( type == Tokenizer.TYPE_END )
							{
							text.setText( text.getText() + "END\n");
							}
						else if (type == Tokenizer.TYPE_STRING )
							{
							text.setText( text.getText() + 
								"String: " + tokenizer.getStringToken() + "; ");
							Scribe.note_secondary( "String: " + tokenizer.getStringToken() );
							}
						else if (type == Tokenizer.TYPE_KEYWORD )
							{
							text.setText(text.getText() +
									"Keyword: " + tokenizer.getStringToken() + " = 0x" + Long.toHexString(tokenizer.getIntegerToken()) + "L; ");
//							Scribe.note_secondary("Keyword: " + tokenizer.getStringToken() + " = 0x" + Long.toHexString(tokenizer.getIntegerToken()) + "L");

							// To use directly in Commands.java - TimeStamp is disabled, too
							Scribe.note_secondary("    public static final long TOKEN_" +
									tokenizer.getStringToken().toUpperCase(Locale.US) +
									" = 0x" + Long.toHexString( tokenizer.getIntegerToken() ) + "L;");

                            Tokenizer.regenerateKeyword( tokenizer.getIntegerToken() );
							}
						else if (type == Tokenizer.TYPE_INTEGER )
							{
							text.setText( text.getText() + 
								"Long: " + tokenizer.getStringToken() + " = " + tokenizer.getIntegerToken() + " ("+ Long.toHexString(tokenizer.getIntegerToken()).toUpperCase( Locale.US ) + "h); ");
							Scribe.note_secondary("Long: " + tokenizer.getStringToken() + " = " + tokenizer.getIntegerToken() + " ("+ Long.toHexString(tokenizer.getIntegerToken()).toUpperCase( Locale.US ) + "h)");
							}
						else if (type == Tokenizer.TYPE_FRACTION )
							{
							text.setText( text.getText() + 
								"Double: " + tokenizer.getStringToken() + " = " + tokenizer.getDoubleToken() + "; ");
							Scribe.note_secondary("Double: " + tokenizer.getStringToken() + " = " + tokenizer.getDoubleToken() );
							}
						else if (type == Tokenizer.TYPE_CHARACTER )
							{
							text.setText( text.getText() + 
								"Character: [" + tokenizer.getStringToken() + "] = " + Long.toHexString( tokenizer.getIntegerToken() ) + "; ");
							Scribe.note_secondary("Character: [" + tokenizer.getStringToken() + "] = " + Long.toHexString( tokenizer.getIntegerToken() ));
							}
						else
							{
							text.setText( text.getText() + "Unknown type; ");
							Scribe.error_secondary("Unknown type; ");
							}
						}
					} 
				catch (IOException e)
					{
					text.setText( text.getText() + "READ ERROR: " + e.toString());
					Scribe.error_secondary("READ ERROR: " + e.toString());
					}

                Scribe.setConfigSecondary( scribeConfig );
				}
			});
		}

	}
