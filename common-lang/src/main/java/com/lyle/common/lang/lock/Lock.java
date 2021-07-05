package com.lyle.common.lang.lock;

public interface Lock {

    /**
     * 获取锁，获取不到等待
     */
    void lock();

    boolean tryLock();

    boolean tryLock(long timeout);

    void unlock();

}
