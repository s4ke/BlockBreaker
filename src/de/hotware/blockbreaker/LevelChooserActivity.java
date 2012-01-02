package de.hotware.blockbreaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class LevelChooserActivity extends Activity{
	private static final String CREATED_KEY = "bbacreated";
	private boolean mCreated = false;

	@Override
	public void onCreate(Bundle pSavedInstanceState) {		
		super.onCreate(pSavedInstanceState);
		if(pSavedInstanceState != null) {
			this.mCreated = pSavedInstanceState.getBoolean(CREATED_KEY);
		}
		if(!this.mCreated) {
			this.mCreated = true;
			this.startGameActivity("levels/default.lev", true);
		}
	}

	@Override
	public void onActivityResult(final int pRequestCode, final int pResultCode, final Intent pData) {
		String codeRepres = "";
		switch(pResultCode) {
		case BlockBreakerActivity.RESULT_RESTART: {
			codeRepres = "Restart";
			break;
		}
		case BlockBreakerActivity.RESULT_CANCELED: {
			codeRepres = "Canceled";
			break;
		}
		case BlockBreakerActivity.RESULT_ERROR: {
			codeRepres = "ERROR";
			break;
		}
		case BlockBreakerActivity.RESULT_WIN: {
			codeRepres = "Win";
			break;
		}
		case BlockBreakerActivity.RESULT_LOSE: {
			codeRepres = "Lose";
			break;
		}
		default: {
			codeRepres = "Unknown Result";
			break;
		}				
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Resultcode: " + codeRepres + " (" + pResultCode + ")")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				//TODO: maybe don't finish just let it stay like this?
				pDialog.dismiss();
				LevelChooserActivity.this.finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onSaveInstanceState(Bundle pBundle) {
		super.onSaveInstanceState(pBundle);
		pBundle.putBoolean(CREATED_KEY, this.mCreated);
	}

	private void startGameActivity(final String pPath, final boolean pIsAsset) {
		Intent intent = new Intent(this, BlockBreakerActivity.class);
		intent.putExtra(BlockBreakerActivity.LEVEL_ARG_KEY, pPath);
		intent.putExtra(BlockBreakerActivity.IS_ASSET_KEY, pIsAsset);
		this.startActivityForResult(intent, 0);
	}
}
