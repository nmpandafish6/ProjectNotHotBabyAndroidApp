package com.cecs490.pnhb.projectnothotbaby;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Nicolas on 3/18/2018.
 */

public class StatisticsHelper {

    public static class StatisticsPackage {
        public double max;
        public double mean;
        public double min;
    }

    public static StatisticsPackage getTemperatureStatistics(){
        StatisticsPackage stats_package = new StatisticsPackage();
        String fileBase = "TEMPERATURE_STATISTICS_";
        int dayCount = 0;
        double weekAverage = 0;
        double weekHigh = Double.MIN_VALUE;
        double weekLow = Double.MAX_VALUE;
        for(int i = 0; i < 7; i++){
            String fileName = fileBase + i;
            try {
                FileInputStream input = ResourceMaster.m_context.openFileInput(fileName);
                dayCount++;
                double dayAverage = 0;
                double dayHigh = Double.MIN_VALUE;
                double dayLow = Double.MAX_VALUE;
                int valueCount = 0;
                Scanner scanner = new Scanner(input);
                scanner.useDelimiter(",");
                while(scanner.hasNext()){
                    double current = 0;
                    try {
                        current = Double.parseDouble(scanner.next());
                    }catch(Exception e){
                        continue;
                    }
                    valueCount++;
                    dayAverage += current;
                    dayHigh = Math.max(current,dayHigh);
                    dayLow = Math.min(current,dayLow);
                }
                dayAverage /= valueCount;
                weekAverage += dayAverage;
                weekHigh = Math.max(weekHigh,dayHigh);
                weekLow = Math.min(weekLow,dayLow);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        weekAverage /= dayCount;
        stats_package.max = weekHigh != Double.MAX_VALUE ? weekHigh : 0;
        stats_package.min = weekLow != Double.MAX_VALUE ? weekLow : 0;
        stats_package.mean = dayCount != 0 ? weekAverage : 0;
        return stats_package;
    }

    public static StatisticsPackage getDewPointStatistics(){
        StatisticsPackage stats_package = new StatisticsPackage();
        String fileBase = "DEWPOINT_STATISTICS_";
        int dayCount = 0;
        double weekAverage = 0;
        double weekHigh = Double.MIN_VALUE;
        double weekLow = Double.MAX_VALUE;
        for(int i = 0; i < 7; i++){
            String fileName = fileBase + i;
            try {
                FileInputStream input = ResourceMaster.m_context.openFileInput(fileName);
                dayCount++;
                double dayAverage = 0;
                double dayHigh = Double.MIN_VALUE;
                double dayLow = Double.MAX_VALUE;
                int valueCount = 0;
                Scanner scanner = new Scanner(input);
                scanner.useDelimiter(",");
                while(scanner.hasNext()){
                    double current = 0;
                    try {
                        current = Double.parseDouble(scanner.next());
                    }catch(Exception e){
                        continue;
                    }
                    valueCount++;
                    dayAverage += current;
                    dayHigh = Math.max(current,dayHigh);
                    dayLow = Math.min(current,dayLow);
                }
                dayAverage /= valueCount;
                weekAverage += dayAverage;
                weekHigh = Math.max(weekHigh,dayHigh);
                weekLow = Math.min(weekLow,dayLow);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        weekAverage /= dayCount;
        stats_package.max = weekHigh != Double.MAX_VALUE ? weekHigh : 0;
        stats_package.min = weekLow != Double.MAX_VALUE ? weekLow : 0;
        stats_package.mean = dayCount != 0 ? weekAverage : 0;
        return stats_package;
    }

    public static StatisticsPackage getMTTRStatistics(){
        StatisticsPackage stats_package = new StatisticsPackage();
        String fileBase = "MTTR_STATISTICS_";
        int dayCount = 0;
        double weekAverage = 0;
        double weekHigh = Double.MIN_VALUE;
        double weekLow = Double.MAX_VALUE;
        for(int i = 0; i < 7; i++){
            String fileName = fileBase + i;
            try {
                FileInputStream input = ResourceMaster.m_context.openFileInput(fileName);
                dayCount++;
                double dayAverage = 0;
                double dayHigh = Double.MIN_VALUE;
                double dayLow = Double.MAX_VALUE;
                int valueCount = 0;
                Scanner scanner = new Scanner(input);
                scanner.useDelimiter(",");
                while(scanner.hasNext()){
                    double current = 0;
                    try {
                        current = Double.parseDouble(scanner.next());
                    }catch(Exception e){
                        continue;
                    }
                    valueCount++;
                    dayAverage += current;
                    dayHigh = Math.max(current,dayHigh);
                    dayLow = Math.min(current,dayLow);
                }
                dayAverage /= valueCount;
                weekAverage += dayAverage;
                weekHigh = Math.max(weekHigh,dayHigh);
                weekLow = Math.min(weekLow,dayLow);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        weekAverage /= dayCount;
        stats_package.max = weekHigh != Double.MAX_VALUE ? weekHigh : 0;
        stats_package.min = weekLow != Double.MAX_VALUE ? weekLow : 0;
        stats_package.mean = dayCount != 0 ? weekAverage : 0;
        return stats_package;
    }
}
