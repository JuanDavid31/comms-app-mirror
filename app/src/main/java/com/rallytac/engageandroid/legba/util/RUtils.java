package com.rallytac.engageandroid.legba.util;


import com.rallytac.engageandroid.R;

public class RUtils {

    public static int getImageResource(String imageName){
        if(imageName == null)return android.R.color.transparent;
        switch (imageName){
            case "primary":
                return R.mipmap.primary_channel_thumb;
            case  "secondary":
                return R.mipmap.secondary_channel_thumb;
            case "tertiary":
                return R.mipmap.tertiary_channel_thumb;
            case "quaternary":
                return R.mipmap.quaternary_channel_thumb;
            case "quinary":
                return R.mipmap.quinary_channel_thumb;
            default:
                return android.R.color.transparent;
        }
    }

    public static int getImageNFCActionResource(String imageName) {
        if(imageName == null)return android.R.color.transparent;
        switch (imageName){
            case "btn1":
                return R.mipmap.nfc_action_copy_5;
            case "btn2":
                return R.mipmap.nfc_action_copy_6;
            case "btn3":
                return R.mipmap.nfc_action_copy_7;
            case "btn4":
                return R.mipmap.nfc_action_copy_8;
            default:
                return android.R.color.transparent;
        }
    }
}
