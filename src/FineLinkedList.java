public class FineLinkedList {
    Node head, tail;

    public FineLinkedList(){
        head = new Node(-1);
        tail = null;
    }

    public void add(int key){
        Node newNode = new Node(key);
        newNode.next = null;

        // We need to run this in a loop because head can change between lock calls
        while (true){
            if(head.lock.tryLock()){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        // Replace a null head's value for any threads waiting on this particular node instead of replacing it
        // which would also replace its lock
        if (head.key == -1) {
            head.key = key;
            head.unlock();
//            System.out.println("Init head " + key);
            return;
        } else if (head.key > key) {
            newNode.next = head;
            head = newNode;
            newNode.next.unlock();
//            System.out.println("Replaced head " + key);
            return;
        } else if (head.next == null) {
            newNode.next = null;
            head.next = newNode;
            head.unlock();
//            System.out.println("Added after head " + key);
            return;
        }
        // The normal case when not dealing with head
        // Loop through until we find a key bigger than key
        // or the end of the list
        // Using hand over hand locking
        Node current = head.next;
        current.lock.lock();
        Node prev = head;
        while (current.next != null) {
            if (current.key > key) {
                break;
            }
            prev.unlock();
            prev = current;
            current = current.next;
            current.lock.lock();
        }
        if (current.next == null) {
            current.next = newNode;
        } else {
            newNode.next = current;
            prev.next = newNode;
        }
        current.unlock();
        prev.unlock();
    }

    public boolean remove(int key){
            // Special behavior if the head needs to be removed
            if (head.key == key){
                head.lock();
                if (head.next != null) {
                    head.key = head.next.key;
                    head.next = head.next.next;
                } else {
                    head.key = -1;
                }
                head.unlock();
                return true;
            }
            if (head.next != null){
                // Grab this node and the next and hold this node and the next
                // This is needed for proper removal
                head.lock();
                head.next.lock();
                Node prev = head;
                Node current = head.next;

                while (current != null && current.key <= key){
                    // If we found the key
                    if (current.key == key){
                        prev.next = current.next;
                        prev.unlock();
                        current.unlock();
                        // Make sure the head gets unlocked. This is not necessary and is for debugging
                        if (head.lock.isHeldByCurrentThread()){
                            head.lock.unlock();
                        }
                        if (head.next != null && head.next.lock.isHeldByCurrentThread()){
                            head.next.lock.unlock();
                        }
                        return true;
                    }
                    // Free locks if not found
                    if (prev.lock.isHeldByCurrentThread()){
                        prev.unlock();
                    }
                    prev = current;
                    current = current.next;
                    current.lock();
                }
                // Make sure locks are freed
                if (head.lock.isHeldByCurrentThread()){
                    head.unlock();
                }
                if (head.next != null && head.next.lock.isHeldByCurrentThread()){
                    head.next.unlock();
                }
                return false;
            } else {
                if (head.lock.isHeldByCurrentThread()){
                    head.unlock();
                }
            }

            return false;
        }

        public boolean search(int key){
            // Only look at current node when searching
            // Quit the search if we find the end of the list or the key
            Node current = head;
            while (current != null){
                current.lock();
                if (current.key == key){
                    current.unlock();
                    return true;
                }
                Node oldCurrent = current;
                current = current.next;
                oldCurrent.unlock();
            }
            return false;
        }
}