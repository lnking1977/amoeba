package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.oracle.util.C04;
import com.meidusa.amoeba.oracle.util.C06;
import com.meidusa.amoeba.oracle.util.NetException;

/**
 * @author hexianmao
 * @version 2008-8-11 ����04:17:11
 */
public abstract class AnoService implements AnoServices {

    protected int             service;
    protected int             serviceSubPackets;
    protected long            version;
    protected byte[]          selectedDrivers;
    protected int             level;
    protected String[] listOfDrivers;

    public AnoService(){
        selectedDrivers = new byte[0];
        level = 0;
    }

    public void doRead(AnoPacketBuffer ano){
    	doReadService(ano);
        doReadVersion(ano);
    }
    
    public void doWrite(AnoPacketBuffer ano){
    	ano.writeUB2(service);
    	ano.writeUB2(serviceSubPackets);
    	ano.writeUB4(0);
    	ano.writeUB4(version);
    	ano.sendRaw(selectedDrivers);
    }
    
    void doReadService(AnoPacketBuffer ano) {
        service = ano.readUB2();
        serviceSubPackets = ano.readUB2();
        ano.readUB4();
    }

    void doReadVersion(AnoPacketBuffer ano) {
        version = ano.receiveVersion();
        selectedDrivers = ano.receiveRaw();
    }

    void init(AnoPacketBuffer ano) {
    	doReadService(ano);
    	doReadServiceStatus(ano);
        b();
    }

    void doReadServiceStatus(AnoPacketBuffer ano) {
    }

    void b() {
    }

    void selectDrivers(String as[], String as1[], int i1) throws NetException {
        label0: switch (i1) {
            case 0: // '\0'
                selectedDrivers = new byte[as.length + 1];
                selectedDrivers[0] = 0;
                int j1 = 0;
                do {
                    if (j1 >= as.length)
                        break label0;
                    if (!as[j1].equals(""))
                        selectedDrivers[j1 + 1] = k(as1, as[j1]);
                    j1++;
                } while (true);

            case 1: // '\001'
                selectedDrivers = new byte[1];
                selectedDrivers[0] = 0;
                break;

            case 2: // '\002'
                int k1 = 0;
                selectedDrivers = new byte[as.length + 1];
                for (k1 = 0; k1 < as.length; k1++)
                    if (!as[k1].equals(""))
                        selectedDrivers[k1] = k(as1, as[k1]);

                selectedDrivers[k1] = 0;
                break;

            case 3: // '\003'
                selectedDrivers = new byte[as.length];
                int l1 = 0;
                do {
                    if (l1 >= as.length)
                        break label0;
                    if (!as[l1].equals(""))
                        selectedDrivers[l1] = k(as1, as[l1]);
                    l1++;
                } while (true);

            default:
                throw new NetException(304);
        }
    }
    
    byte k(String as[], String s) throws NetException {
        for (byte byte0 = 0; byte0 < as.length; byte0++)
            if (s.equals(as[byte0]))
                return byte0;

        throw new NetException(309);
    }
    
    void c() {
    }
}

class SupervisorService extends AnoService {

    protected byte[] l;
    protected int[]  h;
    protected int    status;
    protected int[]  r;

    private int      p = 0;
    private int      q = 2;

    @Override
    void doReadVersion(AnoPacketBuffer ano) {
        version = ano.receiveVersion();
        l = ano.receiveRaw();
        h = ano.receiveUB2Array();
    }

    @Override
    void doReadServiceStatus(AnoPacketBuffer ano) {
        version = ano.receiveVersion();
        status = ano.receiveStatus();
        if (status != 31) {
            throw new RuntimeException("Supervisor service received status failure");
        } else {
            r = ano.receiveUB2Array();
        }
    }

    @Override
    void b() {
        for (int i1 = 0; i1 < r.length; i1++) {
            int j1 = 0;
            do {
                if (j1 >= h.length)
                    break;
                if (r[i1] == h[j1]) {
                    p++;
                    break;
                }
                j1++;
            } while (true);
            if (j1 == h.length)
                throw new RuntimeException("Received Invalid Services from Server ");
        }

        if (p != q)
            throw new RuntimeException("Received Incomplete services from server");
        else
            return;
    }

}

class AuthenticationService extends AnoService {

	
    protected int      const1;
    protected int      status;
    protected String[] selectedDriversDesc;
    protected boolean  l;
	

    public AuthenticationService(){
    	service = 1;
        serviceSubPackets = 3;
        level = 3;
        selectDrivers(listOfDrivers, AnoServices.AUTH_CLASSNAME, level);
        serviceSubPackets += selectedDrivers.length * 2;
    }
    
