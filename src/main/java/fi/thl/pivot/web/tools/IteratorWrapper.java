package fi.thl.pivot.web.tools;

import java.util.Iterator;

public class IteratorWrapper<T> {

    private Iterator<T> delegate;

    public IteratorWrapper(Iterator<T> delegate) {
        this.delegate = delegate;
    }

    public T getNext() {
        return delegate.next();
    }
}