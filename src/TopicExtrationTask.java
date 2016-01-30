
/**
 * Created by yupengzhang on 12/12/15.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TopicExtrationTask implements Runnable{
        private String api_key;
        private String requestText;
        private String url;
        private int tnum;
        private HashMap<Integer, String> topic_category;

        public TopicExtrationTask(String api_key, String requestText, String url, int tnum,
                                  HashMap<Integer, String> topic_category){
            this.api_key = api_key;
            this.requestText = requestText;
            this.url = url;
            this.tnum = tnum;
            this.topic_category = topic_category;
        }

        public void run(){
            String responseData = null;
            try {
                String urlParameters = "api_key=" + api_key + "&text=" + requestText;
                String request = url;
                URL url = new URL(request);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                apiService aS = new apiService();
                InputStream inptStrm = aS.postURL(connection, url, urlParameters, request);
                String jsonTxt = IOUtils.toString(inptStrm);
                responseData = parse(jsonTxt).replace("\"", "");

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                synchronized (topic_category){
                    topic_category.put(tnum, responseData);
                }
            }

        }

        public String parse(String jsonLine) {
            JsonElement jelement = new JsonParser().parse(jsonLine);
            JsonObject  jobject = jelement.getAsJsonObject();
            jobject = jobject.getAsJsonObject("output");
            String result = jobject.get("result").toString();
            return result;
    }
}
