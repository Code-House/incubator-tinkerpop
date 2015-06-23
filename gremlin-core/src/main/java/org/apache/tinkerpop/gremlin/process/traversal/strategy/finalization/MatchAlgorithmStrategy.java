/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.apache.tinkerpop.gremlin.process.traversal.strategy.finalization;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.MatchStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class MatchAlgorithmStrategy extends AbstractTraversalStrategy<TraversalStrategy.FinalizationStrategy> implements TraversalStrategy.FinalizationStrategy {

    private final Class<? extends MatchStep.MatchAlgorithm> matchAlgorithmClass;

    private MatchAlgorithmStrategy(final Class<? extends MatchStep.MatchAlgorithm> matchAlgorithmClass) {
        this.matchAlgorithmClass = matchAlgorithmClass;
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        if (!TraversalHelper.hasStepOfClass(MatchStep.class, traversal))
            return;
        TraversalHelper.getStepsOfClass(MatchStep.class, traversal).forEach(matchStep -> matchStep.setMatchAlgorithm(this.matchAlgorithmClass));
    }

    public static Builder build() {
        return new Builder();
    }

    @Override
    public String toString() {
        return StringFactory.traversalStrategyString(this);
    }

    public final static class Builder {

        private Class<? extends MatchStep.MatchAlgorithm> matchAlgorithmClass = MatchStep.CountMatchAlgorithm.class;

        private Builder() {
        }

        public Builder algorithm(final Class<? extends MatchStep.MatchAlgorithm> matchAlgorithmClass) {
            this.matchAlgorithmClass = matchAlgorithmClass;
            return this;
        }


        public MatchAlgorithmStrategy create() {
            return new MatchAlgorithmStrategy(this.matchAlgorithmClass);
        }
    }
}