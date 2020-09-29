package com.upstart13.legba;

import com.upstart13.legba.data.DataManager;
import com.upstart13.legba.data.dto.Mission;

import org.junit.Test;

import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DeserializerTest {

    @Test
    public void readJsonTest(){
        List<Mission> missions = new DataManager().getMissions();
        missions.get(0).channels.get(0).channelElements.stream().forEach(mission -> System.out.println(mission.toString() + "\n\n"));

        assert missions.size() == 9;
    }
}