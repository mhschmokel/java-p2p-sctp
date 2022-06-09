import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.sun.nio.sctp.*;

public class ServerThread extends Thread {
    private SctpChannel sctpServer;
    private String serverAddress;
 
    public ServerThread(SctpChannel sctpServer, String serverAddress) {
        this.sctpServer = sctpServer;
        this.serverAddress = serverAddress;
    }
 
    public void run() {
        try {

            String command = "";
            String commandResult = "";

            ByteBuffer buf = ByteBuffer.allocateDirect(60);
            Charset charset = Charset.forName("ISO-8859-1");
            CharsetDecoder decoder = charset.newDecoder();

            MessageInfo messageInfo = null;
            
            messageInfo = sctpServer.receive(buf, System.out, null);


            String channelAddress = messageInfo.address().toString();
            channelAddress = channelAddress.substring(1, channelAddress.indexOf(":"));
            
            
            buf.flip();

            command = decoder.decode(buf).toString();

            System.out.println("> " + command);

            if (command.equals("exit")) {
                sctpServer.close();
                System.exit(0);
            }

            commandResult = runCommand(command);

            if (!serverAddress.equals(channelAddress)) {
                System.out.println("");
                System.out.println(commandResult);
                System.out.println("");
            }
            

            commandResult = "=====" + serverAddress + "=====\n" + commandResult;

            buf.clear();

            buf = ByteBuffer.wrap(commandResult.getBytes(charset));

            sctpServer.send(buf, messageInfo);
            
            buf.clear();

            sctpServer.close();

        } catch (Exception ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String runCommand(String command) {
        String cmdResult = "";

        try {
            Runtime rt = Runtime.getRuntime();
            //Process pr = rt.exec("cmd /c dir");
            Process pr = rt.exec(command);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line=null;

            while((line=input.readLine()) != null) {
                cmdResult += line + "\n";
            }

            pr.waitFor();
            return cmdResult;

        } catch(Exception e) {
            cmdResult = "Invalid command: " + e.getMessage();
            // System.out.println(e.toString());
            // e.printStackTrace();
        }

        return cmdResult;            
    }
}