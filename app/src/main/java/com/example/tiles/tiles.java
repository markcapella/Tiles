// *********************************************************************************************************************
// ***                                                                                                               ***
// ***                Tiles                                                                                         ***
// ***                                                                                                               ***
// *** TWiG Software Services, by Mark James Capella                                                                 ***
// ***                                                                                                               ***
// ***    From my original book : Games Apples Play (c) 1983                                                         ***
// ***                                                                                                               ***
// ***    1.1  - Game project copied from Transitions skeleton, background, title, buttons designed                  ***
// ***    1.2  - Game is playable with numbered tiles, maybe not solvable                                            ***
// ***    1.3  - Final version confirmed playable / solvable.                                                        ***
// ***    1.4  - Picky picky code cleanup                                                                            ***
// ***    1.5  - Added checkbox for Show Help on Startup, set default to TRUE                                        ***
// ***    1.6  - Tweaks to code                                                                                      ***
// ***                                                                                                               ***
// *********************************************************************************************************************

package com.example.tiles;

// Standard

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;

import java.util.Random;

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** Main game activity, definitions, etc                                                                          ***
// ***                                                                                                               ***
// *********************************************************************************************************************

public class tiles extends Activity {

	// who we are
   final Context context = this;
   final Random random = new Random();

   // debug flag, set to true to enable toast / flow / status messages
   public static final boolean twigDebug = true;

   // Board and related
   public static final int BOARD_SIZE_X = 4; // Game Board Size X
   public static final int BOARD_SIZE_Y = 4; // Game Board Size Y
   public static final int BOARD_SIZE_N = BOARD_SIZE_X * BOARD_SIZE_Y;

   public static final int BOARD_EMPTY = 0;
   public static final int BOARD_SCRAMBLER = 200;  // Move this many random times

   private int[] persBoard = new int[BOARD_SIZE_N];

   // Define programs persistent data
   // From time the game is installed
   static final boolean init_helpFlg = true;
   private boolean pers_helpFlg;

	// Define programs persistent data
	// From time the game starts to time the game ends
   static final int init_gameStatus = 0;
   private int pers_gameStatus;

	// Load default board bitmap
   private Bitmap defaultBMP;
   private int defaultBMP_X;
	private int defaultBMP_Y;

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** http://developer.android.com/reference/android/app/Activity.html#onCreate%28android.os.Bundle%29              ***
// ***                                                                                                               ***
// *********************************************************************************************************************

   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
		log("onCreate()");

      setContentView(R.layout.tiles);

		onCreate_init (savedInstanceState);
    }

// *********************************************************************************************************************
// *** Disable the hardware back button, not needed for the game                                                     ***
// *********************************************************************************************************************

	public void onBackPressed() {
		log("onBackPressed()");
	}

// *********************************************************************************************************************
// *** Load persistent program values at onCreate, during cold or warm starts                                        ***
// *********************************************************************************************************************

   void onCreate_init(Bundle savedInstanceState) {
      log("onCreate()");

	  	defaultBMP = BitmapFactory.decodeResource(getResources(), R.drawable.numberswhite);
   	defaultBMP_X = defaultBMP.getWidth() / BOARD_SIZE_X;
		defaultBMP_Y = defaultBMP.getHeight() / BOARD_SIZE_Y;

      if (savedInstanceState == null) {
         onCreate_init_cold();
			if (pers_helpFlg)
				do_help();
      } else {
         onCreate_init_warm();
      }

		displayBoard();
	}

// *********************************************************************************************************************
// *** Initialize data that needs to be persistent across swap outs by Android Operating System                      ***
// *********************************************************************************************************************

   public void onCreate_init_cold() {
		log("onCreate_init_cold()");

      SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);

		// Prefs reset first time game is run after install
      pers_helpFlg = prefs.getBoolean("pers_helpFlg", init_helpFlg);

      // Prefs reset every-time new game started
      pers_gameStatus = init_gameStatus;

		// Preset the board
      for (int i = 0; i < BOARD_SIZE_N; i++)
        	persBoard[i] = i;

		// Scramble the board
		while (isGameOver()) {
			int i = 0;
   	   while (i < BOARD_SCRAMBLER) {
				int j = random.nextInt(BOARD_SIZE_N);
				if (moveTheTile(j))
					i++;
			}
		}
	}

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** Restore data that needs to be persistent across swap outs by Android Operating System                         ***
// ***                                                                                                               ***
// *********************************************************************************************************************

   public void onCreate_init_warm() {
		log("onCreate_init_warm()");

      SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);

      // Prefs reloaded every-time game resumes play
      pers_helpFlg = prefs.getBoolean("pers_helpFlg", init_helpFlg);
      pers_gameStatus = prefs.getInt("pers_gameStatus", init_gameStatus);

      for (int i = 0; i < BOARD_SIZE_N; i++)
        	persBoard[i] = prefs.getInt("persBoard_" + i, BOARD_EMPTY);
   }

