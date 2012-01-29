package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 *
 * @author MattK
 * @version $Id: SequenceTester.java 185 2006-01-23 23:03:18Z rambaut $
 */
public class SequenceTester {
    public static String readSequence(String name) {
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(name));
            String sq1 = "";

            String line = br1.readLine();
            if (!((line.substring(0, 1)).equals(">")))
                sq1 += line;
            line = br1.readLine();
            while (line != null) {
                if (line.substring(0, 1).equals(">")) break;
                sq1 = sq1.concat(line.trim());
                line = br1.readLine();
            }
            return sq1;
        }
        catch (Exception e) {
            return "";
        }


    }

    public static Sequence getTestSequence1() {

     return new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("sequence1"),
            "PLQMKKTAFTLLLFIALTLTTSPLVNGSEKSEEINEKDLRKKSELQGTALGNLKQIYYYNEKAKTENKES" +
                    "HDQFLQHTILFKGFFTDHSWYNDLLVDFDSKDIVDKYKGKKVDLYGAYYGYQCAGGTPNKTACMYGGVTL" +
                    "HDNNRLTEEKKVPINLWLDGKQNTVPLETVKTNKKNVTVQELDLQARRYLQEKYNLYNSDVFDGKVQRGL" +
                    "IVFHTSTEPSVNYDLFGAQGQYSNTLLRIYRDNKTISSENMHIDIYLYTSY");
    }

    public static Sequence getTestSequence2() {
        return new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("sequence2"),
            "MYKRLFISHVILIFALILVISTPNVLAESQPDPKPDELHKSSKFTGLMENMKVLYDDNHVSAINVKSIDQ" +
                    "FLYFDLIYSIKDTKLGNYDNVRVEFKNKDLADKYKDKYVDVFGANYYYQCYFSKKTNDINSHQTDKRKTC" +
                    "MYGGVTEHNGNQLDKYRSITVRVFEDGKNLLSFDVQTNKKKVTAQELDYLTRHYLVKNKKLYEFNNSPYE" +
                    "TGYIKFIENENSFWYDMMPAPGDKFDQSKYLMMYNDNKMVDSKDVKIEVYLTTKKK");
    }

    public static String getTestSequence1(String[] arguments) {
        if (arguments.length> 0) return readSequence (arguments [0]);
        return getTestSequence1().getString();
    }
    public static String getTestSequence2(String[] arguments) {
        if (arguments.length> 1) return readSequence (arguments [1]);
        return getTestSequence2().getString();
    }
   
}
