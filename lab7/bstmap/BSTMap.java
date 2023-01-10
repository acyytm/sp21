package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap <Key extends Comparable<Key>, Value> implements Map61B<Key, Value>{

    /** Size of the map. */
    private int size;

    /** Binary search tree.(root) */
    private Node<Key, Value> tree;

    private static class Node<Key, Value> {
        public Key key;
        public Value value;
        public Node<Key, Value> left;
        public Node<Key, Value> right;

        public Node(Key key, Value value) {
            this.key = key;
            this.value = value;
        }
    }

    public BSTMap() {
        size = 0;
        tree = null;
    }

    /**
     * Remove all the key, value pair from this map
     */
    @Override
    public void clear() {
        size = 0;
        clear(tree);
        tree = null;
    }

    /**
     * help method to clean the tree.
     */
    public void clear(Node<Key, Value> node) {
        if(node == null) {
            return;
        }
        clear(node.left);
        clear(node.right);
        node.right = null;
        node.left = null;
    }

    /**
     * Return if it has the key
     * @param key key
     * @return true if this map has the key, false while not
     */
    @Override
    public boolean containsKey(Key key) {
        return get(tree, key) != null;
    }

    /**
     * get value with given key
     * @param key key you want to query
     * @return value map to the key, null if it doesn't exist
     */
    @Override
    public Value get(Key key) {
        Node<Key, Value> target = get(tree, key);
        if(target == null) {
            return null;
        }
        return get(tree, key).value;
    }

    /**
     * Help method, recursively get(key)
     */
    private Node<Key, Value> get(Node<Key, Value> node, Key key) {
        if(node == null) {
            return null;
        }

        if(node.key.equals(key)) {
            return node;
        }
        else if(node.key.compareTo(key) < 0) {
            return get(node.right, key);
        }
        else{
            return get(node.left, key);
        }
    }

    /**
     * Get size of this map
     * @return size of this map
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Put <key, value> to this map
     * @param key
     * @param value
     */
    @Override
    public void put(Key key, Value value) {
        size += 1;
        tree = put(tree, key, value);
    }

    /**
     * Help method, recursive put
     */
    public Node<Key, Value> put(Node<Key, Value> node, Key key, Value value) {
        if(node == null) {
            return new Node<>(key, value);
        }
        if(key.compareTo(node.key) < 0) {
            node.left = put(node.left, key, value);
        }else if(key.compareTo(node.key) == 0){
            node.value = value;
        }else{
            node.right = put(node.right, key, value);
        }
        return node;
    }


    /**
     * Get a set of all keys of this map
     * @return key set of this map
     */
    @Override
    public Set<Key> keySet() {
        Set<Key> set = new HashSet<>();
        keySet(tree, set);
        System.out.println(set);
        return set;
    }

    /**
     * Help method, recursive implement keySet method.
     */
    public void keySet(Node<Key, Value> node, Set<Key> set) {
        if (node == null) {
            return;
        }
        set.add(node.key);
        keySet(node.left, set);
        keySet(node.right, set);
    }

    /**
     * Remove this key, value pair from this map.
     * @param key key you want ot remove
     * @return value map to the given key
     */
    @Override
    public Value remove(Key key) {
        Value res = get(key);
        tree = remove(tree, key);
        if(res != null)
            size -= 1;
        return res;
    }

    /**
     * Help method, return the tree with removed key, recursively
     * @param node current node
     * @param key key
     * @return
     */
    public Node<Key, Value> remove(Node<Key, Value> node, Key key) {
        if (node == null)
            return null;
        if (key.equals(node.key)) {
            if (node.left == null) {
                return node.right;
            }
            else if (node.right == null) {
                return node.left;
            }
            Node<Key, Value> minNode = min(node.right);
            node.value = minNode.value;
            node.key = minNode.key;
            node.right = deleteMin(node.right);
        }
        else if (key.compareTo(node.key) < 0) {
            node.left = remove(node.left, key);
        }
        else {
            node.right = remove(node.right, key);
        }
        return node;
    }

    /**
     * Remove this pair when they are all the same as given key, value
     * @param key
     * @param value
     * @return value of this key in this map
     */
    @Override
    public Value remove(Key key, Value value) {
        Value res = get(key);
        if (res != null && res.equals(value)) {
            tree = remove(tree, key);
            return res;
        }
        return null;
    }

    @Override
    public Iterator<Key> iterator() {
        return new BSTMapIterator();
    }

    private Node<Key, Value> min(Node<Key, Value> node) {
        if (node == null) {
            throw new RuntimeException();
        }
        if (node.left == null) {
            return node;
        }
        return min(node.left);
    }

    /**
     * Delete the min key of this map
     */
    private void deleteMin() {
        tree = deleteMin(tree);
    }

    /**
     * Delete the min key of this map, recursively
     */
    private Node<Key, Value> deleteMin(Node<Key, Value> node) {
        if (node.left == null) {
            return node.right;
        }
        return deleteMin(node.left);
    }

    private class BSTMapIterator implements Iterator<Key> {
        private Iterator<Key> iterator = keySet().iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Key next() {
            return iterator.next();
        }

    }

}
