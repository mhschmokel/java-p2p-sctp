public class Main {
	public static void main(String[] args) {
		IPAddressesGetter ipGetter = new IPAddressesGetter();
		P2P p2p = new P2P(6000, ipGetter);
		p2p.start();
		
	}
}
