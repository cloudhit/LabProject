/**
 * Created by yupengzhang on 12/2/15.
 */
import com.mongodb.util.Hash;
//import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;

import java.io.*;
import java.util.*;
import java.lang.Object;


public class Model {

    public int A; // all topics
    public int U; // user number
    public int V; // vocabulary size
    public int nIter; // iteration number

    public float[] alpha_general;
    public float alpha_general_sum = 0;
    public float[] beta_word;
    public float beta_word_sum = 0;
    public float[] beta_background;
    public float beta_background_sum = 0;
    public float[] gamma;

    public float[][] theta_general;
    public float[][] phi_word;
    public float[] phi_background;
    public float[] rho;

    public short[][] z; // all hidden variables
    public boolean[][][] x;

    public int[][] C_ua;
    public long[] C_lv;
    public int[][] C_word;
    public int[] C_b;

    public int[] countAllWord; // # of words which are general topic a


    public Model(int A_all, int t, int u, int v, int niter, float alpha_g,
                 float beta, float beta_b, float gamm) {

        this.A = A_all;
        this.U = u;
        this.V = v;
        this.nIter = niter;
        // //////////////////////////////////////////////////////////////////////////////////////////
        this.alpha_general = new float[A];
        for (int i = 0; i < A; i++) {
            alpha_general[i] = alpha_g;
            alpha_general_sum += alpha_general[i];
        }

        this.gamma = new float[2];
        for (int i = 0; i < 2; i++) {
            gamma[i] = gamm;
        }

        this.beta_background = new float[V];
        this.beta_word = new float[V];
        for (int i = 0; i < V; i++) {
            beta_background[i] = beta_b;
            beta_background_sum += beta_background[i];
            beta_word[i] = beta;
            beta_word_sum += beta_word[i];
        }
        // ////////////////////////////////////////////////////////////////////////////////
        C_ua = new int[U][A];
        theta_general = new float[U][A];
        for (int i = 0; i < U; i++) {
            for (int j = 0; j < A; j++) {
                C_ua[i][j] = 0;
                theta_general[i][j] = 0;
            }
        }

        C_lv = new long[2];
        C_lv[0] = 0;
        C_lv[1] = 0;

        rho = new float[2];
        rho[0] = 0;
        rho[1] = 0;

        C_word = new int[A][V];
        phi_word = new float[A][V];
        for (int i = 0; i < A; i++) {
            for (int j = 0; j < V; j++) {
                C_word[i][j] = 0;
                phi_word[i][j] = 0;
            }
        }

        C_b = new int[V];
        phi_background = new float[V];
        for (int i = 0; i < V; i++) {
            C_b[i] = 0;
            phi_background[i] = 0;
        }

        countAllWord = new int[A];
        for (int i = 0; i < A; i++) {
            countAllWord[i] = 0;
        }

    }

    public Model(int A_all, int u, int v, int niter, float alpha_g, float beta,
                 float beta_b, float gamm) {
        System.out.println("reading data...");

        this.A = A_all;
        this.U = u;
        this.V = v;
        this.nIter = niter;
        // //////////////////////////////////////////////////////////////////////////////////////////
        this.alpha_general = new float[A];
        for (int i = 0; i < A; i++) {
            alpha_general[i] = alpha_g;
            alpha_general_sum += alpha_general[i];
        }

        this.gamma = new float[2];
        for (int i = 0; i < 2; i++) {
            gamma[i] = gamm;
        }

        this.beta_background = new float[V];
        this.beta_word = new float[V];
        for (int i = 0; i < V; i++) {
            beta_background[i] = beta_b;
            beta_background_sum += beta_background[i];
            beta_word[i] = beta;
            beta_word_sum += beta_word[i];
        }
        // ////////////////////////////////////////////////////////////////////////////////
        C_ua = new int[U][A];
        theta_general = new float[U][A];
        for (int i = 0; i < U; i++) {
            for (int j = 0; j < A; j++) {
                C_ua[i][j] = 0;
                theta_general[i][j] = 0;
            }
        }

        C_lv = new long[2];
        C_lv[0] = 0;
        C_lv[1] = 0;

        rho = new float[2];
        rho[0] = 0;
        rho[1] = 0;

        C_word = new int[A][V];
        phi_word = new float[A][V];
        for (int i = 0; i < A; i++) {
            for (int j = 0; j < V; j++) {
                C_word[i][j] = 0;
                phi_word[i][j] = 0;
            }
        }

        C_b = new int[V];
        phi_background = new float[V];
        for (int i = 0; i < V; i++) {
            C_b[i] = 0;
            phi_background[i] = 0;
        }

        countAllWord = new int[A];
        for (int i = 0; i < A; i++) {
            countAllWord[i] = 0;
        }
    }

