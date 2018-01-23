package it.nextworks.nephele.appaffdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco Capitani on 13/09/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class ExtConnection {

    public int srcPod;

    public int srcTor;

    public int srcSrv;

    public int dstPod;

    public int dstTor;

    public int bandwidth;

    public String dstIp;

    public int getSourceServer() {
        return srcSrv;
    }

    public List<Integer> getTor() {
        ArrayList<Integer> output = new ArrayList<>();
        output.add(srcPod);
        output.add(srcTor);
        return output;
    }

    public ExtConnection(int srcPod, int srcTor, int srcSrv, int dstPod, int dstTor, int bandwidth, String dstIp) {
        this.srcPod = srcPod;
        this.srcTor = srcTor;
        this.srcSrv = srcSrv;
        this.dstPod = dstPod;
        this.dstTor = dstTor;
        this.bandwidth = bandwidth;
        this.dstIp = dstIp;
    }

    @Override
    public String toString() {
        return "ExtConnection{" +
                "srcPod=" + String.valueOf(srcPod) +
                ", srcTor=" + String.valueOf(srcTor) +
                ", srcSrv=" + String.valueOf(srcSrv) +
                ", dstPod=" + String.valueOf(dstPod) +
                ", dstTor=" + String.valueOf(dstTor) +
                ", bandwidth=" + String.valueOf(bandwidth) +
                ", dstIp='" + dstIp + '\'' +
                '}';
    }
}
