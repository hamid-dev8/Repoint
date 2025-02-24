package com.repoint.basics.logic;
import org.web3j.codegen.*;
import org.web3j.utils.Strings;

public class GenerateERC20 {
    public static void main(String[] args) {
        // Paths to ABI and BIN files
        String abiFilePath = "/home/hamidreza/AndroidStudioProjects/Repoint/output/ERC20.abi";
        String binFilePath = "/home/hamidreza/AndroidStudioProjects/Repoint/output/ERC20.bin";

        // Output directory where the ERC20.java will be generated
        String outputDirectory = "/home/hamidreza/AndroidStudioProjects/Repoint/src/main/java/";

        // Java package where ERC20.java should be placed
        String packageName = "com.repoint.basics.logic";

        try {
            SolidityFunctionWrapperGenerator.main(new String[]{
                    "-a", abiFilePath,
                    "-b", binFilePath,
                    "-o", outputDirectory,
                    "-p", packageName
            });
            System.out.println("✅ ERC20.java generated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error generating ERC20.java");
        }
    }
}