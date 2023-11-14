package com.android.devicelevel;


import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class DeviceUtil {
    public static int getDeviceLevel(Context ctx){
        int score = getDeviceBenchmarkLevel(ctx);
        if (score > 35) {
            return 5;
        }
        if (score > 25) {
            return 4;
        }
        if (score > 16) {
            return 3;
        }
        return score <= 15 ? 1 : 2;
    }

    public static int getDeviceBenchmarkLevel(Context ctx) {
        int score = 0 ;
        try {
            int coreNum = DeviceUtil.getCpuCoreNum();
            if(coreNum <= 0) {
                return -1;
            }
            int cpuFreq = (int)(DeviceUtil.getCpuMaxFreq() / 100000L);
            if(cpuFreq <= 0) {
                return -1;
            }
            int ramSize = (int)(DeviceUtil.getTotalMemory(ctx) / 0x100000L);
            if(ramSize <= 0) {
                return -1;
            }
            score = (coreNum * 200 + (cpuFreq * cpuFreq * 10) + ((ramSize / 0x400) * (ramSize / 0x400) * 100) ) / 400;
            Log.i("Papm.DeviceUtil", "getDeviceBenchmarkLevel coreNum:" + coreNum + " cpuFreq:" + cpuFreq + " ramSize:" + ramSize + " score:" + score);
            return score;
        }
        catch(Throwable v9) {
            v9.printStackTrace();
        }
        return -1;
    }

    public static int getCoreNum(String arg4) {
        int coreMaxInd;
        FileInputStream is = null;
        try {
            is = new FileInputStream(arg4);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String v1 = br.readLine();
            br.close();
            if(v1 != null && v1.matches("0-[\\d]+$")) {
                coreMaxInd = Integer.parseInt(v1.substring(2));
                return coreMaxInd + 1;
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    private static long getTotalMemory(Context ctx) {
        ActivityManager am = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.totalMem;
    }

    private static long getCpuMaxFreq() {
        long cpuMaxFreq = 0;
        for(int i =0;  i< getCpuCoreNum(); i++){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq"), "UTF-8"));
                String v1 = br.readLine();
                br.close();
                if(v1 != null) {
                    long v2 = Long.parseLong(v1);
                    if(v2 > cpuMaxFreq) {
                        cpuMaxFreq = v2;
                    }
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }

        }
        return cpuMaxFreq;
    }

    public static int GetCPUFileNum(String arg1) {
        File[] v1 = new File(arg1).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return Pattern.matches("cpu[0-9]", file.getName());
            }
        });
        return v1 == null ? 0 : v1.length;
    }
    private static int getCpuCoreNum() {
        int coreNum = 0;
        try {
            coreNum =  getCoreNum("/sys/devices/system/cpu/possible");
            if (coreNum != 0) {
                return coreNum;
            }
            coreNum = getCoreNum("/sys/devices/system/cpu/present");
            if (coreNum != 0) {
                return coreNum;
            }

            coreNum = GetCPUFileNum("/sys/devices/system/cpu/");

        }catch (Exception e) {
            e.printStackTrace();
        }
        return coreNum ==0 ? 1 : coreNum;
    }
}
