import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.sun.nio.sctp.*;

public class P2P {
	private IPAddressesGetter ipGetter;
	private Thread senderThread;
	private Thread receiverThread;
	private int port;
	private Scanner s = new Scanner(System.in).useDelimiter("\n");
	private String cmdReturn = "";

	ArrayList<InetSocketAddress> aHostsList = new ArrayList<InetSocketAddress>();
	ArrayList<String> ipsAddresses;

	public P2P(int port, IPAddressesGetter ipGetter) {
		this.port = port;
		this.ipGetter = ipGetter;
	}

	public void start() {

		try {
			buidIPsList();

			startReceiver();

			startSender();
		} catch (Exception e) {
			System.out.println("Start error" + e.getMessage());
		}

	}

	private void buidIPsList() {
		try {
			ipsAddresses = ipGetter.getIPsFromFile();

			for (String ip : ipsAddresses) {
				aHostsList.add(new InetSocketAddress(ip, 6000));
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void sendPacket(String message, InetAddress aHost) {
		DatagramPacket packet;

		byte data[] = message.getBytes(StandardCharsets.UTF_8);

		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);

			packet = new DatagramPacket(data, message.length(), aHost, port);

			socket.send(packet);

		} catch (Exception e) {
			System.out.println("Sender: " + e.getMessage());
			socket.close();
		}

	}

	private void startSender() throws UnknownHostException {
		senderThread = new Thread() {
			@Override
			public void run() {
				try {

					while(true) {
						String msg = s.next();

						for (InetSocketAddress address : aHostsList) {
							new ClientThread(address, msg).start();
						}

					}

				} catch (Exception e) {
					System.out.println("Sender: " + e.getMessage());
				}					
			}
		};
		senderThread.start();

	}

	private void runCommand(String command, InetAddress sender) {
		if (command.contains("/result")) {
			command = command.replace("/result", "");

			System.out.println("----- MACHINE IP: " + sender.getHostAddress() + " -----");
			System.out.println(command);
			System.out.println();

			return;
		}

		String result = "";
		cmdReturn = "";

		System.out.println("> " + command);
		try {
			ProcessBuilder builder = new ProcessBuilder().command(command.split(" "));
			builder.redirectErrorStream(true);

			Process proc = builder.start();
			final BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			new Thread() {
				@Override
				public void run() {
					String result = "";
					try {
						cmdReturn = bufferedreader.lines().collect(Collectors.joining("\n"));
					} catch (Exception e) {
						System.out.println("Error reading command result");
						cmdReturn = "Error";
					}
					System.out.println(cmdReturn);
					
					result = "/result" + cmdReturn;
					sendPacket(result, sender);
				}
			}.start();

			proc.waitFor();

		} catch (Exception ex) {
			cmdReturn = "Invalid command: " + command + "\n" + ex.getMessage();
			System.out.println(cmdReturn);
			result = "/result" + cmdReturn;
			sendPacket(result, sender);
		}
	}

	private void startReceiver() {
		receiverThread = new Thread() {
			@Override
			public void run() {

				try {
					SctpServerChannel ssc = SctpServerChannel.open();
					InetSocketAddress serverAddr = new InetSocketAddress(6000);
					ssc.bind(serverAddr, 4);

					String serverAddress = getLocalAddress();

					while(true) {
						SctpChannel sc = ssc.accept();

						new ServerThread(sc, serverAddress).start();
					}

				} catch (Exception e) {
					System.out.println("Receiver: " + e.getMessage());
				}
			}
		};
		receiverThread.start();
	}

	private String getLocalAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while(addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getHostAddress();
					if(ip.contains("192.168.")) {
						break;
					}
					//System.out.println(iface.getDisplayName() + " " + ip);
				}
				
			}
			return ip;
		} catch (SocketException e) {
			return "";
		}
		
	}
	
}
