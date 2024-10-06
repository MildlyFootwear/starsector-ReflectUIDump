package Shoey.UIDump;

import Shoey.UIDump.Kotlin.Kot;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class EFS implements EveryFrameScript {

    boolean donedid = false;
    Kot Kot = new Kot();
    float runTime = 0;
    float bindingpressedtime = 20;
    Logger log = Global.getLogger(this.getClass());
    String lastdump = "";

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        runTime += amount;
        bindingpressedtime += amount;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            if (donedid || bindingpressedtime < 10) {
                bindingpressedtime = 0;
                return;
            }
            donedid = true;
            String s;

            if (Global.getSector().getCampaignUI().getCurrentCoreTab() == null)
                s = "null";
            else
                s = Global.getSector().getCampaignUI().getCurrentCoreTab().name();
            String UIDump;

            Kot.hookCore();

            UIDump = Kot.dump(null);

            if (UIDump.length() > 0) {
                int loop = 0;
                while (true) {
                    loop++;
                    int start = 1000000 * (loop - 1);
                    int end = 1000000 * loop;
                    if (end > UIDump.length()) {
                        end = UIDump.length();
                    }
                    try {
                        Global.getSettings().writeTextFileToCommon("UIDump " + s + " " + runTime + " " + loop, UIDump.substring(start, end));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                    if (1000000 * loop > end) {
                        break;
                    }
                }
                lastdump = UIDump;
            } else {
                log.info("Dump lengths too similar");
                return;
            }
            log.info("Dump finished, length "+UIDump.length() + " to "+("UIDump " + s + " " + runTime));
        }
        donedid = false;
    }
}