    public void intialize(ArrayList<UserProfile> users) {
        System.out.println("initializing...");

        int u, d, w = 0;

        z = new short[users.size()][];
        x = new boolean[users.size()][][];

        for (u = 0; u < users.size(); u++) {
            UserProfile buffer_user = users.get(u);
            z[u] = new short[buffer_user.getTweets2().size()];
            x[u] = new boolean[buffer_user.getTweets2().size()][];

            for (d = 0; d < buffer_user.getTweets2().size(); d++) {
                tweet tw = buffer_user.getTweets2().get(d);
                x[u][d] = new boolean[tw.tweetwords.length];

                double randgeneral = Math.random();
                double thred = 0;
                short a_general = 0;
                for (short a = 0; a < A; a++) {
                    thred += (double) 1 / A;
                    if (thred >= randgeneral) {
                        a_general = a;
                        break;
                    }
                }

                z[u][d] = a_general;
                C_ua[u][a_general]++;
                for (w = 0; w < tw.tweetwords.length; w++) {
                    int word = tw.tweetwords[w];
                    double randback = Math.random();
                    boolean buffer_x;
                    if (randback > 0.5) {
                        buffer_x = true;
                    } else {
                        buffer_x = false;
                    }

                    if (buffer_x == true) {
                        C_lv[1]++;
                        C_word[a_general][word]++;
                        countAllWord[a_general]++;
                        x[u][d][w] = buffer_x;
                    } else {
                        C_lv[0]++;
                        C_b[word]++;
                        x[u][d][w] = buffer_x;
                    }
                }
            }
        }
        System.out.println("Intialize Done");
    }

    public void estimate(ArrayList<UserProfile> users, int nIter) {

        int niter = 0;
        while (true) {
            niter++;
            System.out.println("iteration" + " " + niter + " ...");
            sweep(users);
            if (niter >= nIter) {
                update_distribution();
                break;
            }
        }
    }


    private void sweep(ArrayList<UserProfile> users) {
        for (int cntuser = 0; cntuser < users.size(); cntuser++) {
            UserProfile buffer_user = users.get(cntuser);
            for (int cnttweet = 0; cnttweet < buffer_user.getTweets2().size(); cnttweet++) {
                tweet tw = buffer_user.getTweets2().get(cnttweet);
                sample_z(cntuser, cnttweet, buffer_user, tw);
                for (int cntword = 0; cntword < tw.tweetwords.length; cntword++) {
                    int word = tw.tweetwords[cntword];
                    sample_x(cntuser, cnttweet, cntword, word);
                }
            }
        }
    }

    private void sample_x(int u, int d, int n, int word) {

        boolean binarylabel = x[u][d][n];
        int binary;
        if (binarylabel == true) {
            binary = 1;
        } else {
            binary = 0;
        }

        C_lv[binary]--;
        if (binary == 0) {
            C_b[word]--;
        } else {
            C_word[z[u][d]][word]--;
            countAllWord[z[u][d]]--;
        }

        binarylabel = draw_x(u, d, n, word);

        x[u][d][n] = binarylabel;

        if (binarylabel == true) {
            binary = 1;
        } else {
            binary = 0;
        }

        C_lv[binary]++;
        if (binary == 0) {
            C_b[word]++;
        } else {
            C_word[z[u][d]][word]++;
            countAllWord[z[u][d]]++;
        }
    }

    private boolean draw_x(int u, int d, int n, int word) {

        boolean returnvalue = false;
        double[] P_lv;
        P_lv = new double[2];
        double Pb = 1;
        double Ptopic = 1;

        P_lv[0] = (C_lv[0] + gamma[0])
                / (C_lv[0] + C_lv[1] + gamma[0] + gamma[1]); 
        // counting C_lv

        P_lv[1] = (C_lv[1] + gamma[1])
                / (C_lv[0] + C_lv[1] + gamma[0] + gamma[1]);

        Pb = (C_b[word] + beta_background[word])
                / (C_lv[0] + beta_background_sum); 
        Ptopic = (C_word[z[u][d]][word] + beta_word[word])
                / (countAllWord[z[u][d]] + beta_word_sum);

        double p0 = Pb * P_lv[0];
        double p1 = Ptopic * P_lv[1];

        double sum = p0 + p1;
        double randPick = Math.random();

        if (randPick <= p0 / sum) {
            returnvalue = false;
        } else {
            returnvalue = true;
        }

        return returnvalue;
    }

