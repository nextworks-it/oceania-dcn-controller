package it.nextworks.nephele.appaffdb;

/**
 * Created by Marco Capitani on 13/09/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class ExtConnection {

    public int srcSrv;

    public int dstPod;

    public int dstTor;

    public int bandwidth;

    public String dstIp;

    public int getSource() {
        return srcSrv;
    }

    public ExtConnection(int srcSrv, int dstPod, int dstTor, int bandwidth, String dstIp) {
        this.srcSrv = srcSrv;
        this.dstPod = dstPod;
        this.dstTor = dstTor;
        this.bandwidth = bandwidth;
        this.dstIp = dstIp;
    }
}
