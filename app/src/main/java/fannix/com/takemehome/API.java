package fannix.com.takemehome;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import io.nayuki.bitcoin.crypto.Ripemd160;
import kotlin.Pair;
import neowallet.Neowallet;
import neowallet.Wallet;
import network.o3.o3wallet.API.NEO.InvokeFunctionResponse;
import network.o3.o3wallet.API.NEO.NeoNodeRPC;
import network.o3.o3wallet.API.NEO.NodeResponse;
import network.o3.o3wallet.API.NEO.Stack;

import static com.github.salomonbrys.kotson.BuilderKt.jsonArray;
import static network.o3.o3wallet.CryptoExtensionsKt.byteArrayToHex;

/**
 * Created by mxf3306 on 23/02/2018.
 */

public class API {
    private String url;
    private String addr;
    public API(String url, String addr) {
        this.url = url;
        this.addr = addr;
    }

    private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * Neo stores String in hex format.
     * @param str
     */
    public static String toHex(String str) {
        StringBuffer sb = new StringBuffer();

        char [] arr = str.toCharArray();

        for(char ch: arr) {
            int i = (int) ch;
            int high = (i & 0xF0) >> 4;
            int low = (i & 0x0F);
            sb.append(HEX_CHARS[high]);
            sb.append(HEX_CHARS[low]);
        }

        return sb.toString();
    }

    public static String fromHex(String str) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < str.length(); i += 2) {
            String substr = str.substring(i, i+2);
            char ch = (char) Integer.parseInt(substr, 16);
            sb.append(ch);
        }

        return sb.toString();
    }

    public static String maskAddress(String scAddr, String beaconID)  {
        byte [] scAddrBytes = scAddr.toUpperCase().getBytes();
        byte [] beaconMask = byteArrayToHex(Ripemd160.getHash((beaconID + "0").getBytes())).getBytes();

        return mask(scAddrBytes, beaconMask);
    }

    public static String mask(byte[] original, byte[] mask) {

        String hex = "0123456789ABCDEF";

        StringBuffer sb = new StringBuffer();

        for (int i = 0, j = 0; i < original.length && j < mask.length; i++, j++) {
            int ch = hex.indexOf(original[i]) ^ hex.indexOf(mask[j]);
            sb.append(hex.charAt(ch));
        }

        for (int k = mask.length; k < original.length; k++) {
            sb.append((char)original[k]);
        }

        return sb.toString();
    }

    private InvokeFunctionResponse getInvokeFunctionResponse(String operation, JsonObject paramDict) {
        JsonObject obj = new JsonObject();
        obj.addProperty("jsonrpc", "2.0");
        obj.addProperty("method", NeoNodeRPC.RPC.INVOKEFUNCTION.methodName());

        ArrayList dataList = new ArrayList();
        dataList.add(this.addr);
        dataList.add(operation);

        ArrayList paramList = new ArrayList();
        if (paramDict != null) {
            paramList.add(paramDict);
        }

        dataList.add(jsonArray(paramList));

        obj.add("params", jsonArray(dataList));
        obj.addProperty("id", 3);

        System.out.println(obj.toString());

        String response = HttpRequest.post(this.url).send(obj.toString()).body();
        Gson gson = new Gson();
        System.out.println(response);
        NodeResponse nodeResponse = gson.fromJson(response, NodeResponse.class);
        return gson.fromJson(nodeResponse.getResult(), InvokeFunctionResponse.class);
    }

    public String getContractAddress(String beaconID) {
        JsonObject paramDict =  new JsonObject();
        paramDict.addProperty("type", "ByteArray");
        paramDict.addProperty("value", beaconID);

        InvokeFunctionResponse response = this.getInvokeFunctionResponse("get", paramDict);
        List<Stack> stack = response.getStack();
        if (stack.size() > 0) {
            String addr = stack.get(0).getValue();
            if (addr != "")
                return addr;
        }
        return null;
    }

    public String getEmail() {
        InvokeFunctionResponse response = this.getInvokeFunctionResponse("email", null);

        System.out.println(response);
        String s = response.getStack().get(0).getValue();
        return fromHex(s);
    }

    public String getUrl() {
        InvokeFunctionResponse response = this.getInvokeFunctionResponse("url", null);
        String s = response.getStack().get(0).getValue();
        return fromHex(s);
    }
}
