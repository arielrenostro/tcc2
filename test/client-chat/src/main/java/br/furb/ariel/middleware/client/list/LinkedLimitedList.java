package br.furb.ariel.middleware.client.list;

import java.util.Iterator;

public class LinkedLimitedList<T> {

    private final int limit;

    private Node<T> first;
    private Node<T> last;
    private int size;

    public LinkedLimitedList(int limit) {
        this.limit = limit;
    }

    public void add(T value) {
        Node<T> node;
        if (this.limit == this.size) {
            node = this.first;
            this.first = node.next;
            node.next = null;
        } else {
            node = new Node<>();
            this.size++;
        }

        node.value = value;

        if (this.first == null) {
            this.first = node;
        }
        if (this.last != null) {
            this.last.next = node;
        }
        this.last = node;
    }

    public T get(int idx) {
        Node<T> n = this.first;
        while (idx != 0) {
            n = n.next;
            idx--;
        }
        return n.value;
    }

    public int size() {
        return this.size;
    }

    public Iterator<T> iterator() {
        return new LinkedLimitedListIterator<>(this.first);
    }

    private static class LinkedLimitedListIterator<T> implements Iterator<T> {

        private Node<T> node;

        LinkedLimitedListIterator(Node<T> node) {
            this.node = node;
        }

        @Override
        public boolean hasNext() {
            return this.node != null;
        }

        @Override
        public T next() {
            T value = this.node.value;
            this.node = this.node.next;
            return value;
        }
    }

    private static class Node<T> {

        private Node<T> next;
        private T value;
    }
}
