package com.github.andlyticsproject;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class LoginActivity extends BaseActivity {

	private static final String TAG = "Andlytics";

	protected static final int CREATE_ACCOUNT_REQUEST = 1;

	private LinearLayout accountList;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);
		accountList = (LinearLayout) findViewById(R.id.login_input);
	}

	@Override
	protected void onResume() {
		super.onResume();

		String selectedAccount = Preferences.getAccountName(this);
		boolean skipAutologin = Preferences.getSkipAutologin(this);

		if (!skipAutologin & selectedAccount != null) {
			redirectToMain(selectedAccount);
		} else {
			showAccountList();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.login_menu, menu);
		return true;
	}
	
	/**
	 * Called if item in option menu is selected.
	 * 
	 * @param item
	 *            The chosen menu item
	 * @return boolean true/false
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemLoginmenuAdd:			
			addNewGoogleAccount();
			break;
		default:
			return false;
		}
		return true;
	}

	protected void showAccountList() {

		final AccountManager manager = AccountManager.get(this);
		final Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE_GOOGLE);
		final int size = accounts.length;
		String[] names = new String[size];
		accountList.removeAllViews();
		for (int i = 0; i < size; i++) {
			names[i] = accounts[i].name;
			Boolean hiddenAccount = Preferences.getIsHiddenAccount(this, names[i]);
			View inflate = getLayoutInflater().inflate(R.layout.login_list_item, null);
			TextView accountName = (TextView) inflate.findViewById(R.id.login_list_item_text);
			accountName.setText(accounts[i].name);
			inflate.setTag(accounts[i].name);
			inflate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					String selectedAccount = (String) view.getTag();
					redirectToMain(selectedAccount);
				}
			});
			inflate.setClickable(!hiddenAccount);
			CheckBox enabled = (CheckBox) inflate.findViewById(R.id.login_list_item_enabled);
			enabled.setChecked(!hiddenAccount);
			enabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					View parent = (View) buttonView.getParent();
					Preferences.saveIsHiddenAccount(getApplicationContext(), (String) parent.getTag(), !isChecked);
					parent.setClickable(isChecked);
					// TODO enable/disable syncing
					
				}
			});
			accountList.addView(inflate);
		}
	}

	private void addNewGoogleAccount() {
		AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
			public void run(AccountManagerFuture<Bundle> future) {
				try {
					Bundle bundle = future.getResult();
					bundle.keySet();
					Log.d(TAG, "account added: " + bundle);

					showAccountList();

				} catch (OperationCanceledException e) {
					Log.d(TAG, "addAccount was canceled");
				} catch (IOException e) {
					Log.d(TAG, "addAccount failed: " + e);
				} catch (AuthenticatorException e) {
					Log.d(TAG, "addAccount failed: " + e);
				}
				// gotAccount(false);
			}
		};

		AccountManager.get(LoginActivity.this).addAccount(Constants.ACCOUNT_TYPE_GOOGLE, Constants.AUTH_TOKEN_TYPE_ANDROID_DEVLOPER, null,
				null /* options */, LoginActivity.this, callback, null /* handler */);
	}

	private void redirectToMain(String selectedAccount) {
		Preferences.saveSkipAutoLogin(this, false);
		Intent intent = new Intent(LoginActivity.this, Main.class);
		intent.putExtra(Constants.AUTH_ACCOUNT_NAME, selectedAccount);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
	}


}
