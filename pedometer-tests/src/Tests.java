import android.graphics.Path;
import pedometer.droid.algorithm.common.IDetector;
import pedometer.droid.algorithm.fall.FallDetector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by bursant on 03.06.14.
 */
public class Tests {

    public static String[] fallTestcases = {
            "pedometer-tests/data/falls/testcase1",
            "pedometer-tests/data/falls/testcase1"
    };

    public static void main(String[] args) throws Exception{

        IDetector fallDetector = new FallDetector();

        for(String testcase : fallTestcases){
            InputStream fis = new FileInputStream(testcase);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.defaultCharset()));
            int falls = Integer.parseInt(br.readLine());
            int count = 0;
            String line;

            while ((line = br.readLine()) != null) {
                ;
            }

            String msg = "";
            if(falls == count)
                msg = "PASS";
            else
                msg = "FAIL";

            System.out.println(testcase + "\t" + msg);
        }

    }
}
