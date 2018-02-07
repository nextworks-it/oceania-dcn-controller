package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Created by Marco Capitani on 20/12/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
@JsonDeserialize(using = NetSolBase.NetSolBaseDeserializer.class)
public abstract class NetSolBase {

    @JsonProperty("Method")
    public String method;

    @JsonProperty("Network_Allocation_ID")
    public String netAllocId;

    @JsonProperty("Status")
    public CompStatus status;

    @JsonIgnore
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
            try {
                ObjectCodec mapper = jsonParser.getCodec();
                JsonNode node = mapper.readTree(jsonParser);
                CompStatus status = mapper.treeToValue(node.get("Status"), CompStatus.class);
                if (status.equals(CompStatus.COMPUTED)) {
                String allocId = node.get("Network_Allocation_ID").asText();
                    String method;
                    method = node.get("Method").asText();
                    switch (method) {
                        case "FULL":
                            NetSolOutput solution = new NetSolOutput();
                            solution.method = method;
                            solution.status = status;
                            solution.netAllocId = allocId;
                            if ((status == CompStatus.COMPUTING) || (status == CompStatus.FAILED)) {
                                solution.matrix = null;
                            } else {
                                try {
                                    solution.matrix = mapper.treeToValue(node.get("Network_Allocation_Solution"), int[][].class);
                                } catch (NullPointerException exc) {
                                    // Ugly hack, should fix
                                    solution.matrix = mapper.treeToValue(node.get("Network_Allocation_Changes"), int[][].class);
                                }
                            }
                            return solution;
                        case "INCREMENTAL":
                            NetSolChanges changes = new NetSolChanges();
                            changes.method = method;
                            changes.status = status;
                            changes.netAllocId = allocId;
                            if ((status == CompStatus.COMPUTING) || (status == CompStatus.FAILED)) {
                                changes.changes = null;
                            } else {
                                changes.changes = mapper.treeToValue(node.get("Network_Allocation_Changes"), int[][].class);
                            }
                            return changes;
                        default:
                            throw new IllegalArgumentException(String.format("Unexpected method %s", method));
                    }
                } else {
                    NetSolOutput output = new NetSolOutput(); // at random, not important
                    output.method = null;
                    output.status = status;
                    output.matrix = null;
                    return output;
                }
            } catch (Exception e) {
                throw new JsonParseException(jsonParser, e.getMessage(), e);
            }

        }
    }
}
