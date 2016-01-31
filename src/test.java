/**
 * Created by yupengzhang on 11/23/15.
 */
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.UnknownHostException;
import java.util.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.mongodb.DBCollection;
import com.mongodb.MongoException;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import twitter4j.TwitterException;
@Path("/test")
public class test {
    //taylorswift13
    //MikeTyson
    //ThierryHenry
    //DOTA2
    //JaJimPa
    //ArnoldUpdate
    //dwayne_Douglas
    //Plaid_Page
    //YaoMing
    //kobebryant
    //DalaiLama
    //markzackerberg
    public getConnected gC;
    public DBCollection coll;
    public String name = "taylorswift13";
    private List<String> ans;
    private HashMap<String, Integer> topics;
    public void driver1(TokenizerModel Tmodel, POSModel Pmodel){
        UserProfile up = null;
        List<String> info = new ArrayList();
        Helper.robinAccessToken(info);

        while(true) {
            try {
                up = new UserProfile();
                up.set(info);
                up.setUserProfile(name, Tmodel, Pmodel);
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (e.getMessage().contains("rate limit")) {
                    info.clear();
                    Helper.robinAccessToken(info);
                    System.out.println("peng");
                }else break;
            }
        }

        while(true) {
            try {
                up.getTweets(2000);
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (e.getMessage().contains("rate limit")) {
                    info.clear();
                    Helper.robinAccessToken(info);
                    System.out.println("peng");
                    up.set(info);
                }else break;
            }
        }



        List<String> names = up.getCloseFriends();
        ArrayList<UserProfile> users = new ArrayList<>();
        for(int i = 0; i < names.size(); i ++) {
            UserProfile user_p = null;
            boolean flag = true;
            while (true) {
                try {
                    user_p = new UserProfile();
                    user_p.set(info);
                    user_p.setUserProfile(names.get(i), Tmodel, Pmodel);
                    break;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (e.getMessage().contains("rate limit")) {
                        info.clear();
                        Helper.robinAccessToken(info);
                        System.out.println("peng");
                    } else {
                        flag = false;
                        break;
                    }
                }
            }
            if(flag)
                users.add(user_p);
        }


        System.out.println("size:" + users.size());
        int size = users.size(), block_size = size / Math.min(size, Helper.thread_num),
                remain = size - size/block_size * block_size, num = (remain == 0)?size/block_size:size/block_size + 1;
        getTweetsTask[] gT = new getTweetsTask[num];

        boolean[] isSet = new boolean[1];
        int[] Tnum = new int[2];
        Tnum[0] = num; Tnum[1] = 1;
        boolean[] solved = new boolean[1];
        solved[0] = false;
        for(int i = 0; i < num; i ++){
            if(i != num - 1)
                gT[i] = new getTweetsTask(i * block_size, (i + 1) * block_size, info,users, Tnum, solved);
            else
                gT[i] = new getTweetsTask(i * block_size, size, info, users, Tnum, solved);
        }

        Thread[] h = new Thread[num];
        for(int i = 0; i < num; i ++){
            h[i] = new Thread(gT[i]);
            h[i].start();
        }
        try{
            for(Thread t : h)
                t.join();
        }catch(Exception e){
            e.printStackTrace();
        }

        users.add(up);

        LDAmain.users = users;
        LDAmain.target = users.size() - 1;
        try{
            LDAmain.main();
        }catch(Exception e){
            e.printStackTrace();
        }
        String str = null;
        int cur = 0;
        for(Integer tmp : LDAmain.topicCount.keySet()){
            str = "topic:" + LDAmain.topic_category.get(tmp) + "-";
            List<String> list = LDAmain.wordsInTopics.get(tmp);
            for(int j = 0; j < list.size(); j ++)
                str += list.get(j) + ",";
            System.out.println(str);
            String topic_tmp = LDAmain.topic_category.get(tmp);
            int count = topics.containsKey(topic_tmp)? topics.get(topic_tmp) + 1:1;
            topics.put(topic_tmp, count);
            }
        /*for(String backword:LDAmain.background_words)
            System.out.print(backword + ",");
        */
        for(int i = 0; i < 20; i ++){
            String word = LDAmain.myque.poll();
            System.out.println(word + ":" + LDAmain.rank.get(word));
            ans.add(word);
        }

    }
    public void driver2(TokenizerModel Tmodel, POSModel Pmodel){
        UserProfile up = null;
        List<String> info = new ArrayList();
        Helper.robinAccessToken(info);
        while(true) {
            try {
                up = new UserProfile();
                up.set(info);
                up.setUserProfile(name, Tmodel, Pmodel);
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (e.getMessage().contains("rate limit")) {
                    info.clear();
                    Helper.robinAccessToken(info);
                    System.out.println(e.getMessage() + ":" + Helper.cur_access);
                }else break;
            }
        }
        while(true) {
            try {
                up.getTweets(400);
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (e.getMessage().contains("rate limit")) {
                    info.clear();
                    Helper.robinAccessToken(info);
                    System.out.println(e.getMessage() + ":" + Helper.cur_access);
                    up.set(info);
                }else break;
            }
        }
        List<String> friends = null;
        while(true) {
            try {
                friends = up.getFriends();
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (e.getMessage().contains("rate limit")) {
                    info.clear();
                    Helper.robinAccessToken(info);
                    System.out.println(e.getMessage() + ":" + Helper.cur_access);
                    up.set(info);
                }else break;
            }
        }

        ArrayList<UserProfile> users = new ArrayList<>();
        for(int i = 0; i < friends.size(); i ++){
            UserProfile user_p = null;
            while(true) {
                try {
                    user_p = new UserProfile();
                    user_p.set(info);
                    user_p.setUserProfile(Long.parseLong(friends.get(i)) ,Tmodel,Pmodel);
                    break;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (e.getMessage().contains("rate limit")) {
                        info.clear();
                        Helper.robinAccessToken(info);
                        System.out.println(e.getMessage() + ":" + Helper.cur_access);
                    }else break;
                }
            }
            List<String> list = null;
            while(true) {
                try {
                    list = user_p.getTweets(400);
                    break;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (e.getMessage().contains("rate limit")) {
                        info.clear();
                        Helper.robinAccessToken(info);
                        System.out.println(e.getMessage() + ":" + Helper.cur_access);
                        user_p.set(info);
                    }else break;
                }
            }
            if(list != null && list.size() != 0)
                users.add(user_p);
        }
        users.add(up);

        LDAmain.users = users;
        LDAmain.target = users.size() - 1;
        try{
            LDAmain.main();
        }catch(Exception e){
            e.printStackTrace();
        }
        String str = null;
        int cur = 0;
        for(Integer tmp : LDAmain.topicCount.keySet()){
            str = "topic:" + (cur ++) + " " + LDAmain.topicCount.get(tmp) + "-";
            List<String> list = LDAmain.wordsInTopics.get(tmp);
            for(int j = 0; j < list.size(); j ++)
                str += list.get(j) + ",";
            System.out.println(str);
        }
        for(String backword:LDAmain.background_words)
            System.out.print(backword + ",");

        for(int i = 0; i < 20; i ++){
            String word = LDAmain.myque.poll();
            System.out.println(word + ":" + LDAmain.rank.get(word));
        }

    }