    @Override
    void doReadVersion(AnoPacketBuffer ano) {
        version = ano.receiveVersion();
        const1 = ano.receiveUB2();
        status = ano.receiveStatus();

        selectedDriversDesc = new String[selectedDrivers.length];
        for (int i = 0; i < selectedDrivers.length; i++) {
            selectedDrivers[i] = (byte) ano.receiveUB1();
            selectedDriversDesc[i] = ano.receiveString();
        }
    }

    @Override
    void doReadServiceStatus(AnoPacketBuffer ano) {
    	int numSubPackets = ano.readUB2();
        if (numSubPackets != 2) {
            throw new RuntimeException("Wrong Number of service subpackets");
        }
        version = ano.receiveVersion();
        status = ano.receiveStatus();
        if (status == 64255) {
            for (int i = 0; i < (numSubPackets - 2) / 2; i++) {
                ano.receiveUB1();
                ano.receiveString();
                l = true;
            }
        } else if (status == 64511) {
            l = false;
        } else {
            throw new RuntimeException("Authentication service received status failure");
        }
    }

    @Override
    void b() {
    }
}

class EncryptionService extends AnoService {

    protected short   algID;
    protected boolean isActive; // c
    protected int     a;

    @Override
    void doReadServiceStatus(AnoPacketBuffer ano) {
    	int numSubPackets = ano.readUB2();
        if (numSubPackets != serviceSubPackets) {
            throw new RuntimeException("Wrong Number of service subpackets");
        } else {
            version = ano.receiveVersion();
            algID = ano.receiveUB1();
            isActive = algID > 0;
        }
    }

    @Override
    void b() {
        int i = 0;
        do {
            if (i >= selectedDrivers.length)
                break;
            if (selectedDrivers[i] == algID) {
                a = i;
                break;
            }
            i++;
        } while (true);
        if (i == selectedDrivers.length)
            throw new RuntimeException("Invalid Encryption Algorithm from server");
        else
            return;
    }

}

class DataIntegrityService extends AnoService {

    protected short   h;
    protected byte[]  clientPK;  // j
    protected byte[]  iv;
    protected byte[]  sessionKey;

    protected boolean isActive;  // g
    protected int     k;

    protected C06     e;

    public DataIntegrityService(){
        isActive = false;
    }

    @Override
    void doReadServiceStatus(AnoPacketBuffer ano) {
        version = ano.receiveVersion();
        h = ano.receiveUB1();
        int numSubPackets = ano.readUB2();
        if (numSubPackets != serviceSubPackets && numSubPackets == 8) {
            short word0 = (short) ano.receiveUB2();
            short word1 = (short) ano.receiveUB2();
            byte abyte0[] = ano.receiveRaw();
            byte abyte1[] = ano.receiveRaw();
            byte abyte2[] = ano.receiveRaw();
            byte abyte3[] = ano.receiveRaw();
            if (word0 <= 0 || word1 <= 0)
                throw new RuntimeException("Bad parameters from server");
            int l = (word1 + 7) / 8;
            if (abyte2.length != l || abyte1.length != l)
                throw new RuntimeException("DiffieHellman negotiation out of synch");
            C04 c04 = new C04(abyte0, abyte1, word0, word1);
            clientPK = c04.g();
            iv = abyte3;
            sessionKey = c04.a(abyte2, abyte2.length);
        }
        isActive = h > 0;
    }

    @Override
    void b() {
        int l = 0;
        do {
            if (l >= selectedDrivers.length)
                break;
            if (selectedDrivers[l] == h) {
                k = l;
                break;
            }
            l++;
        } while (true);
        if (l == selectedDrivers.length)
            throw new RuntimeException("Invalid DataIntegrity Algorithm received from server");
        else
            return;
    }

    void c() {
        if (isActive) {
            try {
                String pkgPrefix = "com.meidusa.amoeba.oracle.util.";
                e = (C06) Class.forName(pkgPrefix + DATAINTEGRITY_CLASSNAME[h]).newInstance();
            } catch (Exception exception) {
                throw new RuntimeException("Data Integrity Class not installed");
            }
            e.init(sessionKey, iv);
        }
        if (clientPK != null) {
            int l = 13 + 8 + 4 + clientPK.length;
            sendANOHeader(l, 1, (short) 0);
            serviceSubPackets = 1;
            /*doReadService();
            ano.sendRaw(clientPK);*/
        }
    }

    private void sendANOHeader(int i, int j, short word) {
        /*ano.writeUB4(NA_MAGIC);
        ano.writeUB2(i);
        ano.writeVersion();
        ano.writeUB2(j);
        ano.writeUB1(word);*/
    }

}
