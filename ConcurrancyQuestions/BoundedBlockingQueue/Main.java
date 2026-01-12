package ConcurrancyQuestions.BoundedBlockingQueue;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;

class BoundedBlockingQueue {

    private final Semaphore full;
    private final Semaphore empty;
    private final ConcurrentLinkedDeque<Integer> deque;//ConcurrentDeque allows multiple threads to safely add and remove elements at the same time without causing race conditions 


    BoundedBlockingQueue(int capacity) {
        full = new Semaphore(0);
        empty = new Semaphore(capacity); // means: queue can hold at most `capacity` elements,so only giving permit to capacity number of threads
        deque = new ConcurrentLinkedDeque<>();
    }

    // Producer
    public void enqueue(int element) throws InterruptedException {
        empty.acquire();           //will only allow thread to move foward if it have a permit (can remember as "it will take its permit and allow") 
        deque.addFirst(element);    
        full.release();       // will create a permit for the thread waiting to run dequeue(as it is stopped by the full.acquire()-->asking for the permit)      
    }

    // Consumer
    public int dequeue() throws InterruptedException {
        full.acquire();   //will only allow thread to move foward if it have a permit (can remember as "it will take its permit and allow")         
        int result = deque.pollLast();
        empty.release();  //this will create a permit for the thread trying to enter the enqueue          
        return result;
    }

    public int size() {
        return deque.size();
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {

        BoundedBlockingQueue queue = new BoundedBlockingQueue(2);

        Thread producer = new Thread(() -> {
            try {
                queue.enqueue(1);
                queue.enqueue(2);
                queue.enqueue(3);   // blocks until dequeue
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(1000);
                queue.dequeue();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        System.out.println("Final size: " + queue.size());
    }
}


