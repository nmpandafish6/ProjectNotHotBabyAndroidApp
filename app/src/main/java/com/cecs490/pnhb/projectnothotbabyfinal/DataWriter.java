package com.cecs490.pnhb.projectnothotbabyfinal;

import android.content.Context;
import android.icu.util.Calendar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Nicolas on 3/18/2018.
 */

public class DataWriter {

    public static void writeTemperature(double value){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK)-1;
        String fileBase = "TEMPERATURE_STATISTICS_";
        String fileName = fileBase + day;
        File file = new File(ResourceMaster.m_context.getFilesDir(), fileName);
        if(ResourceMaster.preferences.getInt(Constants.LAST_DATE,-1) != day){
            resetFiles(day);
        }
        try {
            if(file.exists()) {
                FileOutputStream output = ResourceMaster.m_context.openFileOutput(fileName, Context.MODE_APPEND);
                output.write(("," + value).getBytes());
            }else{
                FileOutputStream output = ResourceMaster.m_context.openFileOutput(fileName, Context.MODE_PRIVATE);
                output.write(("" + value).getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResourceMaster.preferenceEditor.putInt(Constants.LAST_DATE, day);
        ResourceMaster.preferenceEditor.commit();
    }

    public static void writeDewPoint(double value){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK)-1;
        String fileBase = "DEWPOINT_STATISTICS_";
        String fileName = fileBase + day;
        File file = new File(ResourceMaster.m_context.getFilesDir(), fileName);
        if(ResourceMaster.preferences.getInt(Constants.LAST_DATE,-1) != day){
            resetFiles(day);
        }
        try {
            if(file.exists()) {
                FileOutputStream output = ResourceMaster.m_context.openFileOutput(fileName, Context.MODE_APPEND);
                output.write(("," + value).getBytes());
            }else{
                FileOutputStream output = ResourceMaster.m_context.openFileOutput(fileName, Context.MODE_PRIVATE);
                output.write(("" + value).getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResourceMaster.preferenceEditor.putInt(Constants.LAST_DATE, day);
        ResourceMaster.preferenceEditor.commit();
    }

    public static void writeMTTR(double value){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK)-1;
        String fileBase = "MTTR_STATISTICS_";
        String fileName = fileBase + day;
        File file = new File(ResourceMaster.m_context.getFilesDir(), fileName);
        if(ResourceMaster.preferences.getInt(Constants.LAST_DATE,-1) != day){
            resetFiles(day);
        }
        try {
            if(file.exists()) {
                FileOutputStream output = ResourceMaster.m_context.openFileOutput(fileName, Context.MODE_APPEND);
                output.write(("," + value).getBytes());
            }else{
                FileOutputStream output = ResourceMaster.m_context.openFileOutput(fileName, Context.MODE_PRIVATE);
                output.write(("" + value).getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResourceMaster.preferenceEditor.putInt(Constants.LAST_DATE, day);
        ResourceMaster.preferenceEditor.commit();
    }

    private static void resetFiles(int day){
        String fileBase = "TEMPERATURE_STATISTICS_";
        String fileName = fileBase + day;
        File file = new File(ResourceMaster.m_context.getFilesDir(), fileName);
        if(file.exists()) file.delete();

        fileBase = "DEWPOINT_STATISTICS_";
        fileName = fileBase + day;
        file = new File(ResourceMaster.m_context.getFilesDir(), fileName);
        if(file.exists()) file.delete();

        fileBase = "MTTR_STATISTICS_";
        fileName = fileBase + day;
        file = new File(ResourceMaster.m_context.getFilesDir(), fileName);
        if(file.exists()) file.delete();

    }
    public static void writeTemperatureDemo(String values){
        String fileBase = "TEMPERATURE_STATISTICS_";
        Scanner scanner = new Scanner(values);
        int index = 0;
        while(scanner.hasNextLine()) {
            String fileName = fileBase + (index++);
            try {
                FileOutputStream output = ResourceMaster.m_context.openFileOutput(fileName, Context.MODE_PRIVATE);
                output.write(scanner.nextLine().getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
