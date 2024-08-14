package com.abc.handoff;

import com.abc.pp.stringhandoff.*;
import com.programix.thread.*;

public class StringHandoffImpl implements StringHandoff {
    private String buffer;
    private boolean isFull;



    public StringHandoffImpl() {
        this.buffer = null;
        this.isFull = false;
    }

    @Override
    public synchronized void pass(String msg, long msTimeout)
        throws InterruptedException, TimedOutException, ShutdownException, IllegalStateException {


        boolean passedSucessfully = false;

        if (!isFull) {
            buffer = msg;

            while(buffer !=  msg) {
                wait(50L);
            }
            isFull = true;
            notifyAll();

        }

        if (msTimeout == 0L) {
            while (isFull) wait();
            // condition has been met
            buffer = msg;
            while(buffer !=  msg) {
                wait(50L);
            }
            isFull = true;
            notifyAll();
        } else {
            long msEndTime = System.currentTimeMillis() + msTimeout;
            long msRemaining = msTimeout;
            while (msRemaining >= 1L) {
                wait(msRemaining);
                if (!isFull) { // condition has been met
                    buffer = msg;
                    passedSucessfully = true;
                    msRemaining = 0L;
                    while(buffer !=  msg) {
                        wait(50L);
                    }
                    isFull = true;
                    notifyAll();
                }

                msRemaining = msEndTime - System.currentTimeMillis();
            }
            if (!passedSucessfully) {
                buffer = null;
                throw new TimedOutException(); // timed out

            }
        }


    }

    @Override
    public synchronized void pass(String msg) throws InterruptedException, ShutdownException, IllegalStateException {
        pass(msg, 0L);
    }

    @Override
    public synchronized String receive(long msTimeout)
        throws InterruptedException, TimedOutException, ShutdownException, IllegalStateException, RuntimeException {
        // Code used to ignore timeouts for now

        boolean receivedSuccessfully = false;
        if (isFull) { // condition has been met
            String returnString = buffer;
            buffer = null;
            isFull = false;
            notifyAll();
            return returnString;
        }

        if (msTimeout == 0L) {
            while (!isFull) wait();
            // condition has been met
            String returnString = buffer;
            buffer = null;
            isFull = false;
            notifyAll();
            return returnString;
        } else {
            long msEndTime = System.currentTimeMillis() + msTimeout;
            long msRemaining = msTimeout;
            while (msRemaining >= 1L) {

                wait(msRemaining);
                if (isFull) { // condition has been met
                    String returnString = buffer;
                    buffer = null;
                    isFull = false;
                    receivedSuccessfully = true;
                    notifyAll();
                    return returnString;
                }

                msRemaining = msEndTime - System.currentTimeMillis();
            }
            if (!receivedSuccessfully) {
                throw new TimedOutException(); // timed out
            }
            buffer = null;

        }
        return null;
    }

    @Override
    public synchronized String receive() throws InterruptedException, ShutdownException, IllegalStateException {
        return receive(0L);
    }

    @Override
    public synchronized void shutdown() {
    }

    @Override
    public Object getLockObject() {
        return this;
    }
}