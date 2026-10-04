package com.analyticalrx.dataplatform.scheduler.utils.contract;

@FunctionalInterface
public interface Lease extends AutoCloseable {

    @Override
    void close();
}