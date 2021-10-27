package com.example.discuter.UtilityClasses;

public class OtpGenerator {

    public static String generateOTP()
    {
        int min=1000,max=9999;
        int OTP = (int)(Math.random()*(max-min+1)+min);

        return String.valueOf(OTP);
    }
}
