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
package org.apache.tinkerpop.gremlin.process.traversal.step.filter;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.HasContainerHolder;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class HasStep<S extends Element> extends FilterStep<S> implements HasContainerHolder, TraversalParent {

    private List<HasContainer> hasContainers;

    public HasStep(final Traversal.Admin traversal, final HasContainer... hasContainers) {
        super(traversal);
        this.hasContainers = new ArrayList<>();
        for (final HasContainer hasContainer : hasContainers) {
            this.addHasContainer(hasContainer);
        }
    }

    @Override
    protected boolean filter(final Traverser.Admin<S> traverser) {
        return HasContainer.testAll(traverser.get(), this.hasContainers);
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.hasContainers);
    }

    @Override
    public List<HasContainer> getHasContainers() {
        return Collections.unmodifiableList(this.hasContainers);
    }

    @Override
    public void addHasContainer(final HasContainer hasContainer) {
        this.hasContainers.add(hasContainer);
        hasContainer.getPredicate().getTraversals().forEach(this::integrateChild);
    }

    @Override
    public void addLocalChild(final Traversal.Admin<?, ?> localChildTraversal) {
        throw new UnsupportedOperationException("Use HasStep.addHasContainer(" + HasContainer.class.getSimpleName() + ") to add a local child traversal:" + this);
    }

    @Override
    public List<Traversal.Admin<?, ?>> getLocalChildren() {
        return this.hasContainers.stream()
                .flatMap(hasContainer -> hasContainer.getPredicate().getTraversals().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(TraverserRequirement.OBJECT);
    }

    @Override
    public HasStep<S> clone() {
        final HasStep<S> clone = (HasStep<S>) super.clone();
        clone.hasContainers = new ArrayList<>();
        for (final HasContainer hasContainer : this.hasContainers) {
            clone.addHasContainer(hasContainer.clone());
        }
        return clone;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        for (final HasContainer hasContainer : this.hasContainers) {
            result ^= hasContainer.hashCode();
        }
        return result;
    }
}
