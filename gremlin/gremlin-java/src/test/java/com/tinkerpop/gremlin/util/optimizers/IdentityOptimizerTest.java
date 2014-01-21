package com.tinkerpop.gremlin.util.optimizers;

import com.tinkerpop.gremlin.Gremlin;
import com.tinkerpop.gremlin.Pipe;
import com.tinkerpop.gremlin.Pipeline;
import com.tinkerpop.gremlin.SimpleHolder;
import com.tinkerpop.gremlin.oltp.map.IdentityPipe;
import com.tinkerpop.gremlin.util.SingleIterator;
import com.tinkerpop.gremlin.util.optimizers.IdentityOptimizer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IdentityOptimizerTest {

    @Test
    public void shouldRemoveIdentityPipes() {
        Pipeline gremlin = Gremlin.of().identity().identity().identity();
        assertEquals(3, gremlin.getPipes().size());
        new IdentityOptimizer().optimize(gremlin);
        assertEquals(0, gremlin.getPipes().size());
    }

    @Test
    public void shouldNotRemoveAsNamedIdentityPipes() {
        Pipeline gremlin = Gremlin.of().identity().as("x").identity().identity().as("y");
        assertEquals(3, gremlin.getPipes().size());
        new IdentityOptimizer().optimize(gremlin);
        assertEquals(2, gremlin.getPipes().size());
        boolean foundX = false;
        int counter = 0;
        for (final Pipe pipe : (List<Pipe>) gremlin.getPipes()) {
            if (pipe instanceof IdentityPipe)
                assertTrue(!pipe.getAs().startsWith("_"));
            if (pipe.getAs().equals("x")) {
                assertFalse(foundX);
                foundX = true;
                counter++;
            } else if (pipe.getAs().equals("y")) {
                assertTrue(foundX);
                counter++;
            }
        }
        assertEquals(2, counter);
    }

    @Test
    public void shouldStillMaintainGlobalChain() {
        // NO OPTIMIZER
        Pipeline gremlin = Gremlin.of().identity().as("x").identity().identity().as("y");
        gremlin.addStarts(new SingleIterator<>(new SimpleHolder<>("marko")));
        assertTrue(gremlin.hasNext());
        assertEquals("marko", gremlin.next());
        assertFalse(gremlin.hasNext());

        // WITH OPTIMIZER
        gremlin = Gremlin.of().identity().as("x").identity().identity().as("y");
        new IdentityOptimizer().optimize(gremlin);
        gremlin.addStarts(new SingleIterator<>(new SimpleHolder<>("marko")));
        assertTrue(gremlin.hasNext());
        assertEquals("marko", gremlin.next());
        assertFalse(gremlin.hasNext());
    }
}
