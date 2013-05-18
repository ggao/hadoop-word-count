package com.dbtsai.hadoop.util;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InMapperCombinerTest {
    Mapper.Context contextMock;

    @Before
    public void setUp() {
        contextMock = mock(Mapper.Context.class);
    }

    @Test
    public void testInMapperCombinerCounting() throws IOException, InterruptedException {
        final int cacheCapacity = 3;

        InMapperCombiner<Text, LongWritable> combiner = new InMapperCombiner<Text, LongWritable>(
                cacheCapacity,
                new CombiningFunction<LongWritable>() {
                    @Override
                    public LongWritable combine(LongWritable value1, LongWritable value2) {
                        value1.set(value1.get() + value2.get());
                        return value1;
                    }
                }
        );

        combiner.write(new Text("Apple"), new LongWritable(1), contextMock);
        combiner.write(new Text("Apple"), new LongWritable(3), contextMock);
        combiner.write(new Text("Apple"), new LongWritable(2), contextMock);

        combiner.write(new Text("Mango"), new LongWritable(7), contextMock);
        combiner.write(new Text("Mango"), new LongWritable(5), contextMock);

        // Since the size of InMapperCombiner is 3, all the key-value paris are in the LRU cache.
        // We force to flush out to the contextMock, and see if the result is combined.
        combiner.flush(contextMock);

        verify(contextMock).write(new Text("Apple"), new LongWritable(6));
        verify(contextMock).write(new Text("Mango"), new LongWritable(12));



//        Abiu
//                Apple
//        Almond
//        Amla (Indian gooseberry)
//        Apricot
//                Avocado
//        Bael
//        Ber (Indian plum)
//        Carambola (starfruit)
//        Cashew
//                Cherry
//        Citrus (orange, lemon, lime, etc.)
//        Coconut
//                Durian
//        Fig
//                Guava
//        Grapefruit
//                Jujube
//        Jackfruit
//                Loquat
//        Lychee
//                Mango
//        Medlar
//        Morello cherry
//        Mulberry
//                Olive
//        Pawpaw, both the tropical Carica papaya and the North American Asimina triloba
//        Pear
//        Peach and nectarine
//                Persimmon
//        Plum
//                Pomegranate
//        Rambutan
//        Sapodilla (chikoo)
//        Soursop
//        Sugar-apple (sharifa)
//        Sweet chestnut
//        Tamarillo
//                Walnut

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

    @Test
    public void testInMapperCombinerCountingWithoutCombiningFunction() throws IOException, InterruptedException {
        final int cacheCapacity = 5;

        InMapperCombiner<Text, LongWritable> combiner = new InMapperCombiner<Text, LongWritable>(cacheCapacity);

        combiner.write(new Text("Apple"), new LongWritable(1), contextMock);
        combiner.write(new Text("Apple"), new LongWritable(3), contextMock);
        combiner.write(new Text("Apple"), new LongWritable(2), contextMock);

        combiner.write(new Text("Mango"), new LongWritable(7), contextMock);
        combiner.write(new Text("Mango"), new LongWritable(5), contextMock);

        // Since there is no combining function, the InMapperCombiner will do nothing;
        // i.e., emit the result to context immediately without combining.

        verify(contextMock).write(new Text("Apple"), new LongWritable(1));
        verify(contextMock).write(new Text("Apple"), new LongWritable(3));
        verify(contextMock).write(new Text("Apple"), new LongWritable(2));

        verify(contextMock).write(new Text("Mango"), new LongWritable(7));
        verify(contextMock).write(new Text("Mango"), new LongWritable(5));
    }
}