    private void sample_z(int u, int d, UserProfile buffer_user, tweet tw) {

        short tweet_topic = z[u][d];
        int w = 0;

        C_ua[u][tweet_topic]--;
        for (w = 0; w < tw.tweetwords.length; w++) {
            int word = tw.tweetwords[w];
            if (x[u][d][w] == true) {
                C_word[tweet_topic][word]--;
                countAllWord[tweet_topic]--;
            }
        }

        short buffer_z;
        buffer_z = draw_z(u, d, buffer_user, tw);

        tweet_topic = buffer_z;
        z[u][d] = tweet_topic;

        C_ua[u][tweet_topic]++;
        for (w = 0; w < tw.tweetwords.length; w++) {
            int word = tw.tweetwords[w];
            if (x[u][d][w] == true) {
                C_word[tweet_topic][word]++;
                countAllWord[tweet_topic]++;
            }
        }
    }

    private short draw_z(int u, int d, UserProfile buffer_user, tweet tw) {
        int word;
        int w;

        double[] P_topic;
        int[] pCount;
        P_topic = new double[A];
        pCount = new int[A];

        HashMap<Integer, Integer> wordcnt = new HashMap<Integer, Integer>(); 
        int totalWords = 0; 

        for (w = 0; w < tw.tweetwords.length; w++) {
            if (x[u][d][w] == true) {
                totalWords++;
                word = tw.tweetwords[w];
                if (!wordcnt.containsKey(word)) {
                    wordcnt.put(word, 1);
                } else {
                    int buffer_word_cnt = wordcnt.get(word) + 1;
                    wordcnt.put(word, buffer_word_cnt);
                }
            }
        }

        for (int a = 0; a < A; a++) {
            P_topic[a] = (C_ua[u][a] + alpha_general[a])
                    / (buffer_user.getTweets2().size() - 1 + alpha_general_sum);

            double buffer_P = 1;

            int i = 0;
            Set s = wordcnt.entrySet();
            Iterator it = s.iterator();
            while (it.hasNext()) {
                Map.Entry m = (Map.Entry) it.next();
                word = (Integer) m.getKey();
                int buffer_cnt = (Integer) m.getValue();
                for (int j = 0; j < buffer_cnt; j++) {
                    double value = (double) (C_word[a][word] + beta_word[word] + j)
                            / (countAllWord[a] + beta_word_sum + i);
                    i++;
                    buffer_P *= value;
                    buffer_P = isOverFlow(buffer_P, pCount, a);
                }
            }

            P_topic[a] *= Math.pow(buffer_P, (double) 1);
        }

        reComputeProbs(P_topic, pCount);

        double randz = Math.random();

        double sum = 0;

        for (int a = 0; a < A; a++) {
            sum += P_topic[a];
        }

        double thred = 0;

        short chosena = -1;

        for (short a = 0; a < A; a++) {
            thred += P_topic[a] / sum;
            if (thred >= randz) {
                chosena = a;
                break;
            }
        }
        if (chosena == -1) {
            System.out.println("chosena equals -1, error!");
        }

        wordcnt.clear();
        return chosena;
    }

    private void reComputeProbs(double[] p_topic, int[] pCount) {
        int max = pCount[0];
        for (int i = 1; i < pCount.length; i++) {
            if (pCount[i] > max)
                max = pCount[i];
        }
        for (int i = 0; i < pCount.length; i++) {
            p_topic[i] = p_topic[i] * Math.pow(1e150, pCount[i] - max);
        }
        if (max > 0) {
            System.out.print(pCount[0] + " ");
            for (int i = 1; i < pCount.length; i++) {
                System.out.print(pCount[i] + " ");
            }
            System.out.println();
        }
    }

    private double isOverFlow(double buffer_P, int[] pCount, int a2) {
        if (buffer_P > 1e150) {
            pCount[a2]++;
            return buffer_P / 1e150;
        }
        if (buffer_P < 1e-150) {
            pCount[a2]--;
            return buffer_P * 1e150;
        }
        return buffer_P;
    }

    public void update_distribution() {

        for (int u = 0; u < U; u++) {
            int c_u_a = 0;
            for (int a = 0; a < A; a++) {
                c_u_a += C_ua[u][a];
            }
            for (int a = 0; a < A; a++) {
                theta_general[u][a] = (C_ua[u][a] + alpha_general[a])
                        / (c_u_a + alpha_general_sum);
            }
        }

        for (int a = 0; a < A; a++) {
            int c_v = 0;
            for (int v = 0; v < V; v++) {
                c_v += C_word[a][v];
            }
            for (int v = 0; v < V; v++) {
                phi_word[a][v] = (C_word[a][v] + beta_word[v])
                        / (c_v + beta_word_sum);
            }
        }

        int c_b_v = 0;
        for (int v = 0; v < V; v++) {
            c_b_v += C_b[v];
        }
        for (int v = 0; v < V; v++) {
            phi_background[v] = (C_b[v] + beta_background[v])
                    / (c_b_v + beta_background_sum);
        }

        for (int l = 0; l < 2; l++) {
            rho[0] = (C_lv[0] + gamma[0])
                    / (C_lv[0] + C_lv[1] + gamma[0] + gamma[1]);
            rho[1] = (C_lv[1] + gamma[1])
                    / (C_lv[0] + C_lv[1] + gamma[0] + gamma[1]);
        }
    }


