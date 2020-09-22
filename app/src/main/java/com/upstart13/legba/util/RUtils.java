package com.upstart13.legba.util;

import com.upstart13.legba.R;
import com.upstart13.legba.data.dto.Channel;

public class RUtils {

    public static int getImageResource(Channel channel){
        if(channel.image == null)return android.R.color.transparent;
        switch (channel.image){
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
}
