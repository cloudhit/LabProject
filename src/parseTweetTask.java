/**
 * Created by yupengzhang on 12/12/15.
 */
import java.io.*;
import java.util.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class parseTweetTask implements Runnable{
    private int downside, upperside;
    public List<tweet> tweets;
    Tokenizer tokenizer;
    POSTaggerME tagger;
    private List<String> list;
    public parseTweetTask(int a, int b,List<String> list, TokenizerModel Tmodel, POSModel Pmodel){
        downside = a;
        upperside = b;
        this.tokenizer = new TokenizerME(Tmodel);
        this.tagger = new POSTaggerME(Pmodel);
        this.list = list;
        tweets = new ArrayList<>();
    }
    public void run(){
        long t1 = System.currentTimeMillis();
        try{
            for(int i = downside; i < upperside; i ++){
                tweet t = new tweet(list.get(i), tokenizer, tagger);
                tweets.add(t);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        long t2 = System.currentTimeMillis();
        System.out.println("upperside:" + upperside + " downside:" + downside + " most cost time:" + (t2 - t1));
    }
}
