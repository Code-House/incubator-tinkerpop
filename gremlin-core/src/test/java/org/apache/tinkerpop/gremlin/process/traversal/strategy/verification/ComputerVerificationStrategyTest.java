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

package org.apache.tinkerpop.gremlin.process.traversal.strategy.verification;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalEngine;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@RunWith(Parameterized.class)
public class ComputerVerificationStrategyTest {

    private TraversalEngine traversalEngine;

    @Before
    public void setup() {
        this.traversalEngine = mock(TraversalEngine.class);
        when(this.traversalEngine.getType()).thenReturn(TraversalEngine.Type.COMPUTER);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"__.out().union(__.out().count(),__.in().count())", __.out().union(__.out().count(), __.in().count())},
                {"__.where(__.out().values(\"name\"))", __.where(__.out().values("name"))},
                {"__.groupCount(\"a\").out().cap(\"a\").count()", __.groupCount("a").out().cap("a").count()},

        });
    }

    @Parameterized.Parameter(value = 0)
    public String name;

    @Parameterized.Parameter(value = 1)
    public Traversal traversal;

    @Test
    public void shouldNotAllowLambdaSteps() {
        try {
            final TraversalStrategies strategies = new DefaultTraversalStrategies();
            strategies.addStrategies(ComputerVerificationStrategy.instance());
            traversal.asAdmin().setStrategies(strategies);
            traversal.asAdmin().setEngine(this.traversalEngine);
            traversal.asAdmin().applyStrategies();
            fail("The strategy should not allow lambdas: " + this.traversal);
        } catch (IllegalStateException ise) {
           assertTrue(true);
        }
    }

}
