package fi.thl.pivot.model;

import java.util.Map;

import org.mockito.Mockito;

import com.google.common.collect.Maps;

import fi.thl.pivot.model.DimensionNode;

final class ModelTestUtil {

    private static Map<String, Integer> surrogateIds = Maps.newHashMap();
    private static int sequence;

    static DimensionNode mockNode(String label) {
        return mockNode(label, mockDimension("MOCK"));
    }

    static DimensionNode mockNode(String label, Dimension d1) {

        DimensionNode n1 = Mockito.mock(DimensionNode.class);
        Mockito.when(n1.getLabel()).thenReturn(Label.create("fi", label));
        Mockito.when(n1.getId()).thenReturn(label);
        Mockito.when(n1.getDimension()).thenReturn(d1);
        Mockito.when(n1.toString()).thenReturn(label);
        Mockito.when(n1.getSurrogateId()).thenReturn(generateSurrogateId(label));
        return n1;
    }

    static Dimension mockDimension(String label) {
        Dimension d1 = Mockito.mock(Dimension.class);
        Mockito.when(d1.getId()).thenReturn("MOCK");
        return d1;
    }

    private static int generateSurrogateId(String label) {
        int id;
        if (surrogateIds.containsKey(label)) {
            id = surrogateIds.get(label);
        } else {
            id = ++sequence;
            surrogateIds.put(label, id);
        }
        return id;
    }
}
