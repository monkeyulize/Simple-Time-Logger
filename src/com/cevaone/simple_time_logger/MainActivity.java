package com.cevaone.simple_time_logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;





public class MainActivity extends ActionBarActivity {
	
	/*ActionBar.Tab tab1, tab2;*/
	
	
	
	Chronometer mChronometer;
	Long timeSincePause = Long.valueOf(0);
	Long start_time;
	Boolean isStopped = Boolean.valueOf(true);
	Boolean isPaused = Boolean.valueOf(false);
	Boolean continue_to_send = Boolean.valueOf(false);
	Boolean can_send = false;
	Boolean save_only = false;
	String project_name = "default project";
	String client_name = "default client";
	String email_address = "somebody@somewhere.com";
	AlertDialog.Builder send_alert;
	AlertDialog.Builder email_change_alert;
	AlertDialog.Builder add_project_alert;
	AlertDialog.Builder add_client_alert;
	AlertDialog.Builder remove_project_alert;
	AlertDialog.Builder remove_client_alert;
	AlertDialog.Builder reset_all_alert;
	AlertDialog.Builder value_not_set_alert;
	AlertDialog.Builder confirm_alert;
	AlertDialog.Builder about_alert;
	AlertDialog test_alert;
	static ArrayList<String> projects;
	static ArrayList<String> clients;
	ArrayList<String> to_remove;
	EditText input;
	ArrayAdapter<String> projects_aa;
	static ArrayAdapter<String> projects_aa_multi;
	ArrayAdapter<String> clients_aa;
	static ArrayAdapter<String> clients_aa_multi;
	
	public static MainActivity instance = null;
	
	ListView projects_listview;
	ListView clients_listview;
	Long elapsed_time = Long.valueOf(0);
    Button button_start;
    Button button_pause;
    Button button_stop;
    Button button_send;	
    Spinner select_project_spinner, select_client_spinner;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Integer saved_chrono_status; //0=stopped, 1=started, 2=paused
    Long saved_elapsed_time;
    //TEST
    Spinner spinner;
    ArrayList<String> strings;
    ArrayAdapter<String> adapter;
    LinearLayout layout;
    //TEST
    
    
    Long get_hours(Long time_in_ms) {
		Long ret;
		ret = time_in_ms / (60*60*1000);
		time_in_ms = time_in_ms - ret*(60*60*1000);
    	return ret;	
    }
    Long get_minutes(Long time_in_ms) {
		Long ret;
		ret = time_in_ms / (60*1000);
		time_in_ms = time_in_ms - ret*(60*1000);
    	return ret;	
    }
    Long get_seconds(Long time_in_ms) {
		Long ret;
		ret = time_in_ms / (1000);
    	return ret;	
    }   
    
    public void write_and_email(String _project_name) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(start_time);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH'h'mm'm'", Locale.US);
		String start_time_string = timeFormat.format(cal.getTime());
    	String filename = _project_name + '_' + start_time_string + "_timeLog.csv";
    	writeTimeLog(filename, project_name, client_name);   		
		if(save_only == false) {
	    	File dir = new File (Environment.getExternalStorageDirectory() + "/timeLogs/");
			File timeLogFile = new File(dir, filename);
			Uri timeLogUri = Uri.fromFile(timeLogFile);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
		
			i.putExtra(Intent.EXTRA_EMAIL, new String[]{email_address});
			i.putExtra(Intent.EXTRA_SUBJECT, '"' + project_name + '"' + " time log");
			i.putExtra(Intent.EXTRA_TEXT, elapsed_time.toString() + "ms elapsed");
			i.putExtra(Intent.EXTRA_STREAM, timeLogUri);
			try {
				startActivity(i);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				
			}		
		}
    	

		
    
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
/*        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        tab1 = actionBar.newTab().setText("1");
        tab2 = actionBar.newTab().setText("2");
        
        actionBar.addTab(tab1);
        actionBar.addTab(tab2);*/
        
        
        instance = this;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        projects = new ArrayList<String>();
        clients = new ArrayList<String>();
        

        
        to_remove = new ArrayList<String>();
        setContentView(R.layout.activity_main);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        button_start = (Button) findViewById(R.id.start);
        button_start.setOnClickListener(mStartListener);
       
        button_pause = (Button) findViewById(R.id.pause);
        button_pause.setOnClickListener(mPauseListener);

        button_stop = (Button) findViewById(R.id.stop);
        button_stop.setOnClickListener(mStopListener);
        
