/**
 * Created by yupengzhang on 11/25/15.
 */
import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

import com.sun.corba.se.impl.logging.IORSystemException;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.IDs;
import org.bson.types.ObjectId;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.cmdline.ModelLoader;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

public class generateDocuments {
    private List<String> users;
    private DBCollection coll;
    private List<String> documents;
    public generateDocuments(List<String> users, DBCollection coll){
        this.users = users;
        this.coll = coll;
        documents = new ArrayList<>();
    }
    public void writeToFile(String filename, List<String> tweets){
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for(String tmp : tweets) {
                out.write(tmp);
                out.newLine();
            }
            out.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
