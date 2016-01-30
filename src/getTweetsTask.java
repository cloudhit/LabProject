import java.util.ArrayList;
import java.util.List;

/**
 * Created by yupengzhang on 12/11/15.
 */
public class getTweetsTask implements Runnable{
    public int downside, upperside;
    public List<String> info;
    public ArrayList<UserProfile> users;
    public int[] tnum;
    public boolean[] solved;
    public getTweetsTask(int a, int b, List<String> info, ArrayList<UserProfile> users, int[] tnum, boolean[] solved){
        downside = a;
        upperside = b;
        this.info = info;
        this.users = users;
        this.tnum = tnum;
        this.solved = solved;
    }
    public void setRemain(int a){
        for(int i = a; i < upperside; i ++){
            users.get(i).set(info);
        }

    }
    public void run(){
        for(int i = downside; i < upperside; i ++){
            UserProfile user_p = users.get(i);
            while(true) {
                try {
                    user_p.getTweets(200);
                    break;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    boolean flag = e.getMessage().contains("rate limit");
                    if(!flag) break;
                    synchronized (tnum) {
                        solved[0] = false;
                        while (tnum[1] < tnum[0]) {
                            try {
                                System.out.println(tnum[0] + " " + tnum[1]);
                                tnum[1]++;
                                tnum.wait();

                            } catch (Exception e1) {
                                e1.printStackTrace();
                            } finally {
                                tnum[1] = 1;
                                break;
                            }
                        }
                        if (!solved[0]) {
                                solved[0] = true;
                                info.clear();
                                Helper.robinAccessToken(info);
                                System.out.println(e.getMessage() + ":" + Helper.cur_access);
                                setRemain(i);
                                tnum.notifyAll();
                        }else{
                            setRemain(i);
                            System.out.println("solved");
                        }
                }
                }
            }
        }
        synchronized (tnum){
            if(!solved[0]) tnum.notifyAll();
        }
    }
}
