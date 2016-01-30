/**
 * Created by yupengzhang on 11/25/15.
 */
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * 登录并授权获取access_token
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */

/*
*/
public class GetAccessToken {
    /**
     * Usage: java  twitter4j.examples.oauth.GetAccessToken [consumer key] [consumer secret]
     *
     * @param args message
     */
    public static void main(String[] args) {
        /**
         * 文件中只需要oauth.consumerKey与oauth.consumerSecret两个内容
         */
        File file = new File("/Users/yupengzhang/Documents/tool/spark/keywordextraction/twitter4j.properties");
        Properties prop = new Properties();
        InputStream is = null;
        OutputStream os = null;
        try {
            if (file.exists()) {
                is = new FileInputStream(file);
                prop.load(is);
            }
            if (args.length < 2) {
                if (null == prop.getProperty("oauth.consumerKey")
                        && null == prop.getProperty("oauth.consumerSecret")) {
                    // consumer key/secret are not set in twitter4j.properties
                    System.out.println(
                            "Usage: java twitter4j.examples.oauth.GetAccessToken [consumer key] [consumer secret]");
                    System.exit(-1);
                }
            } else {
                prop.setProperty("oauth.consumerKey", args[0]);
                prop.setProperty("oauth.consumerSecret", args[1]);
                os = new FileOutputStream("twitter4j.properties");
                prop.store(os, "twitter4j.properties");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignore) {
                }
            }
        }
        try {
            /*ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthAccessToken(null)
                    .setOAuthAccessTokenSecret(null);
            TwitterFactory tf = new TwitterFactory(cb.build());*/
            Twitter twitter = new TwitterFactory().getInstance();
            RequestToken requestToken = twitter.getOAuthRequestToken();
            System.out.println("Got request token.");
            System.out.println("Request token: " + requestToken.getToken());
            System.out.println("Request token secret: " + requestToken.getTokenSecret());
            AccessToken accessToken = null;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (null == accessToken) {
                System.out.println("Open the following URL and grant access to your account:");
                System.out.println(requestToken.getAuthorizationURL());
                try {
                    //控制浏览器去访问Twiiter的授权页面，授权成功后将会跳转回到申请的APP中的Redirect_URI地址，并携带了request_token
                    Desktop.getDesktop().browse(new URI(requestToken.getAuthorizationURL()));
                } catch (UnsupportedOperationException ignore) {
                } catch (IOException ignore) {
                } catch (URISyntaxException e) {
                    throw new AssertionError(e);
                }

                //输入浏览器跳转携带的code参数（应该将Redirect_URI设置成本系统中的一个Servlet的地址，并且下面的代码放到Servlet中来处理授权信息)
                System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
                String pin = br.readLine();
                try {
                    if (pin.length() > 0) {
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                    } else {
                        accessToken = twitter.getOAuthAccessToken(requestToken);
                    }
                } catch (TwitterException te) {
                    if (401 == te.getStatusCode()) {
                        System.out.println("Unable to get the access token.");
                    } else {
                        te.printStackTrace();
                    }
                }
            }
            System.out.println("Got access token.");
            System.out.println("Access token: " + accessToken.getToken());
            System.out.println("Access token secret: " + accessToken.getTokenSecret());

            try {
                prop.setProperty("oauth.accessToken", accessToken.getToken());
                prop.setProperty("oauth.accessTokenSecret", accessToken.getTokenSecret());
                os = new FileOutputStream(file);
                prop.store(os, "twitter4j.properties");
                os.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.exit(-1);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ignore) {
                    }
                }
            }
            System.out.println("Successfully stored access token to " + file.getAbsolutePath() + ".");
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get accessToken: " + te.getMessage());
            System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Failed to read the system input.");
            System.exit(-1);
        }
    }
}

