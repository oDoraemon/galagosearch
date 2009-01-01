// BSD License (http://www.galagosearch.org/license)

package org.galagosearch.tupleflow.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.galagosearch.tupleflow.Counter;

/**
 *
 * @author trevor
 */
public class NetworkedCounterManager implements Runnable {
    HashMap<String, NetworkedCounter> counters = new HashMap<String, NetworkedCounter>();
    boolean stop = false;
    int sleepInterval = 1000;
    Thread thread;

    public synchronized Counter newCounter(
            String counterName, String stageName, String instance, String url) {
        String key = String.format("%s-%s-%s", counterName, stageName, instance);
        if (counters.containsKey(key))
            return counters.get(key);
        NetworkedCounter counter = new NetworkedCounter(counterName, stageName, instance, url);
        counters.put(key, counter);
        return counter;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        stop = true;
        if (thread != null)
            thread.interrupt();
    }

    public void run() {
        boolean finished = false;
        while (!finished) {
            synchronized(this) {
                // By setting the finished flag here, we ensure that counters get
                // flushed when execution ends.
                if (stop) finished = true;

                for (NetworkedCounter counter : counters.values()) {
                    counter.flush();
                }
            }

            try {
                Thread.sleep(sleepInterval);
            } catch (InterruptedException ex) {
                // it's probably time to flush and quit now
            }
        }
    }
}
