import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;

import java.io.IOException;
import java.util.*;

/**
 * Created by yupengzhang on 12/12/15.
 */

public class GraphMain {
    private List<String> sentences;
    private Tokenizer tokenizer;
    private POSTaggerME tagger;
    private Map<String, HashMap<String, Integer>> graph;
    private List<UserProfile> refer_users;
    private UserProfile user;
    private Set<String> importWords;
    private List<String> answer;

    public GraphMain(List<UserProfile> refer_users, UserProfile user){
        this.refer_users = refer_users;
        this.user = user;
        this.sentences = user.getSentences();
        this.tokenizer = user.getTokenizer();
        this.tagger = user.getPOS();
        this.graph = user.getGraph();
        importWords = new HashSet<>();
        answer = new ArrayList();
    }

    public void IF_IDF(){

        HashMap<String, Integer> idf_count = new HashMap<>();
        int size = refer_users.size(), block_size = size / Math.min(size, Helper.thread_num),
                remain = size - size/block_size * block_size, num = (remain == 0)?size/block_size:size/block_size + 1;
        parseTask[] pT = new parseTask[num];
        for(int i = 0; i < num; i ++){
            if(i != num - 1)
                pT[i] = new parseTask(i * block_size, (i + 1)*block_size, refer_users);
            else
                pT[i] = new parseTask(i * block_size, size, refer_users);
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


        for(int i = 0; i < size; i ++){
            Set<String> set = refer_users.get(i).user_words;
            for(String tmp: set){
                int count = idf_count.containsKey(tmp)?idf_count.get(tmp) + 1:1;
                idf_count.put(tmp, count);
            }
        }

        final HashMap<String, Float> tf = user.getTf();
        for(String word : tf.keySet()){
            float w_tdidf = tf.get(word) *  Log.log((1 + refer_users.size()) / idf_count.get(word), 10);
            tf.put(word, w_tdidf);
        }

        int topWordsCnt = (int)(user.user_words.size() * 0.3);
        System.out.println("word count:" + user.user_words.size());

        PriorityQueue<String> top = new PriorityQueue<>(topWordsCnt, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return tf.get(o1) > tf.get(o2)? 1:-1;
            }
        });

        for(String tmp : tf.keySet()){
            if(top.size() < topWordsCnt)
                top.add(tmp);
            else if(tf.get(top.peek()) < tf.get(tmp)){
                top.poll();
                top.offer(tmp);
            }
        }

        while(!top.isEmpty()){
            importWords.add(top.poll());
        }

    }




    public void getKtopKeywordsBySelectivity()throws IOException {

        Map<String, String> tag_map = new HashMap();

        for(int i = 0; i < sentences.size(); i ++){
            String process_str = sentences.get(i);
            process_str = Helper.preprocess(process_str);
            String tokens[] = tokenizer.tokenize(process_str);
            String[] tags = tagger.tag(tokens);
            int window_size = 15;
            for(int j = 0; j < tokens.length;j ++) {
                tokens[j] = tokens[j].toLowerCase();
                tag_map.put(tokens[j], tags[j]);
                for (int k = j + 1; k < tokens.length; k++) {
                    if (k - j > window_size - 1)
                        break;
                    String word1 = tokens[j], word2 = tokens[k].toLowerCase();
                    if (!graph.containsKey(word1))
                        graph.put(word1, new HashMap<String, Integer>());
                    HashMap<String, Integer> map = graph.get(word1);
                    int weight = (map.containsKey(word2)) ? map.get(word2) + 1 : 1;
                    map.put(word2, weight);
                }
            }
        }
        //select the nodes with highest selectivity
        Map<String, MeasureNode> measures = new HashMap();
        for(String key: graph.keySet()){
            HashMap<String, Integer> map = (HashMap<String, Integer>)(graph.get(key));
            if(!measures.containsKey(key))
                measures.put(key, new MeasureNode(key));
            MeasureNode node_w1 = measures.get(key);
            for(String word:map.keySet()){
                if(!measures.containsKey(word))
                    measures.put(word, new MeasureNode(word));
                MeasureNode node_w2 = measures.get(word);
                node_w1.outdegree ++;
                node_w2.indegree ++;
                int weight = (Integer)map.get(word);
                node_w1.outweight += weight;
                node_w2.inweight += weight;
            }
        }
        Queue<MeasureNode> pq = new PriorityQueue<MeasureNode>(Helper.topk, new Comparator<MeasureNode>() {
            @Override
            public int compare(MeasureNode o1, MeasureNode o2) {
                double w1 = (o1.inweight + o1.outweight) * 1.0 /(o1.indegree + o1.outdegree);
                double w2 = (o2.inweight + o2.outweight) * 1.0 /(o2.indegree + o2.outdegree);
                if(w1 > w2) return 1;
                if(w1 < w2) return -1;
                return 0;
            }
        });

        for(String word: measures.keySet()){
            if(pq.size() < Helper.topk)
                pq.offer(measures.get(word));
            else{
                MeasureNode o1 = measures.get(word), o2 = pq.peek();
                double w1 = (o1.inweight + o1.outweight) * 1.0 /(o1.indegree + o1.outdegree);
                double w2 = (o2.inweight + o2.outweight) * 1.0 /(o2.indegree + o2.outdegree);
                if(w1 > w2){
                    pq.poll();
                    pq.offer(o1);
                }
            }
        }
        Helper.setKinds();
        while(!pq.isEmpty()){
            String word = pq.poll().word;
            if(Helper.kinds.contains(tag_map.get(word)) && !Stopwords.isStopword(word) && !Helper.isNoisy(word)
                    && importWords.contains(word))
                answer.add(word);
        }

    }
    public void driver(){
        IF_IDF();
        try {
            getKtopKeywordsBySelectivity();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public List<String> getResult(){
    	return answer;
    }
}
