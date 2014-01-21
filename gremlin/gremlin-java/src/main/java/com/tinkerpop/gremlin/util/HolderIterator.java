package com.tinkerpop.gremlin.util;

import com.tinkerpop.gremlin.Holder;
import com.tinkerpop.gremlin.PathHolder;
import com.tinkerpop.gremlin.Pipe;
import com.tinkerpop.gremlin.SimpleHolder;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class HolderIterator<T> implements Iterator<Holder<T>> {

    private final Iterator<T> iterator;
    private final Pipe pipe;
    private final boolean trackPaths;

    public HolderIterator(final Pipe pipe, final Iterator<T> iterator, final boolean trackPaths) {
        this.iterator = iterator;
        this.pipe = pipe;
        this.trackPaths = trackPaths;
    }

    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    public Holder<T> next() {
        return this.trackPaths ?
                new PathHolder<>(this.pipe.getAs(), this.iterator.next()) :
                new SimpleHolder<>(this.pipe.getAs(), this.iterator.next());
    }
}
