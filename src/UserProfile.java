/**
 * Created by yupengzhang on 11/24/15.
 */
import java.net.UnknownHostException;
import java.util.*;
import java.io.*;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
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
import twitter4j.conf.ConfigurationBuilder;

public class UserProfile {
    private String id_str;
    private String screen_name;
    private List<String> list;
    private List<String> sentences;
    private Map<String, HashMap<String, Integer>> graph;
    private Twitter twitter;
    private RequestToken requestToken;
    private List<tweet> tweets;
    private HashMap<String, Float> tf;
    private TokenizerModel Tmodel;
    private POSModel Pmodel;
    private Tokenizer tokenizer;
    private POSTaggerME tagger;

    public Set<String> user_words;

    public void init()throws TwitterException, IOException{
        list = new ArrayList<String>();
        sentences = new ArrayList<String>();
        graph = new HashMap();
        tweets = new ArrayList<>();
    }
    public void set(List<String> info){
        list.clear();
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(info.get(0))
                .setOAuthConsumerSecret(info.get(1))
                .setOAuthAccessToken(info.get(2))
                .setOAuthAccessTokenSecret(info.get(3));
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter  = tf.getInstance();
        try {
            // get request token.
            // this will throw IllegalStateException if access token is already available
            requestToken = twitter.getOAuthRequestToken();
        } catch (IllegalStateException ie) {
            // access token is already available, or consumer key/secret is not set.
            if (!twitter.getAuthorization().isEnabled()) {
                System.out.println("OAuth consumer key/secret is not set.");
                System.exit(-1);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public UserProfile(){
        try {
            init();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setUserProfile(long id, TokenizerModel Tmodel, POSModel Pmodel) throws TwitterException, IOException{
        id_str = String.valueOf(id);
        User user = twitter.showUser(Long.parseLong(id_str));
        screen_name = user.getScreenName();
        this.Tmodel = Tmodel;
        this.Pmodel = Pmodel;
        tokenizer = new TokenizerME(Tmodel);
        tagger = new POSTaggerME(Pmodel);
    }
    public void setUserProfile(String name, TokenizerModel Tmodel, POSModel Pmodel) throws TwitterException, IOException{
        screen_name = name;
        User user = twitter.showUser(name);
        id_str = String.valueOf(user.getId());
        this.Tmodel = Tmodel;
        this.Pmodel = Pmodel;
        tokenizer = new TokenizerME(Tmodel);
        tagger = new POSTaggerME(Pmodel);
    }
    public void print(Object o){
        System.out.println(o);
    }
    /*
    public boolean judgeExist(){
        DBCursor cur = coll.find(new BasicDBObject("user.id_str", id_str));
        return cur.hasNext();
    }
    /*
    public void getTextByDatabase(int count){
        BasicDBObject keys = new BasicDBObject();
        keys.put("text", 1);
        DBCursor cur = coll.find(new BasicDBObject("user.id_str", id_str), keys).limit(count);
        while(cur.hasNext()) {
            list.add(String.valueOf(cur.next().get("text")));
        }
    }*/

    public void getTextByAPI(int count) throws TwitterException{
        Paging pg = new Paging();
        int numberOfTweets = count, pre = -1;
        pg.setCount(numberOfTweets);
        long lastID = Long.MAX_VALUE;
        List<Status> tweets = new ArrayList();
        while (tweets.size() < numberOfTweets - 5) {
            tweets.addAll(twitter.getUserTimeline(screen_name, pg));
            for (Status t : tweets)
                if (t.getId() < lastID) lastID = t.getId();
            pg.setMaxId(lastID - 1);
            if(tweets.size() == pre) break;
            pre = tweets.size();
        }
        for (Status tmp : tweets) {
            list.add(tmp.getText());
        }
    }

    public List<String> getTweets(int count) throws TwitterException{
        getTextByAPI(count);
        return list;
    }


    public void parseText(){
        try {
            InputStream is = new FileInputStream("/Users/yupengzhang/Documents/tool/eclipse_workplace/hobby/src/en-sent.bin");
            SentenceModel Smodel = new SentenceModel(is);
            SentenceDetectorME sdetector = new SentenceDetectorME(Smodel);
            int cur = 0;
            for (String tmp : list) {
                String tmp_store[] = sdetector.sentDetect(tmp);
                for (String elem : tmp_store) {
                    sentences.add(elem);
                }
            }
            is.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseTweetsP1(){
        try {
            tf = new HashMap<>();
            HashMap<String, Integer> tf_tmp = new HashMap<>();
            int[] total = new int[1];
            user_words = new HashSet<>();


            int size = 1;
            if(list.size() == 0){
                System.out.println("error:" + size);
                return;
            }
            int block_size = list.size() / size;

            parseTweetTask[] pT = new parseTweetTask[size];
            for(int i = 0; i < size; i ++){
                if(i != size - 1)
                    pT[i] = new parseTweetTask(i * block_size, (i + 1) * block_size, list, Tmodel, Pmodel);
                else
                    pT[i] = new parseTweetTask(i * block_size, list.size(), list, Tmodel, Pmodel);
            }



            Thread[] h = new Thread[size];
            for(int i = 0; i < size; i ++){
                h[i] = new Thread(pT[i]);
                h[i].start();
            }
            try{
                for(Thread t : h)
                    t.join();
            }catch(Exception e){
                e.printStackTrace();
            }



            for(int i = 0; i < size; i ++){
                List<tweet> tmpset = pT[i].tweets;
                for(tweet t : tmpset){
                    t.addToSet(tf_tmp, total, user_words);
                    tweets.add(t);
                }
            }

            for(tweet t : tweets){
                t.addToSet(tf_tmp, total, user_words);
            }

            for(String tmp : tf_tmp.keySet()){
                tf.put(tmp, Float.valueOf((float)(tf_tmp.get(tmp))/ total[0]));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void parseTweetsP2(HashMap<String, Integer> wordMap){
        for(int i = 0; i < tweets.size(); i ++)
            tweets.get(i).assignWord(wordMap);
    }

    public HashMap<String, Float> getTf(){
        return tf;
    }

    public List<tweet> getTweets2(){
        return tweets;
    }
    public List<String> getSentences(){return sentences;}
    public String getScreen_name(){
        return screen_name;
    }
    public Tokenizer getTokenizer() {return tokenizer;}
    public POSTaggerME getPOS(){return tagger;}
    public Map<String, HashMap<String, Integer>> getGraph(){return graph;}


    public List<String> getFriends() throws TwitterException{
        List<String> res = new ArrayList();
        IDs ids;
        long cursor = -1;
        int num = 50, cur = 0;
        do {
            ids = twitter.getFriendsIDs(Long.parseLong(id_str), cursor);
            for (long id : ids.getIDs()) {
                res.add(String.valueOf(id));
                cur++;
                if (cur > num) break;
            }
            if (cur > num) break;
        } while ((cursor = ids.getNextCursor()) != 0);
        return res;
    }

    public List<String> getCloseFriends(){
        final HashMap<String, Integer> userFrequence = new HashMap<>();
        List<String> names = new ArrayList<>();

        for(int i = 0; i < list.size(); i ++){
            Helper.findScreenName(list.get(i), names);
            for(String name : names){
                int curFre = (userFrequence.containsKey(name))? userFrequence.get(name) + 1:1;
                userFrequence.put(name, curFre);
            }
            names.clear();
        }
        int m = userFrequence.size();
        PriorityQueue<String> myque = new PriorityQueue<>(m, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return userFrequence.get(o1) < userFrequence.get(o2)? 1: -1;
            }
        });
        for(String tmp : userFrequence.keySet()){
            myque.add(tmp);
        }
        while(!myque.isEmpty()){
            userFrequence.get(myque.peek());
            if(names.size() > 20) break;
            names.add(myque.poll());
        }
        return names;
    }

    public List<String> getBlockFriends(){
        final HashMap<String, Integer> userFrequence = new HashMap<>();
        List<String> names = new ArrayList<>();

        for(int i = 0; i < list.size(); i ++){
            Helper.findScreenName(list.get(i), names);
            for(String name : names){
                int curFre = (userFrequence.containsKey(name))? userFrequence.get(name) + 1:1;
                userFrequence.put(name, curFre);
            }
            names.clear();
        }
        int m = userFrequence.size();
        PriorityQueue<String> myque = new PriorityQueue<>(m, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return userFrequence.get(o1) > userFrequence.get(o2)? 1: -1;
            }
        });
        for(String tmp : userFrequence.keySet()){
            myque.add(tmp);
        }
        while(!myque.isEmpty()){
            userFrequence.get(myque.peek());
            if(names.size() > 20) break;
            names.add(myque.poll());
        }
        return names;
    }

}
