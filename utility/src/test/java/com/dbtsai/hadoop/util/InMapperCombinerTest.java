package com.dbtsai.hadoop.util;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * See http://docs.mockito.googlecode.com/hg/latest/org/mockito/Mockito.html for Mockito example
 */

public class InMapperCombinerTest {
    Mapper.Context contextMock;
    InOrder inOrder;

    @Before
    public void setUp() {
        contextMock = mock(Mapper.Context.class);
        inOrder = inOrder(contextMock);
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

        /**
         *  Testing # of distinguish keys < cacheCapacity
         * */

        combiner.write(new Text("Apple"), new LongWritable(1), contextMock);
        combiner.write(new Text("Apple"), new LongWritable(3), contextMock);
        combiner.write(new Text("Apple"), new LongWritable(2), contextMock);

        combiner.write(new Text("Mango"), new LongWritable(7), contextMock);
        combiner.write(new Text("Mango"), new LongWritable(5), contextMock);

        // Since the size of InMapperCombiner is 3, all the key-value paris are in the LRU cache.
        verifyZeroInteractions(contextMock);

        // We force to flush out to the contextMock, and see if the result is combined.
        combiner.flush(contextMock);

        inOrder.verify(contextMock, times(1)).write(new Text("Apple"), new LongWritable(6));
        inOrder.verify(contextMock, times(1)).write(new Text("Mango"), new LongWritable(12));

        /**
         *  Testing # of distinguish keys > cacheCapacity
         * */

        combiner.write(new Text("Avocado"), new LongWritable(8), contextMock);
        combiner.write(new Text("Avocado"), new LongWritable(11), contextMock);

        combiner.write(new Text("Guava"), new LongWritable(13), contextMock);

        combiner.write(new Text("Jujube"), new LongWritable(17), contextMock);
        combiner.write(new Text("Jujube"), new LongWritable(3), contextMock);
        combiner.write(new Text("Jujube"), new LongWritable(5), contextMock);

        // Since we only have three distinguish keys, nothing is in contextMock.
        verifyZeroInteractions(contextMock);

        // The oldest key, Avocado is emitted to contextMock.
        combiner.write(new Text("Cherry"), new LongWritable(2), contextMock);
        inOrder.verify(contextMock, times(1)).write(new Text("Avocado"), new LongWritable(19));

        // If we add another new key here, Guava will be emitted since it's eldest.
        // However, we can add new record to Guava to fresh it,
        // and then Jujube will be emitted since it's the eldest.
        combiner.write(new Text("Guava"), new LongWritable(1), contextMock);
        verifyZeroInteractions(contextMock);

        combiner.write(new Text("Olive"), new LongWritable(6), contextMock);
        inOrder.verify(contextMock, times(1)).write(new Text("Jujube"), new LongWritable(25));


        combiner.write(new Text("Walnut"), new LongWritable(2), contextMock);
        combiner.write(new Text("Walnut"), new LongWritable(3), contextMock);
        combiner.write(new Text("Guava"), new LongWritable(9), contextMock);
        combiner.write(new Text("Coconut"), new LongWritable(7), contextMock);

        inOrder.verify(contextMock, times(1)).write(new Text("Cherry"), new LongWritable(2));
        inOrder.verify(contextMock, times(1)).write(new Text("Olive"), new LongWritable(6));

        combiner.flush(contextMock);
        inOrder.verify(contextMock, times(1)).write(new Text("Walnut"), new LongWritable(5));
        inOrder.verify(contextMock, times(1)).write(new Text("Guava"), new LongWritable(23));
        inOrder.verify(contextMock, times(1)).write(new Text("Coconut"), new LongWritable(7));
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
        combiner.write(new Text("Mango"), new LongWritable(7), contextMock);

        // Since there is no combining function, the InMapperCombiner will do nothing;
        // i.e., emit the result to context immediately without combining.
        inOrder.verify(contextMock, times(1)).write(new Text("Apple"), new LongWritable(1));
        inOrder.verify(contextMock, times(1)).write(new Text("Apple"), new LongWritable(3));
        inOrder.verify(contextMock, times(1)).write(new Text("Apple"), new LongWritable(2));


        inOrder.verify(contextMock, times(1)).write(new Text("Mango"), new LongWritable(7));
        inOrder.verify(contextMock, times(1)).write(new Text("Mango"), new LongWritable(5));
        inOrder.verify(contextMock, times(1)).write(new Text("Mango"), new LongWritable(7));
    }

    @Test
    public void testInMapperCombinerCountingErrorHandling() throws IOException, InterruptedException {
        /**
         *  Since we can not throw checked InterruptedException and IOException in LinkedHashMap,
         *  we convert it to unchecked exception, catch them in InMapperCombiner.write(), and then
         *  convert back to checked exception.
         * */
        final int cacheCapacity = 2;
        Throwable e = null;

        /**
         *  Test IOException()
         */
        doThrow(new IOException()).when(contextMock).write(new Text("Grape"), new LongWritable(3));

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

        // The following will not throw out the exception since all the key-value paris are in LRU cache.
        combiner.write(new Text("Grape"), new LongWritable(3), contextMock);
        combiner.write(new Text("Orange"), new LongWritable(1), contextMock);

        // Adding a record will emit Grape to contextMock, and it will throw IOException()
        try {
            combiner.write(new Text("Lemon"), new LongWritable(2), contextMock);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue(e instanceof IOException);

        /**
         *  Test InterruptedException()
         */
        e = null;
        doThrow(new InterruptedException()).when(contextMock).write(new Text("Orange"), new LongWritable(1));

        combiner = new InMapperCombiner<Text, LongWritable>(
                cacheCapacity,
                new CombiningFunction<LongWritable>() {
                    @Override
                    public LongWritable combine(LongWritable value1, LongWritable value2) {
                        value1.set(value1.get() + value2.get());
                        return value1;
                    }
                }
        );

        // The following will not throw out the exception since all the key-value paris are in LRU cache.
        combiner.write(new Text("Grape"), new LongWritable(8), contextMock);
        combiner.write(new Text("Orange"), new LongWritable(1), contextMock);

        combiner.write(new Text("Lemon"), new LongWritable(2), contextMock);
        inOrder.verify(contextMock, times(1)).write(new Text("Grape"), new LongWritable(8));

        // Adding a record will emit Orange to contextMock, and it will throw InterruptedException()
        try {
            combiner.write(new Text("Lime"), new LongWritable(8), contextMock);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue(e instanceof InterruptedException);
    }
}
