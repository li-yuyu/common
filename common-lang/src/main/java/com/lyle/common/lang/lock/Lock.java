package com.lyle.common.lang.lock;

import java.util.concurrent.TimeUnit;

public interface Lock {

    /**
     * 获取锁，获取不到等待
     */
    void lock() throws InterruptedException;

    /**
     * 尝试获取锁一次
     *
     * @return 是否成功
     */
    boolean tryLock();

    /**
     * 尝试在超时时间之内获取锁
     *
     * @param timeout 超时时间，超时返回false
     * @return 是否成功
     */
    boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 解锁
     */
    void unlock();

}
