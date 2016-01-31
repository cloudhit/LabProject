/**
 * Created by yupengzhang on 12/2/15.
 */
import java.io.*;
import java.util.ArrayList;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class tweet {

    protected int time;

    protected int[] tweetwords;
    public String[] tmp_words;
    public List<String> tokens;

    public tweet(String dataline, Tokenizer tokenizer, POSTaggerME tagger) {
        try {
            String process_str = Helper.preprocess(dataline);
            String[] tokens_pre = tokenizer.tokenize(process_str);
            tokens = new ArrayList<>();
            for(int i = 0; i < tokens_pre.length; i ++) {
                String tmpToken = tokens_pre[i].toLowerCase();
                if(!Stopwords.isStopword(tmpToken) && !Helper.isNoisy(tmpToken)){
                    tokens.add(tmpToken);
                }
            }
            String[]tags = tagger.tag(tokens.toArray(new String[tokens.size()]));
            Helper.setKinds();
            int index = 0;
            for(int i = 0; i < tokens.size(); i ++) {
                if(Helper.kinds.contains(tags[i]))
                   tokens.set(index++, tokens.get(i));
            }
            for(int i = tokens.size() - 1; i >= index; i --)
                tokens.remove(i);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addToSet(HashMap<String, Integer> tf ,int[] total, Set<String> user_words){
        tmp_words = new String[tokens.size()];
        for(int i = 0; i < tokens.size(); i ++){
            String tmpToken = tokens.get(i);
            if(!user_words.contains(tmpToken))
                user_words.add(tmpToken);
            tmp_words[i] = tmpToken;
        }

        //tf
        for(String word :tmp_words){
            int count = tf.containsKey(word)? tf.get(word) + 1:1;
            tf.put(word, count);
        }
        total[0] += tmp_words.length;
    }

    public void assignWord(HashMap<String, Integer> wordMap){
        tweetwords = new int[tmp_words.length];
        for(int i = 0; i < tmp_words.length; i ++){
            tweetwords[i] = wordMap.get(tmp_words[i]);
        }
    }



}
