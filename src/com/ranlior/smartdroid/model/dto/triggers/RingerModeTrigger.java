/**
 * 
 */
package com.ranlior.smartdroid.model.dto.triggers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ranlior.smartdroid.model.dto.rules.Rule;

/**
 * @author Ran Haveshush
 * Email:  ran.haveshush.shenkar@gmail.com
 *
 */
@DatabaseTable(tableName = "ringer_mode_triggers")
public class RingerModeTrigger extends Trigger {
	
	/**
	 * Holds logger's tag.
	 */
	private static final String TAG = "RingerModeTrigger";
	
	/**
	 * Holds the trigger wanted ringer mode.
	 */
	@DatabaseField(canBeNull=false)
	private int wantedRingerMode = 0;
	

	/**
	 * Default constructor.
	 * ORMLite needs a no-arg constructor.
	 */
	protected RingerModeTrigger() {
		super();
	}

	/**
	 * Minimal constructor.
	 * 
	 * @param context		Context the context instantiating this action
	 * @param rule			Rule represents trigger's rule
	 * @param name			String represents trigger's name
	 * @param description	String represents trigger's description
	 */
	public RingerModeTrigger(Context context, Rule rule, String name, String description, int wantedRingerMode) {
		super(context, rule, RingerModeTrigger.class.getSimpleName(), name, description);
		this.wantedRingerMode = wantedRingerMode;
	}

	/**
	 * @return the wantedRingerMode
	 */
	public int getWantedRingerMode() {
		return wantedRingerMode;
	}

	/**
	 * @param wantedRingerMode the wantedRingerMode to set
	 */
	public void setWantedRingerMode(int wantedRingerMode) {
		this.wantedRingerMode = wantedRingerMode;
	}

	/* (non-Javadoc)
	 * @see com.ranlior.smartdroid.model.dto.triggers.Trigger#register()
	 */
	@Override
	public void register() {
		// Loggers
		Log.d(TAG, "register()");
		
		// Registering a battery broadcast receiver
//		IntentFilter intentFilter = new IntentFilter("android.media.RINGER_MODE_CHANGED");
//		context.registerReceiver(new RingerModeReceiver(RingerModeTrigger.this), intentFilter);
		Intent intent = new Intent("android.media.RINGER_MODE_CHANGED");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context, ((Long)getId()).intValue(),
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
	}
	
	/* (non-Javadoc)
	 * @see com.ranlior.smartdroid.model.dto.triggers.Trigger#unregister()
	 */
	public void unregister() {
		// Loggers
		Log.d(TAG, "unregister()");
		
		// FIXME: implement
	}

}