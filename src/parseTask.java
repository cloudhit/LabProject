import java.util.*;

/**
 * Created by yupengzhang on 12/9/15.
 */
public class parseTask implements Runnable {
    int downside, upperside;
    List<UserProfile> users;
    public parseTask(int a, int b, List<UserProfile> users){
        downside = a;
        upperside = b;
        this.users = users;
    }
    public void run(){
        for(int i = downside; i < upperside; i ++){
            System.out.print(i + ":");
            users.get(i).parseTweetsP1();
        }
    }

}
