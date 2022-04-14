import java.util.concurrent.locks.ReentrantLock;

public class Node {
    int key;
    Node next;
    final ReentrantLock lock;

    public Node(int key) {
        this.key = key;
        lock = new ReentrantLock(true);
    }

    public void lock(){
        lock.lock();
    }

    public synchronized void lockNew(){
        while (true){
            if(lock.tryLock()){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void unlock(){
        lock.unlock();
    }
}
