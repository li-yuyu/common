package com.lyle.common.lang.lock;

public interface Lock {

    boolean lock();

    boolean lock(long timeout);

    boolean lock(long timeout, long expire);

    void unlock();

}
