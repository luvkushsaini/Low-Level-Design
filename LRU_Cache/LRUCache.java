package LRU_Cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LRUCache<K, V> {

    private final int capacity;
    private final ConcurrentHashMap<K, Node<K, V>> map;
    private final DoublyLinkedList<K, V> dll;
    private final ReentrantLock lock = new ReentrantLock();

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new ConcurrentHashMap<>();
        this.dll = new DoublyLinkedList<>();
    }

    // PUT / INSERT
    public void insertInCache(K key, V value) {
        if (capacity <= 0) return;

        lock.lock();
        try {
            // already exists → just move to front + update value
            Node<K, V> existing = map.get(key);
            if (existing != null) {
                existing.value = value;
                dll.moveToFront(existing);
                return;
            }

            // eviction
            if (map.size() == capacity) {
                Node<K, V> removedNode = dll.removeLast();
                if (removedNode != null) {
                    map.remove(removedNode.key);
                }
            }

            Node<K, V> node = new Node<>(key, value);
            dll.addFirst(node);
            map.put(key, node);

        } finally {
            lock.unlock();
        }
    }

    // GET
    public V getValue(K key) {
        Node<K, V> node = map.get(key); // safe read
        if (node == null) return null;

        lock.lock();
        try {
            dll.moveToFront(node);
        } finally {
            lock.unlock();
        }

        return node.value;
    }

    // REMOVE
    public V remove(K key) {
        lock.lock();
        try {
            Node<K, V> node = map.get(key);
            if (node == null) return null;

            dll.remove(node);
            map.remove(key);
            return node.value;

        } finally {
            lock.unlock();
        }
    }
}
