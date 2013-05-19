package com.dbtsai.hadoop.mapreduce;

import com.dbtsai.hadoop.util.InMapperCombiner;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.StringTokenizer;


public class WordCountMapper {

    public static class WordCountMapperWithoutInMapperCombiner extends Mapper<LongWritable, Text, Text, LongWritable> {
        private final static LongWritable one = new LongWritable(1);
        private Text word = new Text();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, one);
            }
        }
    }

    public static class WordCountMapperWithInMapperCombiner extends Mapper<LongWritable, Text, Text, LongWritable> {
        private final static LongWritable one = new LongWritable(1);
        private Text word = new Text();
        private InMapperCombiner combiner;


        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                combiner.write(word, one, context);
            }
        }
    }

    public static class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        public void reduce(Text key, Iterable<LongWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
            context.write(key, new LongWritable(sum));
        }
    }
}
