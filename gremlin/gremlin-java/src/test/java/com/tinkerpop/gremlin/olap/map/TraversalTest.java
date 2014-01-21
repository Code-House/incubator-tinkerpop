package com.tinkerpop.gremlin.olap.map;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.tinkergraph.TinkerFactory;
import com.tinkerpop.gremlin.Gremlin;
import com.tinkerpop.gremlin.olap.GremlinResult;
import com.tinkerpop.gremlin.test.ComplianceTest;
import org.junit.Test;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TraversalTest extends com.tinkerpop.gremlin.test.map.TraversalTest {

    final Graph g = TinkerFactory.createClassic();

    @Test
    public void testCompliance() {
        ComplianceTest.testCompliance(this.getClass());
    }

    @Test
    public void g_V() {
        super.g_V(new GremlinResult<>(g, () -> Gremlin.of().V()));
    }

    @Test
    public void g_v1_out() {
        super.g_v1_out(new GremlinResult<>(g, () -> Gremlin.of().v("1").out()));
    }

    @Test
    public void g_v2_in() {
        super.g_v2_in(new GremlinResult<>(g, () -> Gremlin.of().v("2").in()));
    }

    @Test
    public void g_v4_both() {
        super.g_v4_both(new GremlinResult<>(g, () -> Gremlin.of().v("4").both()));
    }

    @Test
    public void g_v1_outX1_knowsX_name() {
        super.g_v1_outX1_knowsX_name(new GremlinResult<>(g, () -> Gremlin.of().v("1").out(1, "knows").value("name")));
    }

    @Test
    public void g_V_bothX1_createdX_name() {
        super.g_V_bothX1_createdX_name(new GremlinResult<>(g, () -> Gremlin.of().V().both(1, "created").value("name")));
    }

    @Test
    public void g_E() {
        super.g_E(new GremlinResult<>(g, () -> Gremlin.of().E()));
    }

    @Test
    public void g_v1_outE() {
        super.g_v1_outE(new GremlinResult<>(g, () -> Gremlin.of().v("1").outE()));
    }

    @Test
    public void g_v2_inE() {
        super.g_v2_inE(new GremlinResult<>(g, () -> Gremlin.of().v("2").inE()));
    }

    @Test
    public void g_v4_bothE() {
        super.g_v4_bothE(new GremlinResult<>(g, () -> Gremlin.of().v("4").bothE()));
    }

    @Test
    public void g_v4_bothEX1_createdX() {
        super.g_v4_bothEX1_createdX(new GremlinResult<>(g, () -> Gremlin.of().v("4").bothE(1, "created")));
    }

    @Test
    public void g_V_inEX2_knowsX_outV_name() {
        super.g_V_inEX2_knowsX_outV_name(new GremlinResult<>(g, () -> Gremlin.of().V().inE(2, "knows").outV().value("name")));
    }

    @Test
    public void g_v1_outE_inV() {
        super.g_v1_outE_inV(new GremlinResult<>(g, () -> Gremlin.of().v("1").outE().inV()));
    }

    @Test
    public void g_v2_inE_outV() {
        super.g_v2_inE_outV(new GremlinResult<>(g, () -> Gremlin.of().v("2").inE().outV()));
    }

    @Test
    public void g_v1_outXknowsX() {
        super.g_v1_outXknowsX(new GremlinResult<>(g, () -> Gremlin.of().v("1").out("knows")));
    }

    @Test
    public void g_v1_outXknows_createdX() {
        super.g_v1_outXknows_createdX(new GremlinResult<>(g, () -> Gremlin.of().v("1").out("knows", "created")));
    }

    @Test
    public void g_v1_outEXknowsX_inV() {
        super.g_v1_outEXknowsX_inV(new GremlinResult<>(g, () -> Gremlin.of().v("1").outE("knows").inV()));
    }

    @Test
    public void g_v1_outEXknows_createdX_inV() {
        super.g_v1_outEXknows_createdX_inV(new GremlinResult<>(g, () -> Gremlin.of().v("1").outE("knows", "created").inV()));
    }

    @Test
    public void g_V_out_out() {
        super.g_V_out_out(new GremlinResult<>(g, () -> Gremlin.of().V().out().out()));
    }

    @Test
    public void g_v1_out_out_out() {
        super.g_v1_out_out_out(new GremlinResult<>(g, () -> Gremlin.of().v("1").out().out().out()));
    }

    @Test
    public void g_v1_out_propertyXnameX() {
        super.g_v1_out_propertyXnameX(new GremlinResult<>(g, () -> Gremlin.of().v("1").out().value("name")));
    }
}