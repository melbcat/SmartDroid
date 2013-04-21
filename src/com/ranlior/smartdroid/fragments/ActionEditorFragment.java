/**
 * 
 */
package com.ranlior.smartdroid.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import com.ranlior.smartdroid.R;
import com.ranlior.smartdroid.activities.ActionSelectActivity;
import com.ranlior.smartdroid.activities.RuleEditorActivity.State;
import com.ranlior.smartdroid.adapters.ActionExpandableListAdapter;
import com.ranlior.smartdroid.config.SmartDroid;
import com.ranlior.smartdroid.model.database.Db4oHelper;
import com.ranlior.smartdroid.model.dto.actions.Action;
import com.ranlior.smartdroid.model.dto.rules.Rule;

/**
 * @author Ran Haveshush Email: ran.haveshush.shenkar@gmail.com
 * 
 */
public class ActionEditorFragment extends SherlockFragment {

	private static final String TAG = ActionEditorFragment.class.getSimpleName();

	public static final int SELECT_ACTION_REQUEST_CODE = 1002;

	private ObjectContainer db = null;

	private State state;

	private UUID ruleUuId = null;

	private static List<Action> actions = null;

	private ActionExpandableListAdapter expandableActionAdaper = null;

	private ExpandableListView elvActions = null;

	private Listener listener = null;

	private Activity hostingActivity = null;

	/**
	 * Listener interface for the fragment. Container Activity must implement
	 * this interface.
	 * 
	 * @author Ran Haveshush Email: ran.haveshush.shenkar@gmail.com
	 * 
	 */
	public interface Listener {
		/**
		 * @param actions
		 */
		public void setActions(List<Action> actions);
	}

	/**
	 * Create a new instance of the fargment, initialized to show the actions of
	 * the rule by given rule id.
	 */
	public static ActionEditorFragment newInstance(State state, UUID ruleUuid) {
		Log.d(TAG, "newInstance(State state, UUID ruleUuid)");
		ActionEditorFragment fragment = new ActionEditorFragment();

		// Supply rule id input as an argument.
		Bundle args = new Bundle();
		args.putSerializable("state", state);
		if (ruleUuid != null) {
			args.putSerializable(SmartDroid.Extra.EXTRA_RULE_ID, ruleUuid);
		}
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach(Activity activity)");

		// Ensures hosting activity implements the Listener interface
		try {
			listener = (Listener) activity;
			// Gets fragment hosting activity
			hostingActivity = activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement " + Listener.class.getSimpleName());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState(Bundle outState)");

		outState.putSerializable("state", state);
		outState.putSerializable(SmartDroid.Extra.EXTRA_RULE_ID, ruleUuId);

		// Calls the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate(Bundle savedInstanceState)");

		setHasOptionsMenu(true);

		db = Db4oHelper.db(hostingActivity);

		// During creation, if arguments have been supplied to the fragment
		// then parse those out
		Bundle args = getArguments();
		if (args != null) {
			state = (State) args.getSerializable("state");
			ruleUuId = (UUID) args.getSerializable(SmartDroid.Extra.EXTRA_RULE_ID);
		}

		// If recreating a previously destroyed instance
		if (savedInstanceState != null) {
			// Restore value of members from saved state
			state = (State) savedInstanceState.getSerializable("state");
			ruleUuId = (UUID) savedInstanceState.getSerializable(SmartDroid.Extra.EXTRA_RULE_ID);
		}

		// Gets rule's actions
		switch (state) {
		case ADD_RULE:
			actions = new ArrayList<Action>();
			break;
		case EDIT_RULE:
			List<Rule> rules = db.query(new Predicate<Rule>() {
				public boolean match(Rule rule) {
					return ruleUuId.compareTo(rule.getId()) == 0;
				}
			});
			// FIXME: check if needed
			// db.activate(rule, 3);
			actions = rules.get(0).getActions();
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Log.d(TAG, "onCreateOptionsMenu(Menu menu, MenuInflater inflater)");

		inflater.inflate(R.menu.activity_action_select, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected(MenuItem item)");

		switch (item.getItemId()) {
		case R.id.addAction:
			Intent intent = new Intent(hostingActivity, ActionSelectActivity.class);
			startActivityForResult(intent, SELECT_ACTION_REQUEST_CODE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)");

		// If the container view is null,
		// There is no need for us to create the fragment view
		if (container == null) {
			return null;
			// If the container view isn't null,
			// There is need for us to create the fragment view
		} else {
			expandableActionAdaper = new ActionExpandableListAdapter(hostingActivity, actions);
			View view = inflater.inflate(R.layout.fragment_expandable_list_actions, null);
			elvActions = (ExpandableListView) view.findViewById(R.id.expandableListView);
			elvActions.setAdapter(expandableActionAdaper);
			return view;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(int requestCode, int resultCode, Intent data)");

		if (resultCode == hostingActivity.RESULT_OK) {
			if (requestCode == SELECT_ACTION_REQUEST_CODE) {
				String actionClassName = data.getStringExtra(SmartDroid.Extra.EXTRA_ACTION_CLASS_NAME);
				Log.d(TAG, "actionClassName: " + actionClassName);
				Action action = null;

				try {
					action = (Action) Class.forName(SmartDroid.Actions.PACKAGE + "." + actionClassName).newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (java.lang.InstantiationException e) {
					e.printStackTrace();
				}

				actions.add(action);
				elvActions.postDelayed(new Runnable() {
					@Override
					public void run() {
						elvActions.setSelection(expandableActionAdaper.getGroupCount() - 1);
					}
				}, 100L);
				elvActions.expandGroup(expandableActionAdaper.getGroupCount() - 1);
				expandableActionAdaper.notifyDataSetChanged();

				listener.setActions(actions);
			}
		}
	}

}