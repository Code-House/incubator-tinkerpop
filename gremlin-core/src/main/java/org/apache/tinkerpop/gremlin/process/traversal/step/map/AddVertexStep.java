/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal.step.map;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.Mutating;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.CallbackRegistry;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.Event;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.ListCallbackRegistry;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class AddVertexStep<S> extends MapStep<S, Vertex> implements Mutating<Event.VertexAddedEvent> {

    private final Object[] keyValues;
    private final transient Graph graph;

    private CallbackRegistry<Event.VertexAddedEvent> callbackRegistry;

    public AddVertexStep(final Traversal.Admin traversal, final Object... keyValues) {
        super(traversal);
        this.keyValues = keyValues;
        this.graph = this.getTraversal().getGraph().get();
    }

    public Object[] getKeyValues() {
        return keyValues;
    }

    @Override
    protected Vertex map(final Traverser.Admin<S> traverser) {
        final Vertex v = this.graph.addVertex(this.keyValues);
        if (callbackRegistry != null) {
            final Event.VertexAddedEvent vae = new Event.VertexAddedEvent(DetachedFactory.detach(v, true));
            callbackRegistry.getCallbacks().forEach(c -> c.accept(vae));
        }
        return v;
    }

    @Override
    public CallbackRegistry<Event.VertexAddedEvent> getMutatingCallbackRegistry() {
        if (null == callbackRegistry) callbackRegistry = new ListCallbackRegistry<>();
        return callbackRegistry;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode(), i = 0;
        for (final Object item : this.keyValues) {
            result ^= Integer.rotateLeft(item.hashCode(), i += 16);
        }
        return result;
    }
}
