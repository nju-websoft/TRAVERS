package oracle;

/**
 * FIFO queue based on linked list
 */

public class Queue<E> {
    class Node<E> {
        Node<E> next = null;
        E data;
        public Node(E data) {
            this.data = data;
        }
    }

    private Node<E> head = null;
    private Node<E> tail = null;

    public boolean isEmpty() {
        return head == null;
    }

    public void enqueue(E e) {
        Node<E> node = new Node<E>(e);
        if (isEmpty()) {
            head = node;
            tail = node;
            return;
        }
        tail.next = node;
        tail = node;
    }

    public E dequeue() { 
        if (isEmpty()) return null;
        E data = head.data;
        head = head.next;
        return data;
    }

    public int size() {
        Node<E> temp = head;
        int len = 0;
        while (temp != null) {
            len++;
            temp = temp.next;
        }
        return len;
    }

    public static void main(String[] args) {
        Queue<String> queue = new Queue<>();
        queue.enqueue("a");
        queue.enqueue("b");

        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
    }

}
