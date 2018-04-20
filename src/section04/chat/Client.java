package section04.chat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/** TCP协议：
 *  聊天室客户端：
 *
 */
public class Client {
    /**
     * 套接字
     * java.net.Socket
     *  封装了TCP协议，使用它就可以基于TCP协议
     *  进行网络通讯
     *  Socket是运行在客户端的
     */
    private Socket socket;
    /**
     * 构造方法，用来初始化客户端
     * 实例化Socket的一般使用需要传入两个参数的
     * 1.服务端地址：通过IP地址可以找到服务的那的计算机  本机：“localhost”or “127.0.0.1”
     * 2.服务端端口：通过端口可以找到服务端计算机上的服务端应用程序   一般 ：8088
     * 实例化Socket的过程就是连接的过程，若远端计算机没有响应会抛出异常
     *
     */
    public Client(){
        try {
            System.out.println("正在连接服务端：");
            //一般使用带有两个参数的构造方法   192.168.192.26 localhost 127.0.0.1
            socket = new Socket("127.0.0.1",9088);
            System.out.println("已与服务端建立连接。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动客户端的方法
     */
    public void start(){
        try {
            Scanner s = new Scanner(System.in);
            /*
             *　先要求用户输入一个昵称
             */
            String nickName = null;
            while(true){
                System.out.println("输入您的昵称：");
                nickName = s.nextLine();
                if(nickName.length()>0){
                    break;
                }
                System.out.println("输入有误！重新输入：");
            }
            System.out.println("欢迎你："+nickName+"！开始聊天吧！");

            /*
             *  Socket 提供的方法：
             *  OutputStream getOutputStream
             *  获取一个直接输出流，通过该流写出的数据会被发送至远端计算机
             */
            OutputStream out = socket.getOutputStream();
            //多加一层包装是为了pw可以使用自动刷新
            OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");

            PrintWriter pw = new PrintWriter(osw,true);

            //现将昵称发送给服务端
            pw.println(nickName);

            /*
             *启动线程读取服务端发送过来的消息
             */
            ServerHandler handler = new ServerHandler();
            Thread t = new Thread(handler);
            t.start();

            //将字符串发送至服务端
//            pw.println("你好服务端！==================");

            while(true){
                pw.println(s.nextLine());
            }

            /*
            while(b){
                String wr = s.nextLine();
                if(!wr.equals("exit")){
                    pw.println(wr);
                }else{
                    b = false;
                }
            }*/


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args){
        try {
            Client client = new Client();
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("客户端启动失败！");
        }

    }

    /**
     * 该线程用来读取服务端发送过来的消息，并输出到客户端控制台上
     */
    class ServerHandler implements Runnable{
        public void run(){
            try { 
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in,"UTF-8");
                BufferedReader br = new BufferedReader(isr);
                
                String message = null;
                while((message=br.readLine())!=null){
                    System.out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
