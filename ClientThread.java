import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import com.sun.nio.sctp.*;

public class ClientThread extends Thread {
    private InetSocketAddress inetSocketAddress;
    private String msg;
 
    public ClientThread(InetSocketAddress inetSocketAddress, String msg) {
        this.inetSocketAddress = inetSocketAddress;
        this.msg = msg;
    }

    public void run() {
        
        try {
            SctpChannel sc = SctpChannel.open();
            sc.connect(inetSocketAddress);

            if (!sc.isOpen()) {
                return;
            }

            ByteBuffer buf = ByteBuffer.allocateDirect(60);
            ByteBuffer bufRes = ByteBuffer.allocateDirect(2048);
            CharBuffer cbuf = CharBuffer.allocate(60);
            Charset charset = Charset.forName("ISO-8859-1");
            CharsetEncoder encoder = charset.newEncoder();
            CharsetDecoder decoder = charset.newDecoder();

            String commandResult = "";

            encoder.encode(cbuf, buf, true);
            buf.flip();

            buf = ByteBuffer.wrap(msg.getBytes(charset));
            
            MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);

            sc.send(buf, messageInfo);

            if (msg.equals("exit")) {
                sc.close();
                System.exit(0);
            }

            sc.receive(bufRes, System.out, null);

            bufRes.flip();

            commandResult = decoder.decode(bufRes).toString();            

            System.out.println(commandResult);

            sc.close();
        } catch (Exception e) {
            System.out.println("Client thread: " + e.getMessage());
        }

    }

}
