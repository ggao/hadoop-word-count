package com.dbtsai.hadoop;

import com.dbtsai.hadoop.mapreduce.WordCountMR;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.junit.Test;

import java.io.IOException;

public class WordCountInMemoryHadoopTest {
    @Test
    public void testWordCount() throws IOException, ClassNotFoundException, InterruptedException {

        Configuration conf = new Configuration();

        Job job = new Job(conf, "Word Count with Combiner");

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        job.setMapperClass(WordCountMR.WordCountMapper.class);
        job.setReducerClass(WordCountMR.WordCountReducer.class);
        job.setCombinerClass(WordCountMR.WordCountReducer.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(""));
        FileOutputFormat.setOutputPath(job, new Path(""));

        job.waitForCompletion(true);
    }

    @Test
    public void testWordCountWithInMapperCombiner() throws IOException, ClassNotFoundException, InterruptedException {

        Configuration conf = new Configuration();

        Job job = new Job(conf, "Word Count with Combiner");

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        job.setMapperClass(WordCountMR.WordCountMapper.class);
        job.setReducerClass(WordCountMR.WordCountReducer.class);
        job.setCombinerClass(WordCountMR.WordCountReducer.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(""));
        FileOutputFormat.setOutputPath(job, new Path(""));

        job.waitForCompletion(true);
    }
}