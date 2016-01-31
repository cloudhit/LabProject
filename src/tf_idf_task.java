import java.util.*;


/**
 * Created by yupengzhang on 12/11/15.
 */
public class tf_idf_task implements Runnable{
    int downside, upperside;
    HashMap<String, Integer> idf;
    int D;
    Set<String> tf_idf_words;
    public tf_idf_task(HashMap<String, Integer> idf, int a, int b, int D, Set<String> tf_df_words){
        downside = a;
        upperside = b;
        this.idf = idf;
        this.D = D;
        this.tf_idf_words = tf_df_words;
    }
    public void run(){
        for(int i = downside; i < upperside; i ++){
            final HashMap<String, Float> tf = LDAmain.users.get(i).getTf();
            for(String word : tf.keySet()){
                float w_tdidf = tf.get(word) *  Log.log((1 + D) / idf.get(word), 10);
                tf.put(word, w_tdidf);
            }
            PriorityQueue<String> top = new PriorityQueue<>(20, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return tf.get(o1) > tf.get(o2)? 1:-1;
                }
            });

            for(String tmp : tf.keySet()){
                if(top.size() < 20)
                    top.add(tmp);
                else if(tf.get(top.peek()) < tf.get(tmp)){
                    top.poll();
                    top.offer(tmp);
                }
            }

            List<String> tmp = new ArrayList<>();
            while(!top.isEmpty()){
                tmp.add(top.poll());
            }
            synchronized (tf_idf_words){
                for(String tmp_word : tmp){
                    if(!tf_idf_words.contains(tmp_word)){
                        tf_idf_words.add(tmp_word);
                    }
                }
            }
        }
    }
}
