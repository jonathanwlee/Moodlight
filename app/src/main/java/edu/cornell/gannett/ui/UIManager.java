package edu.cornell.gannett.ui;

import android.content.Context;
import android.graphics.Typeface;

/**
 * @author Jonathan
 * Manager for UI tools and assets. 
 */
public class UIManager {
	public Typeface tfLight;
	public Typeface tfRegular;
	public Typeface tfBold;
    private static UIManager instance = null;

    public static UIManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new UIManager(ctx);
        }
        return instance;
    }

    private UIManager(Context appContext) {
		tfLight = Typeface.createFromAsset(appContext.getAssets(),
				"fonts/OpenSans-Light.ttf");
		tfBold = Typeface.createFromAsset(appContext.getAssets(),
				"fonts/OpenSans-Semibold.ttf");

    }

	
	
}
