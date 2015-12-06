package edu.cornell.gannett.ui;

import edu.cornell.gannettML.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

/**
 * Generic class for Alert and Progress dialogs wizard
 * 
 * 
 */

public final class PHWizardAlertDialog {

	private ProgressDialog pdialog;
	private static PHWizardAlertDialog dialogs;

	private PHWizardAlertDialog() {

	}

	public static synchronized PHWizardAlertDialog getInstance() {
		if (dialogs == null) {
			dialogs = new PHWizardAlertDialog();
		}
		return dialogs;
	}

	public static AlertDialog showBridgeList(Context activityContext) { 
		LayoutInflater inflater = ((Activity) activityContext).getLayoutInflater();
		AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
		View dialoglayout = inflater.inflate(R.layout.bridgelistlinear, null);
		builder.setView(dialoglayout);
		AlertDialog alert = builder.create();
		return alert;
	}

	/**
	 * 
	 * @param activityContext
	 * @param resID
	 * @param btnNameResId  String resource id for button name
	 */
	public static AlertDialog showErrorDialog(Context activityContext, String msg, int btnNameResId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
		builder.setTitle(R.string.title_error).setMessage(msg).setPositiveButton(btnNameResId, null);
		AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		return alert; 
	}

	/**
	 * Stops running progress-bar
	 */
	public void closeProgressDialog() {

		if (pdialog != null) {
			pdialog.dismiss();
			pdialog = null;
		}
	}

	/**
	 * Shows progress-bar
	 * 
	 * @param resID
	 * @param act
	 */
	public void showProgressDialog(int resID, Context ctx) {
		String message = ctx.getString(resID);
		pdialog = ProgressDialog.show(ctx, null, message, true, true);
		pdialog.setCancelable(false);

	}

	/**
	 * 
	 * @param activityContext
	 * @param msg
	 * @param btnNameResId
	 */
	public static AlertDialog showAuthenticationErrorDialog(
			final Activity activityContext, String msg, int btnNameResId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
		builder.setTitle(R.string.title_error).setMessage(msg)
		.setPositiveButton(btnNameResId, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				activityContext.finish();

			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}

	public static AlertDialog showTipsDialog(final Activity activityContext) { 

		AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        View content = activityContext.getLayoutInflater().inflate(
                R.layout.dialog_tips, null); // inflate the content of the dialog
        final CheckBox userCheck = (CheckBox) content //the checkbox from that view
                .findViewById(R.id.checkBoxTips);

		builder.setView(content);
		builder.setTitle("Moodlight: Let's Get Started!")
		.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				

			}
		}).setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		AlertDialog alert = builder.create();
		return alert;

	}

}
