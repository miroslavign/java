package name.abhijitsarkar.learning.hadoop.join;

import name.abhijitsarkar.learning.hadoop.join.CustomerMapper;
import name.abhijitsarkar.learning.hadoop.join.TaggedKey;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Test;

public class CustomerMapperTest {
    private final CustomerMapper mapper = new CustomerMapper();
    private final MapDriver<LongWritable, Text, TaggedKey, Text> mapDriver = new MapDriver<LongWritable, Text, TaggedKey, Text>();

    @Test
    public void testMapValidRecord() {
        mapDriver
                .withMapper(mapper)
                .withInput(
                        new LongWritable(1L),
                        new Text(
                                "1,Stephanie Leung,555-555-5555,123 No Such St,Fantasyland,CA 99999"))
                .withOutput(
                        new TaggedKey(1, CustomerMapper.SORT_ORDER),
                        new Text(
                                "Stephanie Leung,555-555-5555,123 No Such St,Fantasyland,CA 99999"))
                .runTest();
    }

    @Test
    public void testMapSkipHeader() {
        mapDriver
                .withMapper(mapper)
                .withInput(new LongWritable(1L),
                        new Text("CUST_ID,CUST_NAME,CUST_PH_NUM,CUST_ADDR"))
                .runTest();
    }
}