        button_send = (Button) findViewById(R.id.sendTime);
        button_send.setOnClickListener(mSendTimeListener);   
        
        button_send.setClickable(false);
 
    	try {
        	FileInputStream input = openFileInput("projects.txt");
        	DataInputStream din = new DataInputStream(input);
        	int sz = din.readInt();for(int i = 0; i < sz; i++) {
        		String line = din.readUTF();
        		projects.add(line);            		
        	}        		
    	} catch (IOException exc) { exc.printStackTrace(); }
    	try {
        	FileInputStream input = openFileInput("clients.txt");
        	DataInputStream din = new DataInputStream(input);
        	int sz = din.readInt();for(int i = 0; i < sz; i++) {
        		String line = din.readUTF();
        		clients.add(line);            		
        	}        		
    	} catch (IOException exc) { exc.printStackTrace(); } 
        
    	email_address = sharedPref.getString(getString(R.string.pref_email), getString(R.string.email_not_set));
      	Long resume_time = SystemClock.elapsedRealtime() - sharedPref.getLong(getString(R.string.pref_timeWhenClosed), 0);
    	saved_elapsed_time = sharedPref.getLong(getString(R.string.pref_elapsedTime), 0);
    	saved_chrono_status = sharedPref.getInt(getString(R.string.pref_chronoStatus), -1);
    	timeSincePause = sharedPref.getLong(getString(R.string.pref_timeSincePause), 0);
    	can_send = sharedPref.getBoolean(getString(R.string.pref_canSend), false);
    	start_time = sharedPref.getLong("pref_startTime", 0);
    	