// *********************************************************************************************************************
// *** Display the board                                                                                             ***
// *********************************************************************************************************************

	public void displayBoard() {
		log("displayBoard()");

		String msg = "TilesBOARD ";
		for (int i = 0; i < BOARD_SIZE_N; i++) {
				msg += persBoard[i];
				setButtonImage(i);
			}
		log(msg);
	}

// *********************************************************************************************************************
// *** Inflate the Android standard hardware menu button View                                                        ***
// *********************************************************************************************************************

    public boolean onCreateOptionsMenu(Menu menu) {
		log("onCreateOptionsMenu()");

		getMenuInflater().inflate(R.menu.tiles, menu);
      return super.onCreateOptionsMenu(menu);
    }

// *********************************************************************************************************************
// *** Process user select menu View items                                                                           ***
// *********************************************************************************************************************

    public boolean onOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelected()");

        switch (item.getItemId()) {

            case R.id.menu_help:
                do_help();
                break;

            case R.id.menu_about:
                do_about();
                break;

            case R.id.menu_exit:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

// *********************************************************************************************************************
// *** Display / Process the About Screen menu View item                                                                           ***
// *********************************************************************************************************************

   void do_about() {
		log("do_about()");

      String aboutMsg = context.getString(R.string.app_name) + "\n";
      try {
         aboutMsg += getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n";
      } catch (NameNotFoundException e) {
   	}
      aboutMsg += "\n" + context.getString(R.string.aboutalert_message);

      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
      alertDialogBuilder.setTitle(R.string.aboutalert_title)
              			   .setMessage(aboutMsg)
                			.setPositiveButton(R.string.aboutalert_button_OK,
                        						 new DialogInterface.OnClickListener() {
                            						 public void onClick(DialogInterface dialog, int id) { }
                        						 })
								.setCancelable(true);

		alertDialogBuilder.create().show();
   }

// *********************************************************************************************************************
// *** Display / Process 1 of 3 Help Screen menu View items                                                          ***
// *********************************************************************************************************************

	void do_help() {
		log("do_help()");

		// Setup Helpbox's checkbox and listener
		View checkBoxView = View.inflate(this, R.layout.checkbox, null);
		CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
		checkBox.setChecked(pers_helpFlg);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	   	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				pers_helpFlg = isChecked;
			}
		});

   	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
      alertDialogBuilder.setTitle(R.string.helpalert_title);
      alertDialogBuilder.setMessage(R.string.helpalert_message1)
      						.setView(checkBoxView)
                        .setNegativeButton(R.string.helpalert_button_MORE,
                                           new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
                                                 do_help2();
                                              }
                                           })
                        .setPositiveButton(R.string.helpalert_button_PLAY,
                                           new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) { }
                                           })
                        .setCancelable(true);

      alertDialogBuilder.create().show();
    }

// *********************************************************************************************************************
// *** Display / Process 2 of 3 Help Screen menu View items                                                          ***
// *********************************************************************************************************************

	void do_help2() {

		// Setup Helpbox's checkbox and listener
		View checkBoxView = View.inflate(this, R.layout.checkbox, null);
		CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
		checkBox.setChecked(pers_helpFlg);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	   	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				pers_helpFlg = isChecked;
			}
		});

   	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
      alertDialogBuilder.setTitle(R.string.helpalert_title);
      alertDialogBuilder.setMessage(R.string.helpalert_message2)
      						.setView(checkBoxView)
                        .setPositiveButton(R.string.helpalert_button_PLAY,
                                           new DialogInterface.OnClickListener() {
                                               public void onClick(DialogInterface dialog, int id) { }
                                           })
                        .setCancelable(true);

		alertDialogBuilder.create().show();
	}

