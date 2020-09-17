package com.upstart13.legba;

import android.util.Log;

import com.upstart13.legba.data.DataManager;
import com.upstart13.legba.data.dto.Mission;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void readJsonTest(){
        List<Mission> missions = new DataManager().getMissions();
        missions.stream().forEach(mission -> System.out.println(mission.toString()));
        assert missions.size() == 9;
    }
}