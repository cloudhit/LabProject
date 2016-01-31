/**
 * Created by yupengzhang on 12/2/15.
 */
import java.util.*;


public class LDAmain {

    public static ArrayList<UserProfile> users;
    public static int target;
    public static float[] topicDistribution;
    public static HashMap<Integer, Integer> topicCount;
    public static HashMap<Integer, List<String> > wordsInTopics;
    public static Set<String> background_words;
    public static HashMap<String, Float> rank;
    public static PriorityQueue<String> myque;
    public static PriorityQueue<String> freque;
    public static HashMap<Integer, String> topic_category;

    public static void show(){
        for(UserProfile user: users){
            List<tweet> list = user.getTweets2();
            System.out.println(list.size());
        }
    }


    public static void main() throws Exception {
        char[] options = { 'f', 'i', 'o', 'p', 's' };

        // 1. get model parameters
        ArrayList<String> modelSettings = new ArrayList<String>();
        getModelPara(modelSettings);
        int A_all = Integer.parseInt(modelSettings.get(0));
        float alpha_g = Float.parseFloat(modelSettings.get(1));
        float beta_word = Float.parseFloat(modelSettings.get(2));
        float beta_b = Float.parseFloat(modelSettings.get(3));
        float gamma = Float.parseFloat(modelSettings.get(4));
        int nIter = Integer.parseInt(modelSettings.get(5));
        System.err.println("Topics:" + A_all + ", alpha_g:" + alpha_g
                + ", beta_word:" + beta_word + ", beta_b:" + beta_b
                + ", gamma:" + gamma + ", iteration:" + nIter);
        modelSettings.clear();

        int outputTopicwordCnt = 20;

        // 2. get documents (users)
        HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
        ArrayList<String> uniWordMap = new ArrayList<String>();
        HashMap<String, Integer> idf_count = new HashMap<>();

        int size = users.size(), block_size = size / Math.min(size, Helper.thread_num),
                remain = size - size/block_size * block_size, num = (remain == 0)?size/block_size:size/block_size + 1;
        parseTask[] pT = new parseTask[num];
        for(int i = 0; i < num; i ++){
            if(i != num - 1)
                pT[i] = new parseTask(i * block_size, (i + 1)*block_size, users);
            else
                pT[i] = new parseTask(i * block_size, size, users);
        }
        Thread[] h = new Thread[num];
        for(int i = 0; i < num; i ++){
            h[i] = new Thread(pT[i]);
            h[i].start();
        }
        try{
            for(Thread t : h)
                t.join();
        }catch(Exception e){
            e.printStackTrace();
        }


        int id = 0;
        for(int i = 0; i < size; i ++){
            Set<String> set = users.get(i).user_words;
            for(String tmp: set){
                if(!wordMap.containsKey(tmp)){
                    wordMap.put(tmp, id ++);
                    uniWordMap.add(tmp);
                }
                int count = idf_count.containsKey(tmp)?idf_count.get(tmp) + 1:1;
                idf_count.put(tmp, count);
            }
        }


        parseTaskP2[] pT2 = new parseTaskP2[num];
        for(int i = 0; i < num; i ++){
            if(i != num - 1)
                pT2[i] = new parseTaskP2(wordMap, i * block_size, (i + 1)*block_size);
            else
                pT2[i] = new parseTaskP2(wordMap, i * block_size, size);
        }

        h = new Thread[num];
        for(int i = 0; i < num; i ++){
            h[i] = new Thread(pT2[i]);
            h[i].start();
        }
        try{
            for(Thread t : h)
                t.join();
        }catch(Exception e){
            e.printStackTrace();
        }



        Set<String> tf_df_words = new HashSet();


        tf_idf_task[] tfidf = new tf_idf_task[num];
        for(int i = 0; i < num; i ++){
            if(i != num - 1)
                tfidf[i] = new tf_idf_task(idf_count, i * block_size, (i + 1)*block_size, size, tf_df_words);
            else tfidf[i] = new tf_idf_task(idf_count, i * block_size, size, size, tf_df_words);
        }
        h = new Thread[num];
        for(int i = 0; i < num; i ++){
            h[i] = new Thread(tfidf[i]);
            h[i].start();
        }
        try{
            for(Thread t : h)
                t.join();
        }catch(Exception e){
            e.printStackTrace();
        }


        if (uniWordMap.size() != wordMap.size()) {
            System.out.println("w:" + wordMap.size());
            System.out.println("u:" + uniWordMap.size());
            System.err
                    .println("uniqword size is not the same as the hashmap size!");
            System.exit(0);
        }


        int uniWordMapSize = uniWordMap.size();

        // 3. run the model
        Model model = new Model(A_all, users.size(), uniWordMapSize, nIter,
                alpha_g, beta_word, beta_b, gamma);
        model.intialize(users);
        model.estimate(users, nIter);
        
        // 4. output model results
        //topicDistribution = model.getTopicDistributionOnUser(target);
        topicCount = model.outputTopicDistributionOnUsers();

        rank = new HashMap<>();

        try {
           wordsInTopics = model.outputWordsInTopics(uniWordMap, outputTopicwordCnt, rank, tf_df_words);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        //getTopicCategory
        topic_category = new HashMap<>();
        StringBuilder stb = new StringBuilder();
        int topic_size = wordsInTopics.size();
        h = new Thread[topic_size];
        TopicExtrationTask[] tE = new TopicExtrationTask[topic_size];
        List<String> words;
        int cur = 0;
        for(int x : wordsInTopics.keySet()){
            words = wordsInTopics.get(x);
            stb.setLength(0);
            for(String word: words)
                stb.append(word + " ");
            tE[cur ++] = new TopicExtrationTask("c3bc1c8b5397e3b78b5f6fba3d578218", stb.toString(),
                    "http://api.datumbox.com/1.0/TopicClassification.json", x, topic_category);
        }

        for(int i = 0; i < topic_size; i ++){
            h[i] = new Thread(tE[i]);
            h[i].start();
        }
        try{
            for(Thread t : h)
                t.join();
        }catch(Exception e){
            e.printStackTrace();
        }


        myque = new PriorityQueue<>(20, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (rank.get(o1) > rank.get(o2))? 1:-1;
            }
        });

        for(String tmp: rank.keySet()){
            if(myque.size() < 20)
                myque.add(tmp);
            else if(rank.get(myque.peek()) < rank.get(tmp)){
                myque.poll();
                myque.add(tmp);
            }
        }

    }

    private static void getModelPara(
                                     ArrayList<String> modelSettings) {
        modelSettings.clear();
        // T , alpha , beta , gamma , iteration , saveStep, saveTimes
        modelSettings.clear();
        // add default parameter settings
        modelSettings.add("10");
        modelSettings.add("1.25");
        modelSettings.add("0.01");
        modelSettings.add("0.01");
        modelSettings.add("20");
        modelSettings.add("100");

    }

    public enum ModelParas {
        topics, alpha_g, beta_word, beta_b, gamma, iteration;
    }
}
