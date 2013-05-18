package com.dbtsai.hadoop.util;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import  org.mockito.Mockito.mock;
import  org.mockito.Mockito.verify;

import org.junit.Test;

public class InMapperCombinerTest {
    @Test
    public void testInMapperCombinerCounter() {

        InMapperCombiner<Text, LongWritable> combiner = new InMapperCombiner<Text, LongWritable>();

        combiner.setCombiningFunction(
                new CombiningFunction<LongWritable>() {
                    @Override
                    public LongWritable combine(LongWritable value1, LongWritable value2) {
                        value1.set(value1.get() + value2.get());
                        return value1;
                    }
                }
        );

//        IAlpineLRUCache<Integer, Integer> cache = new AlpineLRUCache<Integer, Integer>(size);
//        for (int i = 0; i < size * 2; i++) {
//            cache.put(i, i);
//        }
//        Assert.assertTrue(cache.size() <= size);
//
//        Assert.assertTrue(null == cache.get(0));
//        Assert.assertTrue(null == cache.get(1));
//        Assert.assertTrue(null == cache.get(size - 3));
//
//        Assert.assertTrue(null != cache.get(size));
//        Assert.assertTrue(null != cache.get(size + 1));
//        Assert.assertTrue(null != cache.get(size + 3));
//        Assert.assertTrue(null != cache.get((size * 2) - 2));
    }
}