    public HashMap<Integer, List<String> > outputWordsInTopics(ArrayList<String> list,
                                    int Cnt, HashMap<String, Float> rank, Set<String> usefulWords) throws Exception {

        ArrayList<Integer> rankList = new ArrayList<Integer>();
        HashMap<Integer, List<String> > res = new HashMap<>();
        for (int a = 0; a < A; a++) {
            Helper.getTop(phi_word[a], rankList, Cnt);
            List<String> words = new ArrayList<>();
            StringBuilder stb = new StringBuilder();
            for (int i = 0; i < rankList.size(); i++) {
                stb.append(list.get(rankList.get(i)));
                if(usefulWords.contains(stb.toString())) {
                    words.add(stb.toString());
                    if(!rank.containsKey(stb.toString()))
                        rank.put(stb.toString(), Float.valueOf(0));
                    rank.put(stb.toString(), rank.get(stb.toString()) + phi_word[a][rankList.get(i)]);
                }
                stb.setLength(0);
            }
            res.put(a, words);
            rankList.clear();
        }
        return res;
    }

    public float[] getTopicDistributionOnUser(int target){return theta_general[target];}
    public int[] getTopicCountOnUser(int target){return C_ua[target];}


    public HashMap<Integer, Integer> outputTopicDistributionOnUsers(
                                               ) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for(int a = 0; a < A; a ++){
            int sum = 0;
            for(int u = 0; u < U; u ++){
                sum += C_ua[u][a];
            }
            map.put(a, sum);
            System.out.println(sum);
        }
        return map;
    }

    public Set<String> outputBackgroundWordsDistribution(ArrayList<String> list, int Cnt){

        ArrayList<Integer> rankList = new ArrayList<Integer>();

        // phi_background
        Helper.getTop(phi_background, rankList, Cnt);

        Set<String> background_words = new HashSet<>();

        for (int i = 0; i < rankList.size(); i++) {
            background_words.add(list.get(i));
        }
        rankList.clear();
        return background_words;
    }


    public void outputTextWithLabel(String output, ArrayList<UserProfile> users,
                                    ArrayList<String> uniWordMap) throws Exception {
        BufferedWriter writer = null;

        for (int u = 0; u < users.size(); u++) {
            UserProfile buffer_user = users.get(u);

            writer = new BufferedWriter(new FileWriter(new File(output + "/"
                    + buffer_user.getScreen_name())));
            for (int d = 0; d < buffer_user.getTweets2().size(); d++) {
                tweet buffer_tweet = buffer_user.getTweets2().get(d);
                String line = "z=" + z[u][d] + ":  ";
                for (int n = 0; n < buffer_tweet.tweetwords.length; n++) {
                    int word = buffer_tweet.tweetwords[n];
                    if (x[u][d][n] == true) {
                        line += uniWordMap.get(word) + "/" + z[u][d] + " ";
                    } else {
                        line += uniWordMap.get(word) + "/" + "false" + " ";
                    }
                }
                int buffertime = buffer_tweet.time + 1;
                if (buffertime <= 30) {
                    if (buffertime < 10) {
                        line = "2011-09-0" + buffertime + ":\t" + line;
                    } else {
                        line = "2011-09-" + buffertime + ":\t" + line;
                    }
                } else if (buffertime <= 61 && buffertime > 30) {
                    int buffer_time = buffertime - 30;
                    if (buffertime - 30 < 10) {
                        line = "2011-10-0" + buffer_time + ":\t" + line;
                    } else {
                        line = "2011-10-" + buffer_time + ":\t" + line;
                    }
                } else if (buffertime > 61) {
                    int buffer_time = buffertime - 61;
                    if (buffertime - 61 < 10) {
                        line = "2011-11-0" + buffer_time + ":\t" + line;
                    } else {
                        line = "2011-11-" + buffer_time + ":\t" + line;
                    }
                }
                writer.write(line + "\n");
            }
            writer.flush();
            writer.close();
        }
    }

}
