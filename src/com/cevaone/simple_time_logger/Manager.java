package com.cevaone.simple_time_logger;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Manager extends ActionBarActivity {
	ArrayList<String> strings;
	ArrayAdapter<String> adapter;
	Button button_add_row;
	RelativeLayout layout;
	int client_id_counter = 1;
	int project_id_counter = 1;
	int client_checkbox_counter = 1;
	int project_checkbox_counter = 1;
	ArrayList<Pair<Integer, Integer>> c_fields_to_checkboxes;
	ArrayList<Pair<Integer, Integer>> p_fields_to_checkboxes;
	ArrayList<Integer> clients_to_remove;
	ArrayList<Integer> projects_to_remove;
	InputMethodManager inputManager;
	Button remove_checked;
	Intent startIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_manager);
		startIntent = getIntent();
		clients_to_remove = new ArrayList<Integer>();
		projects_to_remove = new ArrayList<Integer>();
		c_fields_to_checkboxes = new ArrayList<Pair<Integer, Integer>>();
		p_fields_to_checkboxes = new ArrayList<Pair<Integer, Integer>>();
		
		remove_checked = (Button) findViewById(R.id.remove_checked);
		remove_checked.setOnClickListener(mRemoveCheckedListener);
		
		layout = new RelativeLayout(this);
		layout = (RelativeLayout) findViewById(R.id.project_manager_layout);
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		for(int i = 0; i < MainActivity.clients.size(); i++) {
			layout = (RelativeLayout) findViewById(R.id.project_manager_layout);
			
			final EditText client_field = new EditText(this);
			client_field.setImeOptions(EditorInfo.IME_ACTION_DONE);
			client_field.setId(client_id_counter);				
			client_field.setSingleLine();
			client_field.setText(MainActivity.clients.get(i));
			
			final CheckBox checkBox = new CheckBox(this);
			checkBox.setId("client".hashCode() + client_id_counter);
			RelativeLayout.LayoutParams chkBoxParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			chkBoxParams.addRule(RelativeLayout.LEFT_OF, R.id.anchor);
			if(client_id_counter < 2) {
				chkBoxParams.addRule(RelativeLayout.BELOW, R.id.client_text);
			} else {
				chkBoxParams.addRule(RelativeLayout.BELOW, (client_id_counter-1));
			}
			
			

			c_fields_to_checkboxes.add(new Pair<Integer, Integer>(client_field.getId(), checkBox.getId()));
			
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Integer corresponding_field_index = -1;
					for(int i = 0; i < c_fields_to_checkboxes.size(); i++) {
						if(c_fields_to_checkboxes.get(i).second == checkBox.getId()) {
							corresponding_field_index = c_fields_to_checkboxes.get(i).first;
							
						}						
					}
					if(clients_to_remove.contains(corresponding_field_index-1)) {
						clients_to_remove.remove(corresponding_field_index-1);						
					} else {
						clients_to_remove.add(corresponding_field_index-1);						
					}	
				}
			});
			
			client_field.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE) {
						MainActivity.clients.set(v.getId() - 1, v.getText().toString());
						MainActivity.clients_aa_multi.notifyDataSetChanged();
						MainActivity.instance.save_clients_projects();
						
					}
					return false;
				}				
			});			
			
			RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);	
			textParams.addRule(RelativeLayout.LEFT_OF, checkBox.getId());	
			
			if(client_id_counter < 2) {
				textParams.addRule(RelativeLayout.BELOW, R.id.client_text);
			} else {
				textParams.addRule(RelativeLayout.BELOW, (client_id_counter-1));
			}			

			client_checkbox_counter++;
			client_id_counter++;
			layout.addView(client_field, textParams);
			layout.addView(checkBox, chkBoxParams);
		}
		for(int i = 0; i < MainActivity.projects.size(); i++) {
			layout = (RelativeLayout) findViewById(R.id.project_manager_layout);
			final EditText project_field = new EditText(this);
			project_field.setImeOptions(EditorInfo.IME_ACTION_DONE);
			project_field.setId(project_id_counter);	
			project_field.setSingleLine();	
			project_field.setText(MainActivity.projects.get(i));
			
			final CheckBox checkBox = new CheckBox(this);
			checkBox.setId("project".hashCode() + project_checkbox_counter);
			RelativeLayout.LayoutParams chkBoxParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);	
			chkBoxParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			if(project_id_counter < 2) {
				chkBoxParams.addRule(RelativeLayout.BELOW, R.id.project_text);
			} else {
				chkBoxParams.addRule(RelativeLayout.BELOW, (project_id_counter-1));
			}		

			p_fields_to_checkboxes.add(new Pair<Integer, Integer>(project_field.getId(), checkBox.getId()));
			
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Integer corresponding_field_index = 0;
					for(int i = 0; i < p_fields_to_checkboxes.size(); i++) {
						if(p_fields_to_checkboxes.get(i).second == checkBox.getId()) {
							corresponding_field_index = p_fields_to_checkboxes.get(i).first;
							
						}						
					}
					if(projects_to_remove.contains(corresponding_field_index-1)) {
						projects_to_remove.remove(corresponding_field_index-1);						
					} else {
						projects_to_remove.add(corresponding_field_index-1);						
					}	
				}
			});
			
			
			
			project_field.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE) {
						MainActivity.projects.set(v.getId() - 1, v.getText().toString());
						MainActivity.projects_aa_multi.notifyDataSetChanged();
						MainActivity.instance.save_clients_projects();
					}
					return false;
				}				
			});			
			
			RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			textParams.addRule(RelativeLayout.LEFT_OF, checkBox.getId());
			textParams.addRule(RelativeLayout.RIGHT_OF, R.id.anchor);	
			
			if(project_id_counter < 2) {
				textParams.addRule(RelativeLayout.BELOW, R.id.project_text);
			} else {
				textParams.addRule(RelativeLayout.BELOW, (project_id_counter-1));
			}	
			

			project_checkbox_counter++;
			project_id_counter++;			
			layout.addView(project_field, textParams);
			layout.addView(checkBox, chkBoxParams);
		}
	}
	
	View.OnClickListener mRemoveCheckedListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
		for(int i = 0; i < clients_to_remove.size(); i++) {

			MainActivity.clients.remove(MainActivity.clients.get(clients_to_remove.get(i)));
			MainActivity.clients_aa_multi.notifyDataSetChanged();
			
		}
		for(int i = 0; i < projects_to_remove.size(); i++) {
			//Toast.makeText(ProjectManager.this, Boolean.valueOf(MainActivity.projects.remove(MainActivity.projects.get(projects_to_remove.get(i)))).toString(), Toast.LENGTH_SHORT).show();
			MainActivity.projects.remove(MainActivity.projects.get(projects_to_remove.get(i)));
			MainActivity.projects_aa_multi.notifyDataSetChanged();
			
		}
		clients_to_remove.clear();
		projects_to_remove.clear();
		MainActivity.instance.save_clients_projects();
		finish();
		startActivity(startIntent);
		
			
		}
		
		
		
	};
	

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.project_manager, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
