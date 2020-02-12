package scripts;

import utils.marketUtils;

import java.io.IOException;


public class script1 {


    public static void main(String[] args) throws IOException {
        marketUtils Utils = new marketUtils();
        Utils.processNiftyData();
        Utils.performOHLStrategy();
    }

}
