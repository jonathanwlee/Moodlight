package edu.cornell.gannett.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.os.Environment;

public class GSRLogger {
	private String logFileName;
	private String logFilePath;
	private SimpleDateFormat logSDF;
	
	public GSRLogger(String mode) {
		logFileName = genFileName(mode);
		logFilePath = genFilePath();
		logSDF = new SimpleDateFormat("HH:mm:ss.SS", Locale.US);
		logSDF.setTimeZone(TimeZone.getTimeZone("UTC-5"));	
	}
	
	public String getLogFilePath() {
		return logFilePath + logFileName;
	}
	
	public void log1P(double GSR, String stressing, int score, HSB hsb) {
		File file = new File(logFilePath, logFileName);
		Boolean fileExists = file.exists();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			if (!fileExists) {
				bw.write("Timestamp, GSR, stressing, hueScore, hue, sat");
				bw.newLine();
			}
			String time = logSDF.format((new Date(System.currentTimeMillis())));
			String GSRText = Double.toString(GSR);
			String scoreText = Integer.toString(score);
			String hueText = Integer.toString(hsb.getHue());
			String satText = Integer.toString(hsb.getSat());
			bw.write(
				time + "," + GSRText + "," + stressing + "," + 
			    scoreText + "," + hueText + "," + satText
			);
			bw.newLine();	
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Testing: was written file successful?
		//readAndPrintFile(file);	
	}
	
	public void log2P(double GSR1, String stressing1, double GSR2, String stressing2, 
			int score, HSB hsb) {
		File file = new File(logFilePath, logFileName);
		Boolean fileExists = file.exists();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			if (!fileExists) {
				bw.write("Timestamp, GSR1, stressing1, GSR2, stressing2, hueScore, hue, sat");
				bw.newLine();
			}
			String time = logSDF.format((new Date(System.currentTimeMillis())));
			String GSR1Text = Double.toString(GSR1);
			String GSR2Text = Double.toString(GSR2);
			String scoreText = Integer.toString(score);
			String hueText = Integer.toString(hsb.getHue());
			String satText = Integer.toString(hsb.getSat());
			bw.write(
				time + "," + GSR1Text + "," + stressing1 + 
				"," + GSR2Text + "," + stressing2 + "," + 
			    scoreText + "," + hueText + "," + satText
			);
			bw.newLine();	
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Testing: was written file successful?
		//readAndPrintFile(file);	
	}
	
	// For testing if written file was successful
	private void readAndPrintFile(File file) {
		if (file.exists()) {
			StringBuilder text = new StringBuilder();
			try {
			    BufferedReader br = new BufferedReader(new FileReader(file));
			    String line;
			    while ((line = br.readLine()) != null) {
			        text.append(line);
			        text.append('\n');
			    }
			    br.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String genFileName(String mode) {
		long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC-5"));
		return "log_" + sdf.format(new Date(time)) + "_" + mode + ".csv";	
	}
	
	private String genFilePath() {
		File storage = Environment.getExternalStorageDirectory();
		String path = storage.getAbsolutePath() + "/moodlight/";
		(new File(path)).mkdirs();
		return path;
	}
}
