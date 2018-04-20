package section04.chat;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


/**
 *   聊天室服务端：
 *
 */
public class Server {
    /*
     * 运行在服务端的ServerSocket主要负责：
     * 1：向系统申请服务端口
     *      客户端就是通过这个端口与之连接的
     * 2：监听申请的服务端口，当一个客户端通过该端口尝试建立连接时，ServerSocket
     *      会在服务端创建一个Socket与客户端建立连接
     */
    private ServerSocket server;

    /*
     * 保存所有客户端的昵称和输出流
     */
    private Map<String,PrintWriter> allOut;

    /**
     * 初始化的同时申请服务端口
     * @throws Exception
     */

    public Server() throws Exception{
        server = new ServerSocket(9088);

        allOut= new HashMap<String,PrintWriter>();
    }

    /**
     * 将给定的输出流存入共享集合
     * @param out
     */
    private synchronized void addOut(String nickName,PrintWriter out ){
        allOut.put(nickName,out);
    }

    /**
    * 将给定的输出流从共享集合删除
    * @param nickName
    */
    private synchronized void removeOut(String nickName){
        allOut.remove(nickName);
    }

    /**
     * 将给定的消息发送给所有客户端
     * @param message
     */
    private synchronized void sendMessage(String message){
        for(PrintWriter out:allOut.values()){
            out.println(message);
        }
    }

    /**
     * 私聊
     * @param nickName,message
     */
    private synchronized void sendMessage(String nickName,String message){
        PrintWriter out = allOut.get(nickName);
        out.println(message);
    }

    public void start(){
        int count=0;
        try {
            while(true) {
                System.out.println("等待客户端连接...");
                /*
                 * ServerSocket的accept方法是一个阻塞方法，作用是监听服务端口，直到一个客户端连接
                 * 并创建一个Socket，使用该Socket即可与刚连接的客户端进行交互
                 */
                Socket socket = server.accept();
                count++;
                System.out.println("第"+count+"客户端连接了！");

                /*
                 * 启动一个线程，来完成与客户端的交互
                 */
                ClientHandler handler = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        try {
            Server server = new Server();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("服务端启动失败！");
        }
    }

//    public static void eat(){}//测试内部类调用外部类静态方法

    /*
     *  该线程负责处理一个客户端的交互
     */
    class ClientHandler implements Runnable{
        /*
         * 该线程处理的客户端的Socket
         */
        private Socket socket;

        //客户端的地址信息；ip地址
        private String host;

        //该用户的昵称
        private String nickName;

        public ClientHandler(Socket socket){
            this.socket = socket;
            /*
             * 通过Socket可以获取远端计算机的地址信息
             */
            InetAddress address = socket.getInetAddress();
            //获取IP地址
            host = address.getHostAddress();

        }
        
        public void run(){
            PrintWriter pw=null;
            try {

                /*
                 *  Socket 提供的方法：
                 *  InputStream getInputStream
                 *  获取一个直接输入流，从该流读取的数据就是从远端计算机发送过来的
                 */
                InputStream is = socket.getInputStream();

                InputStreamReader isr = new InputStreamReader(is,"UTF-8");

                BufferedReader br = new BufferedReader(isr);

                //首先读取一行字符串为昵称
                nickName = br.readLine();

                /*
                 * 通过Socket创建输出流用于将消息发给客户端
                 */
                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");
                pw = new PrintWriter(osw,true);

                /*
                将该客户端的输出流存入到共享集合中
                 */
                addOut(nickName,pw);

                Server.this.sendMessage(nickName + "上线了！");

                String message = null;
                /*
                 * br.readLine在读取客户端发送过来的消息时，由于客户端断线，而其操作系统不同这里
                 * 读取后的结果不同：
                 * windows的客户端断开：br.readLine会抛出异常
                 * Linux的客户端：br.readLine 会返回null
                 */
                while((message = br.readLine())!=null){
                    if(message.startsWith("@") && message.matches("@.+:.*")){
                        int i = message.indexOf(':');
                        String toNickName = message.substring(1,i);
                        if(allOut.containsKey(toNickName)){
                            String myMessage = message.substring(i+1);
                            sendMessage(toNickName,"["+nickName +"]悄悄对你说："+myMessage);
                            sendMessage(nickName,"你悄悄对["+toNickName+"]说："+myMessage);
                            continue; 
                        }else{
                            sendMessage(nickName,"用户名不存在或已下线！");
                            continue;
                        }
                    }

                    //广播消息
                    sendMessage(nickName +"说："+message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                /**
                 * 处理当前客户端断开后的逻辑
                 */
                //将该客户端的输出流从共享集合中删除
                removeOut(nickName);

                sendMessage(nickName +"下线了！");
                try {
                    //socket关闭了，流也会随即关闭！
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
