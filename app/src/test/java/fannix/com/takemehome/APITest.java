package fannix.com.takemehome;

import org.junit.Assert;
import org.junit.Test;

import io.nayuki.bitcoin.crypto.Ripemd160;

import static com.github.salomonbrys.kotson.BuilderKt.jsonArray;
import static fannix.com.takemehome.API.fromHex;
import static fannix.com.takemehome.API.maskAddress;
import static network.o3.o3wallet.CryptoExtensionsKt.byteArrayToHex;

/**
 * Created by mxf3306 on 03/03/2018.
 */


public class APITest {

    public static final String URL = "http://seed2.neo.org:20332";
    public static final String SC_ADDR = "011ce07245481a06042f039407f6b7737e443e47".toUpperCase();
    public static final String REGISTRY_ADDR = "6478509f833cccbbc5a9f70e6d8183065b54b48f";
    public static final String BEACON_ID = "id1: 00000000-0000-0000-0000-000000000000 id2: 0 id3: 0";

    @Test
    public void testRegistry() {
        API api = new API(URL, REGISTRY_ADDR);

        String result = api.getContractAddress("70202E016B4FC400203AAE13CC40D7855A2A5EDF");

        result = API.maskAddress(result, BEACON_ID);

        Assert.assertEquals(SC_ADDR, result);
    }

    @Test
    public void testFromHex() {
        String result = fromHex("6438");
        Assert.assertEquals("d8", result);
    }

    @Test
    public void testGetEmail() {
        API api = new API(URL, SC_ADDR);

        String result = api.getEmail();

        Assert.assertEquals("doge@doge.com", result);
    }

    @Test
    public void testGetUrl() {
        API api = new API(URL, SC_ADDR);

        String result = api.getUrl();

        Assert.assertEquals("https://fannix.github.io/doge.html", result);
    }

    @Test
    public void testMask() {
        byte [] a = "0A".getBytes();
        byte [] b = "0A".getBytes();
        String res = API.mask(a, b);
        Assert.assertEquals("00", res);

        res = API.mask(res.getBytes(), b);
        Assert.assertEquals("0A", res);

        a = "0F".getBytes();
        b = "F0".getBytes();
        res = API.mask(a, b);
        Assert.assertEquals("FF", res);

        a = "0FE".getBytes();
        b = "F0".getBytes();
        res = API.mask(a, b);
        Assert.assertEquals("FFE", res);

        a = "0F".getBytes();
        b = "F0E".getBytes();
        res = API.mask(a, b);
        Assert.assertEquals("FF", res);

    }

    @Test
    public void testMaskAddress() {

        String maskAddr = maskAddress(SC_ADDR, BEACON_ID);

        String registryKey = byteArrayToHex(Ripemd160.getHash(BEACON_ID.getBytes()));
        System.out.println(registryKey);
        //70202E016B4FC400203AAE13CC40D7855A2A5EDF

        System.out.println(maskAddr);
        //DD21460AC14E185BDA33BB36B6C37263E391BDA6

        String originalAddr = maskAddress(maskAddr, BEACON_ID);

        Assert.assertEquals(SC_ADDR.toUpperCase(), originalAddr);
    }
}
