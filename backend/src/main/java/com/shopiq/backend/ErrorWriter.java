package com.shopiq.backend;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;


public class ErrorWriter {
    public static final String outputFile = "errorLog.txt";
    public static int logError(String error)
    {
        long currentTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        int mSecond = calendar.get(Calendar.SECOND);
        int miliMSecond = calendar.get(Calendar.MILLISECOND);
        String log = "["+ mMonth + "/" + mDay + "/" + mYear + " | " + mHour + ":" + mMinute + "." + mSecond + miliMSecond + "]" + "\n" + error + "\n";
        return saveToFile(log, outputFile);
    }
    static int saveToFile(String aInput, String aFileName)
    {
        if(aFileName==null)
            return 1;
        try{
            FileWriter fileWriter = new FileWriter(outputFile, true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(aInput);  //New line
            printWriter.close();
        }catch(FileNotFoundException e){
            return 2;
        }catch(Exception e){
            return 3;
        }
        return 0;
    }
}
