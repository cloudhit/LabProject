/**
 * Created by yupengzhang on 11/26/15.
 */
import java.io.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterFactory;

public class Helper {
    public static int topk = 100;
    public static Set<String> kinds;
    public static int thread_num = 5;

    public static void setKinds(){
        kinds = new HashSet<String>();
        kinds.add("NN");kinds.add("NNS");
        kinds.add("NNP");kinds.add("NNPS");
        kinds.add("WP-wh");kinds.add("WP$");kinds.add("wh");
    }
    public static boolean isNoisy(String token) {

        if (token.contains("@") || token.contains("htt") || token.contains("…") || token.contains("?") || (!token.contains("#") && token.matches("[\\p{Punct}]+")))
            return true;
        for(int i = 0 ; i < token.length(); i ++){
            char c = token.charAt(i);
            if(!(c >= '1' && c <= '9' || c >= 'a' && c <= 'z' || c == '#'))
                return true;
        }
        return false;
    }
    public static HashMap sortByValue(
            HashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        HashMap result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void getTop(float[] array, ArrayList<Integer> rankList, int i) {
        int index = 0;
        HashSet<Integer> scanned = new HashSet<Integer>();
        double max = Double.MIN_VALUE;
        for (int m = 0; m < i && m < array.length; m++) {
            max = Double.MIN_VALUE;
            for (int no = 0; no < array.length; no++) {
                if (!scanned.contains(no)) {
                    if (array[no] > max) {
                        index = no;
                        max = array[no];
                    } else if (array[no] == max && Math.random() > 0.5) {
                        index = no;
                        max = array[no];
                    }
                }
            }
            if (!scanned.contains(index)) {
                scanned.add(index);
                rankList.add(index);
            }
        }
    }

    public static String removeEmojiAndSymbolFromString(
            String content) throws UnsupportedEncodingException{
        String utf8tweet = "";
        try {
            byte[] utf8Bytes = content.getBytes(
                    "UTF-8");

            utf8tweet = new String(
                    utf8Bytes, "UTF-8");
        } catch (
                UnsupportedEncodingException e
                ) {
            e.printStackTrace();
        }
        Pattern unicodeOutliers =
                Pattern.compile(
                        "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                        Pattern.UNICODE_CASE |
                                Pattern.CANON_EQ |
                                Pattern.CASE_INSENSITIVE
                );
        Matcher unicodeOutlierMatcher =
                unicodeOutliers.matcher(
                        utf8tweet);

        utf8tweet =
                unicodeOutlierMatcher.replaceAll(
                        " ");
        return utf8tweet;
    }
    public static String preprocess(String str)throws UnsupportedEncodingException{
        int index;
        str = removeEmojiAndSymbolFromString(str);
        index = str.indexOf("RT");
        if(index != -1){
            index = str.indexOf(":");
            if(index != -1)
                str = str.substring(index);
        }

        str = str.replaceAll("\\(", " ");
        str = str.replaceAll("\\[", " ");
        str = str.replaceAll("\\],", " ");
        str = str.replaceAll("\\{,", " ");
        str = str.replaceAll("\\}", " ");
        str = str.replaceAll("\\)", " ");
        str = str.replaceAll("'", " ");
        str = str.replaceAll("\"", " ");
        str = str.replaceAll(">", " ");
        str = str.replaceAll("-", " ");
        str = str.replaceAll("%", " ");
        if(str.contains("."))
            str = str.replaceAll(".", " ");
        if(str.contains(","))
            str = str.replaceAll(",", " ");
        if(str.indexOf(("!"), 0) >= 0)
            str = str.replaceAll("\\!", " ");
        if(str.indexOf(("?"), 0) >= 0)
            str = str.replaceAll("\\?", " ");
        if(str.contains(":"))
            str = str.replaceAll(":", " ");
        if(str.contains(";"))
            str = str.replaceAll(";", " ");
        return str;
    }

    public static void findScreenName(String s, List<String> names){
        Pattern p = Pattern.compile("RT @");
        Matcher m = p.matcher(s);
        boolean res = m.find();
        int size = s.length();
        if(res){
            int end_index = s.indexOf(":", m.end());
            names.add(s.substring(m.end(), end_index == -1? size:end_index));
        }else{
            Pattern p1 = Pattern.compile("@");
            m = p1.matcher(s);
            res = m.find();
            p = Pattern.compile("[^1-9A-Za-z_]");
            Matcher n = p.matcher(s);
            while(res){
                boolean res1 = n.find(m.start() + 1);
                int end_index = res1? n.start():size;
                names.add(s.substring(m.start() + 1, end_index));
                res = m.find();
            }
        }
    }
    public static int cur_access = 2;
    public static void robinAccessToken(List<String> info){

        List<String> consumerSecrets = new ArrayList<>();
        consumerSecrets.add("FmhkLDiICtCz66X0TmWRXUoYdfYwB0MTqIhJjGFs8LDz7h6Fp7");
        consumerSecrets.add("ecVWxtHv2wg7MLRiDuqsqQCon44PH2NUU0NOHned69rcncDraW");
        consumerSecrets.add("uK6Nb5AFThdkSZzqplx0xj9kq40PdzBYlURgWVAMo3WZDa2mbi");
        consumerSecrets.add("ThMhgPAcvWu7mCNL7i6Y5MNtY4sPj1uiI0Nr0XsvmFxirYKMge");

        List<String> consumerKey = new ArrayList<>();
        consumerKey.add("Yhq7NrhudYWlN6KnIwocwN9zM");
        consumerKey.add("lbdCwMgcJs0b7aGUe3iSviRd6");
        consumerKey.add("PHMVaifKWa5ZbcEU0taLKbGjI");
        consumerKey.add("vQ2ffmMo3fR7KTG9ur6YRH2ZE");

        List<String> accessToken = new ArrayList<>();
        accessToken.add("4307765120-WGwoU0Hf42HmQyk4ZDGJ2UMSXgz8RyEPThDZsZs");
        accessToken.add("4307765120-ZlVyTpeAVzKACZTMpZsjGg290vhyiCqOfl0gQzj");
        accessToken.add("4307765120-NMJNuzdo9CIJeScP1q13j05E4SHC2pLLxtJAW1F");
        accessToken.add("4307765120-jcK3yyLq9uEWlIOBCyvtopOMVhVZZZsHxE2YnSX");

        List<String> accessTokenSecret = new ArrayList<>();
        accessTokenSecret.add("lUwZssfAWSjDJtogQXNpwGIxhLZmuXfvzMiVMMqI70njf");
        accessTokenSecret.add("7APiDTL1U2Y8zNaSmqi5KwAiJy283tutSzLF6d4UK7fma");
        accessTokenSecret.add("krVf6l2hH1R665bCO9eaaHtyNI7BItjHXEl1LGADiGekY");
        accessTokenSecret.add("xrdWxcMEWKj2ccKaef5Nb3SR4IkpiTKF6rexsUwEswS02");

        cur_access = (cur_access + 1) % 4;

        info.add(consumerKey.get(cur_access));
        info.add(consumerSecrets.get(cur_access));
        info.add(accessToken.get(cur_access));
        info.add(accessTokenSecret.get(cur_access));
    }
}
