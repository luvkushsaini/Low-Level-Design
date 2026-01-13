package ConcurrancyQuestions.The

import java.util.concurrent.Semaphore;

Dining Philosophers;
class DiningPhilosophers {
    private Semaphore semaphore;
    private Semaphore[] forkSemaphore;

    public DiningPhilosophers() {
        semaphore = new Semaphore(4);
        forkSemaphore = new Semaphore[5];
        for (int i = 0; i < 5; i++) {
            forkSemaphore[i] = new Semaphore(1);
        }
    }

    public void wantsToEat(int philosopher,
                           Runnable pickLeftFork,
                           Runnable pickRightFork,
                           Runnable eat,
                           Runnable putLeftFork,
                           Runnable putRightFork) throws InterruptedException {

        semaphore.acquire();

        int left = philosopher;
        int right = (philosopher + 1) % 5;

        Semaphore leftForkSemaphore = forkSemaphore[left];
        Semaphore rightForkSemaphore = forkSemaphore[right];

        leftForkSemaphore.acquire();
        rightForkSemaphore.acquire();

        pickLeftFork.run();
        pickRightFork.run();
        eat.run();

        putLeftFork.run();
        leftForkSemaphore.release();
        putRightFork.run();
        rightForkSemaphore.release();

        semaphore.release();
    }
}

//Runnable helps enable multithreading by allowing tasks to be executed by 
// multiple threads in parallel, which can save time. When multiple threads run 
// in parallel and try to access the same shared resource at the same time,
//  problems can occur, so we use synchronization and locking to control access.

public class Main {
    
}
