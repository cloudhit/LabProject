import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yupengzhang on 12/11/15.
 */
public class parseTaskP2 implements  Runnable{
    public HashMap<String, Integer> wordMap;
    int downside, upperside;
    public parseTaskP2(HashMap<String, Integer> wordMap, int a, int b){
        this.wordMap = wordMap;
        downside = a;
        upperside = b;
    }
    public void run(){
        for(int i = downside; i < upperside; i ++){
            System.out.print(i + ":");
            LDAmain.users.get(i).parseTweetsP2(wordMap);
        }
    }
}
