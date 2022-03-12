package greeting.server;

import utils.Sleeper;

public class SleeperImpl implements Sleeper {

    @Override
    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