    	if(saved_chrono_status == 0) {
    		button_start.setText(R.string.start);
    		button_stop.setText(R.string.reset_time);
    		isStopped = Boolean.valueOf(true);
    		isPaused = Boolean.valueOf(false);
    	} else if(saved_chrono_status == 1) {
    		button_start.setText(R.string.start);
    		isPaused = Boolean.valueOf(false);
    		isStopped = Boolean.valueOf(false);
    		mChronometer.start();
    	} else if(saved_chrono_status == 2) {
    		isPaused = Boolean.valueOf(true);
    		button_start.setText(R.string.unpause);
    		isStopped = Boolean.valueOf(false);
    	}
    	
    	
    	if(can_send == true) {
    		button_send.setClickable(true);
    		button_send.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send));
    	} else {
    		button_send.setClickable(false);
    		button_send.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send_unclickable));
    	}
    	
    	if(isStopped == true || isPaused == true) {
    		mChronometer.setBase(SystemClock.elapsedRealtime() - saved_elapsed_time);        		
    	} else {
    		mChronometer.setBase(SystemClock.elapsedRealtime() - resume_time - saved_elapsed_time);
    		mChronometer.start();
    	}

        projects_aa = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, projects);
        clients_aa = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, clients);
        projects_aa.setDropDownViewResource(R.layout.spinner_dropdown_item);
        clients_aa.setDropDownViewResource(R.layout.spinner_dropdown_item);
        
        
        
        
        add_project_alert = new AlertDialog.Builder(this);
        add_project_alert.setTitle("Enter a project name");        
        add_project_alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		String value = input.getText().toString();
        		projects.add(value);
        		projects_aa.notifyDataSetChanged();
        		
        		if(continue_to_send) {
        			send_time();
        			continue_to_send = Boolean.valueOf(false);
        		}
        			
        		
        	}
        });

        add_project_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
        	  		// Cancelled.
        	  }
        });  
        
        add_client_alert = new AlertDialog.Builder(this);
        add_client_alert.setTitle("Enter a client name");        
        add_client_alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		String value = input.getText().toString();
        		clients.add(value);
        		clients_aa.notifyDataSetChanged();
        		
        		if(continue_to_send) {
        			send_time();
        			continue_to_send = Boolean.valueOf(false);
        		}
        			
        		
        	}
        });

        add_client_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
        	  		// Cancelled.
        	  }
        }); 
        
        email_change_alert = new AlertDialog.Builder(this);
        email_change_alert.setTitle("Enter an email address");              
        email_change_alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		String value = input.getText().toString();
        	  		email_address = value;
        	  		editor.putString(getString(R.string.pref_email), email_address);
        	  		Toast.makeText(MainActivity.this, email_address, Toast.LENGTH_SHORT).show();
        	  		
        	  		
        	  		if(continue_to_send) {
        	  			
        	  			write_and_email(project_name);
        	  			continue_to_send = Boolean.valueOf(false);
        	  		}
        	  		
        	  	}
        });

        email_change_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
        	  		// Canceled.
        	  }
        });
        
        
        projects_aa_multi = new ArrayAdapter<String>(this, R.layout.my_simple_list_item_multiple_choice, projects);
        clients_aa_multi = new ArrayAdapter<String>(this, R.layout.my_simple_list_item_multiple_choice, clients);
        
       
       

        remove_project_alert = new AlertDialog.Builder(this);
        remove_project_alert.setTitle("Select projects to remove");
        remove_project_alert.setPositiveButton("Remove selected", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				SparseBooleanArray checked = projects_listview.getCheckedItemPositions();				
				for(int i = 0; i < projects_listview.getCount(); i++) {
					if(checked.get(i)) {
						to_remove.add(projects.get(i));
						
					}
					
				}
				for(int i = 0; i < to_remove.size(); i++) {					
					projects.remove(to_remove.get(i));
					
				}
				to_remove.clear();
				
				projects_aa_multi.notifyDataSetChanged();
			}
        	
        });
        remove_project_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
        	
        });

        remove_client_alert = new AlertDialog.Builder(this);
        remove_client_alert.setTitle("Select projects to remove");
        remove_client_alert.setPositiveButton("Remove selected", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				SparseBooleanArray checked = clients_listview.getCheckedItemPositions();				
				for(int i = 0; i < clients_listview.getCount(); i++) {
					if(checked.get(i)) {
						to_remove.add(clients.get(i));
						
					}
					
				}
				for(int i = 0; i < to_remove.size(); i++) {					
					clients.remove(to_remove.get(i));
					
				}
				to_remove.clear();
				
				clients_aa_multi.notifyDataSetChanged();
			}
        	
        });
        remove_client_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
        	
        });        
        
        
        value_not_set_alert = new AlertDialog.Builder(this);
        value_not_set_alert.setTitle("Whoops!");
        
        value_not_set_alert.setPositiveButton(getString(R.string.positive_response), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
        value_not_set_alert.setNegativeButton(getString(R.string.negative_response), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
			}
		});
        
        
        send_alert = new AlertDialog.Builder(this);
        send_alert.setTitle("Pick a client and project");

		
		
		  

        send_alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		save_only = false;
        		//button_send.setBackground(getResources().getDrawable(R.drawable.button_send_unclickable));
        		button_send.setClickable(true);
        		if(email_address == getString(R.string.email_not_set)) {
        	        value_not_set_alert.setMessage(getString(R.string.email_not_set_message));
        			value_not_set_alert.setPositiveButton(getString(R.string.positive_response), new DialogInterface.OnClickListener() {
        				
        				@Override
        				public void onClick(DialogInterface dialog, int which) {
        					input = new EditText(MainActivity.this);
        					continue_to_send = Boolean.valueOf(true);
        	        		email_change_alert.setView(input); 
        					email_change_alert.show();
        					
        				}
        			});
        			value_not_set_alert.setNegativeButton(getString(R.string.negative_response), new DialogInterface.OnClickListener() {
        				
        				@Override
        				public void onClick(DialogInterface dialog, int which) {
        					button_send.setClickable(true);
        					button_send.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send));
        				}
        			});
        	        
        	        value_not_set_alert.show();
        			
        		} else {
        			write_and_email(project_name);
        			
        		}
        		
        		
        	}
        });
        send_alert.setNeutralButton("Save", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		//button_send.setBackground(getResources().getDrawable(R.drawable.button_send_unclickable));
        		button_send.setClickable(true);
        		save_only = true;
        		write_and_email(project_name);
        	}
        	
        });
        
        
        send_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		button_send.setClickable(true);
        	}
        	
        });
        
        reset_all_alert = new AlertDialog.Builder(this);
        reset_all_alert.setTitle("Reset saved settings?");
        reset_all_alert.setPositiveButton("Reset", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				File file = new File(getFilesDir(), "projects.txt");
				file.delete();				
				file = new File(getFilesDir(), "clients.txt");
				file.delete();
				
				editor.clear();
				editor.apply();
				Intent intent = getIntent();
				finish();
				startActivity(intent);				
			}
        	
        });
        reset_all_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// cancelled
				
			}
        	
        });
        
        confirm_alert = new AlertDialog.Builder(this);
        confirm_alert.setTitle(R.string.confirm_dialog);
        
        about_alert = new AlertDialog.Builder(this);
        about_alert.setTitle(R.string.about);
        //about_alert.setMessage("Test about");
        
        
        
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
        	public void onChronometerTick(Chronometer chronometer) {
        		saved_elapsed_time = SystemClock.elapsedRealtime() - mChronometer.getBase();
        		
        	}
        	
        });  
    }


    
    
    View.OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {     	
        	button_stop.setText(R.string.stop);
        	start_time = System.currentTimeMillis();
        	can_send = false;
    		button_send.setClickable(false);
    		button_send.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send_unclickable));
        	if(isStopped == true) {
        		start_time = System.currentTimeMillis();
        		button_send.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send_unclickable));
        		mChronometer.setBase(SystemClock.elapsedRealtime()); 
        		timeSincePause = Long.valueOf(0);
        		mChronometer.start();
        		saved_chrono_status = 1;
        		isStopped = Boolean.valueOf(false);
           } 
           
           if(isPaused == true){
        	   button_start.setText(R.string.start);
        	   isPaused = Boolean.valueOf(false);
        	   mChronometer.setBase(SystemClock.elapsedRealtime() + timeSincePause);        	  
        	   mChronometer.start();        	   
        	   saved_chrono_status = 1;
           }

        }
    };
    
    View.OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            if(!isStopped) {
            	button_start.setText(R.string.unpause);
            	isPaused = Boolean.valueOf(true);
            	timeSincePause = mChronometer.getBase() - SystemClock.elapsedRealtime();
            	mChronometer.stop();
            	saved_chrono_status = 2;           	
            }

        }
    };
    
    
    
    

    View.OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
        	if(isStopped && button_stop.getText() != getString(R.string.stop)) {
        		confirm_alert.setMessage(R.string.reset_time_confirm);
        		confirm_alert.setPositiveButton(R.string.positive_response, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
		        		mChronometer.setBase(SystemClock.elapsedRealtime()); 
		        		saved_chrono_status = 0;
		        		can_send = false;
		        		button_send.setClickable(false);
		        		button_send.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send_unclickable));
		        		button_stop.setText(R.string.stop);
						
					}
				});
        		confirm_alert.setNegativeButton(R.string.negative_response, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});       		
        		confirm_alert.show();

        	} else if(!isStopped){
            	can_send = true;
        		button_send.setClickable(true);
            	button_send.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_send));
            	button_start.setText(R.string.start);	
            	
            	isStopped = Boolean.valueOf(true);
            	mChronometer.stop();
            	saved_chrono_status = 0;
            	button_stop.setText(R.string.reset_time);
            	elapsed_time = saved_elapsed_time;        		
        		
        	}
        }
    };


    
    View.OnClickListener mSendTimeListener = new OnClickListener() {
        public void onClick(View v) {
        	
        	send_time();
        	
        	
        }
    };
    
    public void setup_send_dialog() {
    	View view = (View) LayoutInflater.from(getApplicationContext()).inflate(R.layout.select_project_client, null);
        select_project_spinner = (Spinner) view.findViewById(R.id.select_project_spinner);
        select_client_spinner = (Spinner) view.findViewById(R.id.select_client_spinner);
        
        TextView client_text = (TextView) view.findViewById(R.id.select_client_text);
        TextView project_text = (TextView) view.findViewById(R.id.select_project_text);
        project_text.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        client_text.setWidth(project_text.getMeasuredWidth());


        select_project_spinner.setAdapter(projects_aa);
        select_client_spinner.setAdapter(clients_aa);
        select_project_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				project_name = parent.getSelectedItem().toString();
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
        select_client_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				client_name = parent.getSelectedItem().toString();
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
        
        
        send_alert.setView(view);    	

    }

    public void send_time() {
    	if(projects.size() == 0) {
	        continue_to_send = true;
    		value_not_set_alert.setMessage(getString(R.string.no_projects_message));
			value_not_set_alert.setPositiveButton(getString(R.string.positive_response), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					input = new EditText(MainActivity.this);
					continue_to_send = Boolean.valueOf(true);
	        		add_project_alert.setView(input); 
	        		add_project_alert.show();
					
				}
			});
			value_not_set_alert.show();
    		
    	} else if(clients.size() == 0){
    		continue_to_send = true;
    		value_not_set_alert.setMessage(getString(R.string.no_clients_message));
			value_not_set_alert.setPositiveButton(getString(R.string.positive_response), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					input = new EditText(MainActivity.this);
					continue_to_send = Boolean.valueOf(true);
	        		add_client_alert.setView(input); 
	        		add_client_alert.show();
					
				}
			});
			value_not_set_alert.show();
    	} else {
    		setup_send_dialog();
    		send_alert.show();
    		button_send.setClickable(false);       		
    	}
    	    	
    	
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	savedInstanceState.putInt("chronoStatus", saved_chrono_status);
    	savedInstanceState.putLong("elapsedTime", saved_elapsed_time);
    	savedInstanceState.putLong("timeSincePause", timeSincePause);
    	savedInstanceState.putBoolean("can_send", can_send);
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    public void save_clients_projects() {
    	try {
    		FileOutputStream output = openFileOutput("projects.txt", Context.MODE_PRIVATE);
    		DataOutputStream dout = new DataOutputStream(output);
    		dout.writeInt(projects.size());
    		for(String line : projects) {
    			dout.writeUTF(line);
    		}
    		dout.flush();
    		dout.close();
    		
    	}	catch (IOException exc) { exc.printStackTrace(); }
    	try {
    		FileOutputStream output = openFileOutput("clients.txt", Context.MODE_PRIVATE);
    		DataOutputStream dout = new DataOutputStream(output);
    		dout.writeInt(clients.size());
    		for(String line : clients) {
    			dout.writeUTF(line);
    		}
    		dout.flush();
    		dout.close();
    		
    	}	catch (IOException exc) { exc.printStackTrace(); }   	
    	
    	
    }
    
    
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    	save_clients_projects();
    	
    	editor.putLong(getString(R.string.pref_elapsedTime), saved_elapsed_time);
    	editor.putInt(getString(R.string.pref_chronoStatus), saved_chrono_status);
    	editor.putLong(getString(R.string.pref_timeSincePause), timeSincePause);
    	editor.putLong(getString(R.string.pref_timeWhenClosed), SystemClock.elapsedRealtime());
    	editor.putBoolean(getString(R.string.pref_canSend), can_send);
    	editor.putLong("pref_startTime", start_time);
    	editor.apply();
    	
    }
    

    
    
    public void writeTimeLog(String filename, String project, String client) {
    	File sdCard = Environment.getExternalStorageDirectory();
    	File dir = new File (sdCard.getAbsolutePath() + "/timeLogs");
    	dir.mkdirs();
    	File file = new File(dir, filename);
   	
    	try {
    		
    		FileOutputStream f = new FileOutputStream(file, false);
    		String data = "";
    		
    		Long t_elapsed_time = elapsed_time;
    		
    		Long hours = get_hours(t_elapsed_time);
    		Long minutes = get_minutes(t_elapsed_time);
    		//Long seconds = get_seconds(t_elapsed_time);

    		String formatted_time = hours.toString() + ':' + minutes.toString() + '\n';
    		
    		Calendar c = Calendar.getInstance();
    		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    		String formattedDate = df.format(c.getTime());
    		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    		String start_time_string = timeFormat.format(c.getTime());
    		
    		
    		data = client + ',' + project + ',' + formattedDate +  ',' + start_time_string + ',' + formatted_time;     		
    		f.write(data.getBytes());
    		
    		f.close();
    		
    	}	catch (IOException exc) { exc.printStackTrace(); }
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
        	case R.id.action_email:
        		input = new EditText(this);
        		email_change_alert.setView(input);  
        		email_change_alert.show();
        		return true;
        	case R.id.action_add_project:
        		input = new EditText(this);
        		add_project_alert.setView(input);
        		add_project_alert.show();
        		return true;
        	case R.id.action_add_client:
        		input = new EditText(this);
        		add_client_alert.setView(input);
        		add_client_alert.show();
        		return true;
        	case R.id.action_reset_all:
        		reset_all_alert.show();
        		return true;
        	case R.id.action_about:
        		View view = (View) LayoutInflater.from(getApplicationContext()).inflate(R.layout.about, null);
        		about_alert.setView(view);
        		about_alert.show();
        		return true;
        	case R.id.action_view_edit:
        		Intent intent = new Intent(this, Manager.class);
        		startActivity(intent);
        		return true;
        	default:
        		
        		return super.onOptionsItemSelected(item);
        }
        
    }



}