// *********************************************************************************************************************
// *** Move the button selected if possible                                                                          ***
// *********************************************************************************************************************

	public void moveButton(View button) {
		log("moveButton()");

		int buttonIndex = indexOfView(button);
		moveTheTile(buttonIndex);

		if (isGameOver())
			alertGameIsWon();
	}
	
// *********************************************************************************************************************
// *** Return where a specific button could move to                                                                  ***
// *********************************************************************************************************************

	public Boolean moveTheTile(int start) {
		log("moveTheTile(" + start + ")");

		// Can't move the empty square
		if (persBoard[start] == BOARD_EMPTY)
			return false;

		// Get our X, Y and look around for empty tile
		int start_X = start / BOARD_SIZE_X;
		int start_Y = start - (start_X * BOARD_SIZE_X);

		if (start_X - 1 >= 0) {
			int end = (start_X - 1) * BOARD_SIZE_X + start_Y;
			if (persBoard[end] == BOARD_EMPTY) {
				log("Moving up");
				swapTiles(start, end);
				return true;
			}
		}

		if (start_X + 1 < BOARD_SIZE_X) {
			int end = (start_X + 1) * BOARD_SIZE_X + start_Y;
			if (persBoard[end] == BOARD_EMPTY) {
				log("Moving down");
				swapTiles(start, end);
				return true;
			}
		}

		if (start_Y - 1 >= 0) {
			int end = start_X * BOARD_SIZE_X + start_Y - 1;
			if (persBoard[end] == BOARD_EMPTY) {
				log("Moving left");
				swapTiles(start, end);
				return true;
			}
		}

		if (start_Y + 1 < BOARD_SIZE_Y) {
			int end = start_X * BOARD_SIZE_X + start_Y + 1;
			if (persBoard[end] == BOARD_EMPTY) {
				log("Moving right");
				swapTiles(start, end);
				return true;
			}
		}

		return false;
	}

// *********************************************************************************************************************
// *** Swap two button positions                                                                                     ***
// *********************************************************************************************************************

	public void swapTiles(int start, int end) {
		log("performSwap()");

		int t = persBoard[start];
		persBoard[start] = persBoard[end];
		persBoard[end] = t;
		setButtonImage(start);
		setButtonImage(end);
	}

// *********************************************************************************************************************
// *** Translate a button view to an int (0 to BOARD_SIZE_X), -1 if (impossibly) button not found                      ***
// *********************************************************************************************************************

	public int indexOfView(View button) {
		log("indexOfView()");

		int buttonInt;
		try {
    		buttonInt = Integer.parseInt(String.valueOf(button.getId()));
		} catch(NumberFormatException e) {
   		log("(Could not parse) " + e);
   		return -1;
		}

		for (int i = 0; i < BOARD_SIZE_N; i++)
			if (buttonInt == context.getResources().getIdentifier("button" + String.valueOf(i), "id", getPackageName()))
			   return i;

	   return 0;
	}

// *********************************************************************************************************************
// *** Translate an int to a button view                                                                             ***
// *********************************************************************************************************************

	public void setButtonImage(int buttonInt) {
		log("setButtonImage(" + buttonInt + ")");

		// grab the button whose image we'll set
		ImageButton buttonView = viewOfIndex(buttonInt);

		// Figure out the original image coords
		int board_X = persBoard[buttonInt] / BOARD_SIZE_X;
		int board_Y = persBoard[buttonInt] - (board_X * BOARD_SIZE_X);

      // Pre-set the board
		Bitmap smallBMP =
			Bitmap.createBitmap(defaultBMP,
									  (board_Y * defaultBMP_Y),
									  (board_X * defaultBMP_X),
									  defaultBMP_Y,
									  defaultBMP_X);
 		buttonView.setImageBitmap(smallBMP);
	}

// *********************************************************************************************************************
// *** Translate an int to a button view                                                                             ***
// *********************************************************************************************************************

	public ImageButton viewOfIndex(int button) {
		log("viewOfIndex()");

	   return (ImageButton) findViewById(context.getResources().
   						 getIdentifier("button" + String.valueOf(button), "id", getPackageName()));
	}

// *********************************************************************************************************************
// *** See if the game is over, by win or loss                                                                       ***
// *********************************************************************************************************************

	public Boolean isGameOver() {
		log("seeIfGameOver()");

		for (int i = 0; i < BOARD_SIZE_N; i++)
			if (persBoard[i] != i)
				return false;

		return true;
	}

