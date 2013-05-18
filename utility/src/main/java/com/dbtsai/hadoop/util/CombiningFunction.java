package com.dbtsai.hadoop.util;

public interface CombiningFunction<VALUE> {
    public VALUE combine(VALUE value1, VALUE value2);
}
