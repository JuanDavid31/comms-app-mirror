package com.rallytac.engageandroid.legba.data.engagedto;

public class EngageClasses {

    public static class TxData{

        public final static int AUDIO_TYPE = 1;
        public final static int PRESENCE_TYPE = 2;

        String id;
        String name;
        int type;
        AddressAndPort rx;
        AddressAndPort tx;
        TxAudio txAudio;


        public TxData(String id, String name, int type, AddressAndPort rx, AddressAndPort tx, TxAudio txAudio) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.rx = rx;
            this.tx = tx;
            this.txAudio = txAudio;

            if(type == PRESENCE_TYPE){

            }
        }
    }

    public static class AddressAndPort {
        String address;
        int port;

        public AddressAndPort(String address, int port) {
            this.address = address;
            this.port = port;
        }
    }

    public static class TxAudio {//{"encoder":25,"framingMs":60,"noHdrExt":false,"fdx":false,"maxTxSecs":120}
        int encoder;
        int framingMs;
        boolean noHdrExt;
        boolean fdx;
        int maxTxSecs;

        public TxAudio(int encoder, int framingMs,boolean fullDuplex, int maxTxSecs) {
            this.encoder = encoder;
            this.framingMs = framingMs;
            this.fdx = fullDuplex;
            this.maxTxSecs = maxTxSecs;
        }
    }

    public static class PresenceTxData{
        String id;
        String name;
        int type = 2;
        AddressAndPort rx = new AddressAndPort("", 49000);
        AddressAndPort tx = new AddressAndPort("239.42.43.1", 49000);
        TxAudio txAudio = new TxAudio(25, 60, false, 120);

        public PresenceTxData(String id, String name) {
            this.id = id;
            this.name = name;
        }

        /*"presence":{
            "format":1,
                    "intervalSecs":30,
                    "forceOnAudioTransmit":false,
                    "listenOnly":false
        }*/

        /*class Presence{
            int format = 1;
            int intervalSecs = 30;
            boolean forceOnAudioTransmit = false;
            boolean listenOnly = false;
        }*/
    }

    public static class PresenceDescriptor{

        Identity identity;

        public PresenceDescriptor(String nodeId, String userId, String displayName) {
            identity = new Identity(nodeId, userId, displayName);
        }

        class Identity{ // {"nodeId":"{e24b92dc-0531-483a-b1a4-1ee3ec3da364}","userId":"mb","displayName":"Mb","avatar":""}
            String nodeId;
            String userId;
            String displayName;
            String avatar = "";

            public Identity(String nodeId, String userId, String displayName) {
                this.nodeId = nodeId;
                this.userId = userId;
                this.displayName = displayName;
            }
        }
    }
}