// *********************************************************************************************************************
// *** Alert the player that he's lost                                                                               ***
// *********************************************************************************************************************

	public void alertGameIsLost() {
		log("alertGameIsLost()");

      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
      alertDialogBuilder.setTitle(R.string.gameIsLostAlert_title)
              			   .setMessage(R.string.gameIsLostAlert_message)
                        .setNegativeButton(R.string.gameIsLostAlert_button_NO,
                                           new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
																 finish();
                                              }
                                           })
                        .setPositiveButton(R.string.gameIsLostAlert_button_YES,
                                           new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
                                              	 onCreate_init_cold();
                                           }
                                           })
								.setCancelable(false);

      alertDialogBuilder.create().show();
	}

// *********************************************************************************************************************
// *** Alert the player that he's won                                                                                ***
// *********************************************************************************************************************

	public void alertGameIsWon() {
		log("alertGameIsWon()");

      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
      alertDialogBuilder.setTitle(R.string.gameIsWonAlert_title)
              			   .setMessage(R.string.gameIsWonAlert_message)
                        .setNegativeButton(R.string.gameIsWonAlert_button_NO,
                                           new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
                                              	finish();
                                              }
                                           })
                        .setPositiveButton(R.string.gameIsWonAlert_button_YES,
                                           new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
                                              	 onCreate_init_cold();
															 }
														 })
								.setCancelable(false);

      alertDialogBuilder.create().show();
	}

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** http://developer.android.com/reference/android/app/Activity.html#onRestart%28%29                              ***
// ***                                                                                                               ***
// *********************************************************************************************************************

   public void onRestart() {
   	super.onRestart();
      log("onRestart()");
   }

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** http://developer.android.com/reference/android/app/Activity.html#onStart%28%29                                ***
// ***                                                                                                               ***
// *********************************************************************************************************************

   public void onStart() {
      super.onStart();
      log("onStart()");
   }

// *********************************************************************************************************************
// ***                                                                                                               ***
// ***                                                                                                               ***
// ***                                                                                                               ***
// *********************************************************************************************************************

	public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
		log("onRestoreInstanceState()");
 	}

// *********************************************************************************************************************
// ***                                                                                                               ***
// ***  http://developer.android.com/reference/android/app/Activity.html#onResume%28%29                              ***
// ***                                                                                                               ***
// *********************************************************************************************************************

   public void onResume() {
      super.onResume();
      log("onResume()");
   }

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** http://developer.android.com/reference/android/app/Activity.html#onPause%28%29                                ***
// ***                                                                                                               ***
// *** Save data that needs to be persistent across swap outs by Android Operating System                            ***
// ***                                                                                                               ***
// *********************************************************************************************************************

   protected void onPause() {
      super.onPause();
      log("onPause()");

      SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();

      // prefs saved every time game paused during play
      editor.putBoolean("pers_helpFlg", pers_helpFlg);
      editor.putInt("pers_gameStatus",  pers_gameStatus);

      for (int i = 0; i < BOARD_SIZE_X; i++)
      	editor.putInt("persBoard_" + i, persBoard[i]);

      editor.commit();
   }

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** http://developer.android.com/reference/android/app/Activity.html#onSaveInstanceState%28android.os.Bundle%29   ***
// ***                                                                                                               ***
// *********************************************************************************************************************

	public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
		log("onSaveInstanceState()");
	}

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** http://developer.android.com/reference/android/app/Activity.html#onStop%28%29                                 ***
// ***                                                                                                               ***
// *********************************************************************************************************************

   public void onStop() {
      super.onStop();
      log("onStop()");
   }

// *********************************************************************************************************************
// ***                                                                                                               ***
// *** http://developer.android.com/reference/android/app/Activity.html#onDestroy%28%29                              ***
// ***                                                                                                               ***
// *********************************************************************************************************************

   public void onDestroy() {
      super.onDestroy();
      log("onDestroy()");
   }

// *********************************************************************************************************************
// *** LogCat a warn message if in debug mode                                                                        ***
// *********************************************************************************************************************

	void log(String msg) {
   	if (twigDebug)
      	Log.w(context.getString(R.string.app_name), context.getString(R.string.app_name) + " " + msg);
	}

}
