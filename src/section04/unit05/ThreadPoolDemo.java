package section04.unit05;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  线程池： ExecutorService
 *      Executors.newFixedThreadPool()  创建n个线程
 *      Executors.newCachedThreadPool()
 */
public class ThreadPoolDemo {
    public static void main(String[] args){
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        for(int i=0;i<5;i++){
            Runnable runn = new Runnable(){
                public void run(){
                    for(int i=0;i<5;i++){
                        System.out.println(i);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            threadPool.execute(runn);
        }
    }
}
