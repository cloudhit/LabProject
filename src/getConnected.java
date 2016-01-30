import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import java.net.UnknownHostException;

/**
 * Created by yupengzhang on 11/25/15.
 */
public class getConnected {
    private Mongo mg;
    private DB db;
    private DBCollection coll;
    public getConnected(){
      try {
          mg = new Mongo("localhost", 27017);
          db = mg.getDB("twitter"); //相当于库名
          coll = db.getCollection("tweetstext");//相当于表名
      }catch(Exception e){
          e.printStackTrace();
      }

    }
    public DBCollection getColl(){
        return coll;
    }
}
