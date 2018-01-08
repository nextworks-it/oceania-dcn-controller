package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import it.nextworks.nephele.OFAAService.ODLInventory.Const;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Marco Capitani on 20/12/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
@JsonDeserialize(using = NetSolBase.NetSolBaseDeserializer.class)
public abstract class NetSolBase {

    public String method;

    @JsonProperty("Network_Allocation_ID") // TODO change
    public String netAllocId;

    @JsonProperty("Status")  // TODO change
    public CompStatus status;

    public abstract int[][] getResult();

    public static class NetSolBaseDeserializer extends StdDeserializer<NetSolBase> {

        public NetSolBaseDeserializer() {
            this(null);
        }

        protected NetSolBaseDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public NetSolBase deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            ObjectCodec mapper = jsonParser.getCodec();
            JsonNode node = mapper.readTree(jsonParser);
            String allocId = node.get("Network_Allocation_ID").asText();  // TODO change
            CompStatus status = CompStatus.valueOf(node.get("Status").asText());  // TODO change
            String method;
            if (!node.has("method")) {
                method = "FULL";
            } else {
                method = node.get("method").asText();
            }
            switch (method) {
                case "FULL":
                    NetSolOutput solution = new NetSolOutput();
                    solution.method = method;
                    solution.status = status;
                    solution.netAllocId = allocId;
                    solution.matrix = mapper.treeToValue(node.get("Network_Allocation_Solution"), int[][].class);
                    return solution; // TODO test.
                    /*
                    NetSolOutput out = new NetSolOutput();
                    out.method = method;
                    out.netAllocId = allocId;
                    out.status = status;
                    JsonNode sol = node.get("Network_Allocation_Solution");
                    int[][] matrix = new int[Const.I * Const.T][Const.P * Const.W * Const.Z];
                    Iterator<JsonNode> elements = sol.elements();
                    int i = 0;
                    while (elements.hasNext()) {
                        Iterator<JsonNode> elements2 = elements.next().elements();
                        int j = 0;
                        while (elements2.hasNext()) {
                            matrix[i][j] = elements2.next().asInt();
                            j++;
                        }
                        i++;
                    }
                    out.matrix = matrix;
                    return out;
                    */
                case "INCREMENTAL":
                    NetSolChanges changes = new NetSolChanges();
                    changes.method = method;
                    changes.status = status;
                    changes.netAllocId = allocId;
                    changes.changes = mapper.treeToValue(node.get("Network_Allocation_Solution"), int[][].class);
                    // TODO wrong...
                    return changes; // TODO test.
                    /*
                    NetSolChanges changes = new NetSolChanges();
                    changes.method = method;
                    changes.netAllocId = allocId;
                    changes.status = status;
                    JsonNode changesNode = node.get("Network_Allocation_Solution");
                    List<int[]> changesList = new ArrayList<>();
                    Iterator<JsonNode> changesIterator = changesNode.elements();
                    while (changesIterator.hasNext()) {
                        Iterator<JsonNode> elements2 = changesIterator.next().elements();
                        int j = 0;
                        int[] change = new int[3];
                        while (elements2.hasNext()) {
                            change[j] = elements2.next().asInt();
                            j++;
                        }
                        changesList.add(change);
                    }
                    int changesNum = changesList.size();
                    int[][] changesMatrix = new int[changesNum][3];
                    int index = 0;
                    for (int[] change : changesList) {
                        changesMatrix[index] = change;
                        index++;
                    }
                    changes.changes = changesMatrix;
                    return changes;
                    */
                default:
                    throw new IllegalArgumentException(String.format("Unexpected method %s", method));
            }
        }
    }
}