    public void driver3(TokenizerModel Tmodel, POSModel Pmodel){
        UserProfile up = null;
        List<String> info = new ArrayList();
        Helper.robinAccessToken(info);
        while(true) {
            try {
                up = new UserProfile();
                up.set(info);
                up.setUserProfile(name, Tmodel, Pmodel);
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (e.getMessage().contains("rate limit")) {
                    info.clear();
                    Helper.robinAccessToken(info);
                    System.out.println(e.getMessage() + ":" + Helper.cur_access);
                }else break;
            }
        }

        while(true) {
            try {
                up.getTweets(2000);
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (e.getMessage().contains("rate limit")) {
                    info.clear();
                    Helper.robinAccessToken(info);
                    System.out.println("peng");
                    up.set(info);
                }else break;
            }
        }

        List<String> names = up.getBlockFriends();
        ArrayList<UserProfile> users = new ArrayList<>();
        for(int i = 0; i < names.size(); i ++) {
            UserProfile user_p = null;
            boolean flag = true;
            while (true) {
                try {
                    user_p = new UserProfile();
                    user_p.set(info);
                    user_p.setUserProfile(names.get(i), Tmodel, Pmodel);
                    break;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (e.getMessage().contains("rate limit")) {
                        info.clear();
                        Helper.robinAccessToken(info);
                        System.out.println("peng");
                    } else {
                        flag = false;
                        break;
                    }
                }
            }
            if(flag)
                users.add(user_p);
        }

        System.out.println("size:" + users.size());
        int size = users.size(), block_size = size / Math.min(size, Helper.thread_num),
                remain = size - size/block_size * block_size, num = (remain == 0)?size/block_size:size/block_size + 1;
        getTweetsTask[] gT = new getTweetsTask[num];

        int[] Tnum = new int[2];
        Tnum[0] = num; Tnum[1] = 1;
        boolean[] solved = new boolean[1];
        solved[0] = false;
        for(int i = 0; i < num; i ++){
            if(i != num - 1)
                gT[i] = new getTweetsTask(i * block_size, (i + 1) * block_size, info,users, Tnum, solved);
            else
                gT[i] = new getTweetsTask(i * block_size, size, info, users, Tnum, solved);
        }

        Thread[] h = new Thread[num];
        for(int i = 0; i < num; i ++){
            h[i] = new Thread(gT[i]);
            h[i].start();
        }
        try{
            for(Thread t : h)
                t.join();
        }catch(Exception e){
            e.printStackTrace();
        }

        users.add(up);
        up.parseText();
        GraphMain gm = new GraphMain(users, up);
        gm.driver();
        ans = gm.getResult();
    }
	@Path("{c}")
	@GET
	@Produces("application/xml")
    public String run(@PathParam("c") String name) throws UnknownHostException,MongoException, IOException, TwitterException {
		ans = new ArrayList();
		topics = new HashMap();
		this.name = name;
        InputStream modelIn = new FileInputStream("/Users/yupengzhang/Documents/tool/eclipse_workplace/hobby/src/en-token.bin");
        TokenizerModel Tmodel = new TokenizerModel(modelIn);

        InputStream is = new FileInputStream("/Users/yupengzhang/Documents/tool/eclipse_workplace/hobby/src/en-pos-maxent.bin");
        POSModel Pmodel = new POSModel(is);

        long t1=System.currentTimeMillis();
        driver1(Tmodel, Pmodel);
        long t2=System.currentTimeMillis();
        System.out.println((t2-t1)/ 1000);
        //up.parseText();
        //up.getKtopKeywordsBySelectivity();
        is.close();
        modelIn.close();
        System.out.println("size:" + ans.size());
        String result = "<test>" + "<words>";
        for(int i = 0; i < 15; i ++){
        	result += "<word>" + ans.get(i) + "," +  "</word>";
        }
        
        String[] top2 = new String[2];
        int max = -1;
        for(String topic: topics.keySet()){
        	if(topics.get(topic) > max){
        		max = topics.get(topic);
        		if(topic.contains(" & ")) topic = topic.replace(" & ", "");
        		top2[0] = topic;
        	}
        }
        topics.remove(top2[0]);
        max = -1;
        for(String topic: topics.keySet()){
        	if(topics.get(topic) > max){
        		max = topics.get(topic);
        		if(topic.contains(" & ")) topic = topic.replace(" & ", "");
        		top2[1] = topic;
        	}
        }
        
        result += "<word>" + top2[0] + "," + "</word>";
        result += "<word>" + top2[1] + "," + "</word>";
        result += "</words> + </test>";
        System.out.println(result);
		return result;

    }
}
